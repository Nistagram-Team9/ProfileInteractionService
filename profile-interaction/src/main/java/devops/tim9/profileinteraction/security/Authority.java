package devops.tim9.profileinteraction.security;


import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import devops.tim9.profileinteraction.security.Role;


@Entity
@Table(name = "authority")
@NoArgsConstructor
@AllArgsConstructor
public class Authority implements GrantedAuthority {

	private static final long serialVersionUID = -3742453340543117722L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private Role role;

	public Authority(Role role) {
		this.role = role;
	}

	@Override
	public String getAuthority() {
		return role.name();
	}

	public void setRole(Role role) {
		this.role = role;
	}

	@JsonIgnore
	public Role getRole() {
		return role;
	}

	@JsonIgnore
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}

