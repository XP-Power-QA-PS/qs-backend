package pnh.dev.qs.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class AdminUpdateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is not valid")
    private String email;

    private String password;

    private Boolean isEnabled;

    private Set<String> roles;
}
