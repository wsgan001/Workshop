package org.processmining.plugins.ding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Traverse one Event Log", returnLabels = { "Filtered Log" }, returnTypes = { XLog.class}, parameterLabels = {"Log" }, userAccessible = true)
public class TraverseLog { //  WorkshopModel.class , // ,"Paramters"
/*
 *  read one EventLog and traverse it and filter it??? By some traces
 *  we add it as a plugIn files later
 */
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Kefang Ding", email = "kfding@gmail.com")
	@PluginVariant(variantLabel = "Traverse the event log", requiredParameterLabels = { 0 })
	public XLog traverse(PluginContext context, XLog log) { // , WorkshopMiningParameters parameters
		// print out information of log
		// trace and other info
		XLogInfo info  = XLogInfoFactory.createLogInfo(log); //
		XLog nlog = null;
		// we have gotten the first effect of it... 
		//Then we need to print the result into the screen of ProM
		XEventClass eventClass = null;
		
		List<Integer> traceVariantCounts = new ArrayList<Integer>();
		List<List<Integer>> traceVariants = new ArrayList<List<Integer>>();
		// but we need to record the variants of all traces
		int count = 0;
	
		for (XTrace trace : log) {
			
			System.out.println(trace.getAttributes().values());
			
			List<Integer> toTraceClass = new ArrayList<Integer>();
			for (XEvent toEvent : trace) {
				eventClass = info.getEventClasses().getClassOf(toEvent);
				toTraceClass.add(eventClass.getIndex());	
				// System.out.println("Event Classifier now: "+eventClass.getId()+" " + eventClass.getIndex());
			}
			
			int i = 0;
			for(; i< traceVariants.size();i++) {
				if(traceVariants.get(i).equals(toTraceClass)) {
					traceVariantCounts.set(i, traceVariantCounts.get(i) + 1);
					break;
				}
			}
			if (i==traceVariants.size()) {
				// not found in it, then we need to add it into the list
				traceVariants.add(count, toTraceClass);
				traceVariantCounts.add(count, 1);
				count ++;
			}	
				
			/*
			 * Advance the progress bar.
			 */
			context.getProgress().inc();
		}
		/*
		// we should print out the variants and their counts
		for(int index =0; index<traceVariants.size(); index++) {
			System.out.println(traceVariants.get(index));
			System.out.println(traceVariantCounts.get(index));
		}
		*/
		// maybe the threshold is kind of complex, cause we need to define all the data above one data
		// check on the paper that defines the frequency threshold
		int threshold = (int) (info.getNumberOfTraces() * 0.1) ;
		
		/*
		if(threshold <= 20) {
			System.out.println("No enough data, no filter");
			return log;
		} 
		*/
		List<Integer> traceIndex = getTraceToFilter(traceVariantCounts, threshold);
		List<List<Integer>> keptTraceVariants = new ArrayList<List<Integer>>();
		
		for (int index =0 ; index < traceIndex.size(); index ++) {
			keptTraceVariants.add(traceVariants.get(traceIndex.get(index)));
			System.out.println(keptTraceVariants.get(index));
		}
		
		System.out.println("the totoal num of variants " + keptTraceVariants.size()) ;
		nlog = filterByTrace(log, keptTraceVariants);
		return nlog;
	}
	
	/*
	 * @param: traceVariantsCounts 
	 * @return back the index of traces variants to filter
	 * 
	 */
	private List<Integer> getTraceToFilter(List<Integer> traceVariantsCounts, int threshold){
		List<Integer> traceIndex = new ArrayList<Integer>(); 
		for(int index =0; index<traceVariantsCounts.size(); index++) {
			if( traceVariantsCounts.get(index) >= threshold) {
				traceIndex.add(index);
			}
		}
		return traceIndex;
	} 
	
	/*
	 * filter xlog and keep the Xtrace in the traceVariants left.
	 * Better to use it should be like only to use traceVariants
	 */
	private XLog filterByTrace(XLog log, List<List<Integer>> keptTraceVariants) {
		// traverse the xlog and compare if it is into the set of traceVariants?? 
		// if it is contained , then keep it in the new log file
		// else, ignore it
		XLogInfo info  = XLogInfoFactory.createLogInfo(log); 
		XEventClass eventClass = null;
		// XLog nlog = XFactory.createLog(log.getAttributes());
		
		XLog nlog= (XLog) log.clone();
		nlog.clear();
		
		for (XTrace trace : log) {
			List<Integer> traceClass = new ArrayList<Integer>();
			for (XEvent toEvent : trace) {
				eventClass = info.getEventClasses().getClassOf(toEvent);
				traceClass.add(eventClass.getIndex());	
				// System.out.println("Event Classifier now: "+eventClass.getId()+" " + eventClass.getIndex());
			}
			if (keptTraceVariants.contains(traceClass)) {
				nlog.add((XTrace)trace.clone());
			}
		}
		return nlog;
	}
	/*
	 * getDupCandidate in one trace, return duplicate task in one trace:: but not help with whole building, right.. 
	 * But loops could be in form, how to say, maybe a little changed, like relationship of c||d
	 * Anyway the result is used for Alpha Algorithm, it could handle parallel events  
	 * If there are two loops, we get the maximimal one 
	 * e.g. a b b c : B is repeated 
	 *   a B c d B d c e  : B is repeated
	 *   here we should use traceVariants , and we have already created it before
	 * Candidates in one trace: 
	 *   event repeated and not in one loop
	 *   
	 *   for each variant, we test if there is some repeated tasks
	 */
	private List<XEvent> getDupCandidates(List<List<Integer>> traceVariants) {
		
		for (List<Integer> tvar : traceVariants) {
			
			// find repeated sequence in the trace
			// we go through the  
			
		}
		
		return null;
	}
	/*
	 * find all repeated events sequence in one trace 
	 * sequence but different. like eg. we should get  b c d and  e f   
	 * a b c d b c d e f e f g. 
	 * 1 2 3 4 2 3 4 5 6 5 6 7
	 * Input are trace variant in integer
	 * 
	 * Another problem is to get sets of them, it means, 
	 *  if they are parallel executed, it have different order, but belong to one loop
	 *  
	 *  I just need to find the length of 2 and loop of lenght of 1.
	 *  Anyway, write it down, maybe for fun at first.
	 */
	private List<Integer> findRepeatedSubSeq(List<Integer> tvar){
		return null;
	}
	
	private Map<List<Integer>, Integer> findLoopUnder2(List<Integer> tvar, int loopLen){
		// we check loop of length 2;; ... abab...see two before, 
		// if they are the same, then we mark it
		Map<List<Integer>, Integer> loopEvents = null;
		int oldIdx = 0, idx=loopLen;
		while( idx< tvar.size() - loopLen) {
			oldIdx = idx - loopLen;
			if(tvar.subList(oldIdx, oldIdx+loopLen).equals(tvar.subList(idx,idx + loopLen))) {
				idx += loopLen;
			}else {
				// but if there exists one match
				// if not repeated again, then we put in into result; 
				// 1: it happens not again, 2: it happens later for same repeatition.
				// we don't care about 2, and put it then check if there is already one in it
				// if()
				
			}
		}
		
		// for loop over 2 no problem for alpha algorithm
		
		// return the only the subsequence of it, or we also mark the position?? 
		// with position, it should use map.
		return null;
	}
}
