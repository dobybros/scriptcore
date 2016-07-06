package intercepters

import script.groovy.servlets.RequestHolder
import script.groovy.servlets.RequestIntercepter
import chat.errors.CoreException

class CommonIntercepter extends RequestIntercepter {
	@Override
	public void invoke(RequestHolder holder) throws CoreException{
		super.proceed(holder);
	}
	
	@Override
	public void invokeError(Throwable t, RequestHolder holder) {
		if (t instanceof CoreException) {
			holder.getResponse().sendError(500, t.getMessage());
		} else {
			holder.getResponse().sendError(501, t.getMessage());
		}
	}
}
