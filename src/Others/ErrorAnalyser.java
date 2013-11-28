package Others;

import java.util.ArrayList;

import Clause.ClauseAnalyser;
import Structures.*;
import Utility.*;


public class ErrorAnalyser {

	public static void main ( String args[]) throws Exception{
		/*
		String[] predFileNames = new String[] {
				"only_medt_aimed/pst_icr_aimed/best.base.stat.in",
				"only_medt_aimed/pst_isr_aimed/best.base.stat.in"				
		};
		
		String[] entPairFileNames = new String[] {
				"only_medt_aimed/pst_icr_aimed/entPairFileName_TK",
				"only_medt_aimed/pst_isr_aimed/entPairFileName_TK"				
		};
		
		new ErrorAnalyser().findComplementaryTPs( predFileNames,
				entPairFileNames, "/hardmnt/amosoz0/tcc/chowdhury/data/AImed/aimedFull.txt");
		*/
		
		/*
		String[] entPairFileNames = new String[] {
				"icr_pairs",
				"isr_pairs"				
		};
		/*
		new ErrorAnalyser().findComplementaryTPs( entPairFileNames, "/media/Study/data/DDIExtraction2011/test_full.txt",
				"/media/Study/data/DDIExtraction2011/test_sen.parsed_psg_segmented_merged_corrected");
				*/
		new ErrorAnalyser().findFPsInSentences("base.stat.in",
				"entPairFileName_WV", "../../data/converted_PPI_corpora/lll/lll.full.blind");
	}
	
	
	/**
	 * 
	 * @param predictionFile
	 * @param allEntPairFile
	 * @param testEntPairFile
	 * @throws Exception 
	 */
	public void findComplementaryTPs ( String[] predictionFiles, 
			String[] testEntPairFiles, String fullDataFileName ) throws Exception{
		
		ArrayList<String> allLines = FileUtility.readNonEmptyFileLines(testEntPairFiles[0]);
		String[][] entPairsICR = new String[allLines.size()][];
		
		for ( int i=0; i<allLines.size(); i++ ) {
			entPairsICR[i] = allLines.get(i).trim().split("\\s+");
		}
		
		allLines = FileUtility.readNonEmptyFileLines(predictionFiles[0]);
		
		for ( int i=0; i<allLines.size(); i++ ) {
			
			if ( allLines.get(i).trim().length() > 0 ) {
				String[] temp = allLines.get(i).trim().split("\\s+");
				
				if ( temp[2].equals("1") && temp[0].equals("0.0") )				
					entPairsICR[i][2] = "FP";
				else if ( temp[2].equals("1") && temp[0].equals("1.0") )				
					entPairsICR[i][2] = "TP";
				else if ( temp[2].equals("0") && temp[0].equals("0.0") )				
					entPairsICR[i][2] = "FN";
			}
		}
		
		allLines = FileUtility.readNonEmptyFileLines(testEntPairFiles[1]);
		String[][] entPairsISenR = new String[allLines.size()][];
		
		for ( int i=0; i<allLines.size(); i++ ) {
			entPairsISenR[i] = allLines.get(i).trim().split("\\s+");
		}
		
		allLines = FileUtility.readNonEmptyFileLines(predictionFiles[1]);
		
		for ( int i=0; i<allLines.size(); i++ ) {
			
			if ( allLines.get(i).trim().length() > 0 ) {
				String[] temp = allLines.get(i).trim().split("\\s+");
				
				if ( temp[2].equals("1") && temp[0].equals("0.0") )				
					entPairsISenR[i][2] = "FP";
				else if ( temp[2].equals("1") && temp[0].equals("1.0") )				
					entPairsISenR[i][2] = "TP";
				else if ( temp[2].equals("0") && temp[0].equals("0.0") )				
					entPairsISenR[i][2] = "FN";
			}
		}
		
		StringBuilder sbICR = new StringBuilder(), sbISenR = new StringBuilder();
		listSentence = Sentence.readFullData(fullDataFileName, "", "", "", false);
		
		for ( int i=0; i<entPairsICR.length; i++ ) {
			for ( int k=0; k<entPairsISenR.length; k++ ) {
				
				if ( ( entPairsICR[i][0].equals(entPairsISenR[k][0]) && entPairsICR[i][1].equals(entPairsISenR[k][1]) )
						||
						( entPairsICR[i][1].equals(entPairsISenR[k][0]) && entPairsICR[i][0].equals(entPairsISenR[k][1]) )
				) {
					if ( entPairsICR[i][2].equals("TP") && !entPairsISenR[k][2].equals("TP") ) {
						sbICR.append(entPairsICR[i][0] + "\t" + entPairsICR[i][1] + "\t"
								+ entPairsICR[i][2] + "\n\n");
						
						sbICR.append(addSentence(entPairsICR[i][0]) + "-------------------------------\n");
					}
					
					else if ( !entPairsICR[i][2].equals("TP") && entPairsISenR[k][2].equals("TP") ) {
						sbISenR.append(entPairsICR[i][0] + "\t" + entPairsICR[i][1] + "\t"
								+ entPairsISenR[k][2] + "\n\n");
						
						sbISenR.append(addSentence(entPairsICR[i][0]) + "-------------------------------\n");
					}
					
					break;
				}
			}
		}
		
		FileUtility.writeInFile("icr_pst_correct", sbICR.toString(), false);
		FileUtility.writeInFile("isr_pst_correct", sbISenR.toString(), false);
	}
	
	ArrayList<Sentence> listSentence;
	String[] arrSenSegmented;
	
	public String addSentence( String entOneID, String entTwoID ) {
		
		int x = 0;
		for ( int i=0; i<listSentence.size(); i++ ) {
			if ( entOneID.contains(listSentence.get(i).senID + ".") ){
				String str = "";
				for ( int e=0; x<2 && e<listSentence.get(i).listOfEntities.size(); e++ )
					if ( listSentence.get(i).listOfEntities.get(e).id.equals(entOneID)
							|| listSentence.get(i).listOfEntities.get(e).id.equals(entTwoID) ) {
						str += listSentence.get(i).listOfEntities.get(e).printString();
						x++;
					}
				
				//str += "\n" + listSentence.get(i-1).text  + "\n" + listSentence.get(i).text + "\n";
				str += "\n" + listSentence.get(i).text + "\n";
				return str;
			}				
		}
		
		return "";
	}
	
	public String addSentence( String entOneID ) {
		
		for ( int i=0; i<listSentence.size(); i++ ) {
			if ( entOneID.contains(listSentence.get(i).senID + ".") ){
				
				return arrSenSegmented[i].trim() + "\n\n" + listSentence.get(i).printString();
			}				
		}
		
		return "";
	}
	
	/**
	 * 
	 * @param predictionFile
	 * @param allEntPairFile
	 * @param testEntPairFile
	 * @throws Exception 
	 */
	public void findComplementaryTPs ( String[] predictionFiles, String fullDataFileName,
			String segmentedFilName) throws Exception{
		
		ArrayList<String> allLines = FileUtility.readNonEmptyFileLines(predictionFiles[0]);
		String[][] entPairsICR = new String[allLines.size()][];
		
		arrSenSegmented = new ClauseAnalyser().readSegmentedData(segmentedFilName);
		
		for ( int i=0; i<allLines.size(); i++ ) {
			entPairsICR[i] = allLines.get(i).trim().split("\\s+");
		}
		
		allLines = FileUtility.readNonEmptyFileLines(predictionFiles[1]);
		String[][] entPairsISenR = new String[allLines.size()][];
		
		for ( int i=0; i<allLines.size(); i++ ) {
			entPairsISenR[i] = allLines.get(i).trim().split("\\s+");
		}
		
		StringBuilder sbICR = new StringBuilder(), sbISenR = new StringBuilder();
		listSentence = Sentence.readFullData(fullDataFileName, "", "", "", false);
		
		for ( int i=0; i<entPairsICR.length; i++ ) {
			for ( int k=0; k<entPairsISenR.length; k++ ) {
				
				if ( ( entPairsICR[i][0].equals(entPairsISenR[k][0]) && entPairsICR[i][1].equals(entPairsISenR[k][1]) )
						||
						( entPairsICR[i][1].equals(entPairsISenR[k][0]) && entPairsICR[i][0].equals(entPairsISenR[k][1]) )
				) {
					if ( entPairsICR[i][2].equals("TP") && !entPairsISenR[k][2].equals("TP") ) {
						sbICR.append(entPairsICR[i][0] + "\t" + entPairsICR[i][1] + "\t"
								+ entPairsICR[i][2] + "\n\n");
						
						sbICR.append(addSentence(entPairsICR[i][0]) + "-------------------------------\n");
					}
					
					else if ( !entPairsICR[i][2].equals("TP") && entPairsISenR[k][2].equals("TP") ) {
						sbISenR.append(entPairsICR[i][0] + "\t" + entPairsICR[i][1] + "\t"
								+ entPairsISenR[k][2] + "\n\n");
						
						sbISenR.append(addSentence(entPairsICR[i][0]) + "-------------------------------\n");
					}
					
					break;
				}
			}
		}
		
		FileUtility.writeInFile("icr_correct", sbICR.toString(), false);
		FileUtility.writeInFile("isr_correct", sbISenR.toString(), false);
	}
	
	
	public void findFPsInSentences ( String predictionFile, String entPairFile, String fullDataFileName ) throws Exception{
		
		ArrayList<String> allLines = FileUtility.readNonEmptyFileLines(entPairFile);
		String[][] entPairs = new String[allLines.size()][];
		
		for ( int i=0; i<allLines.size(); i++ ) {
			entPairs[i] = allLines.get(i).trim().split("\\s+");
		}
		
		StringBuilder sb = new StringBuilder();
		listSentence = Sentence.readFullData(fullDataFileName, "", "", "", false);
		int z = 1;
		
		allLines = FileUtility.readNonEmptyFileLines(predictionFile);
		int tp = 0, fp = 0, fn = 0;
		
		for ( int i=0; i<allLines.size(); i++ ) {
			
			if ( allLines.get(i).trim().length() > 0 ) {
				String[] temp = allLines.get(i).trim().split("\\s+");
				/*
				if ( temp[2].equals("1") ) {
					if ( isNegRelation(entPairs[i][0], entPairs[i][1]) ) {			
						temp[2] = "0";
						//System.out.println(temp[0] + " " + temp[2]);
					}
				}
				*/
				if ( temp[2].equals("1") && temp[0].equals("1.0") ) 			
					tp++;
				else if ( temp[2].equals("1") && temp[0].equals("0.0") ) 			
					fp++;
				else if ( temp[2].equals("0") && temp[0].equals("0.0") )
					fn++;
				
					
				//if ( temp[2].equals("1") && temp[0].equals("0.0") )	{
					// entPairsICR[i][2] = "FP";
				if ( temp[2].equals("0") && temp[0].equals("0.0") )		{		
					// entPairsICR[i][2] = "FN";
					System.out.println(i);
		
					sb.append(z + ": " + addSentence(entPairs[i][0], entPairs[i][1]) + "-------------------------------\n");
					z++;
				}
				/*
				else if ( temp[2].equals("1") && temp[0].equals("1.0") )				
					entPairsICR[i][2] = "TP";
				else 
					*/
			}
		}
		
		FileUtility.writeInFile("all_fn", sb.toString(), false);
		System.out.println("TP: " + tp + "\nFP: " + fp + "\nfn: " + fn);
	}
	
	/*
	public boolean isNegRelation( String entOneID, String entTwoID ) {
		
		boolean isNeg = false;
		int[] entOneBoundary = new int[0], entTwoBoundary = new int[0];
		String[] tokens = new String[0];
		
		for ( int i=0; i<listSentence.size(); i++ ) {
			if ( entOneID.contains(listSentence.get(i).senID + ".") ){
				
				tokens = listSentence.get(i).text.split("\\s+");
				int x = 0;
				for ( int e=0; x<2 && e<listSentence.get(i).listOfEntities.size(); e++ )
					if ( listSentence.get(i).listOfEntities.get(e).id.equals(entOneID)
							|| listSentence.get(i).listOfEntities.get(e).id.equals(entTwoID) ) {
						
						if ( x == 0 ) {
							entOneBoundary = new int[] { listSentence.get(i).listOfEntities.get(e).startIndex,
								listSentence.get(i).listOfEntities.get(e).endIndex };
							x++;
						}
						else {
							entTwoBoundary = new int[] { listSentence.get(i).listOfEntities.get(e).startIndex,
									listSentence.get(i).listOfEntities.get(e).endIndex };
							x++;
						}
					}
								
				break;
			}				
		}
		
		
		
		int[][] listOfBoundaries = new int[tokens.length][2];
		int t = -1;
		
		for ( int i=0; i<tokens.length; i++ ) {
			listOfBoundaries[i] = new int[] { t+1, t+tokens[i].length() }; 
			t+=tokens[i].length(); 
		}
		
		ArrayList<Integer> listOfWordIndexesEntOne = Common.findEntityWordIndexes(entOneBoundary, listOfBoundaries);
		ArrayList<Integer> listOfWordIndexesEntTwo = Common.findEntityWordIndexes(entTwoBoundary, listOfBoundaries);
		
		int x = listOfWordIndexesEntOne.get(0) < listOfWordIndexesEntTwo.get(0)
			? listOfWordIndexesEntOne.get(0) : listOfWordIndexesEntTwo.get(0);
		int y = listOfWordIndexesEntOne.get(0) > listOfWordIndexesEntTwo.get(0)
			? listOfWordIndexesEntOne.get(0) : listOfWordIndexesEntTwo.get(0);
		
			
		for ( int i=x; i>=0 && i >= x-4; i-- )
			if ( tokens[i].equalsIgnoreCase("no") || tokens[i].equalsIgnoreCase("not") )
				return true;
								
		for ( int i=y; i>=0 && i >= x; i-- )
			if ( tokens[i].equalsIgnoreCase("no") || tokens[i].equalsIgnoreCase("not") )
				return true;
		
		return isNeg;
	}
	
	*/
	
	
	
	
	
	
}
