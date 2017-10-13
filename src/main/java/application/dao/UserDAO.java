package application.dao;

import application.models.UserModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public class UserDAO {

	private final JdbcTemplate jdbcTemplate;

	UserDAO(JdbcTemplate jdbcTeml) {
		this.jdbcTemplate = jdbcTeml;
	}

	public UserModel getUserByNickname(String nickname) {
		return jdbcTemplate.queryForObject(
				"SELECT * FROM users " +
					"NATURAL JOIN users_extra " +
					"WHERE LOWER(nickname)=LOWER(?)",
				new Object[] {nickname},
				new UserModel.UserMapper()
		);
	}

	public UserModel getUserByEmail(String email) {
		return jdbcTemplate.queryForObject(
				"SELECT * FROM users " +
						"NATURAL JOIN users_extra " +
						"WHERE LOWER(email)=LOWER(?)",
				new Object[] {email},
				new UserModel.UserMapper()
		);
	}

	public List<UserModel> getUsersByNicknameOrEmail(String nickname, String email) {
		return jdbcTemplate.query(
				"SELECT * FROM users NATURAL JOIN users_extra " +
					"WHERE LOWER(nickname)=LOWER(?) OR LOWER(email)=LOWER(?)",
				new Object[] {nickname, email},
				new UserModel.UserMapper()
		);
	}

	@Transactional
	public UserModel createNewUser(UserModel user) {

		user.setId(jdbcTemplate.queryForObject(
				"INSERT INTO users(nickname) " +
				"VALUES (?) RETURNING user_id",
				new Object[] {user.getNickname()},
				Integer.class)
		);
		jdbcTemplate.update(
				"INSERT INTO users_extra(user_id, fullname, email, about) VALUES(?, ?, ?, ?)",
				user.getId(), user.getFullname(), user.getEmail(), user.getAbout()
		);
		return user;
	}

	@Transactional
	public UserModel updateUser(UserModel user) {
		user.setId(jdbcTemplate.queryForObject(
				"SELECT user_id FROM users WHERE LOWER(nickname)=LOWER(?)",
				Integer.class, user.getNickname()
		));
		if (user.getFullname() != null) {
			jdbcTemplate.update(
					"UPDATE users_extra SET " +
						"fullname=? WHERE user_id=?",
					user.getFullname(), user.getId()
			);
		}
		if (user.getEmail() != null) {
			jdbcTemplate.update(
					"UPDATE users_extra SET " +
						"email=? WHERE user_id=?",
					user.getEmail(), user.getId()
			);
		}
		if (user.getAbout() != null) {
			jdbcTemplate.update(
					"UPDATE users_extra SET " +
						"about=? WHERE user_id=?",
					user.getAbout(), user.getId()
			);
		}
		user = getUserByNickname(user.getNickname());
		return user;
	}
}
