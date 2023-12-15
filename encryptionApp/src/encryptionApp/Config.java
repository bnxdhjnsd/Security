package encryptionApp;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
	private static final String configFile = "src/encryptionApp/config.properties";
	public static Properties getProperties() throws IOException {
		Properties properties = new Properties();
		try(FileInputStream input = new FileInputStream(configFile)){
			properties.load(input);
		}
		return properties;
	}
}
