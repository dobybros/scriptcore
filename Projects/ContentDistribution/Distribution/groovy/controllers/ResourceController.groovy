package controllers

import chat.errors.CoreException
import common.controllers.GroovyServletEx
import common.utils.Utils
import script.file.FileAdapter
import script.groovy.annotation.Bean
import script.groovy.object.GroovyObjectEx
import script.groovy.servlet.annotation.*
import script.groovy.servlets.GroovyServlet

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ControllerMapping(interceptClass = "intercepters/CommonIntercepter.groovy")
public class ResourceController extends GroovyServletEx {
	private static final String TAG = ResourceController.class.getSimpleName();

	@Bean(name = "localFileHandler")
	private GroovyObjectEx<FileAdapter> fileHandler;

	@RequestMapping(uri = "rest/resource/{resourceId}/{fileName}", method = GroovyServlet.GET)
	public void getResources(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable(key = "resourceId") String resourceId,
			@PathVariable(key = "fileName") String fileName,
			@RequestHeader(key = "User-Agent", required = false) String ua) throws CoreException{
		FileAdapter.PathEx path = new FileAdapter.PathEx(Utils.getDocumentPath(resourceId, fileName),
				resourceId, null);
		FileAdapter.FileEntity entity = fileHandler.getObject().getFileEntity(path);
		response.setContentLength((int)entity.getLength());
		fileHandler.getObject().readFile(path, response.getOutputStream());
	}

}
