/*
    Finley Meadows & Michael Anderson
    12/4/2024
    The Frog class runs all the back end calculations for each frog added to the screen
*/

import javax.swing.*;

public class Frog {

    // stages of a frog's life cycle
    private final String[] stages = {"Tadpole", "Frog"};
    // holds the current stage of the frog
    private int stage;
    // the name of the frog
    private final String name = getRandomName();
    // each tadpole needs 3 pieces of algae each day
    private int algaeEaten;
    // each frog needs 3 bugs per day
    private int bugsEaten;
    // each frog needs a burrow
    private boolean hasBurrow;
    // each frog needs a lily pad to eat
    private boolean hasLilyPad;
    // saves the burrow the frog is assigned to
    private FrogContainer burrow;
    // saves the lily pad the frog is assigned to
    private FrogContainer lilyPad;
    // display label for the egg, tadpole, or frog
    private JLabel displayLabel = new JLabel();
    // swims the frog/tadpole around when in the water
    private SwimHandler swimPos;

    public Frog() {
        // starts as a tadpole (stage 0)
        this.stage = 0;
        // hasn't eaten anything yet
        this.algaeEaten = 0;
        this.bugsEaten = 0;
        // unassigned for now
        this.hasBurrow = false;
        displayLabel.setName("FrogLabel");
        // puts the tadpole somewhere random in the water
        randomizeWaterLoc();
        // assigns a SwimMovementHandler to the tadpole so it can be swum around
        swimPos = new SwimHandler(this);
    }

    private String getRandomName() {
        String[] names = {
                "Lily", "Clover", "Fern", "Pebble", "Willow",
                "Mossy", "Dewdrop", "Puddle", "Meadow", "Rainy",
                "Pickle", "Jellybean", "Tater Tot", "Muffin", "Cupcake",
                "Sprout", "Lemon", "Olive", "Pistachio", "Gingersnap",
                "Hoppy", "Bouncer", "Ribbit", "Wiggles", "Toadette",
                "Leapster", "Zippy", "Quibbles", "Splashy", "Flip-Flop",
                "Frodo", "Glimmer", "Toadsworth", "Finley", "Froglina",
                "Mikey", "Skippy", "Doodle", "Liam", "Luca", "Michael",
        };


        return names[(int) (Math.random() * names.length)];
    }

    public void randomizeWaterLoc() {
        int x = (int) (Math.random() * 600) + 50;
        int y = (int) (Math.random() * 200) + 250;
        displayLabel.setLocation(x, y);
    }

    public boolean isAlive() {
        double survivalRate;
        int randNum = (int) (Math.random() * 100) + 1;

        // tadpole 70% max
        if (stage == 0) {
            survivalRate = 0;
            survivalRate += 10 * algaeEaten;
            // based on water cleanliness
            survivalRate += 50 - GameRunner.pond.getWaterDirtiness() / 2.0;

            System.out.println("Tadpole survival rate: " + survivalRate);
        }
        // frog
        else {
            // base survival rate of 15%
            survivalRate = 15;
            // 10% for each bug eaten (max of 3 bugs)
            survivalRate += 10 * bugsEaten;
            // water cleanliness, max of an additional 25%
            survivalRate += 25 - 0.25 * (GameRunner.pond.getWaterDirtiness());
            // if the frog has a burrow it adds 30%
            if (hasBurrow) {
                survivalRate += 30;
            }

            System.out.println("Frog survival rate: " + survivalRate);
        }

        System.out.print(randNum + " <= " + survivalRate + " = ");
        System.out.println(randNum <= survivalRate);

        // if the random number is within the survival rate it returns true
        return randNum <= survivalRate;
    }

    // this method is only run on living Frog objects
    public void endDay() {

        // if the tadpole reaches the end of the day
        if (stage == 0) {
            // reset the food eaten throughout the day
            algaeEaten = 0;

            // 75% chance for the tadpole to grow
            if ((int) (Math.random() * 4) + 1 < 4) {
                this.grow();
                System.out.println(name + " grew!");
            }
        }
        // if the frog reaches the end of the day
        else {
            // reset the food eaten throughout the day
            bugsEaten = 0;

        }
    }

    // moves frog to the next stage
    public void grow() {
        if (stage < 1) {
            stage++;
        }
        else {
            System.out.println(name + " is fully grown");
        }
    }

    public void startSwimming() {
        // if the Frog is still a tadpole
        if (stage == 0) {
            swimPos.startTadpoleSwimTimer();
        }
        // if the Frog is in stage 1, a frog
        else {
            swimPos.startFrogSwimTimer();
        }
    }

    public void stopSwimming() {
        // if the Frog (object) is still a tadpole
        if (stage == 0) {
            swimPos.stopTadpoleSwimTimer();
        }
        // if the Frog is in stage 1, a frog
        else {
            swimPos.stopFrogSwimTimer();
        }

    }

    public JLabel getDisplayLabel() {
        return displayLabel;
    }

    public int getXPos() {
        return displayLabel.getX();
    }

    public int getYPos() {
        return displayLabel.getY();
    }

    public int getLabelWidth() {
        return displayLabel.getWidth();
    }

    public int getLabelHeight() {
        return displayLabel.getHeight();
    }

    public String getStage() {
        return stages[stage];
    }

    public String getName() {
        return name;
    }

    public boolean hasBurrow() {
        return hasBurrow;
    }

    public boolean hasLilyPad() {
        return hasLilyPad;
    }

    public void feedBugs(int bugs) {
        bugsEaten += bugs;
    }

    public void feedPlants(int plants) {
        algaeEaten += plants;
    }

    public void setBurrow(FrogContainer burrow) {
        hasBurrow = true;
        this.burrow = burrow;
    }

    public FrogContainer getBurrow() {
        return burrow;
    }

    public void setLilyPad(FrogContainer lilyPad) {
        hasLilyPad = true;
        this.lilyPad = lilyPad;
    }

    public FrogContainer getLilyPad() {
        return lilyPad;
    }
}
