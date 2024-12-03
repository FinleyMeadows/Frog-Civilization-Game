/*
    Finley Meadows & Michael Anderson
    12/2/2024
    The Pond class will run all the functions of the game
*/

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Pond extends JFrame {

    // Frame's width
    private final int FRAME_WIDTH = 700;
    // Frame's height
    private final int FRAME_HEIGHT = 700;

    // - - - C O M P O N E N T S - - - //

    // Holds every component in the game
    private JLayeredPane layeredPane;

    // - - - M A P S - - - //

    // holds every type of item and its quantity
    private Map<String, Integer> items = new HashMap<String, Integer>();

    // - - - A R R A Y L I S T S - - - //

    // Stores all the display labels
    private ArrayList<JLabel> displayLabels = new ArrayList<JLabel>();

    // Contains all the functions of the game
    public Pond() {
        // IMPORTANT: this = the JFrame

        // Basic JFrame methods
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Sets layout so that the pack() method works
        this.setLayout(new BorderLayout());
        this.setVisible(true);

        // JLayeredPane methods
        layeredPane = new JLayeredPane();
        // The pack() method resizes to preferred sizes not normal sizes
        layeredPane.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        // Adds the layered pane to the center of the JFrame
        this.add(layeredPane, BorderLayout.CENTER);
        // Resizes the JFrame to the size of layeredPane
        this.pack();
    }

    // - - - I T E M  M A N A G E M E N T - - - //

    // Initializes each item and its quantity
    public void initializeItems() {
        // Number of frogs, tadpoles, bugs, plant food, and hours spent in total
        items.put("Frogs", 4);
        items.put("Tadpoles", 0);
        items.put("Bugs", 0 /*TBD*/);
        items.put("Plant Food", 0 /*TBD*/);
        items.put("Total Hours Spent", 0);
    }

    // Creates a display label for each item and positions them accordingly in the frame
    public void initializeItemDisplayLabels() {
        // Creates a display label for each item in the items map<String, Integer>
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            String name = entry.getKey();
            int quantity = entry.getValue();

            JLabel displayLabel = new JLabel();
            displayLabel.setText(name + ": " + quantity);
            // TODO: Figure out sizing and positioning
            // displayLabel.setSize();
            // displayLabel.setLocation();

            // Adds the displayLabel to the displayLabels ArrayList for future access
            displayLabels.add(displayLabel);
        }
    }

    // Updates all display labels whenever necessary
    public void updateDisplayLabels() {
        // Updates the text of all the display labels to match their actual quantities in the map
        for (JLabel displayLabel : displayLabels) {
            String text = displayLabel.getName();
            String itemName = text.substring(0, text.indexOf(":"));

            // Resets the label
            displayLabel.setText(itemName + ": " + items.get(itemName));
        }
    }
}
