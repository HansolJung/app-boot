package it.korea.app_boot.admin.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import it.korea.app_boot.admin.dto.AdminUserDTO;
import it.korea.app_boot.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@Slf4j
public class AdminUserController {

    private final AdminUserService userService;

    /**
     * 회원 리스트
     * @param pageable 페이징 객체
     * @return
     */
    @GetMapping("/list")
    public ModelAndView getUserListView(@PageableDefault(page = 0, size = 10, 
            sort = "createDate", direction = Direction.DESC) Pageable pageable) {

        ModelAndView view = new ModelAndView();
        Map<String, Object> resultMap = new HashMap<>();

        try {
            resultMap = userService.getUserList(pageable);
            log.info("### 회원 리스트 출력 ###", resultMap);
        } catch (Exception e) {
            log.error("######## 회원 리스트 출력 에러 ########");
            e.printStackTrace();
        }

        view.addObject("data", resultMap);
        view.setViewName("views/admin/list");
        
        return view;
    }

    /**
     * 회원 등록 폼
     * @return
     */
    @GetMapping("/create/form")
    public ModelAndView createForm() {
        ModelAndView view = new ModelAndView();
        view.setViewName("views/admin/createForm");
        
        return view;
    }

    /**
     * 회원 수정 폼
     * @param userId 회원 아이디
     * @return
     * @throws Exception
     */
    @GetMapping("/info/{userId}")
    public ModelAndView updateForm(@PathVariable(name = "userId") String userId) {
        ModelAndView view = new ModelAndView();
        view.setViewName("views/admin/detail");

        try {
            AdminUserDTO userDTO = userService.getUser(userId);
            view.addObject("vo", userDTO);

        } catch (Exception e) {
            log.error("회원 정보 가져오기 오류");
            e.printStackTrace();
        }
        
        return view;
    }
}
