package com.cafe.JWT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;


@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{
	@Autowired
	CustomerUsersDetailsService customerUsersDetailsService;
	
	@Autowired
	JwtFilter jwtFilter;

//	@Bean
//	public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
//	    StrictHttpFirewall firewall = new StrictHttpFirewall();
//	    firewall.setAllowUrlEncodedSlash(true);
//	    firewall.setAllowSemicolon(true);
//	    return firewall;
//	}
//	
//	
//	@Override
//	public void configure(WebSecurity web) throws Exception {
//		super.configure(web);
//		web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
//	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(customerUsersDetailsService);
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}

	@Bean(name = BeanIds.AUTHENTICATION_MANAGER)
	@Override
	protected AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues())
		.and()
		.csrf().disable()
		.authorizeRequests()
		.antMatchers("/user/login","/user/signup","/user/forgotPassword")
		.permitAll()
		.anyRequest()
		.authenticated()
		.and().exceptionHandling()
		.and()
		.sessionManagement()
		.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		http.addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class);
		
	}
	

	
	
	
	
	
	

//	
//	@Bean
//	  public DaoAuthenticationProvider authenticationProvider() {
//	      DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//	       
//	      authProvider.setUserDetailsService(customerUserDetailsService);
//	      authProvider.setPasswordEncoder(passwordEncoder());
//	   
//	      return authProvider;
//	  }
//	
//	@Bean
//	  public PasswordEncoder passwordEncoder() {
//	    return new BCryptPasswordEncoder();
//	  }
	
//	@Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//        .authorizeRequests()
//        .anyRequest()
//        .authenticated()
//        .and()
//        .httpBasic();
//        return http.build();
//    }
// 
}
