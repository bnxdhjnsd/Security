package encryptionApp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HashPassword {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String username = "123";
		String password = "123";
		
		String hashedPassword = hashPassword(password);
		insertToDb(username, hashedPassword);
	}

	public static String hashPassword(String password) {
		// TODO Auto-generated method stub
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(password.getBytes());
			byte[] hashedBytes = md.digest();
			StringBuilder sb = new StringBuilder();
			for(byte b : hashedBytes) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	private static void insertToDb(String username, String hashedPassword) {
		try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/security", "root", "");
            String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, hashedPassword);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Username and hashed password inserted successfully!");
            } else {
                System.out.println("Failed to insert username and hashed password!");
            }
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
}
