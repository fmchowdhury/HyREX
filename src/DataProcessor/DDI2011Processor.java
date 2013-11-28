package DataProcessor;

import java.util.ArrayList;

import Structures.Relation;
import Structures.Sentence;
import Utility.FileUtility;

public class DDI2011Processor {
	
	public static void main ( String[] args ) throws Exception {
		
		String path = "/media/Study/data/DDIExtraction2011/";
		args = (path + "test.full  " + path + "Solution_prediction_Unified").split("\\s+");
		
		ArrayList<Sentence> listOfAllSentences = Sentence.readFullData(args[0], "", "", "", false);
		ArrayList<String> listOfAllLines = FileUtility.readNonEmptyFileLines(args[1]);
		
		ArrayList<Sentence> listOfTempSen = new ArrayList<Sentence>();
		ArrayList<String> listOfSenIds = new ArrayList<String>();
		for ( int i=0; i<listOfAllLines.size(); i++ ) {
			String[] str = listOfAllLines.get(i).split("\\s+");
			String senId = str[0].substring(0, str[0].lastIndexOf("."));
			int index = listOfSenIds.indexOf(senId);
			Sentence newSen = new Sentence();
			newSen.senID = senId;
			if ( index >= 0 )
				newSen = listOfTempSen.get(index);
			
			Relation newRel = new Relation( str[0], str[1], str[2].equals("0") ? false : true, "interaction", "");
			newSen.listRels.add(newRel);
			
			if ( index >= 0 )
				listOfTempSen.set(index, newSen);
			else {
				listOfTempSen.add(newSen);
				listOfSenIds.add(senId);
			}
		}
		
		Relation.printRelPolarity = true;
		
		for ( int s=0; s<listOfAllSentences.size(); s++ ) {
		
			Sentence objCurSen = listOfAllSentences.get(s);
			objCurSen.listRels.clear();
			
			int index = listOfSenIds.indexOf(objCurSen.senID);
			
			System.out.println(listOfAllSentences.get(s).listRels.size());
			if ( index >= 0 ) {
				listOfAllSentences.get(s).listRels = listOfTempSen.get(index).listRels;
				System.out.println(listOfAllSentences.get(s).printString());
			}
			System.out.println(listOfAllSentences.get(s).listRels.size());
			
		}
			
		FileUtility.writeInFile(args[0], Sentence.printStringAllSentences(listOfAllSentences), false);
	}

}
