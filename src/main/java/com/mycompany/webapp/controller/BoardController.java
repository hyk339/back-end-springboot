package com.mycompany.webapp.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycompany.webapp.dto.Board;
import com.mycompany.webapp.dto.Pager;
import com.mycompany.webapp.service.BoardService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/board")
@Slf4j
public class BoardController {
	@RequestMapping("/test")
	public Board test() {
	      log.info("실행");
	      Board board = new Board();
	      board.setBno(1);
	      board.setBtitle("제목");
	      board.setBcontent("내용");
	      board.setMid("user");
	      board.setBdate(new Date());
	      return board;
	}
	
	@Resource
	private BoardService boardService;
	
	//pager도 넘어갈수 있도록 return 타입 map으로 설정
	@GetMapping("/list")
	public Map<String, Object> list(@RequestParam(defaultValue = "1") int pageNo){
		log.info("실행");
		int totalRows = boardService.getTotalBoardNum();
		Pager pager = new Pager(5,5, totalRows, pageNo);
		List<Board> list = boardService.getBoards(pager);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("boards", list);
		map.put("pager", pager);
		return map;
	}
	
	@GetMapping("/{bno}")
	public Board read(@PathVariable int bno, @RequestParam(defaultValue = "false") boolean hit) {
		log.info("실행");
		Board board = boardService.getBoard(bno, hit);
		return board;
	}
	
	@PostMapping("/create")
	public Board create(Board board) {
		log.info("실행");
		//attach가 있고 파일만 없어도 넘어온다.
		//multipart 파일이 있냐없냐를 확인하려면 null이냐 아니냐를 먼저 확인해주어야한다.
		//null이 아니라도 첨부파일이 없을수 있으므로 한번더 확인한다.
		//파일 정보가 비어있는지 확인한다.
		if(board.getBattach() != null && !board.getBattach().isEmpty()) {
			MultipartFile mf = board.getBattach();
			board.setBattachoname(mf.getOriginalFilename());
			board.setBattachsname(new Date().getTime() + "-" + mf.getOriginalFilename());
			board.setBattachtype(mf.getContentType());
			try {
			File file = new File("C:/hyundai_ite/upload_files/"+board.getBattachsname());
			mf.transferTo(file);
			} catch(Exception e) {}
		}
		boardService.writeBoard(board);
		board = boardService.getBoard(board.getBno(), false);
		return board;
	}
	
	//Rest에서는 update는 put이거나 patch여야한다.
	//전체를 수정할때는 put
	//일부분만 수정할때 patch를 쓴다고한다. // 그런데 일반적으로 put을 많이 쓴다.
	
	//Multipart/form-data로 데이터를 전송할 때에는 PUT, PATCH는 사용할 수 없다.
	//첨부파일을 바꿀수 없다면 PUT이나 PATCH가 적절하다.
	//POST 방식만 가능
	@PostMapping("/update")
	public Board update(Board board) {
		log.info("실행");
		if(board.getBattach() != null && !board.getBattach().isEmpty()) {
			MultipartFile mf = board.getBattach();
			board.setBattachoname(mf.getOriginalFilename());
			board.setBattachsname(new Date().getTime() + "-" + mf.getOriginalFilename());
			board.setBattachtype(mf.getContentType());
			try {
			File file = new File("C:/hyundai_ite/upload_files/"+board.getBattachsname());
			mf.transferTo(file);
			} catch(Exception e) {}
		}
		boardService.updateBoard(board);
		board = boardService.getBoard(board.getBno(), false);
		return board;
	}
	
	@DeleteMapping("/{bno}")
	public Map<String, String> delete(@PathVariable int bno){
		log.info("실행");
		boardService.removeBoard(bno);
		Map<String, String> map = new HashMap<String, String>();
		map.put("result", "success");
		return map;
	}
	
	@GetMapping("/battach/{bno}")
	public void download(@PathVariable int bno, HttpServletResponse response) {
		try {
		Board board = boardService.getBoard(bno, false);
		String battachoname = board.getBattachoname();
		if(battachoname == null) return;
		
		//파일 이름이 한글로 되어 있을 경우, 응답 헤더에 한글을 넣을 수 없기 때문에 변환해야 함.
		battachoname = new String(battachoname.getBytes("UTF-8"), "ISO-8859-1");
		String battachsname = board.getBattachsname();
		String battachtype = board.getBattachtype();
		
		//응답 생성
		//Content-Disposition: attachment; filename="a.jpg";
		response.setHeader("Content-Disposition", "attachment; filename=\""+ battachsname+"\";");
		response.setContentType(battachtype);
		InputStream is = new FileInputStream("C:/hyundai_ite/upload_files/"+battachsname);
		OutputStream os = response.getOutputStream();
		FileCopyUtils.copy(is, os);
		is.close();
		os.flush();
		os.close();
		} catch(Exception e) {
		}
	}
}
