package com.ui;

import java.awt.Desktop;
import java.io.*;
import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
	private ArrayList<ArrayList<String>> comps; // list of comparisons to
												// perform
	private ArrayList<ButtonList> CheckBoxes;
	private Text data; // data file Text field
	private Text meta; // metadata folder text field
	private Text res; // results folder text field
	private String datadelim;
	private String metadelim;
	private Display display; // display of the app
	private static Label statusLabel; // shows current run status
	private static Button runButton, backButton; // "Run" and "Back" buttons

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
		shell = new Shell(display/* , Config.SHELL_STYLE */);
		shell.setLayout(new GridLayout(7, false));
		makeUI();// makes the rest of the UI
		// makes the and sets the background image of the main window
		shell.setBackgroundImage(new Image(display, Config.BKGD));
		shell.setText("DiffExCalc");
		Rectangle bounds = display.getPrimaryMonitor().getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);
		shell.pack();
		shell.open();
		shell.setSize(800, 300);
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
		Label label = new Label(shell, SWT.NULL);
		label.setText("Data File/Folder: ");
		label.setForeground(WHITE);
		data = new Text(shell, SWT.SINGLE);
		data.addModifyListener(datalistener);
		GridData gridData1 = new GridData(GridData.FILL_HORIZONTAL);
		gridData1.widthHint = Config.textWt;
		gridData1.heightHint = Config.textHt;
		data.setLayoutData(gridData1);
		FileBrowsers fbb = new FileBrowsers(data);
		Button dataFile = fbb.FileBrowseButton(shell);
		dataFile.setLayoutData(Config.GRID_DATA_FILL);
		Button dataFolder = fbb.diags(shell);
		dataFolder.setLayoutData(Config.GRID_DATA_FILL);
		Label label2 = new Label(shell, SWT.CENTER);
		label2.setText("File delimiter:");
		label2.setForeground(WHITE);
		Text t = new Text(shell, SWT.SINGLE);
		t.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				datadelim = t.getText();
			}
		});
		new HelpButton(Config.dataHelpMessage, Config.dataHelpHeader).Help(shell);

		// METADATA ROW
		label = new Label(shell, SWT.CENTER);
		label.setForeground(WHITE);
		label.setText("Metadata File/Folder:");
		meta = new Text(shell, SWT.SINGLE);
		meta.addModifyListener(metalistener);
		meta.setLayoutData(gridData1);
		FileBrowsers fbb2 = new FileBrowsers(meta);
		Button metaFile = fbb2.FileBrowseButton(shell);
		metaFile.setLayoutData(Config.GRID_DATA_FILL);
		Button metaFolder = fbb2.diags(shell);
		metaFolder.setLayoutData(Config.GRID_DATA_FILL);
		Label label3 = new Label(shell, SWT.CENTER);
		label3.setText("File delimiter:");
		label3.setForeground(WHITE);
		Text meta3 = new Text(shell, SWT.SINGLE);
		meta3.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				metadelim = meta3.getText();
			}
		});
		new HelpButton(Config.metadataHelpMessage, Config.metadataHelpHeader).Help(shell);

		// RESULTS ROW
		label = new Label(shell, SWT.CENTER);
		label.setForeground(WHITE);
		label.setText("Results Folder: ");
		res = new Text(shell, SWT.SINGLE);
		res.addModifyListener(reslistener);
		res.setLayoutData(gridData1);
		FileBrowsers fbb4 = new FileBrowsers(res);
		Button resBrowse = fbb4.diags(shell);
		resBrowse.setLayoutData(Config.GRID_DATA_FILL);
		new HelpButton(Config.resultsHelpMessage, Config.resultsHelpHeader).Help(shell);
		;

		// NEXT BUTTON
		Button msgBtn = new Button(shell, SWT.PUSH);
		msgBtn.setText("Next ->");
		msgBtn.addListener(SWT.Selection, event -> findComps());
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.horizontalSpan = 7;
		msgBtn.setLayoutData(gridData);
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
		firstScroll.setLayout(new GridLayout(2, false));
		firstScroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite firstContent = new Composite(firstScroll, SWT.NONE);
		firstContent.setLayout(new GridLayout(2, false));
		firstContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
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
				runDESeq2(first);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		runButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		// checkboxes are shown in columns with 30 comparisons in each check
		// box.
		for (int i = 0; i < comps.size(); i++) {
			// i2 stores the value of i for the purpose of selecting all
			// comparisons in an entire file
			final int i2 = i;
			// this part of the loop sets a checkbox to allow the user to select
			// all comparisons within a single file.
			Button file = new Button(firstContent, SWT.CHECK);
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
				CheckBoxes.get(i).add(b);
			}
		}
		firstScroll.setContent(firstContent);
		firstScroll.setExpandHorizontal(true);
		firstScroll.setExpandVertical(true);
		firstScroll.setMinSize(new Point(Config.SHELL_WIDTH, CheckBoxes.size() * Config.textHt * 2 + 5000));
		shell.pack();

	}

	/**
	 * This runs the DESeq2 R script. It creates the run information file,
	 * displays necessary information, checks if comparisons have been selected,
	 * then passes the args to DiffExCalc, and displays the results file within
	 * a text box in the UI to the user upon completion.
	 * 
	 * @param firstContent
	 * 
	 * @throws IOException
	 *             if the file cannot be found/written to. Rarely throws
	 *             exception, as the file is auto-generated and is not dependent
	 *             on user input whatsoever.
	 */
	private void runDESeq2(Group firstContent) throws IOException {
//		for(Control s: firstContent.getChildren()){
//			s.dispose();
//		}
		try {
			if (Config.OS_TYPE.toLowerCase().contains("win")) {
				statusLabel.setForeground(WHITE);
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
				Thread thread;
				GridData firstData = new GridData(SWT.FILL, SWT.FILL, true, false);
				firstData.horizontalSpan = 2;
				firstContent.setBackground(WHITE);
				Label l = new Label(firstContent, SWT.NONE);
				//l.setForeground(WHITE);
				l.setText("Run Progress");
				l.setFont(new Font(l.getDisplay(), new FontData("Arial", 16, SWT.BOLD)));
				l.setLayoutData(firstData);
				progressBar = new ProgressBar(firstContent, SWT.HORIZONTAL);
				progressBar.setLayoutData(firstData);
				statusLabel = new Label(firstContent, SWT.NONE);
				statusLabel.setLayoutData(firstData);
				//statusLabel.setForeground(WHITE);
				Text text = new Text(firstContent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
				GridData secondData = new GridData(SWT.FILL, SWT.FILL, false, false);
				secondData.widthHint = 400;
				secondData.heightHint = 500;
				text.setLayoutData(secondData);
				Button startNew = new Button(shell, SWT.PUSH);
				startNew.setText("New Run");
				startNew.addListener(SWT.Selection, event -> startNew());
				startNew.setEnabled(false);
				for (ButtonList b : CheckBoxes) {
					b.getFileButton().dispose();
					for (Button button : b) {
						button.dispose();
					}
				}

				shell.pack();
				dia.setMessage("Run Started. Run information is stored in:\n" + info.getPath()
						+ ". \n\nProjected run time: " + ((Config.getSize(compars)) + 4) + " minutes.");
				dia.open();

				thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							DiffExCalc.main(cmds, compars, progressBar, info, statusLabel, text, datadelim, metadelim);
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
				stop.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						thread.interrupt();
						disposeChildren(shell);
						makeUI();
						System.out.println("Thread stopped.");
					}
				});
				shell.pack();
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