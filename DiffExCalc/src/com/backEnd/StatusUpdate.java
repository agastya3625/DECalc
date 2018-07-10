package com.backEnd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * This class allows for transfer of print statements within threads. It was
 * created to allow the subthread running the R process to send progress updates
 * to the GUI thread. It's purpose is to be able to send status updates to both
 * the GUI and the run log file seamlessly.
 * 
 * @version 1.0
 * @author Agastya Sharma July 14th, 2017
 */
public class StatusUpdate {
	private Label label; // Text label to display current task
	private Text text; // Textbox to show all tasks that have been run

	/**
	 * This constructor initializes the 3 status update
	 * 
	 * @param label:
	 *            label to send current update to
	 * 
	 * 
	 * @param text:
	 *            text box to send run upodates to
	 */
	public StatusUpdate(Label label, Text text) {
		this.label = label;
		this.text = text;
	}

	// /**
	// * This method sends data to the file via a FileWrtier.
	// *
	// * @param s:
	// * String to append to the file.
	// *
	// * @throws IOException
	// * caused by FileWriter
	// * @deprecated 7/25/17: functionality moved to R
	// */
	// public void sendToFile(String s) throws IOException {
	// ps = new FileWriter(runInfo, true);
	// ps.append(s + "\n");
	// ps.close();
	// }

	/**
	 * This method appends status updates to the text box as well as updates the
	 * label to reflect the current task.
	 * 
	 * @param s:
	 *            new status update
	 */
	public void appendStatusLabel(String s) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				text.append(s + "\n");
				Rectangle r = label.getBounds();
				Shell shell = label.getShell();
				if (label != null) {
					label.dispose();
					label = new Label(shell, SWT.NONE);
					label.setText(s + "\n");
					label.setBounds(r);
					label.setForeground(new Color(Display.getCurrent(), 255, 255, 255));
				}
			}
		});
	}

}
