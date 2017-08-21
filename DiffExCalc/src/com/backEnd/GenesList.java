package com.backEnd;
import java.util.ArrayList;

/**
 * GenesList serves as a wrapper class for ArrayList. It stores all of the genes
 * present in a single data file. Each GenesList can hold the value of the file
 * that it was made from, which facilitates the making of matrices.
 * 
 * @author Agastya Sharma
 * @version 1.0
 * Date: March 15th, 2017
 */
public class GenesList extends ArrayList<Gene> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4116347293671434383L;
	private String derivedFile; // file that the list was made from

	/**
	 * default constructor call the ArrayList constructor
	 * 
	 */
	public GenesList() {
		super();
	}

	/**
	 * This method is an accessor for the derivedFile field.
	 * 
	 * @return derivedFile: file of the GenesList
	 */
	public String getDerivedFile() {
		return derivedFile;
	}

	/**
	 * This method is a mutator for the derivedFile field.
	 * 
	 * @param derivedFile
	 *            : file of the GenesList
	 */
	public void setDerivedFile(String derivedFile) {
		this.derivedFile = derivedFile;
	}

}
