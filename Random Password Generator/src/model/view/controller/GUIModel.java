package model.view.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * To do:
 * 
 * Print debugging to log - also include warning as this will store password information
 * tidy up the code and insert comments for future users
 * 
 * 
 * DONE - move all the complexity additions to their own methods to streamline the process and prevent confusion, incorrect method calls etc
 * DONE - implement a 'random' option that does not use words to generate a password, but instead generates random letters (and numbers/characters if so selected),
 * DONE - add the potential for external/custom word lists to be used instead of the internally provided one
 * @author conneryj
 *
 */

public class GUIModel {

	private Pattern uppercasePattern = Pattern.compile("\\p{Upper}");
	private Pattern lowercasePattern = Pattern.compile("\\p{Lower}");
	private Pattern numberPattern = Pattern.compile("\\p{Digit}");
	private Pattern specialCharacterPattern = Pattern.compile("\\p{Punct}");

	private String currentVersion = "v0.14";

	private List<String> loggingList;
	private String decodedPath = "";
	private String currentDate;
	private File folder;
	private File logFile;
	private String logFileName;

	private Calendar cal;

	private boolean toRemove = false;

	private boolean defaultWordList;

	private boolean loggingEnabled = false;
	//	private int countRetries = 0;

	private int averageWordLength;
	private int totalNumberOfBaseWords = 0;

	private int numberOfInitialWords = 2; 	//2 is the default

	private int minPasswordLength = 8;		//8 is default
	private int maxPasswordLength = 16;		//16 is default

	final private String wordSplitter = "|";

	private boolean upperCase = true;
	private boolean lowerCase = false;
	private boolean specialChars = false;
	private boolean numbers = false;

	private boolean random = false;


	//this argument is used to determine whether or not to replace letters with numbers/special characters
	private boolean addComplexityWithinWord;

	private boolean randomisedLetters = false;

	private String[] wordsForPassword;
	private String[] specialCharacterList;
	private String[] numbersList;

	private String[] alphabetList;

	Random randomGenerator;


	private List<String> wordList;

	public GUIModel(){

		/**logging setup*/
		initialiseLogging();
		/**logging setup*/


		addComplexityWithinWord = false;

		defaultWordList = true;

		wordList = new ArrayList<String>();
		specialCharacterList = new String[]{"!", "@", "#", "$", "%", "&", "*", "^", "?", "_"};

		numbersList = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

		alphabetList = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
				"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

		try {
			loadWordList("WordList.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static boolean matches(List<Pattern> p, String s){

		for(int i = 0; i < p.size(); i++){
			if(!p.get(i).matcher(s).find()){
				return false;
			}
		}
		return true;
	}

	public String generatePassword(){
		double startTime = System.nanoTime();

		/*****************************************************************************/
		if(loggingEnabled){
			addLogEntry("*****************************************START GENERATION********************************************************");
			addLogEntry("Password min and max constraints:");
			addLogEntry("Min-> "+minPasswordLength+", Max-> "+maxPasswordLength);
			addLogEntry("Number of words: " + numberOfInitialWords);
			addLogEntry("Conditions selected: ");
			addLogEntry(")) Special Characters: " + specialChars);
			addLogEntry(")) Uppercase: " + upperCase);
			addLogEntry(")) Lowercase: " + lowerCase);
			addLogEntry(")) Numbers: " + numbers);
			addLogEntry(")) Complex Password: " + addComplexityWithinWord);
			addLogEntry(")) Random String: " + random);
			addLogEntry("generatePassword -> beginning the password generation");
		}
		/*****************************************************************************/


		List<Pattern> patternList = new ArrayList<Pattern>();
		patternList.add(uppercasePattern);
		patternList.add(lowercasePattern);
		patternList.add(numberPattern);
		if(specialChars){
			patternList.add(specialCharacterPattern);
		}

		String randomLetterPassword = "";
		if(random){

			int randomCount = 0;
			while(!matches(patternList, randomLetterPassword)){
				randomCount++;
				randomLetterPassword = createRandomLetterPassword();
			}

			double endTime = System.nanoTime();
			/*****************************************************************************/
			if(loggingEnabled){
				addLogEntry("It has taken "+randomCount+" attempts to generate a random string password");
				addLogEntry("generatePassword.randomLetterPassword.length() -> "+ randomLetterPassword.length());
				addLogEntry("Final password -> "+randomLetterPassword);
				addLogEntry("Time taken to generate the password -> " + ((endTime - startTime)/1000000000) +"s");
				addLogEntry("*****************************************END GENERATION**********************************************************");
			}
			/*****************************************************************************/


			return randomLetterPassword;
		}

		boolean minLengthReached = false;
		String finalPassword = "";
		int passwordAttemptCount = 0;

		while(!minLengthReached){
			loggingList.clear();
			if(!randomisedLetters){
				String basePassword = determineBaseWords(numberOfInitialWords);

				finalPassword = addComplexity(basePassword);
			}

			if(finalPassword.length() >= minPasswordLength && finalPassword.length() <= maxPasswordLength){
				minLengthReached = true;
			}

			passwordAttemptCount++;
			if(passwordAttemptCount>=1000){

				/*****************************************************************************/
				if(loggingEnabled){
					addLogEntry("******************************************");
					addLogEntry("Too many attempts - password generation not viable with current min/max length settings");
					addLogEntry("Attempts made to create password: "+passwordAttemptCount);
					addLogEntry("Min-> "+minPasswordLength+", Max-> "+maxPasswordLength);
					addLogEntry("******************************************");
				}
				/*****************************************************************************/
				return "-1";
			}
		}



		double endTime = System.nanoTime();

		//put log entries into log file
		if(loggingEnabled){
			if(passwordAttemptCount < 1000){
				for(int i = 0; i < loggingList.size(); i++){
					addLogEntry(loggingList.get(i));
				}

			}
		}
		loggingList.clear();

		/*****************************************************************************/
		if(loggingEnabled){
			addLogEntry("generatePassword -> total attempts: "+passwordAttemptCount);
			addLogEntry("generatePassword.finalPassword.length() -> "+ finalPassword.length());
			addLogEntry("Final password -> "+finalPassword);
			addLogEntry("Time taken to generate the password -> " + ((endTime - startTime)/1000000000) +"s");
			addLogEntry("*****************************************END GENERATION**********************************************************");
		}
		/*****************************************************************************/

		return finalPassword;
	}

	//letters can be set to 60%, numbers 30%, and special characters 10% chance
	private String createRandomLetterPassword(){

		int randomVariable = generateRandomInt(6);

		if(generateRandomInt(2) >= 1){
			randomVariable = randomVariable * -1;
		}

		/**  need to determine the weighting for the various letters/numbers/special chars - uppercase and lowercase will be 50/50  */
		float lettersWeight = 0.6f;
		float numbersWeight;
		if(specialChars){
			numbersWeight = 0.3f;
		}else{
			numbersWeight = 0.4f;
		}
		float specialCharsWeight = 0.1f;

		//22 characters, give or take 5?
		randomVariable = 22 + randomVariable;


		String letter;
		String finalPassword;
		float selectCharType;


		finalPassword = "";

		for(int i = 0; i < randomVariable; i++){

			selectCharType = generateRandomFloat(1f);

			//letter
			if(selectCharType < lettersWeight){

				letter = alphabetList[generateRandomInt(alphabetList.length)];
				//chance of uppercase letter
				if(generateRandomInt(7) <= 2){
					letter = letter.toUpperCase();
				}
				else{
					letter = letter.toLowerCase();
				}
			}
			//number
			else if(selectCharType > lettersWeight && selectCharType < (lettersWeight+numbersWeight)){

				letter = Integer.toString(generateRandomInt(10));

				//special character
			}else {

				letter = specialCharacterList[generateRandomInt(specialCharacterList.length)];
			}

			finalPassword = finalPassword + letter;

		}

		/*****************************************************************************/
		if(loggingEnabled){
			loggingList.add("Random String length: "+finalPassword.length());
		}
		/*****************************************************************************/

		return finalPassword;
	}

	//randomly selects two words from the word list provided. If the total combined characters are over a certain limit then new words will be chosen
	private String determineBaseWords(int numberOfWords){
		String basePassword = "";

		//doubled the size of the array and removed 1 to allow the words to initially have a 'splitter'.
		wordsForPassword = new String[numberOfWords * 2 - 1];

		int loggingWordNumber = 0;

		if(numberOfWords > 0){
			for(int i = 0; i < (numberOfWords * 2 - 1); i++){
				wordsForPassword[i] = wordList.get(generateRandomInt(wordList.size()));
				loggingWordNumber = (i/2) + 1;

				//remove any punctuation in the basePassword - mainly used for imported word lists
				//				wordsForPassword[i] = wordsForPassword[i].replaceAll("[^a-zA-Z ]", ""); -> this has moved to the loadWordList method

				basePassword = basePassword + wordsForPassword[i];

				/*****************************************************************************/
				if(loggingEnabled){
					loggingList.add("Word number "+(loggingWordNumber)+": " + wordsForPassword[i]);
				}
				/*****************************************************************************/

				//this adds | in to separate words for easy capitilisation later
				if(i < numberOfWords*2-2){
					i++;
					wordsForPassword[i] = wordSplitter;
					basePassword = basePassword + wordsForPassword[i];
					loggingWordNumber--;
				}

			}
		}

		/*****************************************************************************/
		if(loggingEnabled){
			loggingList.add("Base word: "+basePassword+" (Length: "+(basePassword.length()-(numberOfWords-1)+")"));
		}
		/*****************************************************************************/




		return basePassword;

	}

	private String addComplexity(String basePassword){
		String complexPassword;

		//initialise the words-to-characters array
		List<String> passwordCharacters = new ArrayList<String>();

		//copy the base password into the array as characters
		passwordCharacters = convertWordsToCharacters(basePassword);

		//add numbers
		if(numbers){
			passwordCharacters = complexityAddNumber(passwordCharacters);
			/*****************************************************************************/
			if(loggingEnabled){
				loggingList.add("--Adding numbers to password--");
			}
			/*****************************************************************************/
		}

		//add uppercase at the start of the word and ensure there are lowercase too if required
		if(upperCase){
			passwordCharacters = addUpperCase(passwordCharacters);
			/*****************************************************************************/
			if(loggingEnabled){
				loggingList.add("--Adding upper case letters to password--");
			}
			/*****************************************************************************/
		}

		//add special chars
		if(specialChars){
			complexityAddSpecialCharacters(passwordCharacters);
			/*****************************************************************************/
			if(loggingEnabled){
				loggingList.add("--Adding special characters to password--");
			}
			/*****************************************************************************/
		}

		complexPassword = convertCharactersToWords(passwordCharacters);

		if(!lowerCase){
			passwordCharacters = addLowerCase(complexPassword);

		} else if(loggingEnabled){
			/*****************************************************************************/

			loggingList.add("--Adding lower case letters to password--");

			/*****************************************************************************/
		}

		complexPassword = convertCharactersToWords(passwordCharacters);

		return complexPassword;

		//end: addComplexity()
	}

	private List<String> addUpperCase(List<String> passwordCharacters){
		for(int i = 0; i < passwordCharacters.size(); i++){

			if(i == 0 ){
				passwordCharacters.set(i, passwordCharacters.get(i).toUpperCase());
			}
			else if(passwordCharacters.get(i).equals(wordSplitter)){
				passwordCharacters.set(i+1, passwordCharacters.get(i+1).toUpperCase());
			}
		}

		return passwordCharacters;
	}

	private List<String> addLowerCase(String complexPassword){
		List<String> passwordCharacters = convertWordsToCharacters(complexPassword);
		for(int x = 0; x < passwordCharacters.size(); x++){

			if(passwordCharacters.get(x).matches("\\S")){
				passwordCharacters.set(x, passwordCharacters.get(x).toUpperCase());
			}
		}
		return passwordCharacters;
	}

	private List<String> complexityAddNumber(List<String> passwordCharacters){

		Random rand = new Random();

		//determines how many numbers will be added (can be more if the allowSkipLetter is set to false)
		/**
		 * To do - clamp number limit based on number of words to prevent too many numbers appearing
		 */
		int numberAmount = rand.nextInt(numberOfInitialWords)+1;

		int skipLetter = 0;
		boolean allowSkipLetter = true;

		/*****************************************************************************/
		if(loggingEnabled){
			loggingList.add("numberAmount -> "+numberAmount);
		}
		/*****************************************************************************/

		int count = 0;

		//iterate through all the characters, randomly giving the chance to convert a relevant letter to a number
		if(addComplexityWithinWord){
			for(int j = 0; j < passwordCharacters.size(); j++){

				//instead of always choosing the first relevant number this randomly decides whether to look at the current letter or skip to the next
				if(allowSkipLetter){
					skipLetter = skipLetter + rand.nextInt(2);
				}

				//when a pair of o's or e's are in a word, it looks ugly having only one converted to a number. this boolean is used to ensure that if the initial paired letter is converted, the following will be too
				allowSkipLetter = true;

				if(count == numberAmount){
					break;
				}

				//if the randomly chosen int skipLetter does not equal 0 when divided by 2, check the letter to see if it matches one of the following cases and if so convert the letter to a number
				if((skipLetter % 2) !=  0){

					switch(passwordCharacters.get(j)){
					case "i":
						passwordCharacters.set(j, "1");
						count++;
						break;
					case "e":
						//if the character is an 'e' and part of a pair this checks the previous 'e'. if it was not converted to a 3 neither is this one.
						if(j > 0){
							if(passwordCharacters.get(j-1).equals("e")){
								break;
							}
						}

						//if there is room for only one more number and the letter following is an 'e' also, do not convert this to a 3 as it will not pair up nicely
						if(j < passwordCharacters.size()-1){
							if(passwordCharacters.get(j+1).equals("e") && count == numberAmount-1){
								break;
							}
						}

						//if all the other rules match, change the 'e' to a 3 and iterate the count
						passwordCharacters.set(j, "3");

						//if the previous letter was converted to a 3 and the next letter is an 'e', ensure that is converted too
						if(j < passwordCharacters.size()-1){
							if(passwordCharacters.get(j+1).equals("e") && count < numberAmount-1){
								allowSkipLetter = false;
								skipLetter = 3;
							}
						}
						count++;
						break;
					case "o":
						//if the character is an 'o' and part of a pair this checks the previous 'o'. if it was not converted to a 0 neither is this one.
						if(j > 0){
							if(passwordCharacters.get(j-1).equals("o")){
								break;
							}
						}
						//if there is room for only one more number and the letter following is an 'o' also, do not convert this to a 0 as it will not pair up nicely
						if(j < passwordCharacters.size()-1){
							if(passwordCharacters.get(j+1).equals("o") && count == numberAmount-1){
								break;
							}
						}
						//if all the other rules match, change the 'o' to a 0 and iterate the count
						passwordCharacters.set(j, "0");

						//if the previous letter was converted to a 0 and the next letter is an 'o', ensure that is converted too.
						if(j < passwordCharacters.size()-1){
							if(passwordCharacters.get(j+1).equals("o") && count < numberAmount-1){
								allowSkipLetter = false;
								skipLetter = 3;
							}
						}
						count++;
						break;
					default:
						break;
					}
				}

			}
		}
		while(count < numberAmount){
			passwordCharacters.add(""+rand.nextInt(10));
			count++;
		}

		return passwordCharacters;
		//end: complexityAddNumbers
	}

	private List<String> complexityAddSpecialCharacters(List<String> passwordCharacters){
		Random rand = new Random();

		/**
		 * To do - clamp special character limit based on number of words to prevent too many special characters appearing
		 */

		int numberAmount = rand.nextInt(numberOfInitialWords)+1;
		int count = 0;
		int skipLetter = 0;
		boolean allowSkipLetter = true;
		@SuppressWarnings("unused")
		boolean singleAtSymbolOnly = false;

		//iterate through all the characters, randomly giving the chance to convert a relevant letter to a number provided addComplexityWithinWord is true
		if(addComplexityWithinWord){
			for(int j = 0; j < passwordCharacters.size(); j++){

				//instead of always choosing the first relevant number this randomly decides whether to look at the current letter or skip to the next
				if(allowSkipLetter){
					skipLetter = skipLetter + rand.nextInt(2);
				}

				//when a pair of o's or e's are in a word, it looks ugly having only one converted to a number. this boolean is used to ensure that if the initial paired letter is converted, the following will be too
				allowSkipLetter = true;

				if(count == numberAmount){
					break;
				}

				//if the randomly chosen int skipLetter does not equal 0 when divided by 2, check the letter to see if it matches one of the following cases and if so convert the letter to a number
				if((skipLetter % 2) !=  0){

					switch(passwordCharacters.get(j)){
					case "a":
						if(singleAtSymbolOnly = false){
							passwordCharacters.set(j, "@");
							count++;
							singleAtSymbolOnly = true;
							break;
						}
						else{
							break;
						}
					case "s":
						//if the character is an 's' and part of a pair this checks the previous 's'. if it was not converted to a $ neither is this one.
						if(j > 0){
							if(passwordCharacters.get(j-1).equals("s")){
								break;
							}
						}
						//if there is room for only one more number and the letter following is an 's' also, do not convert this to a $ as it will not pair up nicely
						if(j < passwordCharacters.size()-1){
							if(passwordCharacters.get(j+1).equals("s") && count == numberAmount-1){
								break;
							}
						}

						//if all the other rules match, change the 's' to a $ and iterate the count
						passwordCharacters.set(j, "$");

						//if the previous letter was converted to a $ and the next letter is an 's', ensure that is converted too
						if(j < passwordCharacters.size()-1){
							if(passwordCharacters.get(j+1).equals("s") && count < numberAmount-1){
								allowSkipLetter = false;
								skipLetter = 3;
							}
						}
						count++;
						break;

					case "l":
						if(!toRemove){
							//if the character is an 'l' and part of a pair this checks the previous 'l'. if it was not converted to a ! neither is this one.
							if(j > 0){
								if(passwordCharacters.get(j-1).equals("l")){
									break;
								}
							}
							//if there is room for only one more number and the letter following is an 'l' also, do not convert this to a ! as it will not pair up nicely
							if(j < passwordCharacters.size()-1){
								if(passwordCharacters.get(j+1).equals("l") && count == numberAmount-1){
									break;
								}
							}
							//if all the other rules match, change the 'l' to a ! and iterate the count
							passwordCharacters.set(j, "!");

							//if the previous letter was converted to a ! and the next letter is an 'l', ensure that is converted too.
							if(j < passwordCharacters.size()-1){
								if(passwordCharacters.get(j+1).equals("o") && count < numberAmount-1){
									allowSkipLetter = false;
									skipLetter = 3;
								}
							}
							count++;
						}
						break;

					default:
						break;
					}
				}

			}
		}

		while(count < numberAmount){
			passwordCharacters.add(""+specialCharacterList[rand.nextInt(specialCharacterList.length)]);
			count++;
		}

		return passwordCharacters;

		//end: complexityAddSpecialCharacters
	}

	/** convert the base password to separate characters in an array for easy replacement */
	private List<String> convertWordsToCharacters(String password){

		List<String> passwordChars = new ArrayList<String>();
		for(int i = 0; i < password.length(); i++){
			passwordChars.add(Character.toString(password.charAt(i)));



		}

		return passwordChars;
	}

	/** convert the updated character array back into the final password */
	private String convertCharactersToWords(List<String> passwordCharacters){

		String password = "";

		for(int i = 0; i < passwordCharacters.size(); i++){
			if(passwordCharacters.get(i).equals(wordSplitter)){
				passwordCharacters.set(i, (passwordCharacters.get(i).replace(wordSplitter, "")));
			}
			password = password + passwordCharacters.get(i);
		}

		return password;
	}


	/** method to load a list of base words for the password generator. At this stage the word list is loaded alongside the program */
	public void loadWordList(String path) throws IOException{

		/*****************************************************************************/
		if(loggingEnabled){
			addLogEntry("Loading list of words...");
		}
		/*****************************************************************************/

		BufferedReader textReader;
		wordList.clear();

		String[] splitWords;

		if(defaultWordList){
			textReader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path)));
			defaultWordList = false;
		} else{
			textReader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));

		}


		for(String line = textReader.readLine(); line != null; line = textReader.readLine()){
			splitWords = line.split(" ");

			for(int i = 0; i < splitWords.length; i++){
				//remove any possible white spaces in the word
				splitWords[i] = splitWords[i].replaceAll(" ", "");
				splitWords[i] = splitWords[i].replaceAll("\t", "");
				//remove any punctuation in the word - mainly used for imported word lists
				splitWords[i] = splitWords[i].replaceAll("[^a-zA-Z ]", "");

				//add the words to the list of possible choices
				if(!(splitWords[i].equals(null) || splitWords[i].equals("") || splitWords[i].length()==0)){
					wordList.add(splitWords[i].toLowerCase());
				}

				//used for determining base password length
				averageWordLength = averageWordLength + splitWords[i].length();
				totalNumberOfBaseWords++;
			}
		}



		averageWordLength = (averageWordLength/totalNumberOfBaseWords) + 1;		//added 1 to offset decimals being truncated

		/*****************************************************************************/
		if(loggingEnabled){
			addLogEntry("loadWordList -> averageWordLength initial value: "+averageWordLength+", totalNumberOfBaseWords: "+totalNumberOfBaseWords+", final averageWordLength: "+(averageWordLength/totalNumberOfBaseWords + 1));
		}
		/*****************************************************************************/

		textReader.close();
	}

	public String addSpecialCharsToGeneratedPassword(String passwordGenerated){
		List<String> passwordCharList = new ArrayList<>(Arrays.asList(passwordGenerated.split("")));
		passwordGenerated = convertCharactersToWords(complexityAddSpecialCharacters(passwordCharList));

		return passwordGenerated;
	}

	public String removeSpecialCharsToGeneratedPassword(String passwordGenerated){
		String[] passwordCharList = passwordGenerated.split("");
		String temp;
		passwordGenerated = "";
		for(int i = 0; i < passwordCharList.length; i++){
			for(String specialChar : specialCharacterList){
				if(passwordCharList[i].equals(specialChar)){
					passwordCharList[i] = "";
				}
			}
			passwordGenerated = passwordGenerated + passwordCharList[i];
		}

		return passwordGenerated;
	}

	public String addNumbersToGeneratedPassword(String passwordGenerated){
		List<String> passwordCharList = new ArrayList<>(Arrays.asList(passwordGenerated.split("")));
		passwordGenerated = convertCharactersToWords(complexityAddNumber(passwordCharList));

		return passwordGenerated;
	}

	public String removeNumbersToGeneratedPassword(String passwordGenerated){
		String[] passwordCharList = passwordGenerated.split("");
		passwordGenerated = "";
		for(int i = 0; i < passwordCharList.length; i++){
			for(String number : numbersList){
				if(passwordCharList[i].equals(number)){
					passwordCharList[i] = "";
				}
			}
			passwordGenerated = passwordGenerated + passwordCharList[i];
		}

		return passwordGenerated;
	}

	private void initialiseLogging(){
		//this field will hold the logging info
		loggingList = new ArrayList<>();

		//get current date and time for Logging purposes - will be used in log filename
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy"); // HH_mm_ss
		Date date = new Date();
		currentDate = dateFormat.format(date);
		logFileName =  currentDate + " PasswordGenerator_JC log "+currentVersion+".txt";

		//get the path of jar file
		String path = GUIModel.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		//convert jar filepath to non-URL version
		try {
			decodedPath = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		//create the folder
		folder = new File(decodedPath, "Password Generator Logs");

		//if unable to create the folder throw exception
		if(!folder.exists() && !folder.mkdirs()){
			throw new RuntimeException("Failed to create log directory!");
		}

		logFile = new File(folder, logFileName);

		//		if(loggingEnabled){
		addLogEntry("Logging initialised...");
		//		}


	}

	private void addLogEntry(String entry){
		BufferedWriter logWriter = null;
		if(loggingEnabled){
			try {
				logWriter = new BufferedWriter(new FileWriter(logFile, true));
				logWriter.write(getCurrentTime()+": "+entry);
				logWriter.newLine();
			} catch (IOException e) {e.printStackTrace();}
			finally {try { logWriter.close(); } catch (IOException e) { e.printStackTrace(); }}
		}
	}

	private String getCurrentTime(){
		cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return sdf.format(cal.getTime());
	}

	/** used by the GUIView class to update whether upper case letters are required or not */
	public void setUpperCase(boolean upperCase) {
		this.upperCase = upperCase;
	}

	/** used by the GUIView class to update whether lower case letters are required or not */
	public void setLowerCase(boolean lowerCase) {
		this.lowerCase = lowerCase;
	}

	/** used by the GUIView class to update whether special characters are required or not */
	public void setSpecialChars(boolean specialChars) {
		this.specialChars = specialChars;
	}

	/** used by the GUIView class to update whether numbers are required or not */
	public void setNumbers(boolean numbers) {
		this.numbers = numbers;
	}

	public void setRandomisedPassword(boolean random){
		this.random = random;
	}

	/** used by the GUIView class to update whether numbers and special characters are inserted into a word, or at the end*/
	public void setAddComplexityWithinWord(boolean addComplexityWithinWord){
		this.addComplexityWithinWord = addComplexityWithinWord;
	}

	/** determines whether the console will print out the debugging or not. */
	public void setLogging(boolean debug) {
		this.loggingEnabled = debug;
	}

	/** get state of logging. */
	public boolean getLogging(){
		return loggingEnabled;
	}

	/** used by GUIView class to set the number of words to generate for the password */
	public void setNumberOfInitialWords(int numberOfInitialWords) {
		this.numberOfInitialWords = numberOfInitialWords;
	}

	/** used by GUIView class to set the minimum required length for the password */
	public void setMinPasswordLength(int minPasswordLength) {
		this.minPasswordLength = minPasswordLength;
	}

	/** used by the GUIView class to set the maximum required length for the password */
	public void setMaxPasswordLength(int maxPasswordLength) {
		this.maxPasswordLength = maxPasswordLength;
	}

	/** this is used in the GUIView class to ensure that at least one of the uppercase/lowercase options are selected */
	public boolean isUpperCase() {
		return upperCase;
	}

	/** this is used in the GUIView class to ensure that at least one of the uppercase/lowercase options are selected */
	public boolean isLowerCase() {
		return lowerCase;
	}

	public int getMinPasswordLength() {
		return minPasswordLength;
	}

	public int getMaxPasswordLength() {
		return maxPasswordLength;
	}

	public String getCurrentVersion() {
		return currentVersion;
	}

	public void deletme(String currentVersion) {
		this.currentVersion = currentVersion;
	}

	public void setDefaultWordList(boolean state){
		defaultWordList = state;
	}

	private int generateRandomInt(int x){

		Random rand = new Random();

		return rand.nextInt(x);
	}

	private float generateRandomFloat(float x){

		Random rand = new Random();
		float result = rand.nextFloat() * x;

		return result;
	}



}
