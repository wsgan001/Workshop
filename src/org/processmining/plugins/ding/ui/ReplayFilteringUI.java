package org.processmining.plugins.ding.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;

import com.fluxicon.slickerbox.ui.SlickerSliderUI;

public class ReplayFilteringUI  extends JPanel implements ChangeListener, ItemListener {

	/**
	 * Firstly, we just need to create one interface which shows the result and control panel on the result
	 * 
	 * adjust threshold information
	 * 
	 * add color information on plaecs
	 * 
	 * control to delete them
	 *  
	 * 
	 */
	private static final long serialVersionUID = 1000L;
	
	protected JPanel rightPanel;
	protected JPanel rootPanel;
	protected ProMJGraphPanel graphPanel;
	protected boolean enableRedraw;
	double threshold;
	
	protected JSlider thresholdSlider;
	protected JLabel thresholdLabel;
	
	protected Color COLOR_BG = new Color(60, 60, 60);
	protected Color COLOR_BG2 = new Color(120, 120, 120);
	protected Color COLOR_FG = new Color(30, 30, 30);
	protected Font smallFont;
	
	
	
	public ReplayFilteringUI(PluginContext context, Petrinet net) {
		// we need net and event log as parameter??? Is that true, or we just need the result net?? 
		
		// redraw shows here.
		
		// firstly to initializeGUI
		initializeGUI();

		// get the updated parameter and then call method to deal with it
	}

	protected void initializeGUI() {
		// derive standard control element font
		smallFont = this.getFont().deriveFont(11f);
		// root panel which includes two views, one for result display, one for threshold control
		rootPanel = new JPanel();
		rootPanel.setBorder(BorderFactory.createEmptyBorder());
		rootPanel.setBackground(new Color(100, 100, 100));
		rootPanel.setLayout(new BorderLayout());
		
		
		// right filter panel for threshold input
		JPanel rightControlPanel = new JPanel();
		rightControlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		rightControlPanel.setBackground(COLOR_BG2);
		rightControlPanel.setOpaque(true);
		rightControlPanel.setLayout(new BorderLayout());
		JLabel nodeSigSliderLabel = new JLabel("Replayable Threshold");
		nodeSigSliderLabel.setFont(this.smallFont);
		nodeSigSliderLabel.setOpaque(false);
		nodeSigSliderLabel.setForeground(COLOR_FG);
		centerHorizontally(nodeSigSliderLabel);
		rightControlPanel.add(nodeSigSliderLabel, BorderLayout.NORTH);
		thresholdLabel = new JLabel("0.000");
		thresholdLabel.setOpaque(false);
		thresholdLabel.setForeground(COLOR_FG);
		thresholdLabel.setFont(this.smallFont);
		centerHorizontally(thresholdLabel);
		rightControlPanel.add(packVerticallyCentered(thresholdLabel, 50, 20), BorderLayout.SOUTH);
		thresholdSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 0);
		thresholdSlider.setUI(new SlickerSliderUI(thresholdSlider));
		thresholdSlider.addChangeListener(this);
		thresholdSlider.setOpaque(false);
		thresholdSlider.setToolTipText("<html>The lower this value, the more<br>"
				+ "events are shown increasing the detail <br>" + "and complexity of the model.</html>");
		rightControlPanel.add(thresholdSlider, BorderLayout.CENTER);
		
		
		rootPanel.add(rightControlPanel, BorderLayout.EAST);
		
		// left result panel to show the result
		
	}
	
	protected void centerHorizontally(JLabel label) {
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
	}
	
	protected JPanel packVerticallyCentered(JComponent component, int width, int height) {
		JPanel boxed = new JPanel();
		boxed.setLayout(new BoxLayout(boxed, BoxLayout.X_AXIS));
		boxed.setBorder(BorderFactory.createEmptyBorder());
		boxed.setOpaque(false);
		Dimension dim = new Dimension(width, height);
		component.setMinimumSize(dim);
		component.setMaximumSize(dim);
		component.setPreferredSize(dim);
		component.setSize(dim);
		boxed.add(Box.createHorizontalGlue());
		boxed.add(component);
		boxed.add(Box.createHorizontalGlue());
		return boxed;
	}


	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == thresholdSlider) {
			// updateThresholdSlider();
			threshold = thresholdSlider.getValue();
			
			thresholdLabel.setText("Set threshold: "+ threshold);
			if (thresholdSlider.getValueIsAdjusting() == false) {
				// fastTransformer.setThreshold(value);
				// here we need to set the parameter into part to redrawGraph..
			
				// redrawGraph(true);
			}
		}
		
	}
	
	public void redrawGraph(boolean isUpdate) {
		//		System.err.println("[FastTransformerPanel] Orientation for graph " + graph.hashCode() + " is " + graph.getAttributeMap().get(AttributeMap.PREF_ORIENTATION));
		if (enableRedraw == false) {
			return; // ignore
		}
		
		// updateGraphAttributesFromUI();
		// setGuiEnabled(false);
		rootPanel.add(graphPanel, BorderLayout.CENTER);
		rootPanel.revalidate();
		
		
	}
}
