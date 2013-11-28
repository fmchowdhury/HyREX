package DataProcessor;

import java.util.ArrayList;

import Structures.Entity;
import Structures.Sentence;
import Utility.FileUtility;

public class NewAnnotatedEntityIncluder {
	
	public static void main ( String[] args ) throws Exception {
		
		String path = "../../data/SemEval2013/task-9-ddi/test_data/task9.2/";
		args = (path + "test.ddi2013.full  " + path + "diseases.in.test.DDI2013  "
				+ "DISO   " + path + "withDiso.test.ddi2013.full").split("\\s+");
		
		ArrayList<Sentence> listOfAllSentences = Sentence.readFullData(args[0], "", "", "", false);
		ArrayList<String> listOfAllLines = FileUtility.readNonEmptyFileLines(args[1]);
		
		for ( int i=0; i<listOfAllLines.size(); i++ ) {
			Entity ent = new Entity(args[2], listOfAllLines.get(i));
			
			for ( int s=0; s<listOfAllSentences.size(); s++ ) {
				Sentence objCurSen = listOfAllSentences.get(s);
				
				if ( objCurSen.senID.equals(ent.senID) ) {
					int e = 0;
					for ( ; e<objCurSen.listOfEntities.size(); e++ ) {
						if ( objCurSen.listOfEntities.get(e).hasOverlap(ent) )
							break;
					}
					
					if ( objCurSen.listOfEntities.size() == e ) {	
						ent.id = ent.senID + ".new." + ent.getNEcategory() + ".e" + objCurSen.listOfEntities.size();
						objCurSen.listOfEntities.add(ent);
					}
					break;
				}
			}
		}
		
		for ( int s=0; s<listOfAllSentences.size(); s++ )
			listOfAllSentences.get(s).sortEntitiesByBoundary();
			
		FileUtility.writeInFile(args[3], Sentence.printStringAllSentences(listOfAllSentences), false);
	}

}
