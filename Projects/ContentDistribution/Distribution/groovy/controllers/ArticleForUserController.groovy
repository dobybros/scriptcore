package controllers

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import script.file.FileAdapter
import script.groovy.annotation.Bean
import script.groovy.object.GroovyObjectEx
import script.groovy.servlet.annotation.ControllerMapping
import script.groovy.servlet.annotation.RequestHeader
import script.groovy.servlet.annotation.RequestMapping
import script.groovy.servlet.annotation.RequestParam
import script.groovy.servlets.GroovyServlet
import chat.errors.CoreException

import common.accounts.services.UserService
import common.distribution.Article
import common.distribution.ArticleService
import common.controllers.GroovyServletEx

@ControllerMapping(interceptClass = "intercepters/HttpSessionIntercepter.groovy")
public class ArticleForUserController extends GroovyServletEx {
	private static final String TAG = ArticleForUserController.class.getSimpleName();
	@Bean
	private GroovyObjectEx<UserService> userService;

	@Bean
	private GroovyObjectEx<ArticleService> articleService;

	@Bean(name = "localFileHandler")
	private GroovyObjectEx<FileAdapter> fileHandler;

	@RequestMapping(uri = "rest/articles", method = GroovyServlet.GET)
	public void getArticles(
		HttpServletRequest request, 
		HttpServletResponse response,
		@RequestParam(key = "c") String companyId,
		@RequestParam(key = "u") String authorUserId,
		@RequestHeader(key = "User-Agent", required = false) String ua) throws CoreException{
		List<Article> articles = articleService.getObject().queryArticles(companyId, authorUserId);
		def obj = success();
		obj.articles = articles;
		respond(response, obj);
	}

}
