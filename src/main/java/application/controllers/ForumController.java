package application.controllers;

import application.dao.ForumDAO;
import application.dao.ThreadDAO;
import application.models.ForumModel;
import application.models.ThreadModel;
import application.models.UserModel;
import application.views.ErrorView;
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
public class ForumController{

	private final ThreadDAO threadDAO;
	private final ForumDAO forumDAO;

	public ForumController(ThreadDAO threadDAO,
	                       ForumDAO forumDAO) {
		this.threadDAO = threadDAO;
		this.forumDAO = forumDAO;
	}


	@PostMapping(path = "/create")
	public ResponseEntity createForum(@RequestBody ForumModel forumModel) {

		try {
			return new ResponseEntity<ForumModel>(
					forumDAO.createNewForum(forumModel),
					HttpStatus.CREATED
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<ErrorView>(
					new ErrorView("Can't find user with nickname " + forumModel.getNickname()),
					HttpStatus.NOT_FOUND
			);
		}
		catch (DuplicateKeyException e) {
			return new ResponseEntity<ForumModel>(
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
			return new ResponseEntity<ThreadModel>(
					forumDAO.createNewThread(forumSlug, threadModel),
					HttpStatus.CREATED
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<ErrorView>(
					new ErrorView("Author or forumSlug do not exists!"),
					HttpStatus.NOT_FOUND
			);
		}
		catch (DuplicateKeyException e) {
			return new ResponseEntity<ThreadModel>(
					threadDAO.getThreadBySlug(threadModel.getThreadSlug()),
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

	@GetMapping(path = "/{forumSlug}/details")
	public ResponseEntity getForum(@PathVariable String forumSlug) {
		try {
			return new ResponseEntity<ForumModel>(
					forumDAO.getForumBySlug(forumSlug),
					HttpStatus.OK
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<ErrorView>(
					new ErrorView("Forum '" + forumSlug + "' do not exists!"),
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
			final List<ThreadModel> threads = forumDAO.getThreads(forumSlug, limit, since, desc);
			return new ResponseEntity<List<ThreadModel>>(
					threads,
					HttpStatus.OK
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<ErrorView>(
					new ErrorView("Forum '" + forumSlug + "' do not exists!"),
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

	@GetMapping(path = "/{forumSlug}/users")
	public ResponseEntity getUsers(
			@PathVariable String forumSlug,
			@RequestParam(name = "limit", required = false, defaultValue = "100") Integer limit,
			@RequestParam(name = "desc", required = false, defaultValue = "false") Boolean desc,
			@RequestParam(name = "since", required = false) String since) {

		try {
			final List<UserModel> users = forumDAO.getUsers(forumSlug, limit, since, desc);
			return new ResponseEntity<List<UserModel>>(
							users,
							HttpStatus.OK
					);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<ErrorView>(
					new ErrorView("Forum '" + forumSlug + "' do not exists!"),
					HttpStatus.NOT_FOUND
			);
		}
		catch (RuntimeException e) {
			return new ResponseEntity<ErrorView>(
					new ErrorView(e.getMessage()),
					HttpStatus.BAD_REQUEST
			);
		}
	}
}
