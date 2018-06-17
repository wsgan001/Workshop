package org.processmining.plugins.ding;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayoutConstants;


public class SaveDialogText {
	
	
	public static void main(String[] args) {
	    // new Main().initUI();
		
		// how to run ProMPropertiesPanel?? 
		
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
            	UIManager.put("swing.boldMetal", Boolean.FALSE);
            	JFrame jFrame = new JFrame("test");
            	FileChooserDemo fc = new FileChooserDemo();
            	jFrame.add(fc);
            	fc.setVisible(true);
                
                
            }
        });
	  }
}

class FileChooserDemo extends JPanel{

	// create one sliker to choose if save the filted log, maybe in the sense of ProM?? ... 
	
	private static final long serialVersionUID = 7043633275971617176L;
	private static final String TITLE = "Save Filted Log";
	
	private static final String SAVE = "Save";
	private static final String NOTSAVE = "Not save";
	

	public FileChooserDemo() {
		// TODO Auto-generated constructor stub

		JPanel basicPanel = new JPanel();
		double sizeBasic[][] = { { 30, TableLayoutConstants.FILL, 50 },
				{ 80, 30, 30, 30, 30, 30, 30, 60, TableLayoutConstants.FILL } };
		
		// basicPanel.setLayout(new TableLayout(sizeBasic));
		basicPanel.setLayout(new GridLayout(2, 3));
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
				getFileName();
			}
		});
		setVisible(true);
	}
	
	private String getFileName() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Specify a file to save");   
		 
		int userSelection = fileChooser.showSaveDialog(this);
		 
		if (userSelection == JFileChooser.APPROVE_OPTION) {
		    File fileToSave = fileChooser.getSelectedFile();
		    
		    System.out.println("Save as file: " + fileToSave.getAbsolutePath());
		    return fileToSave.getAbsolutePath();
		}
		return null;
	}

}


// here I want to add one JPanel which includes the choices if it want to save and then the places to save
class Main {
  JTextArea textArea;
  JButton save;
  void initUI() {
    JFrame frame = new JFrame(Main.class.getSimpleName());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    textArea = new JTextArea(24, 80);
    save = new JButton("Save to file");
    save.addActionListener(new ActionListener() {
		
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			saveToFile();
		}
	});
    frame.add(new JScrollPane(textArea));
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(save);
    frame.add(buttonPanel, BorderLayout.SOUTH);
    frame.setSize(500, 400);
    frame.setVisible(true);
  }


  protected void saveToFile() {
    JFileChooser fileChooser = new JFileChooser();
    int retval = fileChooser.showSaveDialog(save);
    if (retval == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      if (file == null) {
        return;
      }
      System.out.println(file.getName());
      if (!file.getName().toLowerCase().endsWith(".txt")) {
        file = new File(file.getParentFile(), file.getName() + ".txt");
      }
      try {
        //textArea.write(new OutputStreamWriter(new FileOutputStream(file),
        //    "utf-8"));
        // Desktop.getDesktop().open(file);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  
}