/*
    Finley Meadows & Michael Anderson
    12/2/2024
    The GameRunner class calls the Pond class constructor runs the game
*/

/*
    Note to future peeps:
    Changes cannot be directly pushed to the git repo from school computers but the files still save.
    So, you can just reopen the files on a pc and commit the changes made by the school computer.
*/

public class GameRunner {

    // stores the Pond object in a way that can be accessed from multiple classes
    public static Pond pond;

    public static void main(String[] args) {
        // creates the window and starts the game
        pond = new Pond();
    }
}
