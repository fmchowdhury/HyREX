package Structures;

import java.util.ArrayList;

import Kernels.TKOutputGenerator;
import Utility.*;

public class Triggers {

	public static ArrayList<Character> listOf1stChar = new ArrayList<Character>();
	public static ArrayList<ArrayList<String>> listOfTriggers = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> listOfTriggerLemmas = new ArrayList<ArrayList<String>>();
	
	public static ArrayList<String> listOfNegativeWords = new ArrayList<String>();
	
	/**
	 * 
	 */
	public static void readTriggersAndNegativeWord () {
		
		if ( !listOf1stChar.isEmpty() )
			return;
		
		listOfNegativeWords.add("no");
		listOfNegativeWords.add("not");
		listOfNegativeWords.add("neither");
		listOfNegativeWords.add("without");
		listOfNegativeWords.add("lack");
		listOfNegativeWords.add("lacks");
		listOfNegativeWords.add("lacked");
		listOfNegativeWords.add("fail");
		listOfNegativeWords.add("fails");
		listOfNegativeWords.add("failed");
		listOfNegativeWords.add("unable");
		listOfNegativeWords.add("abrogate");
		listOfNegativeWords.add("abrogates");
		listOfNegativeWords.add("abrogated");
		listOfNegativeWords.add("absent");
		listOfNegativeWords.add("absence");
		listOfNegativeWords.add("prevent");
		listOfNegativeWords.add("prevented");
		listOfNegativeWords.add("prevents");
		listOfNegativeWords.add("unlikely");
		listOfNegativeWords.add("unchanged");
		listOfNegativeWords.add("rarely");
		
		
		ArrayList<String> listTemp = new ArrayList<String>();
		
		if ( !TextUtility.isEmptyString(TKOutputGenerator.triggerFileName) )
			listTemp = FileUtility.readNonEmptyFileLines(TKOutputGenerator.triggerFileName);
		
		ArrayList<String> listOfTriggers = new ArrayList<String>();
		ArrayList<String> listOfLemmas = new ArrayList<String>();
		
		String lastLemma = "";
		
		for ( int i=0; i<listTemp.size(); i++ ) {
			String[] str = listTemp.get(i).split("\\s+");
			
			if ( str.length > 1 )
				lastLemma = str[1];
			// assign the lemma of the previous entry to the current entry is no lemma is found
			else
				str = new String[] { str[0], lastLemma };
				
			int ind = listOfTriggers.indexOf(str[0]);
			if ( ind < 0 ) {
				listOfTriggers.add(str[0]);
				listOfLemmas.add(str[1]);
			}
		}
		
		// populate trigger list
		for ( int i=0; i<listOfTriggers.size(); i++ ) {
			String word = listOfTriggers.get(i);
			int tgIndex = Triggers.listOf1stChar.indexOf(word.charAt(0));
			
			if ( tgIndex < 0 ) {
				Triggers.listOf1stChar.add(word.charAt(0));
				Triggers.listOfTriggers.add(new ArrayList<String>());
				Triggers.listOfTriggerLemmas.add(new ArrayList<String>());
				tgIndex = Triggers.listOfTriggers.size()-1;
			}
			
			Triggers.listOfTriggers.get(tgIndex).add(word);
			Triggers.listOfTriggerLemmas.get(tgIndex).add(listOfLemmas.get(i));				
		}
	}
}
