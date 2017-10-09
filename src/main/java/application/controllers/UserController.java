package application.controllers;

import application.dao.UserDAO;
import application.models.UserModel;
import application.views.ErrorView;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/user/{nickname}")
public class UserController {

	private final UserDAO userDAO;

	UserController(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	@PostMapping(path = "/create")
	public ResponseEntity createNewUser(
			@PathVariable String nickname,
			@RequestBody UserModel user) {
		try {
			user.setNickname(nickname);
			return new ResponseEntity<UserModel>(
					userDAO.createNewUser(user),
					HttpStatus.CREATED
			);
		}
		catch (DataAccessException e) {
			return new ResponseEntity<List<UserModel>>(
					userDAO.getUsersByNicknameOrEmail(nickname, user.getEmail()),
					HttpStatus.CONFLICT
			);
		}
		catch (RuntimeException e) {
			return new ResponseEntity<ErrorView>(
					new ErrorView(e.getMessage()),
					HttpStatus.BAD_REQUEST
			);
		}
	}

	@GetMapping(path = "/profile")
	public ResponseEntity getUser(@PathVariable String nickname) {
		try {
			return new ResponseEntity<UserModel>(
					userDAO.getUserByNickname(nickname),
					HttpStatus.OK
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<ErrorView>(
					new ErrorView("User with nickname '" + nickname + "' do not exists!"),
					HttpStatus.NOT_FOUND
			);
		}
		catch (RuntimeException e) {
			return new ResponseEntity<ErrorView>(
					new ErrorView(e.getMessage()),
					HttpStatus.NOT_FOUND
			);
		}
	}

	@PostMapping(path = "/profile")
	public ResponseEntity updateUser(
			@PathVariable String nickname,
			@RequestBody UserModel user) {
		try {
			user.setNickname(nickname);
			return new ResponseEntity<UserModel>(
					userDAO.updateUser(user),
					HttpStatus.OK
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<ErrorView>(
					new ErrorView("User with nickname '" + nickname + "' do not exists!"),
					HttpStatus.NOT_FOUND
			);
		}
		catch (DuplicateKeyException e) {
			return new ResponseEntity<UserModel>(
					userDAO.getUserByEmail(user.getEmail()),
					HttpStatus.CONFLICT
			);
		}
		catch (RuntimeException e) {
			return new ResponseEntity<ErrorView>(
					new ErrorView(e.getMessage()),
					HttpStatus.NOT_FOUND
			);
		}
	}
}
