package application.views;

import application.models.ForumModel;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class ForumView {

	@JsonProperty
	private Integer posts;
	@JsonProperty
	private String slug;
	@JsonProperty
	private Integer threads;
	@JsonProperty
	private String title;
	@JsonProperty
	private String user;

	public ForumView(ForumModel forumModel) {
		posts = forumModel.getPosts();
		slug = forumModel.getSlug();
		threads = forumModel.getThreads();
		title = forumModel.getTitle();
		user = forumModel.getNickname();
	}

	public Integer getPosts() {
		return posts;
	}

	public String getSlug() {
		return slug;
	}

	public Integer getThreads() {
		return threads;
	}

	public String getTitle() {
		return title;
	}

	public String getUser() {
		return user;
	}
}
