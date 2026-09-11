package pnh.dev.qs.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRecipientDTO {

    private Long userId;

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String name;

    private String department;

    @Builder.Default
    private String recipientType = "TO"; // "TO" or "CC"
}
