package devops.tim9.profileinteraction.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import devops.tim9.profileinteraction.model.User;
import devops.tim9.profileinteraction.service.UserService;


@RestController
@RequestMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

	private UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping(value = "/follow/{id}")
	public ResponseEntity<Boolean> get(@PathVariable Integer id) {
		Boolean result = userService.follow(id);
		return new ResponseEntity<>(result, HttpStatus.OK);
		
	}
	
	@GetMapping(value = "/follow-requests")
	public ResponseEntity<List<User>> getFollowRequests() {
		List<User> followRequests = userService.getFollowRequests();
		return new ResponseEntity<>(followRequests, HttpStatus.OK);
		
	}
	
	@GetMapping(value = "/mute/{id}")
	public ResponseEntity<String> mute(@PathVariable Integer id) {
		userService.mute(id);
		return new ResponseEntity<>("Profile successfully muted.", HttpStatus.OK);
		
	}
	
	@GetMapping(value = "/block/{id}")
	public ResponseEntity<String> block(@PathVariable Integer id) {
		userService.block(id);
		return new ResponseEntity<>("Profile successfully blocked.", HttpStatus.OK);
		
	}
	
	@GetMapping(value = "/report/{id}")
	public ResponseEntity<String> report(@PathVariable Integer id) {
		userService.block(id);
		return new ResponseEntity<>("Profile successfully reported.", HttpStatus.OK);
		
	}
	
	@GetMapping(value = "/search/{username}")
	public ResponseEntity<User> search(@PathVariable String username) {
		User user = userService.search(username);
		return new ResponseEntity<>(user, HttpStatus.OK);
		
	}
}
