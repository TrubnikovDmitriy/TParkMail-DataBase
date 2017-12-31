package tardis.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.*;


@SuppressWarnings("unused")
public class ThreadModel {

	@JsonProperty(required = true)
	private String author;
	@JsonProperty(required = true)
	private String message;
	@JsonProperty(required = true)
	private String title;
	@JsonProperty("forum")
	private String forumSlug;
	@JsonProperty("slug")
	private String threadSlug;
	@JsonProperty("id")
	private Integer threadID;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	private Timestamp created;
	@JsonIgnore
	private Integer forumID;
	@JsonIgnore
	private Integer authorID;
	private Integer votes;

	public ThreadModel() { }

	public ThreadModel(String threadSlug, Integer threadID) {
		this.threadSlug = threadSlug;
		this.threadID = threadID;
	}

	public ThreadModel(String author, Timestamp created,
	                   String forumSlug, Integer threadID,
	                   String message, String threadSlug,
	                   String title, Integer votes) {
		this.author = author;
		this.message = message;
		this.title = title;
		this.forumSlug = forumSlug;
		this.threadSlug = threadSlug;
		this.threadID = threadID;
		this.created = created;
		this.votes = votes;
	}

	public void updateThread(ThreadUpdateModel updateThread) {
		if (updateThread.getTitle() != null) {
			this.title = updateThread.getTitle();
		}
		if (updateThread.getMessage() != null) {
			this.message = updateThread.getMessage();
		}
	}

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

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public String getForumSlug() {
		return forumSlug;
	}

	public void setForumSlug(String forumSlug) {
		this.forumSlug = forumSlug;
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

	public Integer getThreadID() {
		return threadID;
	}

	public void setThreadID(Integer threadID) {
		this.threadID = threadID;
	}

	public Integer getForumID() {
		return forumID;
	}

	public void setForumID(Integer forumID) {
		this.forumID = forumID;
	}

	public Integer getAuthorID() {
		return authorID;
	}

	public void setAuthorID(Integer authorID) {
		this.authorID = authorID;
	}

	public static final class ThreadMapper implements RowMapper<ThreadModel> {
		@Override
		public ThreadModel mapRow(ResultSet rs, int rowNum) throws SQLException {
			final ThreadModel threadModel = new ThreadModel();
			threadModel.author = rs.getString(1);
			threadModel.created = rs.getTimestamp(2);
			threadModel.forumSlug = rs.getString(3);
			threadModel.threadID = rs.getInt(4);
			threadModel.message = rs.getString(5);
			threadModel.threadSlug = rs.getString(6);
			threadModel.title = rs.getString(7);
			threadModel.votes = rs.getInt(8);
			return threadModel;
		}
	}
}

