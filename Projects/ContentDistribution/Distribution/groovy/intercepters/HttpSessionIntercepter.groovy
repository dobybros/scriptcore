

package intercepters

import chat.errors.CoreException
import controllers.UserController
import script.groovy.servlets.RequestHolder

import javax.servlet.http.HttpSession

class HttpSessionIntercepter extends CommonIntercepter {
	@Override
	public void invoke(RequestHolder holder) throws CoreException{
		HttpSession session = holder.getRequest().getSession();
		if(session == null || session.getAttribute(UserController.SESSION_ATTRIBUTE_USERID) == null) {
			holder.getResponse().sendError(403, "Need authentication");
		} else {
			super.proceed(holder);
		}
	}
}
