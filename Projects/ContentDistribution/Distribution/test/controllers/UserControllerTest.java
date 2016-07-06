package controllers;

import base.ChatTestCase;
import org.junit.Test;

public class UserControllerTest extends ChatTestCase {

	@Test
	public void testLoginEmployee() throws Exception {
		login("aplomb", "123456");
	}

	@Test
	public void testLoginAdmin() throws Exception {
		login("admin", "123456");
	}

	@Test
	public void testLoginUser() throws Exception {
		login("lily", "123456");
	}
}
