package encryptionApp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Login extends Application{
	
	private Stage stage;
	private TextField user;
	private PasswordField pass;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}
	
	public void start(Stage primaryStage) {
		stage = primaryStage;
		stage.setTitle("Login");
		
		VBox loginBox = new VBox(10);
		loginBox.setPadding(new Insets(40));
		loginBox.setAlignment(Pos.CENTER);
		Label u = new Label("User: (pls type: 123)");
		user = new TextField();
		user.setPromptText("Pls type: 123");
		Label p = new Label("Password: (pls type: 123)");
		pass = new PasswordField();
		pass.setPromptText("Pls type: 123");
		Button loginB = new Button("Login");
		loginB.setOnAction(e->{
			try {
				login();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		loginBox.getChildren().addAll(u, user, p, pass, loginB);
		Scene loginScene = new Scene(loginBox, 300, 200);
		stage.setScene(loginScene);
		stage.show();
	}
	
	private void login() throws Exception {
		String username = user.getText();
		String password = pass.getText();
		DbManager dbManager = new DbManager(); 
		boolean isValid = dbManager.validateUser(username, password);
		
		if(isValid) {
			System.out.println("Passwords Matched.");
			EncryptionApp encryptionApp = new EncryptionApp();
			encryptionApp.start(stage);
		}else {
			showAlert("Alert", "Wrong username or password.");
		}
	}
	
	private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
