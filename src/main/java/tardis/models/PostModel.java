package tardis.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;


@SuppressWarnings("unused")
public class PostModel {

	@JsonProperty(required = true)
	private String author;
	@JsonProperty(required = true)
	private String message;
	@JsonProperty(value = "parent", required = true)
	private Integer parentID;

	@JsonProperty(defaultValue = "false")
	private Boolean isEdited;
	@JsonProperty(value = "forum")
	private String forumSlug;
	@JsonProperty
	private String threadSlug;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	private Timestamp created;
	@JsonProperty(value = "id")
	private Integer postID;
	@JsonProperty(value = "thread")
	private Integer threadID;
	private Integer authorID;
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

	public Integer getThreadID() {
		return threadID;
	}

	public void setThreadID(Integer threadID) {
		this.threadID = threadID;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public Integer getParentID() {
		return parentID;
	}

	public void setParentID(Integer parentID) {
		this.parentID = parentID;
	}

	public String getThreadSlug() {
		return threadSlug;
	}

	public void setThreadSlug(String threadSlug) {
		this.threadSlug = threadSlug;
	}

	public Integer getPostID() {
		return postID;
	}

	public void setPostID(Integer postID) {
		this.postID = postID;
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

	public Integer getAuthorID() {
		return authorID;
	}

	public void setAuthorID(Integer authorID) {
		this.authorID = authorID;
	}

//	public static final class PostMapper implements RowMapper<PostModel> {
//		@Override
//		public PostModel mapRow(ResultSet rs, int rowNum) throws SQLException {
//			final PostModel postModel = new PostModel();
//			postModel.author = rs.getString("author");
//			postModel.created = rs.getTimestamp("created");
//			postModel.forumSlug = rs.getString("forum");
//			postModel.postID = rs.getInteger("id");
//			postModel.message = rs.getString("message");
//			postModel.isEdited = rs.getBoolean("isedited");
//			postModel.parentID = rs.getInteger("parent");
//			postModel.threadID = rs.getInteger("thread");
//			if (postModel.parentID == null) {
//				postModel.parentID = 0L;
//			}
//			return postModel;
//		}
//	}
}
