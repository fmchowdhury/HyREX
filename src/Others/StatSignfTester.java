package Others;

import java.io.IOException;
import java.util.ArrayList;

import Utility.Common;
import Utility.FileUtility;

import org.apache.commons.math3.stat.inference.*;

public class StatSignfTester {

	static ArrayList<String> listPref = new ArrayList<String>(), listBase = new ArrayList<String>(),
	listGold = new ArrayList<String>();

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main ( String[] args ) throws Exception{
		
		String path_base = "ddi_results/out_final_without_filter", path_preferred = "ddi_results/out_final_filter_test";
		
		String goldFile = path_base + "/gold.stat.in",
		baseFile = path_base + "/best.base.stat.in", 
		preferredFile = path_preferred + "/best.base.stat.in";
	
	String prefEntPairsFile = path_preferred + "/entPairFileName_TK", 
		baseEntPairsFile = path_base + "/entPairFileName_TK",
		allEntPairsFile = path_base + "/entPairFileName_TK";
			
		StatSignfTester obj = new StatSignfTester();
		//*
		// read goal predictions for all relation pairs used in experiments 
		obj.readEntiresForGoldFile(baseFile);
		//obj.readEntiresForGoldFile(preferredFile);
		
		// read all relation pairs used in experiments 
		obj.insertEntriesForPairsThatAreLeftOut(preferredFile, prefEntPairsFile, allEntPairsFile);
		obj.insertEntriesForPairsThatAreLeftOut(baseFile, baseEntPairsFile, allEntPairsFile);
		
		preferredFile = preferredFile + ".rev";
		baseFile = baseFile + ".rev";
		/*/
		obj.readEntiresForGoldFile(preferredFile);
		obj.createOutputFileBasePredData(baseFile, baseEntPairsFile, prefEntPairsFile);
		baseFile = baseFile + ".new";		
		obj.readEntiresForGoldFile(baseFile);
		//*/
		
		obj.createStatTestInputFiles(goldFile, baseFile, preferredFile);
		
		args = new String[]{goldFile+".t", baseFile+".t", preferredFile+".t", "1000", "0.01"};
		//*
		
		org.itc.irst.tcc.sre.util.ApproximateRandomizationProcedure.main(args);
		//*/

		
		
	//	obj.performTTest();
	}
	
	
	
	private void performTTest() throws Exception{
		
		double[] arrBase = new double[listBase.size()], arrPref = new double[listPref.size()];
		/*
		for ( int i=0; i<listBase.size(); i++ ){
			if ( listBase.get(i).contains("-1") && listPref.get(i).contains("-1") ) {
				listBase.remove(i);
				listPref.remove(i);
				i--;
			}
		}
		*/
		
		for ( int i=0; i<listBase.size(); i++ ){
			arrBase[i] = Double.valueOf(listBase.get(i));
		}
		
		for ( int i=0; i<listPref.size(); i++ ){			
			arrPref[i] = Double.valueOf(listPref.get(i));
		}
		
		TTest clsTTest = new TTest();
		double p_value = clsTTest.tTest( arrBase, arrPref)/2;
		
		if ( p_value < 0.05 )
			System.out.println( "p-value : " + p_value + "   (significant)");
		else
			System.out.println( "p-value : " + p_value + "   (insignificant)");
	}
	
	
	/**
	 * Identify the inputs which are common in both preferred and base prediction data.
	 * Assuming the preferred data has less number of inputs, we remove the uncommon items
	 * from the base predictions and re-write it in a new file.
	 * 
	 * @param basePredFile
	 * @param baseEntPairsFile
	 * @param prefEntPairsFile
	 * @throws IOException
	 */
	private void createOutputFileBasePredData( String basePredFile, String baseEntPairsFile, String prefEntPairsFile ) throws IOException {
		
		ArrayList<String> listPrefEntPairs = FileUtility.readNonEmptyFileLines(prefEntPairsFile);
		ArrayList<String> listBaseEntPairs = FileUtility.readNonEmptyFileLines(baseEntPairsFile);
		ArrayList<String> listBasePreds = FileUtility.readNonEmptyFileLines(basePredFile);
		StringBuilder sb = new StringBuilder();
			
		for ( int i=0; i<listPrefEntPairs.size(); i++ ) {
			// the i-th input in both predictions have to match
			if ( !listPrefEntPairs.get(i).equals(listBaseEntPairs.get(i)) ) { 
				listBaseEntPairs.remove(i);
				listBasePreds.remove(i);
				i--;
				continue;
			}			
			
			sb.append(listBasePreds.get(i) + "\n");			
		}
		
		FileUtility.writeInFile(basePredFile + ".new", sb.toString(), false);
	}
	
	
	/**
	 * 
	 * @param origPredFile
	 * @param origEntPairsFile
	 * @param allEntPairsFile
	 * @throws IOException 
	 */
	private void insertEntriesForPairsThatAreLeftOut( String origPredFile, String origEntPairsFile, String allEntPairsFile ) throws IOException {
		
		ArrayList<String> listAllEntPairs = FileUtility.readNonEmptyFileLines(allEntPairsFile);
		ArrayList<String> listOrigEntPairs = FileUtility.readNonEmptyFileLines(origEntPairsFile);
		ArrayList<String> listOrigPreds = FileUtility.readNonEmptyFileLines(origPredFile);
		StringBuilder sb = new StringBuilder();
			
		for ( int i=0; i<listAllEntPairs.size(); i++ ) {
			if ( !listAllEntPairs.get(i).equals(listOrigEntPairs.get(i)) ) { 
				// add missing pair
				listOrigEntPairs.add(i, listAllEntPairs.get(i));

				float x = Float.valueOf(listGold.get(i));
				// TN
				//if ( x < 1 )
					//listOrigPreds.add(i, "-1");
				// FN
				//else
					listOrigPreds.add(i, "-1 D-X 0 -0.000");
			}
			
			//if ( listOrigPreds.get(i).matches("0.0\\s+.*\\s+1\\s+.*") ) 
				//listOrigPreds.set(i, "1.0");
			
			sb.append(listOrigPreds.get(i) + "\n");			
		}
		
		FileUtility.writeInFile(origPredFile + ".rev", sb.toString(), false);
	}
	
	/**
	 * 
	 * @param goldFile
	 * @param baseFile
	 * @param preferredFile
	 * @throws Exception
	 */
	private void createStatTestInputFiles( String goldFile, String baseFile, String preferredFile ) throws Exception{
		//listPref = readjustStatTestInputFiles(preferredFile+".rev");
		//listBase = readjustStatTestInputFiles(baseFile+".rev");
		listPref = readjustStatTestInputFiles(preferredFile);
		listBase = readjustStatTestInputFiles(baseFile);
		
		// we don't need true negatives for F-score
		removeTNforAll();
				
		writeStatTestInputFiles( listGold, goldFile+".t");
		writeStatTestInputFiles( listPref, preferredFile+".t");
		writeStatTestInputFiles( listBase, baseFile+".t");
		
	}
	
	 	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private ArrayList<String> readjustStatTestInputFiles( String fileName ) throws Exception{
	
		ArrayList<String> str = FileUtility.readFileLines(fileName);
		
		for ( int i=0; i<str.size(); i++ ){
			if ( !str.get(i).trim().isEmpty() ){
				//if ( str.get(i).matches("0.0\\s+.*\\s+1\\s+.*") ) 
					//str.set(i, "1.0");
				//str.set(i, str.get(i).split("\\s+")[0]);
				str.set(i, str.get(i).split("\\s+")[2]);
			}
		}
		
		return str;
		
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void removeTNforAll() throws Exception{
	
		for ( int i=0; i<listBase.size(); i++ ){
			if (  listBase.get(i).matches("(0.0|0)") && listPref.get(i).matches("(0.0|0)")
				&& listGold.get(i).matches("(0.0|0)") ) {
				listBase.remove(i);
				listPref.remove(i);
				listGold.remove(i);
				i--;
			}
		}
	}
	
	/**
	 * 
	 * @param str
	 * @param fileName
	 * @throws Exception
	 */
	private void writeStatTestInputFiles( ArrayList<String> str, String fileName ) throws Exception{
	
		StringBuilder sb = new StringBuilder();
		float x = 0;
		
		for ( int i=0; i<str.size(); i++ ){
			
			x = Float.valueOf(str.get(i).split("\\s+")[0]);
			if ( x < 1 )			
				sb.append( "0.0\t" + i + "-0\n");
			else
				sb.append( "1.0\t" + i + "-0\n");
		}
		
		FileUtility.writeInFile(fileName, sb.toString(), false);		
	}
	
	/**
	 * Identify all gold TP and TN 
	 * 
	 * @param tkFolderName
	 * @throws Exception
	 */
	private void readEntiresForGoldFile( String baseFile ) throws Exception{
		ArrayList<String> listAllEntPairs = FileUtility.readNonEmptyFileLines(baseFile);
		
		listGold = new ArrayList<String>();
		
		int p = 0;
		for ( int k=0; k<listAllEntPairs.size(); k++ ){
			String[] str = listAllEntPairs.get(k).split("\\s+");
			
			if ( str[0].equals("1.0") && str[2].equals("1") ) {
				listGold.add("1.0");
				p++;
			}
			else if ( str[0].equals("0.0") && str[2].equals("0") ) {
				listGold.add("1.0");
				p++;
			}
			else
				listGold.add("0.0");
		}
		
		System.out.println(p);
	}
	
	
}
