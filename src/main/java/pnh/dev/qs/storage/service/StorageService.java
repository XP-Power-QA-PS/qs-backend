package pnh.dev.qs.storage.service;

import pnh.dev.qs.storage.dto.FileCommitRequest;
import pnh.dev.qs.storage.dto.FileCommitResponse;
import pnh.dev.qs.storage.dto.PresignedUploadRequest;
import pnh.dev.qs.storage.dto.PresignedUploadResponse;

public interface StorageService {

    /**
     * Tạo đường dẫn Presigned Upload URL cho Frontend tải trực tiếp file lên thư mục tạm (tmp/)
     *
     * @param request thông tin file (tên, type, dung lượng)
     * @param userId ID người dùng đang đăng nhập (để phân lập tmp theo user nếu cần)
     * @return PresignedUploadResponse chứa uploadUrl, tmpKey, và thời gian hết hạn
     */
    PresignedUploadResponse generatePresignedUploadUrl(PresignedUploadRequest request, Long userId);

    /**
     * Xác nhận và chuyển file từ thư mục tạm (tmp/) sang thư mục lưu trữ chính thức
     *
     * @param request thông tin file tạm và thư mục đích
     * @return FileCommitResponse thông tin file đã được lưu trữ vĩnh viễn
     */
    FileCommitResponse commitFile(FileCommitRequest request);

    /**
     * Tạo đường dẫn tải xuống có chữ ký (Presigned Download URL) cho các file bảo mật
     *
     * @param objectKey đường dẫn file trong bucket
     * @param expirationMinutes thời gian sống của URL
     * @return Presigned download URL
     */
    String generatePresignedDownloadUrl(String objectKey, int expirationMinutes);

    /**
     * Lấy đường dẫn công khai (áp dụng cho các file trong thư mục public)
     *
     * @param objectKey đường dẫn file trong bucket
     * @return Public download URL
     */
    String getPublicUrl(String objectKey);

    /**
     * Xóa một file khỏi MinIO bằng objectKey
     *
     * @param objectKey đường dẫn file cần xóa
     */
    void deleteFile(String objectKey);

    /**
     * Xóa một file khỏi MinIO bằng URL đầy đủ (tự động bóc tách objectKey)
     *
     * @param url URL đầy đủ của file
     */
    void deleteFileByUrl(String url);

    /**
     * Kiểm tra xem object có tồn tại trong bucket hay không
     *
     * @param objectKey đường dẫn file
     * @return true nếu tồn tại, false nếu không
     */
    boolean doesObjectExist(String objectKey);

    /**
     * Lấy trực tiếp mảng byte của file từ MinIO
     *
     * @param objectKey đường dẫn file trong bucket
     * @return byte[] nội dung file hoặc null nếu lỗi/không tìm thấy
     */
    byte[] getFileBytes(String objectKey);

    /**
     * Lấy trực tiếp mảng byte của file từ đường dẫn URL (MinIO URL hoặc external URL)
     *
     * @param url URL đầy đủ hoặc objectKey của file
     * @return byte[] nội dung file hoặc null nếu lỗi/không tìm thấy
     */
    byte[] getFileBytesFromUrl(String url);
}

