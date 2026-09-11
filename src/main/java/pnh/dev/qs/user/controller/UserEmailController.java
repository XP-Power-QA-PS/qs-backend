package pnh.dev.qs.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pnh.dev.qs.user.dto.EmailSendResultDTO;
import pnh.dev.qs.user.dto.MeetingEmailRequest;
import pnh.dev.qs.user.dto.RecipientUserDTO;
import pnh.dev.qs.user.entity.Role;
import pnh.dev.qs.user.entity.UserAccount;
import pnh.dev.qs.user.repository.UserAccountRepository;
import pnh.dev.qs.user.service.UserEmailService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/emails")
@RequiredArgsConstructor
public class UserEmailController {

    private final UserEmailService userEmailService;
    private final UserAccountRepository userAccountRepository;

    /**
     * API to search and retrieve active internal users for recipient selection in email forms
     */
    @GetMapping("/recipients")
    public ResponseEntity<List<RecipientUserDTO>> getAvailableRecipients(
            @RequestParam(required = false, defaultValue = "") String keyword) {

        List<UserAccount> users = userAccountRepository.searchActiveRecipients(keyword.trim());

        List<RecipientUserDTO> dtoList = users.stream().map(u -> {
            String fullName = "";
            if (u.getProfile() != null) {
                String first = u.getProfile().getFirstName() != null ? u.getProfile().getFirstName() : "";
                String last = u.getProfile().getLastName() != null ? u.getProfile().getLastName() : "";
                fullName = (first + " " + last).trim();
            }
            if (fullName.isBlank()) {
                fullName = u.getUsername();
            }

            List<String> roleNames = u.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            // Determine department/role label
            String dept = roleNames.contains("ROLE_ADMIN") ? "Admin" : "Quality";

            return RecipientUserDTO.builder()
                    .id(u.getId())
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .fullName(fullName)
                    .roles(roleNames)
                    .department(dept)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    /**
     * API to send meeting invitation email to selected recipients.
     * Automatically captures the logged-in user as the Organizer if not provided.
     */
    @PostMapping("/meeting-invite")
    public ResponseEntity<EmailSendResultDTO> sendMeetingInvitation(
            @Valid @RequestBody MeetingEmailRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails instanceof UserAccount user) {
            if (request.getOrganizerEmail() == null || request.getOrganizerEmail().isBlank()) {
                request.setOrganizerEmail(user.getEmail());
            }
            if (request.getOrganizerName() == null || request.getOrganizerName().isBlank()) {
                String name = "";
                if (user.getProfile() != null) {
                    String first = user.getProfile().getFirstName() != null ? user.getProfile().getFirstName() : "";
                    String last = user.getProfile().getLastName() != null ? user.getProfile().getLastName() : "";
                    name = (first + " " + last).trim();
                }
                request.setOrganizerName(!name.isBlank() ? name : user.getUsername());
            }
        }

        EmailSendResultDTO result = userEmailService.sendMeetingInvitation(request);
        return ResponseEntity.ok(result);
    }

    /**
     * API to test email connectivity (Admin/Dev)
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam String toEmail) {
        userEmailService.sendHtmlEmail(
                new String[]{toEmail},
                null,
                "[QS System] Test Email Connection",
                "<h2 style='color:#006194;'>Kết nối Email SMTP thành công!</h2>"
                        + "<p>Hệ thống QS đã cấu hình dịch vụ gửi email thành công.</p>",
                null, null, null
        );

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Đã gửi email kiểm tra thành công tới: " + toEmail);
        return ResponseEntity.ok(resp);
    }
}
