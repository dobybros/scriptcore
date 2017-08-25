package com.docker.script;

import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;

/**
 * Created by liyazhou on 2017/5/8.
 */
public class RuntimeNullHandler implements IRuntimeNullHandler {
    public static final String SAAS_APP_SELLER = "seller";
    public static final String SAAS_APP_BUYER = "buyer";
    public static final String SAAS_NOVOSHOPS_SELLER = "saasnovoshopsseller";
    public static final String SAAS_NOVOSHOPS_BUYER = "saasnovoshopsbuyer";
    @Resource
    private ScriptManager scriptManager;

    @Override
    public BaseRuntime getRuntime(String service) {
//        返回来调用判断
        if (StringUtils.isEmpty(service) || service.equals(SAAS_NOVOSHOPS_SELLER) || service.equals(SAAS_NOVOSHOPS_BUYER)) {
            return null;
        }
        if (service.endsWith(SAAS_APP_SELLER)) {
            return scriptManager.getBaseRuntime(SAAS_NOVOSHOPS_SELLER);
        } else if (service.endsWith(SAAS_APP_BUYER)) {
            return scriptManager.getBaseRuntime(SAAS_NOVOSHOPS_BUYER);
        }
        return null;
    }
}
