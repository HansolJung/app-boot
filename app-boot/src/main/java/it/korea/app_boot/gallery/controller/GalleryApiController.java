package it.korea.app_boot.gallery.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.korea.app_boot.gallery.dto.GalleryDeleteDTO;
import it.korea.app_boot.gallery.dto.GalleryRequestDTO;
import it.korea.app_boot.gallery.service.GalleryService;
import it.korea.app_boot.user.dto.UserSecureDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class GalleryApiController {

    private final GalleryService galleryService;

    /**
     * 갤러리 리스트 가져오기
     * @param pageable 페이징 정보
     * @return
     * @throws Exception
     */
    @GetMapping("/gal")
    public ResponseEntity<Map<String, Object>> getGalleryList(
            @PageableDefault(page = 0, size = 10, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable) throws Exception {
        
        Map<String, Object> resultMap = galleryService.getGalleryList(pageable);

        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    /**
     * 갤러리 상세 정보 가져오기
     * @param nums 갤러리 id
     * @return
     * @throws Exception
     */
    @GetMapping("/gal/{nums}")
    public ResponseEntity<Map<String, Object>> getGalleryDetail(@PathVariable(name = "nums") String nums) throws Exception {
        
        Map<String, Object> resultMap = galleryService.getGalleryDetail(nums);

        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    /**
     * 갤러리 등록하기
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/gal")
    public ResponseEntity<Map<String, Object>> writeGallery(@Valid @ModelAttribute GalleryRequestDTO request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;

        try {
            request.setWriter(user.getUserId());
            galleryService.addGallery(request);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMsg", "OK");
        } catch (Exception e) {
            // 예외 발생 시 공통 모듈을 실행하기 위해 예외를 던진다
            throw new Exception(e.getMessage() == null ? "이미지 등록 실패" : e.getMessage());
        }

        return new ResponseEntity<>(resultMap, status);
    }

    /**
     * 갤러리 수정하기
     * @param request
     * @return
     * @throws Exception
     */
    @PutMapping("/gal")
    public ResponseEntity<Map<String, Object>> updateGallery(@Valid @ModelAttribute GalleryRequestDTO request) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;

        try {
            
            galleryService.updateGallery(request);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMsg", "OK");
        } catch (Exception e) {
            // 예외 발생 시 공통 모듈을 실행하기 위해 예외를 던진다
            throw new Exception(e.getMessage() == null ? "이미지 수정 실패" : e.getMessage());
        }

        return new ResponseEntity<>(resultMap, status);
    }

    /**
     * 갤러리 삭제하기
     * @param deleteDTO 삭제할 id 리스트가 담긴 DTO
     * @return
     * @throws Exception
     */
    @PostMapping("/gal/delete")   // RESTful 원칙에 위배되는 메소드 설정과 URL 형식이지만 삭제할 갤러리 ID 리스트를 전달받기 위해서 어쩔수없이 사용
    public ResponseEntity<Map<String, Object>> deleteGallery(@RequestBody GalleryDeleteDTO deleteDTO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;

        try {

            galleryService.deleteGallery(deleteDTO);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMsg", "OK");
        } catch (Exception e) {
            // 예외 발생 시 공통 모듈을 실행하기 위해 예외를 던진다
            throw new Exception(e.getMessage() == null ? "이미지 삭제 실패" : e.getMessage());
        }

        return new ResponseEntity<>(resultMap, status);
    }
}
