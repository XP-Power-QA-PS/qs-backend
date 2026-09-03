package pnh.dev.qs.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import pnh.dev.qs.user.entity.Gender;

import java.time.LocalDate;

@Data
public class UserProfileUpdateRequest {
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;

    private LocalDate dateOfBirth;
    
    private Gender gender;
    
    private String bio;
}
