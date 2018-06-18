package org.processmining.plugins.ding;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * This class includes the basic information about Event log 
 * and provide utilities for operation
 * @author dkf
 *
 */
public class EventLogUtilities {

	
	public static List<TraceVariant> getTraceVariants( XLog log) {
		// TODO Auto-generated method stub
		
		List<TraceVariant> variants = new ArrayList<TraceVariant>();
		XLogInfo info  = XLogInfoFactory.createLogInfo(log); //
		XEventClass eventClass = null;
		for (XTrace trace : log) {
				
				List<XEventClass> toTraceClass = new ArrayList<XEventClass>();
				for (XEvent toEvent : trace) {
					eventClass = info.getEventClasses().getClassOf(toEvent);
					toTraceClass.add(eventClass);	
				}
				
				int i = 0;
				for(; i< variants.size();i++) {
					if((variants.get(i).getTraceVariant()).equals(toTraceClass)) {
						variants.get(i).addCount(1);
						break;
					}
				}
				if (i==variants.size()) {
					// not found in it, then we need to add it into the list
					variants.add(new TraceVariant(toTraceClass,1));
				}	
			}
		return variants;
	} 
	
	/**
     * we build one map between XEventClasses and Transtitions in Petri Net
     * @param eventClasses
     * @param transitions
     * @return
     */
	public static Map<XEventClass, Transition> getEventTransitionMap(XEventClasses classes,
			Collection<Transition> transitions) {
		Map<XEventClass, Transition> map = new HashMap<XEventClass, Transition>();
		
		for (Transition transition : transitions) {
			boolean visible=false;
			for (XEventClass eventClass : classes.getClasses()) {
				if (eventClass.getId().equals(transition.getAttributeMap().get(AttributeMap.LABEL))) {
					map.put(eventClass, transition);
					visible=true;
				}
			}
			if(!visible){
				transition.setInvisible(true);
			}
		}
		return map;
	}

	public static Map<Place, Place> getPlaceMap(Collection<Place> fromPlaces, Collection<Place> toPlaces) {
		// create a Place Map from clonable Petri net and see if it works
		Map<Place, Place> placeMap = new HashMap<Place, Place>();
		Iterator fiter = fromPlaces.iterator();
		Iterator titer ;
		Place fromP, toP;
		while(fiter.hasNext()) {
			fromP = (Place)fiter.next();
			titer = toPlaces.iterator();
			while(titer.hasNext()) {
				toP = (Place)titer.next();
				if(fromP.getLabel().equals(toP.getLabel())) {
					placeMap.put(fromP, toP);
					break;
				}
			}
		}
		return placeMap;
	}
   
	public static void exportSingleLog(XLog log, String targetName) throws IOException {
		FileOutputStream out = new FileOutputStream(targetName);
		XSerializer logSerializer = new XesXmlSerializer();
		logSerializer.serialize(log, out);
		out.close();
	}
}
