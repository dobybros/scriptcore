package core.intercepters

import script.groovy.servlets.RequestHolder
import chat.errors.CoreException
import core.common.CommonConstants


class InternalServerIntercepter extends CommonIntercepter {
	public static final String HEADER_KEY = "key";
	@Override
	public void invoke(RequestHolder holder) throws CoreException {
		String key = holder.getRequest().getHeader(HEADER_KEY);
		if(key == null)
			key = holder.getRequest().getParameter(HEADER_KEY);
		if(key == null || !key.equals(CommonConstants.INTERNAL_KEY)) {
			throw new CoreException(CommonConstants.ERROR_FORBIDDEN, "key " + key + " is forbidden to visit this url " + holder.getRequest().getRequestURL());
		}
		
		super.invoke(holder);
	}
}
