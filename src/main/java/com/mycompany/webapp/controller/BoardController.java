package com.mycompany.webapp.controller;

import java.util.Date;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.webapp.dto.Board;

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
}
