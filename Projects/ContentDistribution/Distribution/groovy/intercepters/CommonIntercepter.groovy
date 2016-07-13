

package intercepters

import chat.logs.LoggerEx
import script.groovy.servlets.RequestHolder
import script.groovy.servlets.RequestIntercepter
import chat.errors.CoreException

class CommonIntercepter extends RequestIntercepter {
	private static final String TAG = CommonIntercepter.class.getSimpleName();

	@Override
	public void invoke(RequestHolder holder) throws CoreException {
		super.proceed(holder);
	}
	
	@Override
	public void invokeError(Throwable t, RequestHolder holder) {
		t.printStackTrace();
		LoggerEx.error(TAG, holder.getRequest().getRequestURI() + " occured error " + t.getMessage());
		if (t instanceof CoreException) {
			holder.getResponse().sendError(500, ((CoreException)t).getCode() + ": " + t.getMessage());
		} else {
			holder.getResponse().sendError(501, t.getMessage());
		}
	}
}
