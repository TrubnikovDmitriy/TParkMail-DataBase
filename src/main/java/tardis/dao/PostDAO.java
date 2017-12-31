package tardis.dao;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import tardis.models.PostModel;
//import tardis.models.PostUpdateModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
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
	private final UserDAO userDAO;

	public PostDAO(JdbcTemplate jdbcTemplate, ThreadDAO threadDAO, UserDAO userDAO) {
		this.jdbcTemplate = jdbcTemplate;
		this.threadDAO = threadDAO;
		this.userDAO = userDAO;
	}

	//	public PostModel getPostById(Long postId) {
//
//		return jdbcTemplate.queryForObject(
//				"SELECT author, created, slug AS forum, post_id AS id, path," +
//						"isedited, message, parent_id AS parent, th.thread_id AS thread " +
//						"FROM posts p NATURAL JOIN posts_extra px " +
//						"JOIN threads th ON p.post_id=? AND p.thread_id=th.thread_id " +
//						"NATURAL JOIN forums f ",
//				new Object[] {postId},
//				new PostModel.PostMapper()
//		);
//	}

//	public PostModel updatePost(Long postId, PostUpdateModel postUpdate) {
//
//		final PostModel postModel = getPostById(postId);
//		if (postUpdate.getMessage() != null &&
//				!postModel.getMessage().equals(postUpdate.getMessage())) {
//
//			jdbcTemplate.update(
//					"UPDATE posts_extra SET isedited=TRUE, message=? WHERE post_id=?",
//					postUpdate.getMessage(), postId
//			);
//			postModel.setMessage(postUpdate.getMessage());
//			postModel.setEdited(true);
//		}
//		return postModel;
//	}

	public List<PostModel> createNewPosts(String threadIdOrSlug, List<PostModel> posts) {

		final ThreadModel thread = threadDAO.getFullThreadByIdOrSlug(threadIdOrSlug);

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
								new Object[]{ posts.get(0).getAuthor() },
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

}
