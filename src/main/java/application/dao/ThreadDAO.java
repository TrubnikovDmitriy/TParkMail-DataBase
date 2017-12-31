//package application.dao;
//
//import application.models.*;
//import org.springframework.dao.EmptyResultDataAccessException;
//import org.springframework.jdbc.core.BatchPreparedStatementSetter;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;
//import tardis.dao.UserDAO;
//
//import java.sql.*;
//import java.util.Date;
//import java.util.List;
//
//
//@Repository
//public class ThreadDAO {
//
//	private final JdbcTemplate jdbcTemplate;
//	private final UserDAO userDAO;
//
//	private ThreadModel getThreadByIdOrSlug(String threadIdOrSlug) {
//		try {
//			final Long threadTempID = Long.parseLong(threadIdOrSlug);
//			return jdbcTemplate.queryForObject(
//					"SELECT thx.slug, thx.thread_id, f.slug " +
//							"FROM threads_extra thx " +
//							"NATURAL JOIN threads th " +
//							"JOIN forums f " +
//							"ON thx.thread_id=? " +
//							"AND f.forum_id=th.forum_id",
//					new Object[] {threadTempID},
//					(rs, rowNumber) -> {
//						final ThreadModel threadModel = new ThreadModel();
//						threadModel.setThreadSlug(rs.getString(1));
//						threadModel.setThreadID(rs.getLong(2));
//						threadModel.setForumSlug(rs.getString(3));
//						return threadModel;
//					}
//			);
//		} catch (NumberFormatException e) {
//			return jdbcTemplate.queryForObject(
//					"SELECT thx.slug, thx.thread_id, f.slug " +
//							"FROM threads_extra thx " +
//							"NATURAL JOIN threads th " +
//							"JOIN forums f " +
//							"ON LOWER(thx.slug)=LOWER(?) " +
//							"AND f.forum_id=th.forum_id",
//					new Object[] {threadIdOrSlug},
//					(rs, rowNumber) -> {
//						final ThreadModel threadModel = new ThreadModel();
//						threadModel.setThreadSlug(rs.getString(1));
//						threadModel.setThreadID(rs.getLong(2));
//						threadModel.setForumSlug(rs.getString(3));
//						return threadModel;
//					}
//			);
//		}
//	}
//
//	private void updateVotes(ThreadModel threadModel) {
//
//		threadModel.setVotes(jdbcTemplate.queryForObject(
//				"SELECT SUM(voice) FROM votes WHERE thread_id=?",
//				Integer.class, threadModel.getThreadID()
//		));
//	}
//
//	public ThreadDAO(JdbcTemplate jdbcTempl, UserDAO userDAO) {
//		this.jdbcTemplate = jdbcTempl;
//		this.userDAO = userDAO;
//	}
//
//	@Transactional
//	public List<PostModel> createNewPosts(String threadIdOrSlug, List<PostModel> posts) {
//
//		final ThreadModel thread = getThreadByIdOrSlug(threadIdOrSlug);
//
//		final List<Long> postsID = jdbcTemplate.query(
//				"SELECT nextval('threads_thread_id_seq') FROM generate_series(1, ?)",
//				new Object[] {posts.size()},
//				(rs, rowNum) -> rs.getLong("nextval")
//		);
//
//		final Long currentTime = new Date().getTime();
//
//
//		jdbcTemplate.batchUpdate(
//				"INSERT INTO posts(post_id, thread_id, author, parent_id) " +
//						"VALUES (?,?,?,?)",
//				new BatchPreparedStatementSetter() {
//					@Override
//					public void setValues(PreparedStatement ps, int rowNumber)
//							throws SQLException {
//
//						final PostModel post  = posts.get(rowNumber);
//
//						post.setPostID(postsID.get(rowNumber));
//						post.setThreadSlug(thread.getThreadSlug());
//						post.setThreadID(thread.getThreadID());
//						post.setForumSlug(thread.getForumSlug());
//						post.setCreated(new Timestamp(currentTime));
//
//						ps.setLong(1, post.getPostID());
//						ps.setLong(2, post.getThreadID());
//						ps.setString(3, post.getAuthor());
//						if (post.getParentID() != null) {
//							ps.setLong(4, post.getParentID());
//						} else {
//							ps.setNull(4, Types.INTEGER);
//						}
//
//						posts.set(rowNumber, post);
//					}
//
//					@Override
//					public int getBatchSize() {
//						return posts.size();
//					}
//				}
//		);
//
//		jdbcTemplate.batchUpdate(
//				"INSERT INTO posts_extra(post_id, created, message) " +
//						"VALUES (?, ?, ?)",
//				new BatchPreparedStatementSetter() {
//					@Override
//					public void setValues(PreparedStatement ps, int rowNumber)
//							throws SQLException {
//
//						ps.setLong(1, posts.get(rowNumber).getPostID());
//						ps.setTimestamp(2, posts.get(rowNumber).getCreated());
//						ps.setString(3, posts.get(rowNumber).getMessage());
//					}
//
//					@Override
//					public int getBatchSize() {
//						return posts.size();
//					}
//				}
//
//		);
//		return posts;
//	}
//
//	public ThreadModel voteForThread(String threadIdOrSlug, VoteModel voteModel) {
//
//		final ThreadModel threadModel = getFullThreadByIdOrSlug(threadIdOrSlug);
//		final Integer userId = this.userDAO.getUserIdByNickname(voteModel.getNickname());
//
//		jdbcTemplate.update(
//				"INSERT INTO votes(user_id, thread_id, voice) VALUES (?,?,?) " +
//						"ON CONFLICT (user_id, thread_id) DO UPDATE SET voice=?",
//				userId, threadModel.getThreadID(),
//				voteModel.getVoice(),
//				voteModel.getVoice()
//		);
//		this.updateVotes(threadModel);
//		return threadModel;
//	}
//
//	public ThreadModel getThreadBySlug(String threadSlug) {
//		return jdbcTemplate.queryForObject(
//				"SELECT u.nickname, th_x.created, f.slug AS f_slug, " +
//						"th.thread_id, th_x.message, th_x.slug AS th_slug, th_x.title, " +
//						"SUM(v.voice) AS votes " +
//						"FROM threads th JOIN forums f " +
//						"ON f.forum_id=th.forum_id " +
//						"JOIN threads_extra th_x ON " +
//						"th_x.thread_id=th.thread_id AND LOWER(th_x.slug)=LOWER(?)" +
//						"JOIN users u ON th.author_id=u.user_id " +
//						"LEFT JOIN votes v ON th.thread_id=v.thread_id " +
//						"GROUP BY th.thread_id, nickname, created, f.slug, " +
//						"th_x.message, th_x.slug, th_x.title",
//				new Object[] {threadSlug},
//				new ThreadModel.ThreadMapper()
//		);
//	}
//
//	public ThreadModel getFullThreadByIdOrSlug(String threadIdOrSlug) {
//		Long threadTempID;
//		try {
//			threadTempID = Long.parseLong(threadIdOrSlug);
//			return jdbcTemplate.queryForObject(
//					"SELECT u.nickname, th_x.created, f.slug AS f_slug, " +
//							"th.thread_id, th_x.message, th_x.slug AS th_slug, th_x.title, " +
//							"SUM(v.voice) AS votes " +
//							"FROM threads th JOIN forums f " +
//							"ON f.forum_id=th.forum_id " +
//							"JOIN threads_extra th_x ON " +
//							"th_x.thread_id=th.thread_id AND " +
//							"th_x.thread_id=? " +
//							"JOIN users u ON th.author_id=u.user_id " +
//							"LEFT JOIN votes v ON th.thread_id=v.thread_id " +
//							"GROUP BY th.thread_id, nickname, created, f.slug, " +
//							"th_x.message, th_x.slug, th_x.title",
//					new Object[] { threadTempID },
//					new ThreadModel.ThreadMapper()
//			);
//		} catch (NumberFormatException e) {
//			return jdbcTemplate.queryForObject(
//					"SELECT u.nickname, th_x.created, f.slug AS f_slug, " +
//							"th.thread_id, th_x.message, th_x.slug AS th_slug, th_x.title, " +
//							"SUM(v.voice) AS votes " +
//							"FROM threads th JOIN forums f " +
//							"ON f.forum_id=th.forum_id " +
//							"JOIN threads_extra th_x ON " +
//							"th_x.thread_id=th.thread_id AND " +
//							"LOWER(th_x.slug)=LOWER(?) " +
//							"JOIN users u ON th.author_id=u.user_id " +
//							"LEFT JOIN votes v ON th.thread_id=v.thread_id " +
//							"GROUP BY th.thread_id, nickname, created, f.slug, " +
//							"th_x.message, th_x.slug, th_x.title",
//					new Object[] { threadIdOrSlug },
//					new ThreadModel.ThreadMapper()
//			);
//		}
//	}
//
//	public ThreadModel getFullThreadById(Long threadId) {
//		return jdbcTemplate.queryForObject(
//				"SELECT u.nickname, th_x.created, f.slug AS f_slug, " +
//						"th.thread_id, th_x.message, th_x.slug AS th_slug, th_x.title, " +
//						"SUM(v.voice) AS votes " +
//						"FROM threads th JOIN forums f " +
//						"ON f.forum_id=th.forum_id " +
//						"JOIN threads_extra th_x ON " +
//						"th_x.thread_id=th.thread_id AND th_x.thread_id=? " +
//						"JOIN users u ON th.author_id=u.user_id " +
//						"LEFT JOIN votes v ON th.thread_id=v.thread_id " +
//						"GROUP BY th.thread_id, nickname, created, f.slug, " +
//						"th_x.message, th_x.slug, th_x.title",
//				new Object[] {threadId},
//				new ThreadModel.ThreadMapper()
//		);
//
//	}
//
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
//
//	public ThreadModel updateThread(String threadIdOrSlug,
//									ThreadUpdateModel threadUpdate) {
//
//		final ThreadModel threadModel = getFullThreadByIdOrSlug(threadIdOrSlug);
//		if (threadUpdate.getMessage() != null && threadUpdate.getTitle() != null) {
//			jdbcTemplate.update(
//					"UPDATE threads_extra SET message=?, title=? WHERE thread_id=?",
//					threadUpdate.getMessage(), threadUpdate.getTitle(),
//					threadModel.getThreadID()
//			);
//		}
//		if (threadUpdate.getMessage() != null && threadUpdate.getTitle() == null) {
//			jdbcTemplate.update(
//					"UPDATE threads_extra SET message=? WHERE thread_id=?",
//					threadUpdate.getMessage(),
//					threadModel.getThreadID()
//			);
//		}
//		if (threadUpdate.getMessage() == null  && threadUpdate.getTitle() != null) {
//			jdbcTemplate.update(
//					"UPDATE threads_extra SET title=? WHERE thread_id=?",
//					threadUpdate.getTitle(),
//					threadModel.getThreadID()
//			);
//		}
//		threadModel.updateThread(threadUpdate);
//		return threadModel;
//	}
//
//}
