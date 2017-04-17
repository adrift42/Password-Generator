package model.view.controller;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * To do:
 * 
 * Include debugging/logging option in menu bar
 * include a field or panel for debugging values to be shown - i.e. number of attempts taken for the password generation,
 * 
 * Done:
 * implement a 'random' button that feeds back to the GUIModel class, generating a password using random letters and not based upon words - maybe create 2 radio buttons that are for 'random' and 'word-based' and only one can be selected
 * Correct the upperCase and lowerCase combo boxes - need to have at least one selected at all times, or alternatively do not generate a password containing letters if neither is selected
 * Create a objects with min/max password length options
 * Create a popup for any errors (e.g. min/max settings preventing a password from generating)
 * 
 * @author Jasper Connery
 *
 */

public class GUIView extends JFrame{

	private int WIDTH = 450;
	private int HEIGHT = 400;

	private int maxWords = 9;

	private boolean setDebugging;

	private String textWords = "Select what password properties you require and then click 'Generate'\nHover mouse over option for hints";

	private JFrame rootFrame;

	private JPanel mainWindowTopPanel;
	private JPanel mainWindowLeftPanel;
	private JPanel mainWindowCentrePanel;
	private JPanel mainWindowBottomPanel;

	private JPanel centrePanelCentre;
	private JPanel centrePanelLeftSide;
	private JPanel centrePanelRightSide;
	private JPanel centrePanelBottom;

	private JMenu guiMenu;
	private JMenuBar guiMenuBar;
	private JMenu loadList;
	private JMenuItem loadUserWordList;
	private JMenuItem loadDefaultList;

	private Font mainFont;


	private JTextArea programDescriptionText;
	private JTextPane passwordText;

	private JButton generatePasswordButton;

	private JComboBox<Integer> selectNumberOfWords;
	private JComboBox<Integer> minPasswordLength;
	private JComboBox<Integer> maxPasswordLength;

	private final int MIN_PASSWORD_LENGTH = 2;
	private final int MAX_PASSWORD_LENGTH = 50;

	private JCheckBox specialCharactersCheckbox;
	private JCheckBox upperCaseCheckbox;
	private JCheckBox lowerCaseCheckbox;
	private JCheckBox numbersCheckbox;
	private JCheckBox randomPasswordCheckbox;
	private JCheckBox insertComplexityWithinWordCheckbox;

	private JCheckBox loggingCheckbox;

	private JLabel numberOfWords;
	private JLabel minLengthLabel;
	private JLabel maxLengthLabel;

	private Color labelColourEnabled = new Color(1, 1, 1);
	private Color labelColourDisabled = new Color(150, 150, 150);

	private GUIModel model;

	private boolean initialPasswordText;


	public GUIView(){
		model = new GUIModel();
		setDebugging = false;
		mainFont = new Font("Segoe.UI", Font.PLAIN, 12);

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
				initComponents();

			}
		});
	}

	private void initComponents() {
		rootFrame = new JFrame("Generating a password - " + model.getCurrentVersion());
		rootFrame.setSize(new Dimension(WIDTH, HEIGHT));
		rootFrame.setLocationRelativeTo(null);
		rootFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		rootFrame.setLayout(new BorderLayout());
		rootFrame.setResizable(false);

		guiMenuBar = new JMenuBar();

		guiMenu = new JMenu("File");
		guiMenu.setMnemonic(KeyEvent.VK_F);
		guiMenuBar.add(guiMenu);

		loadList = new JMenu("Set Word List");

		loadDefaultList = new JMenuItem("Default");
		loadDefaultList.addActionListener(e ->{
			model.setDefaultWordList(true);
			try {
				model.loadWordList("WordList.txt");
			} catch (Exception e1) {e1.printStackTrace();}
		});

		loadUserWordList = new JMenuItem("Import..");
		loadUserWordList.addActionListener(e ->{

			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				String filePath = selectedFile.getAbsolutePath();
				filePath = filePath.replace("\\", "/");
				if(filePath.endsWith(".txt")){
					try {
						model.loadWordList(filePath);
					} catch (Exception e1) {e1.printStackTrace();}
					System.out.println(filePath);
				}else{
					JOptionPane.showMessageDialog(loadUserWordList, "File must be .txt", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		loadList.add(loadDefaultList);
		loadList.add(loadUserWordList);
		guiMenu.add(loadList);

		/**---------------------number of words for base password-------------------------------------*/
		numberOfWords = new JLabel("Select number of words: ");
		numberOfWords.setFont(mainFont);
		selectNumberOfWords = new JComboBox<Integer>();
		//add the integer options in to select the number of words
		for(int i = 1; i <= maxWords; i++){
			selectNumberOfWords.addItem(i);
			if(i == 2){
				selectNumberOfWords.setSelectedItem(i);
			}
		}
		selectNumberOfWords.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				if(event.getStateChange() == ItemEvent.SELECTED){
					model.setNumberOfInitialWords((int) event.getItem());
				}
			}
		});
		selectNumberOfWords.setForeground(new Color(25, 25, 255));
		selectNumberOfWords.setFont(new Font("Arial", Font.BOLD, 14));
		/**-----------------------------------------------------------------*/

		/**--------------------------Min password length----------------------------------*/
		minLengthLabel = new JLabel("Min password length: ");
		minLengthLabel.setFont(mainFont);
		minPasswordLength = new JComboBox<Integer>();
		for(int i = MIN_PASSWORD_LENGTH; i <= MAX_PASSWORD_LENGTH; i++){
			minPasswordLength.addItem(i);
			if(i == 8){
				minPasswordLength.setSelectedItem(i);
				model.setMinPasswordLength(i);
			}
		}
		minPasswordLength.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				if(event.getStateChange() == ItemEvent.SELECTED){
					int x = (int)event.getItem();

					if(x > model.getMaxPasswordLength()){
						JOptionPane.showMessageDialog(null, "Cannot set "+x+" as min password size, this is higher than max password size (currently "+model.getMaxPasswordLength()+").", "WARNING", JOptionPane.ERROR_MESSAGE);

						x = model.getMaxPasswordLength();
						minPasswordLength.setSelectedItem(x);

					}
					model.setMinPasswordLength(x);
				}

			}
		});
		minPasswordLength.setForeground(new Color(25, 25, 255));
		minPasswordLength.setFont(new Font("Arial", Font.BOLD, 14));

		/**-----------------------------------------------------------------*/

		/**--------------------------Max password length----------------------------------*/
		maxLengthLabel = new JLabel("Max password length: ");
		maxLengthLabel.setFont(mainFont);
		maxPasswordLength = new JComboBox<Integer>();
		for(int i = MIN_PASSWORD_LENGTH; i <= MAX_PASSWORD_LENGTH; i++){
			maxPasswordLength.addItem(i);
			if(i == 16){
				maxPasswordLength.setSelectedItem(i);
				model.setMaxPasswordLength(i);
			}
		}
		maxPasswordLength.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				if(event.getStateChange() == ItemEvent.SELECTED){
					int x = (int)event.getItem();

					if(x < model.getMinPasswordLength()){
						JOptionPane.showMessageDialog(null, "Cannot set "+x+" as max password size, this is lower than min password size (currently "+model.getMinPasswordLength()+").", "WARNING", JOptionPane.ERROR_MESSAGE);

						x = model.getMinPasswordLength();
						maxPasswordLength.setSelectedItem(x);
					}

					model.setMaxPasswordLength(x);
				}
			}
		});
		maxPasswordLength.setForeground(new Color(25, 25, 255));
		maxPasswordLength.setFont(new Font("Arial", Font.BOLD, 14));

		/**-----------------------------------------------------------------*/

		/** this is the code for the generatePasswordButton */
		generatePasswordButton = new JButton("Generate Password");
		generatePasswordButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				//				changeBorder();
				String passwordReturn = model.generatePassword();
				if(!(passwordReturn.equals("-1"))){
					passwordText.setText(passwordReturn);
					passwordText.requestFocus();
					passwordText.selectAll();

					if(initialPasswordText){
						initialPasswordText = false;
						passwordText.setForeground(new Color(1, 1, 1));

					}
				} else{
					//pop up error box
					JOptionPane.showMessageDialog(null, "Unable to generate password between " + model.getMinPasswordLength() + " and "
							+ model.getMaxPasswordLength() + " characters.\n"+
							"Reduce word count or increase max password length.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		/** this is the code for the text describing the program */
		programDescriptionText = new JTextArea(textWords, 2, 2);
		programDescriptionText.setFont(mainFont);
		programDescriptionText.setOpaque(false);
		programDescriptionText.setPreferredSize(new Dimension(WIDTH-20, 30));
		programDescriptionText.setLineWrap(true);
		programDescriptionText.setWrapStyleWord(true);
		programDescriptionText.setEditable(false);


		/** this is the code for the passwordText pane - generated passwords are shown here */
		passwordText = new JTextPane();
		passwordText.setForeground(new Color(150, 100, 100));
		passwordText.setText("Password will generate here");
		initialPasswordText = true;

		/**-------------------------------------------------------------------------------*/
		specialCharactersCheckbox = new JCheckBox("Special Characters", false);
		specialCharactersCheckbox.setToolTipText("include special characters in the password");
		specialCharactersCheckbox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(specialCharactersCheckbox.isSelected()){
					model.setSpecialChars(true);
					if(!initialPasswordText){
						passwordText.setText(model.addSpecialCharsToGeneratedPassword(passwordText.getText()));
					}
				}
				else{
					model.setSpecialChars(false);
					if(!initialPasswordText){
						passwordText.setText(model.removeSpecialCharsToGeneratedPassword(passwordText.getText()));
					}
				}

			}
		});
		/**-------------------------------------------------------------------------------*/

		/**-------------------------------upperCaseCheckbox------------------------------------*/
		upperCaseCheckbox = new JCheckBox("Uppercase", true);
		upperCaseCheckbox.setToolTipText("include upper-case letters in the password");
		upperCaseCheckbox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(upperCaseCheckbox.isSelected()){
					model.setUpperCase(true);

				}
				else{
					model.setUpperCase(false);

					if(!model.isLowerCase()){
						model.setLowerCase(true);
						lowerCaseCheckbox.setSelected(true);
					}
				}

			}
		});
		/**-------------------------------------------------------------------------------*/

		/**---------------------------lowerCaseCheckbox---------------------------------------*/
		lowerCaseCheckbox = new JCheckBox("Lowercase", false);
		lowerCaseCheckbox.setToolTipText("include lower-case letters in the password");
		lowerCaseCheckbox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(lowerCaseCheckbox.isSelected()){
					//do this
					model.setLowerCase(true);

				}
				else{
					//do that
					model.setLowerCase(false);

					if(!model.isUpperCase()){
						model.setUpperCase(true);
						upperCaseCheckbox.setSelected(true);
					}
				}

			}
		});
		/**-------------------------------------------------------------------------------*/

		/**----------------------------numbersCheckbox-------------------------------------*/
		numbersCheckbox = new JCheckBox("Numbers", false);
		numbersCheckbox.setToolTipText("include numbers in the password");
		numbersCheckbox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(numbersCheckbox.isSelected()){
					//do this
					model.setNumbers(true);
					if(!initialPasswordText){
						passwordText.setText(model.addNumbersToGeneratedPassword(passwordText.getText()));
					}
				}
				else{
					//do that
					model.setNumbers(false);
					if(!initialPasswordText){
						passwordText.setText(model.removeNumbersToGeneratedPassword(passwordText.getText()));
					}
				}
			}
		});
		/**-------------------------------------------------------------------------------*/

		/**------------------------------randomPasswordCheckbox-----------------------------------*/
		randomPasswordCheckbox = new JCheckBox("Random String", false);
		randomPasswordCheckbox.setToolTipText("creates a totally random password");
		randomPasswordCheckbox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(randomPasswordCheckbox.isSelected()){
					model.setRandomisedPassword(true);
					changeCheckboxState(false);
					changeComboboxFontColour(false);
				}else{
					model.setRandomisedPassword(false);
					changeCheckboxState(true);
					changeComboboxFontColour(true);
				}

			}
		});
		/**-------------------------------------------------------------------------------*/

		/**--------------------------insertComplexityWithinWordCheckbox-------------------------------*/
		insertComplexityWithinWordCheckbox = new JCheckBox("Complex Passwords", false);
		insertComplexityWithinWordCheckbox.setToolTipText("allows possibility for numbers/special chars to replace letters");
		insertComplexityWithinWordCheckbox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(insertComplexityWithinWordCheckbox.isSelected()){
					model.setAddComplexityWithinWord(true);
				} else{
					model.setAddComplexityWithinWord(false);
				}

			}
		});
		/**-------------------------------------------------------------------------------*/

		/**------------------------debuggingCheckbox------------------------------------*/
		loggingCheckbox = new JCheckBox("Logging", false);
		loggingCheckbox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(loggingCheckbox.isSelected()){
					//do this
					model.setLogging(true);
					JOptionPane.showMessageDialog(null, "**WARNING**\n"+
							"Enabling logging stores password details in plain text!\n"+
							"These logs can be deleted by opening the Password Generator Logs\n"+
							"folder and deleting the .txt file(s)", "WARNING",
							JOptionPane.WARNING_MESSAGE);
				}
				else{
					//do that
					model.setLogging(false);
				}
			}
		});

		/**-------------------------------------------------------------------------------*/


		mainWindowTopPanel = new JPanel();
		mainWindowLeftPanel = new JPanel();
		mainWindowLeftPanel.setLayout(new BoxLayout(mainWindowLeftPanel, BoxLayout.Y_AXIS));
		mainWindowLeftPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		mainWindowCentrePanel = new JPanel();
		mainWindowCentrePanel.setLayout(new BorderLayout());
		mainWindowBottomPanel = new JPanel();
		mainWindowBottomPanel.setLayout(new BoxLayout(mainWindowBottomPanel, BoxLayout.PAGE_AXIS));

		mainWindowTopPanel.add(programDescriptionText);

		//Child of the 'Settings' panel (mainWindowCentrePanel). Contains the two JPanels centrePanelLeftSide and centrePanelRightSide
		centrePanelCentre = new JPanel();

		//Child of child (centrePanelCentre) of 'Settings' JPanel (mainWindowCentrePanel). Contains JLabels
		centrePanelLeftSide = new JPanel();
		centrePanelLeftSide.setLayout(new BoxLayout(centrePanelLeftSide, BoxLayout.PAGE_AXIS));

		//Child of child (centrePanelCentre) of 'Settings' JPanel (mainWindowCentrePanel). Contains JComboBoxes
		centrePanelRightSide = new JPanel();
		centrePanelRightSide.setLayout(new BoxLayout(centrePanelRightSide, BoxLayout.PAGE_AXIS));

		//Child of 'Settings' panel (mainWindowCentrePanel). Contains the generate password button
		centrePanelBottom = new JPanel();
		centrePanelBottom.setLayout(new BorderLayout());
		centrePanelBottom.setBorder(BorderFactory.createBevelBorder(2));

		centrePanelLeftSide.add(numberOfWords);
		centrePanelLeftSide.add(Box.createRigidArea(new Dimension(0, 7)));
		centrePanelLeftSide.add(minLengthLabel);
		centrePanelLeftSide.add(Box.createRigidArea(new Dimension(0, 7)));
		centrePanelLeftSide.add(maxLengthLabel);

		centrePanelRightSide.add(selectNumberOfWords);
		centrePanelRightSide.add(minPasswordLength);
		centrePanelRightSide.add(maxPasswordLength);

		centrePanelBottom.add(generatePasswordButton);


		centrePanelCentre.add(centrePanelLeftSide);
		centrePanelCentre.add(centrePanelRightSide);


		mainWindowCentrePanel.add(centrePanelBottom, BorderLayout.SOUTH);
		mainWindowCentrePanel.add(centrePanelCentre, BorderLayout.CENTER);

		mainWindowCentrePanel.setBorder(BorderFactory.createTitledBorder("Settings"));


		mainWindowLeftPanel.add(specialCharactersCheckbox);
		mainWindowLeftPanel.add(upperCaseCheckbox);
		mainWindowLeftPanel.add(lowerCaseCheckbox);
		mainWindowLeftPanel.add(numbersCheckbox);
		mainWindowLeftPanel.add(insertComplexityWithinWordCheckbox);
		mainWindowLeftPanel.add(randomPasswordCheckbox);
		mainWindowLeftPanel.add(loggingCheckbox);
		mainWindowLeftPanel.setBorder(BorderFactory.createTitledBorder("Conditions"));

		mainWindowBottomPanel.add(passwordText);
		mainWindowBottomPanel.setBorder(BorderFactory.createTitledBorder("Password"));

		rootFrame.setJMenuBar(guiMenuBar);

		rootFrame.add(mainWindowTopPanel, BorderLayout.NORTH);
		rootFrame.add(mainWindowLeftPanel, BorderLayout.WEST);
		rootFrame.add(mainWindowCentrePanel, BorderLayout.CENTER);
		rootFrame.add(mainWindowBottomPanel, BorderLayout.SOUTH);

		rootFrame.pack();

		rootFrame.setVisible(true);
	}

	//this method grays out the dropdown box font colour when the random password checkbox is selected
	private void changeComboboxFontColour(boolean state){
		Color current = new Color(255, 255, 255);
		if(state){
			current = labelColourEnabled;
		}
		else{
			current = labelColourDisabled;
		}

		minLengthLabel.setForeground(current);
		maxLengthLabel.setForeground(current);
		numberOfWords.setForeground(current);

	}


	//changes the checkboxes below to enabled/disabled based upon boolean provided.
	//Used with the random string checkbox
	private void changeCheckboxState(boolean state){

		numbersCheckbox.setEnabled(state);
		upperCaseCheckbox.setEnabled(state);
		lowerCaseCheckbox.setEnabled(state);
		selectNumberOfWords.setEnabled(state);
		insertComplexityWithinWordCheckbox.setEnabled(state);
		minPasswordLength.setEnabled(state);
		maxPasswordLength.setEnabled(state);
		loggingCheckbox.setEnabled(state);

	}

}
