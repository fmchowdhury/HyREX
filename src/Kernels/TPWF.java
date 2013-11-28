package Kernels;

import java.io.IOException;
import java.util.ArrayList;

import Clause.ClauseAnalyser;
import Structures.*;
import Utility.*;

public class TPWF {

	/**
	 * If a sentence matches one of the relation patterns then -
	 * - Construct the dependency graph of the sentence
	 * - All nodes in the shortest path connecting the target pairs must be retained
	 * "Isolation of P1 and its binding specificity with P2" 
	 * - All nodes in the shortest path connecting the trigger word and target pairs must be retained
	 * "P1 activates P2 and inhibits P3"
	 * - All nodes satisfying the 3 rules of MEDT kernel must be retained
	 * - All negative nodes which are directly connected to the target pairs or trigger word or to their immediate parents must be retained. 
	 * 		In the latter case, the corresponding parent should be also kept. 
	 * - All the other nodes would be removed.
	 * - Lets call it "candidate graph"
	 * 
	 * - Construct feature set using e-walks and v-walks for the candidate graph
	 */
	
	/*
	 * we change the pos of target entities in ENT_T, but leave the pos of other words as it is
	 * we consider e1-v-e2 and e2-v-e1 both as the same feature 
	 */
	
	
	public static void init() throws IOException {
		GenericFeatVect.init();
	}
	
	
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
	public void generateTPWFvectorOutput( ArrayList<Sentence> listSentence,
			String outputFile, int medtType, String entPairFileName,				
			ClauseAnalyser.eDataFilterOption relToBeConsidered, String inClauseBoundFileName ) throws Exception{
		
		boolean useWalkFeatures = true, 
				useRegExPatterns = false, 
				useDepPatterns = true, 
				useTriggers = true, 
				useNegativeCues = true,
						
				discardDepRelUsingProbabilityInReducedGraph = false,
				triggersFromWholeRGinsteadOfLCP = true;				
		
		if ( TextUtility.isEmptyString(TKOutputGenerator.triggerFileName) )
			useTriggers = false;
		
		String str = "";
		if ( discardDepRelUsingProbabilityInReducedGraph )
			str += "discardDepRelUsingProbabilityInReducedGraph ";
		if ( useWalkFeatures )
			str += "WalkFeatures ";
		if ( useRegExPatterns )
			str += "RegExPatterns ";
		if ( useDepPatterns )
			str += "DepPatterns ";
		if ( useTriggers )
			str += "Triggers ";
		if ( triggersFromWholeRGinsteadOfLCP )
			str += "TriggersFromWholeRGinsteadOfLCP ";
		if ( useNegativeCues )
			str += "NegativeCues ";
		
		System.out.println(str);
		
		PatternsDepRelFromGraph clsWVG = new PatternsDepRelFromGraph();
		if ( PatternsDepRelFromGraph.listOfAllPatterns.size() == 0 ) {
			clsWVG.collectAllDepRelPatternsFromTrainData(listSentence, discardDepRelUsingProbabilityInReducedGraph);
		}
		
		int[][] arrClauseBoundOfSen = new TKOutputPST().getClauseBoundOfAllSen(inClauseBoundFileName);
		
		FileUtility.writeInFile( outputFile, "", false);
		
		// read trigger word list
		Triggers.readTriggersAndNegativeWord();
		
		for ( int s=0; s<listSentence.size(); s++ ) {
			
			Sentence objCurSen = listSentence.get(s);			
			int senIndex = TKOutputPST.listAllSenIDs.indexOf(objCurSen.senID);
			
			// only those sentences are taken into account which has more than one entity annotations
			if ( objCurSen.listOfEntities.size() > 1 ) {
				generateVectorForSen( objCurSen, 
						medtType, entPairFileName, discardDepRelUsingProbabilityInReducedGraph,
						useWalkFeatures, useRegExPatterns, useDepPatterns, useTriggers, triggersFromWholeRGinsteadOfLCP,
						useNegativeCues, 
						relToBeConsidered, senIndex > 0 ? arrClauseBoundOfSen[senIndex] : null );
			}
		}
		
		FileUtility.writeInFile( outputFile, GenericFeatVect.getInstanceVectors(), false);
	}
	
		
	/**
	 * 
	 * @param tokenWithPos
	 * @param senID
	 * @param sentence
	 * @param listEnt
	 * @param listRel
	 * @param listDependencies
	 * @param medtType
	 * @param entPairFileName
	 * @return
	 * @throws IOException
	 */
	private void generateVectorForSen( Sentence objCurSen, 
				int medtType, String entPairFileName, boolean discardDepRelUsingProbabilityInReducedGraph,
				boolean useWalkFeatures, boolean useRegExPatterns, boolean useDepPatterns, boolean useTriggers,
				boolean triggersFromWholeRGinsteadOfLCP, boolean useNegativeCues, 				
				ClauseAnalyser.eDataFilterOption relToBeConsidered, int[] arrClauseBoundOfSen ) throws IOException{
		
		// for each pair of entities, find minimal subtrees and output it with 1 or 0
		// 1 represents there exists a relation between those entities
		for ( int r=0; r < objCurSen.listRels.size(); r++ ){
			
			Entity e1 = objCurSen.getEntityById(objCurSen.listRels.get(r).arg1);
			Entity e2 = objCurSen.getEntityById(objCurSen.listRels.get(r).arg2);
		
			// checking relation type
			if ( TKOutputPST.skipInstance(arrClauseBoundOfSen, relToBeConsidered, e1, e2, objCurSen,
					r) )
				continue;
			
			if ( !objCurSen.listRels.get(r).isPositive )
				TKOutputPST.totalRelNeg++;		
			else
				TKOutputPST.totalRelPos++;
			
			boolean isSet = setInpVectFromDepGraphOfPairsAndTrigger(objCurSen.listRels.get(r), objCurSen, medtType, 
					discardDepRelUsingProbabilityInReducedGraph,
					useWalkFeatures, useRegExPatterns, useDepPatterns, useTriggers, triggersFromWholeRGinsteadOfLCP,
					useNegativeCues, e1, e2);
			
			if ( isSet )
				GenericFeatVect.listOfAllInstancePolarity.add(objCurSen.listRels.get(r).isPositive ? 1 : -1);
			//*/
			
			if ( !TextUtility.isEmptyString(entPairFileName) ) {
				if ( isSet )
					FileUtility.writeInFile(entPairFileName, e1.id + "\t" + e2.id + "\n", true);
				/*
				//if ( !str.isEmpty() )  
				if ( !isSet )
					FileUtility.writeInFile(entPairFileName, objCurSen.listRels.get(r).printString() + "\tFOUND\n", true);
				else {
					FileUtility.writeInFile(entPairFileName, objCurSen.listRels.get(r).printString() + "\tNOT_FOUND\n", true);
				}
				*/
			}
		}
	}
	
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
	 * @param isResolveOverlappingEntities
	 * @param relToBeConsidered
	 * @param arrClauseBoundOfSen
	 * @return
	 * @throws IOException 
	 */
	public boolean setInpVectFromDepGraphOfPairsAndTrigger( Relation objRel, Sentence objCurSen, 
			int medtType, boolean discardDepRelUsingProbabilityInReducedGraph,
			boolean useWalkFeatures, boolean useRegExPatterns, boolean useDepPatterns, boolean useTriggers,
			boolean triggersFromWholeRGinsteadOfLCP, boolean useNegativeCues, 
			Entity e1, Entity e2
	) throws IOException{
		 
		ArrayList<Integer> listFeatIndsOfCurInp = new ArrayList<Integer>(), listFeatCountOfCurInp = new  ArrayList<Integer>();
	
		DepTreeNode headOfEnt1 = objCurSen.depTree.getHeadWordFromWordBoundaries(e1.getAllWordIndexes(), true, objCurSen), 
				headOfEnt2 = objCurSen.depTree.getHeadWordFromWordBoundaries(e2.getAllWordIndexes(), true, objCurSen);
		
		DepTreeNode dn = null;
		
	//	System.out.println(e1.id + "  " + e2.id);
				
		// All nodes in the shortest path connecting the target pairs must be retained
		// All nodes satisfying the 3 rules of MEDT kernel must be retained
		dn = objCurSen.depTree.clone().findMinimalSubTreeWithEntities(false, headOfEnt1, medtType, headOfEnt2 );
	
		/*
		 *  If there is no minimal subtree/path between target entities then we do not consider to generate
		 *  instance for training/testing.
		 */
		if ( dn == null ) {
			FileUtility.writeInFile( GenericFeatVect.vectOutFile, 
					e1.id + " " + e1.name + "\t" + e2.id + " " + e2.name + "\n\n", true);
			
			FileUtility.writeInFile( GenericFeatVect.vectOutFile, 
					objCurSen.text + "\n\n", true);
			
			return false;
		}
		
		GenericFeatVect.listOfAllInstances.add( e1.id + " " + e1.name
				+ "\t" + e2.id + " " + e2.name + "\n\n" + objCurSen.text + "\n\n");

				
		// Construct feature set using e-walks and v-walks
		if ( dn != null && useWalkFeatures ) {
			
			createNgramFeatures( objCurSen.depTree, 1, e1, listFeatIndsOfCurInp, listFeatCountOfCurInp);
			createNgramFeatures( objCurSen.depTree, 1, e2, listFeatIndsOfCurInp, listFeatCountOfCurInp);
	
			//System.out.println(objCurSen.senID + "  " + dn.wordIndex);
			objCurSen.depTree.replaceEntitiesWithDummies(dn, e1.boundaries, e2.boundaries, new ArrayList<Integer>());
			createFeaturesFromInputGraph( objCurSen.depTree, dn, "", listFeatIndsOfCurInp, listFeatCountOfCurInp, 
					new ArrayList<Integer>(), e1.boundaries, e2.boundaries);
		}
		
		if ( useRegExPatterns ) {
			matchPPIpatternOnSentence(objCurSen.text, e1.name, e2.name, listFeatIndsOfCurInp, listFeatCountOfCurInp);
			matchPPIpatternOnSentence(objCurSen.text, e2.name, e1.name, listFeatIndsOfCurInp, listFeatCountOfCurInp);
		}
		
				
		ArrayList<String> listOfDepRelsInReducedGraph = new ArrayList<String>();
	
		ArrayList<ArrayList<Integer>> listNodesAndLCP  = new PatternsDepRelFromGraph().extractDepRelsInReducedGraph( 
				objRel, objCurSen, listOfDepRelsInReducedGraph, discardDepRelUsingProbabilityInReducedGraph, false);
		
		
		if ( useDepPatterns )
			extractDepPatternFeatures(listOfDepRelsInReducedGraph, listFeatIndsOfCurInp, listFeatCountOfCurInp);
		
			
		
		 //  NOTE: listNodesAndLCP has two elements - (0) all the nodes in Reduced graph, and (1) the least common parents (LCPs) in Reduced graph.
		 
		if ( listNodesAndLCP == null )
			listNodesAndLCP = new ArrayList<ArrayList<Integer>>();
		
		if ( useTriggers && listNodesAndLCP.size() > 0 ) {
			if ( triggersFromWholeRGinsteadOfLCP )
				for ( int i=0; i<listNodesAndLCP.get(0).size(); i++ )
					addTriggerWordFeatures(objCurSen.depGraph.allNodesByWordIndex[listNodesAndLCP.get(0).get(i)].word, listFeatIndsOfCurInp, listFeatCountOfCurInp);
			else
				for ( int i=0; i<listNodesAndLCP.get(1).size(); i++ )
					addTriggerWordFeatures(objCurSen.depGraph.allNodesByWordIndex[listNodesAndLCP.get(1).get(i)].word, listFeatIndsOfCurInp, listFeatCountOfCurInp);
		}
		//*
		if ( useNegativeCues && listNodesAndLCP.size() > 0 ) {
				
			for ( int i=0; i<listNodesAndLCP.get(0).size(); i++ ) {
				//System.out.println(objCurSen.senID + "  " + dn.wordIndex);
				//addNegativeWordFeatures( graph.allNodesByWordIndex[listNodesAndLCP.get(0).get(i)].word, listFeatIndsOfCurInp, listFeatCountOfCurInp);
			
				if ( Triggers.listOfNegativeWords.contains(objCurSen.depGraph.allNodesByWordIndex[listNodesAndLCP.get(0).get(i)].lemma) ) {

					// add a feature indicating that there is a negative word in the reduced graph
					String[] feature = new String[] {
							"HasNegWord@$" + objCurSen.depGraph.allNodesByWordIndex[listNodesAndLCP.get(0).get(i)].lemma,
					};

					GenericFeatVect.addNewFeatureInList(feature, 1, listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
					
					if ( objCurSen.depGraph.allNodesByWordIndex[listNodesAndLCP.get(0).get(i)+1].lemma.equalsIgnoreCase("be") 
							|| objCurSen.depGraph.allNodesByWordIndex[listNodesAndLCP.get(0).get(i)+1].pos.equalsIgnoreCase("IN") ) {
						feature = new String[] {
							"WordNextToNegCue@$" + objCurSen.depGraph.allNodesByWordIndex[listNodesAndLCP.get(0).get(i)+2].lemma,
						};
					}
					else {
						feature = new String[] {
							"WordNextToNegCue@$" + objCurSen.depGraph.allNodesByWordIndex[listNodesAndLCP.get(0).get(i)+1].lemma,
						};
					}
					
					GenericFeatVect.addNewFeatureInList(feature, 1, listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
					
					//*
					// extract negation scope features
					if ( objCurSen.depGraph.allNodesByWordIndex[listNodesAndLCP.get(0).get(i)].lemma.matches("(no|not)") )
							extractNegationScopeFeatures(objCurSen, listFeatIndsOfCurInp, listFeatCountOfCurInp,
								e1, e2, listNodesAndLCP.get(0).get(i), listNodesAndLCP.get(0));
					//*/
								
				}
			}
		}
		//*/
		
		// add Zhou et al. 2005 features
		new ExtAceFeatVect().getZhouEtAl2005FeatVal(objCurSen, e1, e2, listFeatIndsOfCurInp, listFeatCountOfCurInp);
		
		extractNonTargetEntityFeatures( objCurSen, listFeatIndsOfCurInp, listFeatCountOfCurInp, e1, e2);
		
		GenericFeatVect.sortFeatValByIndx(listFeatIndsOfCurInp, listFeatCountOfCurInp);
		
		GenericFeatVect.listOfAllInstancesWithFeat.add(listFeatIndsOfCurInp);
		GenericFeatVect.listOfAllInstancesWithFeatCount.add(listFeatCountOfCurInp);

		return true;
	}
	
			
	/**
	 * 
	 * @param listOfDepRelsInReducedGraph
	 * @param listFeatIndsOfCurInp
	 * @param listFeatCountOfCurInp
	 */
	private void extractDepPatternFeatures ( ArrayList<String> listOfDepRelsInReducedGraph, 
			ArrayList<Integer> listFeatIndsOfCurInp, ArrayList<Integer> listFeatCountOfCurInp  ) {
				
		ArrayList<Integer> listOfMatchedPatternIndexes = new ArrayList<Integer>();
		
		for ( int i=0; i<PatternsDepRelFromGraph.listOfAllPatterns.size(); i++ ) {
			
			if ( DataStrucUtility.hasListOneAllElementsOfListTwo(listOfDepRelsInReducedGraph, 
					PatternsDepRelFromGraph.listOfAllPatterns.get(i) ) )
				listOfMatchedPatternIndexes.add(i);
		}
		
		for ( int i=0; i<listOfMatchedPatternIndexes.size(); i++ ) {
			// add a feature indicating that what dep pattern it is
			String[] feature = new String[] {
					"DepPattern-" + listOfMatchedPatternIndexes.get(i) + "@",
			};

			GenericFeatVect.addNewFeatureInList(feature, 1, listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		}
	}

	
	/**
	 * 
	 * @param dt
	 * @param eBoundaries
	 * @param listFeatIndsOfCurInp
	 * @param listFeatCountOfCurInp
	 * @throws IOException
	 */
	private void createNgramFeatures ( DependencyTree dt, int weight,
			Entity ent, ArrayList<Integer> listFeatIndsOfCurInp, ArrayList<Integer> listFeatCountOfCurInp ) throws IOException {

		String[] feature = new String[0];
		
		// collecting the unigram within a window of {-x, +x}

		int x = ent.getStartWordIndex();
		for ( int i=x-1; i>=x-2 && i>=0; i-- ){
			feature =  new String[] {
					dt.allNodesByWordIndex[i].lemma + "$" + (i-x)};
				
			GenericFeatVect.addNewFeatureInList(feature, 1, listFeatIndsOfCurInp, listFeatCountOfCurInp, weight);
		}

		x = ent.getEndWordIndex();			
		for ( int i=x+1; i<=x+2 && i<dt.allNodesByWordIndex.length; i++ ){
			feature =  new String[] {
					dt.allNodesByWordIndex[i].lemma + "$" + (x-i)};
				
			GenericFeatVect.addNewFeatureInList(feature, 1, listFeatIndsOfCurInp, listFeatCountOfCurInp, weight);
		}
	}
	
	/**
	 * 
	 * @param dn
	 * @param prevRelType
	 * @param listFeatIndsOfCurInp
	 * @param listFeatCountOfCurInp
	 * @throws IOException 
	 */
	private void createFeaturesFromInputGraph ( DependencyTree dt, DepTreeNode dn, String prevRelType, 
			ArrayList<Integer> listFeatIndsOfCurInp, ArrayList<Integer> listFeatCountOfCurInp, 
			ArrayList<Integer> listOfNodeTraversed,
			int[] e1Boundaries, int[] e2Boundaries ) throws IOException {
		// TODO: what will happen if there is multiple root?
		
		int weight = 1;
	//	cc++;
		listOfNodeTraversed.add(dn.wordIndex);
		
		//if ( isEntityButNotOneOfTheTargetEntities(dn, e1Boundaries, e2Boundaries) )
			//dn.lemma = "ENTITYother";
		
		//System.out.println(cc);
		
		String[] feature = new String[0];
		
		//*/
		// BFS traversal
		
		for ( int i=0; i< dn.getChildrenWordIndexes().size(); i++ ){
		//*	
			/*
			// v-walk
			feature =  new String[] {
					dn.lemma + "@" + dn.relNamesWithChildren.get(i) + "@" + dn.children.get(i).lemma,
					dn.children.get(i).lemma + "@" + dn.relNamesWithChildren.get(i) + "@" + dn.lemma
			};
			//addNewFeatureInList(feature, listFeatIndsOfCurInp, listFeatCountOfCurInp);
			*/
			
		//	FileUtility.writeInFile( vectOutFile, feature[0] + "\n", true);
			
			feature =  new String[] {
					dn.pos + "@" + dn.getRelNamesWithChildren().get(i) + "@" + dn.getChildren().get(i).pos,
					dn.getChildren().get(i).pos + "@" + dn.getRelNamesWithChildren().get(i) + "@" + dn.pos
				};				
			GenericFeatVect.addNewFeatureInList(feature, 1, listFeatIndsOfCurInp, listFeatCountOfCurInp, weight);
		//*/
			// e-walk
			if ( !prevRelType.isEmpty() ) {
				feature = new String[] {
					prevRelType + "#" + dn.pos + "#" + dn.getRelNamesWithChildren().get(i),
					dn.getRelNamesWithChildren().get(i) + "#" + dn.pos + "#" + prevRelType
				};
				GenericFeatVect.addNewFeatureInList(feature, 1, listFeatIndsOfCurInp, listFeatCountOfCurInp, weight);
				
				feature = new String[] {
						prevRelType + "#" + dn.lemma + "#" + dn.getRelNamesWithChildren().get(i),
						dn.getRelNamesWithChildren().get(i) + "#" + dn.lemma + "#" + prevRelType
				};
				GenericFeatVect.addNewFeatureInList(feature, 1, listFeatIndsOfCurInp, listFeatCountOfCurInp, weight);
			}
			

			for ( int k=i+1; k< dn.getChildrenWordIndexes().size(); k++ ){
				
				// add e-walk for the siblings WITHOUT taking lexical order in consideration
				feature = new String[] {
						dn.getRelNamesWithChildren().get(i) + "#" + dn.pos + "#" + dn.getRelNamesWithChildren().get(k),
						dn.getRelNamesWithChildren().get(k) + "#" + dn.pos + "#" + dn.getRelNamesWithChildren().get(i)
					};
				GenericFeatVect.addNewFeatureInList(feature, 1, listFeatIndsOfCurInp, listFeatCountOfCurInp, weight);
					
				feature = new String[] {
							dn.getRelNamesWithChildren().get(i) + "#" + dn.lemma + "#" + dn.getRelNamesWithChildren().get(k),
							dn.getRelNamesWithChildren().get(k) + "#" + dn.lemma + "#" + dn.getRelNamesWithChildren().get(i),
					};
				GenericFeatVect.addNewFeatureInList(feature, 1, listFeatIndsOfCurInp, listFeatCountOfCurInp, weight);
				/*
				// 3-gram of siblings and parent
				feature = new String[] {
						dn.children.get(i).lemma + "#" + dn.lemma + "#" + dn.children.get(k).lemma,
						dn.children.get(k).lemma + "#" + dn.lemma + "#" + dn.children.get(i).lemma
					};
				addNewFeatureInList(feature, listFeatIndsOfCurInp, listFeatCountOfCurInp);
				/*
				feature = new String[] {
						dn.children.get(i).pos + "#" + dn.pos + "#" + dn.children.get(k).pos,
						dn.children.get(k).pos + "#" + dn.pos + "#" + dn.children.get(i).pos
					};
				addNewFeatureInList(feature, listFeatIndsOfCurInp, listFeatCountOfCurInp);
				*/
			}
		}
		
		// for grand children
		for ( int i=0; i< dn.getChildrenWordIndexes().size(); i++ ){
			if ( !listOfNodeTraversed.contains(dn.getChildrenWordIndexes().get(i)) )
				createFeaturesFromInputGraph( dt, dn.getChildren().get(i), dn.getRelNamesWithChildren().get(i), 
						listFeatIndsOfCurInp, listFeatCountOfCurInp, listOfNodeTraversed, 
						e1Boundaries, e2Boundaries);
		}
		
		// for parents
		for ( int i=0; dn.getParents() != null && i< dn.getParents().size(); i++ ) {
			/*
			for ( int c=0; c< dn.childrenWordIndex.size(); c++ ){
				// 3-gram
				feature = new String[] {
						dn.getParents().get(i).lemma + "#" + dn.lemma + "#" + dn.children.get(c).lemma,
						//dn.children.get(c).lemma + "#" + dn.lemma + "#" + dn.getParents().get(i).lemma
					};
				addNewFeatureInList(feature, listFeatIndsOfCurInp, listFeatCountOfCurInp);
			}
			*/
			if ( !listOfNodeTraversed.contains(dn.getParents().get(i).wordIndex) ) {				
				createFeaturesFromInputGraph( dt, dn.getParents().get(i), "", 
						listFeatIndsOfCurInp, listFeatCountOfCurInp, listOfNodeTraversed,
						e1Boundaries, e2Boundaries);
			}
		}
	}
	

	/**
	 * 
	 * @param word
	 * @param listFeatIndsOfCurInp
	 * @param listFeatCountOfCurInp
	 */
	private void addTriggerWordFeatures ( String word, 
			ArrayList<Integer> listFeatIndsOfCurInp, ArrayList<Integer> listFeatCountOfCurInp ) {
		
		int tgCharIndex = Triggers.listOf1stChar.indexOf(word.charAt(0));
		
		if ( tgCharIndex >= 0 ) {
			int tgWordIndex = Triggers.listOfTriggers.get(tgCharIndex).indexOf(word);
		
			if ( tgWordIndex >= 0 ) {
				// add a feature indicating that what trigger word it is
				String[] feature = new String[] {
						"Trigger-" + Triggers.listOfTriggerLemmas.get(tgCharIndex).get(tgWordIndex) + "@",
				};

				GenericFeatVect.addNewFeatureInList(feature, 1, listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
//*				
				// add a feature indicating that there is a trigger word
				feature = new String[] {
						"HasTriggerWord@",
				};

				GenericFeatVect.addNewFeatureInList(feature, 1, listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
//				*/
			}
		}
	}
	
	
	
	/**
	 * 
	 * @param sen
	 * @param ent1
	 * @param ent2
	 * @param listFeatIndsOfCurInp
	 * @param listFeatCountOfCurInp
	 */
	public void matchPPIpatternOnSentence ( String sen, String ent1, String ent2,
			ArrayList<Integer> listFeatIndsOfCurInp, ArrayList<Integer> listFeatCountOfCurInp ) {
		
		String ENT_name_1 = "", ENT_name = "";
		
		ENT_name = ent2;
		ENT_name_1 = ent1;
		
		String[] patterns = new String[]{
				ENT_name_1 + "\\s*.*\\s*" + ENT_name + " complex",
				ENT_name_1 + "\\s*.*\\s*activate[sd]?\\s*.*\\s*" + ENT_name,
				ENT_name_1 + "\\s*.*\\s*stimulate[sd]?\\s*.*\\s*" + ENT_name,
				ENT_name_1 + "\\s*.*\\s*bind(s|ed)?\\s*.*\\s*" + ENT_name,
				"association of\\s*.*\\s*" + ENT_name_1 + "\\s*.*\\s*with\\s*.*\\s*" + ENT_name,
				ENT_name_1 + "\\s*.*\\s*interact(s|ed)? with\\s*.*\\s*" + ENT_name,
				"interaction (between|among)\\s*.*\\s*" + ENT_name + "\\s*.*\\s*and\\s*.*\\s*" + ENT_name,
				ENT_name_1 + "\\s*.*\\s*" + ENT_name + " interaction",
				ENT_name_1 + "\\s*.*\\s*" + ENT_name + " binding",
				ENT_name_1 + "\\s*.*\\s*" + ENT_name + " interact",
				ENT_name_1 + "\\s*.*\\s*associates? with\\s*.*\\s*" + ENT_name,
				"association between\\s*.*\\s*" + ENT_name_1 + "\\s*.*\\s*and\\s*.*\\s*" + ENT_name,
				ENT_name_1 + "\\s*.*\\s*and\\s*.*\\s*" + ENT_name + " \\s*.*\\s*association with each other",
				ENT_name_1 + "\\s*.*\\s*binds? to\\s*.*\\s*" + ENT_name,
				"binding of\\s*.*\\s*" + ENT_name_1 + "\\s*.*\\s*to\\s*.*\\s*" + ENT_name,
				ENT_name_1 + " and " + ENT_name + " bind",
				"binding between " + ENT_name_1 + " and " + ENT_name,
				"complex " + ENT_name_1 + " and " + ENT_name,
				ENT_name_1 + " complex with " + ENT_name,
				ENT_name_1 + " complex " + ENT_name,
				ENT_name_1 + "\\s*.*\\s*not (interact|associate|bind|complex)\\s*.*\\s*" + ENT_name,
				ENT_name_1 + "\\s*.*\\s*but not " + ENT_name,
		};
		
		
		for ( int i=0; i<patterns.length; i++ ) {
			if ( sen.matches(".*" + patterns[i] + ".*") ) {
				String[] feature = new String[] {
						"Pattern-" + i + "@",
				};
				GenericFeatVect.addNewFeatureInList(feature, 1, listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			//	System.out.println(sen + "\n" + patterns[i] + "\n");
			} 
		}
	}
	
	/**
	 * 
	 * @param curSen
	 * @param swi
	 * @return
	 */
	public static int getNonConjGovernorIndex ( Sentence curSen, int swi ) {
		
		int wiOfImmediateGovernor = -1;
		
		for ( int p=0; curSen.depTree.allNodesByWordIndex[swi].getParentsWordIndexes() != null 
				&& p<curSen.depTree.allNodesByWordIndex[swi].getParentsWordIndexes().size(); p++ ) {
			if ( !curSen.depTree.allNodesByWordIndex[swi].getRelNamesWithParents().get(p).toLowerCase().contains("conj") ) {
				wiOfImmediateGovernor = 
					curSen.depTree.allNodesByWordIndex[swi].getParentsWordIndexes().get(p);
				break;
			}
		}
		
		return wiOfImmediateGovernor;
	}
	
	
	/**
	 * 
	 * @param wiOfImmediateGovernor
	 * @param curSen
	 * @return
	 */
	public static int getNearestVerbGovernor ( int wiOfImmediateGovernor, Sentence curSen ) {
		int wiOfNearestVerbGovernor = wiOfImmediateGovernor;
		while ( wiOfNearestVerbGovernor > -1 && !curSen.arrWordAndPosByParser[wiOfNearestVerbGovernor][1].toLowerCase().startsWith("v") 
				&& curSen.depTree.allNodesByWordIndex[wiOfNearestVerbGovernor].getParentsWordIndexes().size() > 0 ) {
			
			int p=0;
			for ( ; p<curSen.depTree.allNodesByWordIndex[wiOfNearestVerbGovernor].getParentsWordIndexes().size(); p++ ) {
				if ( curSen.depTree.allNodesByWordIndex[wiOfNearestVerbGovernor].getRelNamesWithParents().get(p).toLowerCase().contains("conj") )
					break;
				else 
					if ( curSen.arrWordAndPosByParser[curSen.depTree.allNodesByWordIndex[wiOfNearestVerbGovernor]
							.getParentsWordIndexes().get(p)][1].toLowerCase().startsWith("v") ) {
					wiOfNearestVerbGovernor = curSen.depTree.allNodesByWordIndex[wiOfNearestVerbGovernor].getParentsWordIndexes().get(p);
					break;
				}
			}
			
			if ( p<curSen.depTree.allNodesByWordIndex[wiOfNearestVerbGovernor].getParentsWordIndexes().size()
					&& curSen.depTree.allNodesByWordIndex[wiOfNearestVerbGovernor].getRelNamesWithParents().get(p).toLowerCase().contains("conj") )
				break;
			
			if ( !curSen.arrWordAndPosByParser[wiOfNearestVerbGovernor][1].toLowerCase().startsWith("v") )
				wiOfNearestVerbGovernor = getNonConjGovernorIndex(curSen, wiOfNearestVerbGovernor);
		}
		
		if ( !curSen.arrWordAndPosByParser[wiOfImmediateGovernor][1].toLowerCase().startsWith("v") 
				&& wiOfNearestVerbGovernor == wiOfImmediateGovernor )
			wiOfNearestVerbGovernor = -1;
		
		return wiOfNearestVerbGovernor;
	}
	
	
	/**
	 * 
	 * @param curSen
	 * @param listFeatIndsOfCurInp
	 * @param listFeatCountOfCurInp
	 * @param e1
	 * @param e2
	 * @param negWI
	 * @param listOfReducedGraph
	 */
	private void extractNegationScopeFeatures( Sentence curSen, ArrayList<Integer> listFeatIndsOfCurInp, 
			ArrayList<Integer> listFeatCountOfCurInp, Entity e1, Entity e2, int negWI, ArrayList<Integer> listOfReducedGraph ) {
			
		int wiOfImmediateGovernor = getNonConjGovernorIndex(curSen, negWI);
		
		if ( !listOfReducedGraph.contains(wiOfImmediateGovernor) || wiOfImmediateGovernor < 0 )
			return;

		int wiOfNearestVerbGovernor = getNearestVerbGovernor(wiOfImmediateGovernor, curSen);
		
	//	int dist = 0;
			
		//*
		if ( !listOfReducedGraph.contains(wiOfNearestVerbGovernor) 
				//||
				//dist > 3
				)
			wiOfNearestVerbGovernor = -1;
		/*		
		if ( wiOfNearestVerbGovernor > -1 &&
				curSen.depTree.allNodesByWordIndex[wiOfNearestVerbGovernor].getParentsWordIndexes().isEmpty()
			) 
			GenericFeatVect.addNewFeatureInList( new String[]{"verbGovernorIsRoot"}, 1, 
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		*/
		
		ArrayList<Integer> listOfWIofEntities = new ArrayList<Integer>();
		listOfWIofEntities.addAll( e1.getAllWordIndexes());
		listOfWIofEntities.addAll( e2.getAllWordIndexes());
			
		/*
		// detect whether drugs are dependent of the verb governor
		if ( wiOfNearestVerbGovernor > -1  
				&& curSen.depTree.allNodesByWordIndex[wiOfNearestVerbGovernor].governAllWIsInList(listOfWIofEntities) )
			GenericFeatVect.addNewFeatureInList( new String[]{"bothEntDependOnVerbGovernor"}, 1, 
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		*/
		if ( wiOfImmediateGovernor > -1  
				&& curSen.depTree.allNodesByWordIndex[wiOfImmediateGovernor].governAllWIsInList(listOfWIofEntities) )
			GenericFeatVect.addNewFeatureInList( new String[]{"bothEntDependOnImmediateGovernor"}, 1, 
						listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		if ( wiOfImmediateGovernor == wiOfNearestVerbGovernor )
			GenericFeatVect.addNewFeatureInList( new String[]{"immediateGovernorIsVerbGovernor"}, 1, 
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		/*
		GenericFeatVect.addNewFeatureInList( new String[]{"immediateGovernor="
					+ curSen.arrLemmasByParser[wiOfImmediateGovernor].toLowerCase()}, 1, 
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		//*/
		if ( wiOfNearestVerbGovernor > -1 )
			GenericFeatVect.addNewFeatureInList( new String[]{"nearestVerbGovernor="
					+ curSen.arrLemmasByParser[wiOfNearestVerbGovernor].toLowerCase()}, 1, 
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			//		*/
	}
	

	/**
	 * 
	 * @param curSen
	 * @param listFeatIndsOfCurInp
	 * @param listFeatCountOfCurInp
	 * @param e1
	 * @param e2
	 */
	private void extractNonTargetEntityFeatures( Sentence curSen, ArrayList<Integer> listFeatIndsOfCurInp, 
			ArrayList<Integer> listFeatCountOfCurInp, Entity e1, Entity e2 ) {
		
		for ( int e=0; e<curSen.listOfEntities.size(); e++ ) {
			
			Entity entOther = curSen.listOfEntities.get(e);
						
			if ( entOther.getNEcategory().equalsIgnoreCase("DISO") ) {
				
				GenericFeatVect.addNewFeatureInList( new String[]{"DISOinsideSentence"}, 1, 
						listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
			//	System.out.println(entOther.id);
				DepTreeNode headOfEnt = curSen.depTree.getHeadWordFromWordBoundaries( entOther.getAllWordIndexes(), true, curSen);
				
				int hwiOfEntOther = headOfEnt.wordIndex;
				
				int wiOfImmediateGovernor = getNonConjGovernorIndex(curSen, hwiOfEntOther);				
				
				if ( wiOfImmediateGovernor < 0 )
					continue;
				
				int wiOfNearestVerbGovernor = getNearestVerbGovernor(wiOfImmediateGovernor, curSen);
					
				ArrayList<Integer> listOfWIofEntities = new ArrayList<Integer>();
				listOfWIofEntities.addAll( e1.getAllWordIndexes());
				listOfWIofEntities.addAll( e2.getAllWordIndexes());
			
				// detect whether drugs are dependent of the verb governor
				if ( wiOfNearestVerbGovernor > -1  
						&& curSen.depTree.allNodesByWordIndex[wiOfNearestVerbGovernor].governAllWIsInList(listOfWIofEntities) ) {	
					/*
					GenericFeatVect.addNewFeatureInList( new String[]{"bothEntDependOnVerbGovernorOfTheDISO"}, 1, 
							listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
					//*/	
					GenericFeatVect.addNewFeatureInList( new String[]{"nearestVerbGovernorOfTheDISO="
						+ curSen.arrLemmasByParser[wiOfNearestVerbGovernor].toLowerCase()}, 1, 
						listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
						//*/
				}
				
				if ( wiOfImmediateGovernor > -1  
						&& curSen.depTree.allNodesByWordIndex[wiOfImmediateGovernor].governAllWIsInList(listOfWIofEntities) ) {
					/*
					GenericFeatVect.addNewFeatureInList( new String[]{"bothEntDependOnImmediateGovernorOfTheDISO"}, 1, 
								listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
					//*/ 
					GenericFeatVect.addNewFeatureInList( new String[]{"immediateGovernorOfTheDISO="
							+ curSen.arrLemmasByParser[wiOfImmediateGovernor].toLowerCase()}, 1, 
							listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
						//	*/
				}
				//*
				if ( wiOfImmediateGovernor == wiOfNearestVerbGovernor )
					GenericFeatVect.addNewFeatureInList( new String[]{"immediateGovernorIsVerbGovernorOfTheDISO"}, 1, 
							listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			//	*/
			}
		}		
	}

}
