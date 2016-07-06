package common.controllers;


import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import script.groovy.runtime.GroovyRuntime
import script.groovy.servlet.annotation.ControllerMapping
import script.groovy.servlet.annotation.RequestMapping
import script.groovy.servlets.GroovyServlet
import chat.errors.CoreException

@ControllerMapping(interceptClass = "intercepters/CommonIntercepter.groovy")
public class ScriptController extends GroovyServletEx{
	private static final String TAG = ScriptController.class.getSimpleName();
	
	@RequestMapping(uri = "rest/reload", method = GroovyServlet.GET)
	public void reload(HttpServletRequest request, 
		HttpServletResponse response) throws CoreException{
			GroovyRuntime.getInstance().redeploy();
		response.getOutputStream().write("Done".getBytes());
	}

	@RequestMapping(uri = "gc", method = GroovyServlet.GET)
	public void gc(HttpServletRequest request,
							  HttpServletResponse response) throws CoreException{
		System.gc();
		response.getOutputStream().write(("used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + "M").getBytes());
	}

}
