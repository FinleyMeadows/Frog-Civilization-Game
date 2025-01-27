/*
    Finley Meadows & Michael Anderson
    12/6/2025
    The Burrow class controls the positioning of frogs within the burrows and
    each burrows visual vs true capacity (3 on screen at a time / 5 total per burrow)
*/

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Burrow {
    // <potential frog pos, isOccupied>
    private Map<Point, Boolean> frogPositions = new HashMap<Point, Boolean>();
    // stores the burrow's image
    private ImageIcon image;
    // stores the number of frogs in the burrow out of 5
    private int numFrogs = 0;

    private JLabel displayLabel;

    public Burrow(JLabel displayLabel) {
        this.displayLabel = displayLabel;
    }


    // returns an open point in the burrow if available, otherwise returns null
    public Point getOpenPosition() {
        for (Map.Entry<Point, Boolean> entry : frogPositions.entrySet()) {
            Point point = entry.getKey();
            boolean isOccupied = entry.getValue();

            if (!isOccupied) {
                frogPositions.put(point, true);
                return point;
            }
        }

        return new Point(0, 0);
    }

    // adds a new point to the burrow map
    public void addPoint(int x, int y) {
        frogPositions.put(new Point(x, y), false);
    }

    public void addFrogs(int additionalFrogs) {
        numFrogs += additionalFrogs;
        if (numFrogs >= 5) {
            System.out.println("This burrow is full");
        }
    }

    public JLabel getDisplayLabel() {
        return displayLabel;
    }

    public int getNumFrogs() {
        return numFrogs;
    }
}
