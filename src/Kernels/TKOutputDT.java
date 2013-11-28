package Kernels;

import java.io.IOException;
import java.util.ArrayList;

import Others.CommonExtra;
import Structures.*;
import Utility.*;

import Clause.ClauseAnalyser;


public class TKOutputDT {

	public static int total_rel = 0;

	/**
	 * 
	 * @param isSimplifyEntity
	 * @param tokenWithPos
	 * @param senID
	 * @param sentence
	 * @param listEnt
	 * @param listRel
	 * @param listDependencies
	 * @param medtType
	 * @param isResolveOverlappingEntitiesT
	 * @param relToBeConsidered
	 * @param arrClauseBoundOfSen
	 * @return
	 * @throws IOException 
	 */
	private String generateDtTKouputForSen( boolean isSimplifyEntity, Sentence objSen, 
			int medtType, boolean isResolveOverlappingEntities,
			ClauseAnalyser.eDataFilterOption relToBeConsidered, int[] arrClauseBoundOfSen, String entPairFileName) throws IOException{
		
		if ( isResolveOverlappingEntities )
			CommonExtra.resolveOverlappingEntities(objSen.listOfEntities, true);
		
		String output = "";
		// System.out.println(senID);
		
		 
		// for each pair of entities, find minimal subtrees and output it with 1 or 0
		// 1 represents there exists a relation between those entities
		for ( int r=0; r < objSen.listRels.size(); r++ ){
			
			Entity e1 = objSen.getEntityById(objSen.listRels.get(r).arg1);
			Entity e2 = objSen.getEntityById(objSen.listRels.get(r).arg2);
			
			if ( DataStrucUtility.hasOverlap( e1.boundaries, e2.boundaries) )
				continue;
				
			// checking relation type
			if ( TKOutputPST.skipInstance(arrClauseBoundOfSen, relToBeConsidered, e1, e2, objSen, r) )
				continue;
				
			DepTreeNode dn = null;
			if ( !DataStrucUtility.hasOverlap( e1.boundaries, e2.boundaries) ){
									
				dn = objSen.depTree.findMinimalSubTreeWithEntities(isSimplifyEntity, 
						e1.getAllWordIndexes(), e2.getAllWordIndexes(), medtType, true, objSen);
			}
			
			
			if ( dn != null ){
				
				String str = " |BT| " + dn.printTree(Common.isTrucEtAl2009Format, Common.isIncludeWord, Common.isIncludePOS, 
						Common.isIncludeRelName, Common.isIncludeLemma, 
						Common.isIncludePOSGeneral, Common.isIncludePharasalCat)
						.replaceAll("\\s+", " ") + " |ET|\n";
				
				output += getOutputForSingleInstance( str, objSen.listRels.get(r).isPositive);
				
				if ( entPairFileName != null && !entPairFileName.isEmpty() )
					FileUtility.writeInFile(entPairFileName, e1.id + "\t" + e2.id + "\n", true);
			}
			// in case if no shortest common dependency sub-tree can be constructed, add empty tree  
			else{
				if ( entPairFileName != null && !entPairFileName.isEmpty() )
					FileUtility.writeInFile(entPairFileName, e1.id + "\t" + e2.id + "\n", true);
				
				output += getOutputForSingleInstance( " |BT| |ET|\n", objSen.listRels.get(r).isPositive);
				
				if ( objSen.listRels.get(r).isPositive )
					System.out.println( objSen.senID + ": " +  e1.name + " || " +  e2.name);
			}
		}
				
		return output;
	}
	

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String getOutputForSingleInstance ( String str, boolean isPositiveInstance ) {
		
		if ( !isPositiveInstance )
			return "-1 " + str;
		else		
			return "1 " + str;
	}
	
	
	/**
	 * 
	 * @param listAbstracts
	 * @param fullDataFileName
	 * @param depParsedFileName
	 * @param psgParsedFileName
	 * @param tmpFullDataFileName
	 * @param tmpDepParsedFileName
	 * @param tmpPsgParsedFileName
	 * @throws Exception
	 */
	public void createFilesFor_i_th_Fold ( ArrayList<String> listAbstracts, 
			ArrayList<DependencyParseOfSen> listDepParseOfAllSen, ArrayList<CFGParseOfSen> listCFGParseOfAllSen,
			String tmpFullDataFileName, String tmpDepParsedFileName, String tmpPsgParsedFileName,
			double percentageOfTrainDataToBeUsed, ArrayList<Sentence> listSentence) throws Exception{
		
		StringBuilder sbFull = new StringBuilder(), sbPsgParsed = new StringBuilder(), sbDepParsed = new StringBuilder();
		
		// checking whether all the sentences have parsed data
		for ( int s=0; s<listSentence.size(); s++ ){
			if ( !listSentence.get(s).senID.equals(listDepParseOfAllSen.get(s).senID) ){
				System.err.println("Mismatch at " + listSentence.get(s).senID + " " + listDepParseOfAllSen.get(s).senID );
				System.exit(0);
			}
		}
		
		// for learning curve experiments
		if ( !TKOutputGenerator.isTestData && percentageOfTrainDataToBeUsed < 1.00 ){
			double limit = listAbstracts.size() * percentageOfTrainDataToBeUsed;
			ArrayList<String> tmp = new ArrayList<String>();
			// removing from end
			//for ( int a=listAbstracts.size()-1; a>limit; a-- )
				//listAbstracts.remove(a);
			// keeping from start 
			for ( int a=0; a<limit; a++ )
				tmp.add(listAbstracts.get(a));
			listAbstracts = tmp;
		}
		
		for ( int s=0; s<listSentence.size(); s++ ) {
		
			Sentence objCurSen = listSentence.get(s);
			
			if ( listAbstracts.contains(objCurSen.absID) ) {
				sbFull.append(objCurSen.printString());
				
				CFGParseOfSen objCFGParseOfSen = listCFGParseOfAllSen.get(s);
				sbPsgParsed.append(objCFGParseOfSen.printString());
		
				DependencyParseOfSen objDepParseOfSen = listDepParseOfAllSen.get(s);
				sbDepParsed.append(objDepParseOfSen.printString());
			}
		}
				
		FileUtility.writeInFile(tmpFullDataFileName, sbFull.toString(), false);
		FileUtility.writeInFile(tmpDepParsedFileName, sbDepParsed.toString(), false);
		FileUtility.writeInFile(tmpPsgParsedFileName, sbPsgParsed.toString(), false);
	}
	
	
	
	/**
	 * 
	 * @param isSimplifyEntity
	 * @param depParsedFileName
	 * @param fullDataFileName
	 * @param outputFile
	 * @param medtType
	 * @param isResolveOverlappingEntities
	 * @param relToBeConsidered
	 * @param inClauseBoundFileName
	 * @param entPairFileName
	 * @throws Exception
	 */
	public void generateTKoutput( boolean isSimplifyEntity, ArrayList<Sentence> listOfSentences,
			String outputFile, int medtType, boolean isResolveOverlappingEntities,
			ClauseAnalyser.eDataFilterOption relToBeConsidered,
			String inClauseBoundFileName, String entPairFileName ) throws Exception{
		
		FileUtility.writeInFile( outputFile, "", false);
		
		int[][] arrClauseBoundOfSen = new TKOutputPST().getClauseBoundOfAllSen(inClauseBoundFileName);
		
		generateTK(isSimplifyEntity, listOfSentences, outputFile, medtType, 
				isResolveOverlappingEntities, relToBeConsidered, TKOutputPST.listAllSenIDs, arrClauseBoundOfSen, 
				entPairFileName); 
		
		//System.out.println(totalRel);
	}
	
	
	
	/**
	 * This function is used by DT and jSRE representations 
	 * 
	 * @param isSimplifyEntity
	 * @param depParsedFileName
	 * @param fullDataFileName
	 * @param outputFile
	 * @param medtType
	 * @param isResolveOverlappingEntities
	 * @param relToBeConsidered
	 * @param listAllSenIDs
	 * @param arrClauseBoundOfSen
	 * @throws Exception
	 */
	public void generateTK ( boolean isSimplifyEntity, ArrayList<Sentence> listSentence,
			String outputFile, int medtType, boolean isResolveOverlappingEntities,
			ClauseAnalyser.eDataFilterOption relToBeConsidered, ArrayList<String> listAllSenIDs,
			int[][] arrClauseBoundOfSen, String entPairFileName ) throws Exception {
		
		StringBuilder sbSemEval2task8Format = new StringBuilder();
		
		total_rel = 0;
		
		String line = "";
		for ( int s=0; s<listSentence.size(); s++ ) {			

			Sentence objCurSen = listSentence.get(s);
			
			int senIndex = listAllSenIDs.indexOf(objCurSen.senID);
			
			// only those sentences are taken into account which has more than one entity annotations
			if ( objCurSen.listOfEntities.size() > 1 ) {
				if ( medtType > -1 ){
					line = new TKOutputDT().generateDtTKouputForSen( isSimplifyEntity,  
						objCurSen,
						medtType, isResolveOverlappingEntities,
						relToBeConsidered, senIndex > 0 ? arrClauseBoundOfSen[senIndex] : null, entPairFileName
								);
					
					FileUtility.writeInFile( outputFile, line, true);
				}
				else {
					sbSemEval2task8Format = new FeatVecCreatorForSLkernel().createSemEval2task8FormatInpPerSen(sbSemEval2task8Format,
							objCurSen, isResolveOverlappingEntities, relToBeConsidered,
							senIndex > 0 ? arrClauseBoundOfSen[senIndex] : null, entPairFileName);
				}					
			}
		}
		
		//System.out.println("Total relations: " + total_rel);
		// BioRelEx format
		if ( medtType < 0 )			
			FileUtility.writeInFile(outputFile, sbSemEval2task8Format.toString(), false);
	}
	
}
