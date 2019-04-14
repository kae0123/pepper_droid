package com.example.zhangjiaying.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

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
import com.aldebaran.qi.sdk.object.touch.Touch;
import com.aldebaran.qi.sdk.object.touch.TouchSensor;

public class Lesson08 extends RobotActivity implements RobotLifecycleCallbacks {

    private Button startChainButton;
    private Say helloSay;
    private Animate helloAnimate;
    private Say introductionSay;
    private Animate animate;
    private Say endingSay;
    private Button stopChainButton;
    private Future<Void> chainFuture;
    private Button skipHelloButton;
    private Future<Void> helloFuture;
    private TouchSensor touchSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chaining_action);

        startChainButton = findViewById(R.id.startChainButton);
        startChainButton.setOnClickListener(view -> {
            startChainAction();
        });

        stopChainButton = findViewById(R.id.stopChainButton);
        stopChainButton.setOnClickListener(view -> {
            stopChainAction();
        });

        skipHelloButton = findViewById(R.id.skipHelloButton);
        skipHelloButton.setOnClickListener(view -> {
            skipHelloAction();
        });

        QiSDK.register(this, this);
    }

    private void skipHelloAction() {
        if(helloFuture != null && !helloFuture.isDone()){
            helloFuture.requestCancellation();
        }
    }

    private void stopChainAction() {
        // chain開始までに時間がかかる可能性があるため
        if(chainFuture != null && !chainFuture.isDone()){
            chainFuture.requestCancellation();
        }
    }

    private void startChainAction() {
        Future<Void> helloSayFuture = helloSay.async().run();
        Future<Void> helloAnimateFuture = helloAnimate.async().run();

        helloFuture = Future.waitAll(helloSayFuture, helloAnimateFuture);

        chainFuture = helloFuture
                .thenCompose(aVoid -> { //aVoidが直前の結果
                    if(aVoid.isSuccess()){
                        Log.i("MyTag", "The hello future has ended with success");
                    }
                    else if(aVoid.isCancelled()){
                        Log.i("MyTag", "The hello future has been cancelled");
                    }
                    else if(aVoid.hasError()){
                        Log.i("MyTag", "The hello future has finished with a error: "
                                + aVoid.getErrorMessage());
                    }

                    return introductionSay.async().run();
                })
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
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Touch touch = qiContext.getTouch();
        touchSensor = touch.getSensor("Head/Touch");
        touchSensor.addOnStateChangedListener(state -> {
            Log.i("MyTag", "The touch state has changed to " + state.getTouched());
            if(state.getTouched()){
                stopChainAction();
            }
        });

        helloSay = SayBuilder.with(qiContext)
                .withText("こんにちは　pepperです。みなさんお元気ですか？")
                .build();

        Animation helloAnimation = AnimationBuilder.with(qiContext)
                .withResources(R.raw.hello_a001)
                .build();

        helloAnimate = AnimateBuilder.with(qiContext)
                .withAnimation(helloAnimation)
                .build();

        introductionSay = SayBuilder.with(qiContext)
                .withText("今からダンスを踊ります。見ててください。")
                .build();

        Animation animation = AnimationBuilder.with(qiContext)
                .withResources(R.raw.disco_a001)
                .build();

        animate = AnimateBuilder.with(qiContext)
                .withAnimation(animation)
                .build();

        endingSay = SayBuilder.with(qiContext)
                .withText("いかがでしたか？")
                .build();
    }

    @Override
    public void onRobotFocusLost() {
        if(touchSensor != null){
            touchSensor.removeAllOnStateChangedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }
}
