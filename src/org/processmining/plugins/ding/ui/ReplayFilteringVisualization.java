package org.processmining.plugins.ding.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.plugins.ding.ReplayFilteringConnection;
import org.processmining.plugins.ding.ReplayFilteringResult;
import org.processmining.plugins.ding.util.NetUtilities;

import com.fluxicon.slickerbox.ui.SlickerRadioButtonUI;
import com.fluxicon.slickerbox.ui.SlickerSliderUI;

@Plugin(name = "Show Places Colored", parameterLabels = { "Petrinet" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
public class ReplayFilteringVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, ReplayFilteringResult result) {
		// we could put the trace  num into result, and then give it here
		return new ReplayMainView(context, result);
	}

}

class ReplayMainView extends JPanel{
	ShowView leftView;
	PlaceControlView rightView;
	
	public ReplayMainView(PluginContext context, ReplayFilteringResult result) {
		// we could set one attribute into context to pass the number of traces
		
		leftView = new ShowView(context, result);
		rightView = new PlaceControlView();
		// the easist way to add threshold is to add into result, but it violates the design pattern
		// so we get it better from context and find the connection for it 
		Collection<ReplayFilteringConnection> connections;
		try {
			// how to seperate the context from UI and normal Plugin ?? 
			// it seems that we firstly get parameters like configuration from UIPluginContext 
			// then we give it to PluginContext, there we test if the connection already exists.
			connections =  context.getConnectionManager().getConnections(ReplayFilteringConnection.class, context, result);
			for (ReplayFilteringConnection connection : connections) {
				// we use different connections to show
				if ( connection.getObjectWithRole(ReplayFilteringConnection.LABEL).equals(ReplayFilteringConnection.connectOriginlType)
						&& connection.getObjectWithRole(ReplayFilteringConnection.REPLAY_RESULT).equals(result)) {
					rightView.setThreshold((double) connection.getObjectWithRole(ReplayFilteringConnection.FILTER_PARAMETERS));
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		
		
		leftView.setRightView(rightView);
		rightView.setLeftView(leftView);
		
		this.add(this.leftView, new Float(70));
		this.add(this.rightView, new Float(30));
		
		leftView.drawResult();
	}
	
}

class PlaceControlView extends JPanel{
	ShowView leftView;
	double threshold = 80;
	
	boolean chooseDelete = false;

	protected JSlider thresholdSlider;
	protected JLabel thresholdLabel;
	
	protected Color COLOR_BG = new Color(60, 60, 60);
	protected Color COLOR_BG2 = new Color(120, 120, 120);
	protected Color COLOR_FG = new Color(30, 30, 30);
	protected Font smallFont;
	
	// from place control view, we get parameter to delete or color places
	public PlaceControlView() {
		
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setBackground(COLOR_BG2);
		this.setOpaque(true);
		this.setLayout(new BorderLayout());
		
		// right filter panel for threshold input
		JPanel thresholdPanel = new JPanel();
		
		thresholdPanel.setOpaque(false);
		thresholdPanel.setLayout(new BoxLayout(thresholdPanel, BoxLayout.Y_AXIS));
		JLabel nodeSigSliderLabel = new JLabel("Replayable Threshold");
		nodeSigSliderLabel.setFont(this.smallFont);
		nodeSigSliderLabel.setOpaque(false);
		nodeSigSliderLabel.setForeground(COLOR_FG);
		centerHorizontally(nodeSigSliderLabel);
		thresholdPanel.add(nodeSigSliderLabel, BorderLayout.NORTH);
		thresholdLabel = new JLabel("0.000");
		thresholdLabel.setOpaque(false);
		thresholdLabel.setForeground(COLOR_FG);
		thresholdLabel.setFont(this.smallFont);
		thresholdLabel.setText(""+threshold);
		centerHorizontally(thresholdLabel);
		// here we add Label to south, but it shoudl be in JPanel called upperControlPanel, I think !! 
		thresholdPanel.add(packVerticallyCentered(thresholdLabel, 50, 20), BorderLayout.SOUTH);
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
		
		this.add(thresholdPanel, BorderLayout.CENTER);
		
		
		// add for radioButton to combine 
		JPanel colorDeletePanel = new JPanel();
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
		
		this.add(colorDeletePanel, BorderLayout.SOUTH);
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
	PluginContext context;
	Petrinet net;
	int traceNum;
	boolean drawDelete;
	ReplayFilteringResult result;
	
	PlaceControlView rightView;
	
	ProMJGraph graph;
	JComponent graphComponent;
	
	// we accept Petrinet and then create one graph from it 
	public ShowView(PluginContext context, ReplayFilteringResult result) {
		this.context = context;
		this.result = result;
		this.net = result.getFPN();
		this.traceNum = result.getTraceNum();
		this.drawDelete = false;
		this.graphComponent = graphVisualize(false);
		this.add(graphComponent, new Float(100));
	}
	
	public void recreateNet(double percentage) {
		int threshold = (int) (traceNum * percentage*0.01);
		// we add connection to context if it creates another petri net..
		Collection<ReplayFilteringConnection> connections;
		try {
			// how to seperate the context from UI and normal Plugin ?? 
			// it seems that we firstly get parameters like configuration from UIPluginContext 
			// then we give it to PluginContext, there we test if the connection already exists.
			connections =  context.getConnectionManager().getConnections(ReplayFilteringConnection.class, context, net, percentage);
			for (ReplayFilteringConnection connection : connections) {
				// we use different connections to show
				if ( connection.getObjectWithRole(ReplayFilteringConnection.FILTER_PARAMETERS).equals(percentage) 
						&& connection.getObjectWithRole(ReplayFilteringConnection.LABEL).equals(ReplayFilteringConnection.connectOnlineType)
						&& ((Petrinet)(connection.getObjectWithRole(ReplayFilteringConnection.PN))).getLabel().equals(net.getLabel())) {
					this.result =  (ReplayFilteringResult) connection.getObjectWithRole(ReplayFilteringConnection.REPLAY_RESULT);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
	
		NetUtilities.markPlaces(net, threshold);
		// after this, we need to drawGraph
		// this.drawDelete = false;
		context.addConnection(new ReplayFilteringConnection(net, percentage, result));
		drawResult();
	}

	public void drawResult() {
		// TODO we need to differ if it is drawDeleteOnes or the coloredOnes
		this.remove(graphComponent);
		
		this.graphComponent = graphVisualize(drawDelete);
		this.add(graphComponent, new Float(100));
		this.revalidate();
		this.repaint();
	}

	public void setRightView(PlaceControlView rightView) {
		this.rightView = rightView;
	}
	
	public void setDrawDelete(boolean drawDelete) {
		this.drawDelete = drawDelete;
	}
	
	public void constructVisualization(ViewSpecificAttributeMap map, boolean isShowMoveLogModel, boolean isShowMoveModel) {
		graph.getModel().beginUpdate();
		graph.getModel().endUpdate();
		graph.refresh();
		graph.revalidate();
		graph.repaint();
	}
	
	JComponent graphVisualize(boolean drawDelete) {
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
				ScalableComponent tmpGraph = GraphBuilder.buildJGraph(dnet); 
				graph = (ProMJGraph)tmpGraph;
				constructVisualization(graph.getViewSpecificAttributes(), true, true);
			}
		}else {
			colorGraph();
		}
		return graph.getComponent();
	}
	
	private void colorGraph() {
		ScalableComponent tmpGraph = GraphBuilder.buildJGraph(net); 
		graph = (ProMJGraph)tmpGraph;
		constructVisualization(graph.getViewSpecificAttributes(), true, true);
		if(!drawDelete) {
			for (Place p : net.getPlaces()) {
				// if place is bad, then we give it a color.. Maybe we could delete it later
				if ((boolean)p.getAttributeMap().get("isMarked")) {
					// make the mark uniform
					graph.getViewSpecificAttributes().putViewSpecific(p, AttributeMap.FILLCOLOR,java.awt.Color.RED);
				}
			}
		}
	}
		
}
