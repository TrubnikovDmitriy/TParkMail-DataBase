package tardis.dao;

import org.springframework.jdbc.core.JdbcTemplate;
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


	public UserModel getUserByID(Integer userID) {
		return jdbcTemplate.queryForObject(
				"SELECT * FROM users WHERE user_id=?",
				new Object[] { userID },
				(rs, rn) -> new UserModel(
						rs.getInt(1),
						rs.getString(2),
						rs.getString(3),
						rs.getString(4),
						rs.getString(5)
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

	public List<UserModel> getUsers(String forumSlug, Integer limit,
	                                String since, Boolean desc) {
		final Long forumID = jdbcTemplate.queryForObject(
				"SELECT forum_id FROM forums WHERE slug=?::citext",
				Long.class, forumSlug
		);
//		if (since == null) {
//			return jdbcTemplate.query(
//					"SELECT nickname, fullname, email, about " +
//							"FROM threads th " +
//							"JOIN users u ON th.author_id = u.user_id " +
//							"WHERE th.forum_id=? " +
//						"UNION " +
//						"SELECT nickname, fullname, email, about " +
//							"FROM threads th " +
//							"JOIN posts p ON p.thread_id = th.thread_id " +
//							"JOIN users u ON p.author_nickname = u.nickname " +
//							"WHERE th.forum_id=? " +
//						"ORDER BY nickname " + (desc ? "DESC " : "ASC ") +
//						(limit != null ? ("LIMIT " + limit) : ""),
//					new Object[]{forumID, forumID},
//					new UserModel.UserMapper()
//			);
//		} else {
//			final String queryWhere = "WHERE th.forum_id=? AND u.nickname" +
//							(desc ? "<?::citext" : ">?::citext");
//			return jdbcTemplate.query(
//					"SELECT nickname, fullname, email, about " +
//							"FROM threads th " +
//							"JOIN users u ON th.author_id = u.user_id " +
//							queryWhere +
//						" UNION " +
//						"SELECT nickname, fullname, email, about " +
//							"FROM threads th " +
//							"JOIN posts p ON p.thread_id = th.thread_id " +
//							"JOIN users u ON p.author_nickname = u.nickname " +
//							queryWhere +
//						" ORDER BY nickname " + (desc ? "DESC " : "ASC ") +
//						(limit != null ? ("LIMIT " + limit) : ""),
//					new Object[]{forumID, since, forumID, since},
//					new UserModel.UserMapper()
//			);
//		}


		if (since == null) {
			return jdbcTemplate.query(
					"SELECT nickname, fullname, email, about " +
							"FROM forum_users fu " +
							"JOIN users u ON fu.user_id=u.user_id " +
							"WHERE fu.forum_id=? " +
							"ORDER BY nickname " + (desc ? "DESC " : "ASC ") +
							(limit != null ? ("LIMIT " + limit) : ""),
					new Object[]{ forumID },
					new UserModel.UserMapper()
			);
		} else {
			final String queryWhere = "WHERE fu.forum_id=? AND u.nickname" +
					(desc ? "<?::citext" : ">?::citext");
			return jdbcTemplate.query(
					"SELECT nickname, fullname, email, about " +
							"FROM forum_users fu " +
							"JOIN users u ON fu.user_id=u.user_id " +
							queryWhere +
							" ORDER BY nickname " + (desc ? "DESC " : "ASC ") +
							(limit != null ? ("LIMIT " + limit) : ""),
					new Object[]{ forumID, since },
					new UserModel.UserMapper()
			);
		}
	}
}
