

package intercepters

import chat.errors.CoreException
import common.accounts.data.User
import controllers.UserController
import script.groovy.servlets.RequestHolder

import javax.servlet.http.HttpSession

class AdminIntercepter extends CommonIntercepter {
	@Override
	public void invoke(RequestHolder holder) throws CoreException{
		HttpSession session = holder.getRequest().getSession();
		String userId = session.getAttribute(UserController.SESSION_ATTRIBUTE_USERID);
		Integer userType = session.getAttribute(UserController.SESSION_ATTRIBUTE_USERTYPE);
		if(session == null || userId == null || userType == null || userType != User.TYPE_ADMIN) {
			holder.getResponse().sendError(403, "Need authentication as an Admin");
		} else {
			super.proceed(holder);
		}
	}
}
