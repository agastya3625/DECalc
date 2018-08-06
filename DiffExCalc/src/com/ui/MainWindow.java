package com.ui;

import java.awt.Desktop;
import java.io.*;
import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import com.backEnd.Config;
import com.backEnd.DiffExCalc;
import com.backEnd.FileFormatException;
import com.backEnd.MetaDataParser;

/**
 * This class is used to run the GUI for calculating differential expression.
 * (DiffExCalc.java)
 * 
 * @version 1.0
 * @author Agastya Sharma July 14th, 2017
 */
public class MainWindow {
	private Shell shell; // main display shell
	public static ProgressBar progressBar;
	private Color WHITE; // standard white color of text
	private String[] cmds = new String[3]; // stores command line args for
											// DiffExCalc.java
	private String TPM = new String();
	private String name = new String();
	private ArrayList<ArrayList<String>> comps; // list of comparisons to
												// perform
	private ArrayList<ArrayList<String>> compars;
	private ArrayList<ButtonList> CheckBoxes;
	private Text data; // data file Text field
	private Text meta; // metadata folder text field
	private Text res; // results folder text field
	private String datadelim;
	private String metadelim;
	private Display display; // display of the app
	private ArrayList<File> filtered;
	private String resname;
	// private File outDir;
	private static Label statusLabel; // shows current run status
	private static Button runButton, backButton; // "Run" and "Back" buttons
	private static File outDir;

	/**
	 * This constructor makes the main window and starts the UI generation
	 * process.
	 * 
	 * @param display:
	 *            Display to use to make the UI window
	 * 
	 */
	public MainWindow(Display display) {
		this.display = display;
		WHITE = new Color(display, 255, 255, 255);
		new Color(display, 0, 0, 0);
		makeParentShell(display);
	}

	/**
	 * This method is the first method called by the constructor, and it makes
	 * the main parent shell
	 * 
	 * @param Parent
	 *            display that the shell should be created within
	 */
	private void makeParentShell(Display display) {
		shell = new Shell(display, SWT.SHELL_TRIM/* Config.SHELL_STYLE */);
		shell.setLayout(new GridLayout(1, false));
		makeUI();// makes the rest of the UI
		// makes the and sets the background image of the main window
		shell.setBackgroundImage(new Image(display, Config.BKGD));
		shell.setText("DiffExCalc");
		Rectangle bounds = display.getPrimaryMonitor().getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);
		// shell.pack();
		shell.open();
		shell.setSize(800, 350);
		shell.setImage(new Image(display, Config.ICON));
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * This method makes the rest of the UI. It shows the fileBrowser selections
	 * and associated help messages.
	 * 
	 */
	private void makeUI() {
		makeMenu();
		TabFolder tabFolder = new TabFolder(shell, SWT.BORDER);
		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
		tabItem.setText("Run DESeq2");
		TabItem tabItem2 = new TabItem(tabFolder, SWT.NULL);
		tabItem2.setText("Show Plotter");
		Group DESeq = new Group(tabFolder, SWT.NONE);
		tabItem.setControl(DESeq);
		Group Plotter = new Group(tabFolder, SWT.NONE);
		tabItem2.setControl(Plotter);
		/// MAKE FILE BROWSERS, DELIMITERS, AND BUTTONS
		// listeners for the browse buttons
		ModifyListener datalistener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setChangedData((Text) e.widget, Config.DATA_POS);
			}
		};

		ModifyListener metalistener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setChangedData((Text) e.widget, Config.METADATA_POS);
			}
		};
		ModifyListener reslistener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setChangedData((Text) e.widget, Config.RESULTS_POS);
			}
		};

		// DATA ROW
		Label label = new Label(DESeq, SWT.NULL);
		label.setText("Data File/Folder: ");
		label.setForeground(WHITE);
		label.setBounds(10, 10, 125, Config.textHt);
		data = new Text(DESeq, SWT.SINGLE);
		data.addModifyListener(datalistener);
		data.setBounds(150, 10, Config.textWt, Config.textHt);
		FileBrowsers fbb = new FileBrowsers(data);
		Button dataFile = fbb.FileBrowseButton(DESeq);
		dataFile.setBounds(165 + Config.textWt, 10, Config.BUTTON_WIDTH, Config.textHt);
		Button dataFolder = fbb.diags(DESeq);
		dataFolder.setBounds(180 + Config.textWt + Config.BUTTON_WIDTH, 10, Config.BUTTON_WIDTH, Config.textHt);
		new HelpButton(Config.dataHelpMessage, Config.dataHelpHeader).Help(DESeq).setBounds(Config.helpButtonX, 10, 50,
				Config.textHt);

		// METADATA ROW
		label = new Label(DESeq, SWT.NULL);
		label.setForeground(WHITE);
		label.setText("Metadata File/Folder:");
		label.setBounds(10, 50, 125, Config.textHt);
		meta = new Text(DESeq, SWT.SINGLE);
		meta.addModifyListener(metalistener);
		meta.setBounds(150, 50, Config.textWt, Config.textHt);
		FileBrowsers fbb2 = new FileBrowsers(meta);
		Button metaFile = fbb2.FileBrowseButton(DESeq);
		metaFile.setBounds(165 + Config.textWt, 50, Config.BUTTON_WIDTH, Config.textHt);
		Button metaFolder = fbb2.diags(DESeq);
		metaFolder.setBounds(180 + Config.textWt + Config.BUTTON_WIDTH, 50, Config.BUTTON_WIDTH, Config.textHt);
		new HelpButton(Config.metadataHelpMessage, Config.metadataHelpHeader).Help(DESeq).setBounds(Config.helpButtonX,
				50, 50, Config.textHt);

		// RESULTS ROW
		label = new Label(DESeq, SWT.NULL);
		label.setForeground(WHITE);
		label.setText("Results Folder: ");
		label.setBounds(10, 90, 125, Config.textHt);
		res = new Text(DESeq, SWT.SINGLE);
		res.addModifyListener(reslistener);
		res.setBounds(150, 90, Config.textWt, Config.textHt);
		FileBrowsers fbb4 = new FileBrowsers(res);
		Button resBrowse = fbb4.diags(DESeq);
		resBrowse.setBounds(165 + Config.textWt, 90, Config.BUTTON_WIDTH, Config.textHt);
		new HelpButton(Config.resultsHelpMessage, Config.resultsHelpHeader).Help(DESeq)
				.setBounds(180 + Config.textWt + Config.BUTTON_WIDTH, 90, 50, Config.textHt);

		// NEXT BUTTON
		Button msgBtn = new Button(DESeq, SWT.PUSH);
		msgBtn.setText("Next ->");
		msgBtn.addListener(SWT.Selection, event -> findComps());
		msgBtn.setBounds(645, 250, Config.BUTTON_WIDTH, Config.textHt);

		// MAKE PLOTTER START SCREEN
		Group g = new Group(tabFolder, SWT.NONE);
		tabItem2.setControl(g);
		showPlot(g);

	}

	/**
	 * This method uses the MetaDataParser class to get the possible comparisons
	 * that can be run within a metadata file.
	 */
	private void findComps() {
		disposeChildren(shell);
		boolean multErrs = false;
		String message = null;
		MessageBox dia = new MessageBox(shell, SWT.ICON_ERROR);
		dia.setText("Error");
		// checks for major errors within the files and folders uploaded by the
		// user to make sure they can be parsed properly. Errors checked: no
		// file or folder can be null. Then, it makes sure
		// that the data file is a file, and that the results and metadata
		// folders are folders. It creates error popups for the user with the
		// error message if any issues are found.
		for (int i = 0; i < cmds.length - 1; i++) {
			try {
				dataErrCheck(new File(cmds[i]));
			} catch (FileFormatException e) {
				if (message == null) {
					message = e.getMessage();
				} else {
					multErrs = true;
					message = message.concat("\n\n" + e.getMessage());
				}

			} catch (NullPointerException e) {
				String temp = null;
				if (i == Config.DATA_POS) {
					temp = "data file/folder could not be found";
				} else if (i == Config.METADATA_POS) {
					temp = "metadata file/folder could not be found";
				}
				if (message == null) {
					message = temp;
				} else {
					multErrs = true;
					message = message.concat("\n\n" + temp);
				}

			}
		}
		if (message != null) {
			if (multErrs) {
				message = "Multiple Errors:\n\n" + message;
			}
			display.beep();
			dia.setMessage(message);
			dia.open();
			shell.dispose();
			makeParentShell(display);
		}
		File folder = new File(cmds[1]);
		comps = new ArrayList<ArrayList<String>>();
		try {
			if (folder.isDirectory()) {
				for (File f : folder.listFiles(Config.FileFilter)) {
					// eliminates any non-metadata files that cannot be read by
					// the
					// program
					ArrayList<ArrayList<String>> temps = MetaDataParser.parse(f);
					temps.remove(0);
					// adds possible comparisons to a master list of comparisons
					// to display
					comps.add(MetaDataParser.generatePerm(MetaDataParser.getPerms(temps)));
				}
			} else {
				ArrayList<ArrayList<String>> temps = MetaDataParser.parse(folder);
				temps.remove(0);
				// adds possible comparisons to a master list of comparisons
				// to display
				comps.add(MetaDataParser.generatePerm(MetaDataParser.getPerms(temps)));

			}
		} catch (Exception e) {
			display.beep();
			dia.setMessage("An error occurred.");
			dia.open();
		}
		showCompCheckBoxes();
	}

	/**
	 * This method displays the check boxes of the comparisons that can be
	 * selected by the user. This only displays the boxes, and does not generate
	 * the comparisons.
	 * 
	 * @param ArrayList<ArrayList<String>>
	 *            that holds all of the comparisons
	 */
	private void showCompCheckBoxes() {
		CheckBoxes = new ArrayList<ButtonList>();
		for (int i = 0; i < comps.size(); i++) {
			CheckBoxes.add(new ButtonList(comps.get(i).size()));
		}
		shell.setLayout(new GridLayout(2, false));
		Label label = new Label(shell, SWT.CENTER);
		label.setText("Select Comparisons to run: ");
		label.setForeground(WHITE);
		label.setFont(new Font(label.getDisplay(), new FontData("Arial", 16, SWT.BOLD)));
		GridData g = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		g.horizontalSpan = 2;
		label.setLayoutData(g);
		GridData secondData = new GridData(SWT.FILL, SWT.FILL, false, false);
		secondData.minimumHeight = 10;
		secondData.horizontalSpan = 1;
		Group first = new Group(shell, SWT.NONE);
		first.setLayout(new GridLayout(2, false));
		GridData firstData = new GridData(SWT.FILL, SWT.FILL, true, false);
		firstData.heightHint = 250;
		firstData.horizontalSpan = 2;
		first.setLayoutData(firstData);
		ScrolledComposite firstScroll = new ScrolledComposite(first, SWT.V_SCROLL);
		firstScroll.setMinHeight(15 * comps.size() / 6 + 100);
		firstScroll.setLayoutData(firstData);
		Composite firstContent = new Composite(firstScroll, SWT.NONE);
		// firstContent.setLayout(new GridLayout(6, false));
		// firstContent.setLayoutData(new GridData(GridData.FILL_BOTH));
		backButton = new Button(shell, SWT.PUSH);
		backButton.setText("<- Back");
		backButton.addListener(SWT.Selection, event -> goBack());
		backButton.setLayoutData(Config.GRID_DATA_FILL);
		runButton = new Button(shell, SWT.PUSH);
		runButton.setText("Run DESeq2 ->");
		// runs DEseq2
		runButton.addListener(SWT.Selection, event -> {
			try {
				label.dispose();
				runDESeq2(first, firstScroll);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		runButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		// checkboxes are shown in columns with 30 comparisons in each check
		// box.
		int x = 10;
		int y = 10;
		int count = 0;
		for (int i = 0; i < comps.size(); i++) {
			// i2 stores the value of i for the purpose of selecting all
			// comparisons in an entire file
			final int i2 = i;
			// this part of the loop sets a checkbox to allow the user to select
			// all comparisons within a single file.
			Button file = new Button(firstContent, SWT.CHECK);
			file.setBounds(x, y, 150, Config.textHt);
			count++;
			y += Config.textHt + 10;
			if (count > Config.getSize(comps) / 6) {
				x += 10 + 150;
				count = 0;
				y = 10;
			}
			file.setText("Metadata file " + (i + 1));
			file.setForeground(WHITE);
			file.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					Button btn = (Button) event.getSource();
					for (int k = 0; k < CheckBoxes.get(i2).size(); k++) {
						CheckBoxes.get(i2).get(k).setSelection(btn.getSelection());
					}
				}
			});
			CheckBoxes.get(i).setFileButton(file);
			// this loop sets the checkboxes for the individual comparisons
			// within a file
			for (int j = 0; j < comps.get(i).size(); j++) {
				Button b = new Button(firstContent, SWT.CHECK);
				b.setText(comps.get(i).get(j));
				b.setForeground(WHITE);
				b.setBounds(x, y, 150, Config.textHt);
				count++;
				y += Config.textHt + 10;
				if (count > Config.getSize(comps) / 6) {
					x += 10 + 150;
					count = 0;
					y = 10;
				}
				CheckBoxes.get(i).add(b);
			}
		}
		firstScroll.setContent(firstContent);
		firstScroll.setExpandHorizontal(true);
		firstScroll.setExpandVertical(true);
		firstScroll.setMinSize(new Point(Config.SHELL_WIDTH - 50, (CheckBoxes.size() / 6) * Config.textHt * 2 + 500));
		shell.pack();
		shell.setSize(800, 350);

	}

	/**
	 * This runs the DESeq2 R script. It creates the run information file,
	 * displays necessary information, checks if comparisons have been selected,
	 * then passes the args to DiffExCalc, and displays the results file within
	 * a text box in the UI to the user upon completion.
	 * 
	 * @param firstContent
	 * @param firstScroll
	 * 
	 * @throws IOException
	 *             if the file cannot be found/written to. Rarely throws
	 *             exception, as the file is auto-generated and is not dependent
	 *             on user input whatsoever.
	 */
	private void runDESeq2(Group firstContent, ScrolledComposite firstScroll) throws IOException {
		try {
			// firstContent.dispose();
			if (Config.OS_TYPE.toLowerCase().contains("win")) {
				statusLabel.setForeground(WHITE);
			}
			runButton.dispose();
			backButton.dispose();
			// File info = new File(cmds[2] + "/DESeq_Run_Log.txt");
			dataErrCheck(new File(cmds[0]));
			dataErrCheck(new File(cmds[1]));
			compars = setCompars(CheckBoxes);
			// shows the data/metadata/results options selected, as well as the
			// comparisons selected and the outputs of the R script.
			MessageBox dia = new MessageBox(shell, Config.INFO_STYLE);
			// message box shows up if no comparisons are selected
			if (!atLeastOne()) {
				display.beep();
				dia.setMessage("Please select at least one comparison");
				dia.open();
				findComps();
			} else {
				firstScroll.dispose();
				firstContent.dispose();
				shell.setLayout(null);
				Thread thread;
				Label l = new Label(shell, SWT.NONE);
				l.setText("Run Progress");
				l.setFont(new Font(l.getDisplay(), new FontData("Arial", 16, SWT.BOLD)));
				l.setForeground(WHITE);
				l.setBounds(25, 25, Config.textWt, Config.textHt);
				progressBar = new ProgressBar(shell, SWT.HORIZONTAL);
				progressBar.setBounds(Config.PROGRESS_BAR);
				progressBar.setBackground(WHITE);
				statusLabel = new Label(shell, SWT.NONE);
				statusLabel.setForeground(WHITE);
				statusLabel.setBounds(Config.PROGRESS_LABEL);
				Text text = new Text(shell, SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
				text.setBounds(25, 100, 750, 150);
				Button startNew = new Button(shell, SWT.PUSH);
				startNew.setText("New Run");
				startNew.setBounds(Config.LEFT_CORNER);
				startNew.addListener(SWT.Selection, event -> startNew());
				startNew.setEnabled(false);
				Button stop = new Button(shell, SWT.PUSH);
				stop.setText("Show Plots");
				stop.setEnabled(false);
				stop.setBounds(Config.RIGHT_CORNER);
				stop.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						disposeChildren(shell);
						showPlot(shell);
					}
				});
				dia.setMessage("Run Started. Projected run time: " + ((Config.getSize(compars)) + 4) + " minutes.");
				dia.open();
				thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							outDir = new File(cmds[Config.RESULTS_POS]);
							DiffExCalc.main(cmds, compars, progressBar, statusLabel, text, datadelim, metadelim);
							display.asyncExec(new Runnable() {
								@Override
								public void run() {
									startNew.setEnabled(true);
									stop.setEnabled(true);
								}
							});
						} catch (Exception e) {
							e.printStackTrace(System.err);
						}
					}
				});
				thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						System.out.println(t.getName() + ": ");
						e.printStackTrace();
					}
				});
				thread.start();

			}
		} catch (FileFormatException e) {
			display.beep();
			MessageBox dia = new MessageBox(shell, SWT.ICON_ERROR);
			dia.setMessage(e.getMessage());
			dia.open();
		}

	}

	private void showPlot(Composite c) {
		if (c instanceof Shell) {
			disposeChildren((Shell) c);
		}
		ModifyListener datalistener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setChangedData((Text) e.widget, 6);
			}
		};
		ModifyListener reslistener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setChangedData((Text) e.widget, 7);
			}
		};
		Label label = new Label(c, SWT.NULL);
		label.setText("TPM File: ");
		label.setForeground(WHITE);
		label.setBounds(10, 10, 100, Config.textHt);
		Text data = new Text(c, SWT.SINGLE);
		data.setBounds(50 + Config.textWt - 175, 10, Config.textWt, Config.textHt);
		data.addModifyListener(datalistener);
		FileBrowsers fbb = new FileBrowsers(data);
		Button dataFile = fbb.FileBrowseButton(c);
		dataFile.setBounds(2 * Config.textWt - 100, 10, Config.BUTTON_WIDTH, Config.textHt);
		if (c instanceof Shell) {
			Label l = new Label(shell, SWT.NULL);
			l.setText("Comparison: ");
			l.setForeground(WHITE);
			l.setBounds(25, 50, Config.textWt, Config.textHt);
			Table table = new Table(c, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
			TableItem all = new TableItem(table, SWT.NONE);
			all.setText("Select All");
			String[] items = getFileNames(new File(cmds[Config.RESULTS_POS] + "/DESeq Results"));
			for (String s : items) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(s);
			}
			table.setSize(Config.textWt, Config.textHt);
			table.setBounds(50 + Config.textWt - 175, 50, Config.textWt, Config.textHt * 5);
			Button start = new Button(c, SWT.PUSH);
			start.setText("Show Plot");
			start.setBounds(Config.RIGHT_CORNER);
			start.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					if (all.getChecked()) {
						for (File f : outDir.listFiles(Config.FileFilter)) {
							runPlotter(outDir.getAbsolutePath() + "/" + f.getName(), 1);
						}
					} else {
						TableItem[] items = table.getItems();
						int all = 0;
						ArrayList<TableItem> allTrue = new ArrayList<TableItem>();
						for (TableItem t : items) {
							if (t.getChecked()) {
								allTrue.add(t);
							}
						}
						if (allTrue.size() > 5) {
							all = 1;
						}
						for (TableItem t : allTrue) {
							name = outDir.getAbsolutePath() + "/" + t.getText() + "_RES.csv";
							runPlotter(name, all);
						}
					}

				}
			});
		} else {
			Text res = new Text(c, SWT.SINGLE);
			res.setBounds(50 + Config.textWt - 175, 50, Config.textWt, Config.textHt);
			res.addModifyListener(reslistener);
			label = new Label(c, SWT.NULL);
			label.setText("Results File: ");
			label.setForeground(WHITE);
			label.setBounds(10, 50, 100, Config.textHt);
			FileBrowsers fbb2 = new FileBrowsers(res);
			Button resFile = fbb2.FileBrowseButton(c);
			resFile.setBounds(2 * Config.textWt - 100, 50, Config.BUTTON_WIDTH, Config.textHt);
			Button start = new Button(c, SWT.PUSH);
			start.setText("Show Plot");
			start.setBounds(645, 250, Config.BUTTON_WIDTH, Config.textHt);
			start.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					runPlotter(resname, 0);
				}

			});
		}

	}

	private String[] getFileNames(File file) {
		filtered = new ArrayList<File>(Arrays.asList(file.listFiles(Config.FileFilter)));
		String[] toReturn = new String[filtered.size()];
		for (int i = 0; i < toReturn.length; i++) {
			String str = filtered.get(i).getName();
			toReturn[i] = str.substring(0, str.indexOf("_RES.csv"));
		}
		return toReturn;
	}

	/**
	 * This method sets the command line args array (cmds) that is passed to the
	 * DiffExCalc.java class to the names of the files selected within the file
	 * browsers.
	 * 
	 * @param Text
	 *            object that contains the name of the file/folder to be added
	 *            to args
	 * 
	 * @param int
	 *            position of the file type within the command line args
	 *            required by DiffExCalc (format of this can be found in
	 *            DiffExCalc.java comments.
	 */
	private void setChangedData(Text text, int i) {
		if (text == null) {
			return;
		} else if (i == 7) {
			resname = text.getText();
		} else if (i == 6) {
			TPM = text.getText();
			return;
		} else if (i == 5) {
			if (text.getText().toLowerCase().endsWith(".r")) {
				Config.RFile_Name = text.getText();
			} else {
				MessageBox dia = new MessageBox(shell, SWT.ERROR);
				dia.setText("Please upload an R file.");
				dia.open();
			}
		} else {
			cmds[i] = text.getText();
		}

	}

	/**
	 * @param text
	 * @return
	 */
	private void runPlotter(String name, int all) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Process pb = new ProcessBuilder("/usr/local/bin/Rscript", Config.PLOTTER_NAME, TPM, name,
							Integer.toString(all)).start();
					BufferedReader read = new BufferedReader(new InputStreamReader(pb.getInputStream()));
					BufferedReader out = new BufferedReader(new InputStreamReader(pb.getErrorStream()));
					String output = read.readLine();
					String error = out.readLine();
					while (output != null || error != null) {
						if (error != null) {
							System.out.println(error);
						}
						output = read.readLine();
						error = out.readLine();
					}
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		});
		thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				System.out.println(t.getName() + ": ");
				e.printStackTrace();
			}
		});
		thread.start();
	}

	/**
	 * This method returns the screen from the comparisons display to the file
	 * selection display. It first eliminates all the children of the shell,
	 * then re-makes the first UI.
	 * 
	 */
	private void goBack() {
		shell.dispose();
		makeParentShell(display);
		for (int i = 0; i < cmds.length; i++) {
			cmds[i] = null;
		}
		shell.setBackgroundImage(new Image(display, Config.BKGD));
	}

	/**
	 * This method simply disposes all elements within a shell so that the shell
	 * can display new elements. It is called when the back button is pressed or
	 * when the comaprisons need to be displayed.
	 * 
	 * @param Shell
	 *            whose children to dispose
	 * 
	 */
	private void disposeChildren(Shell s) {
		for (Control child : s.getChildren()) {
			child.dispose();
		}
	}

	/**
	 * This method checks to ensure the files uploaded are in the correct
	 * format.
	 * 
	 * @param f:
	 *            file to be checked.
	 * 
	 * @throws FileFormatException
	 *             if the file is not in the correct format.
	 * 
	 */
	private void dataErrCheck(File f) throws FileFormatException {
		if (f.isFile()) {
			// eliminates unessential system-specific files
			if (f.getName().toLowerCase().equals(".ds_store")) {
				f.delete();
			}
			// checks if file is a .csv or .tsv
			if (!f.getName().endsWith(".csv") && !f.getName().endsWith(".tsv")) {
				throw new FileFormatException("File " + f.getName() + " is not in the correct format");
			}
			// if a file uploaded is not a file, then it's a diectory. This
			// performs roughly the same algorithm as above, but for every file
			// in a directory.
		} else {
			for (File f1 : f.listFiles(Config.FileFilter)) {
				if (!f1.getName().endsWith(".csv") && !f1.getName().endsWith(".tsv")) {
					throw new FileFormatException("File " + f1.getName() + " is not in the correct format");
				}
				if (f1.isDirectory()) {
					throw new FileFormatException("Folder " + f1.getName() + " cannot contain additional folders");
				}
			}
		}
	}

	/**
	 * This method makes the menu bar that displays the help tab for users.
	 * 
	 */
	private void makeMenu() {
		Menu menuBar, helpMenu;
		MenuItem helpMenuHeader, manual, sampleData, sampleMetadata, changeRFile, showMenu;
		menuBar = new Menu(shell, SWT.BAR);

		helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		helpMenuHeader.setText("&Help");

		helpMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenuHeader.setMenu(helpMenu);

		manual = new MenuItem(helpMenu, SWT.PUSH);
		manual.setText("&DESeq2 Manual");
		manual.addListener(SWT.Selection, event -> showHelp(Config.MANUAL, "PDF"));

		sampleData = new MenuItem(helpMenu, SWT.PUSH);
		sampleData.setText("&Sample Data File");
		sampleData.addListener(SWT.Selection, event -> showHelp(Config.SAMPLE_DATA_FILE, "CSV"));

		sampleMetadata = new MenuItem(helpMenu, SWT.PUSH);
		sampleMetadata.setText("&Sample metaData File");
		sampleMetadata.addListener(SWT.Selection, event -> showHelp(Config.SAMPLE_METADATA_FILE, "CSV"));

		changeRFile = new MenuItem(helpMenu, SWT.PUSH);
		changeRFile.setText("&Upload R File");
		changeRFile.addListener(SWT.Selection, event -> changeRFile());

		showMenu = new MenuItem(helpMenu, SWT.PUSH);
		showMenu.setText("&Show R File");
		showMenu.addListener(SWT.Selection, event -> showHelp(Config.RFile_Name, "R"));

		shell.setMenuBar(menuBar);
	}

	/**
	 * This method allows users to upload their own R file to add anything to
	 * the script.
	 * 
	 */
	private void changeRFile() {
		Shell s = new Shell(display, SWT.SHELL_TRIM | SWT.CENTER);
		s.setSize(435, 140);
		s.setText("Upload New R file");
		Label l = new Label(s, SWT.SINGLE);
		l.setText("Select a new R file to use: ");
		Text getFile = new Text(s, SWT.SINGLE);
		FileBrowsers fbb = new FileBrowsers(getFile);
		fbb.FileBrowseButton(s);
		s.open();
		Button OK = new Button(s, SWT.PUSH);
		OK.setText("Done");
		OK.addListener(SWT.Selection, event -> s.dispose());
		ModifyListener Rlistener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setChangedData((Text) e.widget, 5);
			}
		};
		getFile.addModifyListener(Rlistener);
		Button restore = new Button(s, SWT.PUSH);
		restore.setText("Restore Default File");
		restore.addListener(SWT.Selection, event -> Config.RFile_Name = "./DESEQ_CORVERA.R");
	}

	/**
	 * This method calls the system softwares for displaying the associated help
	 * .csv files and DESeq2 PDF.
	 * 
	 * @param myFile:
	 *            file to open (either PDF or .csv files)
	 * 
	 * @param fileType:
	 *            type of the file (either .csv or PDF)
	 * 
	 */
	private void showHelp(String myFile, String fileType) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().open(new File(myFile));
			} catch (IOException ex) {
				MessageBox dia = new MessageBox(shell, SWT.ERROR);
				dia.setMessage("No application was found that can open " + fileType + "s.");
			}
		}
	}

	/**
	 * This method starts a new run and resets the GUI to accept input for the
	 * new run.
	 * 
	 */
	private void startNew() {
		shell.dispose();
		makeParentShell(display);
	}

	/**
	 * This sets which comparisons to run based on the user selections. This
	 * ensures that the GUI is responsive to users selecting and and deselecting
	 * any given checkbox multiple times.
	 * 
	 * @param checkSelections:
	 *            List that holds all of the checkbox buttons.
	 * 
	 * @return ArrayList<ArrayList<String>> that mirrors the button list passed
	 *         in. It contains the comparisons and maintains the order they are
	 *         in in the metadata files.
	 * 
	 */
	private ArrayList<ArrayList<String>> setCompars(ArrayList<ButtonList> checkSelections) {
		// makes a complementray list of strings to hold the comparisons
		ArrayList<ArrayList<String>> toReturn = new ArrayList<ArrayList<String>>(checkSelections.size());
		for (int i = 0; i < checkSelections.size(); i++) {
			toReturn.add(new ArrayList<String>(checkSelections.get(i).size()));
		}
		for (int i = 0; i < checkSelections.size(); i++) {
			for (int j = 0; j < checkSelections.get(i).size(); j++) {
				// add the comparisons to run per metadata file if selected by
				// the user
				if (checkSelections.get(i).get(j).getSelection()) {
					toReturn.get(i).add(checkSelections.get(i).get(j).getText());
				}
			}
		}
		return toReturn;
	}

	/**
	 * This checks if there is at least one comparison chosen to run the DESeq2
	 * script with.
	 * 
	 * @return boolean true if at least one comparison is selected, false if
	 *         otherwise.
	 * 
	 */
	private boolean atLeastOne() {
		boolean toReturn = false;
		for (int i = 0; i < CheckBoxes.size(); i++) {
			if (CheckBoxes.get(i).OneSelected()) {
				toReturn = true;
			}
		}
		return toReturn;
	}
}