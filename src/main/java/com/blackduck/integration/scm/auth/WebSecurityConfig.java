package com.blackduck.integration.scm.auth;

import javax.inject.Inject;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableWebSecurity
@EnableTransactionManagement
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Inject
	private HubAuthenticationProvider hubAuthenticationProvider;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//Allow public access to static resources
		http.authorizeRequests().antMatchers(HttpMethod.GET, "/img/**", "/js/**","/css/**").permitAll()
		//Must have codescanner role to configure builds
		.antMatchers(HttpMethod.PUT, "/builds/**").hasAnyAuthority("codescanner")
		.antMatchers(HttpMethod.POST, "/builds").hasAnyAuthority("codescanner")
		.antMatchers(HttpMethod.DELETE, "/builds/**").hasAnyAuthority("codescanner")
		//Authenticate everything else
		.anyRequest().authenticated().and().httpBasic()
		//Add logout URL
		.and().formLogin().loginPage("/login").permitAll()
		.and().csrf().ignoringAntMatchers("/login");
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(hubAuthenticationProvider);
	}

}