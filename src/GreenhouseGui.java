import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;


// The gui class that populates data from the controller and acts as
// an interface between the user and the controller.
public class GreenhouseGui  {

    public static void main(String[] args){
        GreenhouseGui gc = new GreenhouseGui(true);
    }



    private GreenhouseControls currentController;

    // GUI Elements
    private JFrame mainWindowFrame;

    private JTextArea eventTextArea;
    private JTextField currentFileField;
    private TitledBorder currentFileFieldBorder;

    private JButton startButton;
    private JButton suspendButton;
    private JButton resumeButton;
    private JButton terminateButton;
    private JButton restartButton;

    private ImageIcon exitIcon;
    private ImageIcon newIcon;
    private ImageIcon openIcon;
    private ImageIcon saveIcon;
    private ImageIcon restartIcon;
    private ImageIcon terminateIcon;
    private ImageIcon startIcon;
    private ImageIcon resumeIcon;
    private ImageIcon suspendIcon;
    private ImageIcon greenhouseIcon;

    private JScrollPane eventTextAreaScrollPane;

    private JMenuBar topFileMenu;

    // Popup menu gui elements
    private JPopupMenu rightClickPopupMenu;

    private JMenuItem startPopup;
    private JMenuItem resumePopup;
    private JMenuItem terminatePopup;
    private JMenuItem suspendPopup;
    private JMenuItem restartPopup;

    private GridBagConstraints gridBagConstraints;

    private JFileChooser eventFileChooser;
    private JFileChooser dumpFileChooser;

    // Used for determining what the main window is.
    // When the "close" button is selected the gui needs
    // to determine if it should close just the window or
    // exit the program all together.
    private boolean isMainWindow;

    // Different scanners used to read and parse various
    // data from input file.
    private Scanner populateActionsScanner;

    // Display on the gui what the currently running event file is.
    private String currentEventFileName;

    public GreenhouseGui(boolean mainWindow){
        this.isMainWindow = mainWindow;

        currentController = new GreenhouseControls(this);
        initializeGUI();
    }

    public void updateEventText(String text){
        eventTextArea.append(text);
    }


    private void setCurrentController(GreenhouseControls gc){
        this.currentController = gc;
    }

    private void initializeGUI(){

        mainWindowFrame = new JFrame();

        // Helper functions to keep code organized.
        createIcons();
        createMenuBar();
        createGuiElements();
        createPopupMenu();

        // Setup main window
        mainWindowFrame.setTitle("Greenhouse Controller© - Leacock Industries");
        mainWindowFrame.setIconImage(greenhouseIcon.getImage());
        mainWindowFrame.setPreferredSize(new Dimension(800, 600));
        mainWindowFrame.setLocationRelativeTo(null);
        mainWindowFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainWindowFrame.setResizable(false);

        createLayoutAndDisplay(eventTextAreaScrollPane, startButton, restartButton,
                terminateButton, suspendButton, resumeButton,  currentFileField);

    }
    // Instantiate icons, formatted for 20x20 pixel sizes
    private void createIcons(){
        greenhouseIcon= new ImageIcon("src/resources/leafIcon.png");
        exitIcon = new ImageIcon("src/resources/exitIcon.png");
        newIcon = new ImageIcon("src/resources/newIcon.png");
        openIcon = new ImageIcon("src/resources/openIcon.png");
        saveIcon = new ImageIcon("src/resources/saveIcon.png");
        restartIcon = new ImageIcon("src/resources/restartIcon.png");
        terminateIcon = new ImageIcon("src/resources/terminateIcon.png");
        startIcon = new ImageIcon("src/resources/startIcon.png");
        suspendIcon = new ImageIcon("src/resources/suspendIcon.png");
        resumeIcon = new ImageIcon("src/resources/resumeIcon.png");
    }
    // Create menu file options, tool-tips and keyboard shortcuts
    private void createMenuBar(){

        topFileMenu = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem exitMenuItem = new JMenuItem("Exit", exitIcon);
        exitMenuItem.setMnemonic(KeyEvent.VK_E);
        exitMenuItem.setToolTipText("Exit Program");
        exitMenuItem.addActionListener((ActionEvent event) -> {
            if(currentController.isRunning()){
                int response = JOptionPane.showConfirmDialog(null,
                        "Exit Program?",
                        "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                    return;
                } else if (response == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
            System.exit(0);
        });

        JMenuItem newMenuItem = new JMenuItem("New Window", newIcon);
        newMenuItem.setMnemonic(KeyEvent.VK_E);
        newMenuItem.setToolTipText("New Window");
        newMenuItem.addActionListener((ActionEvent event) -> {
            // Create a new greenhouse controller and it's window, but set
            // it as not the main window.
            new GreenhouseControls(this);

        });

        JMenuItem saveMenuItem = new JMenuItem("Close", saveIcon);
        saveMenuItem.setMnemonic(KeyEvent.VK_E);
        saveMenuItem.setToolTipText("Close current controller");
        saveMenuItem.addActionListener((ActionEvent event) -> {

            if(currentController.isRunning()){
                int response = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to close the current controller?",
                        "Confirm Close", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                    return;
                } else if (response == JOptionPane.YES_OPTION) {
                    closeWindow();
                }
            }
            closeWindow();
        });

        JMenuItem openMenuItem = new JMenuItem("Open Events", openIcon);
        openMenuItem.setMnemonic(KeyEvent.VK_E);
        openMenuItem.setToolTipText("Open Program");
        openMenuItem.addActionListener((ActionEvent event) -> {
            openCheckAndReadEventFile(eventFileChooser.showOpenDialog(mainWindowFrame));
        });

        JMenuItem restartMenuItem = new JMenuItem("Restore", restartIcon);
        restartMenuItem.setMnemonic(KeyEvent.VK_E);
        restartMenuItem.setToolTipText("Restore Program");
        restartMenuItem.addActionListener((ActionEvent event) -> {
            openCheckAndReadDumpFile(dumpFileChooser.showOpenDialog(mainWindowFrame));

        });

        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(restartMenuItem);
        fileMenu.add(exitMenuItem);

        topFileMenu.add(fileMenu);

        mainWindowFrame.setJMenuBar(topFileMenu);
    }

    // Get the file, inspect it's extension and make sure it's the right kind.
    // Set the gui to display the file being used then start reading the file
    // parse it and send that information off to the controller.
    private void openCheckAndReadDumpFile(int result){
        if(result == JFileChooser.APPROVE_OPTION){
            File dumpFile = dumpFileChooser.getSelectedFile();
            String dumpFilePath = dumpFile.toString();
            //Check to make sure the right Dump file type was read
            //Must be a .out
            String extension = dumpFilePath.substring(dumpFilePath.lastIndexOf(".") + 1);
            if(!extension.equals("out")){
                currentFileField.setText("Please select a Dump (.out) file");
                return;
            }
            //Set the gui element to display the current file
            currentFileField.setText(dumpFilePath);
            readGreenhouseControllerStatesDumpFile(dumpFilePath);
        }
    }

    // Get the file, inspect it's extension and make sure it's the right kind.
    // Set the gui to display the file being used then start reading the file
    // parse it and send that information off to the controller.
    private void openCheckAndReadEventFile(int result){
        if(result == JFileChooser.APPROVE_OPTION){
            File eventFile = eventFileChooser.getSelectedFile();
            String eventFilePath = eventFile.toString();
            //Check to make sure the right Event file type was read
            //Must be a .evt
            String extension = eventFilePath.substring(eventFilePath.lastIndexOf(".") + 1);
            if(!extension.equals("evt")){
                currentFileField.setText("Please select a Event (.evt) file");
                return;
            }
            //Set the gui element to display the current file
            currentFileField.setText(eventFile.toString());
            try {
                populateActionsScanner = new Scanner(eventFile);
                while (populateActionsScanner.hasNextLine()){
                    String event = populateActionsScanner.nextLine();
                    String[] words = event.split("\\s+");
                    if(words.length == 2){
                        currentController.addEvent(words[0], new Long(words[1]));
                    } else if (words.length == 3){
                        currentController.addRepeatingEvent(words[0],
                                new Long(words[1]),
                                new Integer(words[2]));
                    } else {
                        currentFileField.setText("Incorrect event formatting.");
                    }
                }
                startButton.setEnabled(true);
                startPopup.setEnabled(true);

            } catch (FileNotFoundException e){
                System.out.println("Exception during reading event file: " + e.toString());
            }
        }
    }

    // When closing a window check to make sure if the window represents
    // the main (original) greenhouse controller. If it isn't then we want
    // to dispose of the window and stop the events. If it is the main window
    // then we want to exit the entire program.
    private void closeWindow(){
        if(isMainWindow){
            mainWindowFrame.dispatchEvent(new WindowEvent(mainWindowFrame, WindowEvent.WINDOW_CLOSING));
        } else {
            mainWindowFrame.setVisible(false);
            mainWindowFrame.dispose();
            currentController.shutdownController();
        }
    }

    // Each button gets an action event listener for button
    // presses then makes two method calls, one to the parent controller
    // to change it's state, the other updates the gui itself.
    // Enabling and Disabling buttons. When the window is first
    // created only the Start button should be enabled.
    private void createGuiElements(){

        gridBagConstraints = new GridBagConstraints();

        // Create text area for events to be displayed and add them to
        // a pane with a scroll ball. Disable user editing of text area.
        eventTextArea = new JTextArea();
        eventTextAreaScrollPane = new JScrollPane(eventTextArea);
        eventTextArea.setEditable(false);

        // No event file by default is loaded.
        currentEventFileName = "";
        // Use the default Java-style font but make it larger and bold.
        // Set it to the text area that displays the current file running.
        Font greenhouseFont = new Font("Dialog", Font.BOLD, 20);
        currentFileField = new JTextField();
        currentFileField.setFont(greenhouseFont);
        currentFileField.setText(currentEventFileName);
        currentFileField.setEditable(false);
        currentFileField.setHorizontalAlignment(SwingConstants.CENTER);

        startButton = new JButton("Start");
        startButton.addActionListener((ActionEvent event) -> {
            start();
            updateGUI();
        });
        startButton.setIcon(startIcon);
        startButton.setEnabled(false);

        suspendButton = new JButton("Suspend");
        suspendButton.addActionListener((ActionEvent event) -> {
            eventTextArea.append("**** SUSPEND REQUESTED ****\n\r");
            suspend();
            updateGUI();
        });
        suspendButton.setIcon(suspendIcon);
        suspendButton.setEnabled(false);

        resumeButton = new JButton("Resume");
        resumeButton.addActionListener((ActionEvent event) -> {
            eventTextArea.append("**** RESUME REQUESTED ****\n\r");
            resume();
            updateGUI();
        });
        resumeButton.setIcon(resumeIcon);
        resumeButton.setEnabled(false);

        terminateButton = new JButton("Terminate");
        terminateButton.addActionListener((ActionEvent event) -> {
            eventTextArea.append("**** TERMINATION REQUESTED ****\n\r");
            shutdown();
            outputGreenhouseStates();
            updateGUI();
        });
        terminateButton.setIcon(terminateIcon);
        terminateButton.setEnabled(false);

        restartButton = new JButton("Restart");
        restartButton.addActionListener((ActionEvent event) -> {
            eventTextArea.append("**** RESTART REQUESTED ****\n\r");
            restart();
            updateGUI();
        });
        restartButton.setIcon(restartIcon);
        restartButton.setEnabled(false);

        // Popup window to select an event file to run.
        eventFileChooser = new JFileChooser();
        eventFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        eventFileChooser.setFileFilter(new FileNameExtensionFilter(
                "Event Files (.evt)", "evt"));

        dumpFileChooser = new JFileChooser();
        dumpFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        dumpFileChooser.setFileFilter( new FileNameExtensionFilter(
                "Dump Files (.out)", "out"));

    }

    // Helper function to set all the buttons based on what state the controller
    // is in.
    private void updateGUI(){
        if(currentController.isSuspended()){
            // Panel buttons
            startButton.setEnabled(false);
            restartButton.setEnabled(true);
            terminateButton.setEnabled(true);
            suspendButton.setEnabled(false);
            resumeButton.setEnabled(true);
            // Popup buttons
            startPopup.setEnabled(false);
            restartPopup.setEnabled(true);
            terminatePopup.setEnabled(true);
            suspendPopup.setEnabled(false);
            resumePopup.setEnabled(true);
        } else {
            //Panel buttons
            startButton.setEnabled(false);
            restartButton.setEnabled(true);
            terminateButton.setEnabled(true);
            suspendButton.setEnabled(true);
            resumeButton.setEnabled(false);
            // Popup buttons
            startPopup.setEnabled(false);
            restartPopup.setEnabled(true);
            terminatePopup.setEnabled(true);
            suspendPopup.setEnabled(true);
            resumePopup.setEnabled(false);
        }
    }

    // Create the right-click popup menu and set which
    // methods need to be called based on what was clicked.
    private void createPopupMenu(){

        rightClickPopupMenu = new JPopupMenu();
        ActionListener popupMenuListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if(event.getActionCommand().equals("Start")){
                    start();
                    updateGUI();
                } else if(event.getActionCommand().equals("Resume")) {
                    eventTextArea.append("**** RESUME REQUESTED ****\n\r");
                    resume();
                    updateGUI();
                } else if(event.getActionCommand().equals("Terminate")) {
                    eventTextArea.append("**** TERMINATE REQUESTED ****\n\r");
                    shutdown();
                    outputGreenhouseStates();
                    updateGUI();
                } else if(event.getActionCommand().equals("Suspend")) {
                    suspend();
                    updateGUI();
                } else if(event.getActionCommand().equals("Restart")) {
                    eventTextArea.append("**** RESTART REQUESTED ****\n\r");
                    restart();
                    updateGUI();
                }
            }
        };
        startPopup = new JMenuItem("Start", startIcon);
        startPopup.setHorizontalTextPosition(JMenuItem.RIGHT);
        startPopup.addActionListener(popupMenuListener);
        startPopup.setEnabled(false);

        resumePopup = new JMenuItem("Resume", resumeIcon);
        resumePopup.setHorizontalTextPosition(JMenuItem.RIGHT);
        resumePopup.addActionListener(popupMenuListener);
        resumePopup.setEnabled(false);

        terminatePopup = new JMenuItem("Terminate", terminateIcon);
        terminatePopup.setHorizontalTextPosition(JMenuItem.RIGHT);
        terminatePopup.addActionListener(popupMenuListener);
        terminatePopup.setEnabled(false);

        suspendPopup = new JMenuItem("Suspend", suspendIcon);
        suspendPopup.setHorizontalTextPosition(JMenuItem.RIGHT);
        suspendPopup.addActionListener(popupMenuListener);
        suspendPopup.setEnabled(false);

        restartPopup = new JMenuItem("Restart", restartIcon);
        restartPopup.setHorizontalTextPosition(JMenuItem.RIGHT);
        restartPopup.addActionListener(popupMenuListener);
        restartPopup.setEnabled(false);

        rightClickPopupMenu.add(startPopup);
        rightClickPopupMenu.add(resumePopup);
        rightClickPopupMenu.add(terminatePopup);
        rightClickPopupMenu.add(suspendPopup);
        rightClickPopupMenu.add(restartPopup);

        rightClickPopupMenu.setBorder(new BevelBorder(BevelBorder.RAISED));

        mainWindowFrame.addMouseListener(new PopupMenuMouseListener());
    }
    class PopupMenuMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e){
            checkPopup(e);
        }
        @Override
        public void mouseClicked(MouseEvent e){
            checkPopup(e);
        }
        @Override
        public void mouseReleased(MouseEvent e){
            checkPopup(e);
        }
        private void checkPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                rightClickPopupMenu.show(mainWindowFrame, e.getX(), e.getY());
            }
        }
    }

    // Add the components into a layout and display them.
    // Reusing and resetting the constraints for each section
    // of the layout.
    // Here is the order of the elements at the time of writing
    // eventTextAreaScrollPane, startButton, restartButton,
    // terminateButton, suspendButton, resumeButton, currentFileField.
    private void createLayoutAndDisplay(JComponent... args){

        mainWindowFrame.setLayout(new GridBagLayout());

        // Greenhouse Event Info Display
        gridBagConstraints.insets = new Insets(10, 5, 10, 1);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.gridheight = 10;
        gridBagConstraints.gridwidth = 5;
        addComponent(args[0], 0, 0);

        // Control buttons
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        addComponent(args[1], 6, 0);
        addComponent(args[2], 6, 1);
        addComponent(args[3], 6, 2);
        addComponent(args[4], 6, 3);
        addComponent(args[5], 6, 4);

        // File information and setting
        // the border.
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(40, 10, 40, 0);
        addComponent(args[6], 0, 11);
        currentFileFieldBorder = BorderFactory.createTitledBorder(
                null, "Current File", TitledBorder.LEFT,
                TitledBorder.TOP, new Font("Dialog",Font.BOLD,16),
                Color.BLACK);
        args[6].setBorder(currentFileFieldBorder);

        mainWindowFrame.pack();
        mainWindowFrame.setVisible(true);

    }
    // Small helper method for reused code. Takes in
    // gui component and a x,y position and adds it to
    // the main frame.
    private void addComponent(Component component, int x, int y) {
        gridBagConstraints.gridx = x;
        gridBagConstraints.gridy = y;
        mainWindowFrame.add(component, gridBagConstraints);
    }

    // Communicate to the controller that it needs to change state
    // based on interactions with the gui.
    public void start(){
        currentController.start();
    }
    public void restart(){
        currentController.restart();
    }
    public void suspend(){
        currentController.suspend();
    }
    public void resume(){
        currentController.resume();
    }
    public void shutdown() {currentController.shutdown();}


    // Output the current state of what's inside the Greenhouse
    public void outputGreenhouseStates(){
        Iterator<GreenhouseControls.ControllerState> itr = getControllerState().iterator();
        while(itr.hasNext()){
            eventTextArea.append(itr.next().toString() + "\n\r");
        }
    }

    // Helper method to get the list of ControllerStates from the controller.
    public List<GreenhouseControls.ControllerState> getControllerState(){
        return currentController.getControllerState();
    }

    // Read the dump file and create a new Greenhouse Controller. Couple
    // the Controller to the GUI and vice versa. Output to the event text area
    // of what's going on. Call the controller to repair anything and then
    // resume where it left off.
    private void readGreenhouseControllerStatesDumpFile(String objectState){
        try {

            // Read serialized file. Display current state of greenhouse then resume
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(objectState));
            GreenhouseControls gc = (GreenhouseControls)in.readObject();
            setCurrentController(gc);
            currentController.setGui(this);

            eventTextArea.append("**********************************************************\n\r");
            eventTextArea.append("                      Repairing Issue                     \n\r");
            eventTextArea.append("**********************************************************\n\r");

            currentController.fixGreenhouseController();

            eventTextArea.append("**********************************************************\n\r");
            eventTextArea.append("                  Issue Repaired - Resuming               \n\r");
            eventTextArea.append("**********************************************************\n\r");

            currentController.resumeAfterCrash();

        } catch (Exception e){
            System.out.println("Exception during reading of serialized file of controller: " + e.toString());
        }
    }
} ///:~