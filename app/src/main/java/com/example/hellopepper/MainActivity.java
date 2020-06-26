package com.example.hellopepper;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

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
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    int diagnosis = -2;
    private double sickChance = 0.00;
    Diagnosis diagnosisBot = new Diagnosis();
    Future<Void> execFuture;
    private QiContext qiContext = null;
    private CheckBox fever;
    private CheckBox cough;
    private CheckBox appetite;
    private CheckBox shortness;
    private CheckBox fatigue;
    private CheckBox ache;
    private CheckBox mucus;
    private CheckBox headache;
    private CheckBox loss;
    private CheckBox throat;
    private CheckBox nose;
    private CheckBox nausea;
    private CheckBox chills;
    private CheckBox vomiting;
    private CheckBox diarrhea;
    private CheckBox trouble;
    private CheckBox lips;
    private CheckBox confusion;
    private CheckBox pain;
    private Button submit;
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
    //    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private Animate happyDance;

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

        Animation happyDanceAnimation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.happydance) // Set the animation resource.
                .build(); // Build the animation.

        happyDance = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(happyDanceAnimation) // Set the animation.
                .build(); // Build the animate action.

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
                runOnUiThread(() -> checkPrimarySymptoms());
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
                runOnUiThread(() -> checkPrimarySymptoms());
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
                checkPrimarySymptoms();
            });
        });
    }

    public void displaySecondaryCheckboxes() {
        runOnUiThread(() -> {
            setContentView(R.layout.secondarysymptoms);
            submit = findViewById((R.id.submitButton2));
            submit.setOnClickListener(v -> {
                //setContentView(R.layout.inprogress);
                checkSecondarySymptoms();
            });
        });
    }

    public void displayFinalCheckboxes() {
        runOnUiThread(() -> {
            setContentView(R.layout.othersymptoms);
            submit = findViewById((R.id.submitButton3));
            submit.setOnClickListener(v -> {
                //setContentView(R.layout.inprogress);
                checkOtherSymptoms();
            });
        });

    }

    public void checkPrimarySymptoms() {
        final Boolean[][] prioritySymptoms = {new Boolean[0]};
        runOnUiThread(() -> {
            fever = findViewById(R.id.feverBox);
            cough = findViewById(R.id.coughBox);
            appetite = findViewById(R.id.appetiteBox);
            shortness = findViewById(R.id.shortnessBox);
            fatigue = findViewById(R.id.fatigueBox);
            ache = findViewById(R.id.acheBox);
            mucus = findViewById(R.id.mucusBox);

            prioritySymptoms[0] = new Boolean[]{fever.isChecked(), fatigue.isChecked(), cough.isChecked(), appetite.isChecked(), ache.isChecked(), shortness.isChecked(), mucus.isChecked()};
        });
        diagnosisBot.setPrioritySymptoms(prioritySymptoms[0]);
        displaySecondaryCheckboxes();
    }

    public void checkSecondarySymptoms() {
        final Boolean[][] secondarySymptoms = {new Boolean[0]};
        runOnUiThread(() -> {
            headache = findViewById(R.id.headacheBox);
            throat = findViewById(R.id.throatBox);
            nose = findViewById(R.id.noseBox);
            chills = findViewById(R.id.chillsBox);
            loss = findViewById(R.id.lossBox);
            nausea = findViewById(R.id.nauseaBox);
            vomiting = findViewById(R.id.vomitingBox);
            diarrhea = findViewById(R.id.diarrheaBox);

            secondarySymptoms[0] = new Boolean[]{headache.isChecked(), throat.isChecked(), nose.isChecked(), chills.isChecked(), loss.isChecked(), nausea.isChecked(), vomiting.isChecked(), diarrhea.isChecked()};
        });
        StringBuilder sbf = new StringBuilder("");
        sbf.append(secondarySymptoms[0]);
//        Log.i("secondary", sbf.toString());
        diagnosisBot.setSecondarySymptoms(secondarySymptoms[0]);
        displayFinalCheckboxes();
    }

    public void checkOtherSymptoms() {
        final Boolean[][] otherSymptoms = {new Boolean[0]};
        final CheckBox[] otherBox = new CheckBox[1];
        final Boolean[] hasOtherSymptoms = new Boolean[1];
        runOnUiThread(() -> {
            trouble = findViewById(R.id.troubleBox);
            lips = findViewById(R.id.lipsBox);
            confusion = findViewById(R.id.confusionBox);
            pain = findViewById(R.id.painBox);
            otherBox[0] = findViewById(R.id.unlistedBox);
            otherSymptoms[0] = new Boolean[]{trouble.isChecked(), lips.isChecked(), confusion.isChecked(), pain.isChecked()};
            hasOtherSymptoms[0] = otherBox[0].isChecked();
        });
        diagnosisBot.setSeriousSymptoms(otherSymptoms[0], otherBox[0]);
//        diagnosis = diagnosisBot.diagnose();
        double sickChance = diagnosisBot.diagnose();
        int numSymptoms = diagnosisBot.sumSymptoms();
        Boolean hasSeriousSymptoms = diagnosisBot.hasSeriousSypmtoms();
        diagnosis = getDiagnosis(sickChance, numSymptoms);
        diagnosisDone = true;
        displayResults(diagnosis, sickChance, hasSeriousSymptoms, hasOtherSymptoms[0]);
    }

    public int getDiagnosis(double sickChance, int numSymptoms) {
        if (sickChance < 0.5) {
            if (numSymptoms > 2) {
                return 2; //sick, not COVID
            } else {
                return 0; //healthy
            }
        } else {
            return 1; //sick with COVID
        }
    }

    public void displayResults(int diagnosis, double sickChance, Boolean hasSeriousSymptoms, Boolean hasOtherSymptoms) {
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String result = null;
        String finalResult = null;
//        double sickChance = diagnosisBot.sickProb * 100.0;
//        String sickChanceStr = df2.format(sickChance);
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
            happyDance.async().run();
        } else if (diagnosis == 2) {
            result = " sick, but not with COVID-19.";
            runOnUiThread(() -> {
                setContentView(R.layout.otherdisease);
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
            result = " sick with COVID-19.";
            runOnUiThread(() -> {
                setContentView(R.layout.sick);
                Button diagnoseButton = (Button) findViewById(R.id.diagnose);
                Button exitButton = (Button) findViewById(R.id.exit);
                diagnoseButton.setOnClickListener(v -> {
                    restartProgram();
                });
                exitButton.setOnClickListener(v -> {
                    exitProgram();
                });
            });
        }
        updateChance(sickChance);
        if (hasSeriousSymptoms) {
            updateDisclaimer();
        }
//        runOnUiThread(() -> {qiChatbot.variable("diagnosis").setValue(finalResult);});
        finalResult = result;
        String finalResult1 = finalResult;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    qiChatbot.variable("diagnosis").setValue(finalResult1);
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

    public void updateChance(double sickChance) {
        TextView chanceText = (TextView) findViewById(R.id.sickChance);
        int intChance = (int) sickChance;
        if (Math.abs(sickChance - intChance) < 0.01) {
            chanceText.setText("Judging from your symptoms, there is an estimated " + Integer.toString(intChance) + "% chance you have COVID-19 or a similar disease.*");
        } else {
            chanceText.setText("Judging from your symptoms, there is an estimated " + String.format("%.2f", sickChance * 100) + "% chance you have COVID-19 or a similar disease.*");

        }
    }

    public void updateDisclaimer() {
        TextView disclaimer = (TextView) findViewById(R.id.disclaimer);
        disclaimer.setWidth(900);
        disclaimer.setText("Note: you appear to be suffering from symptoms that could potentially be signs of a serious condition. Please contact a doctor or hospital immediately.");
    }

    public void restartProgram() {
        diagnosisBot.resetBot();
        TextView disclaimer = (TextView) findViewById(R.id.disclaimer);
        disclaimer.setWidth(790);
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