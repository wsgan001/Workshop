package org.processmining.plugins.ding.ui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.processmining.plugins.ding.FilteringParameters;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayoutConstants;

public class SaveFilePanel extends JPanel {

		// create one sliker to choose if save the filted log, maybe in the sense of ProM?? ... 
		
		private static final long serialVersionUID = 7043633275971617176L;
		private static final String TITLE = "Save Filted Log";
		
		private static final String SAVE = "Save";
		private static final String NOTSAVE = "Not save";
		

		public SaveFilePanel(final FilteringParameters parameters) {
			// TODO Auto-generated constructor stub

			JPanel basicPanel = new JPanel();
			double sizeBasic[][] = { { 30, TableLayoutConstants.FILL, 50 },
					{ 80, 30, 30, 30, 30, 30, 30, 60, TableLayoutConstants.FILL } };
			
			// basicPanel.setLayout(new TableLayout(sizeBasic));
			basicPanel.setLayout(new GridLayout(3, 3));
			basicPanel.setBackground(new Color(200, 200, 200));
			
			SlickerFactory slickerFactory = SlickerFactory.instance();
			
			// add question
			basicPanel.add(
					slickerFactory.createLabel("<html><h1>Save filted log </h1></html>"),
					"0, 0, 1, 0, l, t");

			JLabel queryChoice = slickerFactory.createLabel("Save the filtered log??");
			
			final JRadioButton saveBtn = slickerFactory.createRadioButton("Yes");		
			final JRadioButton noSaveBtn = slickerFactory.createRadioButton("No");
			noSaveBtn.setSelected(true);
			
			ButtonGroup saveChoice = new ButtonGroup();
			saveChoice.add(saveBtn);
			saveChoice.add(noSaveBtn);

			// basicPanel.add(saveBtn);
			
			// basicPanel.add(noSaveBtn);
			
			int basicRowCounter = 1;
			

			basicPanel.add(saveBtn, "0," + basicRowCounter + ",2," + basicRowCounter + ",l,b");
			basicRowCounter++;
			basicPanel.add(noSaveBtn, "0," + basicRowCounter + ",2," + basicRowCounter + ",l,b");
			
			// after we choose yes, and then we need to choose the file path to save
			// so after we choose  
			saveBtn.addActionListener(new ActionListener() {		
				public void actionPerformed(ActionEvent e) {
					String fname = getFileName();
					parameters.setSaveFile();
					parameters.setSaveList(fname);
				}
			});
			
			add(basicPanel);
		}
		
		
		private String getFileName() {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Specify a file to save");   
			 
			int userSelection = fileChooser.showSaveDialog(this);
			 
			if (userSelection == JFileChooser.APPROVE_OPTION) {
			    File fileToSave = fileChooser.getSelectedFile();
			    String fname = fileToSave.getAbsolutePath();
			    // here we need to add some Reg to make the name quite good..
			    // but one thing is not good, if we want to save other file type
			    System.out.println("Save as file: " + fname);
			    return fname;
			}
			return null;
		}


}
