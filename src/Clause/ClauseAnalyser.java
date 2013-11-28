package Clause;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import Structures.Sentence;
import Utility.*;


/**
 * NOTE: Split the parse trees as it is supplied by the syntactic parser without merging multiple parse trees.
 * Once they are splitted into clauses, merge them in the UnifiedFormatDataProcessor class. 
 * 
 * @author faisal
 *
 */
public class ClauseAnalyser {
	/*
	public static void main ( String[] args ) throws Exception {
	
		String corpus = "lll";
		String path = 
		new ClauseAnalyser().
		analyseCrossClausalRE("../../data/5_PPI_corpora/insSpace/aimed/aimed.parsed.bllip.edu_merged_corrected", 
				"../../data/5_PPI_corpora/insSpace/aimed/aimed.Full", 
				"../../data/5_PPI_corpora/insSpace/aimed/aimed.parsed.bllip.complete", 
				"../../data/5_PPI_corpora/insSpace/aimed/data_with_single_clause", 
				ClauseAnalyser.eDataFilterOption.DATA_WITH_CROSS_CLAUSAL_REL);
	}
	//*/

	public static enum eDataFilterOption{
		DATA_WITH_MULTIPLE_CLAUSE,
		DATA_WITH_SINGLE_CLAUSE,
		DATA_WITH_CROSS_CLAUSAL_REL,
		DATA_WITH_INTRA_CLAUSAL_REL,
		DATA_ALL
	}
	
	
	public static boolean isCrossClausalRel ( int[] arrClauseBoundOfSen, ArrayList<Integer> entOneIndx,
			ArrayList<Integer> entTwoIndx){
		
		return isCrossClausalRel(arrClauseBoundOfSen, DataStrucUtility.listToArray(entOneIndx), DataStrucUtility.listToArray(entTwoIndx));
	}
	
	/**
	 * 
	 * @param arrClauseBoundOfSen
	 * @param entOneBound
	 * @param entTwoBound
	 * @return
	 */
	public static boolean isCrossClausalRel ( int[] arrClauseBoundOfSen, int[] entOneIndx,
			int[] entTwoIndx){
		
		boolean isCCR = false;
		int clauseIdEntOne = 0;
		/*
		if ( Math.abs(entOneIndx[1] - entTwoIndx[0]) <= 2 )
			return false;
			*/
		for ( int i=0; i < arrClauseBoundOfSen.length; i++ )
			if ( arrClauseBoundOfSen[i] > entOneIndx[0]+1 ){
				clauseIdEntOne = i;
				break;
			}
		
		for ( int i=0; i < arrClauseBoundOfSen.length; i++ )
			if ( arrClauseBoundOfSen[i] > entTwoIndx[0]+1 ){
				if ( clauseIdEntOne == i )
					return false;
				else
					return true;
			}		
		
		return isCCR;
	}
	
	
	/**
	 * 
	 * @param inFileName
	 * @return
	 */
	public ArrayList<String[]> readClauseBoundaries ( String inClauseBoundFileName ){
		ArrayList<String> temp = FileUtility.readFileLines(inClauseBoundFileName);
		ArrayList<String[]> listClauseBoundOfAllSen = new ArrayList<String[]>();
		
		for ( int i=0; i<temp.size(); i++ ){
			String str = temp.get(i).trim();
			
			if ( !str.isEmpty() ){
				String[] w = str.split("\\s+");
				listClauseBoundOfAllSen.add(w);
			}
		}
	
		return listClauseBoundOfAllSen;
	}
	
	
	
	/**
	 * 
	 * @param senSegmentedFileName
	 * @param outputFile
	 * @throws Exception
	 */
	public void findClauseBoundariesOfAllSen( String senSegmentedFileName, String outputFile,
			ArrayList<Sentence> listSentence ) throws Exception{
			
		StringBuilder sb = new StringBuilder();		
		String[] arrSenSegmented = readSegmentedData(senSegmentedFileName);
		
		for ( int i=0; i<arrSenSegmented.length; i++ ){
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
			
		//	System.out.println(i + " " + line);
		//	System.out.println(i + " " + listSentence.get(i).senID);
			sb.append(listSentence.get(i).senID + " " + line.trim() + "\n");
		}
		
		FileUtility.writeInFile(outputFile, sb.toString(), false);
	}
	
	/**
	 * 
	 * @param senSegmentedFileName
	 * @param aimedDataFileName
	 * @param depParsedFileName
	 * @param psgParsedFileName
	 * @param outputFolder
	 * @param dfOpt
	 * @throws Exception
	 */
	public void analyseCrossClausalRE( String senSegmentedFileName, String aimedDataFileName,
			String parsedFileName, 
			String outputFolder, eDataFilterOption dfOpt ) throws Exception{
			
		String line = "";
		
		FileUtility.createDirectory(outputFolder);
		String[] arrSenSegmented = readSegmentedData(senSegmentedFileName); 
			
		BufferedReader inputParse = new BufferedReader(new FileReader(new File(parsedFileName)));
		BufferedReader inputAImedFullData = new BufferedReader(new FileReader(new File(aimedDataFileName)));
		ArrayList<String> listFullDataSen = new ArrayList<String>(),
			listDepParsed = new ArrayList<String>(),
			listPsgParsed = new ArrayList<String>();
		
		int senNo = 0;
		StringBuilder sbSegmentedData = new StringBuilder();
		StringBuilder sbFull = new StringBuilder(), sbParse = new StringBuilder();
		int z=0;
		
		while (( line = inputAImedFullData.readLine()) != null){
			sbFull = new StringBuilder();
			sbParse = new StringBuilder();
			
			/**
			 * ---########--- Reading full data
			 */
			sbFull.append(line + "\n");
			// read abstract id, sentence id and sentence
			String senID = inputAImedFullData.readLine();
			sbFull.append(senID + "\n");
			sbFull.append(inputAImedFullData.readLine() + "\n");
			sbFull.append(inputAImedFullData.readLine() + "\n");
		
			// read entities
			line = inputAImedFullData.readLine().trim();
					
			// no entity
			if ( line.isEmpty() )
				sbFull.append(inputAImedFullData.readLine() + "\n");
			else {
				while ( line != null && !(line = line.trim()).isEmpty() ){
					sbFull.append(line + "\n");
					sbFull.append(inputAImedFullData.readLine() + "\n");
					sbFull.append(inputAImedFullData.readLine() + "\n");
					line = inputAImedFullData.readLine();
				}
			}
			
			sbFull.append(line + "\n");
			// read relations
			line = inputAImedFullData.readLine().trim();
			int totRel = 0;
			
			// no relation
			if ( line.isEmpty() )
				sbFull.append(inputAImedFullData.readLine() + "\n");
			else {
				while ( line != null && !(line = line.trim()).isEmpty() ){
					totRel++;
					sbFull.append(line + "\n");
					line = inputAImedFullData.readLine();
				}
			}
			
			//sbFull.append(line + "\n");
						
			/**
			 * ---########--- Reading phrase structure tree parsed data
			 */
			
			//  read parsed data
			 			
			line = inputParse.readLine().trim();
					
			// no parse
			if ( line.isEmpty() )
				inputParse.readLine();
			else {
				while ( line != null && !line.trim().isEmpty() ){
					sbParse.append(line + "\n");
					line = inputParse.readLine();					
				}
			}
			
			/**
			 * ---########--- Reading dependency parsed data
			 */
			
		//	System.out.println(senNo + " " + senID);

			// read tokenWithPos
			sbParse.append(inputParse.readLine() + "\n");
			sbParse.append(inputParse.readLine() + "\n");
			
			// read dependencies
			line = inputParse.readLine().trim();

			// no dep rel
			if ( line.isEmpty() )
				sbParse.append(inputParse.readLine() + "\n");
			else {
				while ( line != null && !(line = line.trim()).isEmpty() ){
					sbParse.append(line + "\n");
					line = inputParse.readLine();
				}
			}

			/**
			 * -- calculating no. of clauses in the sentence
			 */
			
			int totalClause = 0, x=-1;			
			while( (x=arrSenSegmented[senNo].indexOf("<C>", x+1)) >= 0 )
				totalClause++;
			
			boolean isAppend = false;
			
			if ( dfOpt == eDataFilterOption.DATA_WITH_CROSS_CLAUSAL_REL  && totalClause > 1){
				isAppend = true;
			}
			else if ( dfOpt == eDataFilterOption.DATA_WITH_MULTIPLE_CLAUSE && totalClause > 1 ) {
				isAppend = true;				
			} 
			else if ( dfOpt == eDataFilterOption.DATA_WITH_SINGLE_CLAUSE && totalClause == 1 ) {
				isAppend = true;
			}
			else if ( dfOpt == eDataFilterOption.DATA_WITH_INTRA_CLAUSAL_REL ) {
				if ( totalClause == 1 )
					isAppend = true;
			}
			else if ( dfOpt == eDataFilterOption.DATA_ALL ) {
				isAppend = true;
			}
			
			if ( isAppend ){
				listFullDataSen.add(sbFull.toString());
				listDepParsed.add(sbParse.toString());
				listPsgParsed.add(sbParse.toString());
				
				sbSegmentedData.append(arrSenSegmented[senNo].trim() + "\n\n");				
				z++;
			}
			
			senNo++;
		}
				
		aimedDataFileName = outputFolder + "/" + aimedDataFileName.substring( aimedDataFileName.lastIndexOf("/") + 1);
		parsedFileName = outputFolder + "/" + parsedFileName.substring( parsedFileName.lastIndexOf("/") + 1);
		senSegmentedFileName = outputFolder + "/" + senSegmentedFileName.substring( senSegmentedFileName.lastIndexOf("/") + 1);
		
		sbFull = DataStrucUtility.arrayListToStringBuilder(listFullDataSen, "\n");
		sbParse = DataStrucUtility.arrayListToStringBuilder(listPsgParsed, "\n");
		
		FileUtility.writeInFile( aimedDataFileName, sbFull.toString(), false);
		FileUtility.writeInFile( parsedFileName, sbParse.toString(), false);;
		FileUtility.writeInFile( senSegmentedFileName, sbSegmentedData.toString(), false);
		
		inputAImedFullData.close();
		inputParse.close();
		
		//System.out.println(z);
	}
	
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public String[] readSegmentedData( String fileName ) {
		String str = FileUtility.readFileContents(fileName);
		str = str.replaceAll("<T>", "").replaceAll("<P>", "").replaceAll("</T>", "")
		.replaceAll("</P>", "").replaceAll("<S>", "").trim();
		
		return str.split("</S>");
	}
}
