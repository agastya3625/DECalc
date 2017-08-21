package com.backEnd;
import java.io.*;
import java.util.Scanner;

/**
 * This class converts .tsv files into .csv files so that they are more usable for the R script
 * 
 * @author Agastya Sharma Date: July 14th, 2017
 * @version 1.0
 */
public class TSVtoCSV {
	
	/**
	 * main method does the main conversion
	 * 
	 * @param args: File[] of .tsv files to convert to .csvs
	 * @param outdir: directory to output files into
	 * 
	 * @throws IOException caused by FileInputStream
	 */
	public static void main(File[] args, File outdir) throws IOException {
		for (File f : args) {
			FileInputStream fileIn = new FileInputStream(f);
			Scanner scan = new Scanner(fileIn);
			File outfile = new File(outdir+"/"+f.getName().substring(0, f.getName().indexOf(".txt")) + ".csv");
			PrintWriter out = new PrintWriter(outfile);
			//just replaces tabs with commas
			while (scan.hasNext()) {
				String temp = scan.nextLine();
				temp = temp.replace(",", ";");
				temp = temp.replace("\t", ",");
				out.println(temp);
			}
			scan.close();
			out.close();
		}

	}
	
	/**
	 * removes extraneous spaces in lines of text in a file.
	 * 
	 * @param f: File to trim
	 * 
	 * @throws IOException caused by FileInputStream
	 * 
	 * @deprecated
	 */
	public static void Trims(File f) throws IOException{
		FileInputStream fileIn = new FileInputStream(f);
		Scanner scan = new Scanner(fileIn);
		File outfile = new File("outfile.txt");
		PrintWriter out = new PrintWriter(outfile);
		//trims each line individually
		while(scan.hasNext()){
			String temp = scan.nextLine();
			temp = temp.trim();
			out.println(temp);
		}
		outfile.renameTo(f);
		scan.close();
		out.close();
		fileIn.close();
	}
	
	/**
	 * additional converter that takes in a file
	 * 
	 * @param tsv: file to convert
	 * 
	 * @return .csv File
	 * 
	 * @throws IOException caused by FileInputStream
	 */
	public static File TSVtoCSVcon(File tsv) throws IOException{
		FileInputStream fileIn = new FileInputStream(tsv);
		Scanner scan = new Scanner(fileIn);
		File outfile = new File(tsv.getPath().substring(0, tsv.getName().indexOf(".")) + ".csv");
		PrintWriter out = new PrintWriter(outfile);
		while (scan.hasNext()) {
			String temp = scan.nextLine();
			temp = temp.replace(",", ";");
			temp = temp.replace("\t", ",");
			out.println(temp);
		}
		scan.close();
		out.close();
		fileIn.close();
		return outfile;
	}
}
