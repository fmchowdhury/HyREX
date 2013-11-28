package DataProcessor;

import java.io.IOException;
import java.util.ArrayList;

import Structures.Sentence;
import Utility.*;


public class UniXmlToHyRexDataConvertor {

	public static void main ( String args[]) throws Exception{
		/*
		//new DDI2011Processor().getPredictionResultForTestData();
	//*
		String[] pre = new String[]{
			//	"lll", "hprd50", 
			//	"iepa", 
			//	"aimed", 
				"bioinfer"
				};
		String[] xmlFiles = new String[]{
			//	"LLL", "HPRD50",
			//	"IEPA", 
			//	"AImed", 
				"BioInfer"
				};
		
		for ( int i=0; i<pre.length; i++ ) {
			String path = "../../data/5_PPI_corpora/" + pre[i] + "/";
			new UniXmlToHyRexDataConvertor().extractDataFromXml( path + xmlFiles[i] + ".xml", 
				path + pre[i] + ".sen", path + pre[i] + ".full");
		}
		//*/	
		// new DDI2011Processor().splitTrainingDocuments();
		//new DDI2011Processor().transformIntoUnifiedFormat();
		//new UniXmlToHyRexDataConvertor().extractDataFromXml( "../../data/DDIExtraction2011/training.xml", 
				// "../../data/DDIExtraction2011/train.sen", "../../data/DDIExtraction2011/train.full", "", false);
	}
	
	
	/**
	 * 
	 * @param inUnifiedXmlDataFilePath
	 * @param outSenFileName
	 * @param outSenWithEntRelFileName
	 * @throws Exception
	 */
	public void extractDataFromXml( String inUnifiedXmlDataFilePath, String outSenFileName, 
			String outSenWithEntRelFileName, String relName, boolean bAutoGenerateNegativeInstance ) throws Exception {
		
		transformIntoUnifiedFormat(inUnifiedXmlDataFilePath, inUnifiedXmlDataFilePath + ".tmp", relName);
		inUnifiedXmlDataFilePath = inUnifiedXmlDataFilePath + ".tmp";
		
		UnifiedFormatDataProcessor clsUnifiedFormatDataProcessor = new UnifiedFormatDataProcessor();

		boolean bInsertSpacesAtEntBoundary = true, 
		bRemoveDashAtWordBoundary = false, 
		bAddCommaBeforeConj = false, 
		bRemoveCommentsWithNoEntInParentheses = false, 
		bBlindEntities = false;
		
		boolean bAddExtraSpaceBeforeSenEnding = false,
				bResolveOverlapedAnnotationsByDuplicatingSentences = false;
		
		clsUnifiedFormatDataProcessor.extractAndMergeText(inUnifiedXmlDataFilePath, outSenFileName, outSenWithEntRelFileName,
				bInsertSpacesAtEntBoundary, bRemoveDashAtWordBoundary, bAddCommaBeforeConj, bRemoveCommentsWithNoEntInParentheses, bAutoGenerateNegativeInstance);
				
		
		clsUnifiedFormatDataProcessor.addAbstractIdWtihData(outSenWithEntRelFileName);
		//new AIMedDataProcessor().addAbstractIdWtihData(outSenWithEntRelFileName, "../../data/AImed/orig/corpus");
		
		if ( bResolveOverlapedAnnotationsByDuplicatingSentences )
			clsUnifiedFormatDataProcessor.resolveOverlappedAnnotationsByDuplicatingSentences(outSenWithEntRelFileName, outSenFileName);
		
		if ( bBlindEntities ) 
			BlindText.blindEntitiesInSentences(outSenWithEntRelFileName, outSenFileName);
		
		/**
		 * -- NOTE: This line is required as we need to indicate the parser that a full stop
		 * at the end of sentence is not part of an abbreviation.
		 */
		if ( bAddExtraSpaceBeforeSenEnding )
			CommonUtility.addExtraSpaceBeforeSenEnding(outSenFileName);
		
		addColonBeforeCapitalizedCommonWord(outSenFileName, outSenWithEntRelFileName);
		
		prepareDataForBLLIP(outSenFileName);
	}
	
	/**
	 * Prepare data for BLLIP parser
	 * 
	 * @param outSenFileName
	 * @throws IOException
	 */
	private void prepareDataForBLLIP ( String outSenFileName ) throws IOException {
		
		ArrayList<String> listOfAllSen = FileUtility.readFileLines(outSenFileName);
		StringBuilder sb = new StringBuilder();
		
		for ( int i=0; i<listOfAllSen.size(); i++ ) {
			String str = listOfAllSen.get(i).trim();
			
			if ( !str.isEmpty() )
				sb.append("<s> " + str + " </s>");
			sb.append("\n");
		}
		
		FileUtility.writeInFile(outSenFileName + ".bllip", sb.toString(), false);
	}
	
	/**
	 * This method fixes sentence spltting / missing character errors in the DDI2011Challenge corpus.
	 * 
	 * @param outSenFileName
	 * @param outSenWithEntRelFileName
	 * @throws Exception
	 */
	private void addColonBeforeCapitalizedCommonWord ( String outSenFileName, 
			String outSenWithEntRelFileName ) throws Exception {
		
		ArrayList<Sentence> listOfSentences = Sentence.readFullData(outSenWithEntRelFileName, "", "", "", false);
		ArrayList<String> listOfCommonEngWords = FileUtility.readNonEmptyFileLines("db/english_stop_words");
		StringBuilder sb = new StringBuilder();
		StringBuilder sbSen = new StringBuilder();
		
		int tot_cor = 1;
		
		for ( int s=0; s<listOfSentences.size(); s++ ) {
			
			listOfSentences.get(s).text = listOfSentences.get(s).text.replaceAll("\\s+", " ");
			
			//if ( listOfSentences.get(s).senID.contains("DrugDDI.d282.s7") )
				//listOfSentences.get(s).text.trim();
			
			if ( !listOfSentences.get(s).text.contains(":") ) {
				String[] str = listOfSentences.get(s).text.split("\\s+");
				String sen = "";
				int len = str[0].length() - 1;
				boolean found = false;
				for ( int w=1; w < str.length-1; w++ ) {
				
					/*
					if ( !found && w < 12 &&
							str[w].matches("[A-Z][a-z]*") && listOfCommonEngWords.contains(str[w].toLowerCase())
							&& str[w+1].matches("[a-z].*")
							&& !str[w-1].matches(".*[:-]")
						//	&& str[w-1].matches("[A-Z].*")
							) {
						if (  str[w-1].matches("([^a-z].*|.*[^a-z])")
							) {
						}
						else {

							System.out.println(tot_cor);
							System.out.println(listOfSentences.get(s).text);
							System.out.println();
							tot_cor++;
						}							
					}*/
					
					if ( !found && w < 12 &&
							str[w].matches("[A-Z][a-z]*") && listOfCommonEngWords.contains(str[w].toLowerCase())
							&& str[w+1].matches("[a-z].*")
							&& !str[w-1].matches(".*[:-]")
							&& str[w-1].matches("([^a-z].*|.*[^a-z])")
							) {
						
						str[w-1] += " :";
						found = true;
						
						for ( int e=0; e<listOfSentences.get(s).listOfEntities.size(); e++ ) {
							if ( listOfSentences.get(s).listOfEntities.get(e).startIndex > len ) {
								listOfSentences.get(s).listOfEntities.get(e).startIndex++;
								listOfSentences.get(s).listOfEntities.get(e).endIndex++;
								listOfSentences.get(s).listOfEntities.get(e).boundaries[0]++;
								listOfSentences.get(s).listOfEntities.get(e).boundaries[1]++;
							}
						}
						
						len++;
					}
					
					len+=str[w].length();
					sen += " " + str[w-1];
				}
					
				if ( str.length > 2 )
					sen += " " + str[str.length-2] + " " + str[str.length-1];
				else
					sen += " " + str[str.length-1];
				
				//*
				if ( found ) {
					System.out.println(tot_cor);
					System.out.println(listOfSentences.get(s).text);
					System.out.println(sen.trim());
					System.out.println();
					tot_cor++;
				}
				//*/
								
				if ( sen.length() > listOfSentences.get(s).text.length() )
					listOfSentences.get(s).text = sen.trim();
			}
			
			sb.append(listOfSentences.get(s).printString());
			sbSen.append(listOfSentences.get(s).text + "\n\n");
		}
		
		FileUtility.writeInFile(outSenWithEntRelFileName, sb.toString(), false);
		FileUtility.writeInFile(outSenFileName, sbSen.toString(), false);
	}
	
	/**
	 * For DDI
	 * 
	 * @throws Exception
	 */
	public void getPredictionResultForTestData() throws Exception {
		outputPredictions("base.stat.in", "allEntPair", "entPairFileName_TK");
		calculateResult( "../../data/DDIExtraction2011/Solution_prediction_Unified.txt‎", "predictions.out");
	}
	
	
	/**
	 * 
	 * @param inFolder
	 * @param outFile
	 * @throws IOException
	 */
	public void transformSeveralFilesIntoUnifiedFormat ( String inFolder, String outFile, String relName) throws IOException {

		String[] arrOfDocs = FileUtility.getFileNamesFromDir(inFolder);
			
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<corpus source=\"" + 
				inFolder + "\">" + "\n");
		
		// merge files
		for ( int i=0; i<arrOfDocs.length; i++ ) {
			
			String[] allLines = FileUtility.readFileContents(inFolder + "/" + arrOfDocs[i]).replaceAll(">", ">\n").split("\\n");
			
			for ( int k=0; k<allLines.length; k++ ) {
				allLines[k] = allLines[k].trim();
				         
				if ( !allLines[k].contains("<?xml") && !allLines[k].isEmpty() )
					sb.append( allLines[k] + "\n");
			}			
		}
		
		sb.append("</corpus>");
		
		FileUtility.createTmpFile("tmp_conv.xml");
		FileUtility.writeInFile("tmp_conv.xml", sb.toString(), false);
		transformIntoUnifiedFormat("tmp_conv.xml", outFile, relName);
	}

	
	/**
	 * 
	 * @throws IOException
	 */
	public void transformIntoUnifiedFormat ( String inFile, String outFile, String relName ) throws IOException {

		StringBuilder sb = new StringBuilder();
			
		if ( TextUtility.isEmptyString(relName) )
			relName = "interaction=\"true\"";
		
		int len = relName.length();
		
		String[] allLines = FileUtility.readFileContents(inFile).replaceAll(">", ">\n").split("\\n");
			
		for ( int k=0; k<allLines.length; k++ ) {

			allLines[k] = allLines[k].trim();
			         
			if ( !allLines[k].isEmpty() ){
				String tmp = "";
				
				if ( allLines[k].startsWith("<") ){					
					k--;
					do {
						k++;
						tmp += allLines[k] + " ";						
					}while ( !allLines[k].endsWith(">") );
				}
					
				if ( tmp.matches(".*<pair\\s+.*") ){
					
					//int x = tmp.toLowerCase().indexOf(relName);
					//if ( x > 0 )
					tmp = tmp.replace("<pair", "<interaction");
					//else 
						//tmp = "";
				}
				
				if ( !TextUtility.isEmptyString(tmp) )
					sb.append( tmp + "\n");
			}
		}
		
		sb.append("</corpus>");		
		FileUtility.writeInFile(outFile, sb.toString(), false);
	}
	
	
	/**
	 * 
	 * @throws IOException
	 */
	public void splitTrainingDocuments ( String relName ) throws IOException{
		String dirPath = "../../data/SemEval2013/task-9-ddi/Train/DrugBank";
		StringBuilder sbTrDev = new StringBuilder();
		StringBuilder sbTestDev = new StringBuilder();
		
		int noOfFolds = 5;
		
		// read all files in directory
		String[] fileNames = FileUtility.getFileNamesFromDir(dirPath);
		
		int[] arrDDIperDoc = new int[fileNames.length];
		int[] arrAllDDIperDoc = new int[fileNames.length];
		int total = 0, totalAll = 0;
		ArrayList<String> listOfDocID = new ArrayList<String>();
		
		// count total number of DDI "true" pairs in each file, let it T
		for ( int i=0; i<fileNames.length; i++ ) {
			String text = FileUtility.readFileContents(dirPath + "/" + fileNames[i]);
			int x = text.indexOf("<document id=\"") + "<document id=\"".length();
			String id = text.substring(x, text.indexOf("\"", x) );
			
			arrDDIperDoc[i] = TextUtility.countNumberOfSubstring(relName + "=\"true\"", 
					text, true, false);
			
			arrAllDDIperDoc[i] = TextUtility.countNumberOfSubstring(relName + "=\"", 
					text, true, false);
			
			totalAll += arrAllDDIperDoc[i];
			total += arrDDIperDoc[i]; 
			//System.out.println(fileNames[i] + " " + id + " " + arrDDIperDoc[i]);
		
			listOfDocID.add(id);
		}
				
		System.out.println(fileNames.length + " " + total + " " + totalAll);
		// let n = T / 10
		int avg = total / noOfFolds, foldNo = 0, docs = 0;
		int[] folds = new int[10];
		
		StringBuilder sb = new StringBuilder();
		total = 0;
		// create n folds by merging the next available m files such that the total DDI "true"
		for ( int i=0; i<fileNames.length; i++ ) {
			if ( foldNo == noOfFolds-1 || folds[foldNo] + arrDDIperDoc[i] <= avg ){
				folds[foldNo] += arrDDIperDoc[i];
				sb.append(fileNames[i] + "\t\t" + listOfDocID.get(i) 
						+ "\t\t" + arrAllDDIperDoc[i] + "\t\t" + arrDDIperDoc[i] + "\n");
				docs++;
			}
			else {
				FileUtility.writeInFile("../../data/SemEval2013/task-9-ddi/Train/folds/fold_" + (foldNo+1), sb.toString(), false);
				
				if ( foldNo < 7 ) {
					total += folds[foldNo];
					sbTrDev.append(sb.toString());
				}
				else
					sbTestDev.append(sb.toString());
				
				sb = new StringBuilder();
				foldNo++;
				System.out.print(docs + "+");
				
				folds[foldNo] += arrDDIperDoc[i];
				sb.append(fileNames[i] + "\t" + listOfDocID.get(i) 
						+ "\t" + arrAllDDIperDoc[i] + "\t" + arrDDIperDoc[i] + "\n");
				docs=1;
			}
		}
		
		System.out.println(docs + "\n" + total);
		FileUtility.writeInFile("../../data/SemEval2013/task-9-ddi/Train/folds/fold_" + noOfFolds, sb.toString(), false);
		sbTestDev.append(sb.toString());
		
		FileUtility.writeInFile("../../data/SemEval2013/task-9-ddi/Train/folds/training_docs", sbTrDev.toString(), false);
		FileUtility.writeInFile("../../data/SemEval2013/task-9-ddi/Train/folds/test_docs", sbTestDev.toString(), false);
		
		// pairs in those m files <= n but in m+1 files > n
	}
	
	
	/**
	 * 
	 * @param predictionFile
	 * @param allEntPairFile
	 * @param testEntPairFile
	 * @throws IOException 
	 */
	public void outputPredictions ( String predictionFile, String allEntPairFile, String testEntPairFile ) throws IOException{
		
		ArrayList<String> allLines = FileUtility.readNonEmptyFileLines(testEntPairFile);
		ArrayList<String> allLinesPred = FileUtility.readNonEmptyFileLines(predictionFile);
		String[][] testEntPairs = new String[allLines.size()][];
		int tot_pos = 0;
		
		for ( int i=0; i<allLines.size(); i++ ) {
			testEntPairs[i] = allLines.get(i).trim().split("\\s+");
			testEntPairs[i][2] = allLinesPred.get(i).trim().split("\\s+")[2];
		}
		
		allLines = FileUtility.readNonEmptyFileLines(allEntPairFile);
		String[][] allEntPairs = new String[allLines.size()][];
		
		StringBuilder sb = new StringBuilder();
		for ( int i=0; i<allLines.size(); i++ ) {
			allEntPairs[i] = allLines.get(i).trim().split("\\s+");
			
			boolean isFound = false;
			// search the pairs for which predictions have been made
			for ( int k=0; !isFound && k<testEntPairs.length; k++ ) {
				if ( (testEntPairs[k][0].equals(allEntPairs[i][0])
						&& testEntPairs[k][1].equals(allEntPairs[i][1]) )
					|| (testEntPairs[k][1].equals(allEntPairs[i][0])
							&& testEntPairs[k][0].equals(allEntPairs[i][1]) )	
				) {
						sb.append(allEntPairs[i][0] + "\t" + allEntPairs[i][1] 
						    + "\t" + testEntPairs[k][2] + "\n" );
						isFound = true;
						
						if ( testEntPairs[k][2].equals("1") )
							tot_pos++;
				}
			}
			
			if ( !isFound ) {
				sb.append(allEntPairs[i][0] + "\t" + allEntPairs[i][1] 
				     + "\t0\n" );
			}
		}
		
		FileUtility.writeInFile("predictions.out", sb.toString(), false);
		
		System.out.println(tot_pos);
	}
	
	
	/**
	 * 
	 * @param goldPredFile
	 * @param systemPredFile
	 * @throws IOException 
	 */
	public static void calculateResult( String goldPredFile, String systemPredFile ) throws IOException{
		
		ArrayList<String> listGoldPred = FileUtility.readNonEmptyFileLines(goldPredFile);
		ArrayList<String> listSystemPred = FileUtility.readNonEmptyFileLines(systemPredFile);
		String[][] arrGoldPred = new String[listGoldPred.size()][],
					arrSystemPred = new String[listSystemPred.size()][];
		
		for ( int i=0; i<listGoldPred.size(); i++ ) 
			arrGoldPred[i] = listGoldPred.get(i).trim().split("\\s+");
		
		for ( int i=0; i<listSystemPred.size(); i++ ) 
			arrSystemPred[i] = listSystemPred.get(i).trim().split("\\s+");
		
		double TP = 0, FP = 0, FN = 0;

		StringBuilder sb = new StringBuilder();
		
		for ( int i=0; i<arrGoldPred.length; i++ ){
			for ( int k=0; k<arrSystemPred.length; k++ ){
				if ( ( arrSystemPred[k][0].equals(arrGoldPred[i][0])
					&& arrSystemPred[k][1].equals(arrGoldPred[i][1]) )
					|| ( arrSystemPred[k][0].equals(arrGoldPred[i][1])
						&& arrSystemPred[k][1].equals(arrGoldPred[i][0]) ) ) {
					
					if ( arrSystemPred[k][2].equals(arrGoldPred[i][2]) ) {
						if ( arrSystemPred[k][2].equals("1") ) {
							TP++;
							sb.append(arrSystemPred[k][0] + "\t" + arrSystemPred[k][1] + "\t" + "TP\n");
						}
						else
							sb.append(arrSystemPred[k][0] + "\t" + arrSystemPred[k][1] + "\t" + "TN\n");
					}
					else {
						if ( arrSystemPred[k][2].equals("1") ) {
							FP++;
							sb.append(arrSystemPred[k][0] + "\t" + arrSystemPred[k][1] + "\t" + "FP\n");
						}
						else {
							FN++;
							sb.append(arrSystemPred[k][0] + "\t" + arrSystemPred[k][1] + "\t" + "FN\n");
						}
					}
				}					
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
		
		FN=755-TP;
		
		System.out.println("TP: " + TP + "\n" + "FP: " + FP + "\n" + "FN: " + FN);
		
		recall = DataStrucUtility.roundTwoDecimals(TP*100/(TP+FN));
		precision = DataStrucUtility.roundTwoDecimals(TP*100/(TP+FP)); 
		double TN = 7026 - 755 - FP;
		double FPR = FP / (FP + TN); // false positive rate (FPR)
		double FDR = FP / (FP + TP); // false discovery rate (FDR)
		double ACC = (TP + TN) / (TP + FN + FP + TN); 
	    
		System.out.println("TN: " + TN);
		System.out.println("P: " + precision);
		System.out.println("R: " + recall);
		System.out.println("F: " + DataStrucUtility.roundTwoDecimals(2*precision*recall/(precision+recall)));
		System.out.println("FPR: " + DataStrucUtility.roundTwoDecimals(FPR*100));
		System.out.println("FDR: " + DataStrucUtility.roundTwoDecimals(FDR*100));
		System.out.println("ACC: " + DataStrucUtility.roundTwoDecimals(ACC*100));
		
		FileUtility.writeInFile("isr_pairs", sb.toString(), false);
	}
}
