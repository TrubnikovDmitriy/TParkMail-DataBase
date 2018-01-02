package tardis.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


@SuppressWarnings("unused")
public class UserModel {

	@JsonIgnore
	private Integer id;
	private String nickname;
	@JsonProperty
	private String fullname;
	@JsonProperty
	private String email;
	@JsonProperty
	private String about;

	public UserModel() { }

	public UserModel(String email, String fullname, String about) {
		this.email = email;
		this.fullname = fullname;
		this.about = about;
	}

	public UserModel(String email, String fullname, String about, String nickname) {
		this.email = email;
		this.fullname = fullname;
		this.about = about;
		this.nickname = nickname;
	}

	public UserModel(Integer id, String nickname,
	                 String fullname, String email, String about) {
		this.id = id;
		this.nickname = nickname;
		this.fullname = fullname;
		this.email = email;
		this.about = about;
	}

	public UserModel(Integer id, String nickname) {
		this.id = id;
		this.nickname = nickname;
	}

	public void update(UserModel update) {
		if (update.about != null) {
			about = update.about;
		}
		if (update.email != null) {
			email = update.email;
		}
		if (update.fullname != null) {
			fullname = update.fullname;
		}
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Integer getID() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public static final class UserMapper implements RowMapper<UserModel> {
		@Override
		public UserModel mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new UserModel(
					rs.getString("email"),
					rs.getString("fullname"),
					rs.getString("about"),
					rs.getString("nickname")
			);
		}
	}
}
