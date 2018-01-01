package tardis.controllers;

import org.postgresql.util.PSQLException;
import tardis.dao.PostDAO;
import tardis.dao.ThreadDAO;
import tardis.models.PostModel;
import tardis.models.ThreadModel;
import tardis.models.ThreadUpdateModel;
import tardis.models.VoteModel;
import tardis.views.ErrorView;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api/thread/{threadIdOrSlug}")
public class ThreadController{

	private final ThreadDAO threadDAO;
	private final PostDAO postDAO;

	public ThreadController(ThreadDAO threadDAO, PostDAO postDAO) {
		this.threadDAO = threadDAO;
		this.postDAO = postDAO;
	}

	@PostMapping(path = "/create")
	public ResponseEntity createPosts(
			@PathVariable String threadIdOrSlug,
			@RequestBody List<PostModel> posts) {

		try {
			final ThreadModel thread = threadDAO.getFullThreadByIdOrSlug(threadIdOrSlug);
			if (!postDAO.checkParents(posts, thread.getThreadID())) {
				return new ResponseEntity<>(
						new ErrorView("Одно или несколько из родительских сообщений отстувуют"),
						HttpStatus.CONFLICT
				);
			}
			return new ResponseEntity<>(
					postDAO.createNewPosts(thread, posts),
					HttpStatus.CREATED
			);
		} catch (DataIntegrityViolationException | EmptyResultDataAccessException e) {
			return new ResponseEntity<>(
					new ErrorView(e.getMessage()),
					HttpStatus.NOT_FOUND
			);
		}
	}

	@PostMapping(path = "/vote")
	public ResponseEntity vote(
			@PathVariable String threadIdOrSlug,
			@RequestBody VoteModel voteModel) {
		try {
			return new ResponseEntity<>(
					threadDAO.voteForThread(threadIdOrSlug, voteModel),
					HttpStatus.OK
			);
		} catch (DataIntegrityViolationException | EmptyResultDataAccessException e) {
			return new ResponseEntity<>(
					new ErrorView("Thread does not exist"),
					HttpStatus.NOT_FOUND
			);
		}
	}

	@GetMapping(path = "/details")
	public ResponseEntity getThread(@PathVariable  String threadIdOrSlug) {
		try {
			return new ResponseEntity<>(
					threadDAO.getFullThreadByIdOrSlug(threadIdOrSlug),
					HttpStatus.OK
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<>(
					new ErrorView(e.getMessage()),
					HttpStatus.NOT_FOUND
			);
		}
	}

	@PostMapping(path = "/details")
	public ResponseEntity updateThread(
			@PathVariable  String threadIdOrSlug,
			@RequestBody ThreadUpdateModel threadUpdate) {
		try {
			return new ResponseEntity<ThreadModel>(
					threadDAO.updateThread(threadIdOrSlug, threadUpdate),
					HttpStatus.OK
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<ErrorView>(
					new ErrorView(e.getMessage()),
					HttpStatus.NOT_FOUND
			);
		}
	}

	@GetMapping(path = "/posts")
	public ResponseEntity getPosts(
			@PathVariable String threadIdOrSlug,
			@RequestParam(name = "sort", required = false, defaultValue = "flat") String sort,
			@RequestParam(name = "desc", required = false, defaultValue = "false") Boolean desc,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "since", required = false) Long since) {
		try {
			return new ResponseEntity<>(
					postDAO.getPosts(threadIdOrSlug, limit, sort, desc, since),
					HttpStatus.OK
			);
		} catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<>(
					new ErrorView(e.getMessage()),
					HttpStatus.NOT_FOUND
			);
		}
	}
}

