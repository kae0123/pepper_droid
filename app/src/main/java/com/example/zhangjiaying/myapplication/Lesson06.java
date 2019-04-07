package com.example.zhangjiaying.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.Button;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Lesson06 extends RobotActivity implements RobotLifecycleCallbacks{

    // Store the Chat action.
    private QiChatbot qiChatbot;
    private ConstraintLayout mainFrame;
    private Button showColorButton;
    private Topic topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainFrame = findViewById(R.id.mainFrame);
        showColorButton = findViewById(R.id.showColorButton);
        showColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(topic == null || qiChatbot == null){
                    return;
                }

                topic.async().getBookmarks()
                        .andThenConsume(new Consumer<Map<String, Bookmark>>() {
                            @Override
                            public void consume(Map<String, Bookmark> stringBookmarkMap) throws Throwable {
                                Bookmark bookmark = stringBookmarkMap.get("button_clicked");
                                ;
                                qiChatbot.goToBookmark(bookmark,
                                        AutonomousReactionImportance.HIGH,
                                        AutonomousReactionValidity.IMMEDIATE);
                            }
                        });
                updateTabletColor();
            }
        });

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
        Topic hellotopic = TopicBuilder.with(qiContext)
                .withResource(R.raw.sample_talk_01)
                .build();

        // Create a topic.
        topic = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
                .withResource(R.raw.sample_talk_03) // Set the topic resource.
                .build(); // Build the topic.

        // Create a new QiChatbot.
        qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(hellotopic, topic)
                .build();

        qiChatbot.addOnBookmarkReachedListener(bookmark -> {
            if("show_favorite_color".equals(bookmark.getName())){
                updateTabletColor();
            }
        });

        // 複数のExecutorを持つことができるのでmapを作成
        Map<String, QiChatExecutor> executors = new HashMap<>();
        executors.put("randomColorExecutor", new RandomColorExecutor(qiContext));

        qiChatbot.setExecutors(executors);

        // Create a new Chat action.
        Chat chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        chat.async().run();

    }

    private void updateTabletColor() {
        //qiChatbotがnullの時に動くとクラッシュしてしまうため
        if(qiChatbot == null){
            return;
        }
        //クラッシュ回避
        //String tabletColor = qiChatbot.variable("favorite_color").getValue();
        qiChatbot.async().variable("favorite_color")
                .andThenConsume(new Consumer<QiChatVariable>() {
                    @Override
                    public void consume(QiChatVariable qiChatVariable) throws Throwable {
                        String tabletColor = qiChatVariable.getValue();

                        //final をつけて変数を定数に変える
                        final int androidTabletColor = getAndroidTabletColor(tabletColor); //画面に特定の色を表示させたい時、intで指定する

                        displayColor(androidTabletColor);
                    }
                });



    }

    private void displayColor(final int androidTabletColor) {
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

    class RandomColorExecutor extends BaseQiChatExecutor {
        protected RandomColorExecutor(QiContext context) {
            super(context);
        }

        @Override
        public void runWith(List<String> params) {
            boolean bool = new Random().nextBoolean();

            int color;

            if(bool){
                color = Color.BLUE;
            }
            else{
                color = Color.RED;
            }

            displayColor(color);
        }

        @Override
        public void stop() {
        }
    }
}




