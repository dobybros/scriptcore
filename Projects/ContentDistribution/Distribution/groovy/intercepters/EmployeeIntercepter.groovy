

package intercepters

import chat.errors.CoreException
import controllers.UserController
import script.groovy.servlets.RequestHolder

import javax.servlet.http.HttpSession

class EmployeeIntercepter extends CommonIntercepter {
	@Override
	public void invoke(RequestHolder holder) throws CoreException{
		HttpSession session = holder.getRequest().getSession();
		String userId = session.getAttribute(UserController.SESSION_ATTRIBUTE_USERID);
		Integer userType = session.getAttribute(UserController.SESSION_ATTRIBUTE_USERTYPE);
		List<String> companyIds = session.getAttribute(UserController.SESSION_ATTRIBUTE_MYCOMPANYIDS)
		if(session == null || userId == null || companyIds == null || companyIds.isEmpty()) {
			holder.getResponse().sendError(403, "Need authentication as an Employee");
		} else {
			super.proceed(holder);
		}
	}
}
