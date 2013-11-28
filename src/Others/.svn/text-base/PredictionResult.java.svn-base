package Others;

import java.io.IOException;
import java.util.ArrayList;

import Utility.CommonUtility;
import Utility.FileUtility;

public class PredictionResult {
	
	public static void main (String[] args) throws IOException{
		
		String mlPredictions = args[0]; 
		String entPairFile = args[1];
					
		ArrayList<String> tempPairs = FileUtility.readNonEmptyFileLines(entPairFile);
		ArrayList<String> tempLinesPred = FileUtility.readNonEmptyFileLines(mlPredictions);
		
		StringBuilder sb = new StringBuilder();
		
		for ( int i=0; i<tempLinesPred.size(); i++ ) {
				
			if ( tempLinesPred.get(i).trim().length() > 0 ) {
				String[] temp = tempLinesPred.get(i).trim().split("\\s+");
					
				if ( temp[2].equals("1") ) {
					sb.append(temp[3] + "\t");
					temp = tempPairs.get(i).trim().split("\\s+");
					sb.append(temp[0] + "\t" + temp[1] + "\n");
				}
			}
		}
				
		FileUtility.writeInFile(CommonUtility.OUT_DIR + "/extracted_relations.txt", sb.toString(), false);			
	}
}
