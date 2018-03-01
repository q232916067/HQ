package io.agora.agoraandroidhq.view;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.GridLayoutAnimationController;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agora.agoraandroidhq.HqApplication;
import io.agora.agoraandroidhq.R;
import io.agora.agoraandroidhq.control.AgoraSignal;
import io.agora.agoraandroidhq.control.JsonToString;
import io.agora.agoraandroidhq.control.WorkerThread;
import io.agora.agoraandroidhq.module.AGEventHandler;
import io.agora.agoraandroidhq.module.Question;
import io.agora.agoraandroidhq.module.Result;
import io.agora.agoraandroidhq.module.User;
import io.agora.agoraandroidhq.tools.Constants;
import io.agora.agoraandroidhq.tools.GameControl;
import io.agora.agoraandroidhq.tools.HttpUrlUtils;
import io.agora.agoraandroidhq.tools.MessageListDecoration;
import io.agora.agoraandroidhq.tools.MessageRecyclerViewAdapter;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;


/**
 * Created by zhangtao on 2018/1/12.
 */

public class GameActivity extends BaseActivity implements AGEventHandler {

    private boolean isFirst = true;
    private String tag = "[GameActivity]   ";
    private RecyclerView recyclerView;
    private MessageRecyclerViewAdapter messageRecyclerViewAdapter;
    private LinearLayout messageLinearLayou;
    private EditText input_editor;
    private TextView sendButton;
    private ImageView imageViewBack;
    private ImageView sendMessageImage;
    private FrameLayout game_view_layout;
    // private Button gameGangUpButton;
    private boolean wheatherHasFocus = false;
    private ExecutorService executorService;
    private WorkerThread workerThread;
    private RtcEngine rtcEngine;
    private boolean wheatherChangeGameReuslt = true;
    private boolean isFirstTimeSEI = true;
    private boolean questionFlag = true;
    private ArrayList answerList;
    private AgoraSignal agoraSignal;
    private int questionTime;
    private ImageView gameResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paly_game);
        initUIandEvent();
        setUiListener();
    }

    @Override
    protected void initUIandEvent() {
        GameControl.logD(tag + " initUIandEvent");

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        imageViewBack = findViewById(R.id.back_image);
        game_view_layout = findViewById(R.id.game_view_layout);
        messageLinearLayou = findViewById(R.id.sendMessage_layout);
        input_editor = findViewById(R.id.input_editor);
        sendButton = findViewById(R.id.sendMessage_text);
        sendMessageImage = findViewById(R.id.image_sendMessage);
        recyclerView = findViewById(R.id.messageRecycleView);
        messageRecyclerViewAdapter = new MessageRecyclerViewAdapter(GameActivity.this);
        recyclerView.setAdapter(messageRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.addItemDecoration(new MessageListDecoration());

        if (messageRecyclerViewAdapter.getItemCount() > 0) {
            recyclerView.smoothScrollToPosition(messageRecyclerViewAdapter.getItemCount() - 1);
        }
        // AgoraLinkToCloud.addEventHandler(handler);
        gameResult = findViewById(R.id.game_result);
    }

    @Override
    protected void deInitUIandEvent() {
        GameControl.logD(tag + "deInitUIandEvent");
        GameControl.controlCheckThread = false;
        GameControl.currentQuestion = null;
        questionFlag = false;
        if (agoraSignal != null) {
            agoraSignal.removeEnventHandler();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    agoraSignal.onLogoutSDKClick();
                }
            });
        }
        recyclerView = null;
        messageRecyclerViewAdapter = null;
        checkBox_item.clear();
        checkBox_item = null;
        board.clear();
        board = null;
        questionTimeHandler = null;
        executorService.shutdown();
        workerThread.leaveChannel();
        workerThread.eventHandler().removeEventHandler(this);
        System.gc();
    }

    @Override
    protected void setUiListener() {
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                GameActivity.this.finish();
            }
        });
        //  gameGangUpButton = findViewById(R.id.game_gang_up_btn);
        sendMessageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageLinearLayou.setVisibility(View.VISIBLE);
                input_editor.requestFocus();
            }
        });

        input_editor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sendButton.setEnabled(!s.toString().isEmpty());
                sendButton.setEnabled(true);
                sendButton.setClickable(true);
                input_editor.removeTextChangedListener(this);
            }
        });

        input_editor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    wheatherHasFocus = true;
                } else {

                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = input_editor.getText().toString();
                input_editor.clearFocus();
                input_editor.setText("");
                String messageString = null;
                try {
                    messageString = JsonToString.sendMessageString(message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String selfMessage = null;
                try {
                    selfMessage = JsonToString.sendMessageSelf(message);
                } catch (JSONException e) {

                }
                GameControl.logD(tag + "messageSend  =  " + messageString);

                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(GameActivity.this, "content can not be null", Toast.LENGTH_SHORT).show();
                } else {
                    /*TextMessage content = TextMessage.obtain(messageString);
                    AgoraLinkToCloud.sendMessage(content, Constants.questionRoom);*/
                    if (agoraSignal != null) {
                        agoraSignal.sendChannelMessage(messageString, selfMessage);
                    }
                }
            }
        });
        game_view_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageLinearLayou.setVisibility(View.GONE);
            }
        });
    }

    private User getUser() {
        User currentUser = new User();
        currentUser.name = GameControl.currentUserName;
        currentUser.drawable = GameControl.currentUserHeadImage;
        String channelName = getChannelName();
        currentUser.setChannelName(channelName);
        GameControl.currentUser = currentUser;
        return currentUser;
    }

    private void init() throws Exception {
        loginAgoraSignal();
        GameControl.logD(tag + "init");
        initQuestionLayout();
        GameControl.controlCheckThread = true;
        getUser();
        checkSelfPermissions();
        executorService = createExcetorService();
    }

    private ExecutorService createExcetorService() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        return executorService;
    }

    private String getAccount() {
        String uid = Constants.UID.toString() + new Random().nextInt(100000);
        GameControl.logD(tag + "getAccount = " + uid);
        return uid;
    }

    private String getChannelName() {
        Intent intent = getIntent();
        String channelName = intent.getStringExtra("ChannelName");
        GameControl.logD(tag + "getChannelName  = " + channelName);
        return channelName;
    }

    private void loginAgoraSignal() {
        agoraSignal = AgoraSignal.newInstance(GameActivity.this, Constants.AGORA_APP_ID, getAccount(), getChannelName());
        agoraSignal.addEventHandler(agoraHandler);
        agoraSignal.login();
    }

    private Handler agoraHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.LOGIN_AGORA_SIGNAL_FAIL:
                    GameControl.logD(tag + " LOGIN_AGORA_SIGNAL_FAIL");
                    toastHelper(getString(R.string.login_agora_signal_fail));
                    //finish();
                    agoraSignal.login();
                    break;
                case Constants.LOGIN_AGORA_SIGNAL_SUCCESS:
                    GameControl.logD(tag + "LOGIN_AGORA_SIGNAL_SUCCESS");
                    toastHelper(getString(R.string.login_agora_signal_success));
                    break;

                case Constants.AGORA_SIGNAL_RECEIVE:
                    String mess = (String) msg.obj;
                    Object jsonObject = null;
                    try {
                        jsonObject = JsonToString.jsonToString(mess);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    GameControl.logD(tag + " AGORA_SIGNAL_RECEIVE   =  " + mess + "  strinType = " + JsonToString.strinType);
                    switch (JsonToString.strinType) {

                        case "chat":
                            io.agora.agoraandroidhq.module.Message message = (io.agora.agoraandroidhq.module.Message) jsonObject;
                            messageRecyclerViewAdapter.updateData(message);
                            recyclerView.smoothScrollToPosition(messageRecyclerViewAdapter.getItemCount() - 1);
                            break;

                        case "result":
                            Result result = (Result) jsonObject;
                            int correct = result.correct;
                            int res = result.result;
                            int chooseResult = -1;
                            GameControl.logD(tag + " clientWheatherCanPlay = " + GameControl.clientWheatherCanPlay + "  serverWheatherCanPlay = " + GameControl.serverWheatherCanPlay);
                            if (GameControl.clientWheatherCanPlay && GameControl.serverWheatherCanPlay) {
                                chooseResult = GameControl.result;
                            } else {
                                chooseResult = -1;
                            }
                            if (GameControl.currentQuestion != null) {
                                GameControl.logD(tag + " GameControl.currentQues = " + GameControl.currentQuestion.toString());
                                GameControl.logD(tag + " result  showHighLightCheckBox  =  " + checkBox_item.size());
                                   /* setCheckBoxBackHighLight(res);4*/
                                GameControl.logD(tag + " res  = " + res + " chooseResult  = " + chooseResult);

                                if ((res != chooseResult)) {
                                    int answer = res + 1;
                                    time_reduce.setText(getString(R.string.answer_error_message));

                                    time_reduce.setTextColor(Color.RED);
                                    time_reduce.setVisibility(View.VISIBLE);
                                    //game_title.setVisibility(View.INVISIBLE);
                                    GameControl.clientWheatherCanPlay = false;

                                    if (questionTime != 0 && (questionTime != GameControl.timeOut)) {
                                        questionTime = 0;
                                    }

                                    if (wheatherChangeGameReuslt) {

                                        GameControl.gameResult = false;
                                        wheatherChangeGameReuslt = false;
                                    }

                                } else {
                                    time_reduce.setText(R.string.answer_correct_message);
                                    time_reduce.setTextColor(Color.GREEN);
                                    time_reduce.setVisibility(View.VISIBLE);
                                    //game_title.setVisibility(View.INVISIBLE);
                                    GameControl.clientWheatherCanPlay = true;

                                    if (questionTime != 0 && (questionTime != GameControl.timeOut)) {
                                        questionTime = 0;
                                    }
                                }
                                questionTimeHandler.sendEmptyMessageDelayed(1, 5000);
                                //time_reduce.setVisibility(View.INVISIBLE);
                                game_layout.setVisibility(View.VISIBLE);
                                submit_btn.setVisibility(View.GONE);
                                getCorrectCheckBox(res);
                                if ((GameControl.currentQuestion.getId() + 1) == GameControl.total) {
                                    questionTimeHandler.sendEmptyMessageDelayed(2, 6000);
                                }
                            }

                            GameControl.result = -1;
                            break;
                        case "quiz":
/*
                            try {
                                checkWheatherCanPlay();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }*/
                            Question question = (Question) jsonObject;
                            ;

                            /*if (GameControl.clientWheatherCanPlay && GameControl.serverWheatherCanPlay) {

                                showquestionView(question);

                            }*/
                            if (question.getId() == 1) {
                                GameControl.gameResult = true;
                                wheatherChangeGameReuslt = true;
                            }
                            GameControl.logD(tag + " save Question :  id = " + question.getId() + "  " + question.getTimeOut());
                            if (question != null) {
                                GameControl.setCurrentQuestion(question);
                                int total = GameControl.currentQuestion.getTotalQuestion();
                                int timeOut = GameControl.currentQuestion.getTimeOut();
                                if (total != 0) {
                                    GameControl.total = total;
                                }
                                if (timeOut != 0) {
                                    GameControl.timeOut = timeOut;
                                    time_reduce.setText(timeOut + " s");
                                }
                            }
                            //showQuestion();
                            //showquestionView(GameControl.currentQuestion);
                            try {
                                if (!isFirst) {
                                    checkWheatherCanPlay();

                                    isFirst = false;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    break;
                case Constants.AGORA_SIGNAL_SEND:
                    // TextMessage sendContent = (TextMessage) msg.obj;

                    // String sendName = sendContent.getUserInfo().getUserId();
                    // String messge = sendContent.getContent();
                    //logD("handleMessage   = " + messge);

                    String sendMessage = (String) msg.obj;
                    GameControl.logD(tag + " sendMessage  = " + sendMessage);
                    io.agora.agoraandroidhq.module.Message jsonObjects = null;
                    try {
                        jsonObjects = (io.agora.agoraandroidhq.module.Message) JsonToString.jsonToString(sendMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    GameControl.logD(tag + " sendMessage  sendName  sendContent " + jsonObjects.getContent() + "   " + jsonObjects.getSender());
                    String sendName = jsonObjects.getSender();

                    String content = jsonObjects.getContent();
                    GameControl.logD(tag + " sendMessage = ");
                    io.agora.agoraandroidhq.module.Message message = new io.agora.agoraandroidhq.module.Message(sendName, content);
                    message.setIsMe(true);
                    messageRecyclerViewAdapter.updateData(message);
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    recyclerView.smoothScrollToPosition(messageRecyclerViewAdapter.getItemCount() - 1);
                    break;

                case Constants.MESSAGE_SEND_ERROR:
                    //TODO
                    break;
                default:
                    break;
            }
        }
    };

    private void toastHelper(String message) {
        Toast.makeText(GameActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void startCheckWheatherCanPlay() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                GameControl.logD(tag + " startCheckWheatherCanPalyThread");

                try {
                    if (isFirst) {
                        checkWheatherCanPlay();

                        isFirst = false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void checkWheatherCanPlay() throws JSONException {
        AgoraSignal.checkWheatherCanPlay(new HttpUrlUtils.OnResponse() {
            @Override
            public void onResponse(String data) throws JSONException {
                if (data.equals(Constants.MESSAGE_TOAST)) {
                    return;
                }
                // isFirst = false;
                if (data != null) {
                    JSONObject object = new JSONObject(data);
                    boolean wheatherCanPlay = object.getBoolean("result");
                    GameControl.serverWheatherCanPlay = wheatherCanPlay;
                    object = null;
                }
            }
        });
    }

    private Handler questionTimeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0:
                    int questime = (int) msg.obj;
                    //logD("questionTime  = " + questime);
                    GameControl.logD(tag + " questionReduceTime  =  " + questime);
                    if (questime < 0) {

                        questionTime = GameControl.timeOut;
                        return;
                    }
                    time_reduce.setTextColor(Color.RED);
                    time_reduce.setText(questime + " s");
                    if (questime == 0) {
                        questionFlag = false;
                        if (GameControl.serverWheatherCanPlay && GameControl.clientWheatherCanPlay) {
                            //  GameControl.clientWheatherCanPlay = false;
                            submitAnswer();
                        } else {
                            GameControl.result = -1;
                            Toast.makeText(GameActivity.this, "you can not play", Toast.LENGTH_SHORT).show();
                        }
                        game_layout.setVisibility(View.GONE);
                        questionTime = GameControl.timeOut;
                    }
                    break;
                case 1:
                    if (game_layout.getVisibility() == View.VISIBLE) {
                        game_layout.setVisibility(View.GONE);
                        time_reduce.setTextColor(Color.RED);
                        time_reduce.setText(GameControl.timeOut + " s");
                        questionFlag = true;
                        isFirstTimeSEI = true;
                    }
                    break;

                case 2:
                    break;
            }
        }

    };

    private void showQuestion() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                questionTime = GameControl.timeOut;
                GameControl.logD(tag + " showQuestion questionFlag = " + questionFlag);
                while (questionFlag) {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    questionTime = questionTime - 1;
                    if(questionTimeHandler != null) {
                        Message message = questionTimeHandler.obtainMessage();
                        message.what = 0;
                        message.obj = questionTime;
                        questionTimeHandler.sendMessage(message);
                    }
                    if (questionTime == -1) {
                        questionFlag = false;
                    }
                }
            }
        });

        //questionFlag = true;
        GameActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                GameControl.logD(tag + " GameControl serverWheatherCanPlay = " + GameControl.serverWheatherCanPlay + "  GameControl.clientWheatherCanPlay  = " + GameControl.clientWheatherCanPlay);

                if (GameControl.serverWheatherCanPlay && GameControl.clientWheatherCanPlay) {
                    // logD("gameActivituy  =  " + "1111");

                    showquestionView(GameControl.currentQuestion);

                } else {
                    showquestionView(GameControl.currentQuestion);
                    try {
                        changeQuestionViewToRelive();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private boolean checkSelfPermissions() throws Exception {
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO, 0) &&
                checkSelfPermission(Manifest.permission.CAMERA, 1) &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 2);
    }

    public boolean checkSelfPermission(String permission, int requestCode) throws Exception {
        // log.debug("checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        if (Manifest.permission.CAMERA.equals(permission)) {
            workThreadJoinChannel();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        //  log.debug("onRequestPermissionsResult " + requestCode + " " + Arrays.toString(permissions) + " " + Arrays.toString(grantResults));
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        checkSelfPermission(Manifest.permission.CAMERA, 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // finish();

                }
                break;
            }
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    workThreadJoinChannel();
                } else {
                    // finish();
                }
                break;
            }
            case 2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // finish();
                }
                break;
            }
        }
    }

    // Tutorial Step 5
    private void setupRemoteVideo(int uid) {
        FrameLayout container = (FrameLayout) findViewById(R.id.live_view);

        if (container.getChildCount() >= 1) {
            return;
        }
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        container.addView(surfaceView);
        rtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    // Tutorial Step 7
    private void onRemoteUserLeft() {
        FrameLayout container = (FrameLayout) findViewById(R.id.live_view);
        container.removeAllViews();
    }

    // Tutorial Step 10
    private void onRemoteUserVideoMuted(int uid, boolean muted) {
        FrameLayout container = (FrameLayout) findViewById(R.id.live_view);

        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);

        Object tag = surfaceView.getTag();
        if (tag != null && (Integer) tag == uid) {
            surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE);
        }
    }

    // private Button button_show;
    private LinearLayout game_layout;
    private Button submit_btn;
    private LinearLayout question_layout;
    // private ArrayList<String> questionData = new ArrayList<String>();
    private ArrayList<CheckBox> checkBox_item = new ArrayList<CheckBox>();
    private ArrayList<View> board = new ArrayList<View>();
    private TextView game_title;
    private TextView wheath_canPlay_TextView;
    private TextView time_reduce;

    private void initQuestionLayout() {
        // button_show = findViewById(R.id.show_question_btn);
        game_layout = findViewById(R.id.game_layout);
        submit_btn = findViewById(R.id.submit_button);
        game_layout.setVisibility(View.GONE);
        question_layout = findViewById(R.id.question_layout);
        game_title = findViewById(R.id.game_title);
        wheath_canPlay_TextView = findViewById(R.id.wheather_canplay_TextView);
        wheath_canPlay_TextView.setVisibility(View.GONE);
        time_reduce = findViewById(R.id.time_reduce);
        setSubmitbtnListener();
    }

    private void setSubmitbtnListener() {
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GameControl.serverWheatherCanPlay && GameControl.clientWheatherCanPlay) {
                    if (GameControl.currentQuestion != null) {
                        //    showquestionView(GameControl.currentQuestion);
                        submitAnswer();
                    }
                } else if ((!GameControl.serverWheatherCanPlay) | (!GameControl.clientWheatherCanPlay)) {
                    try {
                        buttonToRelive();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void submitAnswer() {
        questionFlag = false;
        // StringBuilder builder = new StringBuilder();
        int a = -1;
        for (int i = 0; i < checkBox_item.size(); i++) {

            CheckBox checkBox = checkBox_item.get(i);
            if (checkBox.isChecked()) {
                a = i;
            }
        }
        if (a == -1) {
            Toast.makeText(GameActivity.this, R.string.answer_is_null_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(GameActivity.this, R.string.choose_success_message, Toast.LENGTH_SHORT).show();
        }
        GameControl.logD(tag + " submit answer  url = " + Constants.HTTP_SEND_ANSWER_TO_SERVER);
        try {
            AgoraSignal.sendAnswerToserver(GameControl.currentQuestion.getId(), a, new HttpUrlUtils.OnResponse() {
                @Override
                public void onResponse(String data) {
                    // logD("sendAnswerToserver   = " + data);
                    GameControl.logD(tag + " sendAnswer OnResponse  =  " + data);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //question_layout.removeAllViews();
        checkBox_item.clear();
        board.clear();
        // questionData.clear();
        game_layout.setVisibility(View.GONE);
        questionTime = GameControl.timeOut;
    }

    private void setCheckBoxBackHighLight(int result) {
        checkBox_item.get(result).setBackgroundColor(Color.GREEN);
    }

    private void showquestionView(Question question) {
        answerList = new ArrayList();
        question_layout.removeAllViews();
        checkBox_item.clear();
        board.clear();
        submit_btn.setVisibility(View.VISIBLE);
        answerList = GameControl.currentQuestion.getAnswerString();
        String title = question.getQuestion();
        game_title.setTextSize(20);
        int questionId = question.getId() + 1;
        String questionTitle = questionId + "   " + title;
        game_title.setText(questionTitle);
        game_title.setVisibility(View.VISIBLE);
        for (int i = 0; i < answerList.size(); i++) {
            if (i == 0) {
                View bo = createBoard();
                board.add(bo);
                question_layout.addView(bo);
            }
            CheckBox checkBox = createCheckBox((String) (answerList.get(i)), i);
            checkBox.setTextSize(20);
            checkBox_item.add(checkBox);
            question_layout.setDividerPadding(20);
            question_layout.addView(checkBox);
            if (i < (answerList.size())) {
                View bo = createBoard();
                board.add(bo);
                question_layout.addView(bo);
            }
        }
        game_layout.setVisibility(View.VISIBLE);
        wheath_canPlay_TextView.setVisibility(View.GONE);
        //time = 10;
    }

    private CheckBox createCheckBox(String text, int position) {

        // View view = game_layout.findViewById(R.id.question_layout);
        CheckBox box = new CheckBox(GameActivity.this);
        //GameControl.logD("text  = " + text);
        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 15, 0, 15);
        box.setText(text);
        box.setTextColor(Color.BLACK);
        box.setTag(position);
        box.setLayoutParams(layoutParams);
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < checkBox_item.size(); i++) {
                        if (checkBox_item.get(i) != buttonView) {
                            checkBox_item.get(i).setChecked(false);
                        }

                        if (checkBox_item.get(i) == buttonView) {
                            GameControl.result = i;
                        }
                    }
                }
            }
        });
        return box;
    }

    private void getCorrectCheckBox(int positions) {
        int question_count = question_layout.getChildCount();
        GameControl.logD(tag + "getCorrectCheckBoxitem =  " + question_count);
        for (int i = 0; i < question_count; i++) {
            // GameControl.logD("childTag = " + question_layout.getChildAt(i).getTag() + "");

            View view = question_layout.getChildAt(i);
            String tag = view.getTag() + "";

            //GameControl.logD("childTag = " + question_layout.getChildAt(i).getTag() + "");

            if (tag.equals(positions + "")) {
                // GameControl.logD("correctChild  =  setBackGround");
                view.setBackgroundColor(Color.GREEN);
            }
        }
    }

    private View createBoard() {
        View view = new View(GameActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        // params.setMargins(left, top, right, bottom);
        params.setMargins(0, 20, 0, 20);
        // view.setBackgroundColor(Color.BLACK);
        view.setLayoutParams(params);
        return view;
    }

    private void changeQuestionViewToRelive() throws JSONException {
        showquestionView(GameControl.currentQuestion);
        wheath_canPlay_TextView.setVisibility(View.VISIBLE);
        wheath_canPlay_TextView.setText(R.string.game_cannot_play);
        wheath_canPlay_TextView.setTextSize(16);
        wheath_canPlay_TextView.setTextColor(Color.RED);
        submit_btn.setText(R.string.relive_message);
        submit_btn.setTextColor(Color.RED);
        game_layout.setVisibility(View.VISIBLE);
        game_title.setVisibility(View.VISIBLE);
    }

    private void buttonToRelive() throws JSONException {
        AgoraSignal.checkRelive(new HttpUrlUtils.OnResponse() {
            @Override
            public void onResponse(String data) throws JSONException {
                // logD("checkWheatherCanPlay -------------");

                //logD(data + "");
                GameControl.logD(tag + "reliveButton  onResponse =  " + data);
                if (data.equals(Constants.MESSAGE_TOAST)) {
                    System.out.println(Toast.makeText(GameActivity.this, R.string.connect_net_error_or_server_error, Toast.LENGTH_SHORT));
                }

                if (data.equals("{}")) {
                    // logD("checkWheatherCanPlay");
                    GameControl.serverWheatherCanPlay = true;
                    GameControl.clientWheatherCanPlay = true;

                    GameActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wheath_canPlay_TextView.setVisibility(View.GONE);
                            submit_btn.setText(R.string.submit_message);
                            submit_btn.setTextColor(Color.BLACK);
                            Toast.makeText(GameActivity.this, R.string.relive_success_message, Toast.LENGTH_SHORT).show();
                            showquestionView(GameControl.currentQuestion);
                        }
                    });
                } else {
                    GameControl.serverWheatherCanPlay = false;
                    Toast.makeText(GameActivity.this, R.string.fail_to_relive_message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        GameControl.logD(tag + "onFirstRemoteVideoDecoded");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setupRemoteVideo(uid);
            }
        });
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        GameControl.logD(tag + "onJoinChannelSuccess  channel  =  " + channel);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onReceiveSEI(final String info) {
        GameControl.logD(tag + "onReceiveSEI  = " + info);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject = null;
                int sid = -1;
                if (info != null) {
                    try {
                        jsonObject = new JSONObject(info);
                        // sid = jsonObject.g etInt("questionId");
                        JSONObject data = jsonObject.getJSONObject("data");

                        sid = data.getInt("questionId");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (GameControl.currentQuestion != null) {
                        if (sid == GameControl.currentQuestion.getId() && isFirstTimeSEI) {
                            showQuestion();
                            isFirstTimeSEI = false;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onRemoteUserLeft();
            }
        });
        GameControl.logD(tag + "onUserOffline");
    }

    @Override
    public void onUserMuteVideo(final int uid, final boolean muted) {
        GameControl.logD(tag + "onUserMuteVideo");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onRemoteUserVideoMuted(uid, muted);
            }
        });
    }

    private void workThreadJoinChannel() {
        workerThread = ((HqApplication) getApplication()).getWorkerThread();
        if (workerThread == null) {
            ((HqApplication) getApplication()).initWorkerThread();
        }
        //joinAgoraLiveChannel();
        workerThread = ((HqApplication) getApplication()).getWorkerThread();
        workerThread.eventHandler().addEventHandler(this);
        workerThread.joinChannel();
        rtcEngine = workerThread.getRtcEngine();
    }
}
