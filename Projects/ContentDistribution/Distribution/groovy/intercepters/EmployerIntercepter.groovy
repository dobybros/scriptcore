

package intercepters

import chat.errors.CoreException
import controllers.UserController
import script.groovy.servlets.RequestHolder

import javax.servlet.http.HttpSession

class EmployerIntercepter extends CommonIntercepter {
	@Override
	public void invoke(RequestHolder holder) throws CoreException{
		HttpSession session = holder.getRequest().getSession();
		String userId = session.getAttribute(UserController.SESSION_ATTRIBUTE_USERID);
		List<String> companyIds = session.getAttribute(UserController.SESSION_ATTRIBUTE_MYCOMPANYIDS)
		List<String> ownCompanyIds = session.getAttribute(UserController.SESSION_ATTRIBUTE_MYCOMPANYIDS)
		if(session == null || userId == null || companyIds == null || !companyIds.contains(userId) || ownCompanyIds == null || !ownCompanyIds.contains(userId)) {
			holder.getResponse().sendError(403, "Need authentication as an Employer");
		} else {
			super.proceed(holder);
		}
	}
}
