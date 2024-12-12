import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SwimMovementHandler extends Frog {

    private int xPos = super.getX();
    private int yPos = super.getY();
    // 1 / xChange
    private int xChange;
    // 'r', 'l'
    private char[] directions = {'r', 'l'};
    private char currentDirection;
    // # of pixels the frog will move
    private int maxVelocity = 3;
    // timer that moves the frog in its direction
    private Timer movementTimer;

    public SwimMovementHandler() {
        // randomizes the xChange [-3, 3]
        this.xChange = (int) (Math.random() * -7) + 3;

        // gets a random direction
        this.currentDirection = directions[(int) (Math.random() * 2)];
        // if moving left it inverts the velocity
        if (currentDirection == 'l') {
            maxVelocity *= -1;
        }
    }

    private int getRandNum(int range, int startingValue) {
        return (int) (Math.random() * range) + startingValue;
    }

    // this method must be run after the frog is added to the water
    public void move() {

        movementTimer = new Timer(250, new ActionListener() {
            int currentVelocity = maxVelocity;
            int frame = 2;

            @Override
            public void actionPerformed(ActionEvent e) {
                // move the max distance on frame 2
                if (frame == 2) {
                    xPos += currentVelocity;

                    // out of bounds
                    if (!inBounds()) {
                        getBorderContact();
                    }
                    // in bounds
                }

                // 3 -> 2 -> 1 -> 0 repeat
                currentVelocity--;
                if (currentVelocity == -1) {
                    currentVelocity = 3;
                }
                // cycles between 4 frames
                frame++;
                if (frame == 5) {
                    frame = 1;
                }
            }
        });
        movementTimer.start();
    }

    // repositions the out-of-bounds JLabel to the edge of the border
    private void stickToBorder() {

    }

    // returns the 'n', 'e', 's', or 'w' for the border being hit
    private char getBorderContact() {
        // west border (left wall)
        if (xPos < 0) {
            return 'w';
        }
        // north border (ceiling)
        else if (yPos < 0) {
            return 'n';
        }
        // east border (right wall)
        else if (xPos + super.getDisplayLabel().getWidth() > Pond.FRAME_WIDTH) {
            return 'e';
        }
        // south border (floor)
        else if (yPos + super.getDisplayLabel().getHeight() > Pond.FRAME_HEIGHT) {
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
            if (super.getDisplayLabel().getWidth() < 30) {
                // TODO: create an actual flipped image of the tadpole
                super.getDisplayLabel().setIcon(new ImageIcon("Animations/Tadpole.gif"));
            }
            // if it's a frog
            else {
                // TODO: create an actual flipped image of the swimming frog
                super.getDisplayLabel().setIcon(new ImageIcon("Animations/SwimmingFrog.gif"));
            }
        }
        // makes it face right
        if (border == 'w') {
            // if it's a tadpole
            if (super.getDisplayLabel().getWidth() < 30) {
                super.getDisplayLabel().setIcon(new ImageIcon("Animations/Tadpole.gif"));
            }
            // if it's a frog
            else {
                super.getDisplayLabel().setIcon(new ImageIcon("Animations/SwimmingFrog.gif"));
            }
        }
    }

    // returns true if the point is in bounds and false if not
    private boolean inBounds() {
        return xPos >= 0 && yPos >= 0 && xPos + super.getDisplayLabel().getWidth() <= Pond.FRAME_WIDTH &&
                yPos + super.getDisplayLabel().getHeight() <= Pond.FRAME_HEIGHT;
    }

    private void bounce() {

    }
}
