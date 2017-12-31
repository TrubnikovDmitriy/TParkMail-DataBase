package tardis.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import tardis.models.UserModel;

import java.util.List;

@Repository
public class UserDAO {

	private final JdbcTemplate jdbcTemplate;

	public UserDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	public void createNewUser(UserModel user) {
		jdbcTemplate.update(
				"INSERT INTO users(nickname, fullname, email, about) VALUES (?, ?, ?, ?)",
				user.getNickname(), user.getFullname(), user.getEmail(), user.getAbout()
		);
	}

	public List<UserModel> getUserByNicknameOrEmail(String nickname, String email) {
		return jdbcTemplate.query(
				"SELECT nickname, fullname, email, about FROM users " +
						"WHERE nickname=?::citext OR email=?::citext",
				new Object[]{ nickname, email },
				(rs, rowNumber)-> new UserModel(
						rs.getString("email"),
						rs.getString("fullname"),
						rs.getString("about"),
						rs.getString("nickname")
				)
		);
	}

	public UserModel getUserByNickname(String nickname) {
		return jdbcTemplate.queryForObject(
				"SELECT nickname, fullname, email, about FROM users " +
						"WHERE nickname=?::citext",
				new Object[]{ nickname },
				(rs, rowNumber)-> new UserModel(
						rs.getString("email"),
						rs.getString("fullname"),
						rs.getString("about"),
						rs.getString("nickname")
				)
		);
	}

	public UserModel getUserByEmail(String email) {
		return jdbcTemplate.queryForObject(
				"SELECT nickname, fullname, email, about FROM users " +
						"WHERE email=?::citext",
				new Object[]{ email },
				(rs, rowNumber)-> new UserModel(
						rs.getString("email"),
						rs.getString("fullname"),
						rs.getString("about"),
						rs.getString("nickname")
				)
		);
	}

	public String getNicknameByID(Integer userID) {
		return jdbcTemplate.queryForObject(
				"SELECT nickname FROM users WHERE user_id=?",
				String.class, userID
		);
	}

	public UserModel updateUser(UserModel newUser) {
		final UserModel oldUser = getUserByNickname(newUser.getNickname());
		oldUser.update(newUser);
		jdbcTemplate.update(
				"UPDATE users SET (fullname, email, about)=(?, ?, ?)" +
						" WHERE nickname=?::citext",
				oldUser.getFullname(), oldUser.getEmail(), oldUser.getAbout(), oldUser.getNickname()
		);
		return oldUser;
	}

	public Integer getUserIdByNickname(String nickname) {
		return jdbcTemplate.queryForObject(
				"SELECT user_id FROM users WHERE nickname=?::citext",
				Integer.class, nickname
		);
	}
}
