package com.example.hellopepper;

import android.os.Bundle;
import android.widget.Button;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;

public class OtherStuff  extends RobotActivity implements RobotLifecycleCallbacks {

    private Button button;
    private QiContext qiContext = null;

    private Say sayAction;
    private Animate waveAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.randomstuff);
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
        this.qiContext = qiContext;
        initActions();

        Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.greetings)
                .build();

        QiChatbot qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        Chat chatAction = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        chatAction.async().run();
        waveAction.async().run();
    }

    private void initActions() {
        // Create a new say action.
        sayAction = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("test") // Set the text to say.
                .build(); // Build the say action.

        Animation wave = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.hello_a001) // Set the animation resource.
                .build(); // Build the animation.

        waveAction = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(wave) // Set the animation.
                .build(); // Build the animate action.
    }

    @Override
    public void onRobotFocusLost() {
        // Nothing here.
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }
}

