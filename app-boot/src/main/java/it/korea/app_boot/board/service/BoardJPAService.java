package it.korea.app_boot.board.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.korea.app_boot.board.dto.BoardDTO;
import it.korea.app_boot.board.dto.BoardFileDTO;
import it.korea.app_boot.board.dto.BoardSearchDTO;
import it.korea.app_boot.board.entity.BoardEntity;
import it.korea.app_boot.board.entity.BoardFileEntity;
import it.korea.app_boot.board.repository.BoardFileRepository;
import it.korea.app_boot.board.repository.BoardRepository;
import it.korea.app_boot.board.repository.BoardSearchSpecification;
import it.korea.app_boot.common.files.FileUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardJPAService {

    @Value("${server.file.upload.path}")
    private String filePath;

    private final BoardRepository boardRepository;
    private final BoardFileRepository fileRepository;
    private final FileUtils fileUtils;

    /**
     * 게시글 리스트
     * @param pageable
     * @return
     * @throws Exception
     */
    public Map<String, Object> getBoardList(BoardSearchDTO searchDTO, Pageable pageable) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        
        // findAll() -> select * from board;
        // Page<BoardEntity> pageObj = boardRepository.findAll(pageable);
        Page<BoardEntity> pageObj = null;
        
        // 검색어를 입력해서 검색했을 경우
        if (!StringUtils.isBlank(searchDTO.getSchType()) && !StringUtils.isBlank(searchDTO.getSchText())) {

            // findBy 절을 커스텀해서 사용한 경우
            // if (searchDTO.getSchType().equals("title")) {  // 제목으로 검색한 경우
            //     pageObj = boardRepository.findByTitleContaining(searchDTO.getSchText(), pageable);
            // } else if (searchDTO.getSchType().equals("writer")) { // 글쓴이로 검색한 경우
            //     pageObj = boardRepository.findByWriterContaining(searchDTO.getSchText(), pageable);
            // }

            // BoardEntity boardEntity = new BoardEntity();
            // BoardSearchSpecification searchSpecification = new BoardSearchSpecification(boardEntity, searchDTO);
            BoardSearchSpecification searchSpecification = new BoardSearchSpecification(searchDTO);
            pageObj = boardRepository.findAll(searchSpecification, pageable);

        } else { // 그냥 페이지 불러올 경우 (검색어 없이 검색한 경우도 포함)
            pageObj = boardRepository.findAll(pageable);
        }

        //List<BoardDTO.Response> list = pageObj.getContent().stream().map(entity -> { return BoardDTO.Response.of(entity); }).toList();
        //List<BoardDTO.Response> list = pageObj.getContent().stream().map(entity -> BoardDTO.Response.of(entity)).toList();
        
        //List<BoardDTO.Response> list = pageObj.getContent().stream().map(BoardDTO.Response::of).collect(Collectors.toList());  // .collect(Collectors.toList()); 를 하면 가변형 리스트.
        List<BoardDTO.Response> list = pageObj.getContent().stream().map(BoardDTO.Response::of).toList(); // 엔티티 리스트에서 DTO 리스트로 변환. toList() 는 불변형 리스트.
        
        resultMap.put("total", pageObj.getTotalElements());
        resultMap.put("content", list);

        return resultMap;
    }

    /**
     * 게시글 상세정보
     * @param brdId 게시글 아이디
     * @return
     * @throws Exception
     */
    @Transactional
    public Map<String, Object> getBoard(int brdId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        //BoardEntity entity = boardRepository.findById(brdId)
        //    .orElseThrow(()-> new RuntimeException("게시글 없음"));

        BoardEntity entity = boardRepository.getBoard(brdId)   // fetch join 을 사용한 getBoard 메서드 호출
            .orElseThrow(()-> new RuntimeException("게시글 없음"));
        BoardDTO.Detail detail = BoardDTO.Detail.of(entity);

        resultMap.put("vo", detail);

        return resultMap;
    }

    /**
     * 게시글 쓰기
     * @param request
     * @return
     * @throws Exception
     */
    @Transactional
    public Map<String, Object> writeBoard(BoardDTO.Request request) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        // 물리적으로 파일을 저장
        Map<String, Object> fileMap = fileUtils.uploadFiles(request.getFile(), filePath);

        BoardEntity entity = new BoardEntity();
        entity.setTitle(request.getTitle());
        entity.setContents(request.getContents());
        entity.setWriter(request.getWriter());

        // 파일이 있을 경우에만 파일 엔티티 생성
        if (fileMap != null) {  
            BoardFileEntity fileEntity = new BoardFileEntity();
            fileEntity.setFileName(fileMap.get("fileName").toString());
            fileEntity.setStoredName(fileMap.get("storedFileName").toString());
            fileEntity.setFilePath(fileMap.get("filePath").toString());
            fileEntity.setFileSize(request.getFile().getSize());
            fileEntity.setCreateDate(LocalDateTime.now());
            entity.addFiles(fileEntity, false);  // 게시글 엔티티와 파일 엔티티 관계를 맺어줌
        }

        boardRepository.save(entity);

        resultMap.put("resultCode", 200);
        resultMap.put("resultMsg", "OK");

        return resultMap;
    }

    /**
     * 게시글 수정
     * @param request
     * @return
     * @throws Exception
     */
    @Transactional
    public Map<String, Object> updateBoard(BoardDTO.Request request) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        // 1. 수정하기 위해 기존 정보를 불러온다 
        BoardEntity entity = boardRepository.getBoard(request.getBrdId())   // fetch join 을 사용한 getBoard 메서드 호출
            .orElseThrow(()-> new RuntimeException("게시글 없음"));

        if (!entity.getWriter().equals(request.getWriter()) &&
                !request.isAdmin()) {   // 만약 로그인된 회원과 글 작성자가 다르고 어드민 권한까지 없다면...
            throw new RuntimeException("본인이 작성한 게시글만 수정이 가능합니다.");
        }
        
        entity.setTitle(request.getTitle());
        entity.setContents(request.getContents());

        BoardDTO.Detail detail = BoardDTO.Detail.of(entity);
        
        // 2. 업로드 할 파일이 있으면 업로드
        if (!request.getFile().isEmpty()) {

            // 2-1. 파일 업로드
            Map<String, Object> fileMap = fileUtils.uploadFiles(request.getFile(), filePath);

            entity.getFileList().clear();  // 기존 파일 목록 비우기
            
            // 2-2. 파일 등록
            // 파일이 있을 경우에만 파일 엔티티 생성
            if (fileMap != null) {  
                BoardFileEntity fileEntity = new BoardFileEntity();
                fileEntity.setFileName(fileMap.get("fileName").toString());
                fileEntity.setStoredName(fileMap.get("storedFileName").toString());
                fileEntity.setFilePath(fileMap.get("filePath").toString());
                fileEntity.setFileSize(request.getFile().getSize());
                fileEntity.setCreateDate(LocalDateTime.now());

                // 파일만 수정했을 경우에도 updateDate 를 갱신하기 위해서 isUpdate 값을 true 로 줌.
                entity.addFiles(fileEntity, true);  // 게시글 엔티티와 파일 엔티티 관계를 맺어줌
            }
        }

        boardRepository.save(entity);

        if (!request.getFile().isEmpty()) {
            // 2-3. 기존 파일 삭제 (작업 도중 DB에 문제가 생길 수도 있기 때문에 물리적 파일 삭제는 제일 마지막에 진행)
            // 게시글 상세 정보 DTO 가 가지고 있는 파일 DTO 리스트 순회
            for (BoardFileDTO fileDTO : detail.getFileList()) {	
                // 파일 정보
                String fullPath = fileDTO.getFilePath() + fileDTO.getStoredName();
                
                //  entity.getFileList().clear(); 를 이미 했기 때문에 따로 파일 정보를 DB에서 삭제할 필요 없음
                //fileRepository.deleteById(fileDTO.getBfId());

                File file = new File(fullPath);

                if (!file.exists()) {
                    throw new NotFoundException("파일이 경로에 없음");
                }

                // 실제 파일 삭제
                fileUtils.deleteFile(fullPath);
            }
        }

        resultMap.put("resultCode", 200);
        resultMap.put("resultMsg", "OK");

        return resultMap;
    }

    /**
     * 게시글 삭제
     * @param brdId 게시글 아이디
     * @param request
     * @return
     * @throws Exception
     */
    @Transactional
    public Map<String, Object> deleteBoard(int brdId, BoardDTO.Request request) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        BoardEntity entity = boardRepository.getBoard(brdId)   // fetch join 을 사용한 getBoard 메서드 호출
            .orElseThrow(()-> new RuntimeException("게시글 없음"));
        
        BoardDTO.Detail detail = BoardDTO.Detail.of(entity);

        if (!detail.getWriter().equals(request.getWriter()) &&
                !request.isAdmin()) {   // 만약 로그인된 회원과 글 작성자가 다르고 어드민 권한까지 없다면...
            throw new RuntimeException("본인이 작성한 게시글만 삭제가 가능합니다.");
        }

        boardRepository.delete(entity);

        // 실제 파일 삭제는 제일 마지막에 진행
        if (detail.getFileList() != null && detail.getFileList().size() > 0) {
            for (BoardFileDTO fileDTO : detail.getFileList()) {
                String fullPath = fileDTO.getFilePath() + fileDTO.getStoredName();
                File file = new File(fullPath);

                if (!file.exists()) {
                    throw new NotFoundException("파일이 경로에 없음");
                }

                // 실제 파일 삭제
                fileUtils.deleteFile(fullPath);
            }
        }

        resultMap.put("resultCode", 200);
        resultMap.put("resultMsg", "OK");

        return resultMap;
    }

    /**
     * 파일 다운로드
     * @param bfId 파일 아이디
     * @return
     * @throws Exception
     */
    public ResponseEntity<Resource> downloadFile(int bfId) throws Exception {
        // http 헤더 객체
        HttpHeaders header = new HttpHeaders();
        Resource resource = null;

        // 파일 정보 
        BoardFileDTO fileDTO = BoardFileDTO.of(
            fileRepository.findById(bfId)
                .orElseThrow(()-> 
                    new NotFoundException("파일정보 DB에 없음")));
        
        String fullPath = fileDTO.getFilePath() + fileDTO.getStoredName();
        String fileName = fileDTO.getFileName();  // 다운로드할 때 사용

        File file = new File(fullPath);

        if (!file.exists()) {
            throw new NotFoundException("파일이 경로에 없음");
        }

        // 파일 타입 > NIO 를 이용한 타입 찾기
        String mimeType = Files.probeContentType(Paths.get(file.getAbsolutePath()));

        if (mimeType == null) {
            mimeType = "application/octet-stream";  // 기본 바이너리 파일
        }

        // 리소스 객체에 url을 통해서 전송할 파일 저장
        resource = new FileSystemResource(file);

        // http 응답에서 브라우저가 콘텐츠를 처리하는 방식
        // inline > 브라우저 바에서 처리 > 다운로드가 아니라 브라우저에서 열기
        // attachment > 다운로드
        header.setContentDisposition(
            ContentDisposition.builder("attachment")
                .filename(fileName, StandardCharsets.UTF_8) 
                .build()
        );

        // mimeType 설정
        header.setContentType(MediaType.parseMediaType(mimeType));
        header.setContentLength(fileDTO.getFileSize());

        // 캐시 설정
        header.setCacheControl("no-cache, no-store, must-revalidate");
        header.set("Pragma", "no-cache");  // old browser 호환
        header.set("Expires", "0");  // 즉시 삭제

        return new ResponseEntity<>(resource, header, HttpStatus.OK);
    }


    /**
     * 파일 삭제
     * @param bfId 파일 아이디
     * @return
     * @throws Exception
     */
    @Transactional
    public Map<String, Object> deleteFile(int bfId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        
        // 파일 정보 
        BoardFileEntity fileEntity = 
            fileRepository.findById(bfId)
                .orElseThrow(()-> 
                    new NotFoundException("파일정보 DB에 없음"));

        BoardFileDTO fileDTO = BoardFileDTO.of(fileEntity);

        String fullPath = fileDTO.getFilePath() + fileDTO.getStoredName();
        
        // 파일 DB에서 삭제
        fileRepository.delete(fileEntity);

        // 게시글 엔티티 updateDate 갱신하기
        BoardEntity boardEntity = fileEntity.getBoard();
        boardEntity.preUpdate();

        File file = new File(fullPath);
        if (!file.exists()) {
            throw new NotFoundException("파일이 경로에 없음");
        }

        // 실제 파일 삭제
        fileUtils.deleteFile(fullPath);

        resultMap.put("resultCode", 200);
        resultMap.put("resultMsg", "OK");

        return resultMap;
    }

    /**
     * 조회수 증가
     * @param id 게시글 아이디
     * @param request
     * @param response
     */
    public void increaseView(int brdId, HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        Cookie oldCookie = null;
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("board")) {
                    oldCookie = cookie;
                }
            }
        }

        if (oldCookie != null) {
            if (!oldCookie.getValue().contains("[" + brdId + "]")) {
                increaseView(brdId);
                oldCookie.setValue(oldCookie.getValue() + "_[" + brdId + "]");
                oldCookie.setPath("/");
                oldCookie.setMaxAge(60);
                response.addCookie(oldCookie);
            }
        } else {
            increaseView(brdId);
            Cookie newCookie = new Cookie("board","[" + brdId + "]");
            newCookie.setPath("/");
            newCookie.setMaxAge(60);
            response.addCookie(newCookie);
        }
    }

    /**
     * 조회수 증가
     * @param brdId 게시글 아이디
     */
    @Transactional
    public void increaseView(int brdId) {

        BoardEntity entity = boardRepository.findById(brdId)
            .orElseThrow(()-> new RuntimeException("게시글 없음"));

        entity.setReadCount(entity.getReadCount() + 1);

        boardRepository.save(entity);
    }
}
