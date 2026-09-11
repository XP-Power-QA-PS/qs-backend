package pnh.dev.qs.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipientUserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;
    private String department;
}
