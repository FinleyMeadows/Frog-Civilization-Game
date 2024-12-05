/*
    Finley Meadows & Michael Anderson
    12/2/2024
    The Pond class will run all the functions of the game
*/

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Pond extends JFrame implements MouseListener {

   
    private final int FRAME_WIDTH = 700;
    private final int FRAME_HEIGHT = 700;

    // - - - C O M P O N E N T S - - - //

    // background labels
    private JLabel sky = new JLabel(new ImageIcon("Pictures/Sky.png"));
    private JLabel water = new JLabel(new ImageIcon("Pictures/Water.png"));
    private JLabel ground = new JLabel(new ImageIcon("Pictures/Ground.png"));

    // frog Images
    private ImageIcon idleFrog = new ImageIcon("Animations/IdleFrog.gif");

    // holds every component in the game
    private JLayeredPane layeredPane;

    // - - - M A P S - - - //

    // holds every type of item and its quantity
    private Map<String, Integer> items = new HashMap<String, Integer>();

    // - - - A R R A Y L I S T S - - - //

    // stores all the display labels
    private ArrayList<JLabel> displayLabels = new ArrayList<JLabel>();

    // contains all the functions of the game
    public Pond() {
        // IMPORTANT: this = the JFrame

        // basic JFrame methods
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Sets layout so that the pack() method works
        this.setLayout(new BorderLayout());
        this.setVisible(true);

        // JLayeredPane methods
        layeredPane = new JLayeredPane();
        // The pack() method resizes to preferred sizes not normal sizes
        layeredPane.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        // adds the layered pane to the center of the JFrame
        this.add(layeredPane, BorderLayout.CENTER);
        // resizes the JFrame to the size of layeredPane
        this.pack();
        
        // adds the sky to the background
        sky.setName("Sky");
        sky.setSize(sky.getPreferredSize());
        sky.addMouseListener(this);
        layeredPane.add(sky, Integer.valueOf(0));

        // adds the water to the background
        water.setName("Water");
        water.setSize(water.getPreferredSize());
        water.setLocation(0, sky.getHeight());
        water.addMouseListener(this);
        layeredPane.add(water, Integer.valueOf(0));

        // adds the ground to the background
        ground.setName("Ground");
        ground.setSize(ground.getPreferredSize());
        ground.setLocation(0, water.getY() + water.getHeight());
        ground.addMouseListener(this);
        layeredPane.add(ground, Integer.valueOf(0));

        // creates every type of item and sets its value
        initializeItems();
        // creates a display label for every kind of item and adds it to the frame
        initializeItemDisplayLabels();
    }

    // - - - I T E M  M A N A G E M E N T - - - //

    // initializes each item and its quantity
    public void initializeItems() {
        // number of frogs, tadpoles, bugs, plant food, and hours spent in total
        items.put("Frogs", 4);
        items.put("Tadpoles", 0);
        items.put("Bugs", 0 /*TBD*/);
        items.put("Plant Food", 0 /*TBD*/);
        items.put("Total Hours Spent", 0);
    }

    // creates a display label for each item and positions them accordingly in the frame
    public void initializeItemDisplayLabels() {
        int yPos = 0;
        
        // creates a display label for each item in the items map<String, Integer>
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            String name = entry.getKey();
            int quantity = entry.getValue();
            
            // customizing the display label
            JLabel displayLabel = new JLabel();
            displayLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            displayLabel.setText(name + ": " + quantity);
            displayLabel.setSize(displayLabel.getPreferredSize());
            displayLabel.setLocation(0, yPos);

            // adds the displayLabel to the displayLabels ArrayList for future access
            displayLabels.add(displayLabel);
            
            // adds it to the layeredPane
            layeredPane.add(displayLabel, Integer.valueOf(1));
            
            // moves the yPos down below the previous label
            yPos += displayLabel.getHeight();
        }
    }

    // updates all display labels whenever necessary
    public void updateDisplayLabels() {
        // updates the text of all the display labels to match their actual quantities in the map
        for (JLabel displayLabel : displayLabels) {
            String text = displayLabel.getName();
            String itemName = text.substring(0, text.indexOf(":"));

            // resets the label
            displayLabel.setText(itemName + ": " + items.get(itemName));
            displayLabel.setSize(displayLabel.getPreferredSize());
        }
    }


    // kinda just a placeholder method for now
    private void openActionMenu(JLabel label) {
        System.out.println(label.getName() + " action menu was opened");
        // adds a frog to the screen for fun
        addFrog();
    }

    // just a method for fun
    private void addFrog() {
        int xPos = (int) (Math.random() * 685);
        int yPos = (int) (Math.random() * 689);

        // resets the gif every time a new frog is created to de-sync the gif cycles
        Image rerenderedImage = idleFrog.getImage().getScaledInstance(690, 506, Image.SCALE_REPLICATE);
        JLabel frog = new JLabel(new ImageIcon(rerenderedImage));
        frog.setSize(700, 700);
        frog.setHorizontalAlignment(SwingConstants.CENTER);
        frog.setVerticalAlignment(SwingConstants.CENTER);
        // frog.setLocation(xPos, yPos);
        layeredPane.add(frog, Integer.valueOf(2));
    }


    // mandatory MouseListener Methods

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // gets the component pressed
        Object object = e.getSource();

        // if the object is a JLabel
        if (object instanceof JLabel temp) {
            // if the JLabel being entered has a name
            // (all JLabels with mouseListeners added should be named)
            if (temp.getName() != null) {
                String name = temp.getName();
                // if that JLabel is a background label (the sky, water, or ground)
                if (name.equals("Sky") || name.equals("Water") || name.equals("Ground")) {
                    // opens the action menu correlated with the label
                    openActionMenu(temp);
                }

            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // gets the component pressed
        Object object = e.getSource();

        // if the object is a JLabel
        if (object instanceof JLabel temp) {
            // if the JLabel being entered has a name
            // (all JLabels with mouseListeners added should be named)
            if (temp.getName() != null) {
                String name = temp.getName();
                // if that JLabel is a background label (the sky, water, or ground)
                if (name.equals("Sky") || name.equals("Water") || name.equals("Ground")) {
                    temp.setBorder(new LineBorder(Color.YELLOW, 1));
                }

            }
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // gets the component pressed
        Object object = e.getSource();

        // if the object is a JLabel
        if (object instanceof JLabel temp) {
            // removes the border after exiting
            temp.setBorder(null);
        }
    }
}
