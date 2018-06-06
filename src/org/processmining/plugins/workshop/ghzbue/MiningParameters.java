package org.processmining.plugins.workshop.ghzbue;
/*
Parameters for a mining a workshop model from an event log.
@author ghzbue
 */

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;

public class MiningParameters
{
	/*
	 Classifier parameter. This determines which classifier will be used during the mining.
	 */
	private XEventClassifier classifier;
	
	
	/*
	 Using default parameter values.
	 */
	public MiningParameters()
	{
		classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
	}
	
	/*
	 Using a given Classifier.
	 @param classifier
	 	the given classifier
	 */
	public MiningParameters(XEventClassifier classifier)
	{
		if(classifier !=null)
		{
			this.classifier = classifier;
		}
	}
	
	/*
	 get the classifier.
	 @return the classifier
	 */
	public XEventClassifier getClassifier()
	{
		return classifier;
	}


	/*
	 Return whether these parameter values are equal to given parameter values.
	 
	 
	 @param Object
	  		the given parameter values.
	  		
	 @return whether these parameter values are equal to given parameter values.
	 */
	public boolean equals(Object obj)
	{
		if(obj instanceof XEventClassifier)
		{
			MiningParameters params = (MiningParameters) obj;
			if(classifier.equals(params.classifier))
			{
				return true;
			}
		}
		return false;
	}
	
	/*
	 Return the hash code for these parameters.
	 */
	public int hashCode()
	{
		return classifier.hashCode();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
