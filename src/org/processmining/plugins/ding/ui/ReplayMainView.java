package org.processmining.plugins.ding.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.ding.FilteringAlphaMinerConnection;
import org.processmining.plugins.ding.FilteringAlphaMinerResult;
import org.processmining.plugins.ding.FilteringParameters;
import org.processmining.plugins.ding.util.NetUtilities;
import org.processmining.plugins.ding.util.ReplayPlaces;

import com.fluxicon.slickerbox.ui.SlickerRadioButtonUI;
import com.fluxicon.slickerbox.ui.SlickerSliderUI;

public class ReplayMainView extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ShowView leftView;
	PlaceControlView rightView;
	
	
	public ReplayMainView(PluginContext context, FilteringAlphaMinerResult result, FilteringParameters parameters) {
		// we could set one attribute into context to pass the number of traces
		
		leftView = new ShowView(context, result,parameters);
		rightView = new PlaceControlView();
		
		RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
		this.setBackground(new Color(240, 240, 240));
		
		leftView.setRightView(rightView);
		rightView.setLeftView(leftView);
		
		this.add(this.leftView, new Float(85));
		this.add(this.rightView, new Float(15));
		
		leftView.drawResult();
	}
	
}

class PlaceControlView extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ShowView leftView;
	double threshold = 100;
	
	boolean chooseDelete = false;

	protected JSlider thresholdSlider;
	protected JLabel thresholdLabel;
	
	protected Color COLOR_BG = new Color(60, 60, 60);
	protected Color COLOR_BG2 = new Color(120, 120, 120);
	protected Color COLOR_FG = new Color(30, 30, 30);
	protected Font smallFont;
	
	// from place control view, we get parameter to delete or color places
	public PlaceControlView() {
		
		//	this.setLayout(new BorderLayout());
		//this.setBackground(COLOR_BG);
		
		RelativeLayout rl = new RelativeLayout(RelativeLayout.Y_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
		this.setBackground(COLOR_BG2);
		Border raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		
		// right filter panel for threshold input
		JPanel thresholdPanel = new JPanel();
		thresholdPanel.setBorder(BorderFactory.createTitledBorder(raisedetched, "Set Replayable Threshold"));
		thresholdPanel.setOpaque(false);
		thresholdPanel.setLayout(new BorderLayout());
		// thresholdPanel.setBackground(COLOR_BG);
		/*
		JLabel nodeSigSliderLabel = new JLabel("Replayable Threshold");
		nodeSigSliderLabel.setFont(this.smallFont);
		nodeSigSliderLabel.setOpaque(false);
		nodeSigSliderLabel.setForeground(COLOR_FG);
		centerHorizontally(nodeSigSliderLabel);
		thresholdPanel.add(nodeSigSliderLabel, BorderLayout.NORTH);
		*/
		thresholdLabel = new JLabel();
		thresholdLabel.setOpaque(false);
		thresholdLabel.setForeground(COLOR_FG);
		thresholdLabel.setFont(this.smallFont);
		thresholdLabel.setText(""+threshold);
		centerHorizontally(thresholdLabel);
		// here we add Label to south, but it shoudl be in JPanel called upperControlPanel, I think !! 
		thresholdPanel.add(packVerticallyCentered(thresholdLabel, 50, 20), BorderLayout.NORTH);
		thresholdSlider = new JSlider(JSlider.VERTICAL, 0, 100, 0);
		thresholdSlider.setUI(new SlickerSliderUI(thresholdSlider));
		// we could get the threshold from before and show it here..
		// anway, two views are quite a lot
		thresholdSlider.setValue((int)threshold);
		thresholdSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if (e.getSource() == thresholdSlider) {
					// updateThresholdSlider();
					threshold = thresholdSlider.getValue();
					thresholdLabel.setText(""+threshold);
					if (thresholdSlider.getValueIsAdjusting() == false) {
						// we need to set one parameter and pass it to leftView and then create graph..
						leftView.recreateNet(threshold);
						
					}
				}
				
			}
		});
		thresholdSlider.setOpaque(false);
		thresholdSlider.setToolTipText("<html>The lower this value, the more<br>"
				+ "events are shown increasing the detail <br>" + "and complexity of the model.</html>");
		thresholdPanel.add(thresholdSlider, BorderLayout.CENTER);
		
		
		// add for radioButton to combine 
		JPanel colorDeletePanel = new JPanel();
		colorDeletePanel.setBorder(BorderFactory.createTitledBorder(raisedetched, "Set Replayable Threshold"));
		colorDeletePanel.setOpaque(false);
		colorDeletePanel.setLayout(new BoxLayout(colorDeletePanel, BoxLayout.Y_AXIS));
		JLabel colorDeleteLabel = new JLabel("Delete Bad Places");
		colorDeleteLabel.setOpaque(false);
		colorDeleteLabel.setForeground(COLOR_FG);
		colorDeleteLabel.setFont(this.smallFont);
		//centerHorizontally(lowerHeaderLabel);
		final JRadioButton colorRadioButton = new JRadioButton("Color Bad Places");
		colorRadioButton.setSelected(true);
		colorRadioButton.setUI(new SlickerRadioButtonUI());
		colorRadioButton.setFont(this.smallFont);
		colorRadioButton.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 2));
		colorRadioButton.setOpaque(false);
		colorRadioButton.setForeground(COLOR_FG);
		colorRadioButton.setAlignmentX(JRadioButton.LEFT_ALIGNMENT);
		colorRadioButton.setHorizontalAlignment(JRadioButton.LEFT);
		colorRadioButton.addItemListener(new ItemListener() {
			
			public void itemStateChanged(ItemEvent e) {
				// color the bad places in original graphs.. 
				// firstly to get the rightView and 
				// then pass the threshold to it and deleteChoice to ??? 
				// rightView only accept the graph to draw,(I think it better) it again..
				if(colorRadioButton.isSelected()) {
					leftView.setDrawDelete(false);
					leftView.drawResult();	
				}
			}
		});
		colorRadioButton.setToolTipText("<html>Color the bad places<br>"
				 + "which are not replayable for traces.</html>");
		final JRadioButton deleteRadioButton = new JRadioButton("Delete Bad Places");
		deleteRadioButton.setUI(new SlickerRadioButtonUI());
		deleteRadioButton.setFont(this.smallFont);
		deleteRadioButton.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 2));
		deleteRadioButton.setOpaque(false);
		deleteRadioButton.setForeground(COLOR_FG);
		deleteRadioButton.setAlignmentX(JRadioButton.LEFT_ALIGNMENT);
		deleteRadioButton.setHorizontalAlignment(JRadioButton.LEFT);
		deleteRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// after we choose delete, we delete places and repaint the graph again
				if(deleteRadioButton.isSelected()) {
					leftView.setDrawDelete(true);
					leftView.drawResult();
				}
			}
		});
		deleteRadioButton.setToolTipText("<html>delete bad places <br>"
				 + "which are not replayable for traces.</html>");
		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(colorRadioButton);
		radioGroup.add(deleteRadioButton);
		
		
		colorDeletePanel.add(colorDeleteLabel);
		colorDeletePanel.add(Box.createVerticalStrut(2));
		colorDeletePanel.add(colorRadioButton);
		colorDeletePanel.add(deleteRadioButton);
		colorDeletePanel.add(Box.createVerticalStrut(5));
		

		//this.add(thresholdPanel, BorderLayout.CENTER);
		//this.add(colorDeletePanel, BorderLayout.SOUTH);
		this.add(thresholdPanel, new Float(60));
		this.add(colorDeletePanel, new Float(30));
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

	public void setLeftView(ShowView leftView) {
		this.leftView = leftView;
	}
	
	public void setThreshold(double thres) {
		this.threshold = thres;
		System.out.println("set threshold "+thres);
	}
	
}
class ShowView extends JPanel{
	/**
	 * We need to add PIP and Zoom function on it
	 */
	private static final long serialVersionUID = 1L;
	PluginContext context;
	Petrinet net;
	FilteringParameters parameters;
	boolean drawDelete;
	
	FilteringAlphaMinerResult result;
	PlaceControlView rightView;
	
	// we want to add graphPanel to make uniform, how to add them??
	// first constructor and from Factory to add them..
	// after we put graph on GraphPanel, then we put JGraphPanel into the show panel./
	// it accepts param ProMJGraph..
	ProMJGraphPanel graphPanel; 
	// constructor is from graph..
	
	Double currentScale = 1.0;
	
	class ZoomListenerOnLeftPanel implements MouseWheelListener {
		public void mouseWheelMoved(MouseWheelEvent e) {
			// TODO Auto-generated method stub
			int stps = e.getWheelRotation();
			Double newScale = currentScale;
			if (stps == -1) {
				newScale *= 1.25;
			}
			else if (stps == 1) {
				newScale *= 0.80;
			}
			setScale(newScale);
		}
	}
	
	private void setScale(Double newScale) {
		// TODO Auto-generated method stub
		graphPanel.setScale(newScale);
		currentScale = newScale;
	}
	
	// we accept Petrinet and then create one graph from it 
	public ShowView(PluginContext context, FilteringAlphaMinerResult result, FilteringParameters parameters) {
		// super(context);
		
		RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
		this.parameters = parameters;
		this.context = context;
		this.result = result;
		this.net = result.getNet();
		this.drawDelete = false;
		graphVisualize();
		this.add(graphPanel, new Float(100));
		// to control the zoom
		// this.addMouseWheelListener(new ZoomListenerOnLeftPanel());
	}
	
	public void recreateNet(double percentage) {
		parameters.setReplayThreshold(percentage);
		int threshold = (int) (parameters.getTraceNum() * percentage*0.01);
		// System.out.println(threshold);
		// we add connection to context if it creates another petri net..
		Collection<FilteringAlphaMinerConnection> connections;
		try {
			// how to seperate the context from UI and normal Plugin ?? 
			// it seems that we firstly get parameters like configuration from UIPluginContext 
			// then we give it to PluginContext, there we test if the connection already exists.
			connections =  context.getConnectionManager().getConnections(FilteringAlphaMinerConnection.class, context, net, percentage);
			for (FilteringAlphaMinerConnection connection : connections) {
				// we use different connections to show
				if ( connection.getObjectWithRole(FilteringAlphaMinerConnection.LABEL).equals(FilteringAlphaMinerConnection.connectReplayType)
						&& connection.getObjectWithRole(FilteringAlphaMinerConnection.FILTER_PARAMETERS).equals(percentage) 
						&& ((Petrinet)(connection.getObjectWithRole(FilteringAlphaMinerConnection.PN))).getLabel().equals(net.getLabel())) {
					this.result =  (FilteringAlphaMinerResult) connection.getObjectWithRole(FilteringAlphaMinerConnection.RESULT);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
	
		ReplayPlaces.markPlaces(net, threshold);
		// how does this result change??? Or it doesn't change??? 
		context.addConnection(new FilteringAlphaMinerConnection(net, percentage, result));
		if(ReplayPlaces.fitNet(net)) {
			JOptionPane.showMessageDialog(this,
				    "The net places fit all event log at threshold " + percentage,
				    "Inane information",
				    JOptionPane.INFORMATION_MESSAGE);
		}
		drawResult();
	}

	public void drawResult() {
		// After setting the threshold to 0, 
		// whatever delete or color, if we find the graph fits well, we will output the info Pane..
		// if places in net are all not marked under threshold, we output one Pane
		this.remove(graphPanel);
		graphVisualize();
		this.add(graphPanel, new Float(100));
		this.revalidate();
		this.repaint();
	}

	public void setRightView(PlaceControlView rightView) {
		this.rightView = rightView;
	}
	
	public void setDrawDelete(boolean drawDelete) {
		this.drawDelete = drawDelete;
	}
	
	
	private void createGraphPanel(Petrinet net) {
		
		/**
		 * // if we use this method, what we get is actually without the PIP and Zoom , I want to try the method
		// graphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, graph);
		// graphPanel.getGraph().setEditable(false);
		// but this method the graph is the 
		 * private ProMJGraphPanel visualizeGraph(GraphLayoutConnection layoutConnection, PluginContext context,
			DirectedGraph<?, ?> graph, ViewSpecificAttributeMap map) {
			}
		   It returns the ProMJGraphPanel, so we just need to add thoes attributes into graph??? 
		   we can create ProMJpanel directly from net..
		   
		   graphPanel = new ProMJGraphPanel(graph);
		// if the setting have sth problem?? 
		graphPanel.getGraph().setEditable(false);
		
		 */
		graphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, net);
		graphPanel.getGraph().setEditable(false);
		
	}
	
	private void graphVisualize() {
		// how to make it ScalableComponet??
		if(drawDelete) {
			// if we need to delete Places, we change the net, but how about the next time??
			// we actually want to use online control, then it means we need to create another net for it?
			// or we control it by createGraph???? 
			Petrinet dnet = NetUtilities.clone(net);
			// no we can't get it from this.. Another way around it to add into place the same attributes
			NetUtilities.resetPetrinet(dnet);
			// we need to test the result of it, if dnet after deleting is empty 
			// we need to keep the colored form of it. 
			if(dnet.getPlaces().size() <1) {
				// we output warning message and
				JOptionPane.showMessageDialog(this,
					    "The deleted net is empty. So only show colored graph",
					    "Inane warning",
					    JOptionPane.WARNING_MESSAGE);
				// show keep the colored graph.
				colorGraph();
				
			}else {
				// here on thing is to get the delete net and then to find it..
				createGraphPanel(dnet);
			}
		}else {
			colorGraph();
		}
		
	}
	
	private void colorGraph() {
		// constructVisualization(graph.getViewSpecificAttributes(), true, true);
		createGraphPanel(net);
		if(!drawDelete) {
			for (Place p : net.getPlaces()) {
				// if place is bad, then we give it a color.. Maybe we could delete it later
				if ((boolean)p.getAttributeMap().get("isMarked")) {
					// make the mark uniform
					graphPanel.getGraph().getViewSpecificAttributes().putViewSpecific(p, AttributeMap.FILLCOLOR,java.awt.Color.RED);
				}
			}
		}
	}
		
}
