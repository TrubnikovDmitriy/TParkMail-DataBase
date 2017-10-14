package application.controllers;

import application.dao.ForumDAO;
import application.dao.PostDAO;
import application.dao.ThreadDAO;
import application.dao.UserDAO;
import application.models.*;
import application.views.ErrorView;
import application.views.PostFullView;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/post/{postId}")
public class PostController {

	private final PostDAO postDAO;
	private final UserDAO userDAO;
	private final ThreadDAO threadDAO;
	private final ForumDAO forumDAO;

	PostController(PostDAO postDAO,
	                      UserDAO userDAO,
	                      ThreadDAO threadDAO,
	                      ForumDAO forumDAO) {
		this.postDAO = postDAO;
		this.userDAO = userDAO;
		this.threadDAO = threadDAO;
		this.forumDAO = forumDAO;
	}

	@GetMapping(path = "/details")
	public ResponseEntity getPostInfo(
			@PathVariable Long postId,
			@RequestParam(name = "related", required = false, defaultValue = "") String related) {

		try {
			final PostModel postModel = postDAO.getPostById(postId);

			final UserModel userModel = related.contains("user") ?
					userDAO.getUserByNickname(postModel.getAuthor()) : null;

			final ForumModel forumModel = related.contains("forum") ?
					forumDAO.getForumBySlug(postModel.getForumSlug()) : null;

			final ThreadModel threadModel = related.contains("thread") ?
					threadDAO.getFullThreadById(postModel.getThreadId()) : null;

			return new ResponseEntity<PostFullView>(
					new PostFullView(postModel, userModel, forumModel, threadModel),
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
	public ResponseEntity updatePost(
			@PathVariable Long postId,
			@RequestBody PostUpdateModel postUpdate) {
		try {
			return new ResponseEntity<PostModel>(
					postDAO.updatePost(postId, postUpdate),
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
