package Kernels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import Clause.ClauseAnalyser;
import DataProcessor.CueDetector;
import Others.CommonExtra;
import Structures.*;
import Utility.*;


public class TKOutputPST {

	public static int totalRelPos = 0, totalRelNeg = 0;
	public static ArrayList<String> listAllSenIDs = new ArrayList<String>();

	/* remove
	String[] arrSenSegmented;
	int segSenInd = 0;
	int ccr = 0;
	//*/
	
	/**
	 * 
	 * @param inClauseBoundFileName
	 * @param listAllSenIDs
	 * @return
	 */
	public int[][] getClauseBoundOfAllSen ( String inClauseBoundFileName ) {
		
		int[][] arrClauseBoundOfSen = null;		
		// if clause segmented data exists
		if ( inClauseBoundFileName != null && new File(inClauseBoundFileName).exists() ) {
			ArrayList<String[]> listClauseBoundOfAllSen = new ClauseAnalyser().readClauseBoundaries(inClauseBoundFileName);		 
			listAllSenIDs = new Common().separateSenIDsFromClauseBound(listClauseBoundOfAllSen);
			arrClauseBoundOfSen = new Common().separateClauseBoundFromSenIDs(listClauseBoundOfAllSen);
			listClauseBoundOfAllSen.clear();
		}
			
		return arrClauseBoundOfSen;
	}
	
	/**
	 * 
	 * @param parsedFileName
	 * @param aimedDataFileName
	 * @param outputFile
	 * @throws Exception
	 */
	public void generateTKoutputForPST( ArrayList<Sentence> listSentence,
			String outputFile, boolean isResolveOverlappingEntities,
			ClauseAnalyser.eDataFilterOption relToBeConsidered,
			String inClauseBoundFileName, String entPairFileName ) throws Exception{
			
		String line = "";
		FileUtility.writeInFile( outputFile, "", false);	
		totalRelPos = 0;
		totalRelNeg = 0;
		
		///* remove
		//arrSenSegmented = new ClauseAnalyser().readSegmentedData(TKOutputGenerator.segmentedFilName);
		
		int[][] arrClauseBoundOfSen = getClauseBoundOfAllSen(inClauseBoundFileName);
		
		for ( int s=0; s<listSentence.size(); s++ ) {			

			Sentence objCurSen = listSentence.get(s);
			
			if ( isResolveOverlappingEntities )
				CommonExtra.resolveOverlappingEntities(objCurSen.listOfEntities, true);
			
			for ( int k=0; k < objCurSen.listRels.size(); k++ ){
				int xx=0;
			
				if ( !objCurSen.listRels.get(k).arg1.equals(objCurSen.listRels.get(k).arg2) ) {
					for ( int e=0; xx<2 && e<objCurSen.listOfEntities.size(); e++ ) {
						if ( objCurSen.listRels.get(k).arg1.equals(objCurSen.listOfEntities.get(e).id) 
									|| objCurSen.listRels.get(k).arg2.equals(objCurSen.listOfEntities.get(e).id) )
							xx++;
					}
					
					if ( xx < 2 )
						System.out.println(objCurSen.listRels.get(k));
				}
			}
			
			int senIndex = listAllSenIDs.indexOf(objCurSen.senID);
			
			// remove
			 //segSenInd = senIndex;
			
			if ( objCurSen.listOfEntities.size() > 1 ){												
				line = generatePstTKouputForSen(objCurSen, 
						relToBeConsidered, senIndex > 0 ? arrClauseBoundOfSen[senIndex] : null, entPairFileName);
				FileUtility.writeInFile( outputFile, line, true);
			}
		}
		
	}
	
	/**
	 * 
	 * @param ent
	 * @param objCurSen
	 * @param dt
	 * @return
	 */
	private static ArrayList<Integer> hasAntiPositiveGovernor ( Entity ent, Sentence objCurSen ) {
		
		DepTreeNode headOfEnt1 = objCurSen.depTree.getHeadWordFromWordBoundaries( ent.getAllWordIndexes(), true, objCurSen);
		
		if ( headOfEnt1 == null ) {
			System.out.println("Empty head word found for   " + ent.printString());
			return null;
		}
		
		if ( headOfEnt1.getParentsWordIndexes() == null || headOfEnt1.getParentsWordIndexes().isEmpty() )
			return null;
		
		for ( int p=0; p<headOfEnt1.getParentsWordIndexes().size(); p++ ) {
			if ( !CueDetector.listOfGovernWordsForNonRelatedEnt.contains(objCurSen.depTree.allNodesByWordIndex[headOfEnt1.getParentsWordIndexes().get(p)].lemma.toLowerCase()) )
					return null;
		}
		
		return headOfEnt1.getParentsWordIndexes();	
	}
	

	static int negInstanceNo = 0, negSenIgnored = 0;
	static ArrayList<String> listOfDiscardedNegatedSentence = new ArrayList<String>();
	
	
	/**
	 * 
	 * @param arrClauseBoundOfSen
	 * @param relToBeConsidered
	 * @param e1
	 * @param e2
	 * @return
	 */
	public static boolean skipInstance ( int[] arrClauseBoundOfSen, ClauseAnalyser.eDataFilterOption relToBeConsidered, 
			Entity e1, Entity e2, Sentence curSen, int relIndex ) {
		
		if ( e1 == null || e2 == null ) {
			System.out.println("One of the entities is missing in " + curSen.listRels.get(relIndex).printString());
			return true;
		}

		// if entities don't have boundary overlap yet part of the same token
		if ( !DataStrucUtility.hasOverlap( e1.boundaries, e2.boundaries) &&
				!DataStrucUtility.getCommonItems(e1.getAllWordIndexes(), e2.getAllWordIndexes()).isEmpty() ) {
			System.out.println("Both the entities share the same token." + curSen.listRels.get(relIndex).printString());
			return true;
		}
		
		//*
		if ( // curSen.text.toLowerCase().matches(".*\\b(no|not)\\b.*") // Stage 1 baseline 
				TKOutputGenerator.listOfSenIdsToIgnore.contains(curSen.senID) && !TKOutputGenerator.isTestData
				) {
			if ( !listOfDiscardedNegatedSentence.contains(curSen.senID) ) {
				listOfDiscardedNegatedSentence.add(curSen.senID);
				negSenIgnored++;
			}
			return true;
		}
		//*/
		
				
		// Sun et. al. ACL 2011
		if ( PairFilterCriteria.notMoreThan2EntInBet && TKOutputGenerator.isTestData && curSen.getNumberOfEntInBetween(e1, e2) > 2 )
			return true;

		/*
		if ( !TKOutputGenerator.isTestData )
		{
			if ( !curSen.getPolarityOfRelation(e1, e2) ) {
				negInstanceNo++;
			
				if ( listOfDiscardedNegPairs.contains(negInstanceNo) )
					return true;
			}
			
			//entitiesWithSameName = true;
			//antiPositiveGovernors = true;
			//alias= true;
		} 
		*/
		
		
		if ( PairFilterCriteria.entitiesWithSameName ) {
			if ( e1.name.equalsIgnoreCase(e2.name) &&
		 		// if there is no special character between the names
				! (e2.startIndex - e1.endIndex == 2 || e1.startIndex - e2.endIndex == 2) 
			//	& !curSen.isCharPartOfAnEntity(e1.startIndex-1) && !curSen.isCharPartOfAnEntity(e2.startIndex-1) 
				)
				
			return true;
		}
		
		if ( PairFilterCriteria.antiPositiveGovernors ) {
			ArrayList<Integer> antiPositiveGovernorofEnt1 = hasAntiPositiveGovernor(e1, curSen);
			ArrayList<Integer> antiPositiveGovernorofEnt2 = hasAntiPositiveGovernor(e2, curSen);
			if (  antiPositiveGovernorofEnt1 != null && antiPositiveGovernorofEnt2 != null 
					&& 
					!(antiPositiveGovernorofEnt1.containsAll(antiPositiveGovernorofEnt2)
						|| antiPositiveGovernorofEnt2.containsAll(antiPositiveGovernorofEnt1)	) )
				return true;
		}
		
		if ( PairFilterCriteria.alias ) {
			String textWithoutSpace = curSen.text.replaceAll("\\s+", "");
			if ( e2.endIndex+1 < textWithoutSpace.length() && e1.endIndex+1 < textWithoutSpace.length()
					&& e2.startIndex > 0 && e1.startIndex > 0 ) {
				if ( (e2.startIndex - e1.endIndex == 2 && String.valueOf(textWithoutSpace.charAt(e2.endIndex+1)).equals(")")
					&& String.valueOf(textWithoutSpace.charAt(e2.startIndex-1)).equals("(") ) 
					|| (e1.startIndex - e2.endIndex == 2 && String.valueOf(textWithoutSpace.charAt(e1.endIndex+1)).equals(")")
							&& String.valueOf(textWithoutSpace.charAt(e1.startIndex-1)).equals("(") )  )
				return true;
			}
			/*
			if ( ( e1.startIndex > 0 && String.valueOf(textWithoutSpace.charAt(e1.startIndex-1)).matches("[(]") ) 
					|| ( e2.startIndex > 0 && String.valueOf(textWithoutSpace.charAt(e2.startIndex-1)).matches("[(]") )  )
				return true;
			//*/
		}
		
		if ( PairFilterCriteria.hasOverlap && DataStrucUtility.hasOverlap( e1.boundaries, e2.boundaries) )
			return true;
		
		if ( PairFilterCriteria.ofType_Gene_protein_RNA && e1.getNEcategory().equalsIgnoreCase("Gene/protein/RNA") && e2.getNEcategory().equalsIgnoreCase("Gene/protein/RNA") )
			return true;
				
		// TODO: add option for filtering because of mismatch of the required types for the arguments
		
		if ( arrClauseBoundOfSen != null &&
				relToBeConsidered == ClauseAnalyser.eDataFilterOption.DATA_WITH_CROSS_CLAUSAL_REL 
				&& !ClauseAnalyser.isCrossClausalRel(arrClauseBoundOfSen, 
						e1.boundaries, e2.boundaries) )
			return true;
	
		if ( arrClauseBoundOfSen != null &&
			relToBeConsidered == ClauseAnalyser.eDataFilterOption.DATA_WITH_INTRA_CLAUSAL_REL ) {
		
			if ( ClauseAnalyser.isCrossClausalRel(arrClauseBoundOfSen, e1.boundaries, e2.boundaries) ) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param senID
	 * @param sentence
	 * @param listEnt
	 * @param listRel
	 * @param pst
	 * @return
	 * @throws IOException 
	 */
	private String generatePstTKouputForSen( Sentence objCurSen,  
			ClauseAnalyser.eDataFilterOption relToBeConsidered, int[] arrClauseBoundOfSen, String entPairFileName
			) throws IOException{
		
		String output = "";
		
		//System.out.println(senID);
		// for each pair of entities, find minimal subtrees and output it with 1 or 0
		// 1 represents there exists a relation between those entities
		for ( int r=0; r < objCurSen.listRels.size(); r++ ){
			Entity e1 = objCurSen.getEntityById(objCurSen.listRels.get(r).arg1);
			Entity e2 = objCurSen.getEntityById(objCurSen.listRels.get(r).arg2);
				
			// checking relation type
			if ( skipInstance(arrClauseBoundOfSen, relToBeConsidered, e1, e2, objCurSen, r) ) {
				if ( objCurSen.listRels.get(r).isPositive
						//&&
						//hasAntiPositiveGovernor(e1, objCurSen, dt) != null && hasAntiPositiveGovernor(e2, objCurSen, dt)  != null 
						) {
					System.out.println(objCurSen.senID + ": " + e1.name + " || " + e2.name);
		//			skipInstance(arrClauseBoundOfSen, relToBeConsidered, e1, e2, objCurSen);
					System.out.println(objCurSen.text + "\n");
				}
				continue;	
			}
	
			PhraseStructureTree temp = objCurSen.psgTree.clone(objCurSen);
			PhraseStrucTreeNode node = null;
			
			if ( !PairFilterCriteria.hasOverlap || !DataStrucUtility.hasOverlap( e1.boundaries, e2.boundaries) ) {
		//		System.out.println(objCurSen.listRels.get(r).printString());
				node = temp.findPathEnclosedTreeWithEntities( e1, e2);
			}
		
			if ( !objCurSen.listRels.get(r).isPositive )
				totalRelNeg++;		
			else
				totalRelPos++;
				
			if ( node != null ){
				/*
				  if ( !objCurSen.listRels.get(r).isPositive ) {
				 
				  // start: remove
					ccr++;
					FileUtility.writeInFile("ccr_posi_aimed", "ccr : " + ccr + "\n" + senID + ": " + ent1 + " || " + ent2 + "\n" +
							listEnt.get(i).id + "\t" + listEnt.get(j).id + "\n\n" , true);
					FileUtility.writeInFile("ccr_posi_aimed", arrSenSegmented[segSenInd].trim() + "\n\n" , true);					
				}
				else {
					ccr++;
					FileUtility.writeInFile("ccr_posi_aimed", "ccr : " + ccr + "\n" + senID + ": " + ent1 + " || " + ent2 + "\n" +
							listEnt.get(i).id + "\t" + listEnt.get(j).id + "\n\n" , true);
					FileUtility.writeInFile("ccr_posi_aimed", arrSenSegmented[segSenInd].trim() + "\n\n" , true);
					
				}
				//*/
			
				String str = " |BT| " + node.printTree( true, true, false, false, false )
						.replaceAll("\\s+", " ") + " |ET|\n";
				
				output += TKOutputDT.getOutputForSingleInstance( str, objCurSen.listRels.get(r).isPositive);
				
				if ( entPairFileName != null && !entPairFileName.isEmpty() )
					FileUtility.writeInFile(entPairFileName, e1.id + "\t" + e2.id + "\n", true);			
			}
			else {
				if ( entPairFileName != null && !entPairFileName.isEmpty() )
					FileUtility.writeInFile(entPairFileName, e1.id + "\t" + e2.id + "\n", true);
				
				if ( objCurSen.listRels.get(r).isPositive )
					System.out.println(objCurSen.senID + ": " + e1.name + " || " + e2.name);
				
				output += TKOutputDT.getOutputForSingleInstance( "\n", objCurSen.listRels.get(r).isPositive);
			}			
		}
		
		return output;
	}
	
}
