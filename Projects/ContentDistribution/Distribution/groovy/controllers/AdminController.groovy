package controllers

import chat.errors.CoreException
import chat.utils.MD5Util
import common.accounts.services.CompanyService
import common.accounts.data.Account
import common.accounts.data.User
import common.accounts.services.UserService
import common.controllers.GroovyServletEx
import groovy.json.JsonSlurper
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils
import script.groovy.annotation.Bean
import script.groovy.object.GroovyObjectEx
import script.groovy.servlet.annotation.ControllerMapping
import script.groovy.servlet.annotation.RequestHeader
import script.groovy.servlet.annotation.RequestMapping
import script.groovy.servlets.GroovyServlet

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/*
 * For create the Admin.
 * This Controller should never expose to public.
 */
@ControllerMapping(interceptClass = "intercepters/CommonIntercepter.groovy")
public class AdminController extends GroovyServletEx {

	public static final int ERRORCODE_REGISTER_ILLEGAL = 100;

	private static final String TAG = "ADMIN";
	@Bean
	private GroovyObjectEx<UserService> userService;

	@Bean
	private GroovyObjectEx<CompanyService> companyService;

	@RequestMapping(uri = "rest/admin/register", method = GroovyServlet.POST)
	public void register(
			HttpServletRequest request, 
			HttpServletResponse response, 
			@RequestHeader(key = "Article-Agent") String ua) throws CoreException{
		String requestStr = IOUtils.toString(request.getInputStream(), "utf8");
		def slurper = new JsonSlurper()
		def json = slurper.parseText(requestStr);
		if(json["account"] == null || json["name"] == null || json["pwd"] == null)
			throw new CoreException(ERRORCODE_REGISTER_ILLEGAL, "Regiser information is illegal, " + requestStr);

		User user = new User();
		user.setName(json["name"]);
		user.setType(User.TYPE_ADMIN);
//		user.setType(User.TYPE_USER);
		List<Account> accounts = [new Account(json["account"])];
		user.setAccounts(accounts);
		user.setLoginAccounts([json["account"]]);
		user.setPassword(MD5Util.md5(Base64.decodeBase64(json["pwd"])));

		userService.getObject().addUser(user);
		def obj = success();
//		obj.user = user;
		respond(response, obj);
	}

}
