package it.korea.app_boot.gallery.service;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.korea.app_boot.common.dto.PageVO;
import it.korea.app_boot.common.files.FileUtils;
import it.korea.app_boot.gallery.dto.GalleryDTO;
import it.korea.app_boot.gallery.dto.GalleryDeleteDTO;
import it.korea.app_boot.gallery.dto.GalleryRequestDTO;
import it.korea.app_boot.gallery.entity.GalleryEntity;
import it.korea.app_boot.gallery.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GalleryService {

    @Value("${server.file.gallery.path}")
    private String filePath;

    private final GalleryRepository galleryRepository;
    private final FileUtils fileUtils;
    private List<String> extensions = Arrays.asList("jpg", "jpeg", "gif", "png", "webp", "bmp");

    /**
     * 갤러리 리스트 가져오기
     * @param pageable 페이징 정보
     * @return
     * @throws Exception
     */
    public Map<String, Object> getGalleryList(Pageable pageable) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        Page<GalleryEntity> entityList = galleryRepository.findAll(pageable);
        List<GalleryDTO> dtoList = entityList.getContent().stream().map(GalleryDTO::of).toList(); 

        PageVO pageVO = new PageVO();
        pageVO.setData(entityList.getNumber(), (int) entityList.getTotalElements());

        resultMap.put("total", entityList.getTotalElements());
        resultMap.put("page", entityList.getNumber());
        resultMap.put("content", dtoList);
        resultMap.put("pageHTML", pageVO.pageHTML());

        return resultMap;
    }

    /**
     * 갤러리 상세정보 가져오기
     * @param nums 갤러리 id
     * @return
     * @throws Exception
     */
    public Map<String, Object> getGalleryDetail(String nums) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        GalleryDTO detail = GalleryDTO.of(galleryRepository.findById(nums)
            .orElseThrow(()-> new RuntimeException("갤러리 없음")));
        
        resultMap.put("vo", detail);

        return resultMap;
    }

    /**
     * 갤러리 등록하기
     * @param request
     * @throws Exception
     */
    @Transactional
    public void addGallery(GalleryRequestDTO request) throws Exception {

        String fileName = request.getFile().getOriginalFilename();
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);

        if (!extensions.contains(ext.toLowerCase())) {
            throw new RuntimeException("파일 형식이 맞지 않습니다. 이미지 파일만 가능합니다.");
        }

        Map<String, Object> fileMap = fileUtils.uploadFiles(request.getFile(), filePath);

        if (fileMap == null) {
            throw new RuntimeException("파일 업로드 실패");
        }

        String thumbFilePath = filePath + "thumb" + File.separator;
        String storedFilePath = filePath + fileMap.get("storedFileName").toString();

        File file = new File(storedFilePath);

        if (!file.exists()) {
            throw new RuntimeException("업로드 파일이 존재하지 않음");
        }

        String thumbName = fileUtils.thumbNailFile(150, 150, file, thumbFilePath);
        String newNums = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
        
        GalleryEntity entity = new GalleryEntity();
        entity.setNums(newNums);
        entity.setTitle(request.getTitle());
        entity.setWriter("admin");
        entity.setFileName(fileMap.get("fileName").toString());
        entity.setStoredName(fileMap.get("storedFileName").toString());
        entity.setFilePath(filePath);
        entity.setFileThumbName(thumbName);

        galleryRepository.save(entity);
    }

    /**
     * 갤러리 수정하기
     * @param request
     * @throws Exception
     */
    @Transactional
    public void updateGallery(GalleryRequestDTO request) throws Exception {

        // 1. 수정하기 위해 기존 정보를 불러온다
        GalleryEntity entity = galleryRepository.findById(request.getNums())
            .orElseThrow(()-> new RuntimeException("갤러리 없음"));

        entity.setTitle(request.getTitle());
        entity.setWriter("admin");

        GalleryDTO detail = GalleryDTO.of(entity);

        // 2. 업로드 할 파일이 있으면 업로드
        if (!request.getFile().isEmpty()) {

            String fileName = request.getFile().getOriginalFilename();
            String ext = fileName.substring(fileName.lastIndexOf(".") + 1);

            if (!extensions.contains(ext.toLowerCase())) {
                throw new RuntimeException("파일 형식이 맞지 않습니다. 이미지 파일만 가능합니다.");
            }

            Map<String, Object> fileMap = fileUtils.uploadFiles(request.getFile(), filePath);

            if (fileMap == null) {
                throw new RuntimeException("파일 업로드 실패");
            }

            String thumbFilePath = filePath + "thumb" + File.separator;
            String storedFilePath = filePath + fileMap.get("storedFileName").toString();

            File file = new File(storedFilePath);

            if (!file.exists()) {
                throw new RuntimeException("업로드 파일이 존재하지 않음");
            }

            String thumbName = fileUtils.thumbNailFile(150, 150, file, thumbFilePath);
        
            entity.setFileName(fileMap.get("fileName").toString());
            entity.setStoredName(fileMap.get("storedFileName").toString());
            entity.setFileThumbName(thumbName);
            entity.setFilePath(filePath);
        }
        
        galleryRepository.save(entity);

        if (!request.getFile().isEmpty()) {
            // 2-3. 기존 파일 삭제 (작업 도중 DB에 문제가 생길 수도 있기 때문에 물리적 파일 삭제는 제일 마지막에 진행)
            
            // 파일 정보
            String fullPath = detail.getFilePath() + detail.getStoredName();
            String thumbFilePath = filePath + "thumb" + File.separator + detail.getFileThumbName();

            File file = new File(fullPath);

            if (!file.exists()) {
                throw new NotFoundException("파일이 경로에 없음");
            }

            // 실제 파일 삭제
            fileUtils.deleteFile(fullPath);
            fileUtils.deleteFile(thumbFilePath);  // 썸네일 파일까지 삭제
        }
    }

    /**
     * 갤러리 삭제하기
     * @param deleteDTO 삭제할 id 리스트가 담긴 DTO
     * @throws Exception
     */
    @Transactional
    public void deleteGallery(GalleryDeleteDTO deleteDTO) throws Exception {

        List<GalleryDTO> details = galleryRepository.findAllById(deleteDTO.getNumsList())
            .stream().map(GalleryDTO::of).toList();  // findAllById 로 갤러리 정보 리스트 얻어와서 DTO 리스트로 변환
        
        galleryRepository.deleteAllById(deleteDTO.getNumsList());  // deleteAllById 로 DB 에서 전부 삭제

        if (details != null && details.size() > 0) {
            // 실제 파일 삭제는 제일 마지막에 진행
            for (GalleryDTO detail : details) {
                // 파일 정보
                String fullPath = detail.getFilePath() + detail.getStoredName();
                String thumbFilePath = filePath + "thumb" + File.separator + detail.getFileThumbName();

                File file = new File(fullPath);

                if (!file.exists()) {
                    throw new NotFoundException("파일이 경로에 없음");
                }

                // 실제 파일 삭제
                fileUtils.deleteFile(fullPath);
                fileUtils.deleteFile(thumbFilePath);  // 썸네일 파일까지 삭제
            }
        } 
    }
}
