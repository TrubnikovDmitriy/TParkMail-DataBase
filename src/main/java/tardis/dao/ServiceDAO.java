package tardis.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class ServiceDAO {

	private final JdbcTemplate jdbcTemplate;

	public ServiceDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void truncate() {
		jdbcTemplate.update("TRUNCATE users RESTART IDENTITY CASCADE");
	}

	public Integer getNumberUsers() {
		return jdbcTemplate.queryForObject("SELECT COUNT(user_id) FROM users", Integer.class);
	}

	public Integer getNumberForums() {
		return jdbcTemplate.queryForObject("SELECT COUNT(forum_id) FROM forums", Integer.class);
	}

	public Integer getNumberThreads() {
		return jdbcTemplate.queryForObject("SELECT COUNT(thread_id) FROM threads", Integer.class);
	}

	public Integer getNumberPosts() {
		return jdbcTemplate.queryForObject("SELECT COUNT(post_id) FROM posts", Integer.class);
	}
}
