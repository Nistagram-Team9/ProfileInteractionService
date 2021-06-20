package devops.tim9.profileinteraction.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import devops.tim9.profileinteraction.config.domain.LoginEvent;
import devops.tim9.profileinteraction.model.User;
import devops.tim9.profileinteraction.security.Role;
import devops.tim9.profileinteraction.security.UserTokenState;
import devops.tim9.profileinteraction.security.VerificationToken;
import devops.tim9.profileinteraction.service.UserService;
import devops.tim9.profileinteraction.service.VerificationTokenService;
import lombok.AllArgsConstructor;




@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@AllArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	private TokenHelper tokenHelper;
	private PasswordEncoder passwordEncoder;
	private UserService userService;
	private VerificationTokenService verificationTokenService;
	private ObjectMapper objectMapper;


	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
			// communication between client and server is stateless
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
			.authorizeRequests()
			.antMatchers("/auth/login").permitAll()
			.antMatchers("/auth/registerAdmin").permitAll()
			.antMatchers("/users/**").permitAll()

				//antMatchers("/auth/registerLibrarian").permitAll()
			//.antMatchers("/books/**").permitAll()
			//.antMatchers("/bookCopies/**").permitAll()
			//.antMatchers("/users/**").permitAll()
			//.antMatchers("/bookRent/**").permitAll()
			// every request needs to be authorized
			.anyRequest().authenticated().and()
			// add filter before every request
			.addFilterBefore(new TokenAuthenticationFilter(tokenHelper, userService),
				BasicAuthenticationFilter.class);
		http.csrf().disable();

	}

	public void configure(WebSecurity web) throws Exception {
		// Token Filter will ignore these paths
		web.ignoring().antMatchers(HttpMethod.POST, "/auth/login", "/h2/**");
		web.ignoring().antMatchers(HttpMethod.GET, "/", "/login", "/h2/**", "/webjars/**", "/*.html", "/favicon.ico",
			"/**/*.html", "/**/*.css", "/**/*.js");

		
	}
	


	public UserTokenState login(JwtAuthenticationRequestToSend authenticationRequestToSend) throws Exception {
		final Authentication authentication;
		try {
			authentication = authenticationManagerBean().authenticate(new UsernamePasswordAuthenticationToken(
					authenticationRequestToSend.getUsername(), authenticationRequestToSend.getPassword()));
		} catch (BadCredentialsException e) {
			return null;
		}
		User user = (User) authentication.getPrincipal();
		SecurityContextHolder.getContext().setAuthentication(authentication);
//		String jwt = tokenHelper.generateToken(user.getUsername());
		int expiresIn = tokenHelper.getExpiredIn();
		Role role = null;
		if (user.getAuthoitiesList().get(0).getRole().equals(Role.ROLE_ADMIN)) {
			role = Role.ROLE_ADMIN;
		} else {
			role = Role.ROLE_USER;
		}
		
		VerificationToken verificationToken = new VerificationToken();
		String jwt = authenticationRequestToSend.getToken();
		verificationToken.setToken(jwt);
		verificationToken.setUser(user);
		verificationTokenService.saveToken(verificationToken);
		System.out.println("TOKEEN");
		System.out.println(verificationTokenService.findByToken(jwt).getToken());;
//		System.out.println(userService.findUserByToken(jwt).getUsername());
		
		return new UserTokenState(jwt, expiresIn, role);
	}

	public User changePassword(String oldPassword, String newPassword) throws Exception {
		Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
		String username = currentUser.getName();
		authenticationManagerBean().authenticate(new UsernamePasswordAuthenticationToken(username, oldPassword));
		User user = (User) userService.loadUserByUsername(username);
		user.setPassword(passwordEncoder.encode(newPassword));
		userService.create(user);
		return user;
	}
	
//
//	@KafkaListener(topics = {"login-events"})
//	public void onMessage(ConsumerRecord<Integer, String> consumerRecord) {
//		System.out.println("Consumer record accepted: " + consumerRecord);
//		System.out.println(consumerRecord);
//		
//		String value = consumerRecord.value();
//		try {
//			LoginEvent loginEvent = objectMapper.readValue(value, LoginEvent.class);
//			System.out.println("login event");
//			System.out.println(loginEvent.toString());
//			JwtAuthenticationRequestToSend request = loginEvent.getAuthenticationRequest();
//			System.out.println("tokennnnn");
//			System.out.println(loginEvent.getAuthenticationRequest());
//			try {
//				this.login(request);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		} catch (JsonMappingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
//
//	}


}

