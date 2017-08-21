package com.ui;

import java.io.*;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.backEnd.*;

/**
 * This class contains the main method that runs once the program is executed.
 * It first "orients" itself to the user's machine by getting information about
 * its OS, Java version, and R version. Then, if all of those are in order, it
 * runs MainWindow.java.
 * 
 * @version 1.0
 * @author Agastya Sharma July 14th, 2017
 */
public class Start {
	private static Display display;
	private static StatusUpdate su;

	/**
	 * this method runs all system checks and file migrations, then starts the
	 * GUI.
	 * 
	 * @param args:
	 *            Java requirement
	 */
	public static void main(String[] args) {
		// makes mac-specific changes to the GUI to change the default menu bar
		if (System.getProperty("os.name").toLowerCase().contains("mac")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DECalc");
		}
		display = Display.getDefault();
		// default heights and widths. thee are changed if the screen display is
		// smaller than these dimensions.
		int newHeight = 790;
		int newWidth = 1366;
		if (display.getMonitors()[0].getBounds().height < 790) {
			newHeight = display.getMonitors()[0].getBounds().height;
		} else {
			newHeight = 790;
		}
		if (display.getMonitors()[0].getBounds().width < 1366) {
			newWidth = display.getMonitors()[0].getBounds().height;
		} else {
			newWidth = 1366;
		}
		Config.ResizeAll(newWidth, newHeight);
		// run java and OS checks
		try {
			OrientMyself();
			// start MainWindow
			if (loadPreferences()) {
				ShowAboutMessage();
			} else {
				new MainWindow(display);
			}
			display.dispose();
		} catch (IllegalArgumentException e) {
			Shell s = new Shell(display, Config.SHELL_STYLE);
			MessageBox dia = new MessageBox(s, SWT.ERROR);
			dia.setMessage(e.getMessage());
			dia.open();
		} catch (VersionException e) {
			Shell s = new Shell(display, Config.SHELL_STYLE);
			s.setSize(400, 400);
			s.setImage(new Image(display, Config.ICON));
			s.open();
			Label label = new Label(s, SWT.CENTER);
			label.setText(e.getMessage());
			label.setBounds(100, 10, 200, 60);
			Button OK = new Button(s, SWT.PUSH);
			OK.setText("OK");
			OK.setBounds(315, 350, Config.BUTTON_WIDTH / 2, 20);
			OK.addListener(SWT.Selection, event -> disposeAll());
			Button details = new Button(s, SWT.PUSH);
			details.setText("Details");
			details.setBounds(10, 350, Config.BUTTON_WIDTH / 2, 20);
			details.addListener(SWT.Selection, event -> {
				try {
					showErrs(s, e);
				} catch (IOException e1) {
				}
			});
			while (!s.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			if (!(e instanceof NullPointerException)) {
				Shell s = new Shell(display, Config.SHELL_STYLE);
				s.setSize(400, 400);
				s.setImage(new Image(display, Config.ICON));
				s.open();
				Label label = new Label(s, SWT.CENTER);
				label.setText("Whoops! \nAn unexpected error occurred. \nClick details for more info.");
				label.setBounds(100, 10, 200, 60);
				Button OK = new Button(s, SWT.PUSH);
				OK.setText("OK");
				OK.setBounds(315, 350, Config.BUTTON_WIDTH / 2, 20);
				OK.addListener(SWT.Selection, event -> disposeAll());
				Button details = new Button(s, SWT.PUSH);
				details.setText("Details");
				details.setBounds(10, 350, Config.BUTTON_WIDTH / 2, 20);
				details.addListener(SWT.Selection, event -> {
					try {
						showErrs(s, e);
					} catch (IOException e1) {
					}
				});
				while (!s.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			} else {
				e.printStackTrace();
			}
		}
	}

	private static void ShowAboutMessage() {
		Shell message = new Shell(display, Config.SHELL_STYLE);
		message.setText("About DECalc");
		message.setImage(new Image(display, Config.ICON));
		message.setSize(600, 500);
		Label l = new Label(message, SWT.WRAP);
		l.setBounds(10, 10, 580, 390);
		l.setText(Config.aboutMessage);
		Link toGit = new Link(message, SWT.NONE);
		toGit.setBounds(10, 400, 580, 20);
		toGit.setText("\t\t<a>https://github.com/agastya3625/DECalc</a>");
		toGit.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				try {
					java.awt.Desktop.getDesktop().browse(java.net.URI.create(arg0.text));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		Button OK = new Button(message, SWT.PUSH);
		OK.setText("Continue");
		OK.setBounds(490, 450, 100, 20);
		OK.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				message.dispose();
				new MainWindow(display);
			}
		});
		Button checkbox = new Button(message, SWT.CHECK);
		checkbox.setText("Show on startup");
		checkbox.setBounds(10, 450, 200, 20);
		checkbox.setSelection(true);
		checkbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				UpdatePrefs(checkbox.getSelection());
			}
		});
		Rectangle bounds = display.getPrimaryMonitor().getBounds();
		Rectangle rect = message.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		message.setLocation(x, y);
		message.open();
		while (!message.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}

	/**
	 * disposes the current display, effectively closes the GUI.
	 */
	private static Object disposeAll() {
		display.dispose();
		return null;
	}

	/**
	 * shows errors and exceptions in a new popup window if unexpected errors
	 * occur during a GUI run.
	 * 
	 * @param s:
	 *            shell base for popup window
	 * 
	 * @param e:
	 *            Exception that occurred, displayed on the Shell.
	 */
	private static void showErrs(Shell s, Exception e) throws IOException {
		for (Control child : s.getChildren()) {
			child.dispose();
		}
		Button OK = new Button(s, SWT.PUSH);
		OK.setText("OK");
		OK.setBounds(315, 350, 75, 20);
		OK.addListener(SWT.Selection, event -> disposeAll());
		Text t = new Text(s, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		t.setBounds(10, 10, 380, 300);
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();
		su = new StatusUpdate(null, null, t);
		su.appendStatusLabel(exceptionAsString);
	}

	/**
	 * This method "orients" the program to the user's machine. it finds the
	 * Java version, compares it to the one used to build this program, and does
	 * the same for R. Exits if Java version is prior to 1.8.0_31.
	 * 
	 * @throws IOException
	 *             caused by getRInfo()
	 * @throws VersionException
	 *             if Java or R versions are obsolete
	 */
	private static void OrientMyself() throws IOException, VersionException {
		getURLs();
		if (!new File(System.getProperty("user.dir") + "/.DECresources").exists()) {
			migrateFiles();
		} else {
			loadFromPaths();
		}
		Config.OS_TYPE = System.getProperty("os.name");
		if (Config.OS_TYPE.toLowerCase().contains("win")) {
			Config.R_COMMAND = "\"C:\\Program Files\\R\\R-3.4.1\\bin\\Rscript\"";
		}
		if (!Config.verifyJavaVersion(System.getProperty("java.version"))) {
			throw new VersionException("Please update Java before using.");
		} else {
			getRInfo();
		}
	}

	/**
	 * The images are used internally by the GUI, so they are treated as URL and
	 * loaded as such.
	 */
	private static void getURLs() {
		InputStream is = ClassLoader.getSystemResourceAsStream(Config.ICON_FILE);
		if (is != null) {
			Config.ICON = new ImageData(is);
		}
		is = ClassLoader.getSystemResourceAsStream(Config.BKGD_FILE);
		if (is != null) {
			Config.BKGD = new ImageData(is);
		}
	}

	/**
	 * This method performs a version check for R by executing R.Version(),
	 * which tells the user which version of R they are using. Exits if version
	 * is prior to 3.3.2.
	 * 
	 * @throws IOException
	 *             caused by the Process running
	 * @throws VersionException
	 *             if R version is obsolete
	 */
	private static void getRInfo() throws IOException, VersionException {
		// executes an R command that returns the current version of R that the
		// user has installed. Compares the
		Process pb = Runtime.getRuntime().exec(Config.R_COMMAND + " " + Config.VERS);
		BufferedReader read = new BufferedReader(new InputStreamReader(pb.getInputStream()));
		String RVersion = read.readLine();
		RVersion = RVersion.split(" ")[3];
		RVersion = RVersion.trim();
		if (RVersion.compareTo(Config.R_VERSION) < 0) {
			throw new VersionException("Please update R before Using DECalc.");
		}
	}

	/**
	 * This method transfers all non-code files used by the GUI outside of the
	 * jar they were packaged in to allow access to sub-processes called by the
	 * GUI.
	 */
	private static void migrateFiles() throws IOException {
		Config.TEMP_DIR = new File(System.getProperty("user.dir") + "/.DECresources");
		Config.TEMP_DIR.setExecutable(true, false);
		Config.TEMP_DIR.mkdir();
		File paths = new File(Config.TEMP_DIR + "/paths.txt");
		PrintWriter out = new PrintWriter(paths);
		Config.prefs = fileStreams(Config.prefs);
		UpdatePrefs(true);
		out.println("Config.prefs:" + Config.prefs);
		Config.RFile_Name = fileStreams(Config.RFile_Name);
		out.println("Config.RFile_Name:" + Config.RFile_Name);
		Config.MANUAL = fileStreamsBinary(Config.MANUAL);
		out.println("Config.MANUAL:" + Config.MANUAL);
		Config.VERS = fileStreams(Config.VERS);
		out.println("Config.VERSe:" + Config.VERS);
		Config.SAMPLE_DATA_FILE = fileStreams(Config.SAMPLE_DATA_FILE);
		out.println("Config.SAMPLE_DATA_FILE:" + Config.SAMPLE_DATA_FILE);
		Config.SAMPLE_METADATA_FILE = fileStreams(Config.SAMPLE_METADATA_FILE);
		out.println("Config.SAMPLE_METADATA_FILE:" + Config.SAMPLE_METADATA_FILE);
		out.close();
	}

	/**
	 * This method creates and manages a file stream for non-binary files to
	 * transfer them outside of the jar.
	 * 
	 * @param internalFileName:
	 *            name of the resource
	 * 
	 * @return String full path of the resource on the new machine
	 */
	private static String fileStreams(String internalFileName) throws IOException {
		BufferedReader br = new BufferedReader(
				new InputStreamReader(ClassLoader.getSystemResourceAsStream(internalFileName)));
		String newName = Config.TEMP_DIR + "/" + internalFileName;
		File newFile = new File(newName);
		newFile.setExecutable(true, false);
		FileWriter fw = new FileWriter(newFile);
		String line = br.readLine();
		while (line != null) {
			fw.write(line + "\n");
			line = br.readLine();
		}
		br.close();
		fw.close();
		return newName;
	}

	/**
	 * This method creates and manages a file stream for binary files (PDFs) to
	 * transfer them outside of the jar.
	 * 
	 * @param internalFileName:
	 *            name of the resource
	 * 
	 * @return String full path of the resource on the new machine
	 */
	private static String fileStreamsBinary(String internalFileName) throws IOException {
		BufferedInputStream br = new BufferedInputStream((ClassLoader.getSystemResourceAsStream(internalFileName)));
		String newName = Config.TEMP_DIR + "/" + internalFileName;
		File newFile = new File(newName);
		newFile.setExecutable(true, false);
		BufferedOutputStream fw = new BufferedOutputStream(new FileOutputStream(newFile));
		int temp = br.read();
		while (temp != -1) {
			fw.write(temp);
			temp = br.read();
		}
		br.close();
		fw.close();
		return newName;
	}

	/**
	 * this method determines whether or not to show the user the "about"
	 * message on startup or not.
	 * 
	 * @return true to show, false to not show.
	 * 
	 * @throws IOException
	 *             caused by parsing the file
	 */
	private static boolean loadPreferences() throws IOException {
		Scanner scan = new Scanner(
				new FileInputStream(new File(System.getProperty("user.dir") + "/.DECresources/prefs.txt")));
		while (scan.hasNext()) {
			String s = scan.nextLine();
			String[] split = s.split(":");
			if (split[0].equals("ShowAbout")) {
				if (split[1].equals("true")) {
					scan.close();
					return true;
				} else {
					scan.close();
					return false;
				}
			}
		}
		scan.close();
		return true;
	}

	/**
	 * updates the user preferences to show the about message or not.
	 * 
	 * @param toUpdate:
	 *            whether or not to show the message on startup or not.
	 */
	private static void UpdatePrefs(boolean toUpdate) {
		try {
			PrintWriter out = new PrintWriter(Config.prefs);
			if (toUpdate) {
				out.println("ShowAbout:true");
			} else {
				out.println("ShowAbout:false");
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * loads all external resources required by the GUI from their locally saved
	 * paths if they have already been migrated out of the jar file and into a
	 * directory.
	 */
	private static void loadFromPaths() {
		try {
			Scanner s = new Scanner(
					new FileInputStream(new File(System.getProperty("user.dir") + "/.DECresources/paths.txt")));
			Config.prefs = s.nextLine().split(":")[1];
			Config.RFile_Name = s.nextLine().split(":")[1];
			Config.MANUAL = s.nextLine().split(":")[1];
			Config.VERS = s.nextLine().split(":")[1];
			Config.SAMPLE_DATA_FILE = s.nextLine().split(":")[1];
			Config.SAMPLE_METADATA_FILE = s.nextLine().split(":")[1];
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}