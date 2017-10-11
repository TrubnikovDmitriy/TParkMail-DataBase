package application.controllers;


import application.dao.ThreadDAO;
import application.models.PostModel;
import application.models.ThreadModel;
import application.models.VoteModel;
import application.views.ErrorView;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/thread/{threadIdOrSlug}")
public class ThreadController{

	private final ThreadDAO threadDAO;

	ThreadController(ThreadDAO threadDAO) {
		this.threadDAO = threadDAO;
	}

	@PostMapping(path = "/create")
	public ResponseEntity createPost(
			@PathVariable String threadIdOrSlug,
			@RequestBody List<PostModel> posts) {

//		try {
			return new ResponseEntity<List<PostModel>>(
					threadDAO.createNewPosts(threadIdOrSlug, posts),
					HttpStatus.CREATED
			);
//		}
//		TODO Error 404,409 /forum/{slug_or_id}/create
//		catch (RuntimeException e) {
//			return new ResponseEntity<ErrorView>(
//					new ErrorView(e.getMessage()),
//					HttpStatus.BAD_REQUEST
//			);
//		}

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
}
