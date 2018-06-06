package org.processmining.plugins.workshop.Me;


import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.util.ui.widgets.BorderPanel;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;

public class ReplayFilteringDialog extends ProMPropertiesPanel implements ProMWizardStep {

	private static final long serialVersionUID = 7834404978722012356L;

	private static final String TITLE = "Configure Replay Places Parameters";
	
	private final BorderPanel threshold;
	private final JLabel thresholdLabel;
	private final JSlider thresholdSlider;
	
	int h_min = 20, h_pref = 30, h_max = 40, w_min = 150, w_max = 1000, w_label = 80, w_slider = 300;
	private static final int SL_MIN = 0; // slider minimum value
	private static final int SL_MAX = 100; // slider maximum value

	
	public ReplayFilteringDialog()  {
		super(TITLE);
		
		threshold = addProperty("Threshold",new BorderPanel(20,3));
		threshold.setLayout(new BorderLayout());
		threshold.setForeground(WidgetColors.COLOR_ENCLOSURE_BG);
//		threshold.setPreferredSize(new Dimension(w_max,h_pref));
//		threshold.setMinimumSize(new Dimension(w_min,h_min));
//		threshold.setMaximumSize(new Dimension(w_max,h_max));
//		
		thresholdSlider = new JSlider(SL_MIN, SL_MAX, getSliderFromThreshold(80));
		// thresholdSlider = SlickerFactory.instance().createNiceIntegerSlider("", 1, 100, 5, Orientation.HORIZONTAL);
		thresholdSlider.addChangeListener(new SliderListener());
		thresholdSlider.setLayout(new BorderLayout());
		thresholdSlider.setOpaque(false);
		thresholdSlider.setPreferredSize(new Dimension(w_slider,h_pref));
		thresholdSlider.setMinimumSize(new Dimension(w_min-w_label,h_min));
		thresholdSlider.setMaximumSize(new Dimension(w_slider,h_max));
//		thresholdSlider.setMajorTickSpacing(10);
//		thresholdSlider.setMinorTickSpacing(1);
//		thresholdSlider.setPaintTicks(true);
//		thresholdSlider.setPaintLabels(true);
		
		thresholdLabel = new JLabel(formatThreshold(getThresholdFromSlider(thresholdSlider.getValue())));
		thresholdLabel.setForeground(WidgetColors.COLOR_LIST_SELECTION_FG);
//		thresholdLabel.setPreferredSize(new Dimension(w_label,h_pref));
//		thresholdLabel.setMinimumSize(new Dimension(w_label,h_min));
//		thresholdLabel.setMaximumSize(new Dimension(w_label,h_max));
		
		threshold.add(thresholdSlider);
		addProperty("Value of Threshold : ",thresholdLabel);
	}
	
	class SliderListener implements ChangeListener{

		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider) e.getSource();
			thresholdLabel.setText(formatThreshold(getThresholdFromSlider(thresholdSlider.getValue())));
			// here we need to return threshold as one parameter to ReplayPlugin   
			
		}
	}
	
	private String formatThreshold(double n) {
		if(Double.isInfinite(n) || Double.isNaN(n))
			return Double.toString(n);
		String result = String.format("%.1f", n);
		if(result.length() > 7)
			result = String.format("%.1e", n);
		return result;
	}

	private double getThreshold() {
		return getThresholdFromSlider(thresholdSlider.getValue());
	}
	
	private double getThresholdFromSlider(double sliderValue) {
		return  sliderValue; // / (SL_MAX - SL_MIN);
	}

	private int getSliderFromThreshold(double threshold) {
		return (int) threshold; // SL_MIN + (int) Math.round( threshold/ (SL_MAX - SL_MIN));
	}
    
	// it seems we need to build some parameters, but replay filtering 
	// we can have only one value, it's threshold...
	// should we create one parameter, or we just use FilteringParameter?? 
	// FilteringParameter. filterType and then threshold it fits
	public Object apply(Object model, JComponent component) {
		if(canApply(model, component)) {
			ReplayFilteringDialog step = (ReplayFilteringDialog) component;
			((FilteringParameters)model).setFilterType(TITLE);
			((FilteringParameters)model).setFilterValue(step.getThreshold());
		}
		return model;
	}

	public boolean canApply(Object model, JComponent component) {
		// TODO Auto-generated method stub
		return component instanceof ReplayFilteringDialog && model instanceof FilteringParameters;
	}

	public JComponent getComponent(Object model) {
		// TODO Auto-generated method stub
		return this;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return TITLE;
	}
    /*
     * test
     
	public static void main(String[] args) {
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	JFrame frame = new JFrame();
            	ReplayFilteringDialog replayDialog = new ReplayFilteringDialog();
            	
            	FilteringParameters params = new FilteringParameters();
            	replayDialog.apply(params, replayDialog);
            	frame.add(replayDialog);
            	frame.setVisible(true);
            	frame.setSize(300,400);
            	if (params != null) {
        			System.out.println(params.getThreshold()); 
        		}else {
        			System.out.println("No parameters are set... So return original");
        		}
            }
        }); 
		// ListWizard<FilteringParameters> listWizard = new ListWizard<FilteringParameters>(replayDialog);
		// FilteringParameters params = ProMWizardDisplay.show(null, listWizard, new FilteringParameters()) ;// ProMWizardDisplay.show(context, listWizard, new FilteringParameters());
		
	}
	*/
}
