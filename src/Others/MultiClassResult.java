package Others;

import java.util.ArrayList;

import Utility.DataStrucUtility;
import Utility.FileUtility;

public class MultiClassResult {

	public static void main ( String[] args ) throws Exception {
		
		String origPath = "results_tune_esu/ace2004";
		String[] arrLabels = FileUtility.getFileNamesFromDir(origPath);
		
		for ( int i=0; i<arrLabels.length; i++ ) {
			String path = origPath + "/" + arrLabels[i] + "/out/";
			String predOutFile = path + "best.base.stat.in", entPairFile = path + "entPairFileName_TK";
			new MultiClassResult().readPredictions( predOutFile, entPairFile, arrLabels[i]);
			
			if ( listEntPairs.isEmpty() )
				System.out.println(arrLabels[i] + " : No output found\n");
			else
				new MultiClassResult().getResult(arrLabels[i]);
		}
	}
	
	static ArrayList<String> listEntPairs = new ArrayList<String>();
	static ArrayList<String> listPredClassLabels = new ArrayList<String>();
	static ArrayList<String> listOrigClassLabels = new ArrayList<String>();
	static ArrayList<Double> listBestScores = new ArrayList<Double>();
	
	/**
	 * 
	 * @param predOutFile
	 * @param entPairFile
	 * @param curLabel
	 */
	public void readPredictions ( String predOutFile, String entPairFile, String curLabel ) {
		ArrayList<String> tempPairs = FileUtility.readNonEmptyFileLines(entPairFile);
		ArrayList<String> tempLinesPred = FileUtility.readNonEmptyFileLines(predOutFile);
		
		if ( tempPairs.isEmpty() || tempLinesPred.isEmpty() )
			return;
		
		for ( int i=0; i<tempLinesPred.size(); i++ ) {
			String[] temp = tempPairs.get(i).trim().split("\\s+");
			String str = temp[0] + "\t" + temp[1];
			
			int k = listEntPairs.indexOf(str);
			if ( k<0 ) {
				listEntPairs.add(str);
				listPredClassLabels.add("");
				listOrigClassLabels.add("");
				listBestScores.add(-1000.0);
				k = listEntPairs.size()-1;
			}
			
			if ( tempLinesPred.get(i).trim().length() > 0 ) {
				temp = tempLinesPred.get(i).trim().split("\\s+");
			
				if ( (temp[0].equals("1.0") && temp[2].equals("1"))
						|| (temp[0].equals("0.0") && temp[2].equals("0")) ) {
					listOrigClassLabels.set(k, curLabel);
				}
				
				if ( temp[2].equals("1") ) {
					double score = Double.valueOf(temp[3]);
					
					if ( score > listBestScores.get(k) )
						listPredClassLabels.set(k, curLabel);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param curLabel
	 */
	public void getResult ( String curLabel ) {
		
		double TP =0, FP =0, FN=0;
		
		for( int i=0; i<listEntPairs.size(); i++ ) {
			if ( listPredClassLabels.get(i).equals(listOrigClassLabels.get(i)) && listPredClassLabels.get(i).equals(curLabel) )
				TP++;
			else {
				if ( listOrigClassLabels.get(i).equals(curLabel) )
					FN++;
				else if ( listPredClassLabels.get(i).equals(curLabel) )
					FP++;
			}	
		}
		
		double recall = DataStrucUtility.roundTwoDecimals(TP*100/(TP+FN)), precision = DataStrucUtility.roundTwoDecimals(TP*100/(TP+FP));
		//System.out.println( curLabel + "\nTP: " + TP + " FP: " + FP + " FN: " + FN + "\n" +
			//	"P: " + precision + " R: " + recall + " F: " + TextUtility.roundTwoDecimals(2*precision*recall/(precision+recall))
				//+ "\n");
		
		System.out.println( curLabel + "\n" + TP + "\n" + FP + "\n" + FN + "\n" +
					 precision + "\n" + recall + "\n" + DataStrucUtility.roundTwoDecimals(2*precision*recall/(precision+recall))
					+ "\n");
	}
}
