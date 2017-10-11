package application.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.util.Date;

@SuppressWarnings("unused")
public class PostModel {

	@JsonProperty(required = true)
	private String author;
	@JsonProperty(required = true)
	private String message;
	@JsonProperty(required = true)
	private Integer parentId;

	@JsonProperty(defaultValue = "false")
	private Boolean isEdited;
	@JsonProperty
	private String forumSlug;
	@JsonProperty
	private String threadSlug;
	@JsonProperty
	private Timestamp created;
	@JsonProperty
	private Integer postId;
	private Integer thredId;
	private String thread;
	private String path;


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

	public Boolean getEdited() {
		return isEdited;
	}

	public void setEdited(Boolean edited) {
		isEdited = edited;
	}

	public String getForumSlug() {
		return forumSlug;
	}

	public void setForumSlug(String forumSlug) {
		this.forumSlug = forumSlug;
	}

	public Integer getThredId() {
		return thredId;
	}

	public void setThredId(Integer thredId) {
		this.thredId = thredId;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public String getThreadSlug() {
		return threadSlug;
	}

	public void setThreadSlug(String threadSlug) {
		this.threadSlug = threadSlug;
	}

	public Integer getPostId() {
		return postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	public String getThread() {
		return thread;
	}

	public void setThread(String thread) {
		this.thread = thread;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
