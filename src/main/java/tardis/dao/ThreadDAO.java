package tardis.dao;

import tardis.models.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class ThreadDAO {

	private final JdbcTemplate jdbcTemplate;
	private final UserDAO userDAO;


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

		final Integer userId = this.userDAO.getUserIdByNickname(voteModel.getNickname());
		jdbcTemplate.update(
				"INSERT INTO votes(user_id, thread_id, voice) VALUES (?,?,?) " +
						"ON CONFLICT (user_id, thread_id) DO UPDATE SET voice=?",
				userId, threadID,
				voteModel.getVoice(),
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

//	public boolean checkParents(List<PostModel> posts, String threadIdOrSlug) {
//		List<Long> parentsID;
//
//		try {
//			final Long threadTempID = Long.parseLong(threadIdOrSlug);
//			parentsID = jdbcTemplate.query(
//					"SELECT post_id FROM posts WHERE thread_id=?",
//					new Object[] {threadTempID},
//					(resultSet, i) -> resultSet.getLong("post_id")
//			);
//
//		} catch (NumberFormatException e) {
//			parentsID = jdbcTemplate.query(
//					"SELECT post_id " +
//							"FROM posts p JOIN threads_extra th " +
//							"ON p.thread_id = th.thread_id " +
//							"WHERE LOWER(th.slug)=LOWER(?::citext)",
//					new Object[] {threadIdOrSlug},
//					(resultSet, i) -> resultSet.getLong("post_id")
//			);
//		}
//		for (PostModel post : posts) {
//			if (post.getParentID() == null) {
//				continue;
//			}
//			if (!parentsID.contains(post.getParentID())) {
//				return false;
//			}
//		}
//		return true;
//	}
//
//	public List<PostModel> getPosts(String threadIdOrSlug, Integer limit,
//									String sort, Boolean desc, Long since) {
//
//		final ThreadModel threadModel = this.getThreadByIdOrSlug(threadIdOrSlug);
//		final StringBuilder query;
//		switch (sort) {
//			case "flat":
//				query = new StringBuilder(
//						"SELECT author, created, slug AS forum, post_id AS id, path," +
//								"isedited, message, parent_id AS parent, th.thread_id AS thread " +
//								"FROM posts p NATURAL JOIN posts_extra px " +
//								"JOIN threads th ON th.thread_id=? AND p.thread_id=th.thread_id " +
//								"NATURAL JOIN forums f "
//				);
//				if (since != null) {
//					query.append("WHERE post_id ");
//					query.append(desc ? "<" : ">");
//					query.append(" ? ");
//				}
//				if (desc) {
//					query.append("ORDER BY created DESC, post_id DESC LIMIT ?");
//				} else {
//					query.append("ORDER BY created, post_id LIMIT ?");
//				}
//
//
//				if (since != null) {
//					return jdbcTemplate.query(
//							query.toString(),
//							new Object[]{threadModel.getThreadID(), since, limit},
//							new PostModel.PostMapper()
//					);
//				} else {
//					return jdbcTemplate.query(
//							query.toString(),
//							new Object[]{threadModel.getThreadID(), limit},
//							new PostModel.PostMapper()
//					);
//				}
//			case "tree":
//				query = new StringBuilder(
//						"SELECT author, created, slug AS forum, post_id AS id, path," +
//								"isedited, message, parent_id AS parent, th.thread_id AS thread " +
//								"FROM posts p NATURAL JOIN posts_extra px " +
//								"JOIN threads th ON th.thread_id=? AND p.thread_id=th.thread_id " +
//								"NATURAL JOIN forums f "
//				);
//				if (since != null) {
//					query.append("WHERE path");
//					query.append(desc ? " < " : " > ");
//					query.append("(SELECT path AS since_path FROM posts WHERE post_id=?)");
//				}
//				if (desc) {
//					query.append("ORDER BY path DESC LIMIT ?");
//				} else {
//					query.append("ORDER BY path LIMIT ?");
//				}
//
//
//				if (since != null) {
//					return jdbcTemplate.query(
//							query.toString(),
//							new Object[]{threadModel.getThreadID(), since, limit},
//							new PostModel.PostMapper()
//					);
//				} else {
//					return jdbcTemplate.query(
//							query.toString(),
//							new Object[]{threadModel.getThreadID(), limit},
//							new PostModel.PostMapper()
//					);
//				}
//			case "parent_tree":
//				if (since == null) {
//					query = new StringBuilder(
//							"SELECT author, created, slug AS forum, post_id AS id, path, " +
//									"isedited, message, parent_id AS parent, th.thread_id AS thread " +
//									"FROM posts p NATURAL JOIN posts_extra px " +
//									"JOIN threads th ON p.thread_id=th.thread_id " +
//									"NATURAL JOIN forums f " +
//									"JOIN (" +
//									"SELECT path AS root_path " +
//									"FROM posts WHERE array_length(path, 1) = 1 " +
//									"AND thread_id=? "
//					);
//					if (desc) {
//						query.append("ORDER BY path DESC LIMIT ?) AS root_posts ON path && root_path ORDER BY path DESC");
//					} else {
//						query.append("ORDER BY path ASC LIMIT ?) AS root_posts ON path && root_path ORDER BY path ASC");
//					}
//					return jdbcTemplate.query(
//							query.toString(),
//							new Object[]{threadModel.getThreadID(), limit},
//							new PostModel.PostMapper()
//					);
//				} else {
//					if (desc) {
//						query = new StringBuilder(
//								"SELECT author, created, slug AS forum, post_id AS id, path, " +
//										"isedited, message, parent_id AS parent, th.thread_id AS thread " +
//										"FROM posts p NATURAL JOIN posts_extra px " +
//										"JOIN threads th ON p.thread_id=th.thread_id " +
//										"NATURAL JOIN forums f " +
//										"JOIN ( " +
//										"SELECT path AS root_path FROM posts " +
//										"WHERE thread_id=? AND array_length(path, 1) = 1 " +
//										"AND path[1] <= ( " +
//										"SELECT path[1] AS since_path FROM posts " +
//										"WHERE post_id=?" +
//										") ORDER BY path DESC LIMIT ? " +
//										") AS root_posts ON path && root_path " +
//										"WHERE path < (" +
//										"SELECT path FROM posts WHERE post_id=? " +
//										") ORDER BY path DESC");
//					} else {
//						query = new StringBuilder(
//								"SELECT author, created, slug AS forum, post_id AS id, path, " +
//										"isedited, message, parent_id AS parent, th.thread_id AS thread " +
//										"FROM posts p NATURAL JOIN posts_extra px " +
//										"JOIN threads th ON p.thread_id=th.thread_id " +
//										"NATURAL JOIN forums f " +
//										"JOIN ( " +
//										"SELECT path AS root_path FROM posts " +
//										"WHERE thread_id=? AND array_length(path, 1) = 1 " +
//										"AND path[1] >= ( " +
//										"SELECT path[1] AS since_path FROM posts " +
//										"WHERE post_id=?" +
//										") ORDER BY path ASC LIMIT ? " +
//										") AS root_posts ON path && root_path " +
//										"WHERE path > (" +
//										"SELECT path FROM posts WHERE post_id=? " +
//										") ORDER BY path");
//					}
//					return jdbcTemplate.query(
//							query.toString(),
//							new Object[]{threadModel.getThreadID(), since, limit, since},
//							new PostModel.PostMapper()
//					);
//				}
//
//			default:
//				return null;
//
//		}
//	}

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
					" t.mess, t.slug, t.title, votes FROM threads t " +
					"JOIN forums f ON t.forum_id=f.forum_id " +
					"JOIN users u ON t.author_id=u.user_id " +
					"WHERE t.thread_id=?";
}
