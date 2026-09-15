package pnh.dev.qs.storage.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import pnh.dev.qs.exception.custom.BadRequestException;
import pnh.dev.qs.exception.custom.ResourceNotFoundException;
import pnh.dev.qs.storage.config.MinioProperties;
import pnh.dev.qs.storage.dto.FileCommitRequest;
import pnh.dev.qs.storage.dto.FileCommitResponse;
import pnh.dev.qs.storage.dto.PresignedUploadRequest;
import pnh.dev.qs.storage.dto.PresignedUploadResponse;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public PresignedUploadResponse generatePresignedUploadUrl(PresignedUploadRequest request, Long userId) {
        long maxSizeBytes = minioProperties.getMaxFileSizeMb() * 1024 * 1024;
        if (request.getFileSize() > maxSizeBytes) {
            throw new BadRequestException(String.format("Dung lượng file vượt quá giới hạn tối đa (%d MB)", minioProperties.getMaxFileSizeMb()));
        }

        String sanitizedFileName = sanitizeFileName(request.getFileName());
        String extension = getFileExtension(sanitizedFileName);
        String uniqueId = UUID.randomUUID().toString().replace("-", "");

        // Cấu trúc thư mục tạm: tmp/{userId}/{uuid}{extension} hoặc tmp/{uuid}{extension}
        String userPrefix = (userId != null) ? userId + "/" : "";
        String tmpKey = "tmp/" + userPrefix + uniqueId + extension;

        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(minioProperties.getBucketName())
                    .object(tmpKey)
                    .expiry(minioProperties.getPresignedUrlExpirationMinutes(), TimeUnit.MINUTES)
                    .build();

            String presignedUrl = minioClient.getPresignedObjectUrl(args);

            return PresignedUploadResponse.builder()
                    .uploadUrl(presignedUrl)
                    .tmpKey(tmpKey)
                    .expiresInMinutes(minioProperties.getPresignedUrlExpirationMinutes())
                    .build();
        } catch (Exception e) {
            log.error("Lỗi khi sinh presigned upload URL cho file {}: {}", request.getFileName(), e.getMessage(), e);
            throw new RuntimeException("Không thể tạo liên kết tải file: " + e.getMessage(), e);
        }
    }

    @Override
    public FileCommitResponse commitFile(FileCommitRequest request) {
        String tmpKey = request.getTmpKey();
        if (!StringUtils.hasText(tmpKey) || !tmpKey.startsWith("tmp/")) {
            throw new BadRequestException("Chỉ được phép xác nhận chuyển file từ thư mục tạm (tmp/)");
        }

        if (tmpKey.contains("..") || request.getTargetFolder().contains("..")) {
            throw new BadRequestException("Đường dẫn file không hợp lệ (path traversal)");
        }

        // 1. Kiểm tra file tạm có thực sự tồn tại trên MinIO
        StatObjectResponse stat = getStatObject(tmpKey);
        if (stat == null) {
            throw new ResourceNotFoundException("File tạm không tồn tại hoặc đã hết hạn trong thư mục tmp: " + tmpKey);
        }

        // 2. Xác định tên file và thư mục lưu trữ chính thức
        String targetFolder = cleanFolder(request.getTargetFolder());
        String finalFileName = StringUtils.hasText(request.getTargetFileName())
                ? sanitizeFileName(request.getTargetFileName())
                : tmpKey.substring(tmpKey.lastIndexOf('/') + 1);

        String targetKey = targetFolder + "/" + finalFileName;

        try {
            // 3. Thực hiện Server-Side Copy từ tmpKey sang targetKey
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(targetKey)
                            .source(
                                    CopySource.builder()
                                            .bucket(minioProperties.getBucketName())
                                            .object(tmpKey)
                                            .build()
                            )
                            .build()
            );

            // 4. Chủ động dọn dẹp file trong thư mục tạm sau khi copy thành công
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .object(tmpKey)
                                .build()
                );
            } catch (Exception e) {
                log.warn("Không thể xóa file tạm {} sau khi copy (file sẽ tự hết hạn theo ILM): {}", tmpKey, e.getMessage());
            }

            String fileUrl = getPublicUrl(targetKey);

            return FileCommitResponse.builder()
                    .objectKey(targetKey)
                    .fileUrl(fileUrl)
                    .fileSize(stat.size())
                    .contentType(stat.contentType())
                    .build();
        } catch (Exception e) {
            log.error("Lỗi khi chuyển file từ {} sang {}: {}", tmpKey, targetKey, e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lưu file vào hệ thống: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedDownloadUrl(String objectKey, int expirationMinutes) {
        if (!StringUtils.hasText(objectKey)) {
            throw new BadRequestException("objectKey không được để trống");
        }
        int expiry = (expirationMinutes > 0) ? expirationMinutes : minioProperties.getPresignedUrlExpirationMinutes();

        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .expiry(expiry, TimeUnit.MINUTES)
                    .build();

            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception e) {
            log.error("Lỗi khi sinh presigned download URL cho {}: {}", objectKey, e.getMessage(), e);
            throw new RuntimeException("Không thể tạo liên kết tải file: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPublicUrl(String objectKey) {
        String endpoint = minioProperties.getEndpoint().replaceAll("/+$", "");
        return endpoint + "/" + minioProperties.getBucketName() + "/" + objectKey;
    }

    @Override
    public void deleteFile(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return;
        }
        String cleanKey = objectKey.trim();
        String bucket = minioProperties.getBucketName();
        int bucketIndex = cleanKey.indexOf("/" + bucket + "/");
        if (bucketIndex != -1) {
            cleanKey = cleanKey.substring(bucketIndex + bucket.length() + 2);
            int queryIndex = cleanKey.indexOf('?');
            if (queryIndex != -1) {
                cleanKey = cleanKey.substring(0, queryIndex);
            }
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(cleanKey)
                            .build()
            );
            log.info("Đã xóa file thành công: {}", cleanKey);
        } catch (Exception e) {
            log.error("Lỗi khi xóa file {}: {}", cleanKey, e.getMessage(), e);
            throw new RuntimeException("Không thể xóa file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFileByUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return;
        }
        deleteFile(url);
    }

    @Override
    public boolean doesObjectExist(String objectKey) {
        return getStatObject(objectKey) != null;
    }

    private StatObjectResponse getStatObject(String objectKey) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .build()
            );
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equalsIgnoreCase(e.errorResponse().code())) {
                return null;
            }
            log.warn("Lỗi kiểm tra object {}: {}", objectKey, e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Lỗi không xác định khi kiểm tra object {}: {}", objectKey, e.getMessage());
            return null;
        }
    }

    private String cleanFolder(String folder) {
        if (!StringUtils.hasText(folder)) {
            return "general";
        }
        return folder.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "file_" + System.currentTimeMillis();
        }
        // Giữ lại tên không dấu, số, dấu chấm, gạch dưới, gạch ngang
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex).toLowerCase();
        }
        return "";
    }

    @Override
    public byte[] getFileBytes(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return null;
        }
        try (var stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(objectKey)
                        .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            log.warn("Không thể lấy byte dữ liệu cho file {}: {}", objectKey, e.getMessage());
            return null;
        }
    }

    @Override
    public byte[] getFileBytesFromUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        String cleanUrl = url.trim();
        String bucket = minioProperties.getBucketName();
        String objectKey = null;

        // Bóc tách objectKey từ URL của MinIO (ví dụ: http://localhost:9000/qs-bucket/complaints/...)
        int bucketIndex = cleanUrl.indexOf("/" + bucket + "/");
        if (bucketIndex != -1) {
            objectKey = cleanUrl.substring(bucketIndex + bucket.length() + 2);
            int queryIndex = objectKey.indexOf('?');
            if (queryIndex != -1) {
                objectKey = objectKey.substring(0, queryIndex);
            }
        } else if (cleanUrl.startsWith("complaints/") || cleanUrl.startsWith("tmp/") || cleanUrl.startsWith("public/")) {
            objectKey = cleanUrl;
        }

        if (objectKey != null) {
            return getFileBytes(objectKey);
        }

        // Tải dự phòng nếu là liên kết ngoài
        if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
            try {
                java.net.URI uri = java.net.URI.create(cleanUrl);
                java.net.URLConnection conn = uri.toURL().openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(5000);
                try (java.io.InputStream in = conn.getInputStream()) {
                    return in.readAllBytes();
                }
            } catch (Exception e) {
                log.warn("Không thể tải ảnh từ liên kết ngoài {}: {}", cleanUrl, e.getMessage());
                return null;
            }
        }

        return null;
    }
}

