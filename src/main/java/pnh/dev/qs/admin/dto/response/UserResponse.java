package pnh.dev.qs.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
public class UserResponse {
    
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String username;
    private String email;
    private boolean isEnabled;
    private Instant lastLoginAt;
    private Set<String> roles;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String avatarUrl;
    private Instant createdAt;
}
