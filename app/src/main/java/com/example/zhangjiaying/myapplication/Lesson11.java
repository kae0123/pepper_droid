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
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TakePictureBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.camera.TakePicture;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;
import com.aldebaran.qi.sdk.object.image.EncodedImage;
import com.aldebaran.qi.sdk.object.image.TimestampedImageHandle;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lesson11 extends RobotActivity implements RobotLifecycleCallbacks {

    private ImageView imageView;
    private Future<Void> chatFuture;
    private QiChatbot qiChatbot;
    private Holder holder;
    private Map<String, Bookmark> bookmarks;

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
        holder = HolderBuilder.with(qiContext)
                .withAutonomousAbilities(AutonomousAbilitiesType.BACKGROUND_MOVEMENT,
                        AutonomousAbilitiesType.BASIC_AWARENESS)
                .build();

        Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.picture_topic2)
                .build();

        bookmarks = topic.getBookmarks();

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

    public void goToBookmark(String bookmarkName){
        Bookmark bookmark = bookmarks.get(bookmarkName);
        if(bookmark!=null){
            qiChatbot.async().goToBookmark(bookmark,
                    AutonomousReactionImportance.HIGH,
                    AutonomousReactionValidity.IMMEDIATE);
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
            if(params.size()>0){
                switch (params.get(0)){
                    case "holdAutonomousAbilities":
                        //オートノマスライフ停止
                        holder.hold();
                        //腕の動き停止
                        qiChatbot.setSpeakingBodyLanguage(BodyLanguageOption.DISABLED);
                        goToBookmark("stop_move");
                        break;

                    case "takePicture":
                        //写真を撮る
                        TakePicture takePicture = TakePictureBuilder.with(getQiContext()).build();

                        TimestampedImageHandle timestampedImageHandle = takePicture.run();
                        EncodedImage encodedImage = timestampedImageHandle.getImage().getValue();
                        ByteBuffer data = encodedImage.getData();

                        byte[] dataArray = data.array();

                        Bitmap bitmap = BitmapFactory.decodeByteArray(dataArray, 0, dataArray.length);

                        displayPicture(bitmap);

                        // オートノマスライフモード復活
                        holder.release();
                        //腕の動き復活(今回はなし)
                        //qiChatbot.setSpeakingBodyLanguage(BodyLanguageOption.NEUTRAL);
                        goToBookmark("photo_done");
                        break;

                    default:
                        Log.i("MyTag", "Unknown parameter given to takePictureExecutor: "+params.get(0));
                }
            }




        }

        @Override
        public void stop() {

        }
    }
}
