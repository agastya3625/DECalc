package com.ui;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Button;

@SuppressWarnings("serial")
/**
 * This class serves as a wrapper class for ArrayList of Button objects. It is
 * used to generate the checklist of comparisons shown to the user.
 * 
 * @version 1.0
 * @author Agastya Sharma July 14th, 2017
 */
public class ButtonList extends ArrayList<Button> {
	private Button fileButton; // file that the list was made from. This allows
								// users to select the entire file's
								// comparisons.

	/**
	 * This constructor calls the superconstructor.
	 * 
	 * @param size:
	 *            size of the ButtonList
	 */
	public ButtonList(int size) {
		super(size);
	}

	/**
	 * @return the fileButton
	 */
	public Button getFileButton() {
		return fileButton;
	}

	/**
	 * @param fileButton
	 *            the fileButton to set
	 */
	public void setFileButton(Button fileButton) {
		this.fileButton = fileButton;
	}

	/**
	 * This method checks if at least one of the comparisons in a ButtonList is
	 * selected.
	 * 
	 * @return boolean true is at least one is selected, false otherwise.
	 */
	public boolean OneSelected() {
		boolean toReturn = false;
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).getSelection()) {
				toReturn = true;
			}
		}
		return toReturn;
	}

}
