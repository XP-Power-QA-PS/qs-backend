package pnh.dev.qs.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenData {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    private String deviceInfo;
    private String ipAddress;
    private String status;
    private Instant createdAt;
}
