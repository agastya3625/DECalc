package com.ui;

/**
 * This class is an exception thrown if the user is using obsolete R or Java
 * softwares. It is handled by the Start.java class.
 * 
 * @version 1.0
 * @author Agastya Sharma July 14th, 2017
 */
@SuppressWarnings("serial")
public class VersionException extends Exception {

	public VersionException() {
		// TODO Auto-generated constructor stub
	}

	public VersionException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public VersionException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public VersionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public VersionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
