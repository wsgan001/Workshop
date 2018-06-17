package org.processmining.plugins.ding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
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
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.ding.ui.FilteringUI;

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
		name = "Filtering Frequent Event log",
		parameterLabels = {"Event log"}, // ,"FilteringParameters"
		returnLabels = { "Petrinet", "Marking", "filtered_log"},
		returnTypes = {Petrinet.class, Marking.class, XLog.class},
		userAccessible = true,
		help = "Plugin for filtering frequent event log")

public class FilteringFrequentEventLog {
	
	/*
	 * After calculating the variants of traces in log
	 * we decide to use different filtering to filter data.
	 * We don't need to show the data distribution, because we could see it at beginning
	 * 
	 * we filter the data according to different filtering criterion. 
	 */

	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "Mining after Filtering Log",  requiredParameterLabels = { 0})
	public Object[] filteringEventLogDefault(UIPluginContext context, XLog log) {
		
		// we could do test on parameters and see if it is used to filter events or traces
		// here we need to use the wizard in ProM and create a sequence of parameters
		
		FilteringUI filteringUI = new FilteringUI();
		FilteringParameters parameters = filteringUI.getConfiguration(context, log);
		if (parameters == null) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		// but just to keep it, both for filtered data, and then output the filtered log 
		// the return value is a problem here , it can't accept null values..
		return filteringEventLogParameter(context, log, parameters);
	}
	

	// @UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	// @PluginVariant(variantLabel = "Mining after Filtering Logged ",  requiredParameterLabels = { 0, 1})
	public Object[] filteringEventLogParameter(PluginContext context, XLog log, FilteringParameters parameters) {
		//check if there is already such connection, if so, we return directly the values
		Collection<FilteringConnection> connections;
		try {
			// how to seperate the context from UI and normal Plugin ?? 
			// it seems that we firstly get parameters like configuration from UIPluginContext 
			// then we give it to PluginContext, there we test if the connection already exists.
			connections =  context.getConnectionManager().getConnections(FilteringConnection.class, context, log);
			for (FilteringConnection connection : connections) {
				if (connection.getObjectWithRole(FilteringConnection.LOG).equals(log)
						&& connection.getObjectWithRole(FilteringConnection.FILTERPARAMETERS).equals(parameters)) {
					return (Object[]) connection.getObjectWithRole(FilteringConnection.FILTEREDLOG);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		
		XLog filtered_log = filtering(context, log, parameters);
		// save filtered log
		if(parameters.isSaveFile()) {
			try {
				EventLogUtilities.exportSingleLog(filtered_log, parameters.getSaveFileName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Petrinet net =null; 
		Marking marking = null;
		if(!parameters.isNoPetrinet()) {
			// we need to put the AlphaMining on this data
			// apply(context, log, wizStep.getEventClassifier(), params);
			Object[] markedNet = AlphaMinerPlugin.apply(context, filtered_log, parameters.getEventClassifier(),
					parameters.getAlphaMinerParameters());
			
			net = (Petrinet)markedNet[0];
			marking = (Marking)markedNet[1];
			
		}
		// here we need more thing to say..
		if (context.getProgress().isCancelled()) {
			context.getFutureResult(0).cancel(true);
			return new Object[] { null, null };
		}
		context.addConnection(new FilteringConnection(log, net, marking,  filtered_log, parameters));
		return new Object[] { net, marking, filtered_log};
	}
	
	
	public XLog filtering(PluginContext context, XLog log, FilteringParameters parameters) {
		// parameters.setFilterType("Choose Top N Vairants");
		// System.out.println(parameters.getFilterType());
		// System.out.println(parameters.getThreshold());
		// we have different parameters and do threee filtering
		XLog filtered_log = null;
		
		//here before we do filtering, we need to get the variants information of the event log
		List<TraceVariant> variants =  EventLogUtilities.getTraceVariants(log);
		
		if(parameters.getFilterType().equals("Variant Over One ThresHold")) {
			filtered_log= filterOverEachVariants(log, variants, parameters.getThreshold());
		}else if(parameters.getFilterType().equals("Cumulative Frequency Over One Threshold")) {
			filtered_log = filterCumulativeVariants( log, variants, parameters.getThreshold());
		}else if(parameters.getFilterType().equals("Choose Top N Vairants")) {
			filtered_log = filterTopVariants( log, variants, parameters.getThreshold()); // 
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
	private XLog filterTopVariants( XLog log, List<TraceVariant> variants, double threshold) {
		List<TraceVariant> keptVariants = null;
		// we need to sort variants at first 
		Collections.sort(variants, TraceVariant.COMPARE_BY_COUNT);
		// min to make sure it's not over the size in positive, max to make sure not over on the negative side.
		int int_threshold = (int) (Math.max(-variants.size(), Math.min(threshold, variants.size())));
		if(int_threshold > 0) {
			// we choose the top ones
			keptVariants = variants.subList(variants.size() - int_threshold, variants.size());
		}else if(int_threshold < 0) {
			// we choose the lowest ones , but need testing to find the right format
			keptVariants = variants.subList(0, -int_threshold);
		}else if(int_threshold == 0) {
			System.out.println("We don't choose data and go back??? ");
		}
		
		XLog nlog = filterByTrace(log, keptVariants);
		return nlog;
	}

	private XLog filterCumulativeVariants( XLog log, List<TraceVariant> variants, double percentage) {
		
		XLogInfo info  = XLogInfoFactory.createLogInfo(log);
		int threshold = (int) (info.getNumberOfTraces() * percentage);
		
		// This need some cumulation on the counts columns and we need to give it
		// we need to sort variants at first 
		Collections.sort(variants, TraceVariant.COMPARE_BY_COUNT);
		// after this we get the variants which is in an ascending order.  
		// from last element we add it up to i and when the sum(i) > threshold and sum(i-1) < threshold,
		// we choose until i-1
		int idx = variants.size() - 1;
		int sum = variants.get(idx).getCount();
		
		while(sum < threshold && idx >0 ) {
			idx -= 1;
			sum += variants.get(idx).getCount();
		}
		// now it's right position and we get the keptVariants, don't know if we need to use idx -1 or not
		List<TraceVariant> keptVariants = variants.subList(idx, variants.size());
		
		XLog nlog = filterByTrace(log, keptVariants);
		return nlog;
	}
    /**
     * it will adjust the threshold of it and then apply into log
     * @param log
     * @param variants
     * @param threshold
     * @return filtered log with kept variants
     */
	private  XLog filterOverEachVariants(XLog log, List<TraceVariant> variants, double percentage) {
		XLogInfo info  = XLogInfoFactory.createLogInfo(log);
		XLog nlog = null;
		
		int threshold = (int) (info.getNumberOfTraces() * percentage);
		
		List<TraceVariant> keptVariants = new ArrayList<TraceVariant>();
		for(int index =0; index<variants.size(); index++) {
			if( variants.get(index).getCount() >= threshold) {
				keptVariants.add(variants.get(index));
			}
		}
		nlog = filterByTrace(log, keptVariants);
		return nlog;
	}	
	
	
	/*
	 * filter xlog and keep the Xtrace in the traceVariants left.
	 * Better to use it should be like only to use traceVariants
	 */
	private XLog filterByTrace(XLog log, List<TraceVariant> keptVariants) {
		// traverse the xlog and compare if it is into the set of traceVariants?? 
		// if it is contained , then keep it in the new log file
		// else, ignore it
		XLogInfo info  = XLogInfoFactory.createLogInfo(log); 
		XEventClass eventClass = null;
		// here is some time wasted, cause the clone and then clear, maybe we could just 
		XLog nlog = new XLogImpl((XAttributeMap) log.getAttributes().clone());
		// XLog nlog= (XLog) log.clone();
		// nlog.clear();
		
		for (XTrace trace : log) {
			List<XEventClass> traceClass = new ArrayList<XEventClass>();
			for (XEvent toEvent : trace) {
				eventClass = info.getEventClasses().getClassOf(toEvent);
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
