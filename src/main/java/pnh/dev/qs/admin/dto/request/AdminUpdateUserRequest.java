package pnh.dev.qs.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class AdminUpdateUserRequest {

    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    @Email(message = "Email format is not valid")
    private String email;

    private String password;

    private Boolean isEnabled;

    private Set<String> roles;
}
