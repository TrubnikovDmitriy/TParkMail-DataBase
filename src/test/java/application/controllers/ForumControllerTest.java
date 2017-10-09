package application.controllers;

import application.models.UserModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import application.models.ForumModel;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@Transactional
public class ForumControllerTest { 

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper mapper;

//	@Test
//	public void testNoAdmin() throws Exception {
//		mockMvc.perform(post("/forum/create")
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(mapper.writeValueAsString(new ForumModel("test", "test", "test")))
//				)
//		.andExpect(status().isNotFound())
//		.andExpect(jsonPath("message").value("Can't find user with nickname test"));
//
//	}

	@Test
	public void testCreateNewUser() throws Exception {
		mockMvc.perform(post("/user/newuser/create")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(
						new UserModel(
								"email@mail.ru",
								"New User",
								"I am a new user!"
						)
				))
		).andExpect(status().isCreated());
	}

}
