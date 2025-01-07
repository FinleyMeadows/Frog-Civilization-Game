import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Burrow {
    // <potential frog pos, isOccupied>
    private Map<Point, Boolean> frogPositions = new HashMap<Point, Boolean>();
    // stores the burrow's image
    private ImageIcon image;

    public Burrow(ImageIcon image) {
        this.image = image;
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

    public ImageIcon getImage() {
        return image;
    }
}
