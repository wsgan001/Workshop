package org.processmining.plugins.ding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.processmining.plugins.ding.util.EventLogUtilities;
import org.processmining.plugins.ding.util.TraceVariant;

/**
 * I know I'm close to it, but I don't like to discuss it further. The task is not so clearly defined for me
 * We need to remove places that are not replayable in more than x% of the cases. 
 * Input: Petri net and Event log
 *    -- Place Not replayable in one trace it means ?? 
 *    -- Then extend it to all the cases.
 *    -- Is it hard to me to understand the others ?? Then learn it !!!
 * 
 * @author dkf  22 June 2018
 *
 */


@Plugin(
		name = "Filtering Places 2nd Versionw.r.t Replayability",
		parameterLabels = {"Event log", "Petri Net"},
		returnLabels = { "Filtered Petri Net"},
		returnTypes = {Petrinet.class},
		userAccessible = true,
		help = "Filtering Places in A Petri Net w.r.t frequency")
public class ReplayPlaces {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Kefang Ding", email = "ding@rwth-aachen.de")
	@PluginVariant(variantLabel = "From Petri net and Event Log", requiredParameterLabels = { 0, 1 })
	public Petrinet filteringByReplay(final UIPluginContext context, Petrinet net, XLog log)
			throws ConnectionCannotBeObtained {
		
		ReplayFilteringDialog replayDialog = new ReplayFilteringDialog();
		ListWizard<FilteringParameters> listWizard = new ListWizard<FilteringParameters>(replayDialog);
		FilteringParameters params = ProMWizardDisplay.show(context, listWizard, new FilteringParameters());
		
		if (params != null) {
			Petrinet nnet  = PetrinetFactory.clonePetrinet(net);
			return filteringNet(context, nnet, log, params); 
		}else {
			System.out.println("No parameters are set... So return original");
			return net;
		}
	}
	
	private Petrinet filteringNet(UIPluginContext context, Petrinet net, XLog log, FilteringParameters params) {
		// we need to add some connnection here
		Set<PetrinetNode> nodes = net.getNodes();
		// first we get the variants from Event log
		XLogInfo info  = XLogInfoFactory.createLogInfo(log); //
		
		Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> arcs = net.getEdges();
		Collection<Place> places = net.getPlaces();
		// Map<Place, Integer> placeFreq = initPlaceFreq(places); 
		initPlaceFreq(places);
        XEventClasses eventClasses = info.getEventClasses();
        // should we hide it, then we don't need to see it so often?? 
        Map<XEventClass,Transition >  transMap = EventLogUtilities.getEventTransitionMap(eventClasses, net.getTransitions());
        
        List<TraceVariant> variants =  EventLogUtilities.getTraceVariants(log);
        
        // for each trace we count the freq of each Arcs, which means we need to build one freq for Arcs
        for(int i=0; i<variants.size(); i++) {	
        	countPlaceFreq(variants.get(i), net, transMap);
        }
        
     // after it, we compare them to threshold
        int threshold = (int) (info.getNumberOfTraces() * params.getThreshold()*0.01);
    
 		// from here we need to change it,the thing we need actully is Petrinet Edge and consider relation ship about 
 		// the Node and Edge to Event log
        Iterator iter = places.iterator();
        while (iter.hasNext()) {
        	// pair<Arc, count>
            Place place = (Place)iter.next();
            // should we count the threshold for one place, it's like ..
            // unFitNum /(fitNum + unFitNum) > threshold, then we delete it..Else keep it here
            // if we use total trace, it seems that fitNum + unFitNum are same
            // not equal for the end place.. SO something is missed...
            int totalNum = ((int)(place.getAttributeMap().get("unFitNum")) + (int)(place.getAttributeMap().get("fitNum")));
            if(totalNum != info.getNumberOfTraces()) {
            	System.out.println("Not equal for the total num and count " + place.getLabel());
            } 
           
            if((Integer)(place.getAttributeMap().get("unFitNum")) > threshold) {
            	// net.removePlace(place);
            	// firstly we just color the place.. How could we add some color to places??? and then draw them out???
            	// so we need to make one attribute into it
            	place.getAttributeMap().put("isMarked", true);
            	net.removePlace(place);
            }
        }
		// check the Petrinet and remove all isolate transitions and places
        // resetPetrinet(net);
		return net;
	}
	
	
	/**
	 * we remove bad places and create the new ones
	 * @param net
	 */
	private void resetPetrinet(Petrinet net) {
		// if places are marked, then we color and delete them. The easist way is to delete them at first step 
		// to delete places:: 
		//  --- if they are connected with transitions
		
	}

	private void countPlaceFreq(TraceVariant traceVariant, Petrinet net, Map<XEventClass, Transition> transMap) {
		// Count the unfit Freq of places in Petri net, maybe also the fitted number.
		// Whatever we don't need actually the map from Place to its Frequency.
		int curCount  = traceVariant.getCount();
		List<Transition> seq = getTraceSeq(traceVariant, transMap);
		
    	Transition transition = null;
    	Arc arc = null;
    	Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = null;
    	Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = null;
    	
    	// for every place we need to check with all traces. Even if it doesn't fit and we can continue the next check.
    	// set a token at first place... Na, we need to check it from another code
    	// for every place, we just need to give bad mark once, missing or remain
    	// missing is bad, remaining how to count. With loop, if it remains and it works better
    	// we see it's fine. But missing needs counting, and then remaining parts
    	
    	// 
    	
    	Place splace = EventLogUtilities.getStartPlace(net);
    	// should we put splace one token already, or we just wait until we see the next transition?? 
    	// if it's what I understand, how should I do for the next steps?? 
    	// One place is not replayable, it means when it should be in the path, it isn't there. 
    	// when it shouldn't be on the trace, it is there. Both situation we need to consider
    	// also the best path to go through the model,like optimal alignment in it
    	splace.getAttributeMap().put("token", 1);
    	// the token at first place could remain..
    	// boolean fit = true;
		// first transition if it connects the initial place
    	for(int i=0; i< seq.size(); i++) {
			// we check from the second element in the sequence and then compare the elements at previous places
			// one benefit maybe record if connections at one place is single ?? 
			transition = seq.get(i);
			
			preset = net.getInEdges(transition);
			// we need to see two transitions together???  
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : preset) {
				arc = (Arc) edge;
				// get the prior place for transition
				Place p= (Place) arc.getSource();
				// we can't make sure all the events show in an order. So if it doesn't, then 
				// we don't have the number of tnum
				if(p.getAttributeMap().get("token") == null || (Integer)p.getAttributeMap().get("token") < 1 ) {
					p.getAttributeMap().put("isMissing", true);
					p.getAttributeMap().put("token", 1);
				}
				int tnum = (Integer)p.getAttributeMap().get("token");
				// token remaining...
				if(tnum > 1) {
					p.getAttributeMap().put("isRemaining", true);
				}
				p.getAttributeMap().put("token", 0);
				// we need to test it here. If there is some remaining, then we need to mark it
			}
			
			// we need to generate the token for the next places
			// it will generate token for places if it executes
			postset = net.getOutEdges(transition);
			// we need to see two transitions together???  
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : postset) {
				arc = (Arc) edge;
				// get the prior place for transition
				Place p= (Place) arc.getTarget();
				// it could have more tokens here, so we need to make sure 
				if(p.getAttributeMap().get("token") == null) 
					// if there is no token, we add it simply 1
					p.getAttributeMap().put("token", 1 );
				else
					// if it has been assigned before, then we could get it directly
					p.getAttributeMap().put("token", (Integer)p.getAttributeMap().get("token") + 1);
			}
			
		}
    	// do we consider about the last token?? Actually we don't consider it, also should we kind of remove the added attribute??
    	// token based, we need to have such transition..
    	splace = EventLogUtilities.getEndPlace(net);
    	// it lacks token, so problem here
    	if(splace.getAttributeMap().get("token") != null) {
			int tnum = (Integer)splace.getAttributeMap().get("token");
			if(tnum > 1) {
				splace.getAttributeMap().put("isRemaining", true);
			}
			splace.getAttributeMap().put("token", 0);
    	}
    	// then after this trace, how to count the unFitNum of place?? 
		// if it is missing, we count it directly, but if token remains, we see it unfit ???
		Iterator piter = net.getPlaces().iterator();
		while(piter.hasNext()) {
			Place place = (Place) piter.next();
			// first this place doesn't show in the path
			if(place.getAttributeMap().get("token") == null) {
				place.getAttributeMap().put("fitNum",  (Integer)place.getAttributeMap().get("fitNum")+ curCount);
			}
			if(place.getAttributeMap().get("isRemaining") == null || ((Integer)place.getAttributeMap().get("token") == 0 && place.getAttributeMap().get("isMissing") == null)) {
				// place is used and fitModel..
				place.getAttributeMap().put("fitNum", (Integer)place.getAttributeMap().get("fitNum")+ curCount);
			}else {
				// token remaining, do we need to add some attribute to signal it ?? 
				// It is fine.. Maybe like this.. but what about if it unfit twice, so one is missing, and one is remaining??
				// after one remaining, then is missing for some place, how to recover it back??
				place.getAttributeMap().put("unFitNum", (Integer)place.getAttributeMap().get("unFitNum")+ curCount);
			}
			// after one trace, we should set the token in each place into null ,just to delete them..
			place.getAttributeMap().remove("token");
			place.getAttributeMap().remove("isMissing");
			place.getAttributeMap().remove("isRemaining");
		}
		
	}

	private void initPlaceFreq(Collection<Place> places) {
		Iterator piter = places.iterator();
		while(piter.hasNext()) {
			Place place = (Place) piter.next();
			// but after each trace, we need to clear token again and assign the new ones. 
			// or we just should to test if it has, it it has they we do it , if not, dont do it
			place.getAttributeMap().put("fitNum", 0);
			place.getAttributeMap().put("unFitNum", 0);
		}
	}
	
	private void initPlaceAttribute(Collection<Place> places) {
		Iterator piter = places.iterator();
		while(piter.hasNext()) {
			Place place = (Place) piter.next();
			// but after each trace, we need to clear token again and assign the new ones. 
			if(! place.getAttributeMap().containsKey("token")) {
				// if it is initialization, it has no "token" symbol
				place.getAttributeMap().put("token", 0);
				place.getAttributeMap().put("isBad", false);
				// place.getAttributeMap().put("isRemaining", false);
			}else {
				// after one trace, it has token. we remove all place attributes
				place.getAttributeMap().remove("token");
				place.getAttributeMap().remove("isBad");
				//place.getAttributeMap().remove("isRemaining");
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
	
}
