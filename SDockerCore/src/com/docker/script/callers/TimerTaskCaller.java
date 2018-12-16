package com.docker.script.callers;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.TimerTaskEx;
import com.docker.script.BaseRuntime;
import script.groovy.runtime.GroovyRuntime;

import java.lang.reflect.InvocationTargetException;

public abstract class TimerTaskCaller extends TimerTaskEx{
    private static final String TAG = TimerTaskCaller.class.getSimpleName();

    public abstract void call();
    @Override
    public final void execute() {
        CallerUtils.callMethod(this, "call");
    }
}
