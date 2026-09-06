package pnh.dev.qs.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pnh.dev.qs.user.dto.UserProfileDTO;
import pnh.dev.qs.user.dto.UserProfileUpdateRequest;
import pnh.dev.qs.user.entity.UserAccount;
import pnh.dev.qs.user.service.UserManagementService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserAccount user = (UserAccount) userDetails;
        return ResponseEntity.ok(userManagementService.getCurrentUserProfile(user.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                        @Valid @RequestBody UserProfileUpdateRequest request) {
        UserAccount user = (UserAccount) userDetails;
        return ResponseEntity.ok(userManagementService.updateProfile(user.getId(), request));
    }
}
