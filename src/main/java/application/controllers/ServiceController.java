package application.controllers;

import application.dao.ServiceDAO;
import application.views.StatusView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/service")
public class ServiceController {

	private final ServiceDAO serviceDAO;

	public ServiceController(ServiceDAO serviceDAO) {
		this.serviceDAO = serviceDAO;
	}

	@GetMapping(path = "/status")
	public ResponseEntity getStatus() {
		return new ResponseEntity< StatusView>(
				new StatusView(
						serviceDAO.getNumberUsers(),
						serviceDAO.getNumberForums(),
						serviceDAO.getNumberThreads(),
						serviceDAO.getNumberPosts()
				),
				HttpStatus.OK
		);
	}

	@PostMapping(path = "/clear")
	public ResponseEntity clearDataBase() {
		serviceDAO.truncate();
		return ResponseEntity.ok("Очистка базы успешно завершена");
	}
}
