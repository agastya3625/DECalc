package com.backEnd;

/**
 * This class creates a filter to eliminate all "invisible" system files that may be present in the directories the user selects to use. 
 * 
 * @author Agastya Sharma Date: July 14th, 2017
 * @version 1.0
 */

import java.io.*;

public class FileFilterer implements FilenameFilter {

	/**
	 * Default Constructor
	 */
	public FileFilterer() {
		super();
	}

	/**
	 * This method determines which files to exclude from use. All system files
	 * (files that start with "." at the beginning) are eliminated.
	 * 
	 * @param dir:
	 *            directory to search in
	 * @param name:
	 *            name of the file
	 * @return true if accepted, false otherwise.
	 */
	@Override
	public boolean accept(File dir, String name) {
		if (name.startsWith(".")) {
			return false;
		} else if(name.toLowerCase().contains("run log")){
			return false;
		} else if(name.contains("_SIG.csv")){
			return false;
		}
		return true;
	}

}
