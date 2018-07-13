package org.processmining.plugins.ding.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.plugins.ding.FilteringParameters;
import org.processmining.plugins.ding.ReplayFilteringConnection;
import org.processmining.plugins.ding.ReplayFilteringDialog;
import org.processmining.plugins.ding.ReplayFilteringResult;

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

public class ReplayPlaces {
	
	public ReplayFilteringResult filteringByReplay(final UIPluginContext context, Petrinet net, XLog log)
			throws ConnectionCannotBeObtained {
		
		ReplayFilteringDialog replayDialog = new ReplayFilteringDialog();
		ListWizard<FilteringParameters> listWizard = new ListWizard<FilteringParameters>(replayDialog);
		FilteringParameters params = ProMWizardDisplay.show(context, listWizard, new FilteringParameters());
		
		if (params != null) {
			// Petrinet nnet  = NetUtilities.clone(net);
			Petrinet nnet  = PetrinetFactory.clonePetrinet(net);
			return filteringNet(context, nnet, log, params);
		}else {
			System.out.println("No parameters are set... So return original");
			return new ReplayFilteringResult(net);
		}
	}
	
	public static void markPlaces(Petrinet net, int threshold) {
		// markPlaces it needs,.maybe before it is not marked, and we mark it again
		Collection<Place> places = net.getPlaces();
        Iterator iter = places.iterator();
        int fnum = 0;
        while (iter.hasNext()) {
        	// pair<Arc, count>
            Place place = (Place)iter.next();
            // to test the totoalNum and the trace numbers
            /** After test they are the same, so no need to test them ..
            int totalNum = ((int)(place.getAttributeMap().get("unFitNum")) + (int)(place.getAttributeMap().get("fitNum")));
            if(fnum!=0 && totalNum != fnum ) {
            	fnum = totalNum;
            	System.out.println("at place "+ place.getLabel()+" we have numer "+ totalNum);
            }
            */
            if((Integer)(place.getAttributeMap().get("unFitNum")) > threshold) {
            	// some thing here is wrong, I set threshold 20%, all places are wrong
            	// 80 two places are wrong. but 90 it change... 
            	// we need to debug this error.
            	// remove places that are not replayable in more than 20% cases. 
            	place.getAttributeMap().put("isMarked", true);
            }else {
            	place.getAttributeMap().put("isMarked", false);
            }
        }
	}
	// two ways to separate the programs, one is to get sammary info and it could be used later
	// one it to accept the threshold and then only work on that.. For me, I'd like to choose 
	// get summary at first and then threshold later
	public static  ReplayFilteringResult filteringNet(PluginContext context, Petrinet net, XLog log, FilteringParameters params) {
		
		Collection<ReplayFilteringConnection> connections;
		try {
			// how to seperate the context from UI and normal Plugin ?? 
			// it seems that we firstly get parameters like configuration from UIPluginContext 
			// then we give it to PluginContext, there we test if the connection already exists.
			connections =  context.getConnectionManager().getConnections(ReplayFilteringConnection.class, context, log, net, params);
			for (ReplayFilteringConnection connection : connections) {
				if (connection.getObjectWithRole(ReplayFilteringConnection.LOG).equals(log)
						&& connection.getObjectWithRole(ReplayFilteringConnection.FILTER_PARAMETERS).equals(params.getThreshold())) {
					return (ReplayFilteringResult) connection.getObjectWithRole(ReplayFilteringConnection.REPLAY_RESULT);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		
		netReplayState(context, net, log, params);
        // after it, we compare them to threshold
		XLogInfo info  = XLogInfoFactory.createLogInfo(log); //
		int traceNum = info.getNumberOfTraces();
		
		int threshold = (int) (traceNum * params.getThreshold()*0.01);
		ReplayPlaces.markPlaces(net, threshold);
		ReplayFilteringResult result = new ReplayFilteringResult(net);
		if (context.getProgress().isCancelled()) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		// how could we add connection again?? so threshold is 0-100
		context.addConnection(new ReplayFilteringConnection(log, net, params.getThreshold(),result));
		return result; 
	}
	
	// store the fit and unfitNum into petri net
	public static void  netReplayState(PluginContext context, Petrinet net, XLog log, FilteringParameters parameters) {
		
		Collection<Place> places = net.getPlaces();
		// Map<Place, Integer> placeFreq = initPlaceFreq(places); 
		initPlaceFreq(places);
        
        // should we hide it, then we don't need to see it so often?? 
        // the easier way is to put transMap into Filtering Parameter..
		Map<XEventClass,Transition >  transMap = parameters.getMap();
        List<TraceVariant> variants =  parameters.getVariants();
        
        // variants we could reuse and then 
        // for each trace we count the freq of each Arcs, which means we need to build one freq for Arcs
        for(int i=0; i<variants.size(); i++) {
        	initPlaceAttribute(places);
        	countPlaceFreq(variants.get(i), net, transMap);
        }
        
        
	}

	public static void countPlaceFreq(TraceVariant traceVariant, Petrinet net, Map<XEventClass, Transition> transMap) {
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
    	Place splace = NetUtilities.getStartPlace(net);
    	splace.getAttributeMap().put("token", 1);
    	// if the first event doesn't connect to splace, so we define the place bad ones..
    	Place eplace = NetUtilities.getEndPlace(net);
    	
    	transition = seq.get(0);
    	if(net.getArc(splace, transition) == null) {
    		// there exists no connection from first event to start place, so splace is bad
    		splace.getAttributeMap().put("isBad", true);
    	}
    	
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
				// not enough token in one place before transition
				int tnum = (Integer)p.getAttributeMap().get("token");
				if(!(tnum == 1 && !(boolean) p.getAttributeMap().get("isBad"))) {
					p.getAttributeMap().put("isBad", true);	
					// now it misses one token, we check if this place has some silent transitions leads to it 
					// collection<Transition,Node> I prefer Node get 
					// silentNodes = getSilentNodes(place);
					// if it's empty, so we mark place bad,
					// if not, we begin to check one node 
					//  this node has places before, while it finds one token there, 
					// this place is fine, so no token is missing
					// no token, check the place has silent nodes, if so trace back
					// if not we choose other ones.. 
					/*
					Collection<PetrinetNode> silentNodes = findSilentNodes(p, net);
					if(silentNodes.size() == 0) {
						p.getAttributeMap().put("isBad", true);
					}else {
						
					}
					*/
				}
				p.getAttributeMap().put("token", tnum -1 < 0 ? 0:tnum -1);
			}
			
			// we need to generate the token for the next places
			// it will generate token for places if it executes
			postset = net.getOutEdges(transition);
			// we need to see two transitions together???  
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : postset) {
				arc = (Arc) edge;
				// get the prior place for transition
				Place p= (Place) arc.getTarget();
				if(p.equals(eplace) && i < seq.size()-1) {
					p.getAttributeMap().put("isBad", true);	
				}
				p.getAttributeMap().put("token", (Integer)p.getAttributeMap().get("token") + 1);
			}
			
		}
    	// do we consider about the last token?? Actually we don't consider it, also should we kind of remove the added attribute??
    	// token based, we need to have such transition..
    	// Place eplace = NetUtilities.getEndPlace(net);
    	// it lacks token, so problem here, if end transition happens twice, so we have remaning token, it's fine
        // but what about the end transition happens once and then other left to execute it ?? 
    	// anyway if it is the end transition, it goes to end, let me see other situation.. 
    	// how about the silent transition in the model???  We shoud discover it...
    	// if we just accept the trace with only concrete transiton, so how about place with silent transiton?? 
    	// we get the token in the next place, and then goes into the model, 
    	// if 
    	int tnum = (Integer)eplace.getAttributeMap().get("token");
    	eplace.getAttributeMap().put("token", tnum -1);
    	
    	// then after this trace, how to count the unFitNum of place?? 
		// if it is missing, we count it directly, but if token remains, we see it unfit ???
		Iterator piter = net.getPlaces().iterator();
		while(piter.hasNext()) {
			Place place = (Place) piter.next();
			// first this place doesn't show in the path
			if((Integer)place.getAttributeMap().get("token") == 0  && !(boolean) place.getAttributeMap().get("isBad")) {
				place.getAttributeMap().put("fitNum",  (Integer)place.getAttributeMap().get("fitNum")+ curCount);
			}else {
				place.getAttributeMap().put("unFitNum", (Integer)place.getAttributeMap().get("unFitNum")+ curCount);
			}
		}
		
	}

	public static Collection<PetrinetNode> findSilentNodes(Place p,Petrinet net) {
		// how to find the previus transition of one place
		Collection<PetrinetNode>  result = new HashSet<>();
		
		Collection<Transition> nodes = net.getTransitions();
		Iterator piter = nodes.iterator();
		while(piter.hasNext()) {
			Transition node = (Transition) piter.next();
			if(node.isInvisible() && net.getArc(node, p) != null ) {
				// how to set the Label for silent transiton in Petri net
				result.add(node);
			}
		}
		
		return result;
	}
	
	public static void initPlaceFreq(Collection<Place> places) {
		Iterator piter = places.iterator();
		while(piter.hasNext()) {
			Place place = (Place) piter.next();
			// but after each trace, we need to clear token again and assign the new ones. 
			// or we just should to test if it has, it it has they we do it , if not, dont do it
			place.getAttributeMap().put("fitNum", 0);
			place.getAttributeMap().put("unFitNum", 0);
			place.getAttributeMap().put("isMarked", false);
		}
	}
	
	public static void initPlaceAttribute(Collection<Place> places) {
		Iterator piter = places.iterator();
		while(piter.hasNext()) {
			Place place = (Place) piter.next();
			// but after each trace, we need to clear token again and assign the new ones. 
			place.getAttributeMap().put("token", 0);
			place.getAttributeMap().put("isBad", false);
			
		}
	}
	
	
	private static List<Transition> getTraceSeq(TraceVariant traceVariant, Map<XEventClass, Transition> transMap) {
		// Could we do it maybe a way directly to get it and not show it here.
		List<XEventClass> traceSeq = traceVariant.getTraceVariant();
		List<Transition> seq = new ArrayList<Transition>();
		for(XEventClass eventClass : traceSeq) {
			seq.add(transMap.get(eventClass));
		}
		return seq;
	}
	
	// if all places under threshold 0 fits, return 1, else 0
	public static boolean fitNet(Petrinet net) {
		
		Iterator piter = net.getPlaces().iterator();
		while(piter.hasNext()) {
			Place place = (Place) piter.next();
			if((boolean) place.getAttributeMap().get("isMarked")) {
				return false;
			}
		
		}
		return true;
	}
}
