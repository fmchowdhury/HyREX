package Utility;

import gov.nih.nlm.nls.lvg.Api.NormApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import Structures.Entity;
import Structures.Sentence;


public class CommonUtility {
	

	public static String EOLmarker= "\n\n THIS IS END OF LINE .";//" EOS .";
	
	public static String OUT_DIR= "out";//" EOS .";
	

	public static String EntPairFileName_PST = OUT_DIR + "/entPairFileName_PST";
	public static String EntPairFileName_DT = OUT_DIR + "/entPairFileName_DT";
	public static String EntPairFileName_JSRE = OUT_DIR + "/entPairFileName_JSRE";
	public static String EntPairFileName_TK = OUT_DIR + "/entPairFileName_TK";
	public static String EntPairFileName_WV = OUT_DIR + "/entPairFileName_WV";
			
	
    public static NormApi apiLvgNorm = null;
	public static String lvgInstallDir =  null;

	/**
	 * To use LVG the following method must be called first.
	 * 
	 */
	public void initLvgNormApi(){
    	Hashtable<String, String> properties = new Hashtable<String, String>();

	    properties.put(gov.nih.nlm.nls.lvg.Lib.Configuration.LVG_DIR, lvgInstallDir);
    			
    	//TODO: getting error for the following line
    	// apiLvgNorm = new NormApi(new Hashtable<String, String>().put(gov.nih.nlm.nls.lvg.Lib.Configuration.LVG_DIR,	lvgInstallDir));
    	
        apiLvgNorm = new NormApi(lvgInstallDir + "data/config/lvg.properties", properties);
    }
	
	/**
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public static void addExtraSpaceBeforeSenEnding( String fileName ) throws Exception {
		String line = null;
		StringBuilder sb = new StringBuilder();
		
		try {
			BufferedReader input =  new BufferedReader(new FileReader(new File(fileName)));
			
			while (( line = input.readLine()) != null){
				if ( !( line = line.trim()).isEmpty() ){
					String endChar = line.substring(line.length() -1);
					if ( endChar.matches("[.?!]") )
						line = line.substring(0, line.length() -1).trim() + " " + endChar;
					else
						line = line + " .";
				}
			
			//	if ( !line.trim().isEmpty() )
			//		line = line + EOLmarker;
				
				sb.append(line + "\n");
			}
			
			input.close();
		}
		catch (IOException e) {
            e.printStackTrace();
        } 
		
		FileUtility.writeInFile(fileName, sb.toString(), false);
	}

		
	
	public static void calculateCrossFoldResult_full( String fullDataFileName, String predWithProbabFile, 
			String predPairsFileName, String evaluationResultFileName ) throws Exception{
		
		ArrayList<String> outLines = FileUtility.readFileLines(evaluationResultFileName);
		
		double TP = 0, FP = 0, FN = 0;
		
		for ( int i=0; i<outLines.size(); i++ ){
			if ( outLines.get(i).contains("TP:") ) {

				String str = outLines.get(i) + " ";
				int x = str.indexOf("TP:") + 3;
				TP += Double.valueOf(str.substring(x, str.indexOf(" ", x)));
				
				x = outLines.get(i).indexOf("FP:") + 3;
				FP += Double.valueOf(str.substring(x, str.indexOf(" ", x)));
				
				x = outLines.get(i).indexOf("FN:") + 3;
				FN += Double.valueOf(str.substring(x, str.indexOf(" ", x)));	
			}
		}
		
		System.out.println(( TP + FN));
		if ( TP < 1 || (TP+FN) < 1 || (TP+FP) < 1 ){
			System.out.println(0);
			return;
		}

		//FN=FN+52;
		
		System.out.println("TP: " + TP + "\n" + "FP: " + FP + "\n" + "FN: " + FN);
		
		double recall = DataStrucUtility.roundTwoDecimals(TP*100/(TP+FN)), precision = DataStrucUtility.roundTwoDecimals(TP*100/(TP+FP)); 
		System.out.println("P: " + precision);
		System.out.println("R: " + recall);
		System.out.println("F: " + DataStrucUtility.roundTwoDecimals(2*precision*recall/(precision+recall)));
		
		FN=1000-TP;
/*		
		System.out.println();
		System.out.println();
		System.out.println("TP: " + TP + "\n" + "FP: " + FP + "\n" + "FN: " + FN);
		
		recall = TextUtility.roundTwoDecimals(TP*100/(TP+FN));
		precision = TextUtility.roundTwoDecimals(TP*100/(TP+FP)); 
		double TN = 7009 - FP;
		double FPR = FP / (FP + TN); // false positive rate (FPR)
		//double FDR = FP / (FP + TP); // false discovery rate (FDR)
		//double ACC = (TP + TN) / (TP + FN + FP + TN); 
	    
		System.out.println("TN: " + TN);
		System.out.println("P: " + precision);
		System.out.println("R: " + recall);
		System.out.println("F: " + TextUtility.roundTwoDecimals(2*precision*recall/(precision+recall)));
		System.out.println("FPR: " + TextUtility.roundTwoDecimals(FPR*100));
		//System.out.println("FDR: " + Utility.roundTwoDecimals(FDR*100));
		//System.out.println("ACC: " + Utility.roundTwoDecimals(ACC*100));
		 
		calculateAUC(fullDataFileName, predWithProbabFile, predPairsFileName);
		*/
	}

	
	public static void calculateCrossFoldResult( String evaluationResultFileName, boolean returnRecall ){
		
		ArrayList<String> outLines = FileUtility.readFileLines(evaluationResultFileName);
		
		double TP = 0, FP = 0, FN = 0;
		
		for ( int i=0; i<outLines.size(); i++ ){
			if ( outLines.get(i).contains("TP:") ) {

				String str = outLines.get(i) + " ";
				int x = str.indexOf("TP:") + 3;
				TP += Double.valueOf(str.substring(x, str.indexOf(" ", x)));
				
				x = outLines.get(i).indexOf("FP:") + 3;
				FP += Double.valueOf(str.substring(x, str.indexOf(" ", x)));
				
				x = outLines.get(i).indexOf("FN:") + 3;
				FN += Double.valueOf(str.substring(x, str.indexOf(" ", x)));	
			}
		}
		
		if ( TP < 1 || (TP+FN) < 1 || (TP+FP) < 1 ){
			System.out.println(0);
			return;
		}

		double recall = DataStrucUtility.roundTwoDecimals(TP*100/(TP+FN)), precision = DataStrucUtility.roundTwoDecimals(TP*100/(TP+FP));
		
		if ( returnRecall )
			System.out.println(recall);
		else
			System.out.println((long)(DataStrucUtility.roundTwoDecimals(2*precision*recall/(precision+recall))*100));
	}

	
	
	public static void calculateCrossFoldResult_oth( String evaluationResultFileName ){
		
		ArrayList<String> outLines = FileUtility.readFileLines(evaluationResultFileName);
		
		double TP = 0, FP = 0, FN = 0, f1 =0;
		
		for ( int i=0; i<outLines.size(); i++ ){
			if ( outLines.get(i).contains("c	tp	fp	fn	total	prec	recall	F1")
					|| outLines.get(i).contains("c\ttp\tfp\tfn\ttotal\tprec\trecall\tF1") ) {

				String[] str = outLines.get(i+1).split("\\s+");
				TP += Double.valueOf(str[1]);
				
				FP += Double.valueOf(str[2]);
				
				FN += Double.valueOf(str[3]);
				f1 += Double.valueOf(str[7]);
			}
		}
		

		//FN = 1000- TP;
		
		System.out.println("TP: " + TP + "\n" + "FP: " + FP + "\n" + "FN: " + FN);
		
		double recall = DataStrucUtility.roundTwoDecimals(TP*100/(TP+FN)), precision = DataStrucUtility.roundTwoDecimals(TP*100/(TP+FP));
			System.out.println("P: " + precision);
			System.out.println("R: " + recall);
			System.out.println("F: " + DataStrucUtility.roundTwoDecimals(2*precision*recall/(precision+recall)));
			
			System.out.println(f1/10);
	}
	//*
	public static void main (String[] args) throws Exception {
		calculateAUC( "../../data/5_PPI_corpora/bioinfer/bioinfer.full", 
				"out/best.base.stat.in", 
				"out/entPairFileName_TK");
	}
	//*/
	/**
	 * 
	 * @param predWithProbabFile
	 * @throws Exception 
	 */
	public static void calculateAUC( String fullDataFileName, String predWithProbabFile, String predPairsFileName ) throws Exception {
		
		ArrayList<String> allLines = null;
		ArrayList<Double> listPosWithDecVal = new ArrayList<Double>(), 
		listNegWithDecVal = new ArrayList<Double>();
		
		if ( !TextUtility.isEmptyString(fullDataFileName) && !TextUtility.isEmptyString(predPairsFileName) ) {
			ArrayList<Sentence> listOfSentences = Sentence.readFullData( fullDataFileName, "", "", CommonUtility.OUT_DIR + "/allEntPair", false);
			
			allLines = FileUtility.readNonEmptyFileLines(predPairsFileName);
			ArrayList<String> listOfAllPairs = FileUtility.readNonEmptyFileLines(CommonUtility.OUT_DIR + "/allEntPair");
			
			// remove those instances from listOfAllPairs for which relation types are already predicted
			for ( int i=0; i<allLines.size(); i++ ) {
				String[] str = allLines.get(i).split("\\s+");
				int x = -1;
				if ( str.length > 2 && ( (x=listOfAllPairs.indexOf(str[0] + "\t" + str[1])) > -1
						|| (x=listOfAllPairs.indexOf(str[1] + "\t" + str[0])) > -1 ) ) {
					listOfAllPairs.remove(x);
				}
			}		
			
			// - add all the automatically discarded TP and TN in the list with a default prediction score  -999.0
			// - add all the automatically discarded FP and FN in the list with a default prediction score -1000.0
			
			// remove those instances from listOfAllPairs for which relation types are already predicted
			for ( int i=0; i<listOfAllPairs.size(); i++ ) {
				for ( int s=0; s<listOfSentences.size(); s++ ) {
					if ( listOfAllPairs.get(i).contains(listOfSentences.get(s).senID + ".") ) {
						String[] str = listOfAllPairs.get(i).split("\\s+");
						Entity e1 = listOfSentences.get(s).getEntityById(str[0]);
						Entity e2 = listOfSentences.get(s).getEntityById(str[1]);
						
						if ( e1 != null && e2 != null ) {
							try{
								if ( listOfSentences.get(s).getPolarityOfRelation(e1, e2) )
									listPosWithDecVal.add(-1000.0);
								else
									listNegWithDecVal.add(-1000.0);
							}
							catch (Exception ex) {
								// Do nothing
							}
						}
					}
				}
			}
		}
		
		allLines = FileUtility.readNonEmptyFileLines(predWithProbabFile);
				
		// listPosWithDecVal = list of scores of all TP and TN instances
		// listNegWithDecVal = list of scores of all FP and FN 
		for ( int i=0; i<allLines.size(); i++ ) {
			if ( allLines.get(i).trim().length() > 0 ) {
				String[] temp = allLines.get(i).trim().split("\\s+");
				
				if ( (temp[2].equals("1") && temp[0].equals("1.0")) || (temp[2].equals("0") && temp[0].equals("0.0")) )
					listPosWithDecVal.add(Double.valueOf(temp[3]));
				else
					listNegWithDecVal.add(Double.valueOf(temp[3]));
			}
		}
		
		
		
		double auc = 0;
		// for each element in listPosWithDecVal, count the number of items of listNegWithDecVal which 
		// have lower predicted scores than it
		for ( int p=0; p<listPosWithDecVal.size(); p++ ) {
			//auc+=listNegWithDecVal.size();
			for ( int n=0; n<listNegWithDecVal.size(); n++ ) {
				/*
				 *  if it is a positive instance that was filtered before prediction,
				 *  then only count the number of items of listNegWithDecVal which have negative predicted scores
				 */
				//if ( listPosWithDecVal.get(p) == -1000.0 && listNegWithDecVal.get(n) < 0 )
					//auc++;
				//else 
					if ( listPosWithDecVal.get(p) > listNegWithDecVal.get(n) ) // i.e. listPosWithDecVal.get(p) - listNegWithDecVal.get(n) > 0
					auc++;
				else if ( listPosWithDecVal.get(p) == listNegWithDecVal.get(n) ) // i.e. listPosWithDecVal.get(p) - listNegWithDecVal.get(n) == 0
					auc += 0.5;
			}			
		}		 
				
		auc = (auc / listPosWithDecVal.size()) / listNegWithDecVal.size();
		
		System.out.println("AUC: " + auc*100);
	}
	
}

