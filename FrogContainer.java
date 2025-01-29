/*
    Finley Meadows & Michael Anderson
    12/6/2025
    The FrogContainer class helps lily pads and burrows keep track of
    the number of frogs stored within them
*/

import javax.swing.*;
import java.util.ArrayList;

public class FrogContainer {
    // stores all the frogs in the burrow
    private ArrayList<Frog> frogs = new ArrayList<Frog>();
    // stores the number of frogs in the container out of 5
    private int numFrogs = 0;
    // stores the container's image
    private ImageIcon image;
    // stores the container's JLabel on screen
    private JLabel displayLabel = new JLabel();

    public FrogContainer() {

    }

    public void addFrog(Frog frog) {
        if (numFrogs < 5) {
            frogs.add(frog);
            numFrogs++;
        }
        else {
            System.out.println("This FrogContainer is full!");
        }
    }

    public void removeFrog(Frog frog) {
        frogs.remove(frog);
        numFrogs--;
    }

    public JLabel getDisplayLabel() {
        return displayLabel;
    }

    public int getNumFrogs() {
        return numFrogs;
    }
}
