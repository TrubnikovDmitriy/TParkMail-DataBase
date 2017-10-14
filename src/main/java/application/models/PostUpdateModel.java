package application.models;

import com.fasterxml.jackson.annotation.JsonProperty;


public class PostUpdateModel {

	@JsonProperty
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
