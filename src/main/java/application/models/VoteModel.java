package application.models;

import com.fasterxml.jackson.annotation.JsonProperty;


@SuppressWarnings("unused")
public class VoteModel {

	@JsonProperty(required = true)
	private String nickname;

	@JsonProperty(required = true)
	private String voice;


	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getVoice() {
		return voice;
	}

	public void setVoice(String voice) {
		this.voice = voice;
	}
}
