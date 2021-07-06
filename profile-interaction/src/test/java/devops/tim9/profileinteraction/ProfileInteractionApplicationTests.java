package devops.tim9.profileinteraction;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@EnableKafka
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
  partitions = 1, 
  controlledShutdown = false,
  brokerProperties = {
      "listeners=PLAINTEXT://localhost:9093", 
      "port=9093"
})
class ProfileInteractionApplicationTests {

	@Test
	void contextLoads() {
	}

}
