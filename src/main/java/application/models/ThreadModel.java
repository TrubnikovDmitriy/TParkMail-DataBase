package application.models;

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
	private Long threadId;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	private Timestamp created;
	@JsonIgnore
	private Long forumId;
	private Integer votes;

	public ThreadModel() {};

	public ThreadModel(String threadSlug, Long threadId) {
		this.threadSlug = threadSlug;
		this.threadId = threadId;
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

	public Long getThreadId() {
		return threadId;
	}

	public void setThreadId(Long threadId) {
		this.threadId = threadId;
	}

	public Long getForumId() {
		return forumId;
	}

	public void setForumId(Long forumId) {
		this.forumId = forumId;
	}

	public static final class ThreadMapper implements RowMapper<ThreadModel> {
		@Override
		public ThreadModel mapRow(ResultSet rs, int rowNum) throws SQLException {
			final ThreadModel threadModel = new ThreadModel();
			threadModel.author = rs.getString("nickname");
			threadModel.created = rs.getTimestamp("created");
			threadModel.forumSlug = rs.getString("f_slug");
			threadModel.threadId = rs.getLong("thread_id");
			threadModel.message = rs.getString("message");
			threadModel.threadSlug = rs.getString("th_slug");
			threadModel.title = rs.getString("title");
			threadModel.votes = rs.getInt("votes");
			return threadModel;
		}
	}
}
