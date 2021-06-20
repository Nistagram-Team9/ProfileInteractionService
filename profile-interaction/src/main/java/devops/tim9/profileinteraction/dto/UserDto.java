package devops.tim9.profileinteraction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

	private String name;
	private String surname;
	private String email;
	private String phoneNumber;
	private String sex;
	private String birthDate;
	private String username;
	private String website;
	private String biography;
	private Boolean isPrivate;
	private Boolean canBeTagged;
	private Boolean isActive;
	private String password;
}
