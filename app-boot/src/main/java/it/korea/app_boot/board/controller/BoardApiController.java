package it.korea.app_boot.board.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.korea.app_boot.board.dto.BoardDTO;
import it.korea.app_boot.board.dto.BoardSearchDTO;
import it.korea.app_boot.board.service.BoardJPAService;
import it.korea.app_boot.board.service.BoardService;
import it.korea.app_boot.user.dto.UserSecureDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


// view 가 아닌 data 를 돌려주는 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class BoardApiController {

    private final BoardService service;

    private final BoardJPAService jpaService;

    /**
     * 게시글 리스트 가져오기
     * @param searchDTO 페이지와 컬럼 정렬 정보 DTO
     * @return
     * @throws Exception
     */
    @GetMapping("/board/list")
    public ResponseEntity<Map<String, Object>> getBoardListData(BoardSearchDTO searchDTO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;

        resultMap = service.getBoardList(searchDTO);

        // HttpServletResponse + HttpStatus 를 결합한 객체를 돌려준다고 보면 된다
        return new ResponseEntity<>(resultMap, status);
    }

    /**
     * 게시글 리스트 가져오기 with JPA
     * @param searchDTO 페이지와 컬럼 정렬 정보 DTO
     * @return
     * @throws Exception
     */
    @GetMapping("/board/data")
    public ResponseEntity<Map<String, Object>> getBoardData(BoardSearchDTO searchDTO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;
        log.info("========= 게시판 가져오기 =========");

        // order by 객체 리스트 만들기
        List<Sort.Order> sorts = new ArrayList<>();
        String[] sidxs = searchDTO.getSidx().split(",");
        String[] sords = searchDTO.getSord().split(",");

        for (int i = 0; i < sidxs.length; i++) {
            if (sords[i].equals("asc")) {
                sorts.add(new Sort.Order(Sort.Direction.ASC, sidxs[i]));
            } else {
                sorts.add(new Sort.Order(Sort.Direction.DESC, sidxs[i]));
            }
        }

        // (현재 페이지, 가져올 개수, order by 객체) 를 전달한다
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), Sort.by(sorts));

        resultMap = jpaService.getBoardList(searchDTO, pageable);

        return new ResponseEntity<>(resultMap, status);
    }

    /**
     * 게시글 상세 정보 가져오기
     * @param brdId 게시글 아이디
     * @return
     * @throws Exception
     */
    @GetMapping("/board/{brdId}")
    public ResponseEntity<Map<String, Object>> getBoard(@PathVariable(name = "brdId") int brdId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;

        resultMap = jpaService.getBoard(brdId);

        return new ResponseEntity<>(resultMap, status);
    }

    /**
     * 게시글 작성하기
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/board")
    public ResponseEntity<Map<String, Object>> writeBoard(@Valid @ModelAttribute BoardDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;
        
        // 로그인 사용자 아이디
        request.setWriter(user.getUserId());
        resultMap = jpaService.writeBoard(request);

        return new ResponseEntity<>(resultMap, status);
    }

    /**
     * 게시글 수정하기
     * @param request
     * @return
     * @throws Exception
     */
    @PutMapping("/board")
    public ResponseEntity<Map<String, Object>> updateBoard(@Valid @ModelAttribute BoardDTO.Request request) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;

        resultMap = jpaService.updateBoard(request);

        return new ResponseEntity<>(resultMap, status);
    }

    /**
     * 게시글 삭제하기
     * @param brdId 게시글 아이디
     * @return
     * @throws Exception
     */
    @DeleteMapping("/board/{brdId}")
    public ResponseEntity<Map<String, Object>> deleteBoard(@PathVariable(name = "brdId") int brdId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;

        resultMap = jpaService.deleteBoard(brdId);

        return new ResponseEntity<>(resultMap, status);
    }

    /**
     * 파일 다운로드 하기
     * @param bfId 파일 아이디
     * @return
     * @throws Exception
     */
    @GetMapping("/board/file/{bfId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable(name = "bfId") int bfId) throws Exception {
        return jpaService.downloadFile(bfId);
    }

    /**
     * 파일 삭제하기
     * @param bfId 파일 아이디
     * @return
     * @throws Exception
     */
    @DeleteMapping("/board/file/{bfId}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable(name = "bfId") int bfId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;

        resultMap = jpaService.deleteFile(bfId);

        return new ResponseEntity<>(resultMap, status);
    }
    
}
