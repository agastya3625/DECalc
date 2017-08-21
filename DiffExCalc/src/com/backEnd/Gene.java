package com.backEnd;

import java.util.ArrayList;

/**
 * This class is used to store Genes. Genes in raw files have three kinds of
 * count values: un-normalized, TPM, and FPKM. The un-normalized counts show the
 * raw counts of the gene. The FPKM and TPM, however, normalize the raw counts
 * to certain lengths, thus providing adjusted values that account for different
 * gene sizes. Additionally, because the same genes may appear in many trials,
 * and each trial has its own file and count values, the countsArray and files
 * fields are used to store multiple values for a given genes. The algorithm for
 * storing these ensure that the value of the gene and its respective trial are
 * stored at the same index of their respective arrays. For example, if Trial x
 * had a value of 1.105, then the values at index x - 1 of each array would be:
 * Trial x and 1.105, respectively.
 * 
 * @author Agastya Sharma
 * @version 1.0 Date: March 15th, 2017
 */
public class Gene {
	private String geneID; // ID of the gene
	private String countsTPM; // TPM counts of a Gene
	private double count; // un-normalized counts of a Gene
	private double genCount; // general count, used to store a count value as a
								// String for easier access
	private String countsFPKM; // FPKM counts of a Gene
	// array that stores counts values for the same gene over multiple trials
	public ArrayList<Double> countsArray = new ArrayList<Double>();
	// array that holds the filenames of all of the files (trials) that the gene
	// is present in.
	public ArrayList<String> files = new ArrayList<String>();

	public Gene() {

	}

	public Gene(String string) {
		this.setgeneID(string);
	}

	/**
	 * This method is used to check for duplicates in GenesLists. The
	 * ArrayList.contains() method indirectly uses this .equals() method to
	 * check if the genes are equal. If the genes have 1) the same address in
	 * memory or 2) the same ID, then they are equal.
	 * 
	 * @param obj:
	 *            obj (must be a Gene)
	 * @return true if equal, false if not
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Gene) { // checks if obj is a Gene obj
			if (this == obj) { // compares references
				return true;
				// compares the names of Gene objects
			} else if (this.getgeneID().equals(((Gene) obj).getgeneID())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method is an accessor for the gene ID
	 * 
	 * @return gene ID
	 */
	public String getgeneID() {
		return geneID;
	}

	/**
	 * This method is an accessor for the genCounts field.
	 * 
	 * @return genCounts as a String
	 */
	public double getgenCount() {
		return genCount;
	}

	/**
	 * This method is an accessor for the un-normalized counts field.
	 * 
	 * @return un-normalized counts value as a String
	 */
	public double getCount() {
		return count;
	}

	/**
	 * This method is an accessor for the TPM counts field.
	 * 
	 * @return TPM counts as a String
	 */
	public String getCountTPM() {
		return countsTPM;
	}

	/**
	 * This method is an accessor for the FPKM counts field.
	 * 
	 * @return FPKM counts as a String
	 */
	public String getCountFPKM() {
		return countsFPKM;
	}

	/**
	 * This method is a mutator for the TPMCounts field.
	 * 
	 * @param countsTPM:
	 *            TPM counts value in integer format
	 */
	public void setTPM(int countsTPM) {
		this.countsTPM = Integer.toString(countsTPM);
	}

	/**
	 * This method is a mutator for the FPKM count value.
	 * 
	 * @param countsFPKM:
	 *            the FPKM count value as an integer
	 */
	public void setFPKM(int countsFPKM) {
		this.countsFPKM = Integer.toString(countsFPKM);
	}

	/**
	 * This method is a mutator for the un-normalized count field.
	 * 
	 * @param count:
	 *            the un-normalized count, in integer format
	 */
	public void setCount(double count) {
		this.count = count;
	}

	/**
	 * This method is a mutator for the genCount field.
	 * 
	 * @param count:
	 *            String count
	 */
	public void setgenCount(double count) {
		this.genCount = count;
	}

	/**
	 * This method is a mutator for the Gene's ID field.
	 * 
	 * @param ID
	 *            of the Gene
	 */
	public void setgeneID(String ID) {
		if (ID.contains("\"")) {
			ID = ID.replace("\"", "");
		}
		this.geneID = ID;
	}

	/**
	 * This method is an accessor for the files array.
	 * 
	 * @return String representation of the files array.
	 */
	public String getFiles() {
		String toReturn = new String();
		for (int i = 0; i < files.size(); i++) {
			toReturn = toReturn.concat(" " + files.get(i) + ";");
		}
		return toReturn;
	}

	/**
	 * This method adds a value to the files array.
	 * 
	 * @param fileName
	 *            : name of file ot be added
	 */
	public void addToFiles(String fileName) {
		files.add(fileName);
	}

	/**
	 * This method is an accessor for the counts array. It is turned into a
	 * String and returned as a String instead of an array.
	 * 
	 * @return String representation of the counts array
	 */
	public String getCountsArray() {
		String toReturn = countsArray.toString();
		toReturn = toReturn.replace("[", "");
		toReturn = toReturn.replace("]", "");
		return toReturn;
	}

	/**
	 * Adds a counts value to the counts Array
	 * 
	 * @param d
	 *            : count value of the gene during this specific trial, in
	 *            String format.
	 */
	public void addToCounts(double d) {
		countsArray.add(d);
	}

	public String toString() {
		return geneID;
	}

	public double getSum() {
		double toReturn = 0;
		for (int i = 0; i < countsArray.size(); i++) {
			toReturn += countsArray.get(i);
		}
		return toReturn;
	}
}
