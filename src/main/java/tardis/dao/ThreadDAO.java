package tardis.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import tardis.models.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;


@Repository
public class ThreadDAO {

	private final JdbcTemplate jdbcTemplate;
	private final Logger logger = LoggerFactory.getLogger(ThreadDAO.class);



	public ThreadDAO(JdbcTemplate jdbcTempl) {
		this.jdbcTemplate = jdbcTempl;
	}


	public ThreadModel createNewThread(ThreadModel th) {
		jdbcTemplate.queryForObject(
				"SELECT nickname, user_id FROM users WHERE nickname=?::citext",
				new Object[] { th.getAuthor() },
				(rs, i) -> {
					th.setAuthor(rs.getString(1));
					th.setAuthorID(rs.getInt(2));
					return th;
				}
		);
		jdbcTemplate.queryForObject(
				"SELECT slug, forum_id FROM forums WHERE slug=?::citext",
				new Object[] { th.getForumSlug() },
				(rs, i) -> {
					th.setForumSlug(rs.getString(1));
					th.setForumID(rs.getInt(2));
					return th;
				}
		);

		if (th.getCreated() != null) {
			th.setThreadID(jdbcTemplate.queryForObject(
					"INSERT INTO threads(forum_id, author_id, title," +
							" mess, slug, created, author_nickname) " +
							"VALUES(?, ?, ?, ?, ?, ?, ?) RETURNING thread_id",
					new Object[]{
							th.getForumID(), th.getAuthorID(), th.getTitle(),
							th.getMessage(), th.getThreadSlug(), th.getCreated(),
							th.getAuthor()
					},
					Integer.class
			));
		} else {
			th.setThreadID(jdbcTemplate.queryForObject(
					"INSERT INTO threads(forum_id, author_id, title, " +
							"mess, slug, author_nickname) " +
							"VALUES(?, ?, ?, ?, ?, ?) RETURNING thread_id",
					new Object[]{
							th.getForumID(), th.getAuthorID(), th.getTitle(),
							th.getMessage(), th.getThreadSlug(), th.getAuthor()
					},
					Integer.class
			));
		}
		// Добавлением создателей ветки в forum_users
		jdbcTemplate.update(
				"INSERT INTO forum_users(forum_id, user_id) " +
						"VALUES (?, ?) ON CONFLICT DO NOTHING",
				th.getForumID(), th.getAuthorID()
		);
		return th;
	}

	public ThreadModel getThreadBySlug(String slug) {
		return jdbcTemplate.queryForObject(
				"SELECT t.author_nickname, t.created, f.slug, " +
						"thread_id, t.mess, t.slug, t.title, t.votes " +
						"FROM threads t " +
						"JOIN forums f ON t.forum_id=f.forum_id " +
						"WHERE t.slug=?::citext",
				new Object[] { slug },
				(rs, i) -> new ThreadModel(
						rs.getString(1),
						rs.getTimestamp(2),
						rs.getString(3),
						rs.getInt(4),
						rs.getString(5),
						rs.getString(6),
						rs.getString(7),
						rs.getInt(8)
				)
		);
	}

	public ThreadModel getThreadByIDforDetails(Integer threadID, String forumSlug) {
		return jdbcTemplate.queryForObject(
				"SELECT t.author_nickname, t.created, t.thread_id, " +
						"t.mess, t.slug, t.title, t.votes " +
						"FROM threads t " +
						"WHERE t.thread_id=?",
				new Object[] { threadID },
				(rs, i) -> new ThreadModel(
						rs.getString(1),
						rs.getTimestamp(2),
						forumSlug,
						rs.getInt(3),
						rs.getString(4),
						rs.getString(5),
						rs.getString(6),
						rs.getInt(7)
				)
		);
	}

	public Integer getThreadIdBySlug(String slug) {
		return jdbcTemplate.queryForObject(
				"SELECT thread_id FROM threads WHERE slug=?::citext",
				Integer.class, slug
		);
	}

	public ThreadModel voteForThread(String threadIdOrSlug, VoteModel voteModel) {

		Integer threadID;
		try {
			threadID = Integer.parseInt(threadIdOrSlug);
		} catch (NumberFormatException e) {
			threadID = getThreadIdBySlug(threadIdOrSlug);
		}

		jdbcTemplate.update(
				"INSERT INTO votes(user_nickname, thread_id, voice) VALUES (?,?,?) " +
						"ON CONFLICT (user_nickname, thread_id) DO UPDATE SET voice=?",
				voteModel.getNickname(), threadID, voteModel.getVoice(),
				voteModel.getVoice()
		);

		return jdbcTemplate.queryForObject(
				queryThreadByID,
				new Object[] { threadID },
				new ThreadModel.ThreadMapper()
		);
	}

	public ThreadModel getThreadForCreatePost(String threadIdOrSlug) {

		final class ThreadMapperForPosts implements RowMapper<ThreadModel> {
			@Override
			public ThreadModel mapRow(ResultSet rs, int rowNum) throws SQLException {
				final ThreadModel thread = new ThreadModel();
				thread.setAuthor(rs.getString(1));
				thread.setForumSlug(rs.getString(2));
				thread.setForumID(rs.getInt(3));
				thread.setThreadID(rs.getInt(4));
				return thread;
			}
		}
		try {
			final Integer threadTempID = Integer.parseInt(threadIdOrSlug);
			return jdbcTemplate.queryForObject(
					"SELECT t.author_nickname AS nickname, f.slug, f.forum_id, thread_id " +
							"FROM threads t " +
								"JOIN forums f ON t.forum_id=f.forum_id " +
							"WHERE t.thread_id=?",
					new Object[] { threadTempID },
					new ThreadMapperForPosts()
			);
		} catch (NumberFormatException e) {
			return jdbcTemplate.queryForObject(
					"SELECT t.author_nickname AS nickname, f.slug, f.forum_id, thread_id " +
							"FROM threads t " +
								"JOIN forums f ON t.forum_id=f.forum_id " +
							"WHERE t.slug=?::citext",
					new Object[] { threadIdOrSlug },
					new ThreadMapperForPosts()
			);
		}
	}

	public ThreadModel getFullThreadByIdOrSlug(String threadIdOrSlug) {
		try {
			final Integer threadTempID = Integer.parseInt(threadIdOrSlug);
			return jdbcTemplate.queryForObject(
					queryThreadByID,
					new Object[] { threadTempID },
					new ThreadModel.ThreadMapper()
			);
		} catch (NumberFormatException e) {
			return jdbcTemplate.queryForObject(
					queryThreadBySlug,
					new Object[] { threadIdOrSlug },
					new ThreadModel.ThreadMapper()
			);
		}
	}

	public ThreadModel updateThread(String threadIdOrSlug,
									ThreadUpdateModel threadUpdate) {

		final ThreadModel threadModel = getFullThreadByIdOrSlug(threadIdOrSlug);
		threadModel.updateThread(threadUpdate);

		jdbcTemplate.update(
				"UPDATE threads SET mess=?, title=? WHERE thread_id=?",
				threadModel.getMessage(),
				threadModel.getTitle(),
				threadModel.getThreadID()
		);
		return threadModel;
	}


	private final String queryThreadBySlug =
			"SELECT t.author_nickname AS nickname, t.created, f.slug, thread_id," +
					" t.mess, t.slug, t.title, votes " +
					"FROM threads t " +
						"JOIN forums f ON t.forum_id=f.forum_id " +
					"WHERE t.slug=?::citext";
	private final String queryThreadByID =
			"SELECT t.author_nickname AS nickname, t.created, f.slug, thread_id," +
					" t.mess, t.slug, t.title, votes " +
					"FROM threads t " +
						"JOIN forums f ON t.forum_id=f.forum_id " +
					"WHERE t.thread_id=?";
}
