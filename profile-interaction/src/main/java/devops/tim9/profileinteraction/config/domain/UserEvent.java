package devops.tim9.profileinteraction.config.domain;

import devops.tim9.profileinteraction.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserEvent {

	private Integer userEventId;
	private User user;
	private String action;
}


