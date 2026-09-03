package pnh.dev.qs.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.exception.custom.ResourceNotFoundException;
import pnh.dev.qs.user.dto.UserProfileDTO;
import pnh.dev.qs.user.dto.UserProfileUpdateRequest;
import pnh.dev.qs.user.entity.UserAccount;
import pnh.dev.qs.user.entity.UserProfile;
import pnh.dev.qs.user.repository.UserAccountRepository;
import pnh.dev.qs.user.repository.UserProfileRepository;
import pnh.dev.qs.user.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getCurrentUserProfile(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDTO(user);
    }

    @Override
    @Transactional
    public UserProfileDTO updateProfile(Long userId, UserProfileUpdateRequest request) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserAccount(user);
            user.setProfile(profile);
        }

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) profile.setPhoneNumber(request.getPhoneNumber());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) profile.setGender(request.getGender());
        if (request.getBio() != null) profile.setBio(request.getBio());

        userProfileRepository.save(profile);

        return mapToDTO(user);
    }

    private UserProfileDTO mapToDTO(UserAccount user) {
        UserProfile profile = user.getProfile();
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(profile != null ? profile.getFirstName() : null)
                .lastName(profile != null ? profile.getLastName() : null)
                .phoneNumber(profile != null ? profile.getPhoneNumber() : null)
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .dateOfBirth(profile != null ? profile.getDateOfBirth() : null)
                .gender(profile != null ? profile.getGender() : null)
                .bio(profile != null ? profile.getBio() : null)
                .build();
    }
}
