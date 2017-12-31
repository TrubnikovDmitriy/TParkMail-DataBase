package tardis.models;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class VoteModel {

	@JsonProperty(required = true)
	private String nickname;

	@JsonProperty(required = true)
	private Integer voice;


	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public boolean getVoice() {
		return voice == 1;
	}

	public void setVoice(Integer voice) {
		this.voice = voice;
	}
}