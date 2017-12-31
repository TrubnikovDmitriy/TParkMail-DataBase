package tardis.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


@SuppressWarnings("unused")
public class ForumModel {

	@JsonProperty(value = "user", required = true)
	private String author;
	@JsonProperty(required = true)
	private String slug;
	@JsonProperty(required = true)
	private String title;
	@JsonIgnore
	private Integer authorID;
	@JsonIgnore
	private Integer forumID;
	private Integer threads;
	private Integer posts;

	public ForumModel() { }

	public ForumModel(String author, String slug, String title) {
		this.author = author;
		this.slug = slug;
		this.title = title;
	}

	public ForumModel(ForumModel forum) {
		this(forum.author, forum.slug, forum.title);
	}

	public ForumModel(String author, String slug, String title,
	                  Integer threads, Integer posts) {
		this.author = author;
		this.slug = slug;
		this.title = title;
		this.threads = threads;
		this.posts = posts;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Integer getThreads() {
		return threads;
	}

	public Integer getPosts() {
		return posts;
	}

	public Integer getAuthorID() {
		return authorID;
	}

	public void setAuthorID(Integer authorID) {
		this.authorID = authorID;
	}

	public Integer getForumID() {
		return forumID;
	}

	public void setForumID(Integer forumID) {
		this.forumID = forumID;
	}

	public void setThreads(Integer threads) {
		this.threads = threads;
	}

	public void setPosts(Integer posts) {
		this.posts = posts;
	}

//	public static final class ForumMapper implements RowMapper<ForumModel> {
//		@Override
//		public ForumModel mapRow(ResultSet rs, int rowNum) throws SQLException {
//			final ForumModel forumModel = new ForumModel();
//			forumModel.slug = rs.getString("slug");
//			forumModel.title = rs.getString("title");
//			forumModel.authorID = rs.getInt("admin_id");
//			forumModel.forumID = rs.getInt("forum_id");
//			return forumModel;
//		}
//	}
}