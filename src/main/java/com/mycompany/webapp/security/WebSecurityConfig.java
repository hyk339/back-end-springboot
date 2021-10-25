package com.mycompany.webapp.security;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.extern.slf4j.Slf4j;

@EnableWebSecurity
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Resource
	private DataSource dataSource;
	
	@Resource
	private CustomUserDetailsService customUserDetailsService;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {	
		log.info("configure(HttpSecurity http) 실행");
		//폼 로그인 비활성화
		/*
		 이거를 하지 않으면 인증이 안된상태에서 board요청이 들어오면 로그인 폼이 제공된다.
		 client가 로그인 폼을 받을수 잇는 상황이 아니니까 우리는 지금 비활성화 시킨상태다.
		 인증이 안되었는데 board요청을 하면 로그인 폼으로 보낼텐데
		 우리는 ajax로 요청했기때문에 loginform을 표시할수도 없다.
		 그래서 비활성화했다.
		 */
		http.formLogin().disable();
		
		//사이트간 요청 위조 방지 비활성화
		http.csrf().disable();
		
		//요청 경로 권한 설정
		http.authorizeRequests()
			//아래 board의 요청은 인증이 되어야한다.
			.antMatchers("/board/**").authenticated()
			.antMatchers("/**").permitAll();
		
		//세션 비활성화
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		//JwtCheckFilter 추가
		//id와 password를 받아서 체크하는 filter전에 jwtcheckfilter가 있어야한다.
		JwtCheckFilter jwtCheckFilter = new JwtCheckFilter();
		http.addFilterBefore(jwtCheckFilter, UsernamePasswordAuthenticationFilter.class);
		
		
		
		/*
		 * 어떻게 설정한 것에 대해서만 활성화 시킬지 우리가 정책을 작성해주어야한다.
		 */
		
		//CORS 설정 활성화
		http.cors();
		
	}	
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		log.info("configure(AuthenticationManagerBuilder auth) 실행");
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(customUserDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		auth.authenticationProvider(provider);
	}	
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		log.info("configure(WebSecurity web) 실행");
		DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
		defaultWebSecurityExpressionHandler.setRoleHierarchy(roleHierarchyImpl());	
		web.expressionHandler(defaultWebSecurityExpressionHandler);
		web.ignoring()
		.antMatchers("/images/**")
		.antMatchers("/css/**")
		.antMatchers("/js/**")
		.antMatchers("/bootstrap-4.6.0-dist/**")
		.antMatchers("/jquery/**")
		.antMatchers("/favicon.ico");		
	}	
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		//return new BCryptPasswordEncoder();
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
	
	@Bean
	public RoleHierarchyImpl roleHierarchyImpl() {
		log.info("실행");
		RoleHierarchyImpl roleHierarchyImpl = new RoleHierarchyImpl();
		roleHierarchyImpl.setHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_USER");
		return roleHierarchyImpl;
	}
	
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	//springsecurity가 실행되면 이 객체를 찾을 것이다.
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration conf = new CorsConfiguration();
		//모든 요청 사이트 허용
		conf.addAllowedOrigin("*");
		//모든 요청 방식 허용
		conf.addAllowedMethod("*");
		//모든 요청 헤드 허용
		conf.addAllowedHeader("*");//"Authorization"라고 넣어도 된다. //"*"는 모든 header를 허용하겠다는 것이다.
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", conf);
		return source;
	}
}
 
 