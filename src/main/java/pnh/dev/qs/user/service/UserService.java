package pnh.dev.qs.user.service;

import pnh.dev.qs.user.dto.UserProfileDTO;
import pnh.dev.qs.user.dto.UserProfileUpdateRequest;

public interface UserService {
    UserProfileDTO getCurrentUserProfile(Long userId);
    UserProfileDTO updateProfile(Long userId, UserProfileUpdateRequest request);
}
