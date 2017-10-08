package application.dao;

import org.springframework.jdbc.core.JdbcTemplate;


public class ThreadDAO {

	private JdbcTemplate jdbcTemplate;

	public ThreadDAO(JdbcTemplate jdbcTempl) {
			jdbcTemplate = jdbcTempl;
	}
}
