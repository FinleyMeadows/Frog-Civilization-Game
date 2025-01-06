import javax.swing.*;

public class Frog {

    // stages of a frog's life cycle
    private final String[] stages = {"Egg", "Tadpole", "Frog"};
    // holds the current stage of the frog
    private int currentStage;
    // the name of the frog
    private String name = getRandomName();
    // each tadpole needs 3 pieces of algae each day
    private int algaeEaten;
    // each frog needs 3 bugs per day
    private int bugsEaten;
    // each frog needs a burrow
    private boolean hasBurrow;
    // display label for the egg, tadpole, or frog
    private JLabel displayLabel = new JLabel();
    // swims the frog/tadpole around when in the water
    private SwimMovementHandler swimPos;

    public Frog() {
        // starts as an egg (stage 0)
        this.currentStage = 0;
        // hasn't eaten anything yet
        this.algaeEaten = 0;
        this.bugsEaten = 0;
        // IDEA: frogs get auto-assigned a burrow if you have space
        this.hasBurrow = false;
        displayLabel.setName("Frog Label");
        randomizeCoords();
        swimPos = new SwimMovementHandler(this);
    }

    private String getRandomName() {
        String[] names = {"Lily", "Clover", "Fern", "Pebble", "Willow",
                "Mossy", "Dewdrop", "Puddle", "Meadow", "Rainy",
                "Pickle", "Jellybean", "Tater Tot", "Muffin", "Cupcake",
                "Sprout", "Lemon", "Olive", "Pistachio", "Gingersnap",
                "Hoppy", "Bouncer", "Ribbit", "Wiggles", "Toadette",
                "Leapster", "Zippy", "Quibbles", "Splashy", "Flip-Flop",
                "Frodo", "Glimmer", "Toadsworth", "Finley", "Froglina",
                "Mikey", "Skippy", "Doodle", "Liam", "Luca", "Michael"
        };

        return names[(int) (Math.random() * names.length)];
    }

    public void startSwimming() {
        swimPos.moveFrog();
    }

    public void stopSwimming() {
    }

    private void randomizeCoords() {
        int x = (int) (Math.random() * (Pond.FRAME_WIDTH - displayLabel.getWidth()));
        int y = (int) (Math.random() * (307 - displayLabel.getHeight())) + 203;
        displayLabel.setLocation(x, y);
    }

    public boolean isAlive() {
        double survivalRate;
        int randNum = (int) (Math.random() * 100) + 1;

        // egg
        if (currentStage == 0) {
            // TODO: find a good survival rate for eggs
            survivalRate = 2; // %
        }
        // tadpole
        else if (currentStage == 1) {
            // TODO: find out what factors determine a tadpole's survival rate
            survivalRate = 1;
            survivalRate += 33 * algaeEaten;
        }
        // frog
        else {
            // base survival rate of 15%
            survivalRate = 15;
            // 10% for each bug eaten (max of 3 bugs)
            survivalRate += 10 * bugsEaten;
            // water cleanliness, max of an additional 25%
            survivalRate += 25 - 0.25 * (100 - Pond.waterCleanliness);
            // if the frog has a burrow it adds 30%
            if (hasBurrow) {
                survivalRate += 30;
            }
        }

        // if the random number is within the survival rate it returns true
        return randNum <= survivalRate;
    }

    // moves frog to the next stage
    public void growFrog() {
        currentStage++;
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
        return stages[currentStage];
    }

    public String getName() {
        return name;
    }
}
