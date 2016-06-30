package script.groovy.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import chat.errors.CoreException;

public class GroovyServletDispatcher extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7785123445517306608L;
	private GroovyServletManager groovyServletManager = GroovyServletManager.getInstance();
	
	private void servletDispatch(HttpServletRequest request, HttpServletResponse response) {
		try {
			RequestHolder holder = groovyServletManager.parseUri(request, response);
			if(holder == null) {
				try {
					response.sendError(404, "Url not found");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				holder.handleRequest();
			}
		} catch (CoreException e) {
			e.printStackTrace();
			try {
				response.sendError(500, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		servletDispatch(request, response);
	}
	
	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) {
		servletDispatch(request, response);
	}
	
	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) {
		servletDispatch(request, response);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		servletDispatch(request, response);
	}
	
	@Override
	public void doHead(HttpServletRequest request, HttpServletResponse response) {
		servletDispatch(request, response);
	}
	
	@Override
	public void doOptions(HttpServletRequest request, HttpServletResponse response) {
		servletDispatch(request, response);
	}
	
	@Override
	public void doTrace(HttpServletRequest request, HttpServletResponse response) {
		servletDispatch(request, response);
	}
}
