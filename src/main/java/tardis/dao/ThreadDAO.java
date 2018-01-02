package tardis.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tardis.models.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class ThreadDAO {

	private final JdbcTemplate jdbcTemplate;
	private final UserDAO userDAO;
	private final Logger logger = LoggerFactory.getLogger(ThreadDAO.class);



	public ThreadDAO(JdbcTemplate jdbcTempl, UserDAO userDAO) {
		this.jdbcTemplate = jdbcTempl;
		this.userDAO = userDAO;
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
					"INSERT INTO threads(forum_id, author_id, title, mess, slug, created) " +
							"VALUES(?, ?, ?, ?, ?, ?) RETURNING thread_id",
					new Object[]{
							th.getForumID(), th.getAuthorID(), th.getTitle(),
							th.getMessage(), th.getThreadSlug(), th.getCreated()
					},
					Integer.class
			));
		} else {
			th.setThreadID(jdbcTemplate.queryForObject(
					"INSERT INTO threads(forum_id, author_id, title, mess, slug) " +
							"VALUES(?, ?, ?, ?, ?) RETURNING thread_id",
					new Object[]{
							th.getForumID(), th.getAuthorID(), th.getTitle(),
							th.getMessage(), th.getThreadSlug()
					},
					Integer.class
			));
		}
		return th;
	}

	public ThreadModel getThreadBySlug(String slug) {
		return jdbcTemplate.queryForObject(
				"SELECT u.nickname, t.created, f.slug, " +
						"thread_id, t.mess, t.slug, t.title, t.votes " +
						"FROM threads t " +
						"JOIN forums f ON t.forum_id=f.forum_id " +
						"JOIN users u ON t.author_id=u.user_id " +
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
				"SELECT u.nickname, t.created, t.thread_id, " +
						"t.mess, t.slug, t.title, t.votes " +
						"FROM threads t " +
						"JOIN users u ON t.author_id=u.user_id " +
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
			"SELECT u.nickname, t.created, f.slug, thread_id," +
					" t.mess, t.slug, t.title, votes FROM threads t " +
					"JOIN forums f ON t.forum_id=f.forum_id " +
					"JOIN users u ON t.author_id=u.user_id " +
					"WHERE t.slug=?::citext";
	private final String queryThreadByID =
			"SELECT u.nickname, t.created, f.slug, thread_id," +
					" t.mess, t.slug, t.title, votes " +
					"FROM threads t " +
						"JOIN forums f ON t.forum_id=f.forum_id " +
						"JOIN users u ON t.author_id=u.user_id " +
					"WHERE t.thread_id=?";
}
