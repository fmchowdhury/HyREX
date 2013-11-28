package Kernels;

import java.util.ArrayList;

import Structures.*;
import Utility.*;

public class PatternsDepRelFromGraph {
	
	static ArrayList<ArrayList<String>> listOfAllPatterns = new ArrayList<ArrayList<String>>();
	static ArrayList<Boolean> listOfLabelsForPatterns = new ArrayList<Boolean>();
	
	
	String vectOutFile = CommonUtility.OUT_DIR + "/all_vect_by_pair";
	
	/**
	 * 
	 * @param isSimplifyEntity
	 * @param parsedFileName
	 * @param aimedDataFileName
	 * @param outputFile
	 * @param medtType
	 * @param isRemoveOverlappingEntities
	 * @throws Exception
	 */
	public void collectAllDepRelPatternsFromTrainData( ArrayList<Sentence> listSentence, 
			boolean discardDepRelUsingProbabilityInReducedGraph ) throws Exception{
		
		listOfAllPatterns = new ArrayList<ArrayList<String>>();
		listOfLabelsForPatterns = new ArrayList<Boolean>();
		
		for ( int s=0; s<listSentence.size(); s++ ) {			
			
			Sentence objCurSen = listSentence.get(s);
			
			// only those sentences are taken into account which has more than one entity annotations
			if ( objCurSen.listOfEntities.size() > 1 ) {
		
				for ( int r=0; r < objCurSen.listRels.size(); r++ ){
					
					ArrayList<String> listOfDepRelsInReducedGraph = new ArrayList<String>();
					extractDepRelsInReducedGraph( objCurSen.listRels.get(r), objCurSen, 
							listOfDepRelsInReducedGraph, discardDepRelUsingProbabilityInReducedGraph , true);
					
					if ( listOfDepRelsInReducedGraph != null && !listOfDepRelsInReducedGraph.isEmpty() ) {
						listOfAllPatterns.add(listOfDepRelsInReducedGraph);
						listOfLabelsForPatterns.add(objCurSen.listRels.get(r).isPositive);
					}
				}
			}
		}
		
		purifyPatternsInGlobalList(discardDepRelUsingProbabilityInReducedGraph);
	}
	
	
	/**
	 * 
	 * @param graph
	 * @param objRel
	 * @param objCurSen
	 * @return
	 */
	private ArrayList<ArrayList<Integer>> extractNodesInReducedGraph( Relation objRel, Sentence objCurSen  ) {
	
	//	System.out.println(objRel.printString());
		
		Entity e1 = objCurSen.getEntityById(objRel.arg1);
		Entity e2 = objCurSen.getEntityById(objRel.arg2);
		
		if ( e1 == null || e2 == null ) {
			System.out.println("One of the entities is missing in " + objRel.printString());
			return null;
		}
		
		if ( DataStrucUtility.hasOverlap( e1.boundaries, e2.boundaries) )
			return null;
				
		/** - Locate the entity pairs.
		 * 	- Locate their heads.
		 */

		// NOTE: the 1st two elements of listOfWordIndexesConnectedToTheTargetNodes must be the head of the target entities
		int headOne = objCurSen.depGraph.findHeadFromListOfWordIndexes(e1.getAllWordIndexes());
		
		int headTwo = objCurSen.depGraph.findHeadFromListOfWordIndexes(e2.getAllWordIndexes());
		
		if ( headOne < 0 || headTwo < 0 )
			return null;
						
		ArrayList<Integer> listOfNodesAlreadyVisited = new ArrayList<Integer>();
		ArrayList<Integer> listOfLeastCommonParent = new ArrayList<Integer>();
		
		char dir = objCurSen.depGraph.findNodesBetCommonParentAndEntPairs( headOne, headTwo, 
				listOfNodesAlreadyVisited, listOfLeastCommonParent);
		
		// if the source and destination have connected path
		if ( dir != 'N' ) {		

			ArrayList<Integer> listOfNodesInReducedGraph = new ArrayList<Integer>();
			listOfNodesInReducedGraph.addAll(listOfNodesAlreadyVisited);
			
			for ( int i=0; i<listOfNodesAlreadyVisited.size(); i++ )
				listOfNodesInReducedGraph.addAll(objCurSen.depGraph.allNodesByWordIndex[listOfNodesAlreadyVisited.get(i)].childrenWordIndex);
			
			
			// if entity 2 is child of entity 1
			if ( dir == 'C' ) { 
				listOfNodesInReducedGraph.addAll(objCurSen.depGraph.allNodesByWordIndex[headOne].parentWordIndexes);
			}
			else if ( dir == 'P' ) { 
				listOfNodesInReducedGraph.addAll(objCurSen.depGraph.allNodesByWordIndex[headTwo].parentWordIndexes);
			}
			else if ( dir == 'M' ) {
				for ( int i=0; i<listOfLeastCommonParent.size(); i++ )
					listOfNodesInReducedGraph.addAll(objCurSen.depGraph.allNodesByWordIndex[listOfLeastCommonParent.get(i)].parentWordIndexes);
			}
			
			DataStrucUtility.removeDuplicateItems(listOfNodesInReducedGraph);
			
			ArrayList<ArrayList<Integer>> listNodesAndLCP = new ArrayList<ArrayList<Integer>>();
			listNodesAndLCP.add(listOfNodesInReducedGraph);
			listNodesAndLCP.add(listOfLeastCommonParent);
			
			return listNodesAndLCP;
		}
		
		return null;
	}
	
	
	/**
	 * 
	 * @param graph
	 * @param objRel
	 * @param objCurSen
	 * @return
	 */
	public ArrayList<ArrayList<Integer>> extractDepRelsInReducedGraph ( Relation objRel, Sentence objCurSen, ArrayList<String> listOfDepRelsInReducedGraph, 
			boolean discardDepRelUsingProbabilityInReducedGraph, boolean collectPatterns  ) {
		
		ArrayList<ArrayList<Integer>> listNodesAndLCP = extractNodesInReducedGraph( objRel, objCurSen);
	
		if ( listNodesAndLCP == null || listNodesAndLCP.get(0) == null ) {
			//if ( objRel.isPositive )
			//	System.out.println("No dependency pattern could be automatically extracted for the instance: " + objRel.printString());
			
			return null;
		}
		
		ArrayList<Integer> listOfNodesInReducedGraph = listNodesAndLCP.get(0);
		
		if ( listOfNodesInReducedGraph != null && listOfNodesInReducedGraph.size() > 0 ) {
		
		//	if ( objRel.isPositive )
			//	TPWF.posi++;
			
			for ( int i=0; i<listOfNodesInReducedGraph.size()-1; i++ ) {
				int k=i+1;
				
				int z = -1;
				// if there is parent-child relation
				if ( (z=objCurSen.depGraph.allNodesByWordIndex[listOfNodesInReducedGraph.get(i)].childrenWordIndex
						.indexOf(listOfNodesInReducedGraph.get(k))) > -1 ) {
					
					listOfDepRelsInReducedGraph.add( objCurSen.depGraph.allNodesByWordIndex[listOfNodesInReducedGraph.get(i)]
					                .relNamesWithChildren.get(z) );							
				}
				// if there is child-parent relation
				else if ( (z=objCurSen.depGraph.allNodesByWordIndex[listOfNodesInReducedGraph.get(i)].parentWordIndexes
						.indexOf(listOfNodesInReducedGraph.get(k))) > -1 ) {
					
					listOfDepRelsInReducedGraph.add( objCurSen.depGraph.allNodesByWordIndex[listOfNodesInReducedGraph.get(i)]
					                 .relNameWithParents.get(z) );
				}
			}
		}
		
		return listNodesAndLCP;
	}

	
	
	/**
	 * 
	 * @param listSinglePatternOfDepRels
	 * @return
	 */
	private void purifyPatternsInGlobalList ( boolean discardDepRelUsingProbabilityInReducedGraph ) {
		
		double threshold = 0.1;
		
		/**
		 	- Let DP is a dependency pattern set for a training instance whose label is L.
			- L is either + or -
			- for each R in DP
			-- calculate Pr(R|L)
			-- if Pr(R|L) < 0.5 then remove it from DP
			- end for
			- continue the above loop for all DPs
			- Let LIST_DP is the set of all DPs
			- if DP appears twice inside LIST_DP but have different labels, then remove both of them
			- if DP appears twice inside LIST_DP but have same labels, then remove one of them
			- if DP has only one dependency relation, then remove it
			- During model training or evaluation, search all the elements of LIST_DP whose all the dependency relations appear inside the reduced graph of the given training/test instance and a feature which corresponds to that element
		 */
		
		ArrayList<String> allRels = new ArrayList<String>();
		ArrayList<Double> allRelProbabilityForPosLab = new ArrayList<Double>();
		ArrayList<Double> allRelProbabilityForNegLab = new ArrayList<Double>();
		int totalPos = 0, totalNeg = 0;
		
		System.out.println("Total dep. patterns BEFORE filteirng = " + listOfAllPatterns.size());
		
		// add all relations in all dep. patterns into a single list
		for ( int dp=0; dp<listOfAllPatterns.size(); dp++ ) {
			if ( listOfLabelsForPatterns.get(dp) )
				totalPos++;
			else
				totalNeg++;
			
			for ( int r=0; r<listOfAllPatterns.get(dp).size(); r++ ) {
				int indx = allRels.indexOf(listOfAllPatterns.get(dp).get(r));
				if ( indx >= 0 ) {
					if ( listOfLabelsForPatterns.get(dp) )
						allRelProbabilityForPosLab.set( indx, allRelProbabilityForPosLab.get(indx) + 1);
					else 
						allRelProbabilityForNegLab.set( indx, allRelProbabilityForPosLab.get(indx) + 1);
				}
				else {
					allRels.add(listOfAllPatterns.get(dp).get(r));
					
					if ( listOfLabelsForPatterns.get(dp) ) {
						allRelProbabilityForPosLab.add(1.0);
						allRelProbabilityForNegLab.add(0.0);
					}
					else { 
						allRelProbabilityForPosLab.add(0.0);
						allRelProbabilityForNegLab.add(1.0);
					}
				}
			}
		}
		
		// calculate probability for the rules w.r.t. the class label		
		for ( int r=0; r<allRels.size(); r++ ) {
			allRelProbabilityForPosLab.set( r, allRelProbabilityForPosLab.get(r) / totalPos);
			allRelProbabilityForNegLab.set( r, allRelProbabilityForPosLab.get(r) / totalNeg);
		}
		
		/*
		 *  NOTE: Empirical results (on HPRD50) show that discarding dep rel using probabilities increases performance.
		 */
				
		// remove those relations from a dep pattern which have (< 0.5) probability with respect to the corresponding class label
		if ( discardDepRelUsingProbabilityInReducedGraph ) {
			System.out.println("Threshold = " + threshold);
			
			for ( int dp=0; dp<listOfAllPatterns.size(); dp++ ) {
				for ( int r=0; r<listOfAllPatterns.get(dp).size(); r++ ) {
					int indx = allRels.indexOf(listOfAllPatterns.get(dp).get(r));
					
					if ( listOfLabelsForPatterns.get(dp) ) {
						if ( allRelProbabilityForPosLab.get(indx) < threshold ) {
							listOfAllPatterns.get(dp).remove(r);
							r--;
						}
					}
					else if ( allRelProbabilityForNegLab.get(indx) < threshold ) {
						listOfAllPatterns.get(dp).remove(r);
						r--;
					}
				}
			}
		}
		
		// remove those dep patterns which occur multiple times but with different labels.
		// remove any duplicate copy of a dep pattern, having same label, if there exists any.
		for ( int dp=0; dp<listOfAllPatterns.size(); dp++ ) {
			
			if ( listOfAllPatterns.get(dp).size() < 2 ) {
				listOfLabelsForPatterns.remove(dp);
				listOfAllPatterns.remove(dp);
				dp--;
				continue;
			}
		}
		
		for ( int dp=0; dp<listOfAllPatterns.size(); dp++ ) {
			int indx = doesPatternHasDuplicate(listOfAllPatterns.get(dp), dp+1);
			if ( indx < 0 )
				continue;
			
			boolean removeOrigPat = false;
			if ( listOfLabelsForPatterns.get(dp) != listOfLabelsForPatterns.get(indx) )
				removeOrigPat = true;
			
			
			while ( indx > 0 && (removeOrigPat || listOfLabelsForPatterns.get(dp) == listOfLabelsForPatterns.get(indx)) ) {
				if ( listOfLabelsForPatterns.get(dp) != listOfLabelsForPatterns.get(indx) )
					removeOrigPat = true;
				
				listOfLabelsForPatterns.remove(indx);
				listOfAllPatterns.remove(indx);
				indx = doesPatternHasDuplicate(listOfAllPatterns.get(dp), indx);
				
			}
			
			if ( removeOrigPat ) {
				listOfLabelsForPatterns.remove(dp);
				listOfAllPatterns.remove(dp);
				dp--;
			}
		}
		
		System.out.println("Total dep. patterns AFTER filteirng = " + listOfAllPatterns.size());
	}
	
	
	
	/**
	 * 
	 * @param listOfDepRelPattern
	 */
	private int doesPatternHasDuplicate ( ArrayList<String> listOfDepRelPattern, int fromIndex ) {
		
		for ( int i=fromIndex; i<listOfAllPatterns.size(); i++ ) {
			if ( listOfAllPatterns.get(i).size() == listOfDepRelPattern.size() 
					&& DataStrucUtility.hasListOneAllElementsOfListTwo(listOfAllPatterns.get(i), listOfDepRelPattern) )
				return i;
		}
		
		return -1;
	}
	
}
