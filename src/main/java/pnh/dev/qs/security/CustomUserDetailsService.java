package pnh.dev.qs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.security.dto.UserSecurityCacheDto;
import pnh.dev.qs.user.entity.Role;
import pnh.dev.qs.user.entity.UserAccount;
import pnh.dev.qs.user.entity.UserProfile;
import pnh.dev.qs.user.repository.UserAccountRepository;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private static final String USER_CACHE_PREFIX = "user_cache:details:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    private final UserAccountRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public @NonNull UserDetails loadUserByUsername(@NonNull String usernameOrEmail) throws UsernameNotFoundException {
        return userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail)));
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        String cacheKey = USER_CACHE_PREFIX + id;

        // 1. Try reading from L2 Redis Cache
        try {
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);
            if (cachedJson != null && !cachedJson.isBlank()) {
                UserSecurityCacheDto dto = objectMapper.readValue(cachedJson, UserSecurityCacheDto.class);
                return mapDtoToUserAccount(dto);
            }
        } catch (Exception e) {
            log.warn("Redis L2 Cache read failed for userId {}: {}. Falling back to database.", id, e.getMessage());
        }

        // 2. Cache Miss or Redis Error: Query Database
        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        // 3. Write to L2 Redis Cache
        try {
            UserSecurityCacheDto dto = mapUserAccountToDto(user);
            String json = objectMapper.writeValueAsString(dto);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL);
        } catch (Exception e) {
            log.warn("Redis L2 Cache write failed for userId {}: {}", id, e.getMessage());
        }

        return user;
    }

    public void evictUserCache(Long userId) {
        if (userId == null) return;
        try {
            redisTemplate.delete(USER_CACHE_PREFIX + userId);
            log.debug("Evicted L2 user cache for userId: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to evict L2 user cache for userId {}: {}", userId, e.getMessage());
        }
    }

    private UserAccount mapDtoToUserAccount(UserSecurityCacheDto dto) {
        UserAccount user = new UserAccount();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(dto.getPasswordHash());
        user.setEnabled(dto.isEnabled());

        if (dto.getRoles() != null) {
            Set<Role> roles = dto.getRoles().stream().map(roleName -> {
                Role r = new Role();
                r.setName(roleName);
                return r;
            }).collect(Collectors.toSet());
            user.setRoles(roles);
        }

        if (dto.getFullName() != null) {
            UserProfile profile = new UserProfile();
            profile.setFullName(dto.getFullName());
            user.setProfile(profile);
        }

        return user;
    }

    private UserSecurityCacheDto mapUserAccountToDto(UserAccount user) {
        Set<String> roles = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                : Collections.emptySet();

        String fullName = user.getProfile() != null ? user.getProfile().getFullName() : null;

        return UserSecurityCacheDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .isEnabled(user.isEnabled())
                .roles(roles)
                .fullName(fullName)
                .build();
    }
}
