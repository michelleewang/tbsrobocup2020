package com.example.hellopepper;

import android.os.Bundle;
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
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    int diagnosis = -2;
    int numSymptoms = 0;
    Diagnosis diagnosisBot = new Diagnosis();
    Future<Void> execFuture;
    private double sickChance = 0.00;
    private QiContext qiContext = null;
    private CheckBox fever, cough, appetite, shortness, fatigue, ache, mucus; //primary symptoms
    private CheckBox headache, loss, throat, nose, nausea, chills, vomiting, diarrhea; //secondary symptoms
    private CheckBox trouble, lips, confusion, pain; //serious symptoms
    private Button submit, moreInfoBack, returnButton, optionsExit;
    private Say sayDiagnosis, instructions, thankyou, anythingelse;
    private Topic topic, topic2;
    private QiChatbot qiChatbot, qiChatbot2;
    private Chat chatAction, diagnosisChat;
    private QiChatVariable chatVariable;
    private Boolean diagnosisDone = false;
    private Boolean inMoreInfo = false;
    private Boolean hasSeriousSymptoms = false;
    private Boolean hasOtherSymptoms = false;
    //    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private Animate happyDance;

    private Bookmark optionsBookmark, diagnosisProposalBookmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        imageView = (ImageView)findViewById(R.id.rainbow);
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
        thankyou = SayBuilder.with(qiContext)
                .withText("Thank you for being cautious.")
                .build();

        sayDiagnosis = SayBuilder.with(qiContext)
                .withText("The diagnosis is complete. Your results should be appearing on-screen.")
                .build();

        instructions = SayBuilder.with(qiContext)
                .withText("Please indicate your symptoms on the screen below and submit when finished.")
                .build();

        anythingelse = SayBuilder.with(qiContext)
                .withText("Anything else?")
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
            } else if (bookmark.getName().equals("startQuestion")) {
                displayOptions(bookmark);
            } else if (bookmark.getName().equals("moreInfo")) {
                moreInfo();
            } else if (bookmark.getName().equals("diagnosisproposal")) {
                diagnosisProposalBookmark = bookmark;
            } else if (bookmark.getName().equals("exitMoreInfo")) {
                exitMoreInfo();
                displayOptions(null);
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

        diagnosisChat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot2)
                .build();
    }

    @Override
    public void onRobotFocusLost() {
        // Nothing here.
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

    public void goToBookmark(Bookmark bookmark) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                qiChatbot.goToBookmark(bookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
            }
        });
        thread.start();
    }

    public void moreInfo() {
        runOnUiThread(() -> {
            setContentView(R.layout.testinginfo);
            moreInfoBack = findViewById((R.id.moreInfoBack));
            moreInfoBack.setOnClickListener(v -> {
                exitMoreInfo();
            });
        });
    }

    public void exitMoreInfo() {
        runOnUiThread(() -> {
            setContentView(R.layout.possibleoptions);
            goToBookmark(optionsBookmark);
        });
    }

    public void displayOptions(Bookmark bookmark) {
        if (bookmark != null) {
            optionsBookmark = bookmark;
        }
        runOnUiThread(() -> {
            setContentView(R.layout.possibleoptions);
            optionsExit = findViewById((R.id.optionsExit));
            optionsExit.setOnClickListener(v -> {
                //setContentView(R.layout.inprogress);
                resetProgram();
            });
        });
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
        final CheckBox[] kidBox = new CheckBox[1];
        final Boolean[] hasOtherSymptomsBool = new Boolean[1];
        final Boolean[] kidOutOfSchool = new Boolean[1];
        runOnUiThread(() -> {
            trouble = findViewById(R.id.troubleBox);
            lips = findViewById(R.id.lipsBox);
            confusion = findViewById(R.id.confusionBox);
            pain = findViewById(R.id.painBox);
            otherBox[0] = findViewById(R.id.unlistedBox);
            kidBox[0] = findViewById(R.id.catchBox);
            otherSymptoms[0] = new Boolean[]{trouble.isChecked(), lips.isChecked(), confusion.isChecked(), pain.isChecked()};
            hasOtherSymptomsBool[0] = otherBox[0].isChecked();
            kidOutOfSchool[0] = kidBox[0].isChecked();
        });
        if (!kidOutOfSchool[0]) {
            diagnosisBot.setSeriousSymptoms(otherSymptoms[0], otherBox[0]);
//        diagnosis = diagnosisBot.diagnose();
            sickChance = diagnosisBot.diagnose();
            numSymptoms = diagnosisBot.sumSymptoms();
            hasSeriousSymptoms = diagnosisBot.hasSeriousSypmtoms();
            hasOtherSymptoms = hasOtherSymptomsBool[0];
            diagnosis = getDiagnosis(sickChance, numSymptoms);
            diagnosisDone = true;
            displayResults(diagnosis);
        } else {
            catchKid();
        }
    }

    public int getDiagnosis(double sickChance, int numSymptoms) {
        if (sickChance < 0.5) {
            if (numSymptoms > 4) {
                return 2; //sick, not COVID
            } else {
                return 0; //healthy
            }
        } else {
            return 1; //sick with COVID
        }
    }

    public void catchKid() {
        runOnUiThread(() -> {
            setContentView(R.layout.notsick);
            returnButton = findViewById((R.id.returnButton));
            returnButton.setOnClickListener(v -> {
                //setContentView(R.layout.inprogress);
                resetProgram();
            });
        });
//        imageViewSetValue("rb");
    }

//    private void imageViewSetValue(String val) {
//        if (val.equals("rb"))
//            runOnUiThread(() -> { imageView.setImageResource(R.drawable.rb); });
//
//    }

    public void displayResults(int diagnosis) {
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        execFuture.requestCancellation();
        String result = null;
        String finalResult = null;
//        double sickChance = diagnosisBot.sickProb * 100.0;
//        String sickChanceStr = df2.format(sickChance);
//        final Boolean[] diagnoseAgain = {null};
        if (diagnosis == 0) {
            result = " healthy.";
            displayResultPopup(R.layout.healthy);
            happyDance.async().run();
        } else if (diagnosis == 2) {
            result = " sick, but not with COVID-19.";
            displayResultPopup(R.layout.otherdisease);
        } else if (diagnosis == 1) {
            result = " sick with COVID-19.";
            displayResultPopup(R.layout.sick);
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
                sayDiagnosis.async().run();
                try {
                    qiChatbot.variable("diagnosis").setValue(finalResult1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void displayMoreInfo() {
        runOnUiThread(() -> {
            setContentView(R.layout.testinginfo);
            moreInfoBack = findViewById((R.id.moreInfoBack));
            moreInfoBack.setOnClickListener(v -> {
                displayResults(diagnosis);
            });
        });
    }

    public void displayResultPopup(int layoutId) {
        runOnUiThread(() -> {
            setContentView(layoutId);
            Button diagnoseButton = (Button) findViewById(R.id.diagnose);
            Button exitButton = (Button) findViewById(R.id.exit);
            Button moreInfoButton = (Button) findViewById(R.id.moreInfo);

            diagnoseButton.setOnClickListener(v -> {
                restartDiagnosis();
            });

            exitButton.setOnClickListener(v -> {
                exitProgram();
            });

            moreInfoButton.setOnClickListener(v -> {
                displayMoreInfo();
            });
        });
    }

    public void updateChance(double sickChance) {
        TextView chanceText = (TextView) findViewById(R.id.sickChance);
        int intChance = (int) sickChance;
        String chanceString = "";
        if (Math.abs(sickChance - intChance) < 0.01) {
            chanceString = Integer.toString(intChance);
            chanceText.setText("Judging from your symptoms, there is an estimated " + chanceString + "% chance you have COVID-19 or a similar disease.*");
        } else {
            chanceString = String.format("%.2f", sickChance * 100);
            chanceText.setText("Judging from your symptoms, there is an estimated " + chanceString + "% chance you have COVID-19 or a similar disease.*");

        }
    }

    public void updateDisclaimer() {
        TextView disclaimer = (TextView) findViewById(R.id.disclaimer);
        disclaimer.setWidth(900);
        String disclaimerText = "Note: you appear to be suffering from symptoms that could potentially be signs of a serious condition. Please contact a doctor or hospital immediately.";
        disclaimer.setText(disclaimerText);
    }

    public void restartDiagnosis() {
        diagnosisBot.resetBot();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TextView disclaimer = (TextView) findViewById(R.id.disclaimer);
        disclaimer.setWidth(790);
        runOnUiThread(() -> {
            instructions.async().run();
            execFuture = diagnosisChat.async().run();
        });
        resetVariables();
        displayCheckboxes();
    }

    public void resetVariables() {
        diagnosis = -2;
        numSymptoms = 0;
        sickChance = 0.00;
        diagnosisDone = false;
        inMoreInfo = false;
        hasSeriousSymptoms = false;
    }

    public void resetProgram() {
        resetVariables();
        execFuture.requestCancellation();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        execFuture = chatAction.async().run();
        setContentView(R.layout.activity_main);
    }

    public void exitProgram() {
        runOnUiThread(() -> setContentView(R.layout.randomstuff));
        thankyou.async().run();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        resetProgram();
    }
}