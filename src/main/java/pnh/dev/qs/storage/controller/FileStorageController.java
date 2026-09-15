package pnh.dev.qs.storage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pnh.dev.qs.storage.dto.FileCommitRequest;
import pnh.dev.qs.storage.dto.FileCommitResponse;
import pnh.dev.qs.storage.dto.PresignedUploadRequest;
import pnh.dev.qs.storage.dto.PresignedUploadResponse;
import pnh.dev.qs.storage.service.StorageService;
import pnh.dev.qs.user.entity.UserAccount;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileStorageController {

    private final StorageService storageService;

    /**
     * Xin presigned PUT URL để frontend đẩy file trực tiếp lên thư mục tmp/ của MinIO
     */
    @PostMapping("/presigned-upload")
    public ResponseEntity<PresignedUploadResponse> getPresignedUploadUrl(
            @Valid @RequestBody PresignedUploadRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = (userDetails instanceof UserAccount user) ? user.getId() : null;
        PresignedUploadResponse response = storageService.generatePresignedUploadUrl(request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Xác nhận và chuyển file từ tmp/ sang thư mục lưu trữ chính thức
     */
    @PostMapping("/commit")
    public ResponseEntity<FileCommitResponse> commitFile(
            @Valid @RequestBody FileCommitRequest request) {
        FileCommitResponse response = storageService.commitFile(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy link tải bảo mật (Presigned GET URL) cho các tài liệu nội bộ
     */
    @GetMapping("/presigned-download")
    public ResponseEntity<Map<String, String>> getPresignedDownloadUrl(
            @RequestParam String objectKey,
            @RequestParam(defaultValue = "15") int expirationMinutes) {
        String downloadUrl = storageService.generatePresignedDownloadUrl(objectKey, expirationMinutes);
        return ResponseEntity.ok(Map.of("downloadUrl", downloadUrl));
    }

    /**
     * Xóa một file khỏi hệ thống
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteFile(@RequestParam String objectKey) {
        storageService.deleteFile(objectKey);
        return ResponseEntity.ok(Map.of("message", "Xóa file thành công"));
    }
}
