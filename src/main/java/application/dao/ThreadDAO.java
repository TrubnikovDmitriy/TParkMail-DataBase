package application.dao;

import application.models.PostModel;
import application.models.ThreadModel;
import application.models.UserModel;
import application.models.VoteModel;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;


@Repository
public class ThreadDAO {

	private final JdbcTemplate jdbcTemplate;
	private final UserDAO userDAO;

	private ThreadModel getThreadByIdOrSlug(String threadIdOrSlug) {
		Long threadTempID;
		try {
			threadTempID = Long.parseLong(threadIdOrSlug);
		} catch (NumberFormatException e) {
			threadTempID = -1L;
		}
		return jdbcTemplate.queryForObject(
				"SELECT thx.slug, thx.thread_id, f.slug " +
						"FROM threads_extra thx " +
						"NATURAL JOIN threads th " +
						"JOIN forums f " +
						"ON (LOWER(thx.slug)=LOWER(?) OR thx.thread_id=?) " +
						"AND f.forum_id=th.forum_id",
				new Object[] {threadIdOrSlug, threadTempID},
				(rs, rowNumber) -> {
					final ThreadModel threadModel = new ThreadModel();
					threadModel.setThreadSlug(rs.getString(1));
					threadModel.setThreadId(rs.getLong(2));
					threadModel.setForumSlug(rs.getString(3));
					return threadModel;
				}
		);

	}

	private void updateVotes(ThreadModel threadModel) {

		threadModel.setVotes(jdbcTemplate.queryForObject(
				"SELECT SUM(voice) FROM threads " +
					"NATURAL JOIN votes WHERE thread_id=?",
				Integer.class, threadModel.getThreadId()
		));
	}

	public ThreadDAO(JdbcTemplate jdbcTempl, UserDAO userDAO) {
			this.jdbcTemplate = jdbcTempl;
			this.userDAO = userDAO;
	}

	@Transactional
	public List<PostModel> createNewPosts(String threadIdOrSlug, List<PostModel> posts) {

		final ThreadModel thread = getThreadByIdOrSlug(threadIdOrSlug);

		final List<Long> postsID = jdbcTemplate.query(
				"SELECT nextval('threads_thread_id_seq') FROM generate_series(1, ?)",
				new Object[] {posts.size()},
				(rs, rowNum) -> rs.getLong("nextval")
		);

		final Long currentTime = new java.util.Date().getTime();

		jdbcTemplate.batchUpdate(
				"INSERT INTO posts(post_id, thread_id, author_id, path) VALUES (?,?,?,?)",
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int rowNumber)
							throws SQLException {

						final PostModel post  = posts.get(rowNumber);

						post.setPostId(postsID.get(rowNumber));
						post.setThreadSlug(thread.getThreadSlug());
						post.setThreadId(thread.getThreadId());
						post.setAuthorId(jdbcTemplate.queryForObject(
								"SELECT user_id FROM users WHERE nickname=?",
								Long.class, post.getAuthor())
						);
						post.setForumSlug(thread.getForumSlug());
						post.setCreated(new Timestamp(currentTime));
						post.setPath((post.getParentId() == null || post.getParentId() == 0) ?
								post.getPostId().toString() :
								jdbcTemplate.queryForObject(
										"SELECT path FROM posts " +
												"WHERE post_id=?",
										Long.class, post.getParentId()) + "."
										+ post.getPostId().toString()
						);

						ps.setLong(1, post.getPostId());
						ps.setLong(2, post.getThreadId());
						ps.setLong(3, post.getAuthorId());
						ps.setString(4, post.getPath());

						posts.set(rowNumber, post);
					}

					@Override
					public int getBatchSize() {
						return posts.size();
					}
				}
		);

		jdbcTemplate.batchUpdate(
				"INSERT INTO posts_extra(post_id, created, message) VALUES (?, ?, ?)",
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int rowNumber)
							throws SQLException {
						ps.setLong(1, posts.get(rowNumber).getPostId());
						ps.setTimestamp(2, posts.get(rowNumber).getCreated());
						ps.setString(3, posts.get(rowNumber).getMessage());
					}

					@Override
					public int getBatchSize() {
						return posts.size();
					}
				}

		);
		return posts;
	}

	@Transactional
	public ThreadModel voteForThread(String threadIdOrSlug, VoteModel voteModel) {
		final ThreadModel threadModel = getFullThreadByIdOrSlug(threadIdOrSlug);
		final UserModel userModel = this.userDAO.getUserByNickname(voteModel.getNickname());

		jdbcTemplate.update(
				"INSERT INTO votes(user_id, thread_id, voice) VALUES (?,?,?) " +
					"ON CONFLICT (user_id, thread_id) DO UPDATE SET voice=?",
				userModel.getId(), threadModel.getThreadId(),
				Integer.parseInt(voteModel.getVoice()),
				Integer.parseInt(voteModel.getVoice())
		);
		this.updateVotes(threadModel);
		return threadModel;
	}

	public ThreadModel getThreadBySlug(String threadSlug) {
		return jdbcTemplate.queryForObject(
				"SELECT u.nickname, th_x.created, f.slug AS f_slug, " +
				"th.thread_id, th_x.message, th_x.slug AS th_slug, th_x.title, " +
						"SUM(v.voice) AS votes " +
						"FROM threads th JOIN forums f " +
						"ON f.forum_id=th.forum_id " +
						"JOIN threads_extra th_x ON " +
						"th_x.thread_id=th.thread_id AND LOWER(th_x.slug)=LOWER(?)" +
						"JOIN users u ON th.author_id=u.user_id " +
						"LEFT JOIN votes v ON th.thread_id=v.thread_id " +
						"GROUP BY th.thread_id, nickname, created, f.slug, " +
						"th_x.message, th_x.slug, th_x.title",
				new Object[] {threadSlug},
				new ThreadModel.ThreadMapper()
		);
	}

	public ThreadModel getFullThreadByIdOrSlug(String threadIdOrSlug) {
		Long threadTempID;
		try {
			threadTempID = Long.parseLong(threadIdOrSlug);
		} catch (NumberFormatException e) {
			threadTempID = -1L;
		}
		return jdbcTemplate.queryForObject(
				"SELECT u.nickname, th_x.created, f.slug AS f_slug, " +
						"th.thread_id, th_x.message, th_x.slug AS th_slug, th_x.title, " +
						"SUM(v.voice) AS votes " +
						"FROM threads th JOIN forums f " +
						"ON f.forum_id=th.forum_id " +
						"JOIN threads_extra th_x ON " +
						"th_x.thread_id=th.thread_id AND " +
						"(LOWER(th_x.slug)=LOWER(?) OR th_x.thread_id=?) " +
						"JOIN users u ON th.author_id=u.user_id " +
						"LEFT JOIN votes v ON th.thread_id=v.thread_id " +
						"GROUP BY th.thread_id, nickname, created, f.slug, " +
						"th_x.message, th_x.slug, th_x.title",
				new Object[] {threadIdOrSlug, threadTempID},
				new ThreadModel.ThreadMapper()
		);

	}

}
