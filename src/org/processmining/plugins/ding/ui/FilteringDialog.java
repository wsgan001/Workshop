package org.processmining.plugins.ding.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.processmining.plugins.ding.FilteringParameters;

/**
 * Parameter dialog for the mining of a workshop model from an event log.
 * 
 * @author hverbeek
 * 
 */
public class FilteringDialog extends JPanel {
	
	
	private static String Over_Threshold = "Variant Over One ThresHold";
	private static String Cumulative_Freq="Cumulative Frequency Over One Threshold";
	private static String Top_Num = "Choose Top N Vairants"; 
	
	/**
	 * Parameter dialog for mining the given event log for a workflow model.
	 * No we don't really need it.. 
	 */
	public FilteringDialog(final FilteringParameters paras) {
//		Container cp = getContentPane();
		
		GridLayout grid = new GridLayout(3,1,0,5);
		setLayout(grid);
		
	    JRadioButton overBtn = new JRadioButton(Over_Threshold); 
	    overBtn.setMnemonic(KeyEvent.VK_O);
	    overBtn.setSelected(true);
	    
	    JRadioButton cumBtn = new JRadioButton(Cumulative_Freq); 
	    cumBtn.setMnemonic(KeyEvent.VK_C);
	    
	    JRadioButton topBtn = new JRadioButton(Top_Num); 
	    topBtn.setMnemonic(KeyEvent.VK_T);
	    
	    ButtonGroup choicesGroup = new ButtonGroup();
	    choicesGroup.add(overBtn);
	    choicesGroup.add(cumBtn);
	    choicesGroup.add(topBtn);        

	    JPanel choicesPanel = new JPanel(new GridLayout(3,1));
	    choicesPanel.setBorder(BorderFactory.createTitledBorder("Filtering Chioces"));
	    choicesPanel.add(overBtn);
	    choicesPanel.add(cumBtn);
	    choicesPanel.add(topBtn);
	    add(choicesPanel);
	    
	    final JLabel choicesMsg = new JLabel("There are three Filtering methods\n", JLabel.LEFT);
	    JPanel msgPanel = new JPanel();
	    msgPanel.setBorder(BorderFactory.createTitledBorder("Choices Information"));
	    // msgPanel.setPreferredSize(new Dimension(50, 300));
	    msgPanel.add(choicesMsg, JPanel.LEFT_ALIGNMENT);
	    add(msgPanel);
	    
	    // we also need to make sure which it chooses?? 
	    overBtn.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e) {
	    		// output the help information w.r.t the commandType
	    		// System.out.println("Variant over one threshold, like percentage oder num");
	    		choicesMsg.setText("Choose : Given percentage over one variant to filter the data");
	    		// choice = Over_Threshold;
	    		paras.setFilterType(Over_Threshold);
	    	}
	    });
	    cumBtn.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e) {
	    		// output the help information w.r.t the commandType
	    		// System.out.println(Cumulative_Freq+ " over percentage");
	    		choicesMsg.setText("Given percentage of cumulative frequency: \n "
	    				+ "Negative means, it sort variants from lowest frequency; "
	    				+ "if positive, if sort variants from highest frequency");
	    		// choice = Cumulative_Freq;
	    		paras.setFilterType(Cumulative_Freq);
	    	}
	    });
	    topBtn.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e) {
	    		// output the help information w.r.t the commandType
	    		// System.out.println(Top_Num+ " over percentage");
	    		choicesMsg.setText("Give the number of top variants you want to choose :\n"
	    				+ "if negative, it means to choose variants with lowest frequency"
	    				+ "if positive , it means to choose variants with highest frequency");
	    		// choice = Top_Num;
	    		paras.setFilterType(Top_Num);
	    	}
	    });
	    
	    
	    final JFormattedTextField  choicesValueInput = new JFormattedTextField(NumberFormat.getNumberInstance());
		choicesValueInput.setValue(0.1);
		choicesValueInput.setColumns(10);
		
		choicesValueInput.addPropertyChangeListener("value",new PropertyChangeListener(){
			
			public void propertyChange(PropertyChangeEvent e) {
				// choiceValue = (double) choicesValueInput.getValue();
				paras.setFilterValue(((Number)choicesValueInput.getValue()).doubleValue());
			}	
		});
		
		
		JPanel valueInputPanel = new JPanel();
		valueInputPanel.setBorder(BorderFactory.createTitledBorder("Choices Value Input"));
		valueInputPanel.add(choicesValueInput);
//		cp.add(valueInputPanel);
		add(valueInputPanel);
	    
		paras.setFilterValue(((Number)choicesValueInput.getValue()).doubleValue());
	}
	
}
