package application.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/thread")
public class ThreadController{

//	@Autowired
//	ThreadController(JdbcTemplate jdbcTeml) {
//		super(jdbcTeml);
//	}
//SELECT u.nickname, th_x.created, f.slug, th.thread_id, th_x.message, th_x.slug, th_x.title FROM threads th JOIN forums f ON f.slug=? AND f.forum_id=th.forum_id JOIN threads_extra th_x ON th_x.created >= ? AND th_x.thread_id=th.thread_id NATURAL JOIN users u ORDER BY th_x.created ? LIMIT ?]

}
