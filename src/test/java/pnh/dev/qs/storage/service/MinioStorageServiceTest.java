package pnh.dev.qs.storage.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pnh.dev.qs.exception.custom.BadRequestException;
import pnh.dev.qs.exception.custom.ResourceNotFoundException;
import pnh.dev.qs.storage.config.MinioProperties;
import pnh.dev.qs.storage.dto.FileCommitRequest;
import pnh.dev.qs.storage.dto.FileCommitResponse;
import pnh.dev.qs.storage.dto.PresignedUploadRequest;
import pnh.dev.qs.storage.dto.PresignedUploadResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    private MinioProperties minioProperties;
    private MinioStorageService storageService;

    @BeforeEach
    void setUp() {
        minioProperties = new MinioProperties();
        minioProperties.setEndpoint("http://localhost:9000");
        minioProperties.setBucketName("qs-bucket");
        minioProperties.setPresignedUrlExpirationMinutes(15);
        minioProperties.setMaxFileSizeMb(20);

        storageService = new MinioStorageService(minioClient, minioProperties);
    }

    @Test
    void generatePresignedUploadUrl_Success() throws Exception {
        PresignedUploadRequest request = PresignedUploadRequest.builder()
                .fileName("avatar.png")
                .contentType("image/png")
                .fileSize(1024 * 1024) // 1MB
                .category("avatar")
                .build();

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://localhost:9000/qs-bucket/tmp/test.png?sig=xyz");

        PresignedUploadResponse response = storageService.generatePresignedUploadUrl(request, 123L);

        assertNotNull(response);
        assertEquals("http://localhost:9000/qs-bucket/tmp/test.png?sig=xyz", response.getUploadUrl());
        assertTrue(response.getTmpKey().startsWith("tmp/123/"));
        assertTrue(response.getTmpKey().endsWith(".png"));
        assertEquals(15, response.getExpiresInMinutes());
    }

    @Test
    void generatePresignedUploadUrl_ExceedsMaxSize_ThrowsBadRequestException() {
        PresignedUploadRequest request = PresignedUploadRequest.builder()
                .fileName("huge_video.mp4")
                .contentType("video/mp4")
                .fileSize(25 * 1024 * 1024) // 25MB > 20MB limit
                .build();

        assertThrows(BadRequestException.class, () -> storageService.generatePresignedUploadUrl(request, 1L));
    }

    @Test
    void commitFile_InvalidTmpKey_ThrowsBadRequestException() {
        FileCommitRequest request = FileCommitRequest.builder()
                .tmpKey("avatars/user1.png") // does not start with tmp/
                .targetFolder("avatars")
                .build();

        assertThrows(BadRequestException.class, () -> storageService.commitFile(request));
    }

    @Test
    void commitFile_PathTraversal_ThrowsBadRequestException() {
        FileCommitRequest request = FileCommitRequest.builder()
                .tmpKey("tmp/../etc/passwd")
                .targetFolder("avatars")
                .build();

        assertThrows(BadRequestException.class, () -> storageService.commitFile(request));
    }

    @Test
    void commitFile_SourceNotFound_ThrowsResourceNotFoundException() throws Exception {
        FileCommitRequest request = FileCommitRequest.builder()
                .tmpKey("tmp/non_existent.jpg")
                .targetFolder("avatars")
                .build();

        ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(errorResponse.code()).thenReturn("NoSuchKey");
        ErrorResponseException notFoundException = new ErrorResponseException(errorResponse, null, null);

        when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(notFoundException);

        assertThrows(ResourceNotFoundException.class, () -> storageService.commitFile(request));
    }

    @Test
    void commitFile_Success() throws Exception {
        FileCommitRequest request = FileCommitRequest.builder()
                .tmpKey("tmp/123/randomfile.png")
                .targetFolder("avatars/123")
                .targetFileName("avatar.png")
                .build();

        StatObjectResponse statResponse = mock(StatObjectResponse.class);
        when(statResponse.size()).thenReturn(5000L);
        when(statResponse.contentType()).thenReturn("image/png");

        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(statResponse);

        FileCommitResponse response = storageService.commitFile(request);

        assertNotNull(response);
        assertEquals("avatars/123/avatar.png", response.getObjectKey());
        assertEquals("http://localhost:9000/qs-bucket/avatars/123/avatar.png", response.getFileUrl());
        assertEquals(5000L, response.getFileSize());
        assertEquals("image/png", response.getContentType());

        // Verify copyObject and removeObject were called
        verify(minioClient, times(1)).copyObject(any(CopyObjectArgs.class));
        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteFile_CallsRemoveObject() throws Exception {
        storageService.deleteFile("avatars/123/avatar.png");
        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteFile_WithFullUrl_ExtractsKeyAndCallsRemoveObject() throws Exception {
        storageService.deleteFile("http://localhost:9000/qs-bucket/complaints/2026/CMP-001/defects/defect_1.png");
        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteFileByUrl_Success() throws Exception {
        storageService.deleteFileByUrl("http://localhost:9000/qs-bucket/complaints/2026/CMP-001/defects/defect_1.png");
        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }
}
