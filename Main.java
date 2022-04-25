package application;

//SQL Imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
// TextAnalyzer Imports
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// JavaFX Imports
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Date: April 9-2022
 * This is an application that will take in the site- 
 * https://www.gutenberg.org/files/1065/1065-h/1065-h.htm
 * -and will output the top 20 word occurrences in the poem on that site.
 * Program will also input top 20 results into SQL table titled word_occurences
 * and print them to the console, as well as retrieve each input and print them to
 * the GUI.
 * @author Joshua
 * @version 1.0
 *
 */
public class Main extends Application implements EventHandler<ActionEvent> {

	Button button;
	TextField textField1;
	Text text;
	Label output;

	/**
	 * This method will run the GUI setup and display the UI to the user.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("The Raven");
		Group root = new Group();
		Scene scene = new Scene(root, 300, 300);
		
		// Button Setup
		button = new Button("Get Top 20 Word Occurances");
		button.setLayoutX(70);
		button.setLayoutY(150);
		
		// Input textField setup
		textField1 = new TextField();
		textField1.setPrefWidth(200);
		textField1.setLayoutX(55);
		textField1.setLayoutY(120);
		
		// Directions text setup
		text = new Text();
		text.setText("Enter website:");
		text.setLayoutX(120);
		text.setLayoutY(110);

		// Button activation will run actionEvent
		button.setOnAction(this);
		
		// Output label after previous method
		output = new Label();
		output.setWrapText(true);
		output.setMaxWidth(200);
		output.setLayoutX(60);
		output.setLayoutY(180);

		Image icon = new Image("icon.png");
		primaryStage.getIcons().add(icon);

		root.getChildren().addAll(button, textField1, text, output);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * This method will handle the entire function of the program, beginning after the button is
	 * pressed in the GUI.
	 */
	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == button) {
			try {
				
				boolean matchFound = false; // Will allow or disallow lines to be read and input into list
				URL url = new URL(textField1.getText()); // Only accepts https://www.gutenberg.org/files/1065/1065-h/1065-h.htm
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream())); // This line can be changed into a file reader
																									 // with a path to the desired HTML file if one so wishes
				ArrayList<String> wordCollection = new ArrayList<String>();

				String line;
				while ((line = reader.readLine()) != null) {

					if ((line.length() > 0) && (line.contains("[ #45484 ]") || matchFound == true)) { // Ignores everything above [ #45484 ], right above the title
						line = line.toLowerCase().replaceAll("\\<[^>]*>", "").replace("[ #45484 ]", "") // Deletes [ #45484 ]
								.replaceAll(",", "").replace("&mdash", "").replace(";", " ")
								.replace("!", "").replace(".", "").replace("?", "")
								.replaceAll("â€?", "").replaceAll("™", "’").replaceAll("œ", "")
								.replace("˜", ""); // There's probably an easier way to do all of this but it works for now
						//System.out.println(line); // Print entire poem
						matchFound = true;

						// Put each word of line into the ArrayList
						if (line.length() > 2) {
							for (String word : line.split(" ")) {
								wordCollection.add(word.replaceAll("[^a-zA-Z0-9-’ ]", "")); // Gets rid of any extra "?" in ArrayList
							}
						}

						if ((line.length() > 0) && (line.contains("be lifted"))) {
							matchFound = false;
						}
					}
				}
				reader.close();
				//System.out.println(wordCollection); // Prints out ArrayList of each word in the order they are added

				// Convert to HashMap using countWords method
				Map<String, Integer> words = new HashMap<String, Integer>();
				countWords(wordCollection, words);
				
				// Organize result into descending order (Most frequently used word)
				List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(words.entrySet());
				Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
				  public int compare( Map.Entry<String, Integer> entry1,
						  			  Map.Entry<String, Integer> entry2) {
				    return entry2.getValue().compareTo(entry1.getValue());
				  }
				});
				
				// Output entire result of organized list in descending order in console
				System.out.println(entries);
				// Output result of top 20 (Change (< 20) to (< entries.size()) for all results instead of top 20)
				for(int i = 0; i < 20; i++) {
				//System.out.println(entries.get(i));
				
				// Insert top 20 results into database table
				try {
					Connection con = getConnection();
					PreparedStatement create = con.prepareStatement("INSERT INTO word(word) VALUES(?)");
					create.setString(1, entries.get(i).toString());
					create.executeUpdate();
				} catch (Exception e) {
					System.out.println(e);
				}

				// Send entries to output GUI using ArrayList
				//output.setText(output.getText() + entries.get(i).toString() + ", ");
				}				
				
				// Print all elements in table to console and input string to GUI
				try {
					Connection con = getConnection();
					java.sql.Statement s = con.createStatement();
			        ResultSet rs = s.executeQuery("SELECT * FROM word");
			        
			        // While loop to print to console and set to GUI the elements found in word table (In alphabetical order)
			        while (rs.next()) {
			        	String results = rs.getString("word");
			        	System.out.println(results);
			        	output.setText(output.getText() + results + ", "); // Set GUI text
			        }
				} catch (Exception e) {
					System.out.println(e);
				}
				
			} catch (MalformedURLException e) {
				System.out.println("Malformed URL: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("I/O Error: " + e.getMessage());
			}
		}
	}
	/**
	 * This method takes in the HashMap and ArrayList, then counts the occurrence of words in the ArrayList,
	 * returning it to the handle method.
	 * 
	 * @param wordCollection: Takes in an ArrayList of Strings called wordCollection from handle method
	 * @param words: Takes in a Map of Strings/Integers called "words" from handle method
	 * @return Will return the the map called Words to the handle method
	 */
	public static Map<String, Integer> countWords (ArrayList<String> wordCollection, Map<String, Integer> words) {
		for(int i = 0; i < wordCollection.size(); i++) {
			String word = wordCollection.get(i);
			Integer count = words.get(word);
			if (count != null) {
				count++;
			} else
				count = 1;
			words.put(word, count);
		}
		return words;
	}

	/** This method will run the launch command to run start and display GUI to user, allowing
	 * the program to continue operation.
	 * 
	 * @param args Runs main method
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		getConnection();
		createTable();
		launch(args);
	}
	
	/**
	 * Creates SQL table with "word" column.
	 * @throws Exception
	 */
	public static void createTable() throws Exception {
		try {
			Connection con = getConnection();
			PreparedStatement create = con.prepareStatement("CREATE TABLE IF NOT EXISTS word(word varchar(255), PRIMARY KEY(word))");
			create.executeUpdate();
			
		} catch (Exception e) {
			System.out.println(e);
		}
		finally{
			System.out.println("Table created.");
		}
	}
	
	/**
	 * Creates SQL Connection.
	 * @return
	 * @throws Exception
	 */
	public static Connection getConnection() throws Exception {
		try {
			String driver = "com.mysql.cj.jdbc.Driver";
			String url = "jdbc:mysql://localhost:3306/word_occurences";
			String username = "root";
			String password = "123456";
			Class.forName(driver);

			Connection conn = DriverManager.getConnection(url, username, password);
			//System.out.println("Connected");
			return conn;
		} catch (Exception e) {
			System.out.println(e);
		}

		return null;
	}
}
