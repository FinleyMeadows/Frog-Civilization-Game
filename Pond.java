/*
    Finley Meadows & Michael Anderson
    12/2/2024
    The Pond class will run all the functions of the game
*/

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

    // holds every component in the game
    private JLayeredPane layeredPane;
    /*
        - Layer 0: sky, water, ground labels
        - Layer 1: rock bed label, all resource and time display labels, bubbles
        - Layer 2: lily pads, bubbles
        - Layer 3: Swimming frogs, idle frogs on lily pads, bubbles
        - Layer 4: frog name labels, bubbles
        - Layer 5: action menu
        - Layer 6: menu button
    */

    // background components
    private JLabel sky = new JLabel(loadImage("Pictures/Sky.png"));
    private JLabel water = new JLabel(loadImage("Pictures/Water.png"));
    private JLabel ground = new JLabel(loadImage("Pictures/Ground.png"));

    // holds all the burrows in a grid layout
    private JPanel burrowPanel = new JPanel();

    // time variables and components
    private JLabel timeLabel = new JLabel();
    private JLabel bedtimeLabel = new JLabel();
    private JLabel nextDayButton = new JLabel();

    // label that displays the frogs name above its image
    private JLabel frogNameLabel;
    // keeps the position of the frog's name synced with the position of the frog's label
    private Timer nameUpdater;

    // button (actually a JLabel) used to open the menu
    private JLabel menuButton = new JLabel();
    // the actual menu (this will be on the layer below the button so the button remains visible)
    private JPanel menu = new JPanel();
    // shows the population / capacity of a burrow
    private JTextArea populationTextArea;
    // stores the component that was last entered
    // this aims to solve the problem where when the mouse is inside the populationTextArea it thinks it's exiting
    // the burrowLabel when really it's still inside it, the happens because the populationTextArea is on a higher
    // layer than the burrowLabel
    private JLabel burrowEntered;

    // - - - M A P S - - - //

    // holds every type of item and its quantity
    private Map<String, Integer> items = new LinkedHashMap<String, Integer>();
    // stores all the frogs and their display labels
    private Map<JLabel, Frog> frogs = new HashMap<JLabel, Frog>();

    // - - - L I S T S  &  A R R A Y S - - - //

    // stores all the display labels
    private ArrayList<JLabel> displayLabels = new ArrayList<JLabel>();
    // stores all the burrow images
    private final Burrow[] burrows = new Burrow[20];

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

        // creates each chunk of the background
        createBackground();
        // creates every type of item and sets its value
        initializeItems();
        // creates a display label for every kind of item and adds it to the frame
        initializeItemDisplayLabels();
        // create the time and time functions in the top right of the frame
        initializeTimeComponents();
        // adds in the menu
        addMenuButton();
        // creates the menu
        createMenu();
        // starts the bubble animations in the water
        startBackgroundEffects();
        // spawns the 4 initial frogs
        spawnFrogs(4);
        // TODO: remove this once done testing
        addLilyPads();
        addBurrows();

        // TODO: Remove this once done
        // adding in some idle frogs on the lily pads
        Frog idleFrog = new Frog();
        JLabel frogLabel = idleFrog.getDisplayLabel();
        frogLabel.setName("FrogLabel");
        frogLabel.setIcon(loadImage("Animations/IdleFrog.gif"));
        frogLabel.setSize(frogLabel.getPreferredSize());
        frogLabel.setLocation(345, 190);
        frogs.put(frogLabel, idleFrog);
        layeredPane.add(frogLabel, Integer.valueOf(3));
    }

    // - - - I T E M  M A N A G E M E N T - - - //

    // initializes each item and its quantity
    public void initializeItems() {
        // number of frogs, tadpoles, eggs, bugs, plant food, and hours spent in total
        items.put("Total Hours Spent", 0);
        items.put("Frogs", 4);
        items.put("Tadpoles", 0);
        items.put("Eggs", 0);
        items.put("Bugs", 0 /*TBD*/);
        items.put("Plant Food", 0 /*TBD*/);
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

        // formats the time label:
        // attempts to use a custom clock font for the time
        try {
            File fontFile = new File("Custom Fonts/digital_7/digital-7.ttf");
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            customFont = customFont.deriveFont(40f); // Set the desired font size

            // use the custom font
            timeLabel.setFont(customFont);

        }
        catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        // timeLabel.setFont(new Font("Dialog", Font.PLAIN, 30));
        // updates the text in the time label
        updateTime();
        timeLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        timeLabel.setIcon(loadImage("Pictures/TimeFrame.png"));
        timeLabel.setSize(width, timeLabel.getPreferredSize().height);
        System.out.println("Clock Size: " + timeLabel.getSize());
        timeLabel.setLocation(FRAME_WIDTH - width, 0);
        // adds it to the frame
        layeredPane.add(timeLabel, Integer.valueOf(6));

        // formats the bedtime label:
        bedtimeLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        bedtimeLabel.setText("Bedtime: " + bedtime + ":00pm");
        bedtimeLabel.setSize(width, timeLabel.getPreferredSize().height);
        bedtimeLabel.setLocation(FRAME_WIDTH - width, timeLabel.getY() + timeLabel.getHeight());
        // adds it to the frame
        layeredPane.add(bedtimeLabel, Integer.valueOf(6));

        // formats next day button:
        nextDayButton.setName("NextDayButton");
        nextDayButton.setFont(new Font("Serif", Font.PLAIN, 20));
        nextDayButton.setText("Next Day");
        // moves to next day when clicked
        nextDayButton.addMouseListener(this);
        nextDayButton.setSize(width, timeLabel.getPreferredSize().height);
        nextDayButton.setLocation(FRAME_WIDTH - width, bedtimeLabel.getY() + bedtimeLabel.getHeight());
        // adds it to the frame
        layeredPane.add(nextDayButton, Integer.valueOf(1));
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

    public void spendTime(int numHours) {
        time += numHours;
        updateTime();
    }


    // - - - B A C K G R O U N D - - - //

    public void createBackground() {
        // adds the SKY to the background
        sky.setName("Sky");
        sky.setSize(sky.getPreferredSize());
        layeredPane.add(sky, Integer.valueOf(0));

        // adds the WATER to the background
        water.setName("Water");
        water.setSize(water.getPreferredSize());
        water.setLocation(0, sky.getHeight());
        layeredPane.add(water, Integer.valueOf(0));

        // adds the GROUND to the background
        ground.setName("Ground");
        ground.setSize(ground.getPreferredSize());
        ground.setLocation(0, water.getY() + water.getHeight());
        layeredPane.add(ground, Integer.valueOf(0));

        // adds rocks to the background
        JLabel rocks = new JLabel(loadImage("Pictures/Rockbed1.png"));
        rocks.setSize(rocks.getPreferredSize());
        rocks.setLocation(0, 360);
        layeredPane.add(rocks, Integer.valueOf(1));
    }

    // TODO: remove once done
    public void addLilyPads() {
        int randPad;

        // spawns 20 lily pads across the surface of the water
        for (int i = 0; i < 20; i++) {
            randPad = (int) (Math.random() * 2);

            JLabel lilyPad = new JLabel();

            // randomly picks between the 2 different variants
            if (randPad == 1) {
                lilyPad.setIcon(loadImage("Pictures/LilyPadWithLotus.png"));
            }
            else {
                lilyPad.setIcon(loadImage("Pictures/LilyPad.png"));
            }

            lilyPad.setSize(lilyPad.getPreferredSize());
            // evenly spaces out each lily pad across 700 pixels
            lilyPad.setLocation(i * 30 + i * 7, 203 - lilyPad.getHeight() + 1);
            layeredPane.add(lilyPad, Integer.valueOf(2));
        }
    }

    private void startBackgroundEffects() {
        backgroundEffectsTimer = new Timer(1000, e -> {
            // randomly spawns bubbles
            int bubbleChance = (int) (Math.random() * 15) + 1;
            if (bubbleChance == 15) {
                int bubbleX = (int) (Math.random() * 668);
                // plays the bubble animations
                spawnBubbles(bubbleX);
                /*
                    playAnimation(loadImage("Animations/BubbleGif.gif"), bubbleX,
                            200, 32, 300, 3375);
                */
            }
        });
        backgroundEffectsTimer.start();
    }

    public void addBurrows() {
        // 680 x 170
        // 59 x 80

        // sets size equal to the dimensions of the ground
        burrowPanel.setSize(ground.getWidth() - 20, ground.getHeight() - 20);
        // sets location equal to the location of the ground
        burrowPanel.setLocation(ground.getX() + 10, ground.getY() + 10);
        // creates a 2x10 grid layout with 5pixel horizontal and vertical gaps
        burrowPanel.setLayout(new GridLayout(2, 10, 10, 10));
        // makes the background transparent
        burrowPanel.setOpaque(false);

        // creates all the empty burrows and adds them to the screen
        for (int i = 0; i < burrows.length; i++) {
            // picks a random empty burrow image
            Burrow burrow = new Burrow(loadImage("Pictures/EmptyBurrow" + ((int) (Math.random() * 5) + 1) + ".png"));
            // assigns a couple locations for frogs to rest in each one of the burrows
            setBurrowPositions();

            // creates a label to hold the burrow
            JLabel burrowLabel = new JLabel();
            burrowLabel.setSize(59, 80);
            burrowLabel.setIcon(burrow.getImage());
            burrowLabel.addMouseListener(this);
            burrowLabel.setName("Burrow" + i);
            System.out.println(burrowLabel.getName());

            burrows[i] = burrow;
            burrowPanel.add(burrowLabel);
        }

        layeredPane.add(burrowPanel, Integer.valueOf(1));
    }

    public void setBurrowPositions() {

    }

    public void displayBurrowPopulation(JLabel label, String name) {
        // +20 pixels to account for the offset of the burrowPanel x
        int burrowX = label.getX() + 10;
        // +530 pixels to account for the offset of the burrowPanel y
        int burrowY = label.getY() + 520;
        // dimensions of the burrow
        int burrowWidth = label.getWidth();
        int burrowHeight = label.getHeight();
        // the number of the burrow [0-19]
        int burrowNum = Integer.parseInt(name.substring(name.indexOf("w") + 1));
        System.out.println(burrowNum);
        Burrow burrow = burrows[burrowNum];
        System.out.println(burrowNum);

        // creates a label to display the population of the burrow
        populationTextArea = new JTextArea(" " + burrow.getNumFrogs() + " \n---\n 5 ") {
            @Override
            public boolean contains(int x, int y) {
                // return false to make this component transparent to mouse events
                return false;
            }
        };
        populationTextArea.setName("PopulationTextArea");
        populationTextArea.setForeground(Color.white);
        populationTextArea.setFont(new Font("Serif", Font.PLAIN, 15));
        populationTextArea.setSize(populationTextArea.getPreferredSize());
        populationTextArea.addMouseListener(this);
        // makes the background transparent so just the text is showing
        populationTextArea.setOpaque(false);
        populationTextArea.setEnabled(false);

        // calculating the positioning of the populationTextArea in order to keep it centered in the burrowLabel
        int xPos = (burrowWidth - populationTextArea.getWidth()) / 2 + burrowX;
        int yPos = (burrowHeight - populationTextArea.getHeight()) / 2 + burrowY;

        populationTextArea.setLocation(xPos, yPos);
        layeredPane.add(populationTextArea, Integer.valueOf(10));
    }

    // - - - A N I M A T I O N - - - //

    private void spawnBubbles(int randX) {
        ImageIcon bubbles = loadImage("Animations/BubbleGif.gif");

        // does a weird work-around to reset the gif each time a new one is created
        Image rerenderedImage = bubbles.getImage().getScaledInstance(32, 300, Image.SCALE_REPLICATE);
        JLabel bubbleAnimation = new JLabel(new ImageIcon(rerenderedImage));
        bubbleAnimation.setSize(32, 300);
        // spawns somewhere on the pond floor so y stays constant
        bubbleAnimation.setLocation(randX, 200);
        // bubble spawns somewhere random on the z-axis between layers [1-4]
        layeredPane.add(bubbleAnimation, Integer.valueOf((int) (Math.random() * 4) + 1));

        // creates a timer to control the animation's duration
        Timer animationTimer = new Timer(3375, e -> {
            // stop the animation and clean up
            bubbleAnimation.setVisible(false);
            layeredPane.remove(bubbleAnimation);
            layeredPane.revalidate();
            layeredPane.repaint();
        });

        // ensure the timer runs only once
        animationTimer.setRepeats(false);
        animationTimer.start();
    }

    // TODO: IDK if I actually need this method because the only thing animated rn is the bubbles and
    //  this method was designed for general use, not specialized
    private void playAnimation(ImageIcon gif, int x, int y, int width, int height, int duration) {
        // does a weird work-around to reset the gif each time a new one is created
        Image rerenderedImage = gif.getImage().getScaledInstance(width, height, Image.SCALE_REPLICATE);
        JLabel animation = new JLabel(new ImageIcon(rerenderedImage));

        animation.setSize(width, height);
        animation.setLocation(x, y);

        // add the animation to the layeredPane
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

    // - - - M E N U  M E T H O D S - - - //

    public void addMenuButton() {
        menuButton.setText("Action Menu");
        menuButton.setFont(new Font("Serif", Font.PLAIN, 20));
        menuButton.setSize(146, 42);
        menuButton.setIcon(loadImage("Pictures/MenuButtonBackground.png"));
        System.out.println("Menu Button: " + menuButton.getSize());
        menuButton.setHorizontalTextPosition(SwingConstants.CENTER);
        menuButton.setLocation(350 - menuButton.getWidth() / 2, 0);
        menuButton.addMouseListener(this);
        menuButton.setName("MenuButton");
        layeredPane.add(menuButton, Integer.valueOf(6));
    }

    public void createMenu() {
        menu.setSize(700, 700);
        menu.setLayout(null);
        JLabel menuBackground = new JLabel();
        menuBackground.setSize(700, 700);
        menuBackground.setIcon(loadImage("Pictures/MenuBackground.png"));
        menu.add(menuBackground);
        menu.setVisible(false);
        layeredPane.add(menu, Integer.valueOf(5));
        menu.revalidate();
        System.out.println(menuBackground.getLocation());
    }

    // - - - M I S C E L L A N E O U S - - - //

    // checks for valid files paths
    public ImageIcon loadImage(String filePath) {
        // if the file path exists it returns the referenced ImageIcon
        if (Files.exists(Path.of(filePath))) {
            return new ImageIcon(filePath);
        }
        // if the path does not exist it prints out the file path and throws a NullPointerException
        else {
            System.out.println("Error: " + filePath + " does not exist");
            return null;
        }
    }

    // spawns n amount of frogs
    public int spawnFrogs(int n) {
        if (n == 0) {
            return 0;
        }
        else {
            Frog swimmingFrog = new Frog();
            // immediately grows the frog into stage 3: "Frog"
            swimmingFrog.growFrog();
            swimmingFrog.growFrog();
            JLabel frogLabel = swimmingFrog.getDisplayLabel();
            frogLabel.addMouseListener(this);
            frogs.put(frogLabel, swimmingFrog);
            swimmingFrog.startSwimming();

            // adds it to the screen between layers 1-4
            layeredPane.add(frogLabel, Integer.valueOf((int) (Math.random() * 4) + 1));

            return spawnFrogs(n - 1);
        }
    }

    public void displayFrogName(JLabel frogLabel) {
        Frog frog = frogs.get(frogLabel);

        frogNameLabel = new JLabel(frog.getName());
        frogNameLabel.setFont(new Font("Serif", Font.PLAIN, 10));
        frogNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        int adjustedWidth = (int) frogNameLabel.getPreferredSize().getWidth() + 5;
        frogNameLabel.setSize(adjustedWidth, (int) frogNameLabel.getPreferredSize().getHeight());
        // aligns the centers of the frogNameLabel and frog label
        int xPos = frogLabel.getX() + (frogLabel.getWidth() - frogNameLabel.getWidth()) / 2;
        frogNameLabel.setLocation(xPos, frogLabel.getY() - frogNameLabel.getHeight());
        layeredPane.add(frogNameLabel, Integer.valueOf(4));

        // keeps the position of the frogNameLabel synced with the position of the frog label
        nameUpdater = new Timer(50, e1 -> {
            int newXPos = frogLabel.getX() + (frogLabel.getWidth() - frogNameLabel.getWidth()) / 2;
            frogNameLabel.setLocation(newXPos, frogLabel.getY() - frogNameLabel.getHeight());
        });
        nameUpdater.start();
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

                // if clicking the next day
                if (name.equals("NextDayButton")) {
                    // TODO: go to next day
                }
                else if (name.equals("MenuButton")) {
                    // opens and closes the menu
                    menu.setVisible(!menu.isVisible());
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
        if (object instanceof JLabel label) {
            // if the JLabel being entered has a name
            // (all JLabels with mouseListeners added should be named)
            if (label.getName() != null) {
                String name = label.getName();

                // if it's a background label it highlights the border
                if (name.equals("MenuButton") || name.equals("NextDayButton")) {
                    label.setBorder(new LineBorder(Color.YELLOW, 1));
                }
                // displays frog's name
                else if (name.equals("FrogLabel")) {
                    displayFrogName(label);
                }
                // displays the population / 5 of a burrow
                else if (name.contains("Burrow")) {
                    System.out.println(label.getLocation());
                    System.out.println(name);
                    displayBurrowPopulation(label, name);
                    burrowEntered = label;
                }
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // gets the component pressed
        Object object = e.getSource();

        // if the object is a JLabel
        if (object instanceof JLabel label) {

            if (label.getName() != null) {
                String name = label.getName();

                // if label is a background label it removes the highlighting border
                if (name.equals("MenuButton") || name.equals("NextDayButton")) {
                    label.setBorder(null);
                }
                // if label is a frog label it removes the name above its head
                else if (name.equals("FrogLabel")) {
                    frogNameLabel.setVisible(false);
                    layeredPane.remove(frogNameLabel);
                    nameUpdater.stop();
                }
                // if exiting a burrow
                else if (name.contains("Burrow")) {
                    // deletes the population / capacity JTextArea fraction from the screen
                    populationTextArea.setVisible(false);
                    layeredPane.remove(populationTextArea);
                    System.out.println("Exited Burrow");
                }
            }
        }
    }
}
