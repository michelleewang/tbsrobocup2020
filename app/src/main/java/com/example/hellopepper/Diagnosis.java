package com.example.hellopepper;

import android.util.Log;
import android.widget.CheckBox;

public class Diagnosis {

    private Boolean[] prioritySymptoms; //most common ones
    private Boolean[] secondarySymptoms; //less often seen ones
    private Boolean[] seriousSymptoms; //
    private Boolean hasOtherSymptoms = false;

    public static Boolean hasSeriousSypmtoms = false; //has potentially life-threatening or serious symptoms, should call doctor/hospital
    public static double sickProb;
    public static int symptomCounter = 0;

    private double[] priorityProbabilities = {0.98, 0.44, 0.76, 0.4, 0.35, 0.55, 0.28}; //fever.isChecked(), fatigue.isChecked(), cough.isChecked(), appetite.isChecked(), ache.isChecked(), shortness.isChecked(), mucus.isChecked()
    private double[] secondaryProbabilities = {0.08, 0.05, 0.05, 0.05, 0.05, 0.05, 0.03, 0.03};
//    private double[] seriousProbabilities = {}

    public double diagnose() {
        double sickProb = 1.0;
        for (int i = 0; i < prioritySymptoms.length; i++) {
            if (!prioritySymptoms[i]) {
                sickProb = sickProb * (1.0 - (0.9 * priorityProbabilities[i]));
            }
        }
        for (int i = 0; i < secondarySymptoms.length; i++) {
            if (!secondarySymptoms[i]) {
                sickProb = sickProb * (1.0 - secondaryProbabilities[i]);
            }
        }
        if(hasOtherSymptoms) {
            sickProb = sickProb * 0.7;
        }
//        Log.i("sickprob", Double.toString(sickProb));
        return sickProb;
//        if(sickProb < 0.5) {
//            if(sumSymptoms() > 2) {
//                return 2; //sick, not COVID
//            } else {
//                return 0; //healthy
//            }
//        } else {
//            return 1; //sick with COVID
//        }

    }

    public int sumSymptoms() {
        symptomCounter = 0;
        for (int i = 0; i < prioritySymptoms.length; i++) {
            if(prioritySymptoms[i]) {
                symptomCounter++;
            }
        }
        for (int l = 0; l < secondarySymptoms.length; l++) {
            if(secondarySymptoms[l]) {
                symptomCounter++;
            }
        }
        for (int j = 0; j < seriousSymptoms.length; j++) {
            if(prioritySymptoms[j]) {
                symptomCounter++;
            }
        }
        return symptomCounter;
    }

    public void setPrioritySymptoms(Boolean[] mainSymptoms) {
        prioritySymptoms = mainSymptoms;
    }

    public void setSecondarySymptoms(Boolean[] otherSymptoms) {
        secondarySymptoms = otherSymptoms;
    }

    public void setSeriousSymptoms(Boolean[] deadlySymptoms, CheckBox otherSymptomsBox) {
        seriousSymptoms = deadlySymptoms;
        hasOtherSymptoms = otherSymptomsBox.isChecked();
        for(int i = 0; i < seriousSymptoms.length; i++) {
            if(seriousSymptoms[i] == true) {
                hasSeriousSypmtoms = true;
            }
        }
    }

    public Boolean hasSeriousSypmtoms() {
        return hasSeriousSypmtoms;
    }

    public double getSickProb() {
        return sickProb;
    }

    public void resetBot() {
        sickProb = 1.0;
        hasSeriousSypmtoms = false;
        symptomCounter = 0;
    }
}
