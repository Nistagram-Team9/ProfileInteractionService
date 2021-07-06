package devops.tim9.profileinteraction.service;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import devops.tim9.profileinteraction.dto.UserDto;
import devops.tim9.profileinteraction.model.User;
import devops.tim9.profileinteraction.repository.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
@EmbeddedKafka(partitions = 1, controlledShutdown = false, brokerProperties = { "listeners=PLAINTEXT://localhost:9093",
		"port=9093" })
public class UserServiceTest {
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	TestRestTemplate testRestTemplate;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	
	@Test
	public void follow_test_happy() throws Exception {
		userRepository.save(new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny",
				"johnny.web", "biography", false, true, true, "123")));
		userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnnyUser3",
				"johnny.web", "biography", false, true, true, "123"));
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				"johnnyUser3", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		User user = userRepository.findByUsername("johnny");
		userService.follow(user.getId());
		User user2 = userRepository.findByUsername("johnnyUser3");
		assertEquals("johnny", user2.getFollowingUsers().get(0).getUsername());
		
	}
	
	@Test
	public void block_test_happy() throws Exception {
		userRepository.save(new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny2",
				"johnny.web", "biography", false, true, true, "123")));
		userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnnyUser4",
				"johnny.web", "biography", false, true, true, "123"));
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				"johnnyUser4", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		User user = userRepository.findByUsername("johnny2");
		userService.block(user.getId());
		User user2 = userRepository.findByUsername("johnnyUser4");
		assertEquals("johnny2", user2.getBlockedProfiles().get(0).getUsername());
		
	}
	
	@Test
	public void report_test_happy() throws Exception {
		userRepository.save(new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny3",
				"johnny.web", "biography", false, true, true, "123")));
		userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnnyUser5",
				"johnny.web", "biography", false, true, true, "123"));
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				"johnnyUser5", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		User user = userRepository.findByUsername("johnny3");
		userService.report(user.getId());
		User user2 = userRepository.findByUsername("johnnyUser5");
		assertEquals("johnny3", user2.getReportedProfiles().get(0).getUsername());
		
	}
	
	
	
	

}
