package io.agora.agoraandroidhq.control;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;

import io.agora.agoraandroidhq.module.MyEngineEventHandler;
import io.agora.agoraandroidhq.tools.GameControl;
import io.agora.agoraandroidhq.view.GameActivity;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

/**
 * Created by zhangtao on 2018/2/7.
 */

public class WorkerThread extends Thread {


    private String tag = "[WorkerThread]   ";
    private final Context mContext;
    private static final int ACTION_WORKER_THREAD_QUIT = 0X1010; // quit this thread
    private static final int ACTION_WORKER_JOIN_CHANNEL = 0X2010;
    private static final int ACTION_WORKER_LEAVE_CHANNEL = 0X2011;
    private static final int ACTION_WORKER_CONFIG_ENGINE = 0X2012;
    private static final int ACTION_WORKER_PREVIEW = 0X2014;
    private static final int ACTION_WORKER_DESTORY_ENGINE = 0x2017;

    private static final class WorkerThreadHandler extends Handler {

        private WorkerThread mWorkerThread;
        WorkerThreadHandler(WorkerThread thread) {
            this.mWorkerThread = thread;
        }
        public void release() {
            mWorkerThread = null;
        }

        @Override
        public void handleMessage(Message msg) {
            if (this.mWorkerThread == null) {
                return;
            }

            switch (msg.what) {
                case ACTION_WORKER_THREAD_QUIT:
                    mWorkerThread.exit();
                    break;
                case ACTION_WORKER_JOIN_CHANNEL:
                    String[] data = (String[]) msg.obj;
                    mWorkerThread.joinChannel();
                    break;
                case ACTION_WORKER_LEAVE_CHANNEL:
                    mWorkerThread.leaveChannel();
                    break;
                case ACTION_WORKER_DESTORY_ENGINE:
                    mWorkerThread.destoryEngine();
                    break;
            }
        }
    }

    private WorkerThreadHandler mWorkerHandler;
    private boolean mReady;
    public final void waitForReady() {
        while (!mReady) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        GameControl.logD(tag+ "  run");
        Looper.prepare();
        mWorkerHandler = new WorkerThreadHandler(this);
        ensureRtcEngineReadyLock();
        mReady = true;
        // enter thread looper
        Looper.loop();
    }

    private RtcEngine mRtcEngine;
    public final void joinChannel() {
        GameControl.logD(tag+"joinChannel");
        if (Thread.currentThread() != this) {
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_JOIN_CHANNEL;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        ensureRtcEngineReadyLock();
        mRtcEngine.setClientRole(io.agora.rtc.Constants.CLIENT_ROLE_AUDIENCE, null);
        // rtcEngine.setParameters("{\"rtc.hq_mode\": {\"hq\": true, \"broadcaster\":false, \"bitrate\":0}}");
        mRtcEngine.enableVideo();
        mRtcEngine.muteLocalVideoStream(true);
        mRtcEngine.setVideoProfile(io.agora.rtc.Constants.VIDEO_PROFILE_360P, false);
        mRtcEngine.joinChannel(null, GameControl.currentUser.channelName, "Extra Optional Data", Integer.parseInt(GameControl.currentUser.account)); // if you do not specify the uid, we will generate the uid for you
    }

    public final void leaveChannel() {
        GameControl.logD(tag+"leaveChannel");
        if (Thread.currentThread() != this) {
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_LEAVE_CHANNEL;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }
    }

    private final MyEngineEventHandler mEngineEventHandler;

    private RtcEngine ensureRtcEngineReadyLock() {
        if (mRtcEngine == null) {
            String appId = io.agora.agoraandroidhq.tools.Constants.AGORA_APP_ID;
            if (TextUtils.isEmpty(appId)) {
                throw new RuntimeException("NEED TO use your App ID, get your own ID at https://dashboard.agora.io/");
            }
            try {
                mRtcEngine = RtcEngine.create(mContext, appId, mEngineEventHandler.mRtcEventHandler);
            } catch (Exception e) {
                throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
            }
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        }
        return mRtcEngine;
    }

    public MyEngineEventHandler eventHandler() {
        return mEngineEventHandler;
    }

    public RtcEngine getRtcEngine() {
        return mRtcEngine;
    }

    public void destoryEngine() {
        GameControl.logD(tag+"destoryEngine");
        if (Thread.currentThread() != this) {
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_LEAVE_CHANNEL;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        if (mRtcEngine != null) {
            RtcEngine.destroy();
        }
    }

    /**
     * call this method to exit
     * should ONLY call this method when this thread is running
     */
    public final void exit() {
        if (Thread.currentThread() != this) {
            mWorkerHandler.sendEmptyMessage(ACTION_WORKER_THREAD_QUIT);
            return;
        }
        mReady = false;
        // TODO should remove all pending(read) messages
        // exit thread looper
        Looper.myLooper().quit();
        mWorkerHandler.release();

    }

    public WorkerThread(Context context) {
        this.mContext = context;
        this.mEngineEventHandler = new MyEngineEventHandler(mContext);
    }
}
