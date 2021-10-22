package com.mycompany.webapp.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.webapp.dto.Board;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rest")
@Slf4j
public class TestRestController {
	//requestmapping은 get, post, put, delete등 모두 지원한다.
	@RequestMapping("/getObject")
	public Board getObject() {
	      log.info("실행");
	      Board board = new Board();
	      board.setBno(1);
	      board.setBtitle("제목");
	      board.setBcontent("내용");
	      board.setMid("user");
	      board.setBdate(new Date());
	      return board;
	}
	
	@RequestMapping("getMap")
	public Map<String, Object> getMap() {
	   log.info("실행");
	   
	   Map<String, Object> map = new HashMap<>();
	   map.put("name", "홍길동");
	   map.put("age", 25);
	   
	   Board board = new Board();
	   board.setBno(1);
	   board.setBtitle("제목");
	   board.setBcontent("내용");
	   board.setMid("user");
	   board.setBdate(new Date());
	   map.put("board", board);
	   
	   return map;
	}
	
	@RequestMapping("/getArray")
	public String[] getArray() {
	   log.info("실행");
	   String[] array = {
	      "Java",
	      "Spring",
	      "Thymeleaf"
	   };
	   return array;
	}
		
	@RequestMapping("/getList1")
	public List<String> getList1() {
	   log.info("실행");
	   List<String> list = new ArrayList<>();
	   list.add("RestController");
	   list.add("JSON");
	   list.add("AJAX");
	   return list;
	}
	
	@RequestMapping("getList2")
	public List<Board> getList2() {
	   log.info("실행");
	   List<Board> list = new ArrayList<>();
	   for(int i=1; i<=3; i++) {
	      Board board = new Board();
	      board.setBno(i);
	      board.setBtitle("제목" + i);
	      board.setBcontent("내용" + i);
	      board.setMid("user");
	      board.setBdate(new Date());
	      list.add(board);
	   }
	   return list;
	}
	
}
