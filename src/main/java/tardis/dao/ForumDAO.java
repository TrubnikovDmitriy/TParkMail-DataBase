package tardis.dao;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import tardis.models.ForumModel;
import tardis.models.ThreadModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
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

		final ThreadModel thread = new ThreadModel();
		jdbcTemplate.queryForObject(
				"SELECT forum_id, slug FROM forums WHERE slug=?::citext",
				new Object[] { forumSlug },
				(rs, rn) -> {
					thread.setForumID(rs.getInt(1));
					thread.setForumSlug(rs.getString(2));
					return thread;
				}
		); // Проверка, что форум вообще существует

		final RowMapper<ThreadModel> mapper = (rs, rn) ->  {
				final ThreadModel thr = new ThreadModel();
				thr.setAuthor(rs.getString(1));
				thr.setCreated(rs.getTimestamp(2));
				thr.setMessage(rs.getString(3));
				thr.setThreadSlug(rs.getString(4));
				thr.setTitle(rs.getString(5));
				thr.setVotes(rs.getInt(6));
				thr.setThreadID(rs.getInt(7));
				thr.setForumSlug(thread.getForumSlug());
				return thr;
		};

		return (since == null) ?
				jdbcTemplate.query(
						"SELECT t.author_nickname, t.created, " +
								"t.mess, t.slug, t.title, t.votes, t.thread_id " +
								"FROM threads t WHERE t.forum_id=? " +
								"ORDER BY t.created" +
								(desc ? " DESC " : " ASC ") + "LIMIT ?;",
						new Object[] { thread.getForumID(), limit},
						mapper
				) :
				jdbcTemplate.query(
						"SELECT t.author_nickname, t.created, " +
								"t.mess, t.slug, t.title, t.votes, t.thread_id " +
								"FROM threads t " +
								"WHERE t.forum_id=? AND " +
								"t.created " + (desc ? "<=" : ">=") + "? " +
								"ORDER BY t.created" +
								(desc ? " DESC " : " ASC ") + "LIMIT ?;",
						new Object[] { thread.getForumID(), since, limit},
						mapper
				);
	}
}
