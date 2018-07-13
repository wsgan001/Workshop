package org.processmining.plugins.ding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.alphaminer.plugins.AlphaMinerPlugin;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.ding.ui.FilteringUI;
import org.processmining.plugins.ding.util.EventLogUtilities;
import org.processmining.plugins.ding.util.ReplayPlaces;
import org.processmining.plugins.ding.util.TraceVariant;

/**
 * This plugin is to filter the event log in three different methods.
 * One improvement is: After the filtering could we direcly call the Alpha Algorithm??
 *  
 * Result could be the filtered log and the petri net;
 *   How to achieve it ??
 *     -- How to make it into the Alpha Algorithm?? 
 *     -- return value    return new Object[] { net, m } :: TO check out the difference of Petrinet and marking 
 *   Maybe firstly we need to get an SVN version for current project
 * Add connection control of this filtering 
 *   Done with it 
 *   
 * 14 June 2018 :: Try to connect to Alpha Plugin and see it but 
 *   at firstly let's create the UI
 *   Then connect it with Plugin
 *   
 * @author dkf
 *
 */
// parameterLabels should be more than the required parameters in the PluginMehtods, 
// userAccessible to show UI for user

@Plugin(
		name = "Alpha Miner with Filtering",
		parameterLabels = {"Event log", "Petrinet"}, 
		returnLabels = { "Filtered Resultl", "Petrinet", "Filtered Log"},
		returnTypes = {FilteringAlphaMinerResult.class, Petrinet.class, XLog.class}, 
		// one problem, how to show the other output files?? Consider later.. 
		userAccessible = true,
		help = "Plugin on Improvement of Alpha Miner: \n"
		  +" Firstly to filter the event log adn then generate model \n"
		  + "Replay the event log on the model to inspect places status\n"
		  + "Result includes colored petri net , filtered peteri net and filtered event log\n")

public class FilteringAlphaMiner {
	
	/*
	 * Add one more method, use more parameters for input parameters it works
	 * But then later, we need to have online control, so we need to get the result with net and event log into it
	 */

	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "Replay Log with Petri net",  requiredParameterLabels = { 0, 1})
	public Object[] replayAlphaMiner(UIPluginContext context, XLog log, Petrinet net) {
		Collection<FilteringAlphaMinerConnection> connections;
		try {
			connections =  context.getConnectionManager().getConnections(FilteringAlphaMinerConnection.class, context, log);
			for (FilteringAlphaMinerConnection connection : connections) {
				if ( connection.getObjectWithRole(FilteringAlphaMinerConnection.LABEL).equals(FilteringAlphaMinerConnection.connectGenerateModelType)
						&&connection.getObjectWithRole(FilteringAlphaMinerConnection.LOG).equals(log)
						&& ((FilteringParameters)connection.getObjectWithRole(FilteringAlphaMinerConnection.PN)).getAlphaMinerParameters().equals(net) 
						) {
					return connection.getObjectWithRole(FilteringAlphaMinerConnection.RESULT);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		
		FilteringAlphaMinerResult result = new FilteringAlphaMinerResult(); 
		result.setNet(net);
		result.setFLog(log);
		
		context.addConnection(new FilteringAlphaMinerConnection(log, null, result));
		return new Object[] {result,  result.getNet(), result.getFLog()};
	}
	
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "Mining after Filtering Log",  requiredParameterLabels = { 0})
	public Object[] filterAlphaMiner(UIPluginContext context, XLog log) {
		
		FilteringUI filteringUI = new FilteringUI();
		FilteringParameters parameters = filteringUI.getConfiguration(context, log);
		if (parameters == null) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		// here we don't need to get the connection ?? No we need to get the connection
		Collection<FilteringAlphaMinerConnection> connections;
		try {
			connections =  context.getConnectionManager().getConnections(FilteringAlphaMinerConnection.class, context, log);
			for (FilteringAlphaMinerConnection connection : connections) {
				if ( connection.getObjectWithRole(FilteringAlphaMinerConnection.LABEL).equals(FilteringAlphaMinerConnection.connectGenerateModelType)
						&&connection.getObjectWithRole(FilteringAlphaMinerConnection.LOG).equals(log)
						&& ((FilteringParameters)connection.getObjectWithRole(FilteringAlphaMinerConnection.FILTER_PARAMETERS)).getAlphaMinerParameters().equals(parameters.getAlphaMinerParameters()) 
						&& ((FilteringParameters)connection.getObjectWithRole(FilteringAlphaMinerConnection.FILTER_PARAMETERS)).getFilter().equals(parameters.getFilter())) {
					return connection.getObjectWithRole(FilteringAlphaMinerConnection.RESULT);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		
		Object[] midResults =  filteringEventLogParameter(context, log, parameters);
		// setting parameter for the replay filtering
		parameters.setNet((Petrinet) midResults[0]);
		// after this we should show the direct view of it  
		FilteringAlphaMinerResult result = new FilteringAlphaMinerResult(); 
		result.setNet((Petrinet) midResults[0]);
		result.setFLog((XLog) midResults[1]);
		// here for test data..
		ReplayPlaces.netReplayState(context, result.getNet(), log, parameters);
		
		context.addConnection(new FilteringAlphaMinerConnection(log, parameters, result));
		return new Object[] {result,  result.getNet(), result.getFLog()};
	}
	
	
	public Object[] filteringEventLogParameter(PluginContext context, XLog log, FilteringParameters parameters) {
		//check if there is already such connection, if so, we return directly the values
		
		XLog filtered_log = filtering(context, log, parameters);
		Petrinet net =null; 
		// Marking marking = null;
		
		// we need to put the AlphaMining on this data
		// apply(context, log, wizStep.getEventClassifier(), params);
		Object[] markedNet = AlphaMinerPlugin.apply(context, filtered_log, parameters.getEventClassifier(),
				parameters.getAlphaMinerParameters());
		
		if (context.getProgress().isCancelled()) {
			context.getFutureResult(0).cancel(true);
			return new Object[] { null, null };
		}
		
		net = (Petrinet)markedNet[0];
		// we create map here
		if(parameters.getInfo()==null) {
			XLogInfo info  = XLogInfoFactory.createLogInfo(log); 
			parameters.setInfo(info);
		}
		
		XEventClasses eventClasses = parameters.getInfo().getEventClasses(parameters.getEventClassifier());
		Map<XEventClass,Transition >  transMap = EventLogUtilities.getEventTransitionMap(eventClasses, net.getTransitions());
		parameters.setMap(transMap);		
		
		return new Object[] { net, filtered_log};
	}
	
	
	public XLog filtering(PluginContext context, XLog log, FilteringParameters parameters) {
		// parameters.setFilterType("Choose Top N Vairants");
		// System.out.println(parameters.getFilterType());
		// System.out.println(parameters.getThreshold());
		// we have different parameters and do threee filtering
		XLog filtered_log = null;
		List<TraceVariant> variants = null;
		//here before we do filtering, we need to get the variants information of the event log
		
		// I will put it into Parameters for the next operation and then generate the Map for it
		if(parameters.getInfo()==null) {
			XLogInfo info  = XLogInfoFactory.createLogInfo(log); 
			parameters.setInfo(info);
		}
		
		if(parameters.getVariants() == null) {
			variants =  EventLogUtilities.getTraceVariants(log, parameters);
			parameters.setVariants(variants);
		}
		variants = parameters.getVariants();
		
		int traceNum = parameters.getInfo().getNumberOfTraces();
		parameters.setTraceNum(traceNum);
		parameters.setVariantNum(variants.size());
		
		if(parameters.getFilterType().equals("Variant Over One ThresHold")) {
			filtered_log= filterOverEachVariants(log, variants, parameters);
		}else if(parameters.getFilterType().equals("Cumulative Frequency Over One Threshold")) {
			filtered_log = filterCumulativeVariants( log, variants, parameters);
		}else if(parameters.getFilterType().equals("Choose Top N Vairants")) {
			filtered_log = filterTopVariants( log, variants, parameters); // 
		}else {
			filtered_log = log;
		}
		return filtered_log;
	}
	
	/**
	 * Filter event log and keep the top ones
	 * @param log
	 * @param variants
	 * @param threshold, if threshold is +n, then we choose the variants with the highest counts;
	 *       if it's -n, then we choose the variants with lowest counts.
	 * @return filtered log
	 */
	private XLog filterTopVariants( XLog log, List<TraceVariant> variants, FilteringParameters parameters) {
		List<TraceVariant> keptVariants = null;
		// we need to sort variants at first 
		Collections.sort(variants, TraceVariant.COMPARE_BY_COUNT);
		// min to make sure it's not over the size in positive, max to make sure not over on the negative side.
		int int_threshold = (int) (Math.max(-variants.size(), Math.min(parameters.getThreshold(), parameters.getVariantNum())));
		if(int_threshold > 0) {
			// we choose the top ones
			keptVariants = variants.subList(variants.size() - int_threshold, variants.size());
		}else if(int_threshold < 0) {
			// we choose the lowest ones , but need testing to find the right format
			keptVariants = variants.subList(0, -int_threshold);
		}else if(int_threshold == 0) {
			System.out.println("We don't choose data and go back??? ");
		}
		
		XLog nlog = filterByTrace(log, keptVariants,parameters);
		return nlog;
	}

	private XLog filterCumulativeVariants( XLog log, List<TraceVariant> variants, FilteringParameters parameters) {
		
		// we allow negative and positive threshold
		int threshold = (int) (parameters.getTraceNum()* parameters.getThreshold());
		
		// This need some cumulation on the counts columns and we need to give it
		// we need to sort variants at first 
		Collections.sort(variants, TraceVariant.COMPARE_BY_COUNT);
		
		int idx = 0, sum=0;
		List<TraceVariant> keptVariants = null; 
		if(threshold > 0) {
			// we choose the top ones
			idx = variants.size() - 1;
			sum = variants.get(idx).getCount();

			while(sum < threshold && idx >0 ) {
				idx -= 1;
				sum += variants.get(idx).getCount();
			}
			// now it's right position and we get the keptVariants, don't know if we need to use idx -1 or not
			keptVariants = variants.subList(idx, variants.size());
			
		}else if(threshold < 0) {
			// we choose the lowest ones , but need testing to find the right format
			idx = 0;
			sum = variants.get(idx).getCount();
			

			while(sum < -threshold && idx < variants.size()-1) {
				idx += 1;
				sum += variants.get(idx).getCount();
			}
			// now it's right position and we get the keptVariants, don't know if we need to use idx -1 or not
			keptVariants = variants.subList(0, idx);
		}
		// after this we get the variants which is in an ascending order.  
		// from last element we add it up to i and when the sum(i) > threshold and sum(i-1) < threshold,
		// we choose until i-1		
		XLog nlog = filterByTrace(log, keptVariants,parameters);
		return nlog;
	}
    /**
     * it will adjust the threshold of it and then apply into log
     * @param log
     * @param variants
     * @param threshold
     * @return filtered log with kept variants
     */
	private  XLog filterOverEachVariants(XLog log, List<TraceVariant> variants, FilteringParameters parameters) {
		
		int threshold = (int) (parameters.getTraceNum()* parameters.getThreshold());
		
		List<TraceVariant> keptVariants = new ArrayList<TraceVariant>();
		for(int index =0; index<variants.size(); index++) {
			if( variants.get(index).getCount() >= threshold) {
				keptVariants.add(variants.get(index));
			}
		}
		XLog nlog = filterByTrace(log, keptVariants, parameters);
		return nlog;
	}	
	
	
	/*
	 * filter xlog and keep the Xtrace in the traceVariants left.
	 * Better to use it should be like only to use traceVariants
	 */
	private XLog filterByTrace(XLog log, List<TraceVariant> keptVariants, FilteringParameters parameters) {
		// traverse the xlog and compare if it is into the set of traceVariants?? 
		// if it is contained , then keep it in the new log file
		// else, ignore it
		XEventClass eventClass = null;
		// here is some time wasted, cause the clone and then clear, maybe we could just 
		XLog nlog = new XLogImpl((XAttributeMap) log.getAttributes().clone());
		// XLog nlog= (XLog) log.clone();
		// nlog.clear();
		
		for (XTrace trace : log) {
			List<XEventClass> traceClass = new ArrayList<XEventClass>();
			for (XEvent toEvent : trace) {
				eventClass = parameters.getInfo().getEventClasses(parameters.getEventClassifier()).getClassOf(toEvent);
				traceClass.add(eventClass);	
				
			}
			for(int i=0;i<keptVariants.size();i++) 
				if(keptVariants.get(i).getTraceVariant().equals(traceClass)) {
					nlog.add((XTrace)trace.clone());
					break;
				}
			
		}
		return nlog;
	}

}
