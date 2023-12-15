package encryptionApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EncryptionApp extends Application {
	
	private TextArea inputArea;
	private TextArea outputArea;
	private CheckBox caesarCheck;
	private CheckBox desCheck;
	private CheckBox aesCheck;
	private String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private SecretKey desKey;
	private SecretKey aesKey;
	private boolean isDark = false;
	public DbManager dbManager;
	private byte[] keyBytes;
	private TextField caesarKeyField;

//	public static void main(String[] args) {
//		launch(args);
//	}

	@Override
	public void start(Stage arg0) throws Exception {
		// TODO Auto-generated method stub
		arg0.setTitle("EnD App");
		inputArea = new TextArea();
		outputArea = new TextArea();
		outputArea.setEditable(false);
		
		dbManager = new DbManager();
		try {
			dbManager.connect();
			isDark = dbManager.load();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		HBox buttonsBox = new HBox(10);
		buttonsBox.setPadding(new Insets(10, 10, 5, 10));
		buttonsBox.setAlignment(Pos.CENTER);
		Button eB = new Button("Encrypt");
		eB.setOnAction(e->encript());
		Button dB = new Button("Decrypt");
		dB.setOnAction(e->decript());
		Button mode = new Button("mode");
		Button saveColor = new Button("Save Mode");
		saveColor.setOnAction(e->{
			try {
				dbManager.saveColor(isDark);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		buttonsBox.getChildren().addAll(eB, dB, mode, saveColor);
		
		
		VBox textAreaBox = new VBox(10);
		textAreaBox.setPadding(new Insets(10));
		textAreaBox.getChildren().addAll(inputArea, outputArea);
		
		caesarCheck = new CheckBox("Caesar");
		caesarCheck.setSelected(true);
		caesarKeyField = new TextField();
		caesarKeyField.setPromptText("Enter Caesar Key");
		caesarKeyField.setPrefWidth(120);
		desCheck = new CheckBox("DES");
		aesCheck = new CheckBox("AES");
		caesarCheck.setOnAction(e -> handleCheckbox(caesarCheck));
        desCheck.setOnAction(e -> handleCheckbox(desCheck));
        aesCheck.setOnAction(e -> handleCheckbox(aesCheck));
		HBox checkBoxBox = new HBox(10);
		checkBoxBox.setPadding(new Insets(0, 10, 0, 10));
		checkBoxBox.setAlignment(Pos.CENTER);
		checkBoxBox.getChildren().addAll(caesarCheck, caesarKeyField, desCheck, aesCheck);
		
		HBox saveLoadKeyBox = new HBox(10);
		saveLoadKeyBox.setPadding(new Insets(-10, 10, 10, 10));
		saveLoadKeyBox.setAlignment(Pos.CENTER);
		Button sB = new Button("Save DES Key in file");
		Button lB = new Button("Load DES Key from file");
		sB.setOnAction(e->{
			try {
				saveKey(desKey);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		lB.setOnAction(e->loadKey());
		saveLoadKeyBox.getChildren().addAll(sB, lB);
		
		HBox dbsaveLoadKeyBox = new HBox(10);
		dbsaveLoadKeyBox.setPadding(new Insets(-15, 10, 10, 10));
		dbsaveLoadKeyBox.setAlignment(Pos.CENTER);
		Button dbsB = new Button("Save DES Key in DB");
		Button dblB = new Button("Load DES Key from DB");
		dbsB.setOnAction(e -> {
			if (desKey == null) {
		        showAlert("Empty Key", "Cannot save an empty key in the database.");
		        return;
		    }
			keyBytes = desKey.getEncoded();
		    try {
				dbManager.saveKey(keyBytes);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		dblB.setOnAction(e->{
			try {
				loadKeyFromDB();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		dbsaveLoadKeyBox.getChildren().addAll(dbsB,dblB);
		
		Button saveMessage = new Button("Save Encrypted Message");
		Button loadMessage = new Button("Load Encrypted Message");
		saveMessage.setOnAction(e -> saveEncryptedMessageToDB());
		loadMessage.setOnAction(e -> loadEncryptedMessageFromDB());
		HBox messageToDB = new HBox(10);
		messageToDB.setPadding(new Insets(-15, 10, 10, 10));
		messageToDB.setAlignment(Pos.CENTER);
		messageToDB.getChildren().addAll(saveMessage, loadMessage);
		
		VBox mainLayout = new VBox(10);
		setColor(isDark, mainLayout);
		mainLayout.getChildren().addAll(textAreaBox, checkBoxBox, buttonsBox, saveLoadKeyBox, dbsaveLoadKeyBox, messageToDB);
		
		mode.setOnAction(e->modeSwitch(mainLayout));
		
		Scene scene = new Scene(mainLayout, 350, 500);
		arg0.setScene(scene);
		arg0.show();
	}

	private void saveEncryptedMessageToDB() {
		// TODO Auto-generated method stub
		String encryptedMessage = outputArea.getText();
		if (encryptedMessage.isEmpty()) {
	        showAlert("Empty Message", "Cannot save an empty message.");
	        return;
	    }
	    dbManager.saveEncryptedMessage(encryptedMessage);
	}

	private void loadEncryptedMessageFromDB() {
	// TODO Auto-generated method stub
		String encryptedMessage = dbManager.loadEncryptedMessage();
	    if (encryptedMessage != null && !encryptedMessage.isEmpty()) {
	        inputArea.setText(encryptedMessage);
	    } else {
	        System.out.println("Cannot load message.");
	    }
}

	private void loadKeyFromDB() throws IOException {
		// TODO Auto-generated method stub
		byte[] loadedKey = dbManager.loadKey();
	    if (loadedKey != null) {
	        desKey = new SecretKeySpec(loadedKey, "DES");
	    } else {
	    	System.out.println("Key cannot be null.");
	    }
	}

	private void loadKey() {
		// TODO Auto-generated method stub
		try {
			File key = new File("src/encryptionApp/key.txt");
			FileInputStream fis = new FileInputStream(key);
			byte[] encodedKey = new byte[(int) key.length()];
			fis.read(encodedKey);
			fis.close();
			desKey = new SecretKeySpec(encodedKey, "DES");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveKey(SecretKey key) throws IOException {
		if (key == null) {
	        showAlert("Empty Key", "Cannot save an empty key.");
	        return; 
	    }
		keyBytes = key.getEncoded();
		File keyFile = new File("src/encryptionApp/key.txt");
		keyFile.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(keyFile);
		fos.write(keyBytes);
		fos.flush();
		fos.close();
		
		//save to db
		//dbManager.saveKey(keyBytes);
	}

	private void setColor(boolean isDark, VBox mainLayout) {
		// TODO Auto-generated method stub
		if(isDark) {
			mainLayout.setStyle("-fx-background-color: lightgray; -fx-text-fill: white;");
		} else {
			mainLayout.setStyle("-fx-background-color: white; -fx-text-fill: black;");
		}
	}

	private void modeSwitch(VBox mainLayout) {
		// TODO Auto-generated method stub
		isDark = !isDark;
		if(isDark) {
			mainLayout.setStyle("-fx-background-color: lightgray; -fx-text-fill: white;");
		} else {
			mainLayout.setStyle("-fx-background-color: white; -fx-text-fill: black;");
		}
	}

	private void handleCheckbox(CheckBox selectedC) {
		// TODO Auto-generated method stub
		if(selectedC == caesarCheck) {
			desCheck.setSelected(false);
			aesCheck.setSelected(false);
		}else if(selectedC == desCheck) {
			caesarCheck.setSelected(false);
			aesCheck.setSelected(false);
		}else if(selectedC == aesCheck) {
			caesarCheck.setSelected(false);
			desCheck.setSelected(false);
		}
	}

	private void encript() {
		// TODO Auto-generated method stub
		outputArea.clear();
		
		String inputText = inputArea.getText();
		if(caesarCheck.isSelected()) {
			if (caesarKeyField.getText().isEmpty()) {
	            showAlert("Caesar Key Missing", "Please enter a Caesar key.");
	            return; 
	        }
			int caesarKey = Integer.parseInt(caesarKeyField.getText());
			String cipherText = caesarEncrypt(inputText, caesarKey);
			outputArea.setText(cipherText);
		}else if(desCheck.isSelected()) {
			String cipherText = desEncrypt(inputText);
			outputArea.setText(cipherText);
		}else if(aesCheck.isSelected()) {
			String cipherText = aesEncrypt(inputText);
			outputArea.setText(cipherText);
		}
	}

	private void showAlert(String title, String message) {
		// TODO Auto-generated method stub
		Alert alert = new Alert(Alert.AlertType.ERROR);
	    alert.setTitle(title);
	    alert.setHeaderText(null);
	    alert.setContentText(message);
	    alert.showAndWait();
	}

	private void decript() {
		// TODO Auto-generated method stub
		outputArea.clear();
		
		String inputText = inputArea.getText();
		if(caesarCheck.isSelected()) {
			if (caesarKeyField.getText().isEmpty()) {
	            showAlert("Caesar Key Missing", "Please enter a Caesar key.");
	            return; 
	        }
			int caesarKey = Integer.parseInt(caesarKeyField.getText());
			String plainText = caesarDecrypt(inputText, caesarKey);
			outputArea.setText(plainText);
		}else if(desCheck.isSelected()) {
			String cipherText = desDecrypt(inputText);
			outputArea.setText(cipherText);
		}else if(aesCheck.isSelected()) {
			String cipherText = aesDecrypt(inputText);
			outputArea.setText(cipherText);
		}
	}

	public String caesarEncrypt(String inputText, int key)
	{
		StringBuilder cipherText = new StringBuilder();
        for (int i = 0; i < inputText.length(); i++) {
            char plainCharacter = inputText.charAt(i);
            int position = alphabet.indexOf(plainCharacter);
            if (position != -1) {
                int newPosition = (position + key) % alphabet.length();
                if (newPosition < 0) {
                    newPosition += alphabet.length();
                }
                char cipherCharacter = alphabet.charAt(newPosition);
                cipherText.append(cipherCharacter);
            }
        }
        return cipherText.toString();
	}
	
	public String caesarDecrypt(String cipherText, int key)
	{
		StringBuilder plainText = new StringBuilder();
        for (int i = 0; i < cipherText.length(); i++) {
            char cipherCharacter = cipherText.charAt(i);
            int position = alphabet.indexOf(cipherCharacter);
            if (position != -1) {
                int newPosition = (position - key) % alphabet.length();
                if (newPosition < 0) {
                    newPosition += alphabet.length();
                }
                char plainCharacter = alphabet.charAt(newPosition);
                plainText.append(plainCharacter);
            }
        }
        return plainText.toString();
	}
	
	private String desEncrypt(String inputText) {
		// TODO Auto-generated method stub
		try {
			Cipher desCipher = Cipher.getInstance("DES");
			desCipher.init(Cipher.ENCRYPT_MODE, getDesKey());
			byte[] byteDataToEncrypt = inputText.getBytes();
			byte[] byteCipherText = desCipher.doFinal(byteDataToEncrypt);
			String encodedText = Base64.getEncoder().encodeToString(byteCipherText);
			return new String(encodedText);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "DES Error";
		}
		
	}

	private Key getDesKey() throws NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		if(desKey == null) {
			KeyGenerator key = KeyGenerator.getInstance("DES");
			desKey = key.generateKey();
		}
		return desKey;
	}
	
	private String desDecrypt(String inputText) {
		// TODO Auto-generated method stub
		try {
			Cipher desCipher = Cipher.getInstance("DES");
			desCipher.init(Cipher.DECRYPT_MODE, getDesKey());
			byte[] decodedBytes = Base64.getDecoder().decode(inputText);
			byte[] byteDecryptedText = desCipher.doFinal(decodedBytes);
			return new String(byteDecryptedText);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "DES Error";
		}
		
	}
	
	private String aesEncrypt(String inputText) {
		// TODO Auto-generated method stub
		try {
			Cipher desCipher = Cipher.getInstance("AES");
			desCipher.init(Cipher.ENCRYPT_MODE, getAesKey());
			byte[] byteDataToEncrypt = inputText.getBytes();
			byte[] byteCipherText = desCipher.doFinal(byteDataToEncrypt);
			String encodedText = Base64.getEncoder().encodeToString(byteCipherText);
			return encodedText;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "AES Error";
		}
		
	}

	private Key getAesKey() throws NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		if(aesKey == null) {
			KeyGenerator key = KeyGenerator.getInstance("AES");
			key.init(256);
			aesKey = key.generateKey();
		}
		return aesKey;
	}
	
	private String aesDecrypt(String inputText) {
		// TODO Auto-generated method stub
		try {
			Cipher desCipher = Cipher.getInstance("AES");
			desCipher.init(Cipher.DECRYPT_MODE, getAesKey());
			byte[] decodedBytes = Base64.getDecoder().decode(inputText);
			byte[] byteDecryptedText = desCipher.doFinal(decodedBytes);
			return new String(byteDecryptedText);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "AES Error";
		}
	}
	
	public void stop() throws Exception {
        super.stop();
        try {
            dbManager.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle disconnection error
        }
    }
	
	
}
