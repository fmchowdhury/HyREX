package Others;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.lang.StringBuilder;

import Clause.ClauseAnalyser;
import Structures.*;
import Utility.*;

public class Main {

	public static void main(String[] args) throws Exception {
		/*
		ArrayList<String> listString = FileUtility.readNonEmptyFileLines("../train/unavailable_tweets");
		ArrayList<String> listStringOld = FileUtility.readNonEmptyFileLines("../train/task-b-train.txt.old");
		System.out.println("########## Start");
		StringBuilder sb = new StringBuilder();
		for ( int i=0; i<listString.size(); i++ ) {
			for ( int k=0; k<listStringOld.size(); k++ ) {
				if ( listStringOld.get(k).contains(listString.get(i)) && !listStringOld.get(k).contains("Not Available") ) {
					System.out.println(listStringOld.get(k));
					break;
				}
			}
		}
		
		/*
		ArrayList<String> listString = FileUtility.readNonEmptyFileLines("../../data/5_PPI_corpora/aimed/bioinfer.true.all");
		
		StringBuilder sb = new StringBuilder();
		for ( int i=0; i<listString.size(); i++ ) {
			String[] str = listString.get(i).split("\\s+");
			sb.append(str[0] + " " + str[1] + " " + str[2] + " " + str[4] + "\n");
		}
		
		FileUtility.writeInFile("../../data/5_PPI_corpora/aimed/bioinfer.true.all", sb.toString(), false);
		* /
		
		TextUtility.getFileOneLinesNotInFileTwo(  
				"../../data/5_PPI_corpora/aimed/bioinfer.true.all", "../../data/5_PPI_corpora/aimed/bioinfer.found.true");
		/*
		ArrayList<String> allLines_1 = Utility.readFileLines("/media/Study/workspace/data/pir.georgetown.edu/word_token_dictionaries/general_name"),
			allLines_2 = Utility.readFileLines("/media/Study/workspace/BioEnEx/experimental_results/pgn_filter_word_list");
		
		for ( int i=0; i<allLines_1.size(); i++){
			String s = allLines_1.get(i).split(":")[1].trim();
			if ( !allLines_2.contains(s) )
				allLines_2.add(s);
		}
		
		StringBuilder sb = new StringBuilder();
		for ( int i=0; i<allLines_2.size(); i++)
			sb.append(allLines_2.get(i)).append("\n");
			
		Utility.writeInFile("/media/Study/workspace/BioEnEx/experimental_results/pgn_filter_word_list_extended", sb.toString(), false);
	*/
//	filterByWordList("/media/Study/workspace/BioEnEx/experimental_results/anno_train_SSC_I_test_BiC_II");
	//*
		/*
		ArrayList<ArrayList<String>> listSen = Utility.readAllMultiLineSentences("/media/Study/data/AImed/aimed_parsed_sp1.6.5_psg");
				
		StringBuilder sb = new StringBuilder();
		
		for ( int i=0; i<listSen.size(); i++ ){
			for ( int k=0; k<listSen.get(i).size(); k++ ){
				sb.append(listSen.get(i).get(k).trim()).append(" ");
			}
			sb.append("\n\n");
		}
		
		Utility.writeInFile("/media/Study/data/AImed/clause/aimed_parsed_psg_merged", sb.toString(), false);
		/*/
		/*
		String fileName = "/media/Study/data/converted_PPI_corpora/Splits/AIMed/AIMed";
		
		for ( int i=1; i<=10; i++ ) {
			StringBuilder sb = new StringBuilder();
			for ( int k=1; k<=10; k++ ){
				
				if ( i != k )
					sb.append(FileUtility.readFileContents(fileName + k + ".txt").trim() + "\n");
			}
		
			FileUtility.writeInFile( "/media/Study/data/converted_PPI_corpora/aimed/folds/train-203-" + i,
					sb.toString(), false);
			
			sb = new StringBuilder();
			sb.append(FileUtility.readFileContents(fileName + i + ".txt").trim() + "\n");
		
			FileUtility.writeInFile( "/media/Study/data/converted_PPI_corpora/aimed/folds/test-203-" + i,
					sb.toString(), false);
			
		}
		//*/
		/*
		TKOutputGenerator.main(
		(" -wv  -trainParse  ../../data/converted_PPI_corpora/lll/lll.parsed.1.6.8.blind   -train" +
	 			" ../../data/converted_PPI_corpora/lll/lll.full.blind -foldFilesFolder  ../../data/converted_PPI_corpora/lll/folds" 
				).split("\\s+"));
		*/
		/*
		new ClauseAnalyser().
		findClauseBoundariesOfAllSen("/media/Study/data/AImed/aimed_parsed_psg_merged_segmented", 
				"/media/Study/data/AImed/aimed_sen_clau_boun",
				"/media/Study/data/AImed/aimedFull.txt");
		/*analyseCrossClausalRE("/media/Study/data/AImed/aimed_parsed_psg_merged_segmented", 
				"/media/Study/data/AImed/aimedFull.txt", "/media/Study/data/AImed/aimed_parsed_sp1.6.5_dt", 
				"/media/Study/data/AImed/aimed_parsed_sp1.6.5_psg", 
				"/media/Study/data/AImed/data_with_single_clause", ClauseAnalyser.eDataFilterOption.DATA_ALL);
		//*/
		
		/*
		new Main().selectSenIndexesRandomly("/media/Study/data/AImed/aimed_parsed_psg_merged_segmented", 
				"/media/Study/data/AImed/aimed_sen_clau_boun",
				"/media/Study/data/AImed/aimedFull.txt");
				*/
		//*,
		
		double TP = 1667,
				FP = 1243;
		double FN = 2402-TP;
		double TN = 5668 - TP - FN - FP;
		double FPR = FP / (FP + TN); // false positive rate (FPR)
		double FDR = FP / (FP + TP); // false discovery rate (FDR)
		double ACC = (TP + TN) / (TP + FN + FP + TN); 
	    
		
		double recall = DataStrucUtility.roundTwoDecimals(TP*100/(TP+FN)), precision = DataStrucUtility.roundTwoDecimals(TP*100/(TP+FP)); 
		//System.out.println("FN: " + FN + "\nTN: " + TN + "\nP: " + precision);
		System.out.println("P: " + precision);
		System.out.println("R: " + recall);
		System.out.println("F: " + DataStrucUtility.roundTwoDecimals(2*precision*recall/(precision+recall)));
		//*
		//System.out.println("FPR: " + DataStrucUtility.roundTwoDecimals(FPR*100));
	//	System.out.println("FDR: " + TextUtility.roundTwoDecimals(FDR*100));
		//System.out.println("ACC: " + TextUtility.roundTwoDecimals(ACC*100));
		//*/
		
		//separateHeadings("/media/Study/data/ACE 2004/ToHyREX/nwire.sentences", "/media/Study/data/ACE 2004/data/English/nwire");
	}
	
	public static void separateHeadings ( String dirPath, String origFileDir ) throws IOException {
		
		String[] fileNames = FileUtility.getFileNamesFromDir(dirPath);
		for ( int i=0; i<fileNames.length; i++ ) {
			
			if ( fileNames[i].endsWith("~") )
				continue;
			
			// read contents of the original text and keep only various meta data appearing before the news story 
			String str = "";
			
			if ( fileNames[i].startsWith("AP") ) {
				String[] tmp = FileUtility.readFileContents(origFileDir + "/" + fileNames[i]).split("<TEXT>");
				int v = tmp[1].indexOf("(AP)");
				if ( v > 100 || v < 0 )
					str = tmp[0];
				else
					str = tmp[0] + tmp[1].substring(0, v) + "(AP) _";
			}
			else	
				str = FileUtility.readFileContents(origFileDir + "/" + fileNames[i]).split("<TEXT>")[0];
			// remove all tags and replace them with new lines
			String[] metaData = str.replaceAll("</.*>", "\n").replaceAll("<.*>", "\n").trim().split("\n+");
						
			StringBuilder sb = new StringBuilder();
			// read the separated sentences
			ArrayList<String> listOfLines = FileUtility.readNonEmptyFileLines(dirPath + "/" + fileNames[i]);
			
			// remove all meta data from the sentences to make sure they are not merged with others
			int m=0;
			for ( int x=0; x<metaData.length && m<metaData.length && x<listOfLines.size(); x++ ) {
				for ( ; m<metaData.length; m++  ) {
					int v = 0;
					if ( (v=listOfLines.get(x).indexOf(metaData[m])) >= 0 ) {
						str = listOfLines.get(x);
						str = str.substring(0, v) + str.substring(v+metaData[m].length());
						listOfLines.set(x, str);
					}
					else
						break;
				}
			}
			
			// insert each meta data as a separate sentence
			for ( m=0; m<metaData.length; m++  )
				listOfLines.add( m, "<s> " + metaData[m] + " </s>");		
						
			for ( int x=0; x<listOfLines.size(); x++ ) {
				str = listOfLines.get(x);
				// correct the case of sentence start and end tags and insert blank lines after each sentence
				str = str.replaceAll("<S>", "<s>").replaceAll("</S>", " </s>").replaceAll("\\s+", " ");
				if ( str.replaceAll("<s>", "").replaceAll("</s>", "").replaceAll("\\s+", "").length() > 0 )
					sb = sb.append( str+ "\n\n");				
			}
			
			FileUtility.writeInFile(dirPath + "/" + fileNames[i], sb.toString(), false);
		}
	}
	
	
	
	
	
	public static void analyseCrossClausalRE( String[] arrSenSegmented, String aimedDataFileName,
			String outputFile ) throws Exception{
			
		String line = "";
		FileUtility.writeInFile( outputFile, "", false);
		
		BufferedReader inputAImedFullData = new BufferedReader(new FileReader(new File(aimedDataFileName)));
		int senNo = 0;
		StringBuilder sb = new StringBuilder();
		int z=0;
		
		while (( line = inputAImedFullData.readLine()) != null){
			// read abstract id, sentence id and sentence			
			String senID = inputAImedFullData.readLine().replaceAll("Sentence Id:", "").trim();
			String sentence = inputAImedFullData.readLine().trim();
			inputAImedFullData.readLine();
		
			ArrayList<String> listEnt = new ArrayList<String>();			
			// read entities
			line = inputAImedFullData.readLine().trim();
						
			// no entity
			if ( line.isEmpty() )
				line = inputAImedFullData.readLine();
			else {
				while ( line != null && !(line = line.trim()).isEmpty() ){
					listEnt.add(line);
					listEnt.add(inputAImedFullData.readLine());
					listEnt.add(inputAImedFullData.readLine());
					line = inputAImedFullData.readLine();
				}
			}
			
			ArrayList<String> listRel = new ArrayList<String>();
			// read relations
			line = inputAImedFullData.readLine().trim();
			
			// no relation
			if ( line.isEmpty() )
				line = inputAImedFullData.readLine();
			else {
				while ( line != null && !(line = line.trim()).isEmpty() ){
					
					listRel.add(line);
					line = inputAImedFullData.readLine();
				}
			}
		
			System.out.println(senID);
						
			if ( listRel.size() > 0 ){
				int totalClause = 0, x=-1;
				
				if ( senID.contains("d30.s251") )
					x= -1;
				while( (x=arrSenSegmented[senNo].indexOf("<C>", x+1)) >= 0 )
					totalClause++;
				
				if ( totalClause > 1 ){
					sb.append(senID + "\n");
					sb.append(sentence + "\n\n");
					sb.append(arrSenSegmented[senNo].trim() + "\n\n");
					
					for ( int y=0; y<listEnt.size(); y++ )
						sb.append(listEnt.get(y) + "\n");
					
					sb.append("\n");
					
					for ( int y=0; y<listRel.size(); y++ )
						sb.append(listRel.get(y) + "\n");
					
					sb.append("\n");
					
					z++;
				}
			}
			
			senNo++;
		}
		
		FileUtility.writeInFile( outputFile, sb.toString(), false);
		inputAImedFullData.close();
		System.out.println(z);
	}
	
	
	
	
	
	
	
	
	public static void filterByWordList( String fileName ) throws IOException{
		
		// TODO: look for new words for filtering. read the bic II papers and Tori et al. 2009 paper
		
		StringBuilder sb = new StringBuilder();
		ArrayList<String> tempList =FileUtility.readFileLines( "/media/Study/workspace/BioEnEx/experimental_results/pgn_filter_word_list_extended" );
		String[][] listWordFilters = new String[tempList.size()][];
		int maxLenOfFilterWords = 0;
		String 	GREEKlettersRegEx = "(alpha|beta|gamma|delta|epsilon|zeta|eta|theta|iota|kappa|lambda|mu|nu|xi|omicron|sigma|tau|upsilon|omega)",
				ROMANRegEx = "[IVXDLCM]+";
		
		for ( int i=0; i<listWordFilters.length; i++ ){
			listWordFilters[i] = tempList.get(i).split("\\s+");
			if ( maxLenOfFilterWords < listWordFilters[i].length )
				maxLenOfFilterWords = listWordFilters[i].length;
		}
		
		ArrayList<String> allLines =FileUtility.readFileLines(fileName);
		
		for ( int k=0; k<allLines.size(); k++ ){
			
			if ( allLines.get(k).equals("BC2GM027092021|18 18|I") )
				k = k+1-1;
				
			String[] str = allLines.get(k).toLowerCase().split("\\|");
			
			if ( str.length < 3 )
				continue;
			
			str = str[2].split("\\s+");
							
			boolean isRemoved = false;
			
			if ( maxLenOfFilterWords >= str.length ){
				for ( int x=0; x<listWordFilters.length; x++ ){
					
					if ( listWordFilters[x].length == str.length ){
						int y = 0;
						for ( y=0; y<str.length; y++ )
							if ( !str[y].contains(listWordFilters[x][y]) )
								break;
						
						if ( y == str.length ){
							allLines.remove(k);
							k--;
							isRemoved = true;
							break;
						}
					}
					
				}
					
			}
			
			if ( !isRemoved && str.length == 1 ){
				if ( str[0].matches(GREEKlettersRegEx) || str[0].matches(ROMANRegEx.toLowerCase()) ){
					allLines.remove(k);
					k--;
					isRemoved = true;
				}
			}
			
			if ( !isRemoved )
				sb.append(allLines.get(k)).append("\n");				
		}
		
		FileUtility.writeInFile("/media/Study/workspace/BioEnEx/experimental_results/anno_train_SSC_I_test_BiC_II_filtered_by_list", sb.toString(), false);
	}
	
	
	
	//----------------------------------------
	
	
	public void selectSenIndexesRandomly ( String senSegmentedFileName, String outputFile,
			String fullDataFileName ) throws Exception{
		
		ArrayList<Sentence> listOfSentences = Sentence.readFullData(fullDataFileName, "", "", "", false);
				
		// create folds of sen id indexes by random index selection
	    Random randomGenerator = new Random();
	    int tempInt = listOfSentences.size();
	    
	    StringBuilder sb = new StringBuilder("");
	    ArrayList<Integer> listOfRandSenIndx = new ArrayList<Integer>();
	    
	    for (int idx = 0; idx <50; idx++){
	    	
	    	int randomInt = randomGenerator.nextInt(tempInt);
	    	// get the randomly selected sen id index
	    	if ( !listOfRandSenIndx.contains(randomInt) )
	    		listOfRandSenIndx.add(randomInt);
	    	else
	    		idx--;
	    }
	    
	    sb = new StringBuilder();		
		String[] arrSenSegmented = new ClauseAnalyser().readSegmentedData(senSegmentedFileName);
		
		for ( int s=0; s<listOfRandSenIndx.size(); s++ ){
			
			int i = listOfRandSenIndx.get(s);
			String[] tempArr = arrSenSegmented[i].replaceAll("<C>", "").split("</C>");
			String temp = "";
			
			// we leave the last item of the array as it is empty
			for ( int k=0; k<tempArr.length-1; k++ ){
				String[][] wp = Common.separateTokenAndPos(tempArr[k].trim(), true);
				
				for ( int x=0; x<wp.length; x++ )
					temp = temp + wp[x][0];
				
				temp = temp + "</C>";
			}
			
			String line = "";
			int k = -1;
			
			/**
			 * Calculating clause boundaries.
			 * TODO: These clause boundaries are not always exact, since some of the word spellings
			 * are modified by the parser.
			 */
			while ( (k=temp.indexOf("</C>", k+1)) >= 0 )
				line = line + " " + k;
			
			sb.append(listOfSentences.get(i).senID + "\n" + line.trim() + "\n\n")
				.append(listOfSentences.get(i).text  + "\n\n")
				.append(arrSenSegmented[i].trim() + "\n\n");
		}
		

	   FileUtility.writeInFile("randSenWithClause", sb.toString(), false);
	}
	
}
