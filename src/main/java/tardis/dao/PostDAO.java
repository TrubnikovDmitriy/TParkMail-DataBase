package tardis.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import tardis.models.PostModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tardis.models.PostUpdateModel;
import tardis.models.ThreadModel;
import tardis.models.UserModel;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;


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
				"SELECT p.post_id, p.thread_id, p.author_nickname, " +
						"p.parent_id, p.mess, p.created, p.isedited, p.forum_slug " +
						"FROM posts p WHERE p.post_id=?",
				new Object[] { postID },
				(rs, rn) -> {
					final PostModel post = new PostModel();
					post.setPostID(rs.getInt(1));
					post.setThreadID(rs.getInt(2));
					post.setAuthor(rs.getString(3));
					post.setParentID(rs.getInt(4));
					post.setMessage(rs.getString(5));
					post.setCreated(rs.getTimestamp(6));
					post.setEdited(rs.getBoolean(7));
					post.setForumSlug(rs.getString(8));
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
				"INSERT INTO posts(post_id, thread_id, author_nickname, " +
						"parent_id, created, mess, forum_slug) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?)",
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int rowNumber)
							throws SQLException {

						final PostModel post  = posts.get(rowNumber);

						// Для возвращаемого значения
						post.setAuthor(posts.get(rowNumber).getAuthor());
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
						ps.setString(3, post.getAuthor());
						ps.setLong(4, post.getParentID());
						ps.setTimestamp(5, post.getCreated());
						ps.setString(6, post.getMessage());
						ps.setString(7, thread.getForumSlug());

						posts.set(rowNumber, post);
					}
					@Override
					public int getBatchSize() {
						return posts.size();
					}
				}
		);


		if (!posts.isEmpty()) {

			// Обновить счетчик постов в форуме
			jdbcTemplate.update(
					"UPDATE forums SET post_count=post_count+? WHERE slug=?::citext",
					posts.size(), thread.getForumSlug()
			);


			// Вставить новых авторов постов в forum_users
			final MapSqlParameterSource parameters =new MapSqlParameterSource();
			final ArrayList<String> userNames = new ArrayList<>(posts.size());
			for (PostModel post : posts) {
				if (!userNames.contains(post.getAuthor())) {
					userNames.add(post.getAuthor());
				}
			}
			parameters.addValue("names", userNames);


			final NamedParameterJdbcTemplate namedParameterJdbcTemplate =
					new NamedParameterJdbcTemplate(jdbcTemplate);

			// Получаем ID юзеров, которых еще нет в forum_users
			final List<Integer> userID = namedParameterJdbcTemplate.query(
					"SELECT user_id FROM users " +
							"WHERE nickname IN (:names) AND user_id NOT IN " +
							"(SELECT user_id FROM forum_users WHERE forum_id=" +
							thread.getForumID() + ')',
					parameters, (rs, rn) -> rs.getInt(1)
			);

			// Вставляем этих юзеров
			jdbcTemplate.batchUpdate(
					"INSERT INTO forum_users(forum_id, user_id) " +
							"VALUES (?, ?) ON CONFLICT DO NOTHING",
					new BatchPreparedStatementSetter() {
						@Override
						public void setValues(PreparedStatement ps, int rowNumber)
								throws SQLException {
							ps.setInt(1, thread.getForumID());
							ps.setInt(2, userID.get(rowNumber));
						}

						@Override
						public int getBatchSize() {
							return userID.size();
						}
					}
			);
		}
		return posts;
	}

	public PostModel updatePost(Integer postID, PostUpdateModel updatePost) {

		final PostModel post = jdbcTemplate.queryForObject(
				"SELECT p.author_nickname AS author, p.created AS created, " +
						"p.forum_slug AS forum, p.post_id AS id, " +
						"p.isedited AS isEdited, p.mess AS message, " +
						"p.parent_id AS parent, p.thread_id AS thread " +
						"FROM posts p " +
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

		try {
			for (PostModel post : posts) {
				if (post.getParentID() == null) continue;
				jdbcTemplate.queryForObject(
						"SELECT post_id FROM posts WHERE thread_id=? AND post_id=?",
						new Object[] { threadID, post.getParentID() },
						(rs, rn) -> rs.getInt(1)
				);
			}
		} catch (EmptyResultDataAccessException e) {
			return false;
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
				"SELECT p.author_nickname AS author, p.created AS created, " +
							"p.forum_slug AS forum, p.post_id AS id, " +
							"p.isedited AS isEdited, p.mess AS message, " +
							"p.parent_id AS parent, p.thread_id AS thread " +
						"FROM posts p WHERE p.thread_id=? " +
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
				"SELECT p.author_nickname AS author, p.created AS created, " +
							"p.forum_slug AS forum, p.post_id AS id, " +
							"p.isedited AS isEdited, p.mess AS message, " +
							"p.parent_id AS parent, p.thread_id AS thread " +
						"FROM posts p WHERE p.thread_id=? " +
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
					"SELECT p.author_nickname AS author, p.created AS created, " +
								"p.forum_slug AS forum, p.post_id AS id, " +
								"p.isedited AS isEdited, p.mess AS message, " +
								"p.parent_id AS parent, p.thread_id AS thread " +
							"FROM posts p JOIN (" +
								"SELECT pq.post_id FROM posts pq " +
								"WHERE pq.parent_id=0 AND pq.thread_id=? " +
								querySince + subqueryOrder +
								" LIMIT " + limit +
							") AS psq ON psq.post_id=p.path[1]" +
							"WHERE p.thread_id=? " +
							queryOrder,
					new Object[]{ threadID, threadID },
					new PostModel.PostMapper()
			);
		} else {
			return jdbcTemplate.query(
					"SELECT p.author_nickname AS author, p.created AS created, " +
								"p.forum_slug AS forum, p.post_id AS id, " +
								"p.isedited AS isEdited, p.mess AS message, " +
								"p.parent_id AS parent, p.thread_id AS thread " +
							"FROM posts p " +
								"JOIN posts prnt ON prnt.post_id=p.path[1] " +
							"WHERE p.thread_id=? AND prnt.parent_id=0 " +
							queryOrder,
					new Object[] { threadID },
					new PostModel.PostMapper()
			);
		}
	}
}
