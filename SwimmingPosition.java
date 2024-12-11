import javax.swing.Timer;

public class Position extends Frog {

    private int xPos;
    private int yPos;
    // rise / 1
    private int slope;
    // 'r', 'l'
    private char[] directions = {'r', 'l'};
    private char direction;
    // # of pixels the frog will move
    private int maxVelocity = 3;
    // timer that moves the frog in its direction
    private Timer movementTimer;

    public SwimmingPosition() {
        // spawn the frog somewhere in the middle of the pond
        this.xPos = getRandNum(350, 175);
        this.yPos = getRandNum(270, 200);
        // randomizes the slope [-3, 3]
        this.slope = (int) (Math.random() * -4) + 3;
        // gets a random direction
        this.direction = directions[(int) (Math.random() * 2)];
        // if moving left it inverts the velocity
        if (direction == 'l') {
            maxVelocity *= -1;
        }
    }

    private int getRandNum(int range, int startingValue) {
        return (int) (Math.random() * range) + startingValue;
    }

    // this method must be run after the frog is added to the water
    public void move() {
        int currentVelocity = maxVelocity;
        int yMovementsRemaining = slope;

        // moves the frog 3 pixels, 2 pixels, and then 1 pixel in the movement direction
        movementTimer = new Timer(250, e -> {
            if (yMovementsRemaining > 0) {
                for (int pixels = currentVelocity; pixels >= 0; pixels--) {
                    if (isInBounds(xPos, yPos + 1)) {
                        yPos += 1;
                    }
                    else {

                    }
                }
            }
        });
    }

    // returns the 'n', 'e', 's', or 'w' for the border being hit
    private char getBorderContact() {

    }

    // returns true if the point is in bounds and false if not
    private boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 && x <= Pond.FRAME_WIDTH && y <= Pond.FRAME_HEIGHT;
    }

    private void bounce() {

    }
}
