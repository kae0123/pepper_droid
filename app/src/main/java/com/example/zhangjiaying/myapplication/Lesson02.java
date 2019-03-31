package com.example.zhangjiaying.myapplication;

import android.os.Bundle;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;

public class Lesson02 extends RobotActivity implements RobotLifecycleCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        //聞き取る内容
        PhraseSet phrase = PhraseSetBuilder.with(qiContext)
                .withTexts("おはよう","おやすみ")
                .build();
        //聞く
        Listen listen = ListenBuilder.with(qiContext)
                .withPhraseSet(phrase)
                .build();
        ListenResult listenResult = listen.run();

        //発話
        /*
        SayBuilder.with(qiContext) // Create the builder with the context.
                .withText(listenResult.getHeardPhrase().getText()) // Set the text to say.
                .build().run(); // Build the say action.*/

        String str = listenResult.getHeardPhrase().getText();
        System.out.println(str);
        if(str.equals("おはよう")) { // 必ず.equals()を使う
            SayBuilder.with(qiContext)
                    .withText("おはようございます")
                    .build().run();
        }
        else{
            SayBuilder.with(qiContext)
                    .withText("おやすみなさい")
                    .build().run();
        }
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }
}
