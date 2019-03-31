package com.example.zhangjiaying.myapplication;

import android.os.Bundle;

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

public class Lesson03_2  extends RobotActivity implements RobotLifecycleCallbacks {

    //private static final String TAG = "QiChatbotActivity";

    // Store the Chat action.
    private Chat chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                .withResource(R.raw.sample_talk_01) // Set the topic resource.
                .build(); // Build the topic.

        // Create a new QiChatbot.
        QiChatbot qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        // Create a new Chat action.
        chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        chat.async().run();

    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }
}
