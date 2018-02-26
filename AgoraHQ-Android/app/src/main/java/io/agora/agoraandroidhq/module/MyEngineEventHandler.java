package io.agora.agoraandroidhq.module;

import android.content.Context;

import io.agora.agoraandroidhq.tools.GameControl;
import io.agora.rtc.IRtcEngineEventHandler;

public class MyEngineEventHandler {

    private String tag = "[MyEngineEventHandler]  ";
    public MyEngineEventHandler(Context ctx) {
        this.mContext = ctx;
    }

    private final Context mContext;

    private AGEventHandler agEventHandler;

    public void addEventHandler(AGEventHandler handler) {
        agEventHandler = handler;
    }

    public void removeEventHandler(AGEventHandler handler) {
        agEventHandler = null;
    }

    public final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {

            GameControl.logD(tag + "onFirstRemoteVideoDecoded ");
            if (agEventHandler != null) {
                agEventHandler.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
            }
        }

        @Override
        public void onUserMuteVideo(int uid, boolean muted) {
            super.onUserMuteVideo(uid, muted);
            GameControl.logD(tag + " onUserMuteVideo");
            if (agEventHandler != null) {
                agEventHandler.onUserMuteVideo(uid, muted);
            }
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            GameControl.logD(tag + "onUserOffline");
            // FIXME this callback may return times

            if (agEventHandler != null) {
                agEventHandler.onUserOffline(uid, reason);
            }
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            GameControl.logD(tag+"onJoinChannelSuccess");
            if (agEventHandler != null) {
                agEventHandler.onJoinChannelSuccess(channel, uid, elapsed);
            }
        }

        @Override
        public void onReceiveSEI(String info) {
            super.onReceiveSEI(info);
            GameControl.logD(tag + "onReceiveSEI");
            if (agEventHandler != null) {
                agEventHandler.onReceiveSEI(info);
            }
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            GameControl.logD(tag+"onUserJoined");
        }
    };
}
