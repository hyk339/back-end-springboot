package com.mycompany.webapp.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.webapp.dto.Member;
import com.mycompany.webapp.security.JWTUtil;
import com.mycompany.webapp.service.MemberService;
import com.mycompany.webapp.service.MemberService.JoinResult;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/member")
@Slf4j
public class MemberController {
	@Resource
	private MemberService memberService;
	
	
	//WebSecurityConfig에서 주입시켰다. 가서 확인해보자. 81라인
	@Resource
	private AuthenticationManager authenticationManager;
	
	//왜 주입이 가능할까?
	//WebSecurityConfig.java => @Bean PasswordEncoder로 설정되어 있다.
	//즉, 관리객체로 만들었었다. 그래서 우리가 아래처럼 주입을 받을 수 있다.
	@Resource
	private PasswordEncoder passwordEncoder;
	
	@RequestMapping("/join1")
	public Map<String, String> join1(Member member){
		log.info("실행");
		member.setMenabled(true);
		member.setMpassword(passwordEncoder.encode(member.getMpassword()));
		// JoinResult는 MemberService.java에서 우리가 설정한 EnumType이다.
		JoinResult jr = memberService.join(member);
		Map<String, String> map = new HashMap<>();
		if(jr == JoinResult.SUCCESS) {
			map.put("result", "success");
		} else if(jr == JoinResult.DUPLICATED) {
			map.put("result", "duplicated");
		} else {
			map.put("result", "fail");
		}
		return map;
	}
	
	@RequestMapping("/join2")
	public Map<String, String> join2(@RequestBody Member member){
		log.info("실행");
		return join1(member);
	}
	
	@RequestMapping("/login1")
	public Map<String, String> login1(String mid, String mpassword){
		log.info("실행");
		if(mid == null || mpassword == null) {
			throw new BadCredentialsException("아이디 또는 패스워드가 제공되지 않았음");
		}
		
		//Spring Security 사용자 인증하기
		
		UsernamePasswordAuthenticationToken token = 
				new UsernamePasswordAuthenticationToken(mid, mpassword);
		/*
		아래 메서드가 springsecurity가 갖고있는 db정보를 통해서 사용자가 입력한 아이디와 패스워드를 검사해서
		성공적으로 id와 password가 넘어왔다면 authentication 객체를 리턴하는데
		만약 제대로 넘어오지 않았다면 위의 BadCredentialsException 예외가 발생한다.
		
		위에서 authenticate메서드를 사용하기 위해서 주입받았다.
		*/
		//여기에서 security가 인증처리할것이다. DB에 있는 아이디와 패스워드를 확인한다. 만약 인증이 성공하면
		//authroitcation객체에 담긴다.
		Authentication authentication = authenticationManager.authenticate(token);
		//SecurityContextHolder는 securityContext를 갖고있는 객체다.
		//여기서 SeucirtyContext를 환경설정할 수 있는 객체를 가져온다.
		SecurityContext securityContext = SecurityContextHolder.getContext();
		//결국 session에 인증정보를 저장할것이다.
		//아래는 인증정보를 저장하는 역할을 한다.
		//session저장을 막기위해 securiyconfig로 가보자.
		securityContext.setAuthentication(authentication);
		
		//응답 내용
		//authority 정보를 어떻게 얻느냐?
		//우리는 현재 member 하나당 mrole이 하나니까 아래처럼 만들었다.next()로 하나만 가져오게했다.
		String authority = authentication.getAuthorities().iterator().next().toString();
		log.info(authority);
		Map<String, String> map = new HashMap<>();
		map.put("result", "success");
		map.put("mid",mid);
		map.put("jwt",JWTUtil.createToken(mid, authority));
		return map;
	}
	
	@RequestMapping("/login2")
	public Map<String, String> login2(@RequestBody Member member){
		log.info("실행");
		return login1(member.getMid(),member.getMpassword());
	}
}
