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
import java.util.Map;
import java.util.TreeMap;

public class Pond extends JFrame implements MouseListener {

    // - - - V A R I A B L E S - - - //
    public static final int FRAME_WIDTH = 700;
    public static final int FRAME_HEIGHT = 700;

    // holds the current hour of the day
    private int time = 8;
    // the time when the day ends
    private int bedtime = 8;
    private String amOrPm = "am";
    // 1-100, 1 being nasty, 100 being crystal clear
    public static int waterCleanliness = 100;
    // times all the background effects
    private Timer backgroundEffectsTimer;

    // - - - C O M P O N E N T S - - - //

    // background labels
    private JLabel sky = new JLabel(new ImageIcon("Pictures/Sky.png"));
    private JLabel water = new JLabel(new ImageIcon("Pictures/Water.png"));
    private JLabel ground = new JLabel(new ImageIcon("Pictures/Ground.png"));

    // time variables and components
    private JLabel timeLabel = new JLabel();
    private JLabel bedtimeLabel = new JLabel();
    private JLabel nextDayLabel = new JLabel();

    // holds every component in the game
    private JLayeredPane layeredPane;

    // - - - M A P S - - - //

    // holds every type of item and its quantity
    private Map<String, Integer> items = new TreeMap<String, Integer>();
    // stores all the frogs and their display labels
    private Map<JLabel, Frog> frogs = new TreeMap<JLabel, Frog>();

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

        // TODO: remove or repurpose this once done with testing
        spawnFrogs(4);

        // creates each chunk of the background
        createBackground();
        // creates every type of item and sets its value
        initializeItems();
        // creates a display label for every kind of item and adds it to the frame
        initializeItemDisplayLabels();
        // create the time and time functions in the top right of the frame
        initializeTimeComponents();
        // starts the bubble animations in the water
        startBackgroundEffects();
    }

    public int spawnFrogs(int n) {
        if (n == 0) {
            return 0;
        }
        else {
            Frog swimmingFrog = new Frog();
            swimmingFrog.growFrog();
            swimmingFrog.growFrog();
            JLabel frogLabel = swimmingFrog.getDisplayLabel();
            layeredPane.add(frogLabel, Integer.valueOf(3));

            return spawnFrogs(n - 1);
        }
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
            displayLabel.setFont(new Font("Serif", Font.PLAIN, 20));
            displayLabel.setText(name + ": " + quantity);
            displayLabel.setSize(displayLabel.getPreferredSize().width + 10, displayLabel.getPreferredSize().height);
            displayLabel.setLocation(0, yPos);

            // adds the displayLabel to the displayLabels ArrayList for future access
            displayLabels.add(displayLabel);

            // adds it to the layeredPane
            layeredPane.add(displayLabel, Integer.valueOf(1));

            // moves the yPos down below the previous label
            yPos += displayLabel.getHeight();
        }
    }

    // updates all resource display labels whenever necessary
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


    // - - - T I M E  M A N A G E M E N T - - - //


    public void initializeTimeComponents() {
        int width = 110;

        // formats the time label
        timeLabel.setFont(new Font("Dialog", Font.PLAIN, 30));
        // updates the text in the time label
        updateTime();
        timeLabel.setSize(width, timeLabel.getPreferredSize().height);
        timeLabel.setLocation(FRAME_WIDTH - width, 0);
        // adds it to the frame
        layeredPane.add(timeLabel, Integer.valueOf(1));

        // formats the bedtime label
        bedtimeLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        bedtimeLabel.setText("Bedtime: " + bedtime + ":00pm");
        bedtimeLabel.setSize(width, timeLabel.getPreferredSize().height);
        bedtimeLabel.setLocation(FRAME_WIDTH - width, timeLabel.getY() + timeLabel.getHeight());
        // adds it to the frame
        layeredPane.add(bedtimeLabel, Integer.valueOf(1));

        // formats next day button
        nextDayLabel.setName("Next Day Label");
        nextDayLabel.setFont(new Font("Serif", Font.PLAIN, 20));
        nextDayLabel.setText("Next Day");
        // moves to next day when clicked
        nextDayLabel.addMouseListener(this);
        nextDayLabel.setSize(width, timeLabel.getPreferredSize().height);
        nextDayLabel.setLocation(FRAME_WIDTH - width, bedtimeLabel.getY() + bedtimeLabel.getHeight());
        // adds it to the frame
        layeredPane.add(nextDayLabel, Integer.valueOf(1));
    }

    // updates the time label
    public void updateTime() {
        // time == 12am or pm
        if (time == 12) {
            if (amOrPm.equals("am")) {
                timeLabel.setText(time + ":00pm");
            }
            else {
                timeLabel.setText(time + ":00am");
            }
        }
        // time != 12am or pm
        else {
            if (time > 12) {
                // Ex: time = 13pm -> 1pm
                time -= 12;
                // flip-flop am and pm
                if (amOrPm.equals("am")) {
                    amOrPm = "pm";
                }
                else {
                    amOrPm = "am";
                }
            }
            timeLabel.setText(time + ":00" + amOrPm);
        }
    }


    // - - - B A C K G R O U N D - - - //

    public void createBackground() {
        // adds the SKY to the background
        sky.setName("Sky");
        sky.setSize(sky.getPreferredSize());
        sky.addMouseListener(this);
        layeredPane.add(sky, Integer.valueOf(0));

        // adds the WATER to the background
        water.setName("Water");
        water.setSize(water.getPreferredSize());
        water.setLocation(0, sky.getHeight());
        water.addMouseListener(this);
        layeredPane.add(water, Integer.valueOf(0));

        // adds the GROUND to the background
        ground.setName("Ground");
        ground.setSize(ground.getPreferredSize());
        ground.setLocation(0, water.getY() + water.getHeight());
        ground.addMouseListener(this);
        layeredPane.add(ground, Integer.valueOf(0));

        // adds rocks to the background
        JLabel rocks = new JLabel(new ImageIcon("Pictures/Rockbed0.png"));
        rocks.setSize(rocks.getPreferredSize());
        rocks.setLocation(0, 360);
        layeredPane.add(rocks, Integer.valueOf(1));
    }

    private void startBackgroundEffects() {
        backgroundEffectsTimer = new Timer(1000, e -> {
            // randomly spawns bubbles
            int bubbleChance = (int) (Math.random() * 15) + 1;
            if (bubbleChance == 15) {
                int bubbleX = (int) (Math.random() * 668);
                // plays the bubble animations
                playAnimation(new ImageIcon("Animations/BubbleGif.gif"), bubbleX,
                        200, 32, 300, 3375);
            }
        });
        backgroundEffectsTimer.start();
    }

    // - - - A N I M A T I O N - - - //

    private void playAnimation(ImageIcon gif, int x, int y, int width, int height, int duration) {
        // does a weird work-around to reset the gif each time a new one is created
        Image rerenderedImage = gif.getImage().getScaledInstance(width, height, Image.SCALE_REPLICATE);
        JLabel animation = new JLabel(new ImageIcon(rerenderedImage));

        animation.setSize(width, height);
        animation.setLocation(x, y);

        // add the animation to the layeredPane
        animation.setVisible(true);
        layeredPane.add(animation, Integer.valueOf(4));

        // creates a timer to control the animation's duration
        Timer animationTimer = new Timer(duration, e -> {
            // stop the animation and clean up
            animation.setVisible(false);
            layeredPane.remove(animation);
            layeredPane.revalidate();
            layeredPane.repaint();
        });

        // ensure the timer runs only once
        animationTimer.setRepeats(false);
        animationTimer.start();
    }

    // - - - M O U S E L I S T E N E R  M E T H O D S - - - //

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
                    // TODO: open the action menu correlated with the label
                }
                else if (name.equals("Next Day Label")) {
                    // TODO: go to next day
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
                temp.setBorder(new LineBorder(Color.YELLOW, 1));
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
