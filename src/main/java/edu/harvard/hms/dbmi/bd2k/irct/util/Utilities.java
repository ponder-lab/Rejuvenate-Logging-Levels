package edu.harvard.hms.dbmi.bd2k.irct.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import edu.harvard.hms.dbmi.bd2k.irct.event.IRCTEventListener;

public class Utilities {
	
	public static IRCTEventListener getIRCTEventListener() {
		try {
			InitialContext ic = new InitialContext();
			return (IRCTEventListener) ic.lookup("java:module/IRCTEventListener");
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
