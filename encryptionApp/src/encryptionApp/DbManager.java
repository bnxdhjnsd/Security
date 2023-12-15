package encryptionApp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Properties;

import javafx.scene.control.Alert;


public class DbManager {
	private Connection connection;
	
	private static final String dbUrl = "db.url";
	private static final String dbUser = "db.username";
	private static final String dbPassword = "db.password";
	
	public Connection connect() throws SQLException, IOException {
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Properties properties = Config.getProperties();
			String url = properties.getProperty(dbUrl);
	        String username = properties.getProperty(dbUser);
	        String password = properties.getProperty(dbPassword);
//			String url = "jdbc:mysql://security.chquo6r7m5j5.ap-southeast-2.rds.amazonaws.com:3306/test";
//			String username = "admin";
//			String password = "12345678";
			connection = DriverManager.getConnection(url, username, password);
			System.out.println("DB Connected.");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			throw new SQLException("DB Error.");
		}
		return connection;
	}
	
	public void disconnect() throws SQLException {
		if(connection != null && !connection.isClosed()) {
			connection.close();
			System.out.println("DB Disconnected.");
		}
	}
	
	public void saveColor(boolean isDark) throws IOException {
		try {
			connect();
			String updateQuery = "UPDATE color SET color = ?";
			PreparedStatement preparedStmt = connection.prepareStatement(updateQuery);
            preparedStmt.setBoolean(1, isDark);
            int rowsAffected = preparedStmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Color mode saved successfully!");
            } else {
                System.out.println("Failed to save color mode!");
            }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				disconnect();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean load() throws IOException {
		boolean isDark = false;
		try {
			connect();
			String selectQuery = "SELECT color From color";
			PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
	        ResultSet resultSet = preparedStatement.executeQuery();
	        if (resultSet.next()) {
	            isDark = resultSet.getBoolean("color");
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				disconnect();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return isDark;
	}
	
	public void saveKey(byte[] keyBytes) throws IOException {
		// TODO Auto-generated method stub
		try {
			String keyString = Base64.getEncoder().encodeToString(keyBytes);
			connect();
			String updateQuery = "UPDATE deskey SET desKey = ?";
			PreparedStatement preparedStmt = connection.prepareStatement(updateQuery);
			preparedStmt.setString(1, keyString);
			int rowsAffected = preparedStmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Des key saved successfully!");
            } else {
                System.out.println("Failed to save Des key!");
            }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				disconnect();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

	public byte[] loadKey() throws IOException {
	    byte[] loadedKey = null;
	    try {
	        connect();
	        String selectQuery = "SELECT desKey FROM deskey";
	        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
	        ResultSet resultSet = preparedStatement.executeQuery();
	        if (resultSet.next()) {
	        	String keyString = resultSet.getString("desKey");
	            loadedKey = Base64.getDecoder().decode(keyString);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            disconnect();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	    return loadedKey;
	}
	
	public boolean validateUser(String user, String pass) throws IOException {
		boolean isValid = false;
		try {
			connect();
			System.out.println("Start comparing passwords.");
			String selectQuery = "SELECT * FROM users WHERE username = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
	        preparedStatement.setString(1, user);
//	        preparedStatement.setString(2, pass);
	        ResultSet resultSet = preparedStatement.executeQuery();
//	        System.out.println(resultSet.next());
	        if(resultSet.next()) {
	        	String dbPassword = resultSet.getString("password");
//	        	System.out.println("The password from DB is: " + dbPassword);
	        	String hashedInputPassword = HashPassword.hashPassword(pass);
//	        	System.out.println(hashedInputPassword);
	        	isValid = dbPassword.equals(hashedInputPassword);
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        try {
	            disconnect();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	    return isValid;
	}
	
	public void saveEncryptedMessage(String encryptedMessage) {
        try {
            connect();
            String updateQuery = "UPDATE message SET message = ?";
            PreparedStatement preparedStmt = connection.prepareStatement(updateQuery);
            preparedStmt.setString(1, encryptedMessage);
            int rowsAffected = preparedStmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Message saved!");
            } else {
                System.out.println("Message failed saving!");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                disconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String loadEncryptedMessage() {
        String encryptedMessage = null;
        try {
            connect();
            String selectQuery = "SELECT message FROM message";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                encryptedMessage = resultSet.getString("message");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                disconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return encryptedMessage;
    }
//	private String encrypt(String inputText) {
//		// TODO Auto-generated method stub
//		StringBuilder encryptedText = new StringBuilder();
//
//	    for (int i = 0; i < inputText.length(); i++) {
//	        char currentChar = inputText.charAt(i);
//
//	        if (Character.isLetter(currentChar)) {
//	            char encryptedChar = (char) (((currentChar - 'A' + secKey) % 26) + 'A');
//	            encryptedText.append(encryptedChar);
//	        } else {
//	            encryptedText.append(currentChar);
//	        }
//	    }
//
//	    return encryptedText.toString();
//	}
//
//	private String decrypt(String inputText) {
//		// TODO Auto-generated method stub
//		StringBuilder decryptedText = new StringBuilder();
//
//	    for (int i = 0; i < inputText.length(); i++) {
//	        char currentChar = inputText.charAt(i);
//
//	        // Check if it's a letter
//	        if (Character.isLetter(currentChar)) {
//	            char decryptedChar = (char) (((currentChar - 'A' - secKey + 26) % 26) + 'A');
//	            decryptedText.append(decryptedChar);
//	        } else {
//	            decryptedText.append(currentChar);
//	        }
//	    }
//
//	    return decryptedText.toString();
//	}
}
