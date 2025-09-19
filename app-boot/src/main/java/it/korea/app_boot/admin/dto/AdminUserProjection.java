package it.korea.app_boot.admin.dto;

import java.time.LocalDateTime;

public interface AdminUserProjection {

    String getUserId();
    String getUserName();
    String getBirth();
    String getGender();
    String getPhone();
    String getEmail();
    String getAddr();
    String getAddrDetail();
    LocalDateTime getCreateDate();
    LocalDateTime getUpdateDate();
    String getUseYn();
    String getDelYn();
    String getRoleId();
    String getRoleName();
}
