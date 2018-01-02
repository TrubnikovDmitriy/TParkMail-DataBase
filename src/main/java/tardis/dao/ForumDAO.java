package tardis.dao;

import tardis.models.ForumModel;
import tardis.models.ThreadModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
}
