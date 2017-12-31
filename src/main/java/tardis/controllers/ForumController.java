package tardis.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tardis.dao.ForumDAO;
import tardis.dao.ThreadDAO;
import tardis.dao.UserDAO;
import tardis.models.ForumModel;
import tardis.models.ThreadModel;
import tardis.models.UserModel;
import tardis.views.ErrorView;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api/forum")
public class ForumController {

	private final ThreadDAO threadDAO;
	private final ForumDAO forumDAO;
	private final UserDAO userDAO;

	public ForumController(ThreadDAO threadDAO,
	                       ForumDAO forumDAO,
	                       UserDAO userDAO) {
		this.threadDAO = threadDAO;
		this.forumDAO = forumDAO;
		this.userDAO = userDAO;
	}

	private final Logger logger = LoggerFactory.getLogger(ForumController.class);


	@PostMapping(path = "/create")
	public ResponseEntity createForum(@RequestBody ForumModel forumModel) {

		try {
			return new ResponseEntity<>(
					forumDAO.createNewForum(forumModel),
					HttpStatus.CREATED
			);
		} catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<>(
					new ErrorView("User does not exist"),
					HttpStatus.NOT_FOUND
			);
		} catch (DuplicateKeyException e) {
			return new ResponseEntity<>(
					forumDAO.getForumBySlug(forumModel.getSlug()),
					HttpStatus.CONFLICT
			);
		}
	}

	@PostMapping(path = "/{forumSlug}/create")
	public ResponseEntity createThread(
			@PathVariable String forumSlug,
			@RequestBody ThreadModel threadModel) {

		try {
			threadModel.setForumSlug(forumSlug);
			return new ResponseEntity<>(
					threadDAO.createNewThread(threadModel),
					HttpStatus.CREATED
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<>(
					new ErrorView("Author or forumSlug do not exists!"),
					HttpStatus.NOT_FOUND
			);
		}
		catch (DuplicateKeyException e) {
			return new ResponseEntity<>(
					threadDAO.getThreadBySlug(threadModel.getThreadSlug()),
					HttpStatus.CONFLICT
			);
		}
	}

	@GetMapping(path = "/{forumSlug}/details")
	public ResponseEntity getForum(@PathVariable String forumSlug) {
		try {
			return new ResponseEntity<>(
					forumDAO.getForumBySlug(forumSlug),
					HttpStatus.OK
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<>(
					new ErrorView("Forum does not exist"),
					HttpStatus.NOT_FOUND
			);
		}
	}

	@GetMapping(path = "/{forumSlug}/threads")
	public ResponseEntity getThreads(
			@PathVariable String forumSlug,
			@RequestParam(name = "limit", required = false, defaultValue = "100") Integer limit,
			@RequestParam(name = "desc", required = false, defaultValue = "false") Boolean desc,
			@RequestParam(name = "since", required = false)
			@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date since) {

		try {
			return new ResponseEntity<>(
					forumDAO.getThreads(forumSlug, limit, since, desc),
					HttpStatus.OK
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<>(
					new ErrorView("Forum does not exist"),
					HttpStatus.NOT_FOUND
			);
		}
	}

	@GetMapping(path = "/{forumSlug}/users")
	public ResponseEntity getUsers(
			@PathVariable String forumSlug,
			@RequestParam(name = "desc", required = false, defaultValue = "false") Boolean desc,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "since", required = false) String since) {

		try {
			return new ResponseEntity<>(
					userDAO.getUsers(forumSlug, limit, since, desc),
					HttpStatus.OK
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<>(
					new ErrorView("Forum '" + forumSlug + "' do not exists!"),
					HttpStatus.NOT_FOUND
			);
		}
	}
}
