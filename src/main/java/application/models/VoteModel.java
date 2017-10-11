package application.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

@SuppressWarnings("unused")
public class VoteModel {

	@JsonProperty(required = true)
	private String nickname;

	@JsonProperty(required = true)
	@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT, pattern = "[-1,+1]") // TODO wtf?
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
