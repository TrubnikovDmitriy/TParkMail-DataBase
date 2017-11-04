package application.controllers;

import application.dao.ThreadDAO;
import application.models.PostModel;
import application.models.ThreadModel;
import application.models.ThreadUpdateModel;
import application.models.VoteModel;
import application.views.ErrorView;
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

	ThreadController(ThreadDAO threadDAO) {
		this.threadDAO = threadDAO;
	}

	@PostMapping(path = "/create")
	public ResponseEntity createPosts(
			@PathVariable String threadIdOrSlug,
			@RequestBody List<PostModel> posts) {

		try {
			if (!threadDAO.checkParents(posts, threadIdOrSlug)) {
				return new ResponseEntity<ErrorView>(
						new ErrorView("Одно или несколько из родительских сообщений отстувуют"),
						HttpStatus.CONFLICT
				);
			}
			return new ResponseEntity<List<PostModel>>(
					threadDAO.createNewPosts(threadIdOrSlug, posts),
					HttpStatus.CREATED
			);
		}
		catch (DataIntegrityViolationException | EmptyResultDataAccessException e) {
			return new ResponseEntity<ErrorView>(
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
			return new ResponseEntity<ThreadModel>(
					threadDAO.voteForThread(threadIdOrSlug, voteModel),
					HttpStatus.OK
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<ErrorView>(
					new ErrorView("Ветки по указанному slug/id не найдено"),
					HttpStatus.NOT_FOUND
			);
		}
	}

	@GetMapping(path = "/details")
	public ResponseEntity getThread(@PathVariable  String threadIdOrSlug) {
		try {
			return new ResponseEntity<ThreadModel>(
					threadDAO.getFullThreadByIdOrSlug(threadIdOrSlug),
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
			@RequestParam(name = "limit", required = false, defaultValue = "100") Integer limit,
			@RequestParam(name = "sort", required = false, defaultValue = "flat") String sort,
			@RequestParam(name = "desc", required = false, defaultValue = "false") Boolean desc,
			@RequestParam(name = "since", required = false) Long since) {
		try {
			return new ResponseEntity<List<PostModel>>(
					threadDAO.getPosts(threadIdOrSlug, limit, sort, desc, since),
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
}
