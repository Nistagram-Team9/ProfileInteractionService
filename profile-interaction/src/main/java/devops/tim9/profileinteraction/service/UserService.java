package devops.tim9.profileinteraction.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import devops.tim9.profileinteraction.config.domain.UserEvent;
import devops.tim9.profileinteraction.dto.UserDto;
import devops.tim9.profileinteraction.model.User;
import devops.tim9.profileinteraction.repository.UserRepository;
import devops.tim9.profileinteraction.security.Authority;
import devops.tim9.profileinteraction.security.Role;

import org.springframework.security.core.userdetails.UserDetailsService;


@Service
public class UserService implements UserDetailsService{
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ObjectMapper objectMapper;
	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.objectMapper = objectMapper;
	}
	
	@KafkaListener(topics = {"user-events"})
	public void onMessage(ConsumerRecord<Integer, String> consumerRecord) {
		String value = consumerRecord.value();
		try {
			UserEvent userEvent = objectMapper.readValue(value, UserEvent.class);
			User user = userEvent.getUser();
			if (userEvent.getAction().equalsIgnoreCase("registerUser") || userEvent.getAction().equalsIgnoreCase("registerAdmin") || userEvent.getAction().equalsIgnoreCase("update")) {
				this.create(user);
			} else if (userEvent.getAction().equalsIgnoreCase("delete")) {
				this.delete(user.getId());
			} 
			
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

	}
	
	public Boolean follow(Integer id) {
			
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		User userToFollow = this.findById(id);
		if (userToFollow.getIsPrivate() == true) {
			userToFollow.getFollowRequests().add(user);
			return false;
		}
		user.getFollowingUsers().add(userToFollow);
		userToFollow.getFollowers().add(user);
		return true;
	}
	
	public void mute(Integer id) {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		User userToMute = this.findById(id);
		user.getMutedProfiles().add(userToMute);
	
	}
	
	public List<User> getFollowRequests(){
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		return user.getFollowRequests();
	}
	
	public void block(Integer id) {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		User userToBlock = this.findById(id);
		user.getBlockedProfiles().add(userToBlock);
		userToBlock.getBlockedProfiles().add(user);
		
	}
	
	public void report(Integer id) {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		User userToReport = this.findById(id);
		user.getReportedProfiles().add(userToReport);
		userToReport.getReportedByProfiles().add(user);
		
	}
	
	public User search(String username) {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		User searchedUser = userRepository.findByUsername(username);
		if (searchedUser != null) {
			if (searchedUser.getIsPrivate() == false) {
				return searchedUser;
			} else {
				if (searchedUser.getFollowers().contains(user)) {
					return searchedUser;
				} else {
					return null;
				}
			}
		}
		return searchedUser;
		
		
	}
	

	public User findById(Integer id) {
		return userRepository.findById(id).orElse(null);
	}

	public User registerUser(UserDto userDto) throws Exception {
		System.out.println("users password");
		System.out.println(userDto.getPassword());
		if (this.usernameTaken(userDto.getUsername())) {
			throw new IllegalArgumentException("Username is already taken.");
		}
		User user = new User(userDto);
		user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
		System.out.println(user.getPassword());
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_USER));
		user.setAuthorities(authorities);
		this.create(user);
		return user;
	}
	
	public User registerAdmin(UserDto userDto) throws Exception {
		if (this.usernameTaken(userDto.getUsername())) {
			throw new IllegalArgumentException("Username is already taken.");
		}
		User user = new User(userDto);
		user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user.setAuthorities(authorities);
		this.create(user);
		return user;
	}
	
	public User update(Integer id, UserDto userDto) {
		User user = this.findById(id);
		if (user != null) {
			user.name = userDto.getName();
			user.surname = userDto.getSurname();
			user.email = userDto.getEmail();
			user.phoneNumber = userDto.getPhoneNumber();
			user.sex = userDto.getSex();
			user.birthDate = userDto.getBirthDate();
			user.username = userDto.getUsername();
			user.website = userDto.getWebsite();
			user.biography = userDto.getBiography();
			user.isPrivate = userDto.getIsPrivate();
			user.canBeTagged = userDto.getCanBeTagged();
			user.isActive = userDto.getIsActive();
			user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
			this.create(user);
			
		}
		return user;
	}


	public boolean usernameTaken(String username) {
		User user = userRepository.findByUsername(username);
		return user != null;
	}

	public List<User> getAll() {
		return userRepository.findAll();
	}

	public User create(User user) {
		return userRepository.save(user);
	}

	public User delete(Integer id) {
		Optional<User> user = userRepository.findById(id);
		userRepository.delete(user.get());
		return user.get();
	}
	
	public User findUserByToken(String token) {
		return userRepository.findByToken(token);
	}

	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username);
	}


}





