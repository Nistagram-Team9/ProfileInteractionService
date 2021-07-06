package devops.tim9.profileinteraction.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import devops.tim9.profileinteraction.config.JwtAuthenticationRequest;
import devops.tim9.profileinteraction.config.WebSecurityConfig;
import devops.tim9.profileinteraction.dto.MessageDto;
import devops.tim9.profileinteraction.dto.UserDto;
import devops.tim9.profileinteraction.model.User;
import devops.tim9.profileinteraction.repository.UserRepository;
import devops.tim9.profileinteraction.security.Authority;
import devops.tim9.profileinteraction.security.Role;
import devops.tim9.profileinteraction.security.UserTokenState;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
@EmbeddedKafka(partitions = 1, controlledShutdown = false, brokerProperties = { "listeners=PLAINTEXT://localhost:9093",
		"port=9093" })
public class UserControllerTest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WebSecurityConfig webSecurityConfig;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	public void follow_test_happy() throws Exception {
		userRepository.save(new User(new UserDto("Jane", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"jane", "johnny.web", "biography", false, true, true, "123")));
		User user1 =new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny", "johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny","123",null));
		User user = userRepository.findByUsername("jane");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<Boolean> responseEntity = testRestTemplate.exchange("/users/follow/"+user.getId(), HttpMethod.GET, httpEntity,
				Boolean.class);
		assertTrue(responseEntity.getBody());

	}
	
	@Test
	public void follow_test_sad() throws Exception {
		userRepository.save(new User(new UserDto("Milly", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"milly", "johnny.web", "biography", true, true, true, "123")));
		User user1 =new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny2", "johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny2","123",null));
		User user = userRepository.findByUsername("milly");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<Boolean> responseEntity = testRestTemplate.exchange("/users/follow/"+user.getId(), HttpMethod.GET, httpEntity,
				Boolean.class);
		assertFalse(responseEntity.getBody());

	}
	
	@Test
	public void followRequests_test_happy() throws Exception {
		User user2 = new User(new UserDto("Milly", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"milly2", "johnny.web", "biography", true, true, true, "123"));
		user2.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities2 = new ArrayList<>();
		authorities2.add(new Authority(Role.ROLE_ADMIN));
		user2.setAuthorities(authorities2);
		userRepository.save(user2);
		User user1 =new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny3", "johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny3","123",null));
		User user = userRepository.findByUsername("milly2");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		testRestTemplate.exchange("/users/follow/"+user.getId(), HttpMethod.GET, httpEntity,
				Boolean.class);
		UserTokenState userTokenState2 = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("milly2","123",null));
		HttpHeaders headers2 = new HttpHeaders();
		headers2.add("Authorization", "Bearer " + userTokenState2.getAccessToken());
		HttpEntity<Long> httpEntity2 = new HttpEntity<>(headers2);
		ResponseEntity<Object> responseEntity2 = testRestTemplate.exchange("/users/follow-requests", HttpMethod.GET, httpEntity2,
				Object.class);
		@SuppressWarnings("unchecked")
		List<User> users=(List<User>) responseEntity2.getBody();
		assertEquals(1, users.size());
		

	}
	
	@Test
	public void mute_test_happy() throws Exception {
		userRepository.save(new User(new UserDto("Milly", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"milly4", "johnny.web", "biography", true, true, true, "123")));
		User user1 =new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny4", "johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny4","123",null));
		User user = userRepository.findByUsername("milly4");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<MessageDto> responseEntity = testRestTemplate.exchange("/users/mute/"+user.getId(), HttpMethod.GET, httpEntity,
				MessageDto.class);
		MessageDto messageDto = responseEntity.getBody();
		assertEquals("Success", messageDto.getStatus());
		User checkMuted = userRepository.findByUsername("johnny4");
		assertEquals("milly4", checkMuted.getMutedProfiles().get(0).getUsername());
	}
	
	@Test
	public void block_test_happy() throws Exception {
		userRepository.save(new User(new UserDto("Milly", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"milly5", "johnny.web", "biography", true, true, true, "123")));
		User user1 =new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny5", "johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny5","123",null));
		User user = userRepository.findByUsername("milly5");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<MessageDto> responseEntity = testRestTemplate.exchange("/users/block/"+user.getId(), HttpMethod.GET, httpEntity,
				MessageDto.class);
		MessageDto messageDto = responseEntity.getBody();
		assertEquals("Success", messageDto.getStatus());
		User checkMuted = userRepository.findByUsername("johnny5");
		assertEquals("milly5", checkMuted.getBlockedProfiles().get(0).getUsername());
	}
	
	@Test
	public void report_test_happy() throws Exception {
		userRepository.save(new User(new UserDto("Milly", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"milly6", "johnny.web", "biography", true, true, true, "123")));
		User user1 =new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny6", "johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny6","123",null));
		User user = userRepository.findByUsername("milly6");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<MessageDto> responseEntity = testRestTemplate.exchange("/users/report/"+user.getId(), HttpMethod.GET, httpEntity,
				MessageDto.class);
		MessageDto messageDto = responseEntity.getBody();
		assertEquals("Success", messageDto.getStatus());
		User checkMuted = userRepository.findByUsername("johnny6");
		assertEquals("milly6", checkMuted.getReportedProfiles().get(0).getUsername());
	}
	
	@Test
	public void search_test_happy() throws Exception {
		userRepository.save(new User(new UserDto("Milly", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"milly7", "johnny.web", "biography", true, true, true, "123")));
		User user1 =new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny7", "johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny7","123",null));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<User> responseEntity = testRestTemplate.exchange("/users/search/milly7", HttpMethod.GET, httpEntity,
				User.class);
		User user = responseEntity.getBody();
		assertEquals("milly7", user.getUsername());
	}
	
	@Test
	public void search_test_sad() throws Exception {
		User user1 =new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.",
				"johnny8", "johnny.web", "biography", false, true, true, "123"));
		user1.setPassword(this.passwordEncoder.encode("123"));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user1.setAuthorities(authorities);
		userRepository.save(user1);
		UserTokenState userTokenState = webSecurityConfig
				.loginTesting(new JwtAuthenticationRequest("johnny8","123",null));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + userTokenState.getAccessToken());
		HttpEntity<Long> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<User> responseEntity = testRestTemplate.exchange("/users/search/milly90", HttpMethod.GET, httpEntity,
				User.class);
		User user = responseEntity.getBody();
		assertNull(user);
	}
	

}
