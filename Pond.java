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
    // 1-100, 1 being nasty, 100 being crystal clear
    public static int waterCleanliness = 100;
    // times all the background effects
    private Timer backgroundEffectsTimer;

    // tracks the current position of the mouse while items are being dragged
    private Point mousePos;
    // stores the initial location of the item in the menu bar
    private Point initialItemLocation;


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

    // - - - M A P S - - - //

    // holds every type of resource and its quantity
    private Map<String, Integer> resources = new LinkedHashMap<String, Integer>();
    // stores all the frogs and their display labels
    private Map<JLabel, Frog> frogs = new HashMap<JLabel, Frog>();

    // - - - L I S T S  &  A R R A Y S - - - //

    // stores all the display labels
    private ArrayList<JLabel> resourceLabels = new ArrayList<JLabel>();
    // stores all the burrow images
    private final Burrow[] burrows = new Burrow[20];
    // holds all the item in the player's inventory
    private ArrayList<JLabel> items = new ArrayList<JLabel>();

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
        initializeResources();
        // sets the frog stat as the selectedResourceLabel
        initializeSelectedResourceLabel();
        // displays all resource labels
        expandResourceLabels();
        // create the time and time functions in the top right of the frame
        initializeTimeComponents();
        // creates the 5 items for the menu bar
        createItems();
        // adds in the menu bar at the top of the screen
        addMenuBar();
        // starts the bubble animations in the water
        startBackgroundEffects();
        // spawns the 4 initial frogs
        spawnFrogs(4);
        // TODO: remove this once done testing
        addLilyPads();
        // adds all the empty burrow in the ground
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
    public void initializeResources() {
        // number of frogs, tadpoles, eggs, bugs, plant food, and hours spent in total
        resources.put("Frogs", 4);
        resources.put("Tadpoles", 0);
        resources.put("Eggs", 0);
        resources.put("Bugs", 0 /*TBD*/);
        resources.put("Plant Food", 0 /*TBD*/);
        resources.put("Total Hours Spent", 0);
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
        layeredPane.add(frogResourceLabel, Integer.valueOf(1));
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
                layeredPane.add(resourceLabel, Integer.valueOf(1));

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
        layeredPane.add(expandLabel, Integer.valueOf(1));
    }


    public void updateResourceLabels() {
        // updates all resource labels actively being displayed
        for (JLabel resourceLabel : resourceLabels) {
            String text = resourceLabel.getName();
            String resourceName = text.substring(0, text.indexOf(":"));

            // resets the label
            resourceLabel.setText(resourceName + ": " + resources.get(resourceName));
            resourceLabel.setOpaque(true);
            resourceLabel.setBackground(new Color(150,134,46));
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
        int width = 110;

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
        JLabel rocks = new JLabel(loadImage("Pictures/RockBed1.png"));
        rocks.setSize(rocks.getPreferredSize());
        rocks.setLocation(0, 360);
        layeredPane.add(rocks, Integer.valueOf(1));
    }

    // TODO: remove once done
    public void addLilyPads() {
        int randPad;

        // spawns 20 lily pads across the surface of the water
        for (int i = 0; i < 20; i++) {
            randPad = (int) (Math.random() * 4);

            JLabel lilyPad = new JLabel();

            // picks a lily pad at random
            switch (randPad) {
                case 0:
                    lilyPad.setIcon(loadImage("Pictures/LilyPadWithLotusAndStem1.png"));
                    break;

                case 1:
                    lilyPad.setIcon(loadImage("Pictures/LilyPadWithLotusAndStem2.png"));
                    break;

                case 2:
                    lilyPad.setIcon(loadImage("Pictures/LilyPadWithStem1.png"));
                    break;

                case 3:
                    lilyPad.setIcon(loadImage("Pictures/LilyPadWithStem2.png"));
                    break;

                default:
                    System.out.println("You messed the randomization up");
                    break;
            }

            lilyPad.setSize(lilyPad.getPreferredSize());
            // evenly spaces out each lily pad across 700 pixels
            lilyPad.setLocation(i * 30 + i * 7, 203 - lilyPad.getHeight() + 307);
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
        layeredPane.add(populationTextArea, Integer.valueOf(2));
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

    // - - - I T E M  &  A C T I O N  M E T H O D S - - - //

    public void createItems() {
        createItem("BugNet", "Pictures/BugNet.png");
        createItem("Hammer", "Pictures/Hammer2.png");
        createItem("PlantFood", "Pictures/PlantFood.png");
        for (int i = 0; i < 2; i++) {
            createItem("LilyPadSeed", "Pictures/LilyPadSeed2.png");
        }
        // TODO: create all the other items
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

        if (item.getName().equals("BugNet")) {
            itemDescription.setText("Collect Bugs:\nCost: 1 Hour");
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

    public void collectBugs() {
        // takes 1 hour
        spendTime(1);
    }

    public void cleanWater() {
        // takes 3 hours
        spendTime(3);
    }

    public void buildBurrow() {
        // takes 5 hours
        spendTime(5);
    }

    public void addPlantFood() {
        // takes 1 hour
        spendTime(1);
    }

    public void plantLilyPad() {
        // takes 1 hour
        spendTime(1);
    }

    public void showLilyPadBorders() {

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
        namePosUpdater = new Timer(1, e1 -> {
            int newXPos = frogLabel.getX() + (frogLabel.getWidth() - frogNameLabel.getWidth()) / 2;
            frogNameLabel.setLocation(newXPos, frogLabel.getY() - frogNameLabel.getHeight());
        });
        namePosUpdater.start();
    }

    // - - - M O U S E L I S T E N E R  M E T H O D S - - - //

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
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Object object = e.getSource();

        // if the object is JLabel
        if (object instanceof JLabel label) {
            // if the label has a name (which it should)
            if (label.getName() != null) {
                String name = label.getName();

                if (name.equals("BugNet")) {
                    // returns the net back to its unselected ImageIcon
                    label.setIcon(loadImage("Pictures/BugNet.png"));
                    // closes the description of the item
                    closeItemDescription();
                    // marks the beginning of the mouse drag
                    mousePos = getMousePosition();
                    // saves the initial position of the item so it can be returned after use
                    initialItemLocation = label.getLocation();
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Object object = e.getSource();

        // if the object is JLabel
        if (object instanceof JLabel label) {
            // if the label has a name (which it should)
            if (label.getName() != null) {
                String name = label.getName();

                if (name.equals("BugNet")) {
                    Point releaseLocation = label.getLocation();

                    // make label invisible while animation is playing
                    label.setVisible(false);
                    layeredPane.remove(label);

                    System.out.println(layeredPane.getComponentAt(releaseLocation).getName());
                    // collect bugs at the release location if within the bounds exposed sky
                    if (sky.contains(releaseLocation) && layeredPane.getComponentAt(releaseLocation).equals(sky)) {
                        collectBugs();
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

        // if the object is a JLabel
        if (object instanceof JLabel label) {
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
                // if entering the net item label and the item isn't being dragged
                else if (name.equals("BugNet") && label.getY() == 0) {
                    // highlights the border around the actual item
                    label.setIcon(loadImage("Pictures/SelectedNet.png"));
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

        // if the object is a JLabel
        if (object instanceof JLabel label) {

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
                // if exiting the net item label
                else if (name.equals("BugNet")) {
                    // highlights the border around the actual item
                    label.setIcon(loadImage("Pictures/BugNet.png"));
                    // closes the item's description
                    closeItemDescription();
                }
            }
        }
    }

    // - - - M O U S E  M O T I O N  L I S T E N E R  M E T H O D S - - - //

    @Override
    public void mouseDragged(MouseEvent e) {
        Object object = e.getSource();

        // if the object is JLabel
        if (object instanceof JLabel label) {
            // if the label has a name (which it should)
            if (label.getName() != null) {
                String name = label.getName();

                if (name.equals("BugNet") && getMousePosition() != null) {
                    // calculates the change is mouse position
                    int changeInX = (int) (getMousePosition().getX() - mousePos.getX());
                    int changeInY = (int) (getMousePosition().getY() - mousePos.getY());
                    // updates the mouse position
                    mousePos = getMousePosition();
                    // applies changes in x and y to item position
                    int newItemX = label.getX() + changeInX;
                    int newItemY = label.getY() + changeInY;
                    label.setLocation(newItemX, newItemY);
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
