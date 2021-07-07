package devops.tim9.profileinteraction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import devops.tim9.profileinteraction.model.User;
import devops.tim9.profileinteraction.security.Authority;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {

	


}