package com.omniscient.log4jcontrib.swingappender.ui.test;

import com.omniscient.log4jcontrib.swingappender.ui.SwingAppenderUI;

/**Test class for SwingAppenderUI.
 * This is NOT a unit test class. It creates a SwingAppenderUI
 * and sends log statements to it. The target class has to be
 * tested manually.
 * @author pshah
 */
public class SwingAppenderUITest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SwingAppenderUI loggerWin = SwingAppenderUI.getInstance();
		//test
		for(int i=0;;i++) {
			try {Thread.sleep(100);} catch(InterruptedException ie) {}
			//A log string always ends with a newline character. Hence we add
			//one here manually to simulate similar output
			loggerWin.doLog("message message message message " + i + "\n");
		}
	}
}
