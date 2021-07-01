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

import devops.tim9.profileinteraction.config.domain.FollowEvent;
import devops.tim9.profileinteraction.config.domain.UserEvent;
import devops.tim9.profileinteraction.dto.UserDto;
import devops.tim9.profileinteraction.model.User;
import devops.tim9.profileinteraction.producer.FollowEventProducer;
import devops.tim9.profileinteraction.repository.UserRepository;
import devops.tim9.profileinteraction.security.Authority;
import devops.tim9.profileinteraction.security.Role;

import org.springframework.security.core.userdetails.UserDetailsService;


@Service
public class UserService implements UserDetailsService{

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ObjectMapper objectMapper;
	
	@Autowired
	FollowEventProducer followEventProducer;

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

		System.out.println("Uslo u follow user");
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		User userToFollow = this.findById(id);
		if (userToFollow.getIsPrivate() == true) {
			System.out.println("User is private");
			userToFollow.getFollowRequests().add(user);
			userRepository.save(userToFollow);
			userRepository.save(user);
			return false;
		}
		System.out.println("User is public");
		user.getFollowingUsers().add(userToFollow);
		userToFollow.getFollowers().add(user);
		userRepository.save(userToFollow);
		userRepository.save(user);
		FollowEvent followEvent = new FollowEvent(null, userToFollow.getUsername(), user.getUsername());
		try {
			followEventProducer.sendFollowEvent(followEvent);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public void mute(Integer id) {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		User userToMute = this.findById(id);
		user.getMutedProfiles().add(userToMute);
		userRepository.save(user);


	}

	public List<User> getFollowRequests(){
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		return user.getFollowRequests();
	}

	public void acceptFollowRequest(Integer id){
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		User userToFollow = userRepository.findById(id).get();
		userToFollow.getFollowers().add(user);
		for (int i = 0; i < userToFollow.getFollowRequests().size(); i++) {
			if (userToFollow.getFollowRequests().get(i).getId().equals(user.getId())) {
				userToFollow.getFollowRequests().remove(i);

			}
		}
		user.getFollowingUsers().add(userToFollow);
		userRepository.save(userToFollow);
		userRepository.save(user);
		FollowEvent followEvent = new FollowEvent(null, userToFollow.getUsername(), user.getUsername());
		try {
			followEventProducer.sendFollowEvent(followEvent);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void block(Integer id) {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		User userToBlock = this.findById(id);
		user.getBlockedProfiles().add(userToBlock);
		userToBlock.getBlockedByProfiles().add(user);
		userRepository.save(userToBlock);
		userRepository.save(user);

	}

	public void report(Integer id) {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		User userToReport = this.findById(id);
		user.getReportedProfiles().add(userToReport);
		userToReport.getReportedByProfiles().add(user);
		userRepository.save(userToReport);
		userRepository.save(user);



	}

	public User search(String username) {
		System.out.println("Search user");
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		User searchedUser = userRepository.findByUsername(username);
		System.out.println(searchedUser);
		if (searchedUser != null) {
			System.out.println("User found");
			if (searchedUser.getIsPrivate() == false) {
				System.out.println("User is not private");
				if (searchedUser.getBlockedByProfiles().contains(user) == true) {
					System.out.println("1");
					searchedUser.setBlocked(true);
				}
				if (user.getBlockedProfiles().contains(searchedUser) == true) {
					System.out.println("2");
					searchedUser.setIAmBlocked(true);
				}
				if (searchedUser.getFollowers().contains(user)) {
					searchedUser.setFollowing(true);
				}else {
					searchedUser.setFollowing(false);
				}

				if (searchedUser.getReportedByProfiles().contains(user)) {
					searchedUser.setReported(true);
				}else {
					searchedUser.setReported(false);
				}
				return searchedUser;
			} else {
				System.out.println("User is private");
				if (searchedUser.getFollowers().contains(user)) {
					System.out.println("user is followed");
					if (searchedUser.getBlockedByProfiles().contains(user) == true) {
						System.out.println("1");
						searchedUser.setBlocked(true); //ja sam ga blokirala
					}
					if (user.getBlockedProfiles().contains(searchedUser) == true) {
						System.out.println("2");
						searchedUser.setIAmBlocked(true); //mene su blokirali
					}
					searchedUser.setFollowing(true);
					if (searchedUser.getReportedByProfiles().contains(user)) {
						searchedUser.setReported(true);
					}else {
						searchedUser.setReported(false);
					}
					return searchedUser;
				} else {
					System.out.println("user not followed");
					searchedUser.setFollowing(false);
					if (searchedUser.getReportedByProfiles().contains(user)) {
						searchedUser.setReported(true);
					}else {
						searchedUser.setReported(false);
					}
					return searchedUser;
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





