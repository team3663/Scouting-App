package com.team3663.scouting_app.utility;

import java.util.ArrayList;

// =============================================================================================
// Class:       Achievements
// Description: Everything related to scouter achievements
// =============================================================================================
 public class Achievements {
    private static final ArrayList<Achievement> achievement_list = new ArrayList<>();

    // Member Function: pop (to the screen) any achievements "met" but not already "popped"
    public void popAchievements() {

    }

    // Member Function: Update data on all known achievements
    public void updateAchievements() {

    }

    // Member Function: Clear data on achievements (ie: new scouter)
    public void clear(){

    }

    // =============================================================================================
    // Class:       Achievement
    // Description: Everything about a single achievement
    // =============================================================================================
     private static class Achievement {
        private String description;
        private boolean met;
        private boolean popped;

        private Achievement(String in_description) {
            description = in_description;
            met = false;
            popped = false;
        }
    }
}
