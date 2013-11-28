package DataProcessor;

import java.util.ArrayList;

import Structures.Sentence;
import Utility.CommonUtility;
import Utility.FileUtility;

public class MultiClassData {

	public static void main ( String[] args ) throws Exception {
		new MultiClassData().splitMultiClassData("/media/Study/workspace/HyREX/data_for_Dasha/reace/data/reACE2004/bnews_data/bnews.full");
	}
	
	public void splitMultiClassData ( String fullDataFile ) throws Exception {
		
		ArrayList<Sentence> listOfAllSentences = Sentence.readFullData(fullDataFile, "", "", "", false);
		ArrayList<String> listOfRelTypes = new ArrayList<String>();
		
		for ( int i=0; i<listOfAllSentences.size(); i++ ) {
			for ( int r=0; r<listOfAllSentences.get(i).listRels.size(); r++ ) {
				if ( !listOfRelTypes.contains(listOfAllSentences.get(i).listRels.get(r).type) && listOfAllSentences.get(i).listRels.get(r).isPositive )
					listOfRelTypes.add(listOfAllSentences.get(i).listRels.get(r).type);
			}
		}
		
		for ( int i=0; i<listOfRelTypes.size(); i++ ) {
			FileUtility.createDirectory( CommonUtility.OUT_DIR + "/" + listOfRelTypes.get(i));
		}
		
		String[] str = fullDataFile.split("/");
		
		for ( int t=0; t<listOfRelTypes.size(); t++ ) {
			StringBuilder sb = new StringBuilder();
		
				for ( int i=0; i<listOfAllSentences.size(); i++ ) {
					sb.append(listOfAllSentences.get(i).printString(listOfRelTypes.get(t)));
				}
			
				
			FileUtility.writeInFile( CommonUtility.OUT_DIR + "/" + listOfRelTypes.get(t) + "/" +
					listOfRelTypes.get(t) + "." + str[str.length-1], sb.toString(), false);
		}
	}
	
}
