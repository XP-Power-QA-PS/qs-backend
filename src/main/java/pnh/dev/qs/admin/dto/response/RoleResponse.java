package pnh.dev.qs.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
public class RoleResponse {
    
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String name;
    private String description;
}
