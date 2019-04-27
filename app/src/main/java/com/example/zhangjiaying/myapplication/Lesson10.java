package com.example.zhangjiaying.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TakePictureBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.camera.TakePicture;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.image.EncodedImage;
import com.aldebaran.qi.sdk.object.image.TimestampedImageHandle;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lesson10 extends RobotActivity implements RobotLifecycleCallbacks {

    private ImageView imageView;
    private Future<Void> chatFuture;
    private QiChatbot qiChatbot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY);

        setContentView(R.layout.activity_take_picture);

        imageView = findViewById(R.id.imageView);

        QiSDK.register(this, this);
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this,this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.picture_topic)
                .build();

        qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        qiChatbot.addOnEndedListener(endReason -> {
            Log.i("MyTag", "qiChatbot ended. Reason: " + endReason);
            if(chatFuture != null && !chatFuture.isDone()){
                chatFuture.requestCancellation();
            }
        });

        Map<String, QiChatExecutor> executors = new HashMap<>();
        executors.put("takePictureExecutor", new TakePictureExecutor(qiContext));
        qiChatbot.setExecutors(executors);

        Chat chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        chatFuture = chat.async().run();

    }

    private void displayPicture(Bitmap bitmap) {
        runOnUiThread(()->imageView.setImageBitmap(bitmap));
    }

    @Override
    public void onRobotFocusLost() {
        if(qiChatbot != null){
            qiChatbot.removeAllOnEndedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }

    class TakePictureExecutor extends BaseQiChatExecutor{

        protected TakePictureExecutor(QiContext context) {
            super(context);
        }

        @Override
        public void runWith(List<String> params) {
            TakePicture takePicture = TakePictureBuilder.with(getQiContext()).build();

            TimestampedImageHandle timestampedImageHandle = takePicture.run();
            EncodedImage encodedImage = timestampedImageHandle.getImage().getValue();
            ByteBuffer data = encodedImage.getData();

            byte[] dataArray = data.array();

            Bitmap bitmap = BitmapFactory.decodeByteArray(dataArray, 0, dataArray.length);

            displayPicture(bitmap);
        }

        @Override
        public void stop() {

        }
    }
}
