package com.backEnd;

import java.io.*;
import java.text.*;
import java.util.*;

import org.eclipse.swt.widgets.*;

/**
 * This class executes the differential expression calculator. it pre-processes
 * all of the data and metadata files, then runs the DEseq2 script.
 * 
 * @author Agastya Sharma Date: July 14th, 2017
 * @version 1.0
 */

public class DiffExCalc {
	private static File dataMatrix, metafolder, outputDir, matrixDir;
	private static StatusUpdate su;
	private static ProgressBar prog;
	private static FileParser fp = new FileParser();
	private static ArrayList<GenesList> deMultiplexedMatrix;
	private static boolean dir;

	/**
	 * The main method contains the main algorithm that runs the R script.
	 * 
	 * @param args:
	 *            String[] that contains the data file/folder, metadata folder,
	 *            and folder to put results files.
	 * @param comps:
	 *            all comparisons that need to be run with the data being passed
	 *            to DESeq2
	 * 
	 * @param progressBar:
	 *            the progress bar that is shown to the user
	 * 
	 * @param runInfo:
	 *            run log file generated
	 * 
	 * @param label:
	 *            text label showing the current running task
	 * 
	 * @param text:
	 *            text box that shows complete history of run tasks
	 * 
	 */
	public static void main(String[] args, ArrayList<ArrayList<String>> comps, ProgressBar progressBar, File runInfo,
			Label label, Text text) {
		if (runInfo.exists()) {
			String absPath = runInfo.getAbsolutePath();
			runInfo.delete();
			runInfo = new File(absPath);
		}
		long startTime = System.currentTimeMillis();
		// used to display the time taken to run.
		try {
			su = new StatusUpdate(runInfo, label, text);
			su.appendStatusLabel("Run Started...");
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						prog = progressBar;
						prog.setMaximum(getSize(comps) + 1);
						prog.setMinimum(0);
					} catch (Exception e) {
						e.printStackTrace(System.err);
					}

				}
			});
			su.sendToFile("\n######## Run Information #########\n");
			su.sendToFile("Data File: " + args[0] + "\n");
			su.sendToFile("metadata folder: " + args[1] + "\n");
			su.sendToFile("Results placed in: " + args[2] + "\n");
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date d = new Date();
			su.sendToFile("Date and time of run start: " + dateFormat.format(d));
			dataMatrix = new File(args[0]);
			metafolder = new File(args[1]);
			outputDir = new File(args[2]);
			dataMatrix.setReadable(true);
			metafolder.setReadable(true);
			outputDir.setWritable(true, false);
			// match the clones
			matrixDir = new File(outputDir.getPath() + "/data matrices");
			matrixDir.mkdirs();
			dir = preProcessingCheck();
			if (dir) {
				su.appendStatusLabel("De-Multiplexing matrices");
				multiplexMatrices();
				// executes the R script
				su.sendToFile("\n######## DESeq2 Script Information #########");
				executeR(matrixDir, metafolder, outputDir, comps);
			} else {
				su.sendToFile("\n######## DESeq2 Script Information #########");
				executeR(dataMatrix, metafolder, outputDir, comps);
			}
			su.sendToFile("\n######## Script Finished #########");
			matrixDir.delete();
			d = new Date();
			su.sendToFile("\nDate and time of run end: " + dateFormat.format(d));
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					prog.setSelection(prog.getMaximum());
				}
			});
			su.appendStatusLabel("Run Finished.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			// always gives the total run time of the run
			try {
				su.sendToFile(showRunTime(startTime));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method makes the matrices required per metadata file. This way, even
	 * though the data matrix has all trials, it is not all passed through the
	 * DESeq2 program at once, thereby optimizing its run time.
	 * 
	 * @param matrixForm:
	 *            String[] showing what clones need to be in the matrix and in
	 *            what order
	 * @param deMultiplexedMatrix:
	 *            matrix represented by an ArrayList of GenesLists, each one
	 *            representing a column in the matrix.
	 * @param metafile:
	 *            metadata file.
	 * @param matrixFolder:
	 *            folder to put the made matrices into.
	 * 
	 * @throws IOException
	 *             caused by FileInputStream
	 */
	public static void MatrixMaker(String[] matrixForm, ArrayList<GenesList> deMultiplexedMatrix, File metafile,
			File matrixFolder) throws IOException {
		FileParser fp = new FileParser();
		// creates a list of GenesLists representing the matrix. This list is
		// then converted into a data matrix.
		ArrayList<GenesList> List2D = new ArrayList<GenesList>();
		for (int i = 0; i < matrixForm.length; i++) {
			List2D.add(fetchList(deMultiplexedMatrix, matrixForm[i]));
		}
		File datamat = new File(matrixFolder.getPath() + "/"
				+ metafile.getName().substring(0, metafile.getName().indexOf(".csv") - 1) + "_DataMatrix.csv");
		// System.out.println(datamat);
		GenesList matrix = fp.condense2DList(List2D);
		fp.tags = matrixForm;
		fp.makeMatrix(matrix, datamat);
	}

	/**
	 * This method creates the bash file that is used to run DESeq2. It creates
	 * a separate call to the R script for each metadata file present. It also
	 * prints the command line arguments required by the R script in the format
	 * they are required to be in.
	 * 
	 * Bash file line format: "usr/bin/local/RScript" "R file" "data matrix"
	 * "metadata file" "list of comparisons within metadata file" "output
	 * directory" "levels of Type variable"
	 * 
	 * Everything after "R file" is the command line arguments for the R script.
	 * Details are within the R file itself.
	 * 
	 * @param datamatrix:
	 *            data matrix to run DESeq2 on
	 * @param metadata:
	 *            File[] containing all metadata files
	 * @param outdir:
	 *            output directory name and path
	 * @param mastercomps:
	 *            comparisons to run
	 * 
	 * @return bash file to run
	 * 
	 * @throws IOException
	 *             caused by PrintWriter
	 */
	private static File bashMaker(File datamatrix, File[] metadata, String outdir, ArrayList<ArrayList<String>> comps)
			throws IOException {
		File[] dataFiles;
		boolean dataDirectory = false;
		if (datamatrix.isDirectory()) {
			dataDirectory = true;
			dataFiles = datamatrix.listFiles(Config.FileFilter);
		} else {
			dataDirectory = false;
			dataFiles = null;
		}
		// bash file

		File bash;
		if (Config.OS_TYPE.contains("Mac")) {
			bash = new File("Rbash.sh");
		} else {
			bash = new File("Rbash.bat");
		}
		PrintWriter pout = new PrintWriter(bash);
		if (Config.OS_TYPE.toLowerCase().contains("mac")) {
			pout.println(Config.MAC_BASH_HEAD);
		}
		// data matrix full file path
		String data = "\"" + datamatrix.getPath() + "\"";
		// creates the actual bash file with correct command line arguments for
		// the R script.
		for (int i = 0; i < metadata.length; i++) {
			// parses the metadata file and gets the possible values of the last
			// column (the only variable column per our metadata file structure)
			// and passes it into the R script.
			ArrayList<ArrayList<String>> elems = MetaDataParser.parse(metadata[i]);
			elems.remove(0);
			String[] elements = new String[elems.size()];
			for (int j = 0; j < elems.size(); j++) {
				String temp = elems.get(j).toString();
				if (Config.OS_TYPE.toLowerCase().contains("mac")) {
					temp = temp.replace("[", "'");
					temp = temp.replace("]", "'");
					temp = temp.replace(",", "','");
					temp = temp.replace(" ", "");
				} else {
					temp = temp.replace("[", "");
					temp = temp.replace("]", "");
					temp = temp.replace(" ", "");
				}
				elements[j] = temp;
			}

			String conts = Arrays.toString(elements);
			conts = conts.replace("[", "");
			conts = conts.replace("]", "");
			conts = conts.replace(", ", " ");

			String compars = comps.get(i).toString();
			compars = compars.replace(", ", "'-'");
			compars = compars.replace(" ", "','");
			compars = compars.replace("[", "'");
			compars = compars.replace("]", "'");
			if (Config.OS_TYPE.toLowerCase().contains("win")) {
				compars = compars.replaceAll("vs", "_");
			}
			// name of R file to be run
			String fileName = Config.RFile_Name;
			if (dataDirectory) {
				data = "\"" + dataFiles[i].getPath() + "\"";
			}
			if (!comps.get(i).isEmpty()) {
				String command = Config.R_COMMAND + " " + "\""+fileName+"\"" + " " + data + " " + "\"" + metadata[i] + "\"" + " "
						+ compars + " " + "\"" + outdir + "\"" + " " + conts;
				su.sendToFile("Command Line for metadata file " + metadata[i] + ":\n\n" + command+"\n\n");
				pout.println(command);
			}
		}
		pout.close();
		bash.setExecutable(true);
		return bash;
	}

	/**
	 * This method handles the running of the R script. It first creates a bash
	 * file that allows for command line arguments to be generated and passed
	 * into the R script. Then, it executes that bash file.
	 * 
	 * @param data:
	 *            data matrix
	 * @param metadata:
	 *            folder of metadata files
	 * @param results:
	 *            results directory
	 * @param comps:
	 *            comparisons to be run
	 * @throws IOException
	 *             caused by FileInputStream
	 * 
	 */
	private static void executeR(File data, File metadata, File results, ArrayList<ArrayList<String>> comps)
			throws IOException {
		// makes the bash file
		File bash = bashMaker(data, metadata.listFiles(Config.FileFilter), results.getPath(), comps);
		bash.setExecutable(true);
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				prog.setSelection(prog.getSelection() + 1);
			}

		});
		// handles the running of the bash file-all output is piped to the
		// console.
		Process pb = new ProcessBuilder(bash.getAbsolutePath()).start();
		BufferedReader read = new BufferedReader(new InputStreamReader(pb.getInputStream()));
		BufferedReader out = new BufferedReader(new InputStreamReader(pb.getErrorStream()));
		String output = read.readLine();
		String error = out.readLine();
		while (output != null || error != null) {
			if (output != null) {
				if (output.contains("]")) {
					output = output.substring(output.indexOf("]") + 1, output.length());
				}
				if (output.contains("Comparison") || output.contains("=========")) {
					if (output.contains("Comparison")) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								try {
									if (!prog.isDisposed()) {
										prog.setSelection(prog.getSelection() + 1);
									}
								} catch (Exception e) {
									e.printStackTrace(System.err);
								}
							}

						});
						su.appendStatusLabel(output + "...");
					}
					su.sendToFile(output);
				} else {
					su.appendStatusLabel(output + "...");
				}

			}
			if (error != null) {
				// avoids printing unneccessary output caused by the R
				// script so
				// that the ouput is presented to the user in a way they can
				// easily track it the status of the run.
				if (!error.contains("package") && !error.contains("cluster") && !error.contains("    ")) {
					System.err.println(error);
				}
			}
			output = read.readLine();
			error = out.readLine();
		}
		read.close();
		out.close();
		bash.delete();
	}

	/**
	 * This method returns a GenesList based on the biological trial name. It is
	 * used in building the data matrices that are passed through the
	 * DiffExcalc. The matrix is demultiplexed by converting each column into
	 * its own GenesList. This method then looks for the GenesList associated
	 * with a certain biological replicate to add to a new matrix.
	 * 
	 * @param deMultiplexedMatrix:
	 *            the data matrix, stored as an ArrayList of GenesLists, one per
	 *            column of the data matrix.
	 * @param tag:
	 *            ID for each genesList that the GenesList name will
	 *            contain-used for parsing.
	 * 
	 * @return GenesList for the biological replicate in question.
	 */
	private static GenesList fetchList(ArrayList<GenesList> deMultiplexedMatrix, String tag) {
		for (GenesList f : deMultiplexedMatrix) {
			if (f.getDerivedFile().contains(tag)) {
				return f;
			}
		}
		return null;
	}

	/**
	 * This method calculates the differentiation index (a metric used to rank
	 * prospective clones-currently an unused function, as the differentiation
	 * index is no longer used.
	 * 
	 * @param folder:
	 *            of files (easch file conatins the counts and genes for a
	 *            single clone)
	 * @param g:
	 *            GenesList of genes to be used to calculate the differentiation
	 *            index
	 * 
	 * @return ArrayList of differentiation indices (one per clone)
	 * @throws IOException
	 *             caused by FileInputStream
	 * 
	 * @deprecated
	 */
	public static ArrayList<Double> getDiffIdx(GenesList g, File folder) throws IOException {
		FileParser biofile = new FileParser();
		ArrayList<GenesList> List2D = new ArrayList<GenesList>();
		ArrayList<Double> toReturn = new ArrayList<Double>();
		for (File f : folder.listFiles(Config.FileFilter)) {
			List2D.add(biofile.toList(f, "double"));
		}

		for (int i = 0; i < List2D.size(); i++) {
			double temp = 0;
			for (int j = 0; j < List2D.get(i).size(); j++) {
				if (g.contains(List2D.get(i).get(j))) {
					temp += List2D.get(i).get(j).getgenCount();
				}
			}
			toReturn.add(temp);
		}
		return toReturn;
	}

	/**
	 * This method gets all significant genes (genes having a fold change
	 * greater than a specified threshold) from a results file created by
	 * DESeq2. It returns the genes as a GenesList object. This method is
	 * currently not used, as this functionality was implemented within the R
	 * Script.
	 * 
	 * @param sigGenes:
	 *            File to be parsed
	 * @return GenesList of significant genes
	 * @throws IOException
	 *             caused by FileInputStream
	 * 
	 * @deprecated
	 */
	public static GenesList getSigGenes(File sigGenes) throws IOException {
		FileInputStream fileIn = new FileInputStream(sigGenes);
		Scanner in = new Scanner(fileIn);
		GenesList toReturn = new GenesList();
		while (in.hasNext()) {
			toReturn.add(new Gene(in.nextLine()));
		}
		in.close();
		return toReturn;
	}

	private static void dataPreProcessing() throws IOException {
		if (dataMatrix.isFile()) {
			if (dataMatrix.getName().endsWith(".csv")) {
				deMultiplexedMatrix = fp.MatrixDemultiplexer(dataMatrix);
			} else if (dataMatrix.getName().endsWith(".tsv")) {
				deMultiplexedMatrix = fp.MatrixDemultiplexer(TSVtoCSV.TSVtoCSVcon(dataMatrix));
			}
		} else {
			deMultiplexedMatrix = new ArrayList<GenesList>();
			for (File f : dataMatrix.listFiles(Config.FileFilter)) {
				if (f.getName().endsWith(".csv")) {
					deMultiplexedMatrix.add(fp.toList(f, null));
				} else if (f.getName().endsWith(".tsv")) {
					deMultiplexedMatrix.add(fp.toList(TSVtoCSV.TSVtoCSVcon(f), "int"));
				}
			}
		}
	}

	/**
	 * This method checks the metadata files, converts them to csvs if
	 * necessary.
	 * 
	 * @throws IOException
	 *             caused by TSVtoCSV
	 */
	private static void metaPreProcessing() throws IOException {
		if (metafolder.isFile()) {
			if (metafolder.getName().endsWith(".tsv") || metafolder.getName().endsWith(".txt")) {
				File temp = TSVtoCSV.TSVtoCSVcon(metafolder);
				metafolder = temp;
			}

		} else {
			for (File f : metafolder.listFiles(Config.FileFilter)) {
				if (f.getName().endsWith(".tsv") || f.getName().endsWith(".txt")) {
					File temp = TSVtoCSV.TSVtoCSVcon(f);
					f = temp;
				}
			}
		}
	}

	/**
	 * This method checks the data file/folder and determines if it needs to be
	 * demultiplexed. If the data file has every trial in the same order that
	 * every metadata file does, then it is not demultiplexed and rearranged to
	 * accommodate each individual metadata file. This is done to avoid passing
	 * extraneous data to DESeq2, which optimizes its running time.
	 * 
	 * @throws IOException
	 *             caused by MetaDataParser
	 * 
	 * @return true if pre-processing is necessary, false if not
	 */
	private static boolean preProcessingCheck() throws IOException {
		boolean toReturn = false;
		if (dataMatrix.isFile()) {
			String fileTitle = fp.getTitle(dataMatrix).substring(fp.getTitle(dataMatrix).indexOf(",") + 1,
					fp.getTitle(dataMatrix).length());
			if (metafolder.isDirectory()) {
				// compares rows of metadata file with the header of the data
				// matrix to check equality for each metadata file

				for (File f : metafolder.listFiles(Config.FileFilter)) {
					String metaTrials = MetaDataParser.parse(f).get(0).toString();
					metaTrials = metaTrials.replace("[", "");
					metaTrials = metaTrials.replace("]", "");
					metaTrials = metaTrials.replace(" ", "");
					if (!fileTitle.equals(metaTrials)) {
						toReturn = true;
					}
				}
			} else {
				// does the same as the loop above, just for a single metadata
				// file
				String metaTrials = MetaDataParser.parse(metafolder).get(0).toString();
				metaTrials = metaTrials.replace("[", "");
				metaTrials = metaTrials.replace("]", "");
				if (!fileTitle.equals(metaTrials)) {
					toReturn = true;
				}

			}
		}
		return toReturn;
	}

	/**
	 * This method combines the data files (or data matrix) to the combination
	 * of each metadata file.
	 * 
	 * @throws IOException
	 *             caused by MetaDataParser
	 * 
	 */
	private static void multiplexMatrices() throws IOException {
		dataPreProcessing();
		metaPreProcessing();
		// stores all of the String[]s that have the matrix forms
		String[][] matrixForms = new String[metafolder.listFiles(Config.FileFilter).length][];
		for (int i = 0; i < matrixForms.length; i++) {
			matrixForms[i] = MetaDataParser.parse(metafolder.listFiles(Config.FileFilter)[i]).get(0).toArray(
					new String[MetaDataParser.parse(metafolder.listFiles(Config.FileFilter)[i]).get(0).size()]);
		}
		// makes each matrix
		for (int i = 0; i < matrixForms.length; i++) {
			MatrixMaker(matrixForms[i], deMultiplexedMatrix, metafolder.listFiles(Config.FileFilter)[i], matrixDir);
		}
	}

	/**
	 * This method calculates the run time of the program and displays it the
	 * the HH:MM:SS.millisec format.
	 * 
	 * @param startTime:
	 *            stating time of the program, taken as the time at the start of
	 *            the program
	 * 
	 * @return String HH:MM:SS.millisec format String
	 * 
	 */
	private static String showRunTime(long startTime) {
		long endTime = System.currentTimeMillis();
		int millisec = Math.round(((endTime - startTime)));
		int hours = millisec / 3600000;
		int minutes = (millisec / 60000) - hours * 60;
		int seconds = (millisec / 1000) - minutes * 60 - hours * 3600;
		int milliseconds = millisec - seconds * 1000 - minutes * 60000 - hours * 3600000;
		return "Total Run Time: " + String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":"
				+ String.format("%02d", seconds) + "." + String.format("%03d", milliseconds);

	}

	/**
	 * This method calculates the total number of elements in a 2D ArrayList.
	 * Example: if the ArrayList contains 3 other lists, each with 4 elements,
	 * this method return 12.
	 * 
	 * @param List2D:
	 *            List to get all elements from
	 * 
	 * @return int number of elements
	 * 
	 */
	public static int getSize(ArrayList<ArrayList<String>> List2D) {
		int toReturn = 0;
		for (int i = 0; i < List2D.size(); i++) {
			toReturn += List2D.get(i).size();
		}
		return toReturn;
	}

}
