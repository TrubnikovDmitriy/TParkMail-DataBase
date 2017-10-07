package application.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@SuppressWarnings("unused")
public class ThreadModel {

	@JsonProperty(required = true)
	private String author;
	@JsonProperty(required = true)
	private String message;
	@JsonProperty(required = true)
	private String title;

	private Date created;
	private String forumSlug;
	private Integer id;
	private String threadSlug;
	private Integer votes;


	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

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

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getForumSlug() {
		return forumSlug;
	}

	public void setForumSlug(String forumSlug) {
		this.forumSlug = forumSlug;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getThreadSlug() {
		return threadSlug;
	}

	public void setThreadSlug(String threadSlug) {
		this.threadSlug = threadSlug;
	}

	public Integer getVotes() {
		return votes;
	}

	public void setVotes(Integer votes) {
		this.votes = votes;
	}
}
