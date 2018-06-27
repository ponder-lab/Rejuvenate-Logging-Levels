package p;

import java.util.logging.*;

public class A {

	public boolean m(String[] args) {
		Logger.getGlobal().info("hi");

		LogRecord record = new LogRecord(Level.WARNING, "test");
		Logger.getGlobal().log(record);

		return true;
	}

}
