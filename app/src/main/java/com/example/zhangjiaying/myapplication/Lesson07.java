package com.example.zhangjiaying.myapplication;

import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Say;

public class Lesson07 extends RobotActivity implements RobotLifecycleCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chaining_action);

        QiSDK.register(this, this);
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Say helloSay = SayBuilder.with(qiContext)
                .withText("こんにちは　pepperです。みなさんお元気ですか？")
                .build();

        Animation helloAnimation = AnimationBuilder.with(qiContext)
                .withResources(R.raw.hello_a001)
                .build();

        Animate helloAnimate = AnimateBuilder.with(qiContext)
                .withAnimation(helloAnimation)
                .build();

        Say introductionSay = SayBuilder.with(qiContext)
                .withText("今からダンスを踊ります。見ててください。")
                .build();

        Animation animation = AnimationBuilder.with(qiContext)
                .withResources(R.raw.disco_a001)
                .build();

        Animate animate = AnimateBuilder.with(qiContext)
                .withAnimation(animation)
                .build();

        Say endingSay = SayBuilder.with(qiContext)
                .withText("いかがでしたか？")
                .build();

        Future<Void> helloSayFuture = helloSay.async().run();
        Future<Void> helloAnimateFuture = helloAnimate.async().run();

        Future<Void> helloFuture = Future.waitAll(helloSayFuture, helloAnimateFuture);

        Future<Void> chainFuture = helloFuture
                .andThenCompose(aVoid -> introductionSay.async().run())
                .andThenCompose(aVoid -> animate.async().run())
                .andThenCompose(aVoid -> endingSay.async().run());

        chainFuture.thenConsume(voidFuture -> {
            if(voidFuture.hasError()){
                Log.i("MyTag", "The Chain finish with error: " + voidFuture.getErrorMessage());
            }
            else{
                Log.i("MyTag", "The chain finish without error: ");
            }
        });
    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }
}
