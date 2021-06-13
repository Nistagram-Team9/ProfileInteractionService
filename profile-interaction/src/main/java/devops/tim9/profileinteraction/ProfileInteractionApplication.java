package devops.tim9.profileinteraction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ProfileInteractionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProfileInteractionApplication.class, args);
	}

}
