package com.docker.script;

/**
 * 当handle为空的时候处理的接口
 * Created by liyazhou on 2017/5/8.
 */
public interface IRuntimeNullHandler {
    BaseRuntime getRuntime(String service);
}
