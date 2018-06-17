package org.processmining.plugins.ding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;

/**
 * This class is used to Removing places in replayable check
 * -- first to read the model and compare it with event
 * 
   -- count the places frequency 
   -- delete the places in the model not so replayable, by adding threshold
   --generate new model:: Now we need to reuse connection and parameters
    More task:  How to show the new generated Petrinet ??
     -- In Alpha Algorithm,there is some marking and it shows the marking..Now we need to find and visualize the graph
        How to accept the threshold from user??  
    
 * @author dkf
 *
 */

@Plugin(
		name = "Filtering Places w.r.t Replayability",
		parameterLabels = {"Event log", "Petri Net"},
		returnLabels = { "Filtered Petri Net"},
		returnTypes = {Petrinet.class},
		userAccessible = true,
		help = "Filtering Places in A Petri Net w.r.t frequency")

public class ReplayFiltering {
	// firstly we need to import a PetriNet into our class, but actually we could have interface
	// which helps me do it
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Kefang Ding", email = "ding@rwth-aachen.de")
	@PluginVariant(variantLabel = "From Petri net and Event Log", requiredParameterLabels = { 0, 1 })
	public Petrinet filteringLog(final UIPluginContext context, Petrinet net, XLog log)
			throws ConnectionCannotBeObtained {
		
		// double percentage = 0.1; // places over percentage could be kept
		
		ReplayFilteringDialog replayDialog = new ReplayFilteringDialog();
		ListWizard<FilteringParameters> listWizard = new ListWizard<FilteringParameters>(replayDialog);
		FilteringParameters params = ProMWizardDisplay.show(context, listWizard, new FilteringParameters());
		
		if (params != null) {
			return replayLog(context, net, log, params); 
		}else {
			System.out.println("No parameters are set... So return original");
			return net;
		}
	}

	private Petrinet replayLog(UIPluginContext context, Petrinet net, XLog log, FilteringParameters params) {
		// could we make it better, maybe an active sliderbar to control the petri net and change its form?? 
		XLogInfo info  = XLogInfoFactory.createLogInfo(log); //

		Collection<Place> places = net.getPlaces();
		// create one map to store its value and then to init it by method
		Map<Place, Integer> placeFreq = initPlaceFreq(places); 
      
        XEventClasses eventClasses = info.getEventClasses();
        // build A Map for Transition in net and the eventClasses
        Map<XEventClass,Transition >  transMap = EventLogUtilities.getEventTransitionMap(eventClasses, net.getTransitions());
        
        // it's like some basic information about the log , so we could put it into one class
        // and use it from it 
        List<TraceVariant> variants =  EventLogUtilities.getTraceVariants(log);
        
        for(int i=0; i<variants.size(); i++) {	
        	countPlaceVariant(variants.get(i), net,transMap, placeFreq);
        }
        
        // create a new PetriNet
 		// Petrinet nnet  = PetrinetFactory.newPetrinet("New Petrinet After Filtering"); // since Petrinet is one interface, so we can't create PatriNet directly from it
        // because after clone,actually the places have already changed, so we can't remove it
 		// like this. We need to compare them first and then remove it..
 		// postprocess here needed: after remove places, there are actually 
 		
 		Petrinet nnet  = PetrinetFactory.clonePetrinet(net);
 		Map<Place, Place> placeMap = EventLogUtilities.getPlaceMap(net.getPlaces(), nnet.getPlaces());
		
 		// filtering places
        int threshold = (int) (info.getNumberOfTraces() * params.getThreshold()*0.01);
        Iterator freqIter = placeFreq.entrySet().iterator();
        while (freqIter.hasNext()) {
            Map.Entry pair = (Map.Entry)freqIter.next();
            if((Integer)(pair.getValue()) < threshold) {
            	nnet.removePlace(placeMap.get((Place)pair.getKey()));

            }
        }
		
		return nnet;
	}
    private void countPlaceVariant(TraceVariant traceVariant, Petrinet net, Map<XEventClass, Transition> transMap, Map<Place, Integer> placeFreq) {
    	// one problem is maybe about the end number, there is no freq for it  
    	int curCount;
    	List<Transition> seq = getTraceSeq(traceVariant, transMap);
    	Transition transition = null;
    	Arc arc = null;
    	Place place = null;
    	Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = null;
    	// deal with it until the last transiton, but we need to condiser once more?? Maybe? 
		for(int i=0; i< seq.size(); i++) {
			transition = seq.get(i);
			preset = net.getInEdges(transition);
			
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : preset) {
				if (edge instanceof Arc) {
					arc = (Arc) edge;
					place = (Place) arc.getSource();
					
					curCount = placeFreq.get(place);
					curCount += traceVariant.getCount(); 
					placeFreq.replace(place, curCount);		
				}
			}
			
		}
		
		// deal with the end transition and give end place count for it 
		preset = net.getOutEdges(transition);
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : preset) {
			if (edge instanceof Arc) {
				arc = (Arc) edge;
				place = (Place) arc.getTarget();
				
				curCount = placeFreq.get(place);
				curCount += traceVariant.getCount(); 
				placeFreq.replace(place, curCount);		
			}
		}
	}

	private List<Transition> getTraceSeq(TraceVariant traceVariant, Map<XEventClass, Transition> transMap) {
		// TODO Auto-generated method stub
		List<XEventClass> traceSeq = traceVariant.getTraceVariant();
		List<Transition> seq = new ArrayList<Transition>();
		for(XEventClass eventClass : traceSeq) {
			seq.add(transMap.get(eventClass));
		}
		return seq;
	}
	
	/**
	 * init the Place Freq it means to give one initial count to each places
	 * @param places
	 * @return 
	 */
	private Map<Place, Integer> initPlaceFreq(Collection<Place> places) {
		Map<Place, Integer> placesFreq= new LinkedHashMap<Place, Integer>();
		Iterator piter = places.iterator();
		while(piter.hasNext()) {
			placesFreq.put((Place) piter.next(), 0);
		}
		return placesFreq;
	}
	

}
