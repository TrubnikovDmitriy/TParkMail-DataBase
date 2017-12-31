package tardis.views;

import com.fasterxml.jackson.annotation.JsonProperty;


@SuppressWarnings("unused")
public class StatusView {

	@JsonProperty(value = "user")
	private Integer numberOfUsers;
	@JsonProperty(value = "forum")
	private Integer numberOfForums;
	@JsonProperty(value = "thread")
	private Integer numberOfThreads;
	@JsonProperty(value = "post")
	private Integer numberOfPosts;

	public StatusView() {}

	public StatusView(Integer numberOfUsers, Integer numberOfForums,
	                  Integer numberOfThreads, Integer numberOfPosts) {
		this.numberOfUsers = numberOfUsers;
		this.numberOfForums = numberOfForums;
		this.numberOfThreads = numberOfThreads;
		this.numberOfPosts = numberOfPosts;
	}

	public Integer getNumberOfUsers() {
		return numberOfUsers;
	}

	public Integer getNumberOfForums() {
		return numberOfForums;
	}

	public Integer getNumberOfThreads() {
		return numberOfThreads;
	}

	public Integer getNumberOfPosts() {
		return numberOfPosts;
	}
}
