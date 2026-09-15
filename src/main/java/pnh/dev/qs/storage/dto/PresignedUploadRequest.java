package pnh.dev.qs.storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUploadRequest {

    @NotBlank(message = "Tên file không được để trống")
    private String fileName;

    @NotBlank(message = "Content-Type không được để trống")
    private String contentType;

    @Positive(message = "Kích thước file phải lớn hơn 0")
    private long fileSize;

    /**
     * Phân loại mục đích lưu trữ (ví dụ: "avatar", "complaint", "equipment", "document", "general")
     */
    private String category;
}
