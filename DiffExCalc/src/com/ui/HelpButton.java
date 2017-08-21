package com.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

/**
 * This class creates the help buttons that show what files to upload for the
 * data, metadata, and results files.
 * 
 * @author Agastya Sharma Date: July 14th, 2017
 * @version 1.0
 */
public class HelpButton {
	/**
	 * help message show in the dialog box
	 */
	private String message;

	/**
	 * title of message bow
	 */
	private String title;

	/**
	 * sets the title and message of the button
	 * 
	 * @param message:
	 *            help message
	 * 
	 * @param title:
	 *            help box title
	 */
	public HelpButton(String message, String title) {
		this.message = message;
		this.title = title;
	}

	/**
	 * creates and returns a styled help button
	 * 
	 * @param s:
	 *            parent shell of the button
	 * 
	 * @return styled Help button
	 */
	public Button Help(Shell s) {
		return start(s);
	}

	/**
	 * creates and returns a styled help button
	 * 
	 * @param s:
	 *            parent shell of the button
	 * 
	 */
	private Button start(Shell s) {
		Button button = new Button(s, SWT.PUSH);
		button.setText("?");
		button.addListener(SWT.Selection, event -> showHelp(message, s, title));
		return button;
	}

	/**
	 * shows the help dialog box when the button is clicked
	 * 
	 * @param message: message to display
	 * 
	 * @param shell: parent shell of the help button
	 * 
	 * @param title: title of the message box
	 * 
	 */
	private void showHelp(String message, Shell shell, String title) {
		int style = SWT.ICON_INFORMATION | SWT.OK;
		MessageBox dia = new MessageBox(shell, style);
		dia.setText(title);
		dia.setMessage(message);
		dia.open();
	}
}
