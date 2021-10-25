package com.mycompany.webapp.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class JwtCheckFilter extends OncePerRequestFilter{

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		log.info("실행");
		//JWT 얻기
		String jwt = null;
		if(request.getHeader("Authorization") != null && 
			request.getHeader("Authorization").startsWith("Bearer")) {
			jwt = request.getHeader("Authorization").substring(7);
		}
		
		log.info("jwt : "+jwt);
		if(jwt != null) {
			//JWT 유효성 검사
			Claims claims = JWTUtil.validateToken(jwt);
			if(claims != null) {
				log.info("유효한 토큰");
				//JWT에서 Payload 얻기
				String mid = JWTUtil.getMid(claims);
				String authority = JWTUtil.getAuthority(claims);
				log.info("mid : "+mid);
				log.info("authority : "+authority);
				/*
				 * 패스워드가 필요없다. 왜냐하면 이미 인증이 되었기 때문에
				 * DB와 관련이 없다. password를 주었던 이유는 DB에가서 id와 패스워드를 확인하기 위해서였다.
				 * 아래는 JWT를 가지고 인증을 세팅한것이다.
				 */
				//Security 인증 처리
				UsernamePasswordAuthenticationToken authentication = 
						new UsernamePasswordAuthenticationToken(mid, null, AuthorityUtils.createAuthorityList(authority)); //"ROLE_USER","ROLE_MANAGER"
				
				
				/*id와 password가 맞냐 안맞냐를 확인하는 코드, 필요없다.
				Authentication authentication = authenticationManager.authenticate(token); 
				*/
				SecurityContext securityContext = SecurityContextHolder.getContext();
				//아래가 setting이 되어서 UsernamePasswordAuthenticationFilter에서 확인하는 작업을 하지 않는다. 건너뛴다.
				//id, password 검사하는 것을 건너 뛴다.
				//authentication을 이미 만들었기 때문이다.
				securityContext.setAuthentication(authentication);
			} else {
				log.info("유효하지 않은 토큰");
			}
		}
		
		//다음 필터를 실행
		filterChain.doFilter(request, response);
	}
	
	
}
