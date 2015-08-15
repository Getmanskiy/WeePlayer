package com.omniscient.log4jcontrib.swingappender.ui.test;

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**Test class for SwingAppende.
 * This is NOT a unit test class. It creates a SwingAppenderUI
 * and sends log statements to it. The target class has to be
 * tested manually.
 * @author pshah
 */
public class SwingAppenderTest {

	static{
		URL configFileUrl = ClassLoader.getSystemResource("log4j.properties");
		System.out.println("Config File - " + configFileUrl.getPath());
		PropertyConfigurator.configure(configFileUrl);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger logger = Logger.getLogger("name");
		for (int i = 0; i < 1000; i++) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException ie) {
			}
			logger.info("message  " + i);
		}
	}
}
