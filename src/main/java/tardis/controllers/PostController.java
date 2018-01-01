package tardis.controllers;

import tardis.models.PostUpdateModel;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tardis.dao.ForumDAO;
import tardis.dao.PostDAO;
import tardis.dao.ThreadDAO;
import tardis.dao.UserDAO;
import tardis.models.ForumModel;
import tardis.models.PostModel;
import tardis.models.ThreadModel;
import tardis.models.UserModel;
import tardis.views.ErrorView;
import tardis.views.PostFullView;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api/post/{postID}")
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
			@PathVariable Integer postID,
			@RequestParam(name = "related", required = false, defaultValue = "") String related) {

		try {
			final PostModel postModel = postDAO.getPostByIDforDetails(postID);

			final UserModel userModel = related.contains("user") ?
					userDAO.getUserByID(postModel.getAuthorID()) : null;

			final ForumModel forumModel = related.contains("forum") ?
					forumDAO.getForumBySlug(postModel.getForumSlug()) : null;

			final ThreadModel threadModel = related.contains("thread") ?
					threadDAO.getThreadByIDforDetails(
							postModel.getThreadID(), postModel.getForumSlug()) : null;

			return new ResponseEntity<>(
					new PostFullView(postModel, userModel, forumModel, threadModel),
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
	public ResponseEntity updatePost(
			@PathVariable Integer postID,
			@RequestBody PostUpdateModel updatePost) {
		try {
			return new ResponseEntity<>(
					postDAO.updatePost(postID, updatePost),
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
}
