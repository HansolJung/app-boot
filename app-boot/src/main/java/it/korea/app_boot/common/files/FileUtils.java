package it.korea.app_boot.common.files;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.MultiStepRescaleOp;

@Component
public class FileUtils {

    /**
     * 파일 업로드
     * @param file 파일 
     * @param type 게시판 타입
     * @return
     * @throws Exception
     */
    public Map<String, Object> uploadFiles(MultipartFile file, String filePath) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();
        
        if (file == null || file.isEmpty()) {
            return null;
        }

        String fileName = file.getOriginalFilename();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        String randName = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
        String storedFileName = randName + "." + extension;

        String newfilePath = filePath;
        String fullPath = newfilePath + storedFileName;
        
        File newFile = new File(fullPath);

        // 경로가 없다면 만들어준다
        if (!newFile.getParentFile().exists()) {

            // 모든 부모 파일 경로 만들기
            newFile.getParentFile().mkdirs();
        }

        newFile.createNewFile();  // 빈 파일 생성
        file.transferTo(newFile);  // 파일 복사

        resultMap.put("fileName", fileName);
        resultMap.put("storedFileName", storedFileName);
        resultMap.put("filePath", newfilePath);

        return resultMap;
    }

    /**
     * 파일 삭제
     * @param deleteFilePath 삭제할 파일 경로
     * @throws Exception
     */
    public void deleteFile(String deleteFilePath) throws Exception {
        File deleteFile = new File(deleteFilePath);

        if (deleteFile.exists()) {
            deleteFile.delete();
        }
    }

    /**
     * 썸네일 만들기
     * @param width 가로 픽셀
     * @param height 세로 픽셀
     * @param originFile 원본 파일
     * @param thumbPath 썸네일 경로
     * @return
     */
    public String thumbNailFile(int width, int height, File originFile, String thumbPath) throws Exception {
        String thumbFileName = "";

        String fileName = originFile.getName();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        String randName = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
        thumbFileName = randName + "." + extension;

        try (
            InputStream in = new FileInputStream(originFile);
            BufferedInputStream bf = new BufferedInputStream(in);
        ) {

            // 원본 이미지 파일 뜨기
            BufferedImage originImage = ImageIO.read(originFile);

            // 이미지 사이즈 줄이기
            MultiStepRescaleOp scaleImage = new MultiStepRescaleOp(width, height);

            // 마스킹 처리
            scaleImage.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);

            // 리사이즈 이미지 생성
            BufferedImage resizeImage = scaleImage.filter(originImage, null);

            String thumbFilePath = thumbPath + thumbFileName;
            File resizeFile = new File(thumbFilePath);

            // 경로가 없다면 만들어준다
            if (!resizeFile.getParentFile().exists()) {

                // 모든 부모 파일 경로 만들기
                resizeFile.getParentFile().mkdirs();
            }

            // 리사이즈한 파일을 실제 경로에 생성. 결과를 리턴해준다.
            boolean isWrite = ImageIO.write(resizeImage, extension, resizeFile);

            if (!isWrite) {
                throw new RuntimeException("썸네일 생성 오류");
            }
            
        } catch (Exception e) {
            thumbFileName = null;
            e.printStackTrace();
            throw new RuntimeException("썸네일 생성 오류");
        }

        return thumbFileName;
    }
}
