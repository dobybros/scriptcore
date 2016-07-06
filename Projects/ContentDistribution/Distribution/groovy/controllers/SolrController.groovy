package controllers

import chat.errors.CoreException
import common.controllers.GroovyServletEx
import connectors.solr.SolrIndexService
import org.apache.solr.common.SolrDocumentList
import script.file.FileAdapter
import script.groovy.annotation.Bean
import script.groovy.object.GroovyObjectEx
import script.groovy.servlet.annotation.ControllerMapping
import script.groovy.servlet.annotation.RequestHeader
import script.groovy.servlet.annotation.RequestMapping
import script.groovy.servlet.annotation.RequestParam
import script.groovy.servlets.GroovyServlet

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ControllerMapping(interceptClass = "intercepters/CommonIntercepter.groovy")
public class SolrController extends GroovyServletEx {
	private static final String TAG = SolrController.class.getSimpleName();

	@Bean(name = "localFileHandler")
	private GroovyObjectEx<FileAdapter> fileHandler;

	@RequestMapping(uri = "rest/search", method = GroovyServlet.GET)
	public void search(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(key = "q") String query,
			@RequestHeader(key = "User-Agent", required = false) String ua) throws CoreException{
		SolrDocumentList docs = SolrIndexService.getInstance().query("content:" + query, "articles");
//		for(SolrDocument doc : docs) {
//
//		}
		def obj = success();
		obj.docs = docs;
		respond(response, obj);
	}

}
