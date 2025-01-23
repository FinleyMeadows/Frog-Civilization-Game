/*
    Finley Meadows & Michael Anderson
    12/7/2024
    The SwimMovementHandler class calculates the movement for each frog and tadpole swimming in the pond
*/

import javax.swing.*;

public class SwimMovementHandler {

    private final Frog frog;
    private int xPos;
    private int yPos;
    // x 
    private int[] xVelocities;
    private int xVelocityIndex = 0;
    private int maxYVelocity;
    private int yVelocity;
    // starts [(+x), (+y)]
    private int[] movementDirections = {1, 1};
    // timer that calculates a frogs swimming movement
    private Timer frogSwimTimer;
    // timer that calculates a tadpole's swimming movement
    private Timer tadpoleSwimTimer;
    // start on frame 2 / 4 because the timer starts with a 250 milli delay (1 frame)
    private int frame = 2;

    public SwimMovementHandler(Frog frog) {
        this.frog = frog;
        this.xPos = frog.getXPos();
        this.yPos = frog.getYPos();

        // 50-50 swimming up or down, (+) vs (-) y-movement direction
        if (((int) (Math.random() * 2)) == 1) {
            movementDirections[1] *= -1;
        }
    }

    public void initializeFrogValues() {
        // f2: x-change 3 pixels, f3: x-change 2 pixels, f4: x-change 1 pixel
        xVelocities = new int[]{3, 2, 1};

        // y-change can be between 1 and 3 pixels (creates some level of variety)
        maxYVelocity = (int) (Math.random() * 3) + 1;

        // left facing
        if (((int) (Math.random() * 2)) == 1) {
            movementDirections[0] *= -1;
            frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/LeftSwimmingFrog/Frame1.png"));
        }
        // right facing
        else {
            frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/RightSwimmingFrog/Frame1.png"));
        }
        // adjusts the size of the display label
        frog.getDisplayLabel().setSize(frog.getDisplayLabel().getPreferredSize());
    }

    public void initializeTadPoleValues() {
        // f2 & f4: x-change 1 pixel
        xVelocities = new int[]{2};

        // y-change of 1 pixel
        maxYVelocity = 1;

        // left facing
        if (((int) (Math.random() * 2)) == 1) {
            movementDirections[0] *= -1;
            frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/LeftSwimmingTadpole/Frame1.png"));
        }
        // right facing
        else {
            frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/RightSwimmingTadpole/Frame1.png"));
        }
        // adjusts the size of the display label
        frog.getDisplayLabel().setSize(frog.getDisplayLabel().getPreferredSize());
    }

    public void createFrogSwimTimer() {
        // Frame 1: no change
        // Frame 2: x change of xVelocities[0] pixels & y change of 1 pixel
        // Frame 3: x change of xVelocities[1] pixels & possible y change of 1 pixels
        // Frame 4: x change of xVelocities[2] pixel & possible y change of 1 pixels
        // Repeat
        
        // this starts after 250 milliseconds which means this starts on the second frame
        frogSwimTimer = new Timer(250, e -> {
            switch (frame) {
                case 1:
                    // no movements are made on frame 1

                    // resets the animation
                    switchFrame();
                    // resets the xVelocity
                    xVelocityIndex = 0;
                    // resets the yVelocity
                    yVelocity = maxYVelocity;

                    break;

                case 2:
                    switchFrame();

                    // y change of 1 pixel:
                    yPos += movementDirections[1];
                    // slows down for next movement
                    yVelocity--;
                    // checks and deals with border violations
                    checkBorders();

                    // x change of 3 pixels:
                    // divides the total change in x into 1 pixel increments to check for borders each change
                    incrementXVelocity();

                    break;

                case 3, 4:
                    // possible y change of 1 pixel for frames 3 & 4
                    if (yVelocity > 0) {
                        yPos += movementDirections[1];
                        checkBorders();
                        yVelocity--;
                    }

                    // x change of 2 or 1 pixel(s)
                    incrementXVelocity();

                    break;

                default:
                    // this point should never be reached
                    System.out.println("You messed something up");

                    break;
            }

            // updates the position of the label
            frog.getDisplayLabel().setLocation(xPos, yPos);

            // cycle frames 1-4
            frame++;
            if (frame == 5) {
                frame = 1;
            }
        });
    }

    public void createTadpoleSwimTimer() {
        tadpoleSwimTimer = new Timer(250, e -> {

            switch (frame) {
                case 1, 3:
                    // switches the tadpole to its contracted ImageIcon
                    switchFrame();

                    // no movement happens on these frames because the tadpole is contracted

                    // resets the x-velocity
                    xVelocityIndex = 0;

                    break;

                case 2, 4:
                    // switches the tadpole to its normal ImageIcon
                    switchFrame();

                    // y-change of (maxYVelocity) pixels
                    // increments the y-velocity
                    for (int i = 0; i < maxYVelocity; i++) {
                        // (+)  or (-) 1 pixel
                        yPos += movementDirections[1];

                        // checks for border violations
                        checkBorders();
                    }

                    // x-change of (x-velocity) pixels
                    incrementXVelocity();

                    break;
            }

            // updates the position of the label
            frog.getDisplayLabel().setLocation(xPos, yPos);

            // cycle frames 1-4
            frame++;
            if (frame == 5) {
                frame = 1;
            }
        });
    }

    public void switchFrame() {
        // if 'this' is a tadpole
        if (frog.getStage().equals("Tadpole")) {
            // if facing left
            if (movementDirections[0] == 1) {
                // frame 1 or 3
                if (frame % 2 == 1) {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/RightSwimmingTadpole/Frame1.png"));
                }
                // frame 2 or 4
                else  {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/RightSwimmingTadpole/Frame2.png"));
                }
            }
            // if facing left
            else {
                // frame 1 or 3
                if (frame % 2 == 1) {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/LeftSwimmingTadpole/Frame1.png"));
                }
                // frame 2 or 4
                else {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/LeftSwimmingTadpole/Frame2.png"));
                }
            }
        }
        // if 'this' is a frog
        else {
            // if facing right
            if (movementDirections[0] == 1) {
                // frame 1
                if (frame == 1) {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/RightSwimmingFrog/Frame1.png"));
                }
                // frame > 1
                else {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/RightSwimmingFrog/Frame2.png"));
                }
            }
            // if facing left
            else {
                // frame 1
                if (frame == 1) {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/LeftSwimmingFrog/Frame1.png"));
                }
                // frame > 1
                else {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/LeftSwimmingFrog/Frame2.png"));
                }
            }
        }
    }

    // divides the total change in x into 1 pixel increments to check for borders each change
    private void incrementXVelocity() {
        for (int i = 0; i < xVelocities[xVelocityIndex]; i++) {
            // changes the xPos by 1 pixel
            xPos += xVelocities[xVelocityIndex] * movementDirections[0];
            // checks for border violations
            checkBorders();
        }

        // slows down for the next set of movements
        xVelocityIndex += 1;
    }
    
    // checks if the position is out of bounds and corrects the position if needed
    private void checkBorders() {
        if (!inBounds()) {
            char border = getBorderContact();

            switch (border) {
                case 'n':
                    // brings label back in bounds
                    yPos = 204;
                    // starts moving down
                    movementDirections[1] *= -1;

                    break;

                case 'e':
                    // flips the image left
                    flipImage();
                    // brings label back in bounds
                    xPos = Pond.FRAME_WIDTH - frog.getLabelWidth() - 1;
                    // starts moving left
                    movementDirections[0] *= -1;

                    break;

                case 's':
                    // brings label back in bounds
                    yPos = 510 - frog.getLabelHeight() - 1;
                    // starts moving up
                    movementDirections[1] *= -1;

                    break;

                case 'w':
                    // flips the image right
                    flipImage();
                    // brings label back in bounds
                    xPos = 1;
                    // starts moving right
                    movementDirections[0] *= -1;

                    break;

                default:
                    // do nothing

                    break;
            }
        }
    }

    // returns the 'n', 'e', 's', or 'w' border being violated
    private char getBorderContact() {
        // west border (left wall)
        if (xPos < 0) {
            return 'w';
        }
        // north border (ceiling)
        else if (yPos < 203) {
            return 'n';
        }
        // east border (right wall)
        else if (xPos + frog.getLabelWidth() > Pond.FRAME_WIDTH) {
            return 'e';
        }
        // south border (floor)
        else if (yPos + frog.getLabelHeight() > 510) {
            return 's';
        }
        // still in bounds
        else {
            return 'x';
        }
    }


    // flips the swimming frog or tadpole gif over the y-axis
    private void flipImage() {
        char border = getBorderContact();

        // makes it face left
        if (border == 'e') {
            // if it's a tadpole
            if (frog.getStage().equals("Tadpole")) {
                // frame 1 or 3
                if (frame == 1) {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/LeftSwimmingTadpole/Frame1.png"));
                }
                // frame 2 or 4
                else {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/LeftSwimmingTadpole/Frame2.png"));
                }
            }
            // if it's a frog
            else {
                // frame 1
                if (frame == 1) {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/LeftSwimmingFrog/Frame1.png"));
                }
                // frame > 1
                else {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/LeftSwimmingFrog/Frame2.png"));
                }
            }
        }
        // makes it face right
        if (border == 'w') {
            // if it's a tadpole
            if (frog.getStage().equals("Tadpole")) {
                // frame 1 or 3
                if (frame == 1) {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/RightSwimmingTadpole/Frame1.png"));
                }
                // frame 2 or 4
                else {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/RightSwimmingTadpole/Frame2.png"));
                }
            }
            // if it's a frog
            else {
                // frame 1
                if (frame == 1) {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/RightSwimmingFrog/Frame1.png"));
                }
                // frame > 1
                else {
                    frog.getDisplayLabel().setIcon(new ImageIcon("Pictures/RightSwimmingFrog/Frame2.png"));
                }
            }
        }
    }

    // returns true if the frog is out of bounds
    private boolean inBounds() {
        return xPos >= 0 && yPos >= 203 && xPos + frog.getLabelWidth() <= Pond.FRAME_WIDTH &&
                yPos + frog.getLabelHeight() <= 510;
    }

    public void startFrogSwimTimer() {
        // sets the x and y velocities for the frog
        initializeFrogValues();
        // creates a timer to control the frogs movement
        createFrogSwimTimer();
        // starts the timer; starts frog swimming
        frogSwimTimer.start();
    }

    public void stopFrogSwimTimer() {
        frogSwimTimer.stop();
    }

    public void startTadpoleSwimTimer() {
        // sets the x and y velocities for the tadpole
        initializeTadPoleValues();
        // creates a timer to control the tadpole's movement
        createTadpoleSwimTimer();
        // starts the timer; starts tadpole swimming
        tadpoleSwimTimer.start();
    }

    public void stopTadpoleSwimTimer() {
        tadpoleSwimTimer.stop();
    };
}
