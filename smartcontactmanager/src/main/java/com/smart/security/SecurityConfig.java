package com.smart.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.smart.enums.UserRole;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{
	
	@Bean
	public UserDetailsService getUserDetailsService() {
		return new CustomUserDetailsServiceImpl();
	}
	
	@Bean
	public BCryptPasswordEncoder getBCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authenticationProvider=new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(getUserDetailsService());
		authenticationProvider.setPasswordEncoder(getBCryptPasswordEncoder());
		return authenticationProvider;
	}

	//For Authentication
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	//For Authorization
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/admin/**").hasRole(UserRole.ADMIN.toString());
		http.authorizeRequests().antMatchers("/user/**").hasRole(UserRole.USER.toString());
		http.authorizeRequests().antMatchers("/").permitAll();
		http.formLogin().loginPage("/signin");
		http.formLogin().loginProcessingUrl("/dologin");
		http.formLogin().defaultSuccessUrl("/user/index");
		//http.formLogin().failureUrl("");
		http.csrf().disable();
		http.headers().frameOptions().disable();
	}

}
