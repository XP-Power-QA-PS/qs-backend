package pnh.dev.qs.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileCommitResponse {
    private String objectKey;
    private String fileUrl;
    private long fileSize;
    private String contentType;
}
