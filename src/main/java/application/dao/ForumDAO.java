package application.dao;


import application.models.ForumModel;
import application.models.UserModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ForumDAO {

	private JdbcTemplate jdbcTemplate;

	public ForumDAO(JdbcTemplate jdbcTempl) {
		jdbcTemplate = jdbcTempl;
	}

	public Integer createNew(ForumModel forumModel) {
		Integer adminID = jdbcTemplate.queryForObject(
				"SELECT user_id FROM users WHERE nickname=?",
				Integer.class,
				forumModel.getNickname()
		);
		jdbcTemplate.update(
				"INSERT INTO forums(admin_id, title, slug) VALUES(?, ?, ?)",
				adminID, forumModel.getTitle(), forumModel.getSlug()
				);
		forumModel.setPosts(0);
		forumModel.setThreads(0);
		return 0;
	}

	@Transactional
	public ForumModel getForumBySlug(String forumSlug) {
		final ForumModel forumModel = jdbcTemplate.queryForObject(
				"SELECT slug, title, admin_id, forum_id FROM forums WHERE slug=?",
				new Object[] {forumSlug},
				new ForumModel.ForumMapper()
		);
		forumModel.setNickname(
				jdbcTemplate.queryForObject(
						"SELECT nickname FROM users WHERE user_id=?",
						String.class,
						forumModel.getAdminID()
				)
		);
		forumModel.setPosts(
				jdbcTemplate.queryForObject(
					"SELECT COUNT(th.thread_id) FROM forums f " +
						"NATURAL JOIN threads th WHERE f.slug = ?",
					Integer.class,
					forumSlug
				)
		);
		forumModel.setThreads(
				jdbcTemplate.queryForObject(
					"SELECT COUNT(p.post_id) FROM forums f JOIN threads th " +
							"ON f.slug = ? AND f.forum_id=th.forum_id " +
							"NATURAL JOIN posts p",
					Integer.class,
					forumSlug
				)
		);
		return forumModel;
//      Нужна ли лямбда?
//				new RowMapper<ForumModel>() {
//					@Override
//					public ForumModel mapRow(ResultSet rs, int rowNum) throws SQLException {
//						final ForumModel forumModel = new ForumModel();
//						forumModel.setSlug(rs.getString("slug"));
//						forumModel.setTitle(rs.getString("title"));
//						forumModel.setAdminID(rs.getInt("admin_id"));
//						return forumModel;
//					}
//				});
	}

//	void getThreads(String forumSlug) {}
//
//	UserModel[] getUsers(String forumSlug) {}
}
