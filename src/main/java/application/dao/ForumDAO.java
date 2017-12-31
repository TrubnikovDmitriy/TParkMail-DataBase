//package application.dao;
//
//import application.models.ForumModel;
//import application.models.ThreadModel;
//import application.models.UserModel;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//
//@Repository
//public class ForumDAO {
//
//	private final JdbcTemplate jdbcTemplate;
//
//	public ForumDAO(JdbcTemplate jdbcTempl) {
//		this.jdbcTemplate = jdbcTempl;
//	}
//
//	@Transactional
//	public ForumModel createNewForum(ForumModel forumModel) {
//		final UserModel userModel = jdbcTemplate.queryForObject(
//				"SELECT * FROM users NATURAL JOIN users_extra " +
//					"WHERE LOWER(nickname)=LOWER(?)",
//				new Object[] {forumModel.getAuthor()},
//				new UserModel.UserMapper()
//		);
//		jdbcTemplate.update(
//				"INSERT INTO forums(admin_id, title, slug) VALUES(?, ?, ?)",
//				userModel.getId(), forumModel.getTitle(), forumModel.getSlug()
//		);
//		forumModel.setAuthor(userModel.getAuthor());
//		forumModel.setPosts(0);
//		forumModel.setThreads(0);
//		return forumModel;
//	}
//
//	@Transactional
//	public ThreadModel createNewThread(String forumSlug, final ThreadModel threadModel) {
//
//		jdbcTemplate.queryForObject(
//				"SELECT forum_id, slug FROM " +
//						"forums WHERE LOWER(slug)=LOWER(?) ",
//				new Object[] {forumSlug},
//				(rs, rowNumber) -> {
//					threadModel.setForumID(rs.getLong(1));
//					threadModel.setForumSlug(rs.getString(2));
//					return threadModel;
//				}
//		);
//
//		final Integer authorID = jdbcTemplate.queryForObject(
//				"SELECT user_id FROM users WHERE LOWER(nickname)=LOWER(?)",
//				Integer.class, threadModel.getAuthor()
//		);
//
//		threadModel.setThreadID(jdbcTemplate.queryForObject(
//				"INSERT INTO threads(forum_id, author_id) VALUES(?, ?) RETURNING thread_id",
//				new Object[] {threadModel.getForumID(), authorID},
//				Long.class)
//		);
//
//		if (threadModel.getCreated() == null) {
//			jdbcTemplate.update(
//					"INSERT INTO threads_extra(thread_id, message, title, slug) " +
//							"VALUES(?, ?, ?, ?)",
//					threadModel.getThreadID(),
//					threadModel.getMessage(),
//					threadModel.getTitle(),
//					threadModel.getThreadSlug()
//			);
//		} else {
//			jdbcTemplate.update(
//					"INSERT INTO threads_extra(thread_id, created, message, title, slug) " +
//							"VALUES(?, ?, ?, ?, ?)",
//					threadModel.getThreadID(),
//					threadModel.getCreated(),
//					threadModel.getMessage(),
//					threadModel.getTitle(),
//					threadModel.getThreadSlug()
//			);
//		}
//		return threadModel;
//	}
//
//	public ForumModel getForumBySlug(String forumSlug) {
//		final ForumModel forumModel = jdbcTemplate.queryForObject(
//				"SELECT slug, title, admin_id, forum_id " +
//					"FROM forums WHERE LOWER(slug)=LOWER(?::citext)",
//				new Object[] {forumSlug},
//				new ForumModel.ForumMapper()
//		);
//		forumModel.setAuthor(
//				jdbcTemplate.queryForObject(
//						"SELECT nickname FROM users WHERE user_id=?",
//						String.class,
//						forumModel.getAuthorID()
//				)
//		);
//		forumModel.setThreads(
//				jdbcTemplate.queryForObject(
//					"SELECT COUNT(th.thread_id) FROM forums f " +
//						"NATURAL JOIN threads th WHERE LOWER(f.slug)=LOWER(?)",
//					Integer.class,
//					forumSlug
//				)
//		);
//		forumModel.setPosts(
//				jdbcTemplate.queryForObject(
//					"SELECT COUNT(p.post_id) FROM forums f JOIN threads th " +
//							"ON LOWER(f.slug)=LOWER(?::citext) AND f.forum_id=th.forum_id " +
//							"JOIN posts p ON th.thread_id=p.thread_id",
//					Integer.class,
//					forumSlug
//				)
//		);
//		return forumModel;
//	}
//
//	public List<ThreadModel> getThreads(String forumSlug, Integer limit,
//	                                    java.util.Date since, Boolean desc) {
//		jdbcTemplate.queryForObject(
//				"SELECT forum_id FROM forums WHERE LOWER(slug)=LOWER(?)",
//				Integer.class, forumSlug
//		); // Проверка, что форум вообще существует
//
//		return since == null ?
//				jdbcTemplate.query(
//						"SELECT u.nickname, th_x.created, f.slug AS f_slug, " +
//								"th.thread_id, th_x.message, th_x.slug AS th_slug, th_x.title, " +
//								"SUM(v.voice) AS votes " +
//								"FROM threads th JOIN forums f " +
//								"ON LOWER(f.slug)=LOWER(?) AND f.forum_id=th.forum_id " +
//								"JOIN threads_extra th_x ON " +
//								"th_x.thread_id=th.thread_id " +
//								"JOIN users u ON th.author_id=u.user_id " +
//								"LEFT JOIN votes v ON th.thread_id=v.thread_id " +
//								"GROUP BY th.thread_id, nickname, created, f.slug, " +
//								"th_x.message, th_x.slug, th_x.title " +
//								"ORDER BY th_x.created" +
//								(desc ? " DESC " : " ASC ") + "LIMIT ?;",
//						new Object[] {forumSlug, limit},
//						new ThreadModel.ThreadMapper()
//				) :
//				jdbcTemplate.query(
//						"SELECT u.nickname, th_x.created, f.slug AS f_slug, " +
//								"th.thread_id, th_x.message, th_x.slug AS th_slug, th_x.title, " +
//								"SUM(v.voice) AS votes " +
//								"FROM threads th JOIN forums f " +
//								"ON LOWER(f.slug)=LOWER(?) AND f.forum_id=th.forum_id " +
//								"JOIN threads_extra th_x ON " +
//								"th_x.created " + (desc ? "<=" : ">=") + " ? AND " +
//								"th_x.thread_id=th.thread_id " +
//								"JOIN users u ON th.author_id=u.user_id " +
//								"LEFT JOIN votes v ON th.thread_id=v.thread_id " +
//								"GROUP BY th.thread_id, nickname, created, f.slug, " +
//								"th_x.message, th_x.slug, th_x.title " +
//								"ORDER BY th_x.created" +
//								(desc ? " DESC " : " ASC ") + "LIMIT ?;",
//						new Object[] {forumSlug, since, limit},
//						new ThreadModel.ThreadMapper()
//				);
//
//	}
//
//	public List<UserModel> getUsers(String forumSlug,
//			Integer limit, String since, Boolean desc) {
//		jdbcTemplate.queryForObject(
//				"SELECT forum_id FROM forums WHERE LOWER(slug)=LOWER(?::citext)",
//				Integer.class, forumSlug
//		); // Проверка, что форум вообще существует
//		final StringBuilder query = new StringBuilder(
//				"SELECT DISTINCT ux.about, ux.email, ux.fullname, u.nickname, u.user_id " +
//				"FROM forums f JOIN threads th " +
//				"ON LOWER(f.slug)=LOWER(?::citext) AND f.forum_id=th.forum_id " +
//				"LEFT JOIN posts p " +
//				"ON th.thread_id=p.thread_id " +
//				"JOIN users u " +
//				"ON th.author_id=u.user_id OR p.author=u.nickname " +
//				"NATURAL JOIN users_extra ux ");
//		if (since != null) {
//			query.append(desc ?
//					"WHERE nickname < ?::citext " :
//					"WHERE nickname > ?::citext "
//			);
//		}
//		query.append(desc ?
//				"ORDER BY nickname DESC LIMIT ? " :
//				"ORDER BY nickname ASC LIMIT ? "
//		);
//		if (since == null) {
//			return jdbcTemplate.query(
//					query.toString(),
//					new Object[]{forumSlug, limit},
//					new UserModel.UserMapper()
//			);
//		} else {
//			return jdbcTemplate.query(
//					query.toString(),
//					new Object[]{forumSlug, since, limit},
//					new UserModel.UserMapper()
//			);
//		}
//	}
//}
