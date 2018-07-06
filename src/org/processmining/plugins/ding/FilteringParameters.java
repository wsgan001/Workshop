package org.processmining.plugins.ding;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

/**
 * Parameters for the mining of a workshop model from an event log.
 *  ----Event Log files 
 *  –  threshold for filtering event log
	–- filtering type
	---traceNum 
	–- Generated Petrinet 
	---threshold for replay filtering
 * @author kefang ding
 * 
 */
public class FilteringParameters {

	/**
	 * Filter Parameter, it decides which parameter to use;; 
	 * type of Filter + threshold of input value
	 * 
	// if threshold is positive, then it means using the highest one,
	// if it's negative, it uses the lowest one
	// Cumulative frequency, setting 80%, we sort the frequency by descending order 
	// and choose the most highest ones
	// if -20%, it means we choose the cumulative lowest 20% by increasing order. 
	// for selecting certain number of frequency, we choose 3,
	// it means we choose the top 3 variant with highest frequency.
	// -3, means we choose the last 3 variants with lowest frequency. 
	// if it's 0, it means, we choose them all!!!! but for selecting num..
	// if they are bigger than the size, then all;;  
	// information about variants count needed!!!
	// if it's zero??? It means we have empty, which is not allowed!!! 
	 */
	
	private String filterType; 
	private double threshold ;
	private int variantNum;
	
	private AlphaMinerParameters alphaParas;
	XEventClassifier eventClassifier ;
	
	private Petrinet net;
	int traceNum;
	double replayThreshold;
	
	/**
	 * Create default parameter values.
	 */
	public FilteringParameters() {
		filterType = new String("Variant Over One ThresHold");
		threshold = 0.1; 
	}
	
	public FilteringParameters(String filterType,double threshold, AlphaMinerParameters alphaParas ) {
		this.filterType = filterType; 
		this.threshold = threshold; 
		this.alphaParas = alphaParas;
	}
	
	
	public double getReplayThreshold() {
		return replayThreshold;
	}
	
	public void setReplayThreshold(double replayThreshold) {
		this.replayThreshold = replayThreshold;
	}
	
	public int getTraceNum() {
		// TODO Auto-generated method stub
		return traceNum;
	}
	
	public void setTraceNum(int traceNum) {
		this.traceNum = traceNum;
	}
	/**
	 * Set the classifier to the given classifier.
	 * 
	 * @param classifier
	 *            The given classifier.
	 */
	public void setFilter(String filterType, double threshold) {
		if (filterType != null) {
			this.filterType = filterType;
			this.threshold = threshold;
		}
	}
	
	public Object[] getFilter() {
		return new Object[] {filterType, threshold};
	}
	
	public void setFilterType(String filterType) {
		if (filterType != null) {
			this.filterType = filterType;
		}
	}
	
	
	public void setFilterValue( double threshold) {
		this.threshold = threshold;
	}
	
	public void setAlphaParameters(AlphaMinerParameters alphaParas) {
		this.alphaParas = alphaParas;
	}

	public void setEventClassifier(XEventClassifier eventClassifier) {
		// TODO Auto-generated method stub
		this.eventClassifier = eventClassifier;
	}
	
	public XEventClassifier getEventClassifier() {
		return eventClassifier;
	}

	/**
	 * return the filter, Type and threshold in a list?? Why do we need to do it ?? 
	 * could we set the attribute into public ??
	 */
	public String getFilterType() {
		return filterType;
	}
	
	
	public double getThreshold() {
		return threshold;
	}
	
	
	public AlphaMinerParameters getAlphaMinerParameters() {
		return alphaParas;
	}
	
	/**
	 * Returns whether these parameter values are equal to the given parameter
	 * values.
	 * 
	 * @param object
	 *            The given parameter values.
	 * @return Whether these parameter values are equal to the given parameter
	 *         values.
	 */
	public boolean equals(Object object) {
		if (object instanceof FilteringParameters) {
			FilteringParameters parameters = (FilteringParameters) object;
			if (filterType.equals(parameters.filterType) && threshold == parameters.threshold) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the hash code for these parameters.
	 */
	public int hashCode() {
		return filterType.hashCode();
	}

	public int getVariantNum() {
		return variantNum;
	}

	public void setVariantNum(int variantNum) {
		this.variantNum = variantNum;
	}

	public Petrinet getNet() {
		return net;
	}

	public void setNet(Petrinet net) {
		this.net = net;
	}


}
