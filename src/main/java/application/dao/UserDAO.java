package application.dao;

import application.models.UserModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
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
						"WHERE nickname=?",
				new Object[] {nickname},
				new UserModel.UserMapper()
		);
	}

	public UserModel getUserByEmail(String email) {
		return jdbcTemplate.queryForObject(
				"SELECT * FROM users " +
						"NATURAL JOIN users_extra " +
						"WHERE email=?",
				new Object[] {email},
				new UserModel.UserMapper()
		);
	}

	public List<UserModel> getUsersByNicknameOrEmail(String nickname, String email) {
		return jdbcTemplate.query(
				"SELECT * FROM users NATURAL JOIN users_extra " +
						"WHERE nickname=? OR email=?",
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
				"SELECT user_id FROM users WHERE nickname=?",
				Integer.class, user.getNickname()
		));
		jdbcTemplate.update(
				"UPDATE users_extra SET " +
					"(fullname, email, about)=(?, ?, ?) " +
					"WHERE user_id=?",
				user.getFullname(), user.getEmail(), user.getAbout(),
				user.getId()
		);
		return user;
	}
}
