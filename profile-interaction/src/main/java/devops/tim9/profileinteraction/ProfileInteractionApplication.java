package devops.tim9.profileinteraction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableFeignClients
@SpringBootApplication
public class ProfileInteractionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProfileInteractionApplication.class, args);
	}

	

}
