package controllers

import chat.utils.MD5Util
import common.accounts.data.Account
import common.accounts.data.Company
import common.accounts.services.CompanyService
import common.controllers.GroovyServletEx
import groovy.json.JsonSlurper
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


import script.groovy.annotation.Bean
import script.groovy.object.GroovyObjectEx
import script.groovy.servlet.annotation.ControllerMapping
import script.groovy.servlet.annotation.RequestHeader
import script.groovy.servlet.annotation.RequestMapping
import script.groovy.servlets.GroovyServlet
import chat.errors.CoreException

import common.accounts.data.User
import common.accounts.services.UserService

import javax.servlet.http.HttpSession

//@ControllerMapping(interceptClass = "core/intercepters/InternalServerIntercepter.groovy")
@ControllerMapping(interceptClass = "intercepters/CommonIntercepter.groovy")
public class UserController extends GroovyServletEx {
	public static final String SESSION_ATTRIBUTE_USERID = "userId";
	public static final String SESSION_ATTRIBUTE_MYCOMPANYIDS = "myCompanyIds";
	public static final String SESSION_ATTRIBUTE_OWNCOMPANYIDS = "ownCompanyIds";
	public static final String SESSION_ATTRIBUTE_USERTYPE = "userType";

	public static final int ERRORCODE_REGISTER_ILLEGAL = 100;
	public static final int ERRORCODE_LOGIN_FAILED = 101;
	public static final int ERRORCODE_LOGIN_ILLEGAL = 102;

	private static final String TAG = "ADMIN";
	@Bean
	private GroovyObjectEx<UserService> userService;

	@Bean
	private GroovyObjectEx<CompanyService> companyService;

	@RequestMapping(uri = "rest/login", method = GroovyServlet.POST)
	public void login(
		HttpServletRequest request, 
		HttpServletResponse response, 
		@RequestHeader(key = "User-Agent") String ua) throws CoreException{
		def json = readJson(request);

		if(json["account"] == null || json["pwd"] == null)
			throw new CoreException(ERRORCODE_LOGIN_ILLEGAL, "Login parameters are illegal, " + json["account"]);

		String pwdMd5 = MD5Util.md5(Base64.decodeBase64(json["pwd"]))
		User user = userService.getObject().getUserFromLogin(json["account"], pwdMd5);
		if(user == null) {
			throw new CoreException(ERRORCODE_LOGIN_FAILED, "User doesn't be found by account " + json["account"] + " and password");
		}
        List<String> myCompanyIds = new ArrayList<>();
        List<String> ownCompanyIds = new ArrayList<>();
		List<Company> myCompanies = companyService.getObject().getMyCompanies(user.getId());
		if(myCompanies != null) {
			//Consider user is the employer of company who must be an employee of the company.
			for(Company company : myCompanies) {
				List<String> employerIds = company.getEmployerIds();
				if(employerIds != null && employerIds.contains(user.getId())) {
					ownCompanyIds.add(company.getId());
				}
				myCompanyIds.add(company.getId());
			}
		}

		HttpSession session = request.getSession(true);
		session.setAttribute(SESSION_ATTRIBUTE_USERID, user.getId());
		if(!myCompanyIds.isEmpty()) {
			session.setAttribute(SESSION_ATTRIBUTE_MYCOMPANYIDS, myCompanyIds);
		}
		if(!ownCompanyIds.isEmpty()) {
			session.setAttribute(SESSION_ATTRIBUTE_OWNCOMPANYIDS, ownCompanyIds);
		}
		session.setAttribute(SESSION_ATTRIBUTE_USERTYPE, user.getType());

		def obj = success();
        def login = [:];
        login.id = user.getId();
        login.name = user.getName();
        if(myCompanyIds != null)
            login.myCompanyIds = myCompanyIds;
        if(ownCompanyIds != null)
            login.ownCompanyIds = ownCompanyIds;
        login.type = user.getType();
        obj.login = login;
		respond(response, obj);
	}

	@RequestMapping(uri = "rest/register", method = GroovyServlet.POST)
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
		user.setType(User.TYPE_USER);
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
