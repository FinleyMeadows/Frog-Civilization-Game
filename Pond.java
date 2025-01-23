/*
    Finley Meadows & Michael Anderson
    12/2/2024
    The Pond class controls the majority of all visual elements on the screen
*/

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Pond extends JFrame implements MouseListener, MouseMotionListener {

    // - - - V A R I A B L E S - - - //
    public static final int FRAME_WIDTH = 700;
    public static final int FRAME_HEIGHT = 700;

    // holds the current hour of the day
    private int time = 8;
    // the time when the day ends
    private int bedtime = 8;
    private String amOrPm = "am";
    // 0-100, 0 being crystal clear, 100 being nasty
    public int waterDirtiness = 0;
    // times all the background effects
    private Timer backgroundEffectsTimer;

    // stores the initial location of the item in the menu bar
    private Point initialItemLocation;

    // tracks when the mouse is being pressed to suppress unwanted interactions
    private boolean mouseIsPressed;

    // tracks the true location of the mouse because the actual one from the getMouseLocation() method is inaccurate
    private Point mousePos;

    // stores the initial x-difference between the mouse's position and the label's position before dragging an item
    private int initialXOffset;
    // stores the initial y-difference between the mouse's position and the label's position before dragging an item
    private int initialYOffset;

    // the more the player cleans their water the better the get at it
    // each time the water is cleaned a random number in generated between ~1-30
    // the cleaningLevel is added to that number to slowly increase the efficiency of the cleaning action
    private int cleaningLevel = 1;

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
    private JLabel water = new JLabel(loadImage("Pictures/WaterLevels/Water.png"));
    private JLabel ground = new JLabel(loadImage("Pictures/Ground.png"));
    private JLabel rockBed;

    // holds all the burrows in a grid layout
    private JPanel burrowPanel = new JPanel();

    // time variables and components
    private JLabel clock = new JLabel();
    private JLabel bedtimeLabel = new JLabel();
    private JLabel nextDayButton = new JLabel();

    // label that displays the frogs name above its image
    private JLabel frogNameLabel;
    // keeps the position of the frog's name synced with the position of the frog's label
    private Timer namePosUpdater;

    // this is basically the player's inventory of items and actions
    private JLabel menuBar = new JLabel();

    // shows the population / capacity of a burrow
    private JTextArea populationTextArea;

    // stores the current item's description being displayed
    private JTextArea itemDescription;
    // give the itemDescription a background because JTextAreas can't hold ImageIcons
    private JLabel itemDescriptionBackground;

    // used to display increases in resources
    // saved in class-scope to be deleted once ready
    private JLabel popup;
    // used to fade the background when displaying a resource popup
    private JLabel translucentPane;

    // - - - M A P S - - - //

    // holds every type of resource and its quantity
    private Map<String, Integer> resources = new LinkedHashMap<String, Integer>();
    // stores all the frogs and their display labels
    private Map<JLabel, Frog> frogs = new HashMap<JLabel, Frog>();
    // stores all the tadpoles and their display labels
    private Map<JLabel, Frog> tadpoles = new HashMap<JLabel, Frog>();
    // stores all the burrows on screen <burrowName, Burrow>
    private Map<String, Burrow> burrows = new HashMap<String, Burrow>();
    // stores all the lily pads and their corresponding stages
    // stage 1: seed, 2: sprouted seed, 3: lily pad
    private Map<JLabel, Integer> lilyPads = new HashMap<JLabel, Integer>();

    // - - - L I S T S  &  A R R A Y S - - - //

    // stores all the display labels
    private ArrayList<JLabel> resourceLabels = new ArrayList<JLabel>();
    // holds all the item in the player's inventory
    private ArrayList<JLabel> items = new ArrayList<JLabel>();
    // stores all available slots for planting a lily pad
    private ArrayList<JLabel> availableLilyPadSlots = new ArrayList<JLabel>();

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
        // null layout so components can be placed anywhere with ease
        layeredPane.setLayout(null);
        // the pack() method resizes to preferred sizes not normal sizes
        layeredPane.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        // adds the layered pane to the center of the JFrame
        this.add(layeredPane, BorderLayout.CENTER);
        // resizes the JFrame to the size of layeredPane
        this.pack();

        // creates each chunk of the background
        createBackground();
        // updates the water image based on the 'waterDirtiness' level
        updateWaterDirtiness(0);
        // adds in the temporarily invisible lily pad slots for planting
        createLilyPadSlots();
        // creates every type of item and sets its value
        initializeResources();
        // sets the frog stat as the selectedResourceLabel
        initializeSelectedResourceLabel();
        // displays all resource labels
        expandResourceLabels();
        // create the time and time functions in the top right of the frame
        initializeTimeComponents();
        // sets the rock bed image
        updatePlantLevels();
        // creates the 5 items for the menu bar
        createItems();
        // adds in the menu bar at the top of the screen
        addMenuBar();
        // starts the bubble animations in the water
        startBackgroundEffects();
        // spawns the 4 initial frogs
        spawnFrogs(4);

        // adds all the empty burrow in the ground
        addBurrows();

        // TODO: remove once done with testing
        // spawns in a couple tadpoles to test their swimming
        spawnTadpoles(4);

        // after everything is added updates all the resources
        updateResourceLabels();
    }

    // - - - R E S O U R C E  M A N A G E M E N T - - - //

    // initializes each item and its quantity
    public void initializeResources() {
        // number of frogs, tadpoles, eggs, bugs, plant food, and hours spent in total
        resources.put("Frogs", 0);
        resources.put("Tadpoles", 0);
        resources.put("Eggs", 0);
        resources.put("Bugs", 0);
        resources.put("Plants", 50);
        resources.put("Hours Spent", 0);
    }

    // sets the selectedResourceLabel to the frog stat
    public void initializeSelectedResourceLabel() {
        // adds the frog label
        JLabel frogResourceLabel = new JLabel();
        // adds the frame background
        frogResourceLabel.setIcon(loadImage("Pictures/ClockFrame.png"));
        frogResourceLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        frogResourceLabel.setText("Frogs: " + resources.get("Frogs"));
        // moves the text to the correct x position
        frogResourceLabel.setIconTextGap(-145);
        frogResourceLabel.setVerticalTextPosition(SwingConstants.CENTER);
        frogResourceLabel.setSize(150, 50);
        frogResourceLabel.setLocation(0, 0);

        // adds the resourceLabel to the resourceLabels ArrayList
        resourceLabels.add(frogResourceLabel);

        // adds the resourceLabel to the layeredPane
        layeredPane.add(frogResourceLabel, Integer.valueOf(3));
    }

    // displays all the resource labels with the selectedResourceLabel at the top and the condense label below
    public void expandResourceLabels() {
        // delete the expand label from the screen and the last index of the resourceLabels ArrayList
        if (resourceLabels.size() > 1) {
            // remove it from the screen
            resourceLabels.get(1).setVisible(false);
            layeredPane.remove(resourceLabels.get(1));
            // removes it from the resourceLabels ArrayList
            resourceLabels.removeLast();
        }

        int yPos = 50;

        // add in all the other labels and the condense label at the last index
        for (Map.Entry<String, Integer> entry : resources.entrySet()) {
            String name = entry.getKey();
            int quantity = entry.getValue();

            // if the current resource is not already displayed on the selectedResourceLabel
            if (!resourceLabels.getFirst().getText().contains(name)) {

                // customizing the display label
                JLabel resourceLabel = new JLabel();
                resourceLabel.setName("ResourceLabel");
                // adds the frame background
                resourceLabel.setIcon(loadImage("Pictures/ResourceLabelFrame.png"));
                resourceLabel.setFont(new Font("Serif", Font.PLAIN, 15));
                resourceLabel.setText(name + ": " + quantity);
                // moves the text to the correct x position
                resourceLabel.setIconTextGap(-145);
                resourceLabel.addMouseListener(this);
                resourceLabel.setSize(150, 25);
                resourceLabel.setLocation(0, yPos);
                System.out.println("Display label size: " + resourceLabel.getSize());

                // adds the resourceLabel to the resourceLabels ArrayList
                resourceLabels.add(resourceLabel);

                // adds the resourceLabel to the layeredPane
                layeredPane.add(resourceLabel, Integer.valueOf(3));

                // moves the yPos down below the previous label
                yPos += 25;
            }
        }

        // adds the condense label at the bottom of the stack
        JLabel condenseLabel = new JLabel();
        condenseLabel.setName("CondenseToggle");
        condenseLabel.setIcon(loadImage("Pictures/CondenseToggle.png"));
        condenseLabel.setSize(150, 25);
        condenseLabel.setLocation(0, yPos);
        condenseLabel.addMouseListener(this);
        resourceLabels.add(condenseLabel);
        layeredPane.add(condenseLabel, Integer.valueOf(3));

    }

    // displays only the selectedResourceLabel and the expand label below
    public void condenseResourceLabels() {
        // removes every resource label from the screen besides the selectedResourceLabel
        for (int i = resourceLabels.size() - 1; i > 0; i--) {
            // removes the label from the screen
            resourceLabels.get(i).setVisible(false);
            layeredPane.remove(resourceLabels.get(i));
            // removes the label from the resourceLabels ArrayList
            resourceLabels.removeLast();
        }
        System.out.println(resourceLabels.size());

        // adds the expand label to the last index of the ArrayList below the selectedResourceLabel
        JLabel expandLabel = new JLabel();
        expandLabel.setName("ExpandToggle");
        expandLabel.setIcon(loadImage("Pictures/ExpandToggle.png"));
        expandLabel.setSize(150, 25);
        expandLabel.setLocation(0, 50);
        expandLabel.addMouseListener(this);
        resourceLabels.add(expandLabel);
        layeredPane.add(expandLabel, Integer.valueOf(3));
    }


    public void updateResourceLabels() {
        // updates all resource labels actively being displayed
        for (JLabel resourceLabel : resourceLabels) {
            String text = resourceLabel.getText();
            // if the label isn't the 'expand' or 'condense' label then it updates it
            if (!text.isEmpty()) {
                System.out.println("Text: " + text);
                String resourceName = text.substring(0, text.indexOf(":"));

                // if updating the # of frogs
                if (resourceName.equals("Frogs")) {
                    // resets the label
                    resourceLabel.setText(resourceName + ": " + frogs.size());
                }
                // if updating the # of tadpoles
                else if (resourceName.equals("Tadpoles")) {
                    // resets the label
                    resourceLabel.setText(resourceName + ": " + tadpoles.size());
                }
                else {
                    // resets the label
                    resourceLabel.setText(resourceName + ": " + resources.get(resourceName));
                }
            }
        }
    }

    public void printArr() {
        System.out.print("{ ");
        for (JLabel label : resourceLabels) {
            System.out.print(label.getText() + " ");
        }
        System.out.println(" }");
    }

    public void switchSelectedResourceLabel(JLabel newLabel) {
        // saves the old selectedResourceLabel's text so it can be reinserted into index 1
        String oldText = resourceLabels.getFirst().getText();

        System.out.println(oldText);

        printArr();

        // sets the first label in the resourceLabels ArrayList to the new label's text
        resourceLabels.getFirst().setText(newLabel.getText());

        // removes the new label's old label in the array
        for (int i = 1; i < resourceLabels.size(); i++) {
            if (resourceLabels.get(i).getText().equals(newLabel.getText())) {
                resourceLabels.get(i).setVisible(true);
                layeredPane.remove(resourceLabels.get(i));
                resourceLabels.remove(i);
                break;
            }
        }

        printArr();

        // reinserts the old selectedResourceLabel into index 1
        JLabel demotedResourceLabel = new JLabel();
        demotedResourceLabel.setName("ResourceLabel");
        // adds the frame background
        demotedResourceLabel.setIcon(loadImage("Pictures/ResourceLabelFrame.png"));
        demotedResourceLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        demotedResourceLabel.setText(oldText);
        // moves the text to the correct x position
        demotedResourceLabel.setIconTextGap(-145);
        demotedResourceLabel.addMouseListener(this);
        demotedResourceLabel.setSize(150, 25);
        demotedResourceLabel.setLocation(0, 50);
        resourceLabels.add(1, demotedResourceLabel);
        System.out.println(resourceLabels.size());

        layeredPane.add(demotedResourceLabel, Integer.valueOf(1));

        printArr();

        int yPos = 75;

        for (int i = 2; i < resourceLabels.size(); i++) {
            resourceLabels.get(i).setVisible(false);
            resourceLabels.get(i).setLocation(0, yPos);
            resourceLabels.get(i).setVisible(true);
            System.out.println(i + ": " + resourceLabels.get(i).getLocation());
            System.out.println(resourceLabels.get(i).getText());
            yPos += 25;
        }

        printArr();

    }


    // - - - T I M E  M A N A G E M E N T - - - //

    public void initializeTimeComponents() {
        // formats the time label:
        // attempts to use a custom clock font for the time
        try {
            File fontFile = new File("Custom Fonts/digital_7/digital-7.ttf");
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            customFont = customFont.deriveFont(45f); // set the font size

            // use the custom font
            clock.setFont(customFont);

        }
        catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        // clock.setFont(new Font("Dialog", Font.PLAIN, 30));
        // updates the text in the time label
        updateTime();
        clock.setHorizontalTextPosition(SwingConstants.CENTER);
        clock.setIcon(loadImage("Pictures/ClockFrame.png"));
        clock.setSize(150, 50);
        clock.setLocation(550, 0);
        // adds it to the frame
        layeredPane.add(clock, Integer.valueOf(1));
        System.out.println("Clock Size: " + clock.getSize());

        // formats the bedtime label:
        bedtimeLabel.setIcon(loadImage("Pictures/ResourceLabelFrame.png"));
        bedtimeLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        bedtimeLabel.setText("Bedtime: " + bedtime + ":00pm");
        bedtimeLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        bedtimeLabel.setSize(150, 25);
        bedtimeLabel.setLocation(550, 50);
        // adds it to the frame
        layeredPane.add(bedtimeLabel, Integer.valueOf(1));

        // formats next day button:
        nextDayButton.setIcon(loadImage("Pictures/NextDayFrame.png"));
        nextDayButton.setName("NextDayButton");
        nextDayButton.setFont(new Font("Serif", Font.PLAIN, 15));
        nextDayButton.setText("Next Day");
        nextDayButton.setIconTextGap(-95);
        nextDayButton.setVerticalTextPosition(SwingConstants.CENTER);
        // moves to next day when clicked
        nextDayButton.addMouseListener(this);
        nextDayButton.setSize(100, 25);
        nextDayButton.setLocation(600, 75);
        // adds it to the frame
        layeredPane.add(nextDayButton, Integer.valueOf(1));
    }

    // updates the time label
    public void updateTime() {
        // time == 12am or pm
        if (time == 12) {
            if (amOrPm.equals("am")) {
                clock.setText(time + ":00pm");
            }
            else {
                clock.setText(time + ":00am");
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
            clock.setText(" " + time + ":00" + amOrPm);
        }
    }

    public void spendTime(int numHours) {
        time += numHours;
        updateTime();
        resources.put("Hours Spent", resources.get("Hours Spent") + numHours);
        updateResourceLabels();
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

        System.out.println(water.getSize());
        System.out.println(water.getLocation());

        // adds the GROUND to the background
        ground.setName("Ground");
        ground.setSize(ground.getPreferredSize());
        ground.setLocation(0, water.getY() + water.getHeight());
        layeredPane.add(ground, Integer.valueOf(0));

        // adds rocks to the background
        rockBed = new JLabel(loadImage("Pictures/RockBed1.png")) {
            @Override
            public boolean contains(int x, int y) {
                // return false to make this component transparent to mouse events
                return false;
            }

        };
        rockBed.setSize(rockBed.getPreferredSize());
        rockBed.setLocation(0, 360);
        layeredPane.add(rockBed, Integer.valueOf(1));
    }

    public void createLilyPadSlots() {
        int gap = 0;

        // spawns 20 lily pad slots across the surface of the water
        for (int i = 0; i < 20; i++) {
            // 29 * 20 = 580 pixels occupied by lily pads
            // 700 - 580 = 120 pixels to be divided in between each lily pad

            // 6 5-pixel gaps (30 pixels)
            if (i % 3 == 0 && i != 0) {
                gap += 5;
                System.out.println(i + ": 5 gap");
            }
            // + 15 6-pixel gaps (90 pixels) = 120 pixels
            else {
                gap += 6;
            }

            JLabel lilyPadSlot = new JLabel();
            lilyPadSlot.setName("Slot" + i);
            lilyPadSlot.setIcon(loadImage("Pictures/LilyPadSlot.png"));
            lilyPadSlot.setSize(lilyPadSlot.getPreferredSize());
            // evenly spaces out each lily pad slot across 700 pixels
            lilyPadSlot.setLocation(i * 29 + gap, 203 - lilyPadSlot.getHeight());
            // invisible until planting a lily pad seed
            lilyPadSlot.setVisible(false);
            layeredPane.add(lilyPadSlot, Integer.valueOf(2));
            // adds a new unoccupied slot to the lilyPadSlots map
            availableLilyPadSlots.add(lilyPadSlot);
        }
    }

    private void startBackgroundEffects() {
        backgroundEffectsTimer = new Timer(1000, e -> {
            // randomly spawns bubbles
            int bubbleChance = (int) (Math.random() * 15) + 1;
            if (bubbleChance == 15) {
                int bubbleX = (int) (Math.random() * 668);

                // plays the bubble animation
                playAnimation(loadImage("Animations/BubbleGif.gif"), bubbleX,
                        200, 32, 300, 3375);

            }
        });
        backgroundEffectsTimer.start();
    }

    public void addBurrows() {
        // 680 x 170
        // 59 x 80

        for (int r = 1; r <= 2; r++) {
            for (int c = 1; c <= 10; c++) {
                // creates a label to hold the burrow
                JLabel burrowLabel = new JLabel();
                burrowLabel.setSize(59, 80);
                // gets a random empty burrow image 1-5
                burrowLabel.setIcon(loadImage("Pictures/EmptyBurrow" + ((int) (Math.random() * 5) + 1) + ".png"));
                burrowLabel.addMouseListener(this);
                // name = EmptyBurrow [1-20]
                burrowLabel.setName("EmptyBurrow" + (c + (r - 1) * 10));
                // loc = cumulative h-gap + burrow widths, ground start loc + cumulative v-gap + burrow heights
                burrowLabel.setLocation((10 * c) + ((c - 1) * 59), 510 + (10 * r) + (r - 1) * 80);
                System.out.println(burrowLabel.getLocation());

                Burrow burrow = new Burrow(burrowLabel);
                // assigns a couple locations for frogs to rest in each one of the burrows
                setBurrowPositions();

                // adds the burrow to the map
                burrows.put(burrowLabel.getName(), burrow);
                layeredPane.add(burrowLabel, Integer.valueOf(1));
            }
        }

        layeredPane.add(burrowPanel, Integer.valueOf(1));
    }

    public void setBurrowPositions() {

    }

    public void displayBurrowPopulation(JLabel label, String name) {
        // +20 pixels to account for the offset of the burrowPanel x
        int burrowX = label.getX();
        // +530 pixels to account for the offset of the burrowPanel y
        int burrowY = label.getY();
        // dimensions of the burrow
        int burrowWidth = label.getWidth();
        int burrowHeight = label.getHeight();
        // stores the burrow
        Burrow burrow = burrows.get(name);

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
        layeredPane.add(populationTextArea, Integer.valueOf(3));
    }


    // - - - A N I M A T I O N - - - //

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
        });

        // ensure the timer runs only once
        animationTimer.setRepeats(false);
        animationTimer.start();
    }

    // - - - M E N U - - - //

    public void addMenuBar() {
        // 8 40x40 inventory slots at the top of the screen

        menuBar.setLayout(null);
        menuBar.setIcon(loadImage("Pictures/MenuBarFrame.png"));
        menuBar.setSize(400, 50);
        menuBar.setLocation(150, 0);

        // adding in the 5 slots
        for (int i = 0; i < 5; i++) {
            JLabel barSlot = new JLabel();
            barSlot.setIcon(loadImage("Pictures/itemFrame.png"));
            barSlot.setOpaque(true);
            barSlot.setSize(50, 50);
            barSlot.setLocation((i + 1) * 25 + i * 50, 0);
            menuBar.add(barSlot);

            // adds in the item associated with that slot
            items.get(i).setLocation(150 + (i + 1) * 25 + i * 50, 0);
            layeredPane.add(items.get(i), Integer.valueOf(5));
        }

        layeredPane.add(menuBar, Integer.valueOf(1));

        System.out.println("Menu bar size: " + menuBar.getSize());
    }


    // - - - I T E M S  &  A C T I O N S - - - //


    // - - - I T E M  M A N A G E M E N T - - - //

    public void createItems() {
        createItem("BugNetItem", "Pictures/BugNet.png");
        createItem("HammerItem", "Pictures/Hammer2.png");
        createItem("PlantFoodItem", "Pictures/PlantFood.png");
        createItem("LilyPadSeedItem", "Pictures/LilyPadSeed2.png");
        createItem("SpongeItem", "Pictures/SmallerSponge.png");
    }

    public void createItem(String name, String iconFilePath) {
        JLabel item = new JLabel();
        item.setName(name);
        item.setIcon(loadImage(iconFilePath));
        item.setSize(50, 50);
        item.addMouseListener(this);
        item.addMouseMotionListener(this);
        items.add(item);
    }

    public void openItemDescription(JLabel item) {
        // adds the background of the description
        itemDescriptionBackground = new JLabel();
        itemDescriptionBackground.setIcon(loadImage("Pictures/descriptionFrame.png"));
        itemDescriptionBackground.setSize(50, 25);
        itemDescriptionBackground.setLocation(item.getX(), item.getY() + 50);
        itemDescriptionBackground.setVisible(true);
        layeredPane.add(itemDescriptionBackground, Integer.valueOf(1));

        itemDescription = new JTextArea();
        itemDescription.setFont(new Font("Serif", Font.PLAIN, 8));
        // centers the text
        itemDescription.setMargin(new Insets(2, 5, 0, 0));
        itemDescription.setOpaque(false);
        itemDescription.setLocation(itemDescriptionBackground.getLocation());
        itemDescription.setSize(50, 25);

        String itemName = item.getName();

        if (itemName.equals("BugNetItem")) {
            itemDescription.setText("Collect Bugs:\nCost: 1 Hour");
        }
        else if (itemName.equals("HammerItem")) {
            itemDescription.setText("Build Burrow:\nCost: 5 Hours");
        }
        else if (itemName.equals("PlantFoodItem")) {
            itemDescription.setText("Grow Plants:\nCost: 1 Hour");
        }
        else if (itemName.equals("LilyPadSeedItem")) {
            itemDescription.setText("Plant Lily Pad:\nCost: 1 Hour");
        }
        else if (itemName.equals("SpongeItem")) {
            itemDescription.setText("Clean Water:\nCost: 3 Hours");
        }

        itemDescription.setVisible(true);
        layeredPane.add(itemDescription, Integer.valueOf(2));
    }

    public void closeItemDescription() {
        // removes the description from the screen
        itemDescription.setVisible(false);
        layeredPane.remove(itemDescription);
        // removes the description's background from the screen
        itemDescriptionBackground.setVisible(false);
        layeredPane.remove(itemDescriptionBackground);
    }


    // - - - C O L L E C T I N G  B U G S - - - //

    public void collectBugs() {
        // takes 1 hour
        spendTime(1);

        updateTrueMousePosition();

        // plays the bug net animation
        playAnimation(loadImage("Animations/NetAnimation.gif"), (int) mousePos.getX() - 35,
                (int) mousePos.getY() - 35,  70, 70,1200);

        Timer stallTimer = new Timer(1200, e -> {
            // adds [1-30] bugs to the total
            int bugsCollected = (int) (Math.random() * 30) + 1;
            resources.put("Bugs", resources.get("Bugs") + bugsCollected);
            updateResourceLabels();

            // displays bugs collected on a popup
            displayPopup("You collected " + bugsCollected + " bugs!");
        });

        stallTimer.setRepeats(false);
        stallTimer.start();
    }


    // - - - C L E A N I N G  W A T E R - - - //

    public void cleanWater() {
        // takes 3 hours
        spendTime(3);

        playAnimation(loadImage("Animations/SpongeAnimation.gif"),
                0, 203, 700, 307, 3600);

        // runs whatever is needed after the animation finishes
        Timer stallTimer = new Timer(3600, e -> {
            // [1-30]% + cleaningLevel
            int percentCleaned = (int) (Math.random() * 30) + cleaningLevel;

            // updates the waterDirtiness level & and the water as a visual
            updateWaterDirtiness(percentCleaned);

            displayPopup("The pond is now " + percentCleaned + "% cleaner!");

            // cleaning level increases after every repetition
            cleaningLevel++;
        });

        // only runs once
        stallTimer.setRepeats(false);
        stallTimer.start();
    }

    public void updateWaterDirtiness(int percentCleaned) {
        waterDirtiness -= percentCleaned;
        // makes sure the level stays above 0%
        if (waterDirtiness < 0) {
            waterDirtiness = 0;
        }

        // updates the water image
        int multipleOfTen = waterDirtiness / 10 * 10;
        System.out.println("Water image num: " + multipleOfTen);

        if (multipleOfTen == 0) {
            water.setIcon(loadImage("Pictures/WaterLevels/Water.png"));
        }
        else {
            water.setIcon(loadImage("Pictures/WaterLevels/Water" + multipleOfTen + ".png"));
        }

    }


    // - - - B U R R O W S - - - //

    public void buildBurrow(Burrow burrow) {
        // takes 5 hours
        spendTime(5);

        JLabel burrowLabel = burrow.getDisplayLabel();

        String burrowName = burrowLabel.getName();

        // deletes the old name in the map of burrows
        burrows.remove(burrowName);

        // renames the burrow so it can't be built upon further
        burrow.getDisplayLabel().setName("Burrow" + burrowName.substring(burrowName.indexOf("w") + 1));

        // adds in the new name to the map of burrows
        burrows.put(burrow.getDisplayLabel().getName(), burrow);

        // does a weird work-around to reset the gif each time a new one is created
        ImageIcon gif = loadImage("Animations/HammerAnimation.gif");
        Image rerenderedImage = gif.getImage().getScaledInstance(50, 50, Image.SCALE_REPLICATE);
        JLabel animation = new JLabel(new ImageIcon(rerenderedImage));

        animation.setSize(50, 50);
        animation.setLocation(burrowLabel.getX() + 4, burrowLabel.getY() + 15);

        // add the animation to the layeredPane
        layeredPane.add(animation, Integer.valueOf(4));

        Timer halfBuiltTimer = new Timer(333, e -> {
            burrow.getDisplayLabel().setIcon(loadImage("Pictures/HalfBuiltBurrow.png"));
        });

        halfBuiltTimer.setRepeats(false);
        halfBuiltTimer.start();

        Timer fullBuiltTimer = new Timer(916, e -> {

            // picks between burrows [1-8]
            int randBurrowNum = (int) (Math.random() * 8) + 1;
            // assigns the burrow a random built burrow image
            burrow.getDisplayLabel().setIcon(loadImage("Pictures/Burrow" + randBurrowNum + ".png"));
        });

        fullBuiltTimer.setRepeats(false);
        fullBuiltTimer.start();



        // creates a timer to control the animation's duration
        Timer hammerTimer = new Timer(1333, e -> {
            // stop the animation and clean up
            animation.setVisible(false);
            layeredPane.remove(animation);
        });

        // ensure the timer runs only once
        hammerTimer.setRepeats(false);
        hammerTimer.start();

        // automatically assigns available frogs to the new burrow
        assignFrogsToBurrow(burrow);
    }

    public void assignFrogsToBurrow(Burrow burrow) {
        int numFrogsAssigned = burrow.getNumFrogs();

        // finds all frogs without a burrow and assigns a max of 5 of them to the new burrow
        for (Map.Entry<JLabel, Frog> entry : frogs.entrySet()) {
            Frog frog = entry.getValue();

            // if the frog does not have a burrow it gets added to the new burrow
            if (!frog.hasBurrow() && numFrogsAssigned < 5) {
                // adds the frog to the burrow
                burrow.addFrogs(1);
                // changes the frogs burrow status so it cannot be assigned again
                frog.setHasBurrow(true);

                numFrogsAssigned++;
                if (numFrogsAssigned == 5) {
                    break;
                }
            }
        }
    }


    // - - - P L A N T  F O O D - - - //

    public void addPlantFood() {
        // takes 1 hour
        spendTime(1);

        updateTrueMousePosition();

        // plays the plant food animation
        playAnimation(loadImage("Animations/PlantFoodAnimation.gif"), (int) mousePos.getX() - 25,
                (int) mousePos.getY() - 52,  50, 105,1083);

        Timer stallTimer = new Timer(1083, e -> {
            // grows [1-30] available plants
            int plantFood = (int) (Math.random() * 30) + 1;
            resources.put("Plants", resources.get("Plants") + plantFood);
            updateResourceLabels();

            // displays bugs collected on a popup
            displayPopup("You grew " + plantFood + " plants!");
            // updates the rockBed image
            updatePlantLevels();
        });

        stallTimer.setRepeats(false);
        stallTimer.start();
    }

    // changes the amount of algae on the rocks according to the amount of plants you have
    public void updatePlantLevels() {
        int numPlants = resources.get("Plants");

        if (numPlants >= 50) {
            rockBed.setIcon(loadImage("Pictures/RockBed2.png"));
        }
        else if (numPlants > 20){
            rockBed.setIcon(loadImage("Pictures/RockBed1.png"));
        }
        else {
            rockBed.setIcon(loadImage("Pictures/RockBed0.png"));
        }
    }


    // - - - L I L Y  P A D S - - - //

    public void plantLilyPad(Point slotPos) {
        // takes 1 hour
        spendTime(1);

        JLabel lilyPad = new JLabel();
        // sets the image to the floating seed
        lilyPad.setIcon(loadImage("Pictures/SeedDay1.png"));
        lilyPad.setSize(lilyPad.getPreferredSize());
        // puts it into the location of the slot label
        lilyPad.setLocation((int) slotPos.getX(), 203 - lilyPad.getHeight() + 5);
        layeredPane.add(lilyPad, Integer.valueOf(2));
        lilyPads.put(lilyPad, 1);
    }

    public void growLilyPads() {
        for (Map.Entry<JLabel, Integer> entry : lilyPads.entrySet()) {
            JLabel lilyPad = entry.getKey();
            int day = entry.getValue();

            if (day == 1) {
                lilyPad.setIcon(loadImage("Pictures/SeedDay2.png"));
                // updates the map
                lilyPads.put(lilyPad, lilyPads.get(lilyPad) + 1);
            }
            else if (day == 2) {
                // 50% chance to add a flower to the lily pad
                if ((int) (Math.random() * 2) == 1) {
                    lilyPad.setIcon(loadImage("Pictures/LilyPadWithLotus.png"));
                }
                else {
                    lilyPad.setIcon(loadImage("Pictures/LilyPad.png"));
                }

                // updates the size and location of the lily pad so it rests slightly in the water
                lilyPad.setSize(lilyPad.getPreferredSize());
                lilyPad.setLocation(lilyPad.getX(), 203 - lilyPad.getHeight() + 1);

                // updates the map
                lilyPads.put(lilyPad, lilyPads.get(lilyPad) + 1);
            }
        }
    }

    public void invertLilyPadSlotVisibility() {
        // flips the visibility of all unoccupied lily pad slots
        // visible -> invisible || invisible -> visible
        for (JLabel slot : availableLilyPadSlots) {
            slot.setVisible(!slot.isVisible());
        }
    }


    // - - - D A Y  S Y S T E M - - - //

    // feeds frogs and tadpoles with food collected and runs death calculations
    public void endDay() {
        /*
            End of Day:
            Frogs need to eat
            Tadpoles need to eat
            Frog and tadpole death needs to be calculated
        */

        // feeds the frogs
        int numBugs = resources.get("Bugs");

        // feeds the tadpoles
        int numPlants = resources.get("Plants");

        // calculates the death of each tadpole

        // calculates the death of each frog

    }

    public void startDay() {
        /*
            Start of new Day:
            Tadpoles need to hatch
            Tadpoles need to grow
            Eggs need to be laid
            Lily pads need to grow
            Water needs to be dirtied
            Frogs will be randomly distributed between the lily pads, water, and burrows
        */
    }


    public void displayDaySummary() {

    }


    // - - - M I S C E L L A N E O U S - - - //

    // checks for valid files paths
    public ImageIcon loadImage(String filePath) {
        // if the file path exists it returns the referenced ImageIcon
        if (Files.exists(Path.of(filePath))) {
            return new ImageIcon(filePath);
        }
        // if the path does not exist it prints out the file path and throws a NullPointerException
        throw new NullPointerException(filePath + " is an invalid file path");
    }

    // spawns n amount of frogs
    public int spawnFrogs(int n) {
        if (n == 0) {
            return 0;
        }
        else {
            // creates the Frog object
            Frog swimmingFrog = new Frog();
            // edits the display label from that Frog object
            JLabel frogLabel = swimmingFrog.getDisplayLabel();
            // adds a MouseListener so the frog's name can be displayed when hovered over
            frogLabel.addMouseListener(this);

            // needs to start as a tadpole and then grow into a frog

            // adds the tadpole to the 'tadpoles' map
            tadpoles.put(frogLabel, swimmingFrog);
            // immediately grows the frog into stage 1: "Frog"
            swimmingFrog.grow();
            // moves new frogs from the 'tadpoles' map to the 'frogs' map after growth
            moveTadpolesToFrogMap();

            // makes the frog start swimming
            swimmingFrog.startSwimming();

            // adds it to the screen between layers 1-4
            layeredPane.add(frogLabel, Integer.valueOf((int) (Math.random() * 4) + 1));

            return spawnFrogs(n - 1);
        }
    }

    public int spawnTadpoles(int n) {
        if (n == 0) {
            return 0;
        }
        else {
            // creates the Frog object
            Frog tadpole = new Frog();
            // edits the display label from that Frog object
            JLabel tadpoleLabel = tadpole.getDisplayLabel();
            // adds a MouseListener so the frog's name can be displayed when hovered over
            tadpoleLabel.addMouseListener(this);
            // adds the tadpole to the tadpole map
            tadpoles.put(tadpoleLabel, tadpole);
            // makes the tadpole start swimming
            tadpole.startSwimming();

            // adds it to the screen between layers 1-4
            layeredPane.add(tadpoleLabel, Integer.valueOf((int) (Math.random() * 4) + 1));

            return spawnTadpoles(n - 1);
        }
    }

    public void displayFrogName(JLabel frogLabel) {
        Frog frog = frogs.get(frogLabel);
        // if the Frog (object) is still a tadpole
        if (frog == null) {
            frog = tadpoles.get(frogLabel);
        }

        frogNameLabel = new JLabel(frog.getName());
        // sets the font to size 10 plain "Serif"
        frogNameLabel.setFont(new Font("Serif", Font.PLAIN, 10));
        // centers the text
        frogNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // adjusts the width so the font can fit
        int adjustedWidth = (int) frogNameLabel.getPreferredSize().getWidth() + 5;
        // updates the size
        frogNameLabel.setSize(adjustedWidth, (int) frogNameLabel.getPreferredSize().getHeight());
        // aligns the centers of the frogNameLabel and frog label
        int xPos = frogLabel.getX() + (frogLabel.getWidth() - frogNameLabel.getWidth()) / 2;
        frogNameLabel.setLocation(xPos, frogLabel.getY() - frogNameLabel.getHeight());
        layeredPane.add(frogNameLabel, Integer.valueOf(5));

        // keeps the position of the frogNameLabel synced with the position of the frog label
        namePosUpdater = new Timer(1, e -> {
            int newXPos = frogLabel.getX() + (frogLabel.getWidth() - frogNameLabel.getWidth()) / 2;
            frogNameLabel.setLocation(newXPos, frogLabel.getY() - frogNameLabel.getHeight());
        });
        namePosUpdater.start();
    }

    public boolean thereIsEnoughTime(int additiveHours) {
        return true; // TODO: figure this shit out, I'm too tired rn
    }

    public void removeSlot(String slotName) {
        // finds the slot with the same name and removes it from the ArrayList
        for (int i = 0; i < availableLilyPadSlots.size(); i++) {
            if (availableLilyPadSlots.get(i).getName().equals(slotName)) {
                availableLilyPadSlots.remove(i);
                System.out.println("Removed slot " + i);
                break;
            }
        }
    }

    public void updateTrueMousePosition() {
        // updates the mouses position
        mousePos = new Point((int) getMousePosition().getX(), (int) getMousePosition().getY() - 25);
    }

    // displays a popup, ex: "You Collect 10 Bugs!"
    public void displayPopup(String text) {
        translucentPane = new JLabel();
        translucentPane.setHorizontalAlignment(SwingConstants.LEFT);
        translucentPane.setVerticalAlignment(SwingConstants.TOP);

        translucentPane.setIcon(new ImageIcon(getLightenedBackground()));
        translucentPane.setName("TranslucentPane");
        translucentPane.setSize(700, 700);
        translucentPane.setLocation(0, 0);
        translucentPane.setOpaque(false);
        translucentPane.addMouseListener(this);
        translucentPane.setVisible(true);
        layeredPane.add(translucentPane, Integer.valueOf(7));

        popup = new JLabel();
        popup.setName("Popup");
        popup.setText(text);
        popup.setHorizontalTextPosition(SwingConstants.CENTER);
        popup.setFont(new Font("Serif", Font.PLAIN, 30));

        // scales the size of the image to the preferred size of the popup
        int prefWidth = (int) popup.getPreferredSize().getWidth() + 30;
        int prefHeight = (int) popup.getPreferredSize().getHeight() + 20;

        ImageIcon image = loadImage("Pictures/PopupFrame.png");
        Image scaledImage = image.getImage().getScaledInstance(prefWidth, prefHeight, Image.SCALE_REPLICATE);
        popup.setIcon(new ImageIcon(scaledImage));

        popup.setSize(popup.getPreferredSize());
        // centers the popup in the screen
        popup.setLocation(350 - popup.getWidth() / 2, 350 - popup.getHeight() / 2);
        popup.addMouseListener(this);
        popup.setOpaque(true);
        popup.setVisible(true);
        layeredPane.add(popup, Integer.valueOf(8));
    }


    public BufferedImage getLightenedBackground() {
        BufferedImage image = new BufferedImage(700, 700, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Paint the container
        layeredPane.paint(g2d);

        // Overlay a translucent white rectangle
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 700, 700);

        g2d.dispose();

        return image;
    }

    // moves grown tadpoles (frogs) into the 'frogs' map
    public void moveTadpolesToFrogMap() {
        // traverses the 'frogs' map and moves stage 1 tadpoles (frogs) into the 'frogs' map
        for (Map.Entry<JLabel, Frog> entry : tadpoles.entrySet()) {
            JLabel frogLabel = entry.getKey();
            Frog frog = entry.getValue();

            // if the Frog (object) is a frog then it gets moved into the 'frogs' map
            if (frog.getStage().equals("Frog")) {
                // removes the now frog from the 'tadpoles' map
                tadpoles.remove(frogLabel);
                // puts the frog into the 'frogs' map
                frogs.put(frogLabel, frog);
            }
        }
    }

    // returns the dirtiness level of the water
    public int getWaterDirtiness() {
        return waterDirtiness;
    }


    // - - - M O U S E  L I S T E N E R - - - //

    @Override
    public void mouseClicked(MouseEvent e) {
        // gets the component pressed
        Object object = e.getSource();

        // if the object is a JLabel
        if (object instanceof JLabel label) {
            // if the JLabel being entered has a name
            // (all JLabels with mouseListeners added should be named)
            if (label.getName() != null) {
                String name = label.getName();

                // if clicking the next day
                if (name.equals("NextDayButton")) {
                    // TODO: go to next day
                    growLilyPads();
                }
                // if clicking the condense label
                else if (name.equals("CondenseToggle")) {
                    condenseResourceLabels();
                }
                // if clicking the expand label
                else if (name.equals("ExpandToggle")) {
                    expandResourceLabels();
                }
                // if clicking on a resource label
                else if (name.equals("ResourceLabel")) {
                    switchSelectedResourceLabel(label);
                }
                // if clicking on the glass pane in front of the pop-up
                else if (name.equals("TranslucentPane") || name.equals("Popup")) {
                    // removes the glass pane
                    translucentPane.setVisible(false);
                    layeredPane.remove(translucentPane);
                    // removes the pop-up
                    popup.setVisible(false);
                    layeredPane.remove(popup);
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseIsPressed = true;

        Object object = e.getSource();

        // if the object is JLabel
        if (object instanceof JLabel label) {
            // if the label has a name (which it should)
            if (label.getName() != null) {
                String name = label.getName();

                if (name.contains("Item")) {
                    switch (name) {
                        case "BugNetItem" :
                            // returns the net back to its unselected ImageIcon
                            label.setIcon(loadImage("Pictures/BugNet.png"));
                            break;

                        case "HammerItem" :
                            // returns the hammer back to its unselected ImageIcon
                            label.setIcon(loadImage("Pictures/Hammer2.png"));
                            break;

                        case "PlantFoodItem" :
                            // returns the plant food back to its unselected ImageIcon
                            label.setIcon(loadImage("Pictures/PlantFood.png"));
                            break;

                        case "LilyPadSeedItem" :
                            // returns the lily pad seed back to its unselected ImageIcon
                            label.setIcon(loadImage("Pictures/LilyPadSeed2.png"));
                            // makes all lily pad slots visible
                            invertLilyPadSlotVisibility();
                            break;

                        case "SpongeItem" :
                            // returns the sponge back to its unselected ImageIcon
                            label.setIcon(loadImage("Pictures/SmallerSponge.png"));
                            break;
                    }
                    // closes the description of the item
                    closeItemDescription();
                    // marks the beginning of the mouse drag
                    updateTrueMousePosition();
                    initialXOffset = (int) (label.getX() - mousePos.getX());
                    initialYOffset = (int) (label.getY() - mousePos.getY());

                    // saves the initial position of the item so it can be returned after use
                    initialItemLocation = label.getLocation();
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseIsPressed = false;

        Object object = e.getSource();

        // if the object is JLabel
        if (object instanceof JLabel label) {
            // if the label has a name (which it should)
            if (label.getName() != null) {
                String name = label.getName();

                if (name.contains("Item")) {

                    updateTrueMousePosition();

                    Point releaseLocation = mousePos;

                    // make label invisible while animation is playing
                    label.setVisible(false);
                    layeredPane.remove(label);

                    // if the mouse is on screen
                    if (releaseLocation != null) {
                        // saves the component
                        Component component = layeredPane.getComponentAt(releaseLocation);
                        // saves the name of the component
                        String componentName = component.getName();
                        System.out.println(component.getClass());

                        System.out.println(releaseLocation);
                        System.out.println(componentName);

                        System.out.println(name);

                        if (componentName != null) {
                            // collect bugs at the release location if within the bounds exposed sky
                            if (sky.contains(releaseLocation) && componentName.equals("Sky") && name.equals("BugNetItem")) {
                                collectBugs();
                                System.out.println("Collecting Bugs...");
                            }
                            // if releasing on a burrow
                            else if (componentName.contains("EmptyBurrow") &&
                                    name.equals("HammerItem")) {
                                System.out.println("Hammering...");
                                buildBurrow(burrows.get(componentName));
                            }
                            // if using the plant food
                            else if ((componentName.equals("Sky") || componentName.equals("Water")) &&
                                    name.equals("PlantFoodItem")) {
                                System.out.println("Fertilizing Plants...");
                                addPlantFood();
                            }
                            else if (name.equals("LilyPadSeedItem")) {

                                if (componentName.contains("Slot")) {
                                    plantLilyPad(component.getLocation());

                                    // removes the now occupied slot from the screen and the ArrayList
                                    removeSlot(componentName);
                                    layeredPane.remove(component);
                                }
                            }
                            // if using the sponge
                            else if (componentName.equals("Water") && name.equals("SpongeItem")) {
                                cleanWater();
                                System.out.println("Cleaning Water...");
                            }
                        }
                    }

                    // this needs to be outside the !null if condition because it needs to trigger no matter what if
                    // the LilyPadSeedItem is used
                    if (name.equals("LilyPadSeedItem")) {
                        // makes all slots invisible
                        invertLilyPadSlotVisibility();
                    }

                    // return item back to its slot in the menu bar
                    label.setLocation(initialItemLocation);
                    label.setVisible(true);
                    layeredPane.add(label);
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // gets the component pressed
        Object object = e.getSource();

        // if the object is a JLabel and the mouse is not actively being pressed
        if (object instanceof JLabel label && !mouseIsPressed) {
            // if the JLabel being entered has a name
            // (all JLabels with mouseListeners added should be named)
            if (label.getName() != null) {
                String name = label.getName();

                // if it's a background label it highlights the border
                if (name.contains("Button") || name.contains("Toggle") || name.equals("ResourceLabel")) {
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
                }
                else if (name.contains("Item")) {
                    // if entering the net item label and the item isn't being dragged
                    if (name.equals("BugNetItem") && label.getY() == 0) {
                        // highlights the border around the actual item
                        label.setIcon(loadImage("Pictures/SelectedNet.png"));
                    }
                    // if entering the hammer item label and the item isn't being dragged
                    else if (name.equals("HammerItem") && label.getY() == 0) {
                        // highlights the border around the actual item
                        label.setIcon(loadImage("Pictures/SelectedHammer.png"));
                    }
                    // if entering the plant food item label and the item isn't being dragged
                    else if (name.equals("PlantFoodItem") && label.getY() == 0) {
                        // highlights the border around the actual item
                        label.setIcon(loadImage("Pictures/SelectedPlantFood.png"));
                    }
                    // if entering the lily pad seed item label and the item isn't being dragged
                    else if (name.equals("LilyPadSeedItem") && label.getY() == 0) {
                        // highlights the border around the actual item
                        label.setIcon(loadImage("Pictures/SelectedLilyPadSeed.png"));
                    }
                    // if entering the sponge item label and the item isn't being dragged
                    else if (name.equals("SpongeItem") && label.getY() == 0) {
                        // highlights the border around the actual item
                        label.setIcon(loadImage("Pictures/SelectedSmallerSponge.png"));
                    }
                    // displays the description of the item
                    openItemDescription(label);
                }
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // gets the component pressed
        Object object = e.getSource();

        // if the object is a JLabel and the mouse isn't being pressed
        if (object instanceof JLabel label && !mouseIsPressed) {

            if (label.getName() != null) {
                String name = label.getName();

                // if label is a background label it removes the highlighting border
                if (name.contains("Button") || name.contains("Toggle") || name.equals("ResourceLabel")) {
                    label.setBorder(null);
                }
                // if label is a frog label it removes the name above its head
                else if (name.equals("FrogLabel")) {
                    frogNameLabel.setVisible(false);
                    layeredPane.remove(frogNameLabel);
                    namePosUpdater.stop();
                }
                // if exiting a burrow
                else if (name.contains("Burrow")) {
                    // deletes the population / capacity JTextArea fraction from the screen
                    populationTextArea.setVisible(false);
                    layeredPane.remove(populationTextArea);
                    System.out.println("Exited Burrow");
                }
                else if (name.contains("Item")) {
                    // if exiting the net item label
                    switch (name) {
                        case "BugNetItem" ->
                            // highlights the border around the actual item
                            label.setIcon(loadImage("Pictures/BugNet.png"));

                        // if exiting the hammer item label
                        case "HammerItem" ->
                            // highlights the border around the actual item
                            label.setIcon(loadImage("Pictures/Hammer2.png"));

                        // if exiting the plant food item label
                        case "PlantFoodItem" ->
                            // highlights the border around the actual item
                            label.setIcon(loadImage("Pictures/PlantFood.png"));

                        // if exiting the lily pad seed item label
                        case "LilyPadSeedItem" ->
                            // highlights the border around the actual item
                            label.setIcon(loadImage("Pictures/LilyPadSeed2.png"));
                        // if exiting the lily pad seed item label

                        case "SpongeItem" ->
                            // highlights the border around the actual item
                            label.setIcon(loadImage("Pictures/SmallerSponge.png"));
                    }
                    // closes the item's description
                    closeItemDescription();
                }
            }
        }
    }

    // - - - M O U S E  M O T I O N  L I S T E N E R - - - //

    @Override
    public void mouseDragged(MouseEvent e) {
        Object object = e.getSource();

        // if the object is JLabel
        if (object instanceof JLabel label) {
            // if the label has a name (which it should)
            if (label.getName() != null) {

                /*
                // TODO: remove once done testing
                JLabel tracker = new JLabel() {
                    @Override
                    public boolean contains(int x, int y) {
                        // return false to make this component transparent to mouse events
                        return false;
                    }
                };

                tracker.setSize(1, 1);
                tracker.setBackground(Color.red);
                tracker.setOpaque(true);
                tracker.setLocation((int) getMousePosition().getX(), (int) getMousePosition().getY() - 25);
                layeredPane.add(tracker, Integer.valueOf(10));

                 */

                String name = label.getName();

                // if the label is any of the items the position gets updated
                if (name.contains("Item") && mousePos != null) {
                    updateTrueMousePosition();
                    label.setLocation((int) mousePos.getX() + initialXOffset,
                            (int) mousePos.getY() + initialYOffset);
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
