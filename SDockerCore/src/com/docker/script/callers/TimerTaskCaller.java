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
        BaseRuntime baseRuntime = (BaseRuntime) GroovyRuntime.getCurrentGroovyRuntime(this.getClass().getClassLoader());
        if(baseRuntime != null) {
            try {
                baseRuntime.executeBeanMethod(this, "call");
            } catch (CoreException e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "executeBeanMethod(CoreException) " + this + " failed, " + e.getMessage());
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "executeBeanMethod(InvocationTargetException) " + this + " failed, " + e.getMessage());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "executeBeanMethod(IllegalAccessException) " + this + " failed, " + e.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
                LoggerEx.error(TAG, "executeBeanMethod(Throwable) " + this + " failed, " + t.getMessage());
            }
        }
    }
}