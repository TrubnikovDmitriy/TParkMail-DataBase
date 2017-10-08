package application.dao;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by dmitriy on 08.10.17.
 */
public class ThreadDAO {

	private JdbcTemplate jdbcTemplate;

	public ThreadDAO(JdbcTemplate jdbcTempl) {
			jdbcTemplate = jdbcTempl;
	}
}
