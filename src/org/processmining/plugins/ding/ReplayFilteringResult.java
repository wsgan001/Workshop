package org.processmining.plugins.ding;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;

/**
 * Model the result I get from replay filtering
 * @author dkf
 * 29.06.2018
 */
public class ReplayFilteringResult {
	// first original Petri net
	Petrinet net;
	// then filtered Petrinet, this is to get the control from that part 
	Petrinet fnet;
	int traceNum;
	double threshold;
	
	public double getThreshold() {
		return threshold;
	}
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	public ReplayFilteringResult(Petrinet fnet) {
		this.fnet = fnet;
	}
	public ReplayFilteringResult(Petrinet fnet, int traceNum) {
		this.fnet = fnet;
		this.traceNum = traceNum;
	}
	public void setFPN(Petrinet fnet) {
		this.fnet = fnet;
	}
	
	public Petrinet getFPN() {
		return fnet;
	}

	public int getTraceNum() {
		// TODO Auto-generated method stub
		return traceNum;
	}
	
	public void setTraceNum(int traceNum) {
		this.traceNum = traceNum;
	}
}
