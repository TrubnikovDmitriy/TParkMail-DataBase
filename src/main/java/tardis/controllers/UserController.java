package tardis.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tardis.dao.UserDAO;
import tardis.models.UserModel;
import tardis.views.ErrorView;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api/user/{nickname}")
public class UserController {

	private final UserDAO userDAO;
	private final Logger logger = LoggerFactory.getLogger(UserController.class);

	UserController(UserDAO userDAO) {
		this.userDAO = userDAO;
	}


	@PostMapping(path = "/create")
	public ResponseEntity createNewUser(
			@PathVariable String nickname,
			@RequestBody UserModel user) {
		try {
			user.setNickname(nickname);
			userDAO.createNewUser(user);
			return new ResponseEntity<>(user, HttpStatus.CREATED);
		}
		catch (DuplicateKeyException e) {
			List<UserModel> users = userDAO.getUserByNicknameOrEmail(nickname, user.getEmail());
			return new ResponseEntity<>(
					users,
					HttpStatus.CONFLICT
			);
		}
	}

	@GetMapping(path = "/profile")
	public ResponseEntity getUser(@PathVariable String nickname) {
		try {
			return new ResponseEntity<>(
					userDAO.getUserByNickname(nickname),
					HttpStatus.OK
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<>(
					new ErrorView("User does not exist"),
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
			return new ResponseEntity<>(
					userDAO.updateUser(user),
					HttpStatus.OK
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<>(
					new ErrorView("User does not exist"),
					HttpStatus.NOT_FOUND
			);
		}
		catch (DuplicateKeyException e) {
			return new ResponseEntity<>(
					userDAO.getUserByEmail(user.getEmail()),
					HttpStatus.CONFLICT
			);
		}
	}
}
