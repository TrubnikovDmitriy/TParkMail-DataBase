package tardis.dao;

import tardis.models.ForumModel;
//import application.models.ThreadModel;
import tardis.models.ThreadModel;
import tardis.models.UserModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Repository
public class ForumDAO {

	private final JdbcTemplate jdbcTemplate;

	public ForumDAO(JdbcTemplate jdbcTempl) {
		this.jdbcTemplate = jdbcTempl;
	}

	public ForumModel createNewForum(ForumModel forum) {

		jdbcTemplate.queryForObject(
				"SELECT user_id, nickname FROM users WHERE nickname=?::citext",
				new Object[] { forum.getAuthor() },
				(rs, i) -> {
					forum.setAuthorID(rs.getInt(1));
					forum.setAuthor(rs.getString(2));
					return forum;
				}
		);

		jdbcTemplate.update(
				"INSERT INTO forums(slug, title, author_id) VALUES (?, ?, ?)",
				forum.getSlug(), forum.getTitle(), forum.getAuthorID()
		);

		forum.setPosts(0);
		forum.setThreads(0);
		return forum;
	}

	public ForumModel getForumBySlug(String slug) {

		return jdbcTemplate.queryForObject(
				"SELECT f.slug AS slug, f.title AS title, u.nickname AS author, " +
						"post_count AS posts, thread_count AS threads " +
						"FROM forums f " +
						"JOIN users u ON f.author_id=u.user_id " +
						"WHERE f.slug=?::citext",
				new Object[] { slug },
				(rs, i) -> new ForumModel(
						rs.getString("author"),
						rs.getString("slug"),
						rs.getString("title"),
						rs.getInt("threads"),
						rs.getInt("posts")
				)
		);
	}

	public List<ThreadModel> getThreads(String forumSlug, Integer limit,
	                                    Date since, Boolean desc) {

		forumSlug = jdbcTemplate.queryForObject(
				"SELECT slug FROM forums WHERE slug=?::citext",
				String.class, forumSlug
		); // Проверка, что форум вообще существует

		return (since == null) ?
				jdbcTemplate.query(
						"SELECT u.nickname, t.created, f.slug, " +
								"thread_id, t.mess, t.slug, t.title, t.votes " +
								"FROM threads t " +
								"JOIN forums f ON t.forum_id=f.forum_id " +
								"JOIN users u ON t.author_id=u.user_id " +
								"WHERE f.slug=?::citext " +
								"ORDER BY t.created" +
								(desc ? " DESC " : " ASC ") + "LIMIT ?;",
						new Object[] {forumSlug, limit},
						new ThreadModel.ThreadMapper()
				) :
				jdbcTemplate.query(
						"SELECT u.nickname, t.created, f.slug, " +
								"thread_id, t.mess, t.slug, t.title, t.votes " +
								"FROM threads t " +
								"JOIN forums f ON t.forum_id=f.forum_id " +
								"JOIN users u ON t.author_id=u.user_id " +
								"WHERE f.slug=?::citext AND " +
								"t.created " + (desc ? "<=" : ">=") + "? " +
								"ORDER BY t.created" +
								(desc ? " DESC " : " ASC ") + "LIMIT ?;",
						new Object[] {forumSlug, since, limit},
						new ThreadModel.ThreadMapper()
				);
	}
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
//				userModel.getID(), forumModel.getTitle(), forumModel.getSlug()
//		);
//		forumModel.setAuthor(userModel.getAuthor());
//		forumModel.setPosts(0);
//		forumModel.setThreads(0);
//		return forumModel;
//	}
//
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
}
