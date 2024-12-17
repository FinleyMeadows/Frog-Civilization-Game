import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SwimMovementHandler {

    private Frog frog;
    private int xPos;
    private int yPos;
    // x 
    private int[] xVelocities = {3, 2, 1};
    private int xVelocityIndex = 0;
    private final int maxYVelocity;
    private int yVelocity;
    // starts [(+x), (+y)]
    private int[] movementDirections = {1, 1};
    // timer that moves the frog in its direction
    private Timer movementTimer;
    private int frame = 2;

    public SwimMovementHandler(Frog frog) {
        this.frog = frog;
        this.xPos = frog.getXPos();
        this.yPos = frog.getYPos();
        this.maxYVelocity = (int) (Math.random() * 3) + 1;



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

        // 50-50 up or down
        if (((int) (Math.random() * 2)) == 1) {
            movementDirections[1] *= -1;
        }
    }

    // this method must be run after the frog is added to the water
    public void moveFrog() {
        // Frame 1: no change
        // Frame 2: x change of xVelocities[0] pixels & y change of 1 pixel
        // Frame 3: x change of xVelocities[1] pixels & possible y change of 1 pixels
        // Frame 4: x change of xVelocities[2] pixel & possible y change of 1 pixels
        // Repeat
        
        // this starts after 250 milliseconds which means this starts on the second frame
        movementTimer = new Timer(250, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                switch (frame) {
                    case 1:
                        // no movement are made on frame 1

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
            }
        });
        movementTimer.start();
    }

    public void switchFrame() {
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

    // divides the total change in x into 1 pixel increments to check for borders each change
    private void incrementXVelocity() {
        for (int i = 0; i < xVelocities[xVelocityIndex]; i++) {
            // changes the xPos by 1 pixel
            xPos += xVelocities[xVelocityIndex] * movementDirections[0];
            // check for border violations
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

    // returns the 'n', 'e', 's', or 'w' for the border being hit
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
                // TODO: create an actual flipped image of the tadpole
                frog.getDisplayLabel().setIcon(new ImageIcon("Animations/Tadpole.gif"));
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
                frog.getDisplayLabel().setIcon(new ImageIcon("Animations/Tadpole.gif"));
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

    // returns true if the point is in bounds and false if not
    private boolean inBounds() {
        return xPos >= 0 && yPos >= 203 && xPos + frog.getLabelWidth() <= Pond.FRAME_WIDTH &&
                yPos + frog.getLabelHeight() <= 510;
    }
}
