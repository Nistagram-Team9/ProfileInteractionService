package devops.tim9.profileinteraction.service;

import org.springframework.stereotype.Service;

import devops.tim9.profileinteraction.repository.VerificationTokenRepository;
import devops.tim9.profileinteraction.security.VerificationToken;

@Service
public class VerificationTokenService {

	private final VerificationTokenRepository verificationTokenRepository;

	public VerificationTokenService(VerificationTokenRepository verificationTokenRepository) {
		this.verificationTokenRepository = verificationTokenRepository;
	}

	public void saveToken(VerificationToken token) {
		verificationTokenRepository.save(token);
	}
	
	public VerificationToken findByToken(String token) {
		return verificationTokenRepository.findByToken(token);
	}

}
