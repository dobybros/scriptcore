package com.docker.script.callers;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.RunnableEx;
import com.docker.script.BaseRuntime;
import script.groovy.runtime.GroovyRuntime;

import java.lang.reflect.InvocationTargetException;

public abstract class RunnableCaller implements Runnable {
    private String TAG = RunnableCaller.class.getSimpleName();

    public abstract void call();

    @Override
    public final void run() {
        CallerUtils.callMethod(this, "call");
    }
}
