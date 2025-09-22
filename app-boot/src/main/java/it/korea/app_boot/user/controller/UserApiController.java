package it.korea.app_boot.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.korea.app_boot.user.dto.UserRequestDTO;
import it.korea.app_boot.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserApiController {

    private final UserService userService;

    @PostMapping("/user/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody // JSON 타입으로 받기 때문에 @RequestBody 사용
            UserRequestDTO userRequestDTO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;

        try {

            userService.register(userRequestDTO);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMsg", "OK");
        } catch (Exception e) {
            // 예외 발생 시 공통 모듈을 실행하기 위해 예외를 던진다
            throw new Exception(e.getMessage() == null ? "회원가입 실패" : e.getMessage());
        }

        return new ResponseEntity<>(resultMap, status);
    }
}
