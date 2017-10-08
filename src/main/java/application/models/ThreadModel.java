package application.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.*;
import java.text.SimpleDateFormat;

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
	private Integer threadId;
	@JsonProperty
	private String created;
	@JsonIgnore
	private Integer forumId;
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

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
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

	public Integer getThreadId() {
		return threadId;
	}

	public void setThreadId(Integer threadId) {
		this.threadId = threadId;
	}

	public Integer getForumId() {
		return forumId;
	}

	public void setForumId(Integer forumId) {
		this.forumId = forumId;
	}

	public static final class ThreadMapper implements RowMapper<ThreadModel> {
		@Override
		public ThreadModel mapRow(ResultSet rs, int rowNum) throws SQLException {
			final ThreadModel threadModel = new ThreadModel();
			threadModel.author = rs.getString("nickname");
			threadModel.created = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
					.format(rs.getTimestamp("created"));
			threadModel.forumSlug = rs.getString("f_slug");
			threadModel.threadId = rs.getInt("thread_id");
			threadModel.message = rs.getString("message");
			threadModel.threadSlug = rs.getString("th_slug");
			threadModel.title = rs.getString("title");
			threadModel.votes = rs.getInt("votes");
			return threadModel;
		}
	}
}
