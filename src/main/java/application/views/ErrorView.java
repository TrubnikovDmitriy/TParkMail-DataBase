package application.views;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ErrorView {

	@JsonProperty("message")
	private String message;

	public ErrorView(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}