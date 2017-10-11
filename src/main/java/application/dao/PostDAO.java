package application.dao;

import application.models.PostModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PostDAO {

	private final JdbcTemplate jdbcTemplate;

	PostDAO(JdbcTemplate jdbcTempl) {
		this.jdbcTemplate = jdbcTempl;
	}

//	public PostModel createNewPost(PostModel postModel) {
//
//	}
}
