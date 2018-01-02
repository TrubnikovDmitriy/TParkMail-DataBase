package tardis.models;

import com.fasterxml.jackson.annotation.JsonProperty;


@SuppressWarnings("unused")
public class ThreadUpdateModel {
	@JsonProperty
	private String message;
	@JsonProperty
	private String title;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}

