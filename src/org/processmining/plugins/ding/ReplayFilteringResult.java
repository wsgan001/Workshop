package org.processmining.plugins.ding;

import org.deckfour.xes.model.XLog;
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
	
	XLog flog;
	
	public ReplayFilteringResult() {
		// TODO Auto-generated constructor stub
	}
	public ReplayFilteringResult(Petrinet fnet) {
		this.fnet = fnet;
	}

	public Petrinet getNet() {
		return net;
	}
	public void setNet(Petrinet net) {
		this.net = net;
	}
	public Petrinet getFnet() {
		return fnet;
	}
	public void setFnet(Petrinet fnet) {
		this.fnet = fnet;
	}
	public XLog getFLog() {
		return flog;
	}
	public void setFLog(XLog flog) {
		this.flog = flog;
	}

	
}
