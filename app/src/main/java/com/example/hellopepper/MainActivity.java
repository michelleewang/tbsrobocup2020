package com.example.hellopepper;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;

import com.aldebaran.qi.Future;
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
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    int diagnosis = -2;
    Future<Void> execFuture;
    private QiContext qiContext = null;
    private CheckBox fever;
    private CheckBox cough;
    private CheckBox shortness;
    private CheckBox trouble;
    private CheckBox fatigue;
    private CheckBox chills;
    private CheckBox headache;
    private CheckBox throat;
    private CheckBox nausea;
    private CheckBox diarrhea;
    private Button submit;
    private Boolean[] dumbArray;
    private Say sayAction, startQuestion, alive, sick, youaredead, diagnosing, thankyou, instructions;
    private Bookmark bookmark = null;
    private Bookmark doneBookmark = null;
    private Topic topic, topic2;
    private QiChatbot qiChatbot, qiChatbot2;
    private Chat chatAction, chatAction2;
    private QiChatVariable chatVariable;
    private String[] yesStringArray = {"yes", "yup", "affirmative", "yeah", "sure"};
    private List<String> yesStrings = Arrays.asList(yesStringArray);
    private Boolean diagnosisDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        execFuture = chatAction.async().run();
    }

    private void initActions() {
//        chatVariable = qiChatbot.variable("Chat");
//        chatVariable.addOnValueChangedListener(currentValue -> {
//            if(yesStrings.contains(chatVariable.getValue().toLowerCase())) {
//                displayCheckboxes();
//            }
//        });

        startQuestion = SayBuilder.with(qiContext)
                .withText("Hello. What are you here for today?")
                .build();

        alive = SayBuilder.with(qiContext)
                .withText("healthy")
                .build();

        sick = SayBuilder.with(qiContext)
                .withText("sick")
                .build();

        youaredead = SayBuilder.with(qiContext)
                .withText("dead")
                .build();

        diagnosing = SayBuilder.with(qiContext)
                .withText("Diagnosing...")
                .build();

        instructions = SayBuilder.with(qiContext)
                .withText(" Please indicate your symptoms on the screen below. Press Submit or inform me when finished.")
                .build();

        thankyou = SayBuilder.with(qiContext)
                .withText("Thank you.")
                .build();


        sayAction = SayBuilder.with(qiContext)
                .withText("test")
                .build();

        topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.test)
                .build();

        qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        chatVariable = qiChatbot.variable("diagnosis");
        qiChatbot.variable("diagnosis").setValue("");

        qiChatbot.addOnBookmarkReachedListener((bookmark) -> {
            if (bookmark.getName().equals("begun")) {
                displayCheckboxes();
            } else if (bookmark.getName().equals("delay")) {
                displayCheckboxes();
            } else if (bookmark.getName().equals("done") && !diagnosisDone) {
                runOnUiThread(() -> performDiagnosis());
            }
        });

        chatAction = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        topic2 = TopicBuilder.with(qiContext)
                .withResource(R.raw.shortdiagnosis)
                .build();

        qiChatbot2 = QiChatbotBuilder.with(qiContext)
                .withTopic(topic2)
                .build();

        chatVariable = qiChatbot2.variable("diagnosis");
        qiChatbot2.variable("diagnosis").setValue("");

        qiChatbot2.addOnBookmarkReachedListener((bookmark) -> {
            if (bookmark.getName().equals("done") && !diagnosisDone) {
                runOnUiThread(() -> performDiagnosis());
            }
        });

        chatAction2 = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot2)
                .build();

//        for (Map.Entry<String, Bookmark> entry : topic.getBookmarks().entrySet()) {
//            Bookmark temp = entry.getValue();
//            if (temp.getName() == "finaldiagnosis") {
//                doneBookmark = temp;
//                break;
//            }
//        }
    }

    public void initShortenedDiagnosis() {
    }

    @Override
    public void onRobotFocusLost() {
        // Nothing here.
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

    public void displayCheckboxes() {
        diagnosisDone = false;
        runOnUiThread(() -> {
            setContentView(R.layout.symptomcheckbox);
            submit = findViewById((R.id.submitButton));
            submit.setOnClickListener(v -> {
                //setContentView(R.layout.inprogress);
                performDiagnosis();
            });
        });
    }

    public void performDiagnosis() {
//        qiChatbot.goToBookmark(doneBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
        runOnUiThread(() -> {
            fever = findViewById(R.id.feverBox);
            cough = findViewById(R.id.coughBox);
            shortness = findViewById(R.id.shortnessBox);
            trouble = findViewById(R.id.troubleBox);
            fatigue = findViewById(R.id.fatigueBox);
            chills = findViewById(R.id.chillBox);
            headache = findViewById(R.id.headacheBox);
            throat = findViewById(R.id.throatBox);
            nausea = findViewById(R.id.nauseaBox);
            diarrhea = findViewById(R.id.diarrheaBox);

            dumbArray = new Boolean[]{fever.isChecked(), cough.isChecked(), shortness.isChecked(), trouble.isChecked(), fatigue.isChecked(), chills.isChecked(), headache.isChecked(), throat.isChecked(), nausea.isChecked(), diarrhea.isChecked()};
        });
        diagnosis = diagnose(dumbArray);
        diagnosisDone = true;
        displayResults(diagnosis);
    }

    public void displayResults(int diagnosis) {
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String result = null;
//        final Boolean[] diagnoseAgain = {null};
        if (diagnosis == 0) {
            result = " healthy.";
            runOnUiThread(() -> {
                setContentView(R.layout.healthy);
                Button diagnoseButton = (Button) findViewById(R.id.diagnose);
                Button exitButton = (Button) findViewById(R.id.exit);
                diagnoseButton.setOnClickListener(v -> {
                    restartProgram();
                });
                exitButton.setOnClickListener(v -> {
                    exitProgram();
                });
            });
        } else if (diagnosis == 1) {
            result = " sick.";
            runOnUiThread(() -> {
                setContentView(R.layout.sick);
                Button diagnoseButton = (Button) findViewById(R.id.diagnose2);
                Button exitButton = (Button) findViewById(R.id.exit2);
                diagnoseButton.setOnClickListener(v -> {
                    restartProgram();
                });
                exitButton.setOnClickListener(v -> {
                    exitProgram();
                });
            });
        }
        String finalResult = result;
//        runOnUiThread(() -> {qiChatbot.variable("diagnosis").setValue(finalResult);});
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    qiChatbot.variable("diagnosis").setValue(finalResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        execFuture.requestCancellation();
//        if(diagnoseAgain[0]) {
//            execFuture = chatAction.async().run();
//            runOnUiThread(() -> {setContentView(R.layout.activity_main);});
//        } else {
//            runOnUiThread(() -> {setContentView(R.layout.randomstuff);});
//        }
    }

    public int diagnose(Boolean[] dumbArray) {
        int counter = 0;
        for (Boolean var : dumbArray) {
            if (var) {
                counter++;
            }
        }
        float tally = (float) counter / dumbArray.length;
        if (tally >= 0.5) {
            return 1;
        } else {
            return 0;
        }
    }

    public void restartProgram() {
        initShortenedDiagnosis();
        runOnUiThread(() -> {
            instructions.async().run();
            execFuture = chatAction2.async().run();
//          setContentView(R.layout.activity_main);
        });
        displayCheckboxes();
    }

    public void exitProgram() {
        runOnUiThread(() -> setContentView(R.layout.randomstuff));
        thankyou.async().run();
    }
}