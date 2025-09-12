package it.korea.app_boot.gallery.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GalleryRequestDTO {

    @NotBlank(message = "제목은 필수 항목입니다.")
    private String title;
    @NotNull(message = "파일은 필수 항목입니다.")
    private MultipartFile file;
}
