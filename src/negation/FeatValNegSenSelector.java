package negation;

import java.util.ArrayList;

public class FeatValNegSenSelector {

	public boolean classLabel = false;
	

	 /* if the sentence has only two drugs then whether the negation cue is on the before/between/after of the pairs.
	 * immediate governor of the negation
	 * the nearest verb governor of the negation without being connected by a "conj"
	 * is the verb governor main verb/root of the sentence
	 * are all drug mentions dependent on that verb governor
	 * are all but one drug mentions dependent on that verb governor
	 * is the negation preceded by "although" in the same clause
	 * is there any drug mentions after the negation inside the sentence
	 */ 
	
	public boolean has2MentionOfInterest = false,
			hasLessThan2MentionOfInterest = false,
			allMentionOfInterestOnRight = false,
			allMentionOfInterestOnLeft = false; 
	
	public int wiOfNearestVerbGovernor = -1,
		wiOfImmediateGovernor = -1,
		wiOfNegation = -1,
		charIndxOfNegation = -1;
	
	public boolean isVerbGovernorRoot = false,
			
			areAllMentionOfInterestDependOnVerbGovernor = false,			
			areAllButOneMentionOfInterestDependOnVerbGovernor = false,
			
			areAllMentionOfInterestDependOnImmeidateGovernor = false,			
			areAllButOneMentionOfInterestDependOnImmeidateGovernor = false,
					
			isAlthoughPrecedeNegationInSameClause = false,
			
			hasAnyMentionOfInterestOnRight = false,
			hasCommaBeforeNextMentionOfInterest = false,
			hasCommaAfterPrevMentionOfInterest = false,
			sentenceHasBut = false,
			immediateGovernorIsVerbGovernor = false,
			containsAlone = false;
	
	public String negWord = "",
		   immediateGovernor = "",
		   nearestVerbGovernor = "",
		   senID = "";	
	
	public ArrayList<String> listOfKeyWordsFound = new ArrayList<String>();
	public ArrayList<Integer> listOfWIofKeyWords = new ArrayList<Integer>();
	
	public ArrayList<String> listOfWordsBefore = new ArrayList<String>();
	public ArrayList<String> listOfWordsAfter = new ArrayList<String>();
}
