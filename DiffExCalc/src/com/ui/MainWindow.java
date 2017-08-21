package com.ui;

import java.awt.Desktop;
import java.io.*;
import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
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
	private ArrayList<ArrayList<String>> comps;
	private ArrayList<ButtonList> CheckBoxes;
	private Text data; // data file Text field
	private Text meta; // metadata folder text field
	private Text res; // results folder text field
	// list of comparisons to perform
	private Display display; // display of the app
	private static Label statusLabel; //shows current run status
	private static Button runButton, backButton; //"Run" and "Back" buttons 
	private GC gc; //allows for drawing on the GUI background

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
		shell = new Shell(display, Config.SHELL_STYLE);
		makeUI();// makes the rest of the UI
		// makes the and sets the background image of the main window
		shell.setBackgroundImage(new Image(display, Config.BKGD));
		shell.setText("DiffExCalc");
		shell.setSize(Config.SHELL_WIDTH, Config.SHELL_HEIGHT);
		Rectangle bounds = display.getPrimaryMonitor().getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);
		shell.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				Config.ResizeAll(shell.getBounds().width, shell.getBounds().height);
				for (Control c : shell.getChildren()) {
					c.redraw();
				}
			}
		});
		shell.open();
		shell.setImage(new Image(display, Config.ICON));
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
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
		Label label = new Label(shell, SWT.CENTER);
		label.setText("Welcome to DESeq2!");
		label.setForeground(WHITE);
		Point p = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int x = 683 - (p.x / 2);
		label.setBounds(x, 5, p.x + 5, p.y + 5);
		makeFileBrowsers(); // makes the interactive file browsers
		setButtons(); // makes all the buttons needed for the first screen
	}

	/**
	 * This method makes the file browsers that let the user browse their
	 * computer for the data file, metadata folder, and results folder
	 * 
	 */
	private void makeFileBrowsers() {
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
		// browse button labels
		Label label = new Label(shell, SWT.CENTER);
		label.setText("Data File/Folder: ");
		label.setForeground(WHITE);
		label.setBounds(40, 40, 125, Config.textHt);
		data = new Text(shell, SWT.SINGLE);
		data.setBounds(190, 40, Config.textWt, Config.textHt);
		data.addModifyListener(datalistener);
		label = new Label(shell, SWT.CENTER);
		label.setForeground(WHITE);
		label.setText("Metadata File/Folder:");
		label.setBounds(40, 80, 125, Config.textHt);
		meta = new Text(shell, SWT.SINGLE);
		meta.setBounds(190, 80, Config.textWt, Config.textHt);
		meta.addModifyListener(metalistener);
		label = new Label(shell, SWT.CENTER);
		label.setForeground(WHITE);
		label.setText("Results Folder: ");
		label.setBounds(40, 120, 125, Config.textHt);
		res = new Text(shell, SWT.SINGLE);
		res.setBounds(190, 120, Config.textWt, Config.textHt);
		res.addModifyListener(reslistener);
	}

	/**
	 * This method makes the "next" button that shows the user all comparisons,
	 * it shows the browse buttons, and the associated help buttons that assist
	 * the user by providing information about the files and folders they will
	 * upload
	 * 
	 */
	private void setButtons() {
		Button msgBtn = new Button(shell, SWT.PUSH);
		msgBtn.setText("Next ->");
		msgBtn.addListener(SWT.Selection, event -> findComps());
		msgBtn.setBounds(Config.RIGHT_CORNER);
		setBrowseButtons();
		setHelpButtons();
	}

	/**
	 * This method makes the "Browse" buttons for each folder/file the user
	 * uploads.
	 * 
	 */
	private void setBrowseButtons() {
		FileBrowsers fbb = new FileBrowsers(data);
		FileBrowsers fbb2 = new FileBrowsers(meta);
		Button dataFile = fbb.FileBrowseButton(shell);
		dataFile.setBounds(455, 40, Config.BUTTON_WIDTH, Config.textHt);
		Button dataFolder = fbb.diags(shell);
		dataFolder.setBounds(620, 40, Config.BUTTON_WIDTH, Config.textHt);
		Button metaFile = fbb2.FileBrowseButton(shell);
		Button metaFolder = fbb2.diags(shell);
		metaFile.setBounds(620, 80, Config.BUTTON_WIDTH, Config.textHt);
		metaFolder.setBounds(455, 80, Config.BUTTON_WIDTH, Config.textHt);
		FileBrowsers fbb4 = new FileBrowsers(res);
		Button resBrowse = fbb4.diags(shell);
		resBrowse.setBounds(455, 120, Config.BUTTON_WIDTH, Config.textHt);
	}

	/**
	 * This method makes the help buttons.
	 * 
	 */
	private void setHelpButtons() {
		HelpButton data = new HelpButton(Config.dataHelpMessage, Config.dataHelpHeader);
		Button hdata = data.Help(shell);
		hdata.setBounds(785, 40, 40, Config.textHt);
		HelpButton meta = new HelpButton(Config.metadataHelpMessage, Config.metadataHelpHeader);
		Button hmeta = meta.Help(shell);
		hmeta.setBounds(785, 80, 40, Config.textHt);
		HelpButton res = new HelpButton(Config.resultsHelpMessage, Config.resultsHelpHeader);
		Button hres = res.Help(shell);
		hres.setBounds(540, 120, 40, Config.textHt);
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
		RunDESeqButton();
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
		Label l = new Label(shell, SWT.NONE);
		l.setText("Run Progress");
		l.setFont(new Font(l.getDisplay(), new FontData("Arial", 16, SWT.BOLD)));
		if (!Config.OS_TYPE.toLowerCase().contains("wind")) {
			l.setBounds(Config.SHELL_WIDTH - 398, 20, 150, Config.textHt);
			shell.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					gc = e.gc;
					gc.setLineWidth(4);
					gc.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
					gc.fillRoundRectangle(Config.SHELL_WIDTH - 625, 15, 600, 80, 30, 30);
					gc.drawRoundRectangle(Config.SHELL_WIDTH - 625, 15, 600, 80, 30, 30);
				}
			});
		} else {
			l.setForeground(WHITE);
		}
		progressBar = new ProgressBar(shell, SWT.HORIZONTAL);
		progressBar.setBounds(Config.PROGRESS_BAR);
		statusLabel = new Label(shell, SWT.NONE);
		statusLabel.setBounds(Config.PROGRESS_LABEL);
		if (Config.OS_TYPE.toLowerCase().contains("win")) {
			statusLabel.setForeground(WHITE);
		}
		CheckBoxes = new ArrayList<ButtonList>();
		for (int i = 0; i < comps.size(); i++) {
			CheckBoxes.add(new ButtonList(comps.get(i).size()));
		}
		int idx = 0;
		int x = 25;
		int y = 50;
		int offset = 10;
		Label label = new Label(shell, SWT.CENTER);
		label.setText("Select Comparisons to run: ");
		label.setForeground(WHITE);
		label.setFont(new Font(label.getDisplay(), new FontData("Arial", 16, SWT.BOLD)));
		label.setBounds(25, 25, 225, Config.textHt);
		// checkboxes are shown in columns with 30 comparisons in each check
		// box.
		for (int i = 0; i < comps.size(); i++) {
			// i2 stores the value of i for the purpose of selecting all
			// comparisons in an entire file
			final int i2 = i;
			// this part of the loop sets a checkbox to allow the user to select
			// all comparisons within a single file.
			Button file = new Button(shell, SWT.CHECK);
			file.setText("metadata file " + (i + 1));
			if (idx < 30) {
				file.setBounds(x, y, 175, Config.textHt);
				idx++;
				y += 20;
			} else {
				x += 175;
				idx = 0;
				y = 50;
			}
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
				Button b = new Button(shell, SWT.CHECK);
				b.setText(comps.get(i).get(j));
				if (idx < 30) {
					b.setBounds(x + offset, y, 175, Config.textHt);
					idx++;
					y += 20;
				} else {
					x += 175;
					idx = 0;
					y = 50;
				}
				b.setForeground(WHITE);
				CheckBoxes.get(i).add(b);
			}
		}

	}

	/**
	 * This makes the run button and the back button shown when the comparisons
	 * are shown.
	 * 
	 */
	private void RunDESeqButton() {
		runButton = new Button(shell, SWT.PUSH);
		runButton.setText("Run DESeq2 ->");
		// runs DEseq2
		runButton.addListener(SWT.Selection, event -> {
			try {
				runDESeq2();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		runButton.setBounds(Config.RIGHT_CORNER);
		backButton = new Button(shell, SWT.PUSH);
		backButton.setText("<- Back");
		backButton.addListener(SWT.Selection, event -> goBack());
		backButton.setBounds(Config.LEFT_CORNER);
	}

	/**
	 * This runs the DESeq2 R script. It creates the run information file,
	 * displays necessary information, checks if comparisons have been selected,
	 * then passes the args to DiffExCalc, and displays the results file within
	 * a text box in the UI to the user upon completion.
	 * 
	 * @throws IOException
	 *             if the file cannot be found/written to. Rarely throws
	 *             exception, as the file is auto-generated and is not dependent
	 *             on user input whatsoever.
	 */
	private void runDESeq2() throws IOException {
		try {
			for (ButtonList b : CheckBoxes) {
				b.getFileButton().setEnabled(false);
				for (Button button : b) {
					button.setEnabled(false);
				}
			}
			runButton.dispose();
			backButton.dispose();
			File info = new File(cmds[2] + "/DESeq_Run_Log.txt");
			dataErrCheck(new File(cmds[0]));
			dataErrCheck(new File(cmds[1]));
			ArrayList<ArrayList<String>> compars = setCompars(CheckBoxes);

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
				Text text = new Text(shell, SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
				text.setBounds(Config.SHELL_WIDTH - 618, 115, 600, 500);
				dia.setMessage("Run Started. Run information is stored in:\n" + info.getPath()
						+ ". \n\nProjected run time: " + ((DiffExCalc.getSize(compars)) + 4) + " minutes.");
				dia.open();
				Button startNew = new Button(shell, SWT.PUSH);
				startNew.setBounds(Config.RIGHT_CORNER);
				startNew.setText("New Run");
				startNew.addListener(SWT.Selection, event -> startNew());
				startNew.setEnabled(false);

				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							DiffExCalc.main(cmds, compars, progressBar, info, statusLabel, text);
							display.asyncExec(new Runnable() {
								@Override
								public void run() {
									startNew.setEnabled(true);
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
				Button stop = new Button(shell, SWT.PUSH);
				stop.setText("Stop Run");
				stop.setBounds(Config.RIGHT_CORNER.x - Config.RIGHT_CORNER.width - 25, Config.RIGHT_CORNER.y,
						Config.RIGHT_CORNER.width, Config.RIGHT_CORNER.height);
				stop.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						thread.interrupt();
						disposeChildren(shell);
						findComps();
						System.out.println("Thread stopped.");
						// info.delete();
					}
				});
			}
		} catch (FileFormatException e) {
			display.beep();
			MessageBox dia = new MessageBox(shell, SWT.ICON_ERROR);
			dia.setMessage(e.getMessage());
			dia.open();
		}

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
		}
		if (i == 5) {
			if (text.getText().toLowerCase().endsWith(".r")) {
				Config.RFile_Name = text.getText();
				// System.out.println(Config.RFile_Name);
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
	 * This method returns the screen from the comparisons display to the file
	 * selection display. It first eliminates all the children of the shell,
	 * then re-makes the first UI.
	 * 
	 */
	private void goBack() {
		disposeChildren(shell);
		makeUI();
		for (int i = 0; i < cmds.length; i++) {
			cmds[i] = null;
		}
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
		l.setBounds(10, 10, Config.BUTTON_WIDTH, 20);
		Text getFile = new Text(s, SWT.SINGLE);
		getFile.setBounds(10, 40, Config.textWt, Config.textHt);
		FileBrowsers fbb = new FileBrowsers(getFile);
		Button dataFile = fbb.FileBrowseButton(s);
		dataFile.setBounds(275, 40, Config.BUTTON_WIDTH, Config.textHt);
		s.open();
		Button OK = new Button(s, SWT.PUSH);
		OK.setText("Done");
		OK.setBounds(325, 80, 100, 20);
		OK.addListener(SWT.Selection, event -> s.dispose());
		ModifyListener Rlistener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setChangedData((Text) e.widget, 5);
			}
		};
		getFile.addModifyListener(Rlistener);
		Button restore = new Button(s, SWT.PUSH);
		restore.setText("Restore Default File");
		restore.setBounds(10, 80, Config.BUTTON_WIDTH, 20);
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