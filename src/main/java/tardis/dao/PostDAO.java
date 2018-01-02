package tardis.dao;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.transaction.annotation.Transactional;
import tardis.models.PostModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tardis.models.PostUpdateModel;
import tardis.models.ThreadModel;
import tardis.models.UserModel;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


@Repository
public class PostDAO {

	private final JdbcTemplate jdbcTemplate;
	private final ThreadDAO threadDAO;

	public PostDAO(JdbcTemplate jdbcTemplate, ThreadDAO threadDAO) {
		this.jdbcTemplate = jdbcTemplate;
		this.threadDAO = threadDAO;
	}

	public PostModel getPostByIDforDetails(Integer postID) {
		return jdbcTemplate.queryForObject(
				"SELECT p.*, u.nickname, f.slug, f.forum_id FROM posts p " +
						"JOIN users u ON p.author_id=u.user_id " +
						"JOIN threads th ON th.thread_id=p.thread_id " +
						"JOIN forums f ON f.forum_id=th.forum_id " +
					"WHERE p.post_id=?",
				new Object[] { postID },
				(rs, rn) -> {
					final PostModel post = new PostModel();
					post.setPostID(rs.getInt(1));
					post.setThreadID(rs.getInt(2));
					post.setAuthorID(rs.getInt(3));
					post.setParentID(rs.getInt(4));
					post.setMessage(rs.getString(6));
					post.setCreated(rs.getTimestamp(7));
					post.setEdited(rs.getBoolean(8));
					post.setAuthor(rs.getString(9));
					post.setForumSlug(rs.getString(10));
					return post;
				}
		);
	}

	public List<PostModel> createNewPosts(ThreadModel thread, List<PostModel> posts) {

		final List<Integer> postsID = jdbcTemplate.query(
				"SELECT nextval('threads_thread_id_seq') FROM generate_series(1, ?)",
				new Object[] { posts.size() },
				(rs, rowNum) -> rs.getInt("nextval")
		);
		final Long currentTime = new Date().getTime();

		jdbcTemplate.batchUpdate(
				"INSERT INTO posts(post_id, thread_id, author_id, parent_id, created, mess) " +
						"VALUES (?, ?, ?, ?, ?, ?)",
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int rowNumber)
							throws SQLException {

						final PostModel post  = posts.get(rowNumber);
						final UserModel user = jdbcTemplate.queryForObject(
								"SELECT user_id, nickname FROM users WHERE nickname=?::citext",
								new Object[]{ posts.get(rowNumber).getAuthor() },
								(rs, rn)-> new UserModel(
										rs.getInt("user_id"),
										rs.getString("nickname")
								)
						);

						// Для возвращаемого значения
						post.setAuthor(user.getNickname());
						post.setCreated(new Timestamp(currentTime));
						post.setForumSlug(thread.getForumSlug());
						post.setEdited(false);
						post.setPostID(postsID.get(rowNumber));
						// Если сообщение без родителей, то parentID не присылается
						post.setParentID(post.getParentID() == null ? 0 : post.getParentID());
						// message уже есть
						post.setThreadID(thread.getThreadID());


						// Для вставляемой строки в БД
						ps.setInt(1, post.getPostID());
						ps.setInt(2, post.getThreadID());
						ps.setInt(3, user.getID());
						ps.setLong(4, post.getParentID());
						ps.setTimestamp(5, post.getCreated());
						ps.setString(6, post.getMessage());

						posts.set(rowNumber, post);
					}
					@Override
					public int getBatchSize() {
						return posts.size();
					}
				}
		);
		return posts;
	}

	public PostModel updatePost(Integer postID, PostUpdateModel updatePost) {

		final PostModel post = jdbcTemplate.queryForObject(
				"SELECT u.nickname AS author, p.created AS created," +
						" f.slug AS forum, p.post_id AS id, " +
						"p.isedited AS isEdited, p.mess AS message, " +
						"p.parent_id AS parent, th.thread_id AS thread " +
						"FROM threads th " +
						"JOIN forums f ON th.forum_id = f.forum_id " +
						"JOIN posts p ON th.thread_id=p.thread_id " +
						"JOIN users u ON p.author_id = u.user_id " +
						"WHERE p.post_id=? ",
				new Object[] { postID },
				new PostModel.PostMapper()
		);
		if (updatePost.getMessage() == null ||
				updatePost.getMessage().equals(post.getMessage())) {
			return post;
		}
		jdbcTemplate.update(
				"UPDATE posts SET mess=? WHERE post_id=?",
				updatePost.getMessage(), postID
		);
		post.setMessage(updatePost.getMessage());
		post.setEdited(true);
		return post;
	}

	public Boolean checkParents(List<PostModel> posts, Integer threadID) {

		final List<Integer> postsID = jdbcTemplate.query(
				"SELECT post_id FROM posts WHERE thread_id=?",
				new Object[] { threadID },
				(rs, rn) -> rs.getInt(1)
		);

		for (PostModel post : posts) {
			if (post.getParentID() != null && !postsID.contains(post.getParentID())) {
				return false;
			}
		}
		return true;
	}

	public List<PostModel> getPosts(String threadIdOrSlug, Integer limit,
									String sort, Boolean desc, Long since) {
		Integer threadID;
		try {
			threadID = Integer.parseInt(threadIdOrSlug);
			jdbcTemplate.queryForObject(
					"SELECT thread_id FROM threads WHERE thread_id=" + threadIdOrSlug,
					Integer.class); // todo think about it
		} catch (NumberFormatException e) {
			threadID = threadDAO.getThreadIdBySlug(threadIdOrSlug);
		}
		switch (sort) {
			case "flat":
				return flatSort(threadID, desc, limit, since);
			case "tree":
				return treeSort(threadID, desc, limit, since);
			case "parent_tree":
				return parentTreeSort(threadID, desc, limit, since);
			default:
				throw new RuntimeException("Unexpected sorting");
		}
	}

	private List<PostModel> flatSort(Integer threadID, Boolean desc,
	                                 Integer limit, Long since) {
		final String querySince = (since == null) ? "" :
				"AND p.post_id" + (desc ? "<" : ">") + since + ' ';
		final String queryOrder = (!desc ? "ORDER BY p.created, p.post_id " :
				"ORDER BY p.created DESC, p.post_id DESC ");
		return jdbcTemplate.query(
				"SELECT u.nickname AS author, p.created AS created," +
							" f.slug AS forum, p.post_id AS id, " +
							"p.isedited AS isEdited, p.mess AS message, " +
							"p.parent_id AS parent, th.thread_id AS thread " +
						"FROM threads th " +
							"JOIN forums f ON th.forum_id = f.forum_id " +
							"JOIN posts p ON th.thread_id=p.thread_id " +
							"JOIN users u ON p.author_id = u.user_id " +
						"WHERE th.thread_id=? " +
						querySince + queryOrder +
						(limit != null ? (" LIMIT " + limit) : ""),
				new Object[] { threadID },
				new PostModel.PostMapper()
		);
	}

	private List<PostModel> treeSort(Integer threadID, Boolean desc,
	                                 Integer limit, Long since) {
		final String querySince = (since == null) ? "" :
				"AND p.path" + (desc ? "<" : ">") + "(SELECT path FROM posts WHERE post_id=" + since + ") ";
		final String queryOrder = "ORDER BY p.path " + (desc ? "DESC " : "");

		return jdbcTemplate.query(
				"SELECT u.nickname AS author, p.created AS created," +
							" f.slug AS forum, p.post_id AS id, " +
							"p.isedited AS isEdited, p.mess AS message, " +
							"p.parent_id AS parent, th.thread_id AS thread " +
						"FROM threads th " +
							"JOIN forums f ON th.forum_id = f.forum_id " +
							"JOIN posts p ON th.thread_id=p.thread_id " +
							"JOIN users u ON p.author_id = u.user_id " +
						"WHERE th.thread_id=? " +
						querySince + queryOrder +
						(limit != null ? (" LIMIT " + limit) : ""),
				new Object[] { threadID },
				new PostModel.PostMapper()
		);
	}

	private List<PostModel> parentTreeSort(Integer threadID, Boolean desc,
	                                       Integer limit, Long since) {
		final String querySince = (since == null) ? "" :
				"AND pq.path" + (desc ? "<" : ">") + "(SELECT path FROM posts WHERE post_id=" + since + ") ";
		final String queryOrder = "ORDER BY p.path " + (desc ? "DESC " : "");
		final String subqueryOrder = "ORDER BY pq.path " + (desc ? "DESC " : "");

		if (limit != null) {
			return jdbcTemplate.query(
					"SELECT u.nickname AS author, p.created AS created," +
								" f.slug AS forum, p.post_id AS id, " +
								"p.isedited AS isEdited, p.mess AS message, " +
								"p.parent_id AS parent, th.thread_id AS thread " +
							"FROM threads th " +
								"JOIN forums f ON th.forum_id = f.forum_id " +
								"JOIN posts p ON th.thread_id=p.thread_id " +
								"JOIN users u ON p.author_id = u.user_id " +
							"WHERE path[1] IN " +
								"(SELECT pq.post_id FROM posts pq " +
								"WHERE pq.parent_id=0 AND pq.thread_id=? " +
								querySince + subqueryOrder +
								" LIMIT " + limit + ") " +
							queryOrder,
					new Object[]{threadID},
					new PostModel.PostMapper()
			);
		} else {
			return jdbcTemplate.query(
					"SELECT u.nickname AS author, p.created AS created," +
								" f.slug AS forum, p.post_id AS id, " +
								"p.isedited AS isEdited, p.mess AS message, " +
								"p.parent_id AS parent, th.thread_id AS thread " +
							"FROM threads th " +
								"JOIN forums f ON th.forum_id = f.forum_id " +
								"JOIN posts p ON th.thread_id=p.thread_id " +
								"JOIN users u ON p.author_id = u.user_id " +
								"JOIN posts prnt ON prnt.post_id=p.path[1] " +
							"WHERE th.thread_id=? AND prnt.parent_id=0 " +
							queryOrder,
					new Object[] { threadID },
					new PostModel.PostMapper()
			);
		}
	}
}
