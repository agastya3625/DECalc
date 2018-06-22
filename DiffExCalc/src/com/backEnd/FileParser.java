//package com.backEnd;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Scanner;
//
///**
// * This class is used to parse files found in gene expression analysis and
// * convert large amounts of files into matrix form. Raw files are files that
// * have been created by DNA sequencing and genome alignment softwares. Raw
// * matrices were inserted into DESeq2, an R program that calculated fold changes
// * of genes and subsequently enumerated significant genes. Finally, the files
// * created by DESeq2 are used to create matrices of significant genes.
// * 
// * Throughout this program, different kinds of matrices will be referenced.
// * Thus, in this program, Type I matrices are matrices made from raw data files.
// * Type I matrices are passed into DESeq2. Type II and Type III matrices are
// * matrices that are made from the DESeq2 output files. These matrices organize
// * the genes in order to easily identify statistically significant genes to
// * further study. Type II matrices simply hold values for significant genes.
// * Thus, is a gene is significant in one trial but not in another, its value
// * will only appear in these matrices for the trial in which it was significant.
// * Type III matrices , like Type II matrices, also show values for all
// * significant genes. However, these matrices also show the corresponding
// * non-significant values for each non-significant gene or trial. These matrices
// * further help identify genes to study further.
// * 
// * This class still is in its infancy in development. Further expansions include
// * optimization of algorithm, addition of advanced error-handling, and the
// * possible addition of an auxiliary class responsible for running the user
// * interface outside of the main FileParser class.
// * 
// * @author Agastya Sharma Date: March 15th, 2017
// * @version 1.0
// */
//public class FileParser {
//	private FileInputStream fileIn; // used for scanning files
//	private PrintWriter out; // used for writing data to files
//	private Scanner in; // Scanner dedicated to files
//
//	private Gene toAdd; // Gene to add to lists, used in several methods
//	public String[] tags = "Toxo_10d_SIG, Toxo_28d_SIG, WNV_10d_SIG, WNV_29d_SIG".split(", ");
//
//	/**
//	 * This method is used to handle raw data files. These files created
//	 * matrices for each of the TPM, FPKM, and un-normalized counts. Because
//	 * TPM, FPKM, and un-normalized counts were used in different matrices, this
//	 * method takes a file that has all three count values for each gene and
//	 * creates three separate GenesLists, one for each count type so that they
//	 * can be added to different matrices with ease. The 2-dimensional ArrayList
//	 * returned is simply used so that all three GenesLists can be returned
//	 * simultaneously.
//	 * 
//	 * @param file:
//	 *            output file for the matrix
//	 * 
//	 * @param fileList
//	 *            : raw data file that needs to be parsed to create matrices.
//	 * 
//	 * @param position:
//	 *            int dictating the TPM, FPKM, or un-normalized counts to put in
//	 *            matrix
//	 * 
//	 * @throws IOException
//	 *             caused by FileInputStream
//	 * 
//	 * @throws FileNotFoundException
//	 *             caused by FileInputStream
//	 */
//
//	public void createRawMatrix(File[] fileList, File file, int position) throws IOException, FileNotFoundException {
//		GenesList g = new GenesList();
//		Scanner[] scans = new Scanner[fileList.length];
//		FileInputStream[] inputs = new FileInputStream[fileList.length];
//		for (int i = 0; i < inputs.length; i++) {
//			inputs[i] = new FileInputStream(fileList[i]);
//		}
//		for (int i = 0; i < fileList.length; i++) {
//			scans[i] = new Scanner(inputs[i]);
//			scans[i].nextLine();
//		}
//		while (scans[0].hasNext()) {
//			Gene toAdd = new Gene();
//			for (int i = 0; i < scans.length; i++) {
//				String[] s = scans[i].nextLine().split("\t");
//				if (toAdd.getgeneID() == null) {
//					toAdd.setgeneID(s[0]);
//				}
//				toAdd.addToCounts(Math.round(Math.round(Double.parseDouble((s[position])))));
//			}
//			g.add(toAdd);
//		}
//		for (int i = 0; i < fileList.length; i++) {
//			inputs[i].close();
//			scans[i].close();
//		}
//		makeMatrix(g, file);
//	}
//
//	public void createSigMatrix(File[] fileList, File fileOut, int threshold) throws IOException, FileNotFoundException {
//		ArrayList<GenesList> masterList = new ArrayList<GenesList>();
//		for(int i = 0; i < fileList.length;i++){
//			masterList.add(toList(fileList[i], ""));
//		}
//		GenesList finalList = condense2DList(masterList);
//		makeMatrix(finalList, null, fileOut);
////		Scanner[] scans = new Scanner[fileList.length];
////		FileInputStream[] inputs = new FileInputStream[fileList.length];
////		for (int i = 0; i < inputs.length; i++) {
////			inputs[i] = new FileInputStream(fileList[i]);
////		}
////		for (int i = 0; i < fileList.length; i++) {
////			scans[i] = new Scanner(inputs[i]);
////			scans[i].nextLine();
////		}
////		for (int i = 0; i < scans.length; i++) {
////			while (scans[i].hasNext()) {
////				String[] s = scans[i].nextLine().split(",");
////				Gene toAdd = new Gene();
////				if (toAdd.getgeneID() == null) {
////					toAdd.setgeneID(s[0]);
////				}
////				if(g.contains(toAdd)){
////					toAdd.addToCounts(Math.round(Math.round(Double.parseDouble((s[])))));
////				} else{
////					toAdd.addToCounts(0);
////					g.add(toAdd);
////				}
////				
////			}
////			
////		}
////		for (int i = 0; i < fileList.length; i++) {
////			inputs[i].close();
////			scans[i].close();
////		}
//	}
//	
//	public void getP_Vals(File toParse, File fileOut, int threshold) throws IOException, FileNotFoundException {
//		FileInputStream inputs = new FileInputStream(toParse);
//		PrintWriter pout = new PrintWriter(fileOut);
//		Scanner scan = new Scanner(inputs);
//		String title = scan.nextLine();
//		title = "Gene ID"+title.substring(0, title.indexOf(",,,,"));
//		pout.println(title);
//		while (scan.hasNext()) {
//			boolean print = false;
//			String[] s = scan.nextLine().split(",");
//			String toPrint = s[0] + ",";
//			for (int i = 1; i < s.length; i++) {
//				double lfc = Double.parseDouble((s[i]));
//				if (lfc > threshold || lfc < threshold * -1) {
//					print = true;
//					toPrint = toPrint + lfc + ",";
//				} else {
//					toPrint = toPrint + "0,";
//				}
//			}
//			if (print) {
//				pout.println(toPrint);
//			}
//		}
//		pout.close();
//		scan.close();
//		inputs.close();
//	}
//
//	/**
//	 * This method makes a matrix from two files. This method would use the
//	 * truncated files created by this program. Matrices are created column by
//	 * column in this method, where each new file being added is a column. This
//	 * method will likely be heavily modified or removed entirely as the switch
//	 * is made to making matrices row by row.
//	 * 
//	 * 
//	 * @param g:
//	 *            GenesList to be made into a matrix
//	 * @param file:
//	 *            File matrix being constructed.
//	 * 
//	 * @throws FileNotFoundException
//	 *             if file cannot be found
//	 */
//	public void makeMatrix(GenesList g, File file) throws FileNotFoundException {
//		out = new PrintWriter(file); // prints to file
//		String temp = "Gene ID, ";
//		// adds the String representation of the files array, becomes the header
//		// for the matrix.
//		String string = Arrays.toString(tags);
//		string = string.replace("[", "");
//		string = string.replace("]", "");
//		string = string.replace(" ", "");
//		temp = temp.concat(string);
//		out.println(temp);
//		for (int i = 0; i < g.size(); i++) {
//			out.println(g.get(i).getgeneID() + ", " + g.get(i).getCountsArray());
//		}
//		out.close();
//	}
//
//	/**
//	 * This method gets rid of any extraneous text at the end of matrix files.
//	 * Often, the algorithm leaves an extra comma, which is dealt with in this
//	 * method.
//	 * 
//	 * @param file:
//	 *            File to be trimmed.
//	 */
//	public void Trim(File file) {
//		try {
//			// secondary file is used to preserve the data in the original while
//			// the method is in progress.
//			File outfile = new File("OutputFile");
//			out = new PrintWriter(outfile);
//			Scanner scan = new Scanner(file);
//			out.println(scan.nextLine());
//			while (scan.hasNext()) {
//				String temp = scan.nextLine();
//				// if (temp.contains("NS, ")) {
//				// takes off the last character of the line.
//				temp = temp.substring(0, temp.length() - 1);
//				// }
//				out.println(temp);
//			}
//			outfile.renameTo(file);
//			scan.close();
//			out.close();
//		} catch (FileNotFoundException e) {
//			System.out.println("Caused by Trim()");
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * This method converts a data file to a list.
//	 * 
//	 * @param filetoParse:
//	 *            File to be made into a list
//	 * 
//	 * @param type:
//	 *            what type of counts to put into the matrix (integer or with
//	 *            decimals)
//	 * 
//	 * @throws FileNotFoundException
//	 *             if filetoParse cannot be found
//	 * 
//	 * @return List representation of all of the genes in the File provided.
//	 */
//	public GenesList toList(File filetoParse, String type) throws FileNotFoundException {
//		fileIn = new FileInputStream(filetoParse);
//		in = new Scanner(fileIn);
//		in.nextLine();
//		GenesList g = new GenesList();
//		String name = filetoParse.getName().substring(0, filetoParse.getName().indexOf(".csv"));
//		g.setDerivedFile(name);
//		String line;
//		String[] lineVals;
//		// parses line by line to add all values to the the GenesList
//		while (in.hasNext()) {
//			line = in.nextLine();
//			lineVals = line.split(",");
//			toAdd = new Gene();
//			String ID = lineVals[0];
//			ID = ID.replace("\"", "");
//			toAdd.setgeneID(ID);
//			toAdd.setCount(Double.parseDouble(lineVals[2]));
//			toAdd.addToCounts(Double.parseDouble(lineVals[2]));
//			// if (type.equals("int")) {
////			for (int i = 1; i < lineVals.length; i++) {
////				toAdd.addToCounts(Double.parseDouble(lineVals[i]));
////			}
//			// toAdd.setgenCount(Math.round(Math.round(Double.parseDouble(lineVals[1]))));
//			// toAdd.addToCounts(Math.round(Math.round(Double.parseDouble(lineVals[1]))));
//			// } else {
//			// toAdd.setgenCount(Double.parseDouble(lineVals[1]));
//			// toAdd.addToCounts(Double.parseDouble(lineVals[1]));
//			// }
//			toAdd.addToFiles(name);
//			g.add(toAdd);
//		}
//		return g;
//	}
//
//	/**
//	 * This method converts a GenesList to a matrix using the values found in
//	 * the counts and files arrays of each gene.
//	 * 
//	 * @param listToAdd
//	 *            : list to convert to a File
//	 * @param secondaryList
//	 *            : this function was used when the significant/non-significant
//	 *            data sets were being merged (see class comment for further
//	 *            information). usually null is passed into this field.
//	 * @param outputFile
//	 *            : file that the data is written into.
//	 * 
//	 * @throws FileNotFoundException
//	 *             if file cannot be found
//	 */
//	public void makeMatrix(GenesList listToAdd, GenesList secondaryList, File outputFile) throws FileNotFoundException {
//		out = new PrintWriter(outputFile);
//		String temp = "Gene ID, ";
//		// adds the String representation of the files array, becomes the header
//		// for the matrix.
//		String string = Arrays.toString(tags);
//		string = string.replace("[", "");
//		string = string.replace("]", "");
//		temp = temp.concat(string);
//		out.println(temp);
//		for (int x = 0; x < listToAdd.size(); x++) {
//			String toPrint = new String();
//			Gene toUse = listToAdd.get(x);
//			toPrint = toPrint.concat(toUse.getgeneID() + ", ");
//			for (int k = 0; k < tags.length; k++) {
//				// if Gene toUse has a significant value for a given file, it is
//				// printed. if not, either the non-significant value is taken
//				// from the secondaryList, or "NS," (Not Significant) is printed
//				// in place of the value.
//				if (toUse.files.contains(tags[k])) {
//						if(toUse.files.size() ==1){
//							toPrint = toPrint.concat(toUse.getCount()+", ");
//						} else{
//							toPrint = toPrint.concat(toUse.countsArray.get(toUse.files.indexOf(tags[k])) + ", ");
//						}
//				} else {
//					// Gene tempGene = secondaryList.get(secondaryList
//					// .indexOf(toUse));
//					// toPrint = toPrint.concat(tempGene.countsArray
//					// .get(tempGene.files.indexOf(tags[k])) + ", ");
//					toPrint = toPrint.concat("0, ");
//				}
//			}
//			out.println(toPrint);
//		}
//		out.close();
//		Trim(outputFile);
//	}
//
//	/**
//	 * This method takes a 2D list of GenesLists derived from files and
//	 * condenses it into one master list. Tests for duplicates by comparing the
//	 * gene ID fields. If duplicates are present, the count value for that gene
//	 * is added to counts array of its duplicate. The derived file is also added
//	 * to files array of its duplicate.
//	 * 
//	 * @param List2D
//	 *            : an arrayList of GenesLists. Each list is a representation of
//	 *            a file.
//	 * @return masterList: a list of individual genes represented, and all of
//	 *         the files they are found in.
//	 */
//	public GenesList condense2DList(ArrayList<GenesList> List2D) {
//		GenesList masterList = new GenesList(); // list to be returned
//		for (int i = 0; i < List2D.size(); i++) {
//			//masterList.setDerivedFile(List2D.get(i).getDerivedFile());
//			for (int j = 0; j < List2D.get(i).size(); j++) {
//				Gene temp = List2D.get(i).get(j);
//				// adds to list if Gene is not a duplicate
//				if (!masterList.contains(temp)) {
//					masterList.add(temp);
//				} else {
//					// // if gene is a duplicate, then its copy is found and
//					// // retrieved. The counts and file values for the
//					// duplicate
//					// // are added to the counts and files arrays of the Gene
//					// // currently in the list.
//					int index = masterList.indexOf(temp);
//					Gene g = masterList.get(index);
//					g.addToFiles(List2D.get(i).getDerivedFile());
//					g.addToCounts(temp.getgenCount());
//					masterList.set(index, g);
//					//masterList.get(masterList.indexOf(temp)).addToCounts(temp.getgenCount());
//
//				}
//			}
//		}
//		return masterList;
//	}
//
//	/**
//	 * This method takes a list of files from a folder and sorts it into a
//	 * customized order to facilitate conversion to a matrix. This method is
//	 * still a work in progress and needs to be fully implemented.
//	 * 
//	 * @param folder:
//	 *            File[] returned by the File.listFiles() method
//	 */
//
//	public void makeTags(File[] folder) {
//		String out = new String();
//		String temp = new String();
//		for (File f : folder) {
//			temp = f.getName();
//			temp = temp.substring(0, temp.indexOf("_S"));
//			out = out.concat(temp + ",");
//		}
//		tags = out.split(",");
//	}
//
//	public File[] sort(String[] tags, File[] initial) {
//		File[] toReturn = new File[tags.length];
//		for (int i = 0; i < toReturn.length; i++) {
//			for (int j = 0; j < initial.length; j++) {
//				if (initial[j].getName().contains(tags[i]) && initial[j].getName().contains("Sig")) {
//					toReturn[i] = initial[j];
//				}
//			}
//		}
//		return toReturn;
//	}
//
//	public ArrayList<GenesList> MatrixDemultiplexer(File f) throws IOException {
//		ArrayList<GenesList> toReturn = new ArrayList<GenesList>();
//		fileIn = new FileInputStream(f);
//		in = new Scanner(fileIn);
//		String[] splits = in.nextLine().split(",");
//		for (int i = 0; i < splits.length - 1; i++) {
//			GenesList g = new GenesList();
//			g.setDerivedFile(splits[i + 1]);
//			toReturn.add(g);
//
//		}
//		// prints all counts
//		while (in.hasNext()) {
//			String[] split = in.nextLine().split(",");
//			for (int i = 0; i < split.length - 1; i++) {
//				Gene toAdd = new Gene();
//				toAdd.setgeneID(split[0]);
//				toAdd.setgenCount(Math.round(Double.parseDouble(split[i + 1])));
//				toAdd.addToCounts(Math.round(Double.parseDouble(split[i + 1])));
//				toReturn.get(i).add(toAdd);
//
//			}
//		}
//		fileIn.close();
//		in.close();
//		return toReturn;
//	}
//
//	public void findDups(File[] files, File output) throws IOException {
//		ArrayList<GenesList> List2D = new ArrayList<GenesList>();
//		for (File f : files) {
//			List2D.add(toList(f));
//		}
//		GenesList g = condense2DList(List2D);
//		out = new PrintWriter(output);
//		out.println(g.getDerivedFile());
//		for (int i = 0; i < g.size(); i++) {
//			out.println(g.get(i).getgeneID() + ", " + g.get(i).getCountsArray());
//		}
//		out.close();
//	}
//
//	private GenesList toList(File f) throws IOException {
//		fileIn = new FileInputStream(f);
//		in = new Scanner(fileIn);
//		GenesList g = new GenesList();
//		g.setDerivedFile(f.getName());
//		while (in.hasNext()) {
//			Gene toAdd = new Gene();
//			String[] line = in.nextLine().split(",");
//			toAdd.setgeneID(line[0]);
//			for (int i = 1; i < line.length; i++) {
//				toAdd.addToCounts(Double.parseDouble(line[i]));
//			}
//			g.add(toAdd);
//		}
//		return g;
//	}
//
//	public String getTitle(File f) throws IOException {
//		fileIn = new FileInputStream(f);
//		in = new Scanner(fileIn);
//		String title = in.nextLine();
//		in.close();
//		fileIn.close();
//		return title;
//	}
//}