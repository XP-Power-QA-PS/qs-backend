package pnh.dev.qs.storage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileCommitRequest {

    @NotBlank(message = "tmpKey không được để trống")
    private String tmpKey;

    @NotBlank(message = "targetFolder không được để trống")
    private String targetFolder;

    private String targetFileName;
}
