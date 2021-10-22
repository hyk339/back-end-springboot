package com.mycompany.webapp.security;

import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JWTUtil {
	//비밀키(노출이 되면 안됨)
	private static final String secretKey = "12345";
	
	//JWT 생성
	public static String createToken(String mid, String authority) { //authroiy가 맞다.
		//절대 JWT에 저장시켜서 안되는 것은 개인정보!!(비밀번호)
		//왜 안되냐?
		//사이트에다 가져다 붙이면 내용 다 나온다.
		//JWT안에 비밀번호 넣으면 절대 안된다.
		log.info("실행");
		String result = null;
		try {
			//헤더에는 알고리즘의 종류, 
			String token = Jwts.builder()
					//헤더 설정
					.setHeaderParam("alg", "HS256")
					.setHeaderParam("typ", "JWT")
					//토큰의 유효기간 설정 (유효기간이 지나면 해당 토큰은 서버가 인증 안한다. 기간이 얼마짜리 토큰을 만들지는 자유롭게)
					.setExpiration(new Date(new Date().getTime()+1000*60*60*24))
					//페이로드 설정 (페이로드를 다른 말로 claim이라고 한다.)
					.claim("mid", mid)
					.claim("authority", authority)
					//서명 설정 (signwith 다음에 알고리즘 이름을 넣는다.)
					.signWith(SignatureAlgorithm.HS256, secretKey.getBytes("UTF-8"))
					//토큰 생성 (압축해라)
					.compact();
			
			//이렇게 만든 토큰을 client에게 준다.
			result = token;
		} catch(Exception e) {
		}
		return result;
	}
	
	//client에서 받은 토큰이 맞는지 검사하는 곳
	
	//JWT 유효성 검사
	public static Claims validateToken(String token) {
		log.info("실행");

		Claims result = null;
		try {
			Claims claims = Jwts.parser()
						.setSigningKey(secretKey.getBytes())
						//맞지 않는게 들어오면 많은 예외가 발생한다. 마우스 올려보자.
						//jws는 signkey도 확인한다.
						.parseClaimsJws(token)
						.getBody();
			result = claims;
			//claims에서 날짜를 얻어서 현재날짜와 학인하는데 이후이냐?
			//true면 유효하다..
			//false면 유효기간 지났다..
			/*
				알아서 시간체크한다. 그래서 이부분 피룡 없다.
			boolean validate = claims.getExpiration().after(new Date());
			if(validate) {
				result = claims;
			}*/
		} catch(Exception e) {
		}
		return result;
	}
	
	
	//JWT에서 정보 얻기
	public static String getMid(Claims claims) {
		log.info("실행");
		//들어가있는 값이 String이기때문에 String.class를 썼다.
		return claims.get("mid", String.class);
		
	}

	public static String getAuthority(Claims claims) {
		log.info("실행");
		return claims.get("authority", String.class);
	}
	//확인
	/*
	public static void main(String[] args) throws Exception {
		//토큰 생성
		String mid = "user";
		String authority = "ROLE_USER";
		String jwt = createToken(mid,authority);
		log.info(jwt);
		
		//Thread.sleep(2000); //토큰 유효성 검사하려고 시간 1초로 바꾸고 테스트했던 부분
		
		//토큰 유효성 검사
		Claims claims = validateToken(jwt);
		if(claims != null) {
			log.info("유효한 토큰");
			log.info("mid : "+getMid(claims));
			log.info("authority : "+getAuthority(claims));
		} else {
			log.info("유효하지 않은 토큰");
		}
	}
	*/
}
