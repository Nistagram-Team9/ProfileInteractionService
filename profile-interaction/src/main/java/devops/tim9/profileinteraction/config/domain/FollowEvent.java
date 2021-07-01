package devops.tim9.profileinteraction.config.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FollowEvent {
	private Integer followEventId;
	private String usernameFollowed;
	private String usernameFollowedBy;

}
