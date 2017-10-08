package application.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@SuppressWarnings("unused")
public class PostModel {

	@JsonProperty(required = true)
	private String author;
	@JsonProperty(required = true)
	private String message;

	private Boolean isEdited = false;
	private PostModel parent = null;
	private String forumSlug;
	private Integer thredId;
	private Date created;
	private Integer id;


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

	public PostModel getParent() {
		return parent;
	}

	public void setParent(PostModel parent) {
		this.parent = parent;
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

	public void setCreated(Date created) {
		this.created = created;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
