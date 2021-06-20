package devops.tim9.profileinteraction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import devops.tim9.profileinteraction.model.User;


public interface UserRepository extends JpaRepository<User, Integer> {

	User findByUsername(String username);

	@Query(value = "select * from users inner join verfication_tokens using (id)", nativeQuery = true)
	User findByToken(String token);
	

}
