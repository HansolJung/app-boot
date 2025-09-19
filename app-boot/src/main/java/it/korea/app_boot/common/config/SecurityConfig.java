package it.korea.app_boot.common.config;


import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.crypto.password.Md4PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import it.korea.app_boot.common.handler.LoginFailureHanlder;
import it.korea.app_boot.common.handler.LoginSuccessHandler;
import it.korea.app_boot.common.handler.LogoutHandler;
import it.korea.app_boot.user.service.UserServiceDetails;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserServiceDetails userServiceDetails;

    // 시큐리티 무시하기
    // WebSecurityCustomizer 는 추상화 메서드를 하나만 가지는 함수형 인터페이스기 때문에,
    // 추상화 메서드 customize() 를 람다식으로 구현해서 리턴할 수 있다.
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web
            .ignoring()
            .requestMatchers("/static/imgs/**", "/static/img/**")    // 우리가 직접 만든 외부 파일 연동 링크
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
            // 마지막 명령어는 스프링 리소스 관련 처리. 아래와 같은 모든 경로들을 처리한다.
            /*
             * 1. classpath:/META-INF/resources/    <- 라이브러리 리소스 폴더
             * 2. classpath:/resources/
             * 3. classpath:/static/
             * 4. classpath:/public/   
             */
    }

    // 모든 인증 허용
    // @Bean
    // public WebSecurityCustomizer webSecurityCustomizer() {
    //     return web -> web.ignoring()
    //                      .requestMatchers("/**") // 모든 경로에 대해 인증 허용
    //                      .permitAll();
    // }

    // 보안 처리
    // Security 6 의 특징: 메서드 파라미터를 전부 함수형 인터페이스로 처리한다
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth      // 인증, 비인증 경로 처리      
                .requestMatchers("/user/login/**").permitAll()    // permitAll 은 인증 처리하지 않는다는 뜻
                .requestMatchers("/user/login/error").permitAll()
                .requestMatchers("/user/logout/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/.well-known/**").permitAll()   // chrome dev-tool 에러 처리
                .requestMatchers("/favicon.ico").permitAll()  // favicon 에러 처리
                .requestMatchers(HttpMethod.GET, "/gal/**", "/api/v1/gal/**").permitAll()  // GET 방식인 /gal 은 모두 허용
                .requestMatchers(HttpMethod.GET, "/board/**", "/api/v1/board/**").permitAll()   // GET 방식인 /board 는 모두 허용
                .requestMatchers("/admin/**", "/api/v1/admin/**").hasAnyRole("ADMIN")   // ADMIN 권한을 가지고 있어야만 허용
                .anyRequest().authenticated())   // 위의 설정들을 제외한 모든 요청은 인증 처리하겠다는 뜻
            .formLogin(form -> form
                .loginPage("/user/login")   // 내가 만든 로그인 페이지 경로
                .loginProcessingUrl("/login/proc")  // 로그인 처리 시작 경로
                .successHandler(new LoginSuccessHandler())   // 성공할 경우
                .failureHandler(new LoginFailureHanlder()))   // 실패할 경우
            .logout(logout -> logout
                .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher("/logout"))
                //.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .invalidateHttpSession(true)   // 스프링 세션 제거
                .deleteCookies("JSESSIONID")    // 세션 ID 제거
                .clearAuthentication(true)   // 로그인 객체 삭제
                .logoutSuccessHandler(new LogoutHandler()))   // 로그아웃 후 처리
            .exceptionHandling(exp -> exp
                .defaultAuthenticationEntryPointFor(   // 인증받지 않은 API 요청에 대해서는 401 응답 (리다이렉트 하지 않음)
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                    //new AntPathRequestMatcher("/api/**"))
                .defaultAccessDeniedHandlerFor(   // 인증은 받았으나 권한이 없는 API 요청에 대해서는 403 응답 (리다이렉트 하지 않음)
                    new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN), 
                    PathPatternRequestMatcher.withDefaults().matcher("/api/**")));
                    //new AntPathRequestMatcher("/api/**")));
            
        return http.build();
    }

    // auth provider 생성해서 전달 > 사용자가 만든 것을 전달한다.
    @Bean
    public AuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userServiceDetails);
        provider.setPasswordEncoder(bcyPasswordEncoder());

        return provider;
    }

    // 패스워드 암호화 객체 설정
    @Bean
    public PasswordEncoder bcyPasswordEncoder() {
        // 단방향 암호화 방식. 복호화 없음. 값 비교는 가능.
        return new BCryptPasswordEncoder();
    }
}
