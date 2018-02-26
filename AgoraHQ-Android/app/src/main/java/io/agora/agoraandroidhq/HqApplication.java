package io.agora.agoraandroidhq;

import android.app.Application;

import io.agora.agoraandroidhq.control.WorkerThread;
import io.agora.agoraandroidhq.tools.GameControl;

/**
 * Created by zhangtao on 2018/1/12.
 */

public class HqApplication extends Application {

    private String tag = "[HqApplication]  ";
    private WorkerThread mWorkerThread;
    public synchronized void initWorkerThread() {
        if (mWorkerThread == null) {
            mWorkerThread = new WorkerThread(getApplicationContext());
            mWorkerThread.start();
           // mWorkerThread.waitForReady();
            GameControl.logD(tag+" initWorkerThread");
        }
    }

    public synchronized WorkerThread getWorkerThread() {
        return mWorkerThread;
    }

    public synchronized void deInitWorkerThread() {
        GameControl.logD(tag+"deInitWorkerThread");
        mWorkerThread.exit();
        try {
            mWorkerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mWorkerThread = null;
    }
}
