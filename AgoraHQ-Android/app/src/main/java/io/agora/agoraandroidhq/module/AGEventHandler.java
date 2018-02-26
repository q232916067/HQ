package io.agora.agoraandroidhq.module;

import io.agora.rtc.IRtcEngineEventHandler;

/**
 * Created by zhangtao on 2018/2/7.
 */

public interface AGEventHandler {
    void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed);
    void onJoinChannelSuccess(String channel, int uid, int elapsed);
    void onReceiveSEI(String info);
    void onUserOffline(int uid, int reason);
    void onUserMuteVideo(final int uid, final boolean muted);
}
