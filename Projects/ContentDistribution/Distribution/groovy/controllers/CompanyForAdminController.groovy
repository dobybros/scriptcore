package controllers

import chat.errors.CoreException
import common.accounts.data.Company
import common.accounts.services.CompanyService
import common.accounts.services.UserService
import common.controllers.GroovyServletEx
import script.groovy.annotation.Bean
import script.groovy.object.GroovyObjectEx
import script.groovy.servlet.annotation.ControllerMapping
import script.groovy.servlet.annotation.PathVariable
import script.groovy.servlet.annotation.RequestHeader
import script.groovy.servlet.annotation.RequestMapping
import script.groovy.servlets.GroovyServlet

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

//@ControllerMapping(interceptClass = "core/intercepters/InternalServerIntercepter.groovy")
@ControllerMapping(interceptClass = "intercepters/AdminIntercepter.groovy")
public class CompanyForAdminController extends GroovyServletEx {
	public static final int ERRORCODE_COMPANY_ILLEGAL = 300;

	private static final String TAG = CompanyForAdminController.class.getSimpleName();
	@Bean
	private GroovyObjectEx<UserService> userService;

	@Bean
	private GroovyObjectEx<CompanyService> companyService;

	@RequestMapping(uri = "rest/company", method = GroovyServlet.POST)
	public void newCompany(
		HttpServletRequest request, 
		HttpServletResponse response, 
		@RequestHeader(key = "User-Agent") String ua) throws CoreException{
		def json = readJson(request);

		if(json["name"] == null)
			throw new CoreException(ERRORCODE_COMPANY_ILLEGAL, "New　company parameters are illegal, " + json);
		List<String> employeeIds = json["employeeIds"];
		List<String> employerIds = json["employerIds"];
		Company company = new Company();
		company.setEmployeeIds(employeeIds);
		company.setEmployerIds(employerIds);
		company.setName(json["name"]);
		companyService.getObject().addCompany(company);

		def obj = success();
		respond(response, obj);
	}

	@RequestMapping(uri = "rest/company/{companyId}", method = GroovyServlet.DELETE)
	public void deleteCompany(
			HttpServletRequest request, 
			HttpServletResponse response,
			@PathVariable(key = "companyId") String companyId,
			@RequestHeader(key = "User-Agent") String ua) throws CoreException{

		companyService.getObject().deleteCompany(companyId);

		def obj = success();
		respond(response, obj);
	}

	@RequestMapping(uri = "rest/company/{companyId}", method = GroovyServlet.GET)
	public void getCompany(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable(key = "companyId") String companyId,
			@RequestHeader(key = "User-Agent") String ua) throws CoreException{

		Company company = companyService.getObject().getCompany(companyId);

		def obj = success();
		obj.company = company;
		respond(response, obj);
	}

	@RequestMapping(uri = "rest/companies", method = GroovyServlet.GET)
	public void getCompanies(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestHeader(key = "User-Agent") String ua) throws CoreException{

		List<Company> companies = companyService.getObject().getAllCompanies();

		def obj = success();
		obj.companies = companies;
		respond(response, obj);
	}
}
