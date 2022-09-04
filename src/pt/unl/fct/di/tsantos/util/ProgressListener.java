package pt.unl.fct.di.tsantos.util;

import java.util.EventListener;

public interface ProgressListener extends EventListener {
    public void reportProgress(int progress);
    public void reportCurrentTask(String taskDescription);
}
