package devops.tim9.profileinteraction.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import devops.tim9.profileinteraction.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class TokenHelper {

	@Value("${app.title}")
	private String APP_NAME;

	@Value("${app.secret}")
	public String SECRET;

	@Value("${app.expires}")
	private int EXPIRES_IN;

	@Value("${app.authHeader}")
	private String AUTH_HEADER;

	private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

	private final static int EXPIRING_CONST = 100;

	public String generateToken(String username) {
		return Jwts.builder().setIssuer(APP_NAME).setSubject(username).setIssuedAt(new Date())
				.setExpiration(generateExpirationDate()).signWith(SIGNATURE_ALGORITHM, SECRET).compact();
	}

	private Date generateExpirationDate() {
		return Date.from( LocalDateTime.now().plusMinutes(EXPIRES_IN).atZone( ZoneId.systemDefault()).toInstant());
	}

	// Functions for refreshing JWT token
	public String refreshToken(String token) {
		String refreshedToken;
		try {
			final Claims claims = this.getAllClaimsFromToken(token);
			if (claims == null) {
				throw new Exception("Claims is not allowed to be null.");
			} else {
				claims.setIssuedAt(java.sql.Date.valueOf(LocalDate.now()));
			}
			refreshedToken = Jwts.builder().setClaims(claims).setExpiration(generateExpirationDate())
				.signWith(SIGNATURE_ALGORITHM, SECRET).compact();
		} catch (Exception e) {
			refreshedToken = null;
		}
		return refreshedToken;
	}

	public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {
		final Date created = this.getIssuedAtDateFromToken(token);
		return !(this.isCreatedBeforeLastPasswordReset(created, lastPasswordReset))
			&& (!(this.isTokenExpired(token)));
	}

	// Functions for validating JWT token data
	public Boolean validateToken(String token, UserDetails userDetails) {
		User user = (User) userDetails;
		final String username = getUsernameFromToken(token);
		final Date created = getIssuedAtDateFromToken(token);

		return (username != null && username.equals(userDetails.getUsername()));
	}

	private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
		return (lastPasswordReset != null && created.getTime() + EXPIRING_CONST < lastPasswordReset.getTime());
	}

	private Boolean isTokenExpired(String token) {
		final LocalDate expiration = this.getExpirationDateFromToken(token);
		return expiration.isBefore(LocalDate.now());
	}

	// Functions for getting data from token
	private Claims getAllClaimsFromToken(String token) {
		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
		} catch (Exception e) {
			claims = null;
		}
		return claims;
	}

	public String getUsernameFromToken(String token) {
		String username;
		try {
			final Claims claims = this.getAllClaimsFromToken(token);
			if (claims == null) {
				throw new Exception("Claims is not allowed to be null.");
			} else {
				username = claims.getSubject();
			}
		} catch (Exception e) {
			username = null;
		}
		return username;
	}

	public Date getIssuedAtDateFromToken(String token) {
		Date issueAt;
		try {
			final Claims claims = this.getAllClaimsFromToken(token);
			if (claims == null) {
				throw new Exception("Claims is not allowed to be null.");
			} else {
				issueAt = claims.getIssuedAt();
			}
		} catch (Exception e) {
			issueAt = null;
		}
		return issueAt;
	}

	public String getAudienceFromToken(String token) {
		String audience;
		try {
			final Claims claims = this.getAllClaimsFromToken(token);
			if (claims == null) {
				throw new Exception("Claims is not allowed to be null.");
			} else {
				audience = claims.getAudience();
			}
		} catch (Exception e) {
			audience = null;
		}
		return audience;
	}

	public LocalDate getExpirationDateFromToken(String token) {
		LocalDate expiration;
		try {
			final Claims claims = this.getAllClaimsFromToken(token);
			if (claims == null) {
				throw new Exception("Claims is not allowed to be null.");
			} else {
				//expiration = new claims.getExpiration().toLocalDate();
				expiration = new java.sql.Date(claims.getExpiration().getTime()).toLocalDate();
			}
		} catch (Exception e) {
			expiration = null;
		}
		return expiration;
	}

	public int getExpiredIn() {
		return EXPIRES_IN;
	}

	// Functions for getting JWT token out of HTTP request
	public String getToken(HttpServletRequest request) {
		String authHeader = getAuthHeaderFromHeader(request);

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			return authHeader.substring(7);
		}

		return null;
	}

	public String getAuthHeaderFromHeader(HttpServletRequest request) {
		return request.getHeader(AUTH_HEADER);
	}

}
