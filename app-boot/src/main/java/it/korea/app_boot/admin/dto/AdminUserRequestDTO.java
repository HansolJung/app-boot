package it.korea.app_boot.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class AdminUserRequestDTO {

    @NotBlank(message = "아이디는 필수 항목입니다.")
    private String userId;
    @NotBlank(message = "패스워드는 필수 항목입니다.")
    private String passwd;
    @NotBlank(message = "권한은 필수 항목입니다.")
    private String userRole;
    @NotBlank(message = "이름은 필수 항목입니다.")
    private String userName;
    @NotBlank(message = "생년월일은 필수 항목입니다.")
    private String birth;
    @NotBlank(message = "성별은 필수 항목입니다.")
    private String gender;
    @NotBlank(message = "전화번호는 필수 항목입니다.")
    private String phone;
    @NotBlank(message = "이메일은 필수 항목입니다.")
    private String email;
    @NotBlank(message = "주소는 필수 항목입니다.")
    private String addr;
    private String addrDetail; 
    @NotBlank(message = "사용여부는 필수 항목입니다.")
    private String useYn;
}
