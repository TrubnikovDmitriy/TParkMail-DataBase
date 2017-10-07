package application.controllers;

import application.dao.ForumDAO;
import application.models.ForumModel;
import application.views.ErrorView;
import application.views.ForumView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/forum")
public class ForumController{

	private ForumDAO forumDAO;

	@Autowired
	ForumController(JdbcTemplate jdbcTemlate) {
		forumDAO = new ForumDAO(jdbcTemlate);
	}

	@PostMapping(path = "/create")
	public ResponseEntity createForum(@RequestBody ForumModel forumModel) {

		try {
			forumDAO.createNew(forumModel);
			return new ResponseEntity<ForumView>(
					new ForumView(forumModel),
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
			return new ResponseEntity<ForumView>(
					new ForumView(forumDAO.getForumBySlug(forumModel.getSlug())),
					HttpStatus.CONFLICT
			);
		}
	}

	@GetMapping(path = "/{forumSlug}/details")
	public ResponseEntity getForum(@PathVariable String forumSlug) {
		try {
			return new ResponseEntity<ForumView>(
					new ForumView(forumDAO.getForumBySlug(forumSlug)),
					HttpStatus.OK
			);
		}
		catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<ErrorView>(
					new ErrorView("Can't find forum with slug " + forumSlug),
					HttpStatus.NOT_FOUND
			);
		}
	}

}
