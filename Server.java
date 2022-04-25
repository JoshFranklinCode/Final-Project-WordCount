package application;

import java.io.*;
import java.net.*;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class Server extends Application {
	@Override // Override the start method in the Application class
	public void start(Stage primaryStage) {
		// Text area for displaying contents
		TextArea ta = new TextArea();

		// Create a scene and place it in the stage
		Scene scene = new Scene(new ScrollPane(ta), 450, 200);
		primaryStage.setTitle("Server"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage

		new Thread(() -> {
			try {
				// Create a server socket
				ServerSocket serverSocket = new ServerSocket(8000);
				Platform.runLater(() -> ta.appendText("Server started at " + new Date() + '\n'));

				// Listen for a connection request
				Socket socket = serverSocket.accept();

				// Create data input and output streams
				DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
				DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());

				while (true) {
					// Receive number from the client
					int number = inputFromClient.readInt();

					// Run method to check if input is prime or not
					boolean isItPrime = checkForPrime(number);
					

					if (isItPrime == false) {
						// Send negative result back to the client
						int notPrime = 0;
						outputToClient.writeInt(notPrime);

						Platform.runLater(() -> {
							ta.appendText("Number received from client to check prime number is: " + number + '\n');

						});
					} else if (isItPrime == true) {
						// Send positive result back to the client
						int isPrime = 1;
						outputToClient.writeInt(isPrime);

						Platform.runLater(() -> {
							ta.appendText("Number received from client to check prime number is: " + number + '\n');
						});
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}).start();
	}
	/**
	 * Method used to determine if user input is prime or not. Will return a
	 * boolean of true or false to Thread.
	 * @param userInput
	 * @return
	 */
	static boolean checkForPrime(int userInput) {
		boolean isItPrime = true;
		if (userInput <= 1) {
			isItPrime = false;
			return isItPrime;
		} else {
			for (int i = 2; i <= userInput / 2; i++) {
				if ((userInput % i) == 0) {
					isItPrime = false;

					break;
				}
			}
		}

		return isItPrime;
	}

	/**
	 * The main method is only needed for the IDE with limited JavaFX support. Not
	 * needed for running from the command line.
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
