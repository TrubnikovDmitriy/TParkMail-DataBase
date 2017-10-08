package application.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class UserModel {

	@JsonProperty(required = true)
	private String email;
	@JsonProperty(required = true)
	private String fullname;
	@JsonProperty
	private String about;
	@JsonIgnore
	private int id;
	private String nickname;

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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public static final class UserMapper implements RowMapper<UserModel> {
		@Override
		public UserModel mapRow(ResultSet rs, int rowNum) throws SQLException {
			final UserModel userModel = new UserModel();
			userModel.about = rs.getString("about");
			userModel.email = rs.getString("email");
			userModel.fullname = rs.getString("fullname");
			userModel.nickname = rs.getString("nickname");
			userModel.id = rs.getInt("user_id");
			return userModel;
		}
	}
}
