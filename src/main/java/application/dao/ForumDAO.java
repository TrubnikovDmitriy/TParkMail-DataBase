package application.dao;


import application.models.ForumModel;
import application.models.ThreadModel;
import application.models.UserModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;


@Repository
public class ForumDAO {

	private JdbcTemplate jdbcTemplate;

	public ForumDAO(JdbcTemplate jdbcTempl) {
		jdbcTemplate = jdbcTempl;
	}

	public ForumModel createNewForum(ForumModel forumModel) {
		final Integer adminID = jdbcTemplate.queryForObject(
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
		return forumModel;
	}

	@Transactional
	public ThreadModel createNewThread(String forumSlug, ThreadModel threadModel) {

		threadModel.setForumId(jdbcTemplate.queryForObject(
				"SELECT forum_id FROM forums WHERE slug=?",
				Integer.class, forumSlug));

		final Integer authorID = jdbcTemplate.queryForObject(
				"SELECT user_id FROM users WHERE nickname=?",
				Integer.class, threadModel.getAuthor());

		threadModel.setThreadId(jdbcTemplate.queryForObject(
				"INSERT INTO threads(forum_id, author_id) VALUES(?, ?) RETURNING thread_id",
				new Object[] {threadModel.getForumId(), authorID},
				Integer.class));

		jdbcTemplate.update(
				"INSERT INTO threads_extra(thread_id, created, message, slug, title) " +
					"VALUES(?, ?, ?, ?, ?)",
				threadModel.getThreadId(),
				Timestamp.valueOf(threadModel.getCreated()),
				threadModel.getMessage(),
				threadModel.getThreadSlug(),
				threadModel.getTitle()
		);
		return threadModel;
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

	public List<ThreadModel> getThreads(String forumSlug,
			Integer limit, Timestamp since, Boolean desc) {
		final String query =
				"SELECT u.nickname, th_x.created, f.slug AS f_slug, " +
				"th.thread_id, th_x.message, th_x.slug AS th_slug, th_x.title, " +
				"SUM(v.voice) AS votes " +
				"FROM threads th JOIN forums f " +
				"ON f.slug=? AND f.forum_id=th.forum_id " +
				"JOIN threads_extra th_x " +
				"ON th_x.created >= ? AND th_x.thread_id=th.thread_id " +
				"JOIN users u ON th.author_id=u.user_id " +
				"LEFT JOIN votes v ON th.thread_id=v.thread_id " +
				"GROUP BY th.thread_id, nickname, created, f.slug, " +
				"th_x.message, th_x.slug, th_x.title " +
				"ORDER BY th_x.created" +
				(desc ? " DESC " : " ASC ") + "LIMIT ?;";

		return jdbcTemplate.query(
				query,
				new Object[] {forumSlug, since, limit},
				new ThreadModel.ThreadMapper()
		);
	}

	public List<UserModel> getUsers(String forumSlug,
			Integer limit, String since, Boolean desc) {
		final String query =
				"SELECT DISTINCT ux.about, ux.email, ux.fullname, u.nickname, u.user_id " +
				"FROM forums f JOIN threads th " +
				"ON f.slug=? AND f.forum_id=th.forum_id " +
				"LEFT JOIN posts p " +
				"ON th.thread_id=p.thread_id " +
				"JOIN users u " +
				"ON th.author_id=u.user_id OR p.author_id=u.user_id " +
				"NATURAL JOIN users_extra ux " +
				"WHERE nickname > ? ORDER BY nickname " +
				(desc ? "DESC" : "ASC") +
				(limit != null ? " LIMIT ?;" : ";");
		return jdbcTemplate.query(
				query,
				new Object[] {forumSlug, since, limit},
				new UserModel.UserMapper()
		);
	}
}
