package application.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class ForumModel {

	@JsonProperty(value = "user", required = true)
	private String nickname;
	@JsonProperty(required = true)
	private String slug;
	@JsonProperty(required = true)
	private String title;
	@JsonIgnore
	private Integer adminID;
	@JsonIgnore
	private Integer forumID;
	private Integer threads;
	private Integer posts;
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

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getThreads() {
		return threads;
	}

	public int getPosts() {
		return posts;
	}

	public Integer getAdminID() {
		return adminID;
	}

	public void setAdminID(Integer adminID) {
		this.adminID = adminID;
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

	public static final class ForumMapper implements RowMapper<ForumModel> {
		@Override
		public ForumModel mapRow(ResultSet rs, int rowNum) throws SQLException {
			final ForumModel forumModel = new ForumModel();
			forumModel.slug = rs.getString("slug");
			forumModel.title = rs.getString("title");
			forumModel.adminID = rs.getInt("admin_id");
			forumModel.forumID = rs.getInt("forum_id");
			return forumModel;
		}
	}

	//	public boolean createNewForum() {
//		try {
//			JDBC.beginTransaction();
//			ResultSet resultSet = JDBC.executeReturnSet(
//					"SELECT user_id FROM users " +
//					"WHERE nickname = '" + nickname + "';"
//			);
//			resultSet.next();
//			this.adminID = resultSet.getInt("user_id");
//
//			resultSet = JDBC.executeReturnSet(
//					"INSERT INTO forums(admin_id) " +
//					"VALUES(" + adminID + ')' +
//					"RETURNING forum_id;"
//			);
//			resultSet.next();
//			this.forumID = resultSet.getInt("forum_id");
//
//			JDBC.executeReturnVoid(
//					"INSERT INTO forums_extra(forum_id, title, slug)" +
//						"VALUES(" +
//							forumID + ", '" +
//							this.title + "', '" +
//							this.slug + "');"
//			);
//			JDBC.commitTransaction();
//			this.threads = 0;
//			this.posts = 0;
//			return true;
//		}
//		catch (SQLException sqlException) {
//			System.out.println("ErrorSQL: " + sqlException.getMessage());
//			return false;
//		}
//	}

}