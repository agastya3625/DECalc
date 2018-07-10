package com.ui;

import java.io.File;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

/**
 * This class creates a File browsing dialog with a text box that displays the
 * content viewers click on in the file dialog.
 * 
 * @author Agastya Sharma Date: July 14th, 2017
 * @version 1.0
 */
public class FileBrowsers {
	private Text text; // Text area to display selected file

	/**
	 * Constructor that takes in the text box to display files in
	 * 
	 * @param t:
	 *            Text area to display content in
	 */
	public FileBrowsers(Text t) {
		text = t;
	}

	/**
	 * This method returns a button for the user to click to browse files on
	 * their computer. Specifically, this method only creates dialogs that allow
	 * for users to browse files, not folders.
	 * 
	 * @param s:
	 *            Shell for button to be displayed in
	 * 
	 * @return Button that opens a file browser
	 */
	public Button FileBrowseButton(Composite s) {
		Button button = new Button(s, SWT.PUSH);
		button.setText("Browse Files");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(s.getShell(), SWT.NULL);
				dialog.setFilterExtensions(new String[] { "*.csv", "*.tsv", "*.txt" });
				String path = dialog.open();
				if (path != null) {
					File file = new File(path);
					// displays the files in text box
					if (file.isFile())
						displayFiles(new String[] { file.toString() });
					else
						displayFiles(file.list());

				}
			}
		});
		return button;
	}

	/**
	 * This method displays the files in the text box once they have been
	 * selected by the user.
	 * 
	 * @param files:
	 *            String[] of files in a folder selected
	 */
	public void displayFiles(String[] files) {
		for (int i = 0; files != null && i < files.length; i++) {
			text.setText(files[i]);
			text.setEditable(true);
		}
	}

	/**
	 * This method returns a button for the user to click to browse folders on
	 * their computer. Specifically, this method only creates dialogs that allow
	 * for users to select folders, not files.
	 * 
	 * @param shell:
	 *            Shell for button to be displayed in
	 * 
	 * @return Button that opens a directory browser
	 */
	public Button diags(Composite shell) {

		// Clicking the button will allow the user
		// to select a directory
		Button button = new Button(shell, SWT.PUSH);
		button.setText("Browse Folders");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				DirectoryDialog dlg = new DirectoryDialog(shell.getShell());
				String path = dlg.open();
				if (path != null) {
					// Set the text box to the new selection
					text.setText(path);
				}
			}
		});
		return button;
	}
}
