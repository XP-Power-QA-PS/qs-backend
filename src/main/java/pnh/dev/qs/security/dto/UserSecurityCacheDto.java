package pnh.dev.qs.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSecurityCacheDto {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private boolean isEnabled;
    private Set<String> roles;
    private String firstName;
    private String lastName;
}
