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
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.plugins.ding.ReplayFilteringResult;
import org.processmining.plugins.ding.util.NetUtilities;

import com.fluxicon.slickerbox.ui.SlickerRadioButtonUI;
import com.fluxicon.slickerbox.ui.SlickerSliderUI;

@Plugin(name = "Show Places Colored", parameterLabels = { "Petrinet" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
public class ReplayFilteringVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, ReplayFilteringResult result) {
		
		return new ReplayMainView(context, result.getFPN());
	}

}

class ReplayMainView extends JPanel{
	ShowView leftView;
	PlaceControlView rightView;
	
	public ReplayMainView(PluginContext context, Petrinet net) {
		
		// RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS);
		
		// setLayout(new BorderLayout());
		
		leftView = new ShowView(context, net);
		rightView = new PlaceControlView(context);
		
		leftView.setRightView(rightView);
		rightView.setLeftView(leftView);
		
		this.add(this.leftView, new Float(70));
		this.add(this.rightView, new Float(30));
		
		leftView.drawResult(false);
	}
	
}

class PlaceControlView extends JPanel{
	ShowView rightView;
	double threshold;
	
	boolean chooseDelete = false;

	protected JSlider thresholdSlider;
	protected JLabel thresholdLabel;
	
	protected Color COLOR_BG = new Color(60, 60, 60);
	protected Color COLOR_BG2 = new Color(120, 120, 120);
	protected Color COLOR_FG = new Color(30, 30, 30);
	protected Font smallFont;
	
	// from place control view, we get parameter to delete or color places
	public PlaceControlView(PluginContext context) {
		
		// right filter panel for threshold input
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setBackground(COLOR_BG2);
		this.setOpaque(true);
		this.setLayout(new BorderLayout());
		JLabel nodeSigSliderLabel = new JLabel("Replayable Threshold");
		nodeSigSliderLabel.setFont(this.smallFont);
		nodeSigSliderLabel.setOpaque(false);
		nodeSigSliderLabel.setForeground(COLOR_FG);
		centerHorizontally(nodeSigSliderLabel);
		this.add(nodeSigSliderLabel, BorderLayout.NORTH);
		thresholdLabel = new JLabel("0.000");
		thresholdLabel.setOpaque(false);
		thresholdLabel.setForeground(COLOR_FG);
		thresholdLabel.setFont(this.smallFont);
		centerHorizontally(thresholdLabel);
		this.add(packVerticallyCentered(thresholdLabel, 50, 20), BorderLayout.SOUTH);
		thresholdSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 0);
		thresholdSlider.setUI(new SlickerSliderUI(thresholdSlider));
		
		thresholdSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if (e.getSource() == thresholdSlider) {
					// updateThresholdSlider();
					threshold = thresholdSlider.getValue();
					thresholdLabel.setText(""+threshold/1000.0);
					if (thresholdSlider.getValueIsAdjusting() == false) {
						// we need to recall the
						// how to wait it until it doesn't change and then pass the value
					}
				}
				
			}
		});
		thresholdSlider.setOpaque(false);
		thresholdSlider.setToolTipText("<html>The lower this value, the more<br>"
				+ "events are shown increasing the detail <br>" + "and complexity of the model.</html>");
		this.add(thresholdSlider, BorderLayout.CENTER);
		
		
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
					chooseDelete = false;
					rightView.drawResult(chooseDelete);	
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
					chooseDelete = true;
					rightView.drawResult(chooseDelete);
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

	
	public void setLeftView(ShowView rightView) {
		this.rightView = rightView;
	}
	
}
class ShowView extends JPanel{
	PluginContext context;
	Petrinet net;
	
	PlaceControlView rightView;
	
	ProMJGraph graph;
	JComponent graphComponent;
	
	// we accept Petrinet and then create one graph from it 
	public ShowView(PluginContext context, Petrinet net) {
		this.context = context;
		this.net = net;
		
		this.graphComponent = graphVisualize(false);
		this.add(graphComponent, new Float(100));
	}
	
	public void drawResult(boolean drawDelete) {
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
			ScalableComponent tmpGraph = GraphBuilder.buildJGraph(dnet); 
			graph = (ProMJGraph)tmpGraph;
			
			constructVisualization(graph.getViewSpecificAttributes(), true, true);
			
		}else {
		
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
		return graph.getComponent();
	}
		
}
