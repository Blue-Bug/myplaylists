package com.myplaylists.web.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/setting/*","/posts/create","/posts/{id}/remove",
                        "/posts/{id}/edit").authenticated()
                .mvcMatchers("/","/login","/sign-up","/email-verify",
                        "/posts/{id}").permitAll()
                .mvcMatchers(HttpMethod.GET,"/profile/*").permitAll()
                .anyRequest().denyAll();

        http.formLogin()
                .loginPage("/login")
                .successHandler(new SavedRequestAwareAuthenticationSuccessHandler())
                .permitAll();

        http.logout().logoutSuccessUrl("/");

        //사용자 세션을 1개로 제한
        http.sessionManagement(session -> session
                        .maximumSessions(1)
                );
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .mvcMatchers("/bootstrap/**","/image/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}
