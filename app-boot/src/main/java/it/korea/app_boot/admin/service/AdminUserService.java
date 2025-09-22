package it.korea.app_boot.admin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.korea.app_boot.admin.dto.AdminUserDTO;
import it.korea.app_boot.admin.dto.AdminUserProjection;
import it.korea.app_boot.admin.dto.AdminUserRequestDTO;
import it.korea.app_boot.admin.dto.AdminUserSearchDTO;
import it.korea.app_boot.admin.dto.AdminUserUpdateRequestDTO;
import it.korea.app_boot.common.dto.PageVO;
import it.korea.app_boot.user.entity.UserEntity;
import it.korea.app_boot.user.entity.UserRoleEntity;
import it.korea.app_boot.user.repository.UserRepository;
import it.korea.app_boot.user.repository.UserRoleRepository;
import it.korea.app_boot.user.repository.UserSearchSpecification;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;   // security config 에서 bean 으로 등록했기 때문에 bcrypt를 똑같이 사용할 수 있음

    /**
     * 회원 리스트 가져오기
     * @param pageable 페이징 객체
     * @return
     * @throws Exception
     */
    @Transactional   // LAZY 모드로 가져오려면 Transactional 이어야 한다
    public Map<String, Object> getUserList(Pageable pageable) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        Page<UserEntity> pageList = userRepository.findAll(pageable);

        List<AdminUserDTO> userList = pageList.getContent().stream().map(AdminUserDTO::of).toList();

        PageVO pageVO = new PageVO();
        pageVO.setData(pageList.getNumber(), (int) pageList.getTotalElements());

        resultMap.put("total", pageList.getTotalElements());
        resultMap.put("content", userList);
        resultMap.put("pageHTML", pageVO.pageHTML());
        resultMap.put("page", pageList.getNumber());
        
        return resultMap;
    }

    /**
     * 회원 리스트 가져오기 (with Axios 호출 or 검색)
     * @param pageable 페이징 객체
     * @return
     * @throws Exception
     */
    @Transactional  // LAZY 모드로 가져오려면 Transactional 이어야 한다
    public Map<String, Object> getUserList(Pageable pageable, AdminUserSearchDTO searchDTO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        Page<UserEntity> pageList = null;

        // if (StringUtils.isNotBlank(searchDTO.getSearchText())) {
        //     pageList = userRepository.findByUserIdContainingOrUserNameContaining(searchDTO.getSearchText(), searchDTO.getSearchText(), pageable);
        // } else {
        //     pageList = userRepository.findAll(pageable);
        // }

        UserSearchSpecification searchSpecification = new UserSearchSpecification(searchDTO);
        pageList = userRepository.findAll(searchSpecification, pageable);

        List<AdminUserDTO> userList = pageList.getContent().stream().map(AdminUserDTO::of).toList();

        PageVO pageVO = new PageVO();
        pageVO.setData(pageList.getNumber(), (int) pageList.getTotalElements());

        resultMap.put("total", pageList.getTotalElements());
        resultMap.put("content", userList);
        resultMap.put("pageHTML", pageVO.pageHTML());
        resultMap.put("page", pageList.getNumber());
        
        return resultMap;
    }

    /**
     * 회원 상세정보 가져오기
     * @param userId 회원 아이디
     * @return
     * @throws Exception
     */
    @Transactional   // LAZY 모드로 가져오려면 Transactional 이어야 한다
    public AdminUserDTO getUser(String userId) throws Exception {
        AdminUserProjection user = userRepository.getUserById(userId)
            .orElseThrow(()-> new RuntimeException("회원 없음"));

        return AdminUserDTO.of(user);
       // return AdminUserDTO.of(userRepository.findById(userId).orElseThrow(()-> new RuntimeException("회원 없음")));
    }

    /**
     * 회원 등록하기
     * @param userRequestDTO 회원 등록 내용 DTO
     * @throws Exception
     */
    @Transactional
    public void createUser(AdminUserRequestDTO userRequestDTO) throws Exception {

        UserRoleEntity userRoleEntity = userRoleRepository.findById(userRequestDTO.getUserRole())  // 해당하는 권한이 존재하는지 체크
            .orElseThrow(()-> new RuntimeException("권한 없음"));

        if (!userRepository.findById(userRequestDTO.getUserId()).isPresent()) {   // 등록하려는 아이디와 동일한 회원이 없을 경우에만 회원 등록
            UserEntity userEntity = new UserEntity();
            userEntity.setUserId(userRequestDTO.getUserId());
            userEntity.setUserName(userRequestDTO.getUserName());
            userEntity.setPasswd(passwordEncoder.encode(userRequestDTO.getPasswd()));
            userEntity.setBirth(userRequestDTO.getBirth());
            userEntity.setGender(userRequestDTO.getGender());
            userEntity.setPhone(userRequestDTO.getPhone());
            userEntity.setEmail(userRequestDTO.getEmail());
            userEntity.setAddr(userRequestDTO.getAddr());
            userEntity.setAddrDetail(userRequestDTO.getAddrDetail());
            userEntity.setUseYn(userRequestDTO.getUseYn());
            userEntity.setDelYn("N");
            userEntity.setRole(userRoleEntity);
            
            userRepository.save(userEntity);
        } else {
            throw new RuntimeException("해당 아이디를 가진 회원이 이미 존재");
        }
    }

    /**
     * 회원 정보 수정하기
     * @param userRequestDTO 회원 정보 수정 내용 DTO
     * @throws Exception
     */
    @Transactional
    public void updateUser(AdminUserUpdateRequestDTO userRequestDTO) throws Exception {

        UserRoleEntity userRoleEntity = userRoleRepository.findById(userRequestDTO.getUserRole())  // 해당하는 권한이 존재하는지 체크
            .orElseThrow(()-> new RuntimeException("권한 없음"));

        UserEntity userEntity = userRepository.findById(userRequestDTO.getUserId())
            .orElseThrow(()-> new RuntimeException("사용자 없음"));

        userEntity.setUserId(userRequestDTO.getUserId());
        userEntity.setUserName(userRequestDTO.getUserName());

        if (StringUtils.isNotBlank(userRequestDTO.getPasswd())) {   // 비밀번호가 입력되어서 넘어왔을 경우에만 수정
            userEntity.setPasswd(passwordEncoder.encode(userRequestDTO.getPasswd()));
        }

        userEntity.setPhone(userRequestDTO.getPhone());
        userEntity.setEmail(userRequestDTO.getEmail());
        userEntity.setAddr(userRequestDTO.getAddr());
        userEntity.setAddrDetail(userRequestDTO.getAddrDetail());
        userEntity.setUseYn(userRequestDTO.getUseYn());
        userEntity.setRole(userRoleEntity);

        userRepository.save(userEntity);
    }

    /**
     * 회원 삭제 처리하기
     * @param userId 회원 아이디
     * @throws Exception
     */
    @Transactional
    public void deleteUser(String userId) throws Exception {

        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(()-> new RuntimeException("사용자 없음"));

        userEntity.setUseYn("N");  // 사용 여부 N로 변경
        userEntity.setDelYn("Y");  // 삭제 여부 Y로 변경

        userRepository.save(userEntity);
    }
}
