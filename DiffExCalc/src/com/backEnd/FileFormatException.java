package com.backEnd;

/**
 * This class is an exception thrown if the user has entered incorectly
 * formatted metadata and/or data files. It is handled by the MainWindow.java
 * class.
 * 
 * @version 1.0
 * @author Agastya Sharma July 14th, 2017
 */
public class FileFormatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileFormatException() {
		// TODO Auto-generated constructor stub
	}

	public FileFormatException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public FileFormatException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public FileFormatException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public FileFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
