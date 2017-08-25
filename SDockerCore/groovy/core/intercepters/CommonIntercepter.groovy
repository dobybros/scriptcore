

package core.intercepters

import chat.errors.CoreException
import chat.logs.LoggerEx
import core.utils.CleanLinkedHashMap
import groovy.json.JsonOutput
import script.groovy.servlets.RequestHolder
import script.groovy.servlets.RequestIntercepter

import javax.servlet.http.HttpServletResponse

class CommonIntercepter extends RequestIntercepter {
	private static final String TAG = CommonIntercepter.class.getSimpleName();

	@Override
	public void invoke(RequestHolder holder) throws CoreException {
		super.proceed(holder);
	}
	
	@Override
	public void invokeError(Throwable t, RequestHolder holder) {
		t.printStackTrace();
		LoggerEx.error(TAG, t.getLocalizedMessage() + ": " + t.getStackTrace());
		LoggerEx.error(TAG, t.getMessage() + "：" + t.printStackTrace());
		LoggerEx.error(TAG, holder.getRequest().getRequestURI() + " occured error " + t.getMessage());
		if (t instanceof CoreException) {
//			holder.getResponse().sendError(500, ((CoreException)t).getCode() + ": " + t.getMessage());
			respond(holder.getResponse(), failed(((CoreException)t).getCode(), t.getMessage()));
		} else {
			holder.getResponse().sendError(500, t.getMessage());
		}
	}

	Map failed(int code, String description) {
		return [code : code, desp : description] as CleanLinkedHashMap;
	}

	void respond(HttpServletResponse response, Object map) {
//        JsonBuilder builder = new JsonBuilder(map);
//        String returnStr = builder.toString();
		String returnStr = JsonOutput.toJson(map);

		response.setContentType("application/json");
//        LoggerEx.debug(this.getClass().getSimpleName(), "respond " + returnStr);
		response.getOutputStream().write(returnStr.getBytes("utf-8"));
	}
}
