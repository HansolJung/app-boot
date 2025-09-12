package it.korea.app_boot.board.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import it.korea.app_boot.board.service.BoardJPAService;


@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardJPAService service;

    /**
     * 게시글 리스트 뷰
     * @return
     */
    @GetMapping("/list")
    public ModelAndView listView() {
        ModelAndView view = new ModelAndView();
        view.setViewName("views/board/boardList");
        
        return view;
    }

    /**
     * 게시글 상세 뷰
     * @param brdId 게시글 아이디
     * @return
     * @throws Exception
     */
    @GetMapping("/{brdId}")
    public ModelAndView detailView(@PathVariable(name = "brdId") int brdId) throws Exception {
        ModelAndView view = new ModelAndView();
        Map<String, Object> resultMap = new HashMap<>();
        
        resultMap = service.getBoard(brdId);
        view.addObject("vo", resultMap.get("vo"));

        view.setViewName("views/board/boardDetail");
        
        return view;
    }
    
    /**
     * 게시글 작성 폼
     * @return
     */
    @GetMapping("/add/form")
    public ModelAndView writeForm() {
        ModelAndView view = new ModelAndView();
        view.setViewName("views/board/writeForm");
        
        return view;
    }

    /**
     * 게시글 업데이트 폼
     * @param brdId 게시글 아이디
     * @return
     * @throws Exception
     */
    @GetMapping("/update/{brdId}")
    public ModelAndView updateForm(@PathVariable(name = "brdId") int brdId) throws Exception {
        ModelAndView view = new ModelAndView();
        Map<String, Object> resultMap = new HashMap<>();
        
        resultMap = service.getBoard(brdId);
        view.addObject("vo", resultMap.get("vo"));

        view.setViewName("views/board/updateForm");
        
        return view;
    }
}
