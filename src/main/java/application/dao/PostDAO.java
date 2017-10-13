package application.dao;

import application.models.PostModel;
import application.models.PostUpdateModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class PostDAO {

	private final JdbcTemplate jdbcTemplate;

	PostDAO(JdbcTemplate jdbcTempl) {
		this.jdbcTemplate = jdbcTempl;
	}

	public PostModel getPostById(Long postId) {

		return jdbcTemplate.queryForObject(
				"SELECT nickname AS author, created, slug AS forum, post_id AS id, path," +
						"isedited, message, parent_id AS parent, th.thread_id AS thread " +
						"FROM posts p NATURAL JOIN posts_extra px " +
						"JOIN threads th ON p.post_id=? AND p.thread_id=th.thread_id " +
						"NATURAL JOIN forums f " +
						"JOIN users u ON u.user_id=p.author_id ",
				new Object[] {postId},
				new PostModel.PostMapper()
		);
	}

	public PostModel updatePost(Long postId, PostUpdateModel postUpdate) {

		final PostModel postModel = getPostById(postId);
		if (postUpdate.getMessage() != null &&
				!postModel.getMessage().equals(postUpdate.getMessage())) {

			jdbcTemplate.update(
					"UPDATE posts_extra SET isedited=TRUE, message=? WHERE post_id=?",
					postUpdate.getMessage(), postId
			);
			postModel.setMessage(postUpdate.getMessage());
			postModel.setEdited(true);
		}
		return postModel;
	}
}
