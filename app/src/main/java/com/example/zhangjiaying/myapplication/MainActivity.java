package com.example.zhangjiaying.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks{

    // Store the Chat action.
    private Chat chat;
    private QiChatbot qiChatbot;
    private ConstraintLayout mainFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainFrame = findViewById(R.id.mainFrame);

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // Create a topic.
        Topic topic = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
                .withResource(R.raw.sample_talk_02) // Set the topic resource.
                .build(); // Build the topic.

        // Create a new QiChatbot.
        qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        qiChatbot.addOnBookmarkReachedListener(bookmark -> {
            if("show_favorite_color".equals(bookmark.getName())){
                updateTabletColor();
            }
        });

        // Create a new Chat action.
        chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        chat.async().run();

    }

    private void updateTabletColor() {
        //qiChatbotがnullの時に動くとクラッシュしてしまうため
        if(qiChatbot == null){
            return;
        }
        String tabletColor = qiChatbot.variable("favorite_color").getValue();
        //final をつけて変数を定数に変える
        final int androidTabletColor = getAndroidTabletColor(tabletColor); //画面に特定の色を表示させたい時、intで指定する

        //qichatがバックグラウンドで動いているため、必ず画面を更新させるためにUIスレッド上で動かす
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainFrame.setBackgroundColor(androidTabletColor);
            }
        });
    }

    private int getAndroidTabletColor(String color) {
        switch(color){
            case "赤":
                return Color.RED;
            case "緑":
                return Color.GREEN;
            case  "青":
                return Color.BLUE;
            default:
                return 0;
        }
    }


    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }
}


