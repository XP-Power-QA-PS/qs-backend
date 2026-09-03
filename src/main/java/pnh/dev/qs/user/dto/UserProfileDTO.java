package pnh.dev.qs.user.dto;

import lombok.Builder;
import lombok.Data;
import pnh.dev.qs.user.entity.Gender;

import java.time.LocalDate;

@Data
@Builder
public class UserProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String bio;
}
