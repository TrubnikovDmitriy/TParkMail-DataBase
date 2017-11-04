package application.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@SuppressWarnings("unused")
public class PostModel {

	@JsonProperty(required = true)
	private String author;
	@JsonProperty(required = true)
	private String message;
	@JsonProperty(value = "parent", required = true)
	private Long parentId;

	@JsonProperty(defaultValue = "false")
	private Boolean isEdited;
	@JsonProperty(value = "forum")
	private String forumSlug;
	@JsonProperty
	private String threadSlug;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	private Timestamp created;
	@JsonProperty(value = "id")
	private Long postId;
	@JsonProperty(value = "thread")
	private Long threadId;
	private Long authorId;
	@JsonIgnore
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

	public Long getThreadId() {
		return threadId;
	}

	public void setThreadId(Long threadId) {
		this.threadId = threadId;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getThreadSlug() {
		return threadSlug;
	}

	public void setThreadSlug(String threadSlug) {
		this.threadSlug = threadSlug;
	}

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
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

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	public static final class PostMapper implements RowMapper<PostModel> {
		@Override
		public PostModel mapRow(ResultSet rs, int rowNum) throws SQLException {
			final PostModel postModel = new PostModel();
			postModel.author = rs.getString("author");
			postModel.created = rs.getTimestamp("created");
			postModel.forumSlug = rs.getString("forum");
			postModel.postId = rs.getLong("id");
			postModel.message = rs.getString("message");
			postModel.isEdited = rs.getBoolean("isedited");
			postModel.parentId = rs.getLong("parent");
			postModel.threadId = rs.getLong("thread");
			if (postModel.parentId == null) {
				postModel.parentId = 0L;
			}
			return postModel;
		}
	}
}
