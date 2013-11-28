package Structures;


import java.util.ArrayList;

import Kernels.TKOutputGenerator;
import Utility.*;


public class DependencyTree {

	public ArrayList<Integer> rootIndexes = new ArrayList<Integer>();
	public ArrayList<Boolean> hasRootChildren = new ArrayList<Boolean>();
	public DepTreeNode[] allNodesByWordIndex = new DepTreeNode[0];
	
	public DependencyTree() {
		
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public DependencyTree clone()
    {
		DependencyTree newNode = new DependencyTree();
		
		newNode.rootIndexes = (ArrayList<Integer>) this.rootIndexes.clone();
		newNode.hasRootChildren = (ArrayList<Boolean>) this.hasRootChildren.clone();
		newNode.allNodesByWordIndex = (DepTreeNode[]) this.allNodesByWordIndex.clone();
		
		return newNode;
    }
	
	/**
	 * 
	 * @param senID
	 * @param wordAndPos
	 * @param origSen
	 */
	public DependencyTree ( String[][] arrWordWithPosByParser, String[] arrAllDepRelations, String[][] wordsAndNEs ){
		
		/**
		 * NOTE:
		 * Cotransfection of hGITRL and hGITR in embryonic kidney 293 cells activated the anti-apoptotic transcription factor NF-kappaB, via a pathway that appeared to involve TNFR-associated factor 2 (TRAF2) [7] and NF-kappaB-inducing kinase (NIK) [8].
		 * 
		 * In the above sentence, there is a vice-versa dependency -
		 * nsubj(appeared-22, pathway-20)
		 * rcmod(pathway-20, appeared-22) 
		 */
	
		String[][] arrAllSeparatedRelAndArgs = new SyntacticParser().separateRelationAndArgs(arrAllDepRelations);
		boolean isTokensGiven = true;
		int totWord = 0;
		
		/*
		ArrayList<String> listOfArgPairs = new ArrayList<String>();
		
		for ( int i=0; i<arrAllSeparatedRelAndArgs.length; i++ ) {
			int x = -1;
			int argIndxOne = Integer.valueOf(arrAllSeparatedRelAndArgs[i][2]);
			int argIndxTwo = Integer.valueOf(arrAllSeparatedRelAndArgs[i][4]);
			
			// TODO:  in case of dual dependency, keep the one for which the 1st argument already have a parent in a different dependency to form a complete tree 
			/*
			// in case of dual dependency, we only keep the one where the word index of the 1st argument is larger  
			if ( (x=listOfArgPairs.indexOf(argIndxOne + "-" + argIndxTwo)) > -1 || (x=listOfArgPairs.indexOf(argIndxTwo + "-" + argIndxOne)) > -1 ) {
				if ( argIndxTwo > argIndxOne ) {
					arrAllSeparatedRelAndArgs[i][2] = "-1";
					arrAllSeparatedRelAndArgs[i][4] = "-1";
				}
				else {
					arrAllSeparatedRelAndArgs[x][2] = "-1";
					arrAllSeparatedRelAndArgs[x][4] = "-1";
				}
				
				listOfArgPairs.add(argIndxOne + "-" + argIndxTwo);
				
				continue;	
			}
			* /
			if ( argIndxOne > totWord )
				totWord = argIndxOne;
			
			if ( argIndxTwo > totWord )
				totWord = argIndxTwo;
			
			listOfArgPairs.add(argIndxOne + "-" + argIndxTwo);
		}*/
		
		
		// in case of dual dependency, keep the one for which the 1st argument already have a parent in a different dependency to form a complete tree
		for ( int i=0; i<arrAllSeparatedRelAndArgs.length; i++ ) {
			for ( int k=i+1; k<arrAllSeparatedRelAndArgs.length; k++ ) {
				// if dual dependency exists
				if ( arrAllSeparatedRelAndArgs[i][2].equals(arrAllSeparatedRelAndArgs[k][4])
						&& arrAllSeparatedRelAndArgs[i][4].equals(arrAllSeparatedRelAndArgs[k][2]) ) {
					
					for ( int x=0; x<arrAllSeparatedRelAndArgs.length; x++ ) {
						if ( x!=i && x!=k ) {
							if ( arrAllSeparatedRelAndArgs[x][4].equals(arrAllSeparatedRelAndArgs[k][2]) ) {
								arrAllSeparatedRelAndArgs[i][2] = "-1";
								arrAllSeparatedRelAndArgs[i][4] = "-1";
								break;
							}
							else if ( arrAllSeparatedRelAndArgs[x][4].equals(arrAllSeparatedRelAndArgs[i][2]) ) {
								arrAllSeparatedRelAndArgs[k][2] = "-1";
								arrAllSeparatedRelAndArgs[k][4] = "-1";
								break;
							}
								
						}
					}
				}
			}
			
			int argIndxOne = Integer.valueOf(arrAllSeparatedRelAndArgs[i][2]);
			int argIndxTwo = Integer.valueOf(arrAllSeparatedRelAndArgs[i][4]);
			
			if ( argIndxOne > totWord )
				totWord = argIndxOne;
			
			if ( argIndxTwo > totWord )
				totWord = argIndxTwo;
		}
		
		if ( arrWordWithPosByParser == null || arrWordWithPosByParser.length == 0 ) { 
			isTokensGiven = false;	
			allNodesByWordIndex = new DepTreeNode[totWord];
		}
		else 	
			allNodesByWordIndex = new DepTreeNode[arrWordWithPosByParser.length];
		
		int sCharIndex = 0;
		
		for ( int i=0; i<arrWordWithPosByParser.length; i++ ){
			
			allNodesByWordIndex[i] = new DepTreeNode();
			
			if ( isTokensGiven ) {
				allNodesByWordIndex[i].word = arrWordWithPosByParser[i][0];			
				allNodesByWordIndex[i].pos = arrWordWithPosByParser[i][1];	
			}
			
			allNodesByWordIndex[i].startCharIndex = sCharIndex;
			
			sCharIndex = sCharIndex + allNodesByWordIndex[i].word.length();
			allNodesByWordIndex[i].endCharIndex = sCharIndex  - 1;
			
			for ( int j=0; j<Common.arrPosToGeneralPos.length; j++)
				if ( allNodesByWordIndex[i].pos != null
						&& !allNodesByWordIndex[i].pos.equals("")
						&& Common.arrPosToGeneralPos[j][1].equalsIgnoreCase(allNodesByWordIndex[i].pos) )
					allNodesByWordIndex[i].posGeneral = Common.arrPosToGeneralPos[j][0]; 
				
			allNodesByWordIndex[i].wordIndex = i;	
			allNodesByWordIndex[i].lemma = 
				SyntacticParser.getLemma(allNodesByWordIndex[i].word, allNodesByWordIndex[i].pos);
			
			if ( !allNodesByWordIndex[i].lemma.equals("-") )
				allNodesByWordIndex[i].lemma = allNodesByWordIndex[i].lemma.replaceAll("-", "");
		}
		
		populateTree(arrAllSeparatedRelAndArgs, isTokensGiven);
		assignNEcategory(wordsAndNEs);
	}
	
	
	/**
	 * 
	 * @param wordsAndNE
	 */
	private void assignNEcategory( String[][] wordsAndNE ){
		for ( int i=0; i<wordsAndNE.length; i++ )
			for ( int k=0; k<allNodesByWordIndex.length; k++ )
				if ( allNodesByWordIndex[k].word.equals(wordsAndNE[i][0]) ){
					allNodesByWordIndex[k].setNEcategory(wordsAndNE[i][1]);
					break;
				}
	}
	
	
	/**
	 * Thomas et al. 2011 BioNLp
	 * 
	 * @param depType
	 * @return
	 */
	public String generalizeDepType ( String depType ) {
		
		if ( depType.matches(".*subj.*") )
			return "subj";
		else if ( depType.matches(".*obj.*") )
			return "obj";
		else if ( depType.matches("(.*prep.*|agent)") )
			return "prep";
		else if ( depType.matches("(nn|apos)") )
			return "nn";
		
		return depType;
	}
	
	/**
	 * 
	 * @param separatedRelAndArgs
	 * @param isTokensGiven
	 * @param isConj
	 */
	private void addChildrenAndParents ( String[] separatedRelAndArgs, boolean isTokensGiven, boolean isConj ) {
		
		int argOneIndx = Integer.valueOf(separatedRelAndArgs[2]) - 1,
			argTwoIndx = Integer.valueOf(separatedRelAndArgs[4]) - 1;
	
		// any dependency relation of a word with root is discarded by the following checking
		if ( argOneIndx < 0 || argTwoIndx < 0 || argOneIndx == argTwoIndx )
			return;
		
		if ( isConj ) {
			/**
			 *  NOTE: we assume arg2 has a conj_and or conj_or with arg1, then
			 *  the parents of arg1 are also parents of arg2  
			 */
			for ( int p1=0; p1<allNodesByWordIndex[argOneIndx].getParentsWordIndexes().size(); p1++ ) {
				int wiOfParentofArg1 = allNodesByWordIndex[argOneIndx].getParentsWordIndexes().get(p1); 
			
				if ( wiOfParentofArg1 != argTwoIndx && !allNodesByWordIndex[argTwoIndx].getParentsWordIndexes().contains(wiOfParentofArg1) ) {
				
					allNodesByWordIndex[argTwoIndx].addParent(allNodesByWordIndex[wiOfParentofArg1],
							allNodesByWordIndex[argOneIndx].getRelNamesWithParents().get(p1));
					
					allNodesByWordIndex[wiOfParentofArg1].addChild(allNodesByWordIndex[argTwoIndx],
							allNodesByWordIndex[argOneIndx].getRelNamesWithParents().get(p1));
				}
			}
			
			/**
			 * Additionally, If the words (i.e. arg1 and arg2) are consecutive verbs, then the children of arg1 would be also the 
			 * children of arg2, and vice versa.
			 */
			if ( allNodesByWordIndex[argOneIndx].pos.toLowerCase().matches("v.*") 
					&& allNodesByWordIndex[argTwoIndx].pos.toLowerCase().matches("v.*")
					&& argTwoIndx - argOneIndx == 2 ) {
				
				for ( int ch=0; ch<allNodesByWordIndex[argOneIndx].getChildrenWordIndexes().size(); ch++ ) {
					int wiOfChild = allNodesByWordIndex[argOneIndx].getChildrenWordIndexes().get(ch); 
					
					if ( wiOfChild != argTwoIndx && !allNodesByWordIndex[argTwoIndx].getChildrenWordIndexes().contains(wiOfChild) ) {
						
						allNodesByWordIndex[argTwoIndx].addChild(allNodesByWordIndex[wiOfChild],
								allNodesByWordIndex[argOneIndx].getRelNamesWithChildren().get(ch));
						
						allNodesByWordIndex[wiOfChild].addParent(allNodesByWordIndex[argTwoIndx],
								allNodesByWordIndex[argOneIndx].getRelNamesWithChildren().get(ch));
					}
				}
				
				for ( int ch=0; ch<allNodesByWordIndex[argTwoIndx].getChildrenWordIndexes().size(); ch++ ) {
					int wiOfChild = allNodesByWordIndex[argTwoIndx].getChildrenWordIndexes().get(ch); 
					
					if ( wiOfChild != argOneIndx && !allNodesByWordIndex[argOneIndx].getChildrenWordIndexes().contains(wiOfChild) ) {
						
						allNodesByWordIndex[argOneIndx].addChild(allNodesByWordIndex[wiOfChild],
								allNodesByWordIndex[argTwoIndx].getRelNamesWithChildren().get(ch));
						
						allNodesByWordIndex[wiOfChild].addParent(allNodesByWordIndex[argOneIndx],
								allNodesByWordIndex[argTwoIndx].getRelNamesWithChildren().get(ch));
					}
				}
			}
		}
		else {
			// NOTE: we assume arg2 is child of arg1
			allNodesByWordIndex[argTwoIndx].addParent(allNodesByWordIndex[argOneIndx],
					separatedRelAndArgs[0]);
			
			allNodesByWordIndex[argTwoIndx].wordIndex = argTwoIndx;
			
			allNodesByWordIndex[argOneIndx].addChild(allNodesByWordIndex[argTwoIndx],
					separatedRelAndArgs[0]);
			
			allNodesByWordIndex[argOneIndx].wordIndex = argOneIndx;	
			
			if ( !isTokensGiven ) {
				allNodesByWordIndex[argOneIndx].word = separatedRelAndArgs[1];
				allNodesByWordIndex[argTwoIndx].word = separatedRelAndArgs[3];
			}
		}
	}
	
	/**
	 * 
	 * @param allSeparatedRelAndArgs
	 */
	private void populateTree ( String[][] allSeparatedRelAndArgs, boolean isTokensGiven ){
				
		for ( int i=0; i<allSeparatedRelAndArgs.length; i++ ) {
			if ( allSeparatedRelAndArgs[i][0].equals("dep") 
					&& allSeparatedRelAndArgs[i][2].equals("18") 
					&& allSeparatedRelAndArgs[i][4].equals("20") )
				allSeparatedRelAndArgs[i][0].trim();
			addChildrenAndParents( allSeparatedRelAndArgs[i], isTokensGiven, false);
		}
		
		for ( int i=0; i<allSeparatedRelAndArgs.length; i++ )
			// only consider the conj_and and conj_or relations
			if ( allSeparatedRelAndArgs[i][0].contains("conj") )
				addChildrenAndParents( allSeparatedRelAndArgs[i], isTokensGiven, true);

		for ( int i=0; i<allNodesByWordIndex.length; i++ )
			if ( allNodesByWordIndex[i].getParentsWordIndexes().isEmpty() && !allNodesByWordIndex[i].getChildrenWordIndexes().isEmpty() ) {
				rootIndexes.add(i);
				if ( allNodesByWordIndex[i].getChildrenWordIndexes().size() < 1 )
					hasRootChildren.add(false);
				else
					hasRootChildren.add(true);
			}
		
		/*
		for ( int i=0; i<allNodesByWordIndex.length; i++ ){
			System.out.print(allNodesByWordIndex[i].wordIndex + " [" + allNodesByWordIndex[i].startCharIndex
					+ ", " + allNodesByWordIndex[i].endCharIndex + "]"
					+ allNodesByWordIndex[i].word  + " (");
			for ( int k=0; k<allNodesByWordIndex[i].getParentsWordIndexes().size(); k++ )
				System.out.print(allNodesByWordIndex[i].getParentsWordIndexes().get(k) + " ");
				System.out.print(") -> ");
			for ( int k=0; k<allNodesByWordIndex[i].childrenWordIndex.size(); k++ ){
				System.out.print(allNodesByWordIndex[i].childrenWordIndex.get(k) + " ");
			}
			System.out.println();
		}
	 */
		
		for ( int i=0; i<rootIndexes.size(); i++ )
			allNodesByWordIndex[rootIndexes.get(i)].populateGrandChildList();
	}
	
	/**
	 * 
	 * @param listOfIndexesOfEntityWords
	 * @return
	 */
	private DepTreeNode findHeadAndSubTree ( ArrayList<Integer> listOfIndexesOfWords ){
		
		ArrayList<DepTreeNode> listOfNodes = new ArrayList<DepTreeNode>();
		
		if ( listOfIndexesOfWords.size() == 1 )
			return allNodesByWordIndex[listOfIndexesOfWords.get(0)].copyExcludingChildrenAndParents();
		
		for ( int i=0; i<listOfIndexesOfWords.size(); i++ ){
			if ( !allNodesByWordIndex[listOfIndexesOfWords.get(i)].getParentsWordIndexes().isEmpty()
					|| !allNodesByWordIndex[listOfIndexesOfWords.get(i)].getChildrenWordIndexes().isEmpty() ){
				DepTreeNode node = allNodesByWordIndex[listOfIndexesOfWords.get(i)].copyExcludingChildrenAndParents();			
				listOfNodes.add(node);
			}
		}
		
		// Hypothesis: except one of the words, all the others will have their parents inside one of themselves 
		
		/**
		 * The above hypothesis fails in cases like following -
		 * Bone/JJ morphogenetic/JJ protein-2/JJ (/NNP BMP-2/NN )/NNS induces/VBZ bone/NN formation/NN and/CC regeneration/NN in/IN adult/JJ vertebrates/NNS and/CC regulates/VBZ important/JJ developmental/JJ processes/NNS in/IN all/DT animals/NNS ./.
		 * amod()-6, Bone-1) 
		 * amod()-6, morphogenetic-2) 
		 * amod()-6, protein-2-3)
		 * nn()-6, (-4)
		 * nn()-6, BMP-2-5)
		 */
		
		if ( listOfNodes.size() == 0 )
			return null;
		else if ( listOfNodes.size() == 1 )
			return listOfNodes.get(0);
		
		DepTreeNode dn = listOfNodes.get(0);
		boolean isFound = false, ignoreConj = true;
		
		while ( !isFound ) {
			for ( int i=1; i<listOfNodes.size(); i++ ){
				if ( dn.wordIndex != listOfNodes.get(i).wordIndex ){
					DepTreeNode x = findCommonHead(dn, listOfNodes.get(i), 0, ignoreConj);
					if ( x != null ) {
						dn = x;
						isFound = true;
					}
				}
			}
			
			if ( !isFound && !ignoreConj )
				break;
			
			if ( !isFound )
				ignoreConj = false;
		}
		
		return dn;
	}
	
	
	/**
	 * Basic idea:
	 * Find the grandparents which has both the target nodes as grand children.
	 * Then, for each of those grandparents find the minimum number of connecting nodes to the target nodes. 
	 * These nodes together with the target nodes and the particular grandparent form the minimal sub-tree rooted
	 * at that grandparent.
	 * Once all such minimal sub-tree are computed, select the shortest one.
	 * 
	 * @param node1
	 * @param nodeOneTraversed
	 * @param node2
	 * @param nodeTwoTraversed
	 * @return
	 */
	
	
	private ArrayList<Integer> findShortestPathBetweenTwoNodes( int parentWI, ArrayList<Integer> nodeTraversed,
			ArrayList<Integer> shortestPath, int childWI, boolean ignoreConj ){
			
		// We don't allow a head to be selected between two words directly because of a conj relation.
		int x = -1;
		if ( (x=allNodesByWordIndex[parentWI].getChildrenWordIndexes().indexOf(childWI)) > -1 
				&& 
				( !ignoreConj || !allNodesByWordIndex[parentWI].getRelNamesWithChildren().get(x).contains("conj")) ){
			
			nodeTraversed.add(childWI);
			
			if ( nodeTraversed.size() < shortestPath.size() || shortestPath.size() == 0  )
				return nodeTraversed;
		}
		else{
			
			for ( int i=0; i<allNodesByWordIndex[parentWI].getChildrenWordIndexes().size(); i++ ){
				// ic = intermediate child
				int ic = allNodesByWordIndex[parentWI].getChildrenWordIndexes().get(i);
						
				if ( !nodeTraversed.contains(ic) 
						&& ( allNodesByWordIndex[ic].getChildrenWordIndexes().contains(childWI)
							 || allNodesByWordIndex[ic].getGrandChildrenWIs().contains(childWI) )
						&& ( !ignoreConj || !allNodesByWordIndex[parentWI].getRelNamesWithChildren().get(i).contains("conj"))	
						&& (shortestPath.size() == 0 || shortestPath.size() > nodeTraversed.size()) ) {
					
					ArrayList<Integer> temp = DataStrucUtility.listCopy(nodeTraversed);					
					temp.add(ic);
					temp = findShortestPathBetweenTwoNodes(ic, temp, shortestPath, childWI, ignoreConj);
					
					if ( temp.contains(childWI) && (temp.size() < shortestPath.size() || shortestPath.size() == 0) )
						shortestPath = temp;
				}					
			}	
		}
		
		return shortestPath;
	}

	
	/**
	 * 
	 * @param node1
	 * @param node2
	 * @return
	 */
	private DepTreeNode findCommonHead( DepTreeNode node1, DepTreeNode node2, int stageNo, boolean ignoreConj ){	
		
		// We don't allow a head to be selected between two words directly because of a conj relation.
		String depRelType = "";
		if ( allNodesByWordIndex[node2.wordIndex].getParentsWordIndexes().contains(node1.wordIndex) && 
				( stageNo > 0 || !ignoreConj ||
						!(depRelType=allNodesByWordIndex[node2.wordIndex].getRelNamesWithParents()
						.get(allNodesByWordIndex[node2.wordIndex].getParentsWordIndexes()
								.indexOf(node1.wordIndex))).contains("conj"))
			){
			node1.addChild(node2, false, depRelType);
			return node1;
		}
		else if ( allNodesByWordIndex[node1.wordIndex].getParentsWordIndexes().contains(node2.wordIndex) && 
				( stageNo > 0 || !ignoreConj || 
						!(depRelType=allNodesByWordIndex[node1.wordIndex].getRelNamesWithParents()
						.get(allNodesByWordIndex[node1.wordIndex].getParentsWordIndexes()
								.indexOf(node2.wordIndex))).contains("conj"))
				){
			node2.addChild(node1, true, depRelType);
			return node2;
		}
				
		ArrayList<Integer> shortestPathForNodeOne = new ArrayList<Integer>(),
								shortestPathForNodeTwo = new ArrayList<Integer>();
		
		int commonHeadIndx = -1;
		/*
		if ( allNodesByWordIndex[node1.wordIndex].getGrandChildrenWIs().contains(node2.wordIndex) ){
		
			shortestPathForNodeOne.add(node1.wordIndex);
			shortestPathForNodeOne = findShortestPathBetweenTwoNodes(
					node1.wordIndex, shortestPathForNodeOne, 
					new ArrayList<Integer>(), node2.wordIndex, ignoreConj);
			
			commonHeadIndx = node1.wordIndex;
		}
		else if ( allNodesByWordIndex[node2.wordIndex].getGrandChildrenWIs().contains(node1.wordIndex) ){
			
			shortestPathForNodeTwo.add(node2.wordIndex);
			shortestPathForNodeTwo = findShortestPathBetweenTwoNodes(
					node2.wordIndex, shortestPathForNodeTwo, 
					new ArrayList<Integer>(), node1.wordIndex, ignoreConj);
			
			commonHeadIndx = node2.wordIndex;
		}	
		else*/ {
		
			// Find the grandparents which has both the target nodes as grand children.
			for ( int i=0; i<allNodesByWordIndex.length; i++ ){
				
				if ( ( allNodesByWordIndex[i].getChildrenWordIndexes().contains(node1.wordIndex) ||
						allNodesByWordIndex[i].getGrandChildrenWIs().contains(node1.wordIndex) || 
						allNodesByWordIndex[i].wordIndex == node1.wordIndex )
						&& 
						(allNodesByWordIndex[i].getChildrenWordIndexes().contains(node2.wordIndex) ||
								allNodesByWordIndex[i].getGrandChildrenWIs().contains(node2.wordIndex) || 
								allNodesByWordIndex[i].wordIndex == node2.wordIndex ) ){
				
					ArrayList<Integer> tempPathForNodeOne = new ArrayList<Integer>(),
								tempPathForNodeTwo = new ArrayList<Integer>();

					tempPathForNodeOne.add(allNodesByWordIndex[i].wordIndex);
					tempPathForNodeTwo.add(allNodesByWordIndex[i].wordIndex);
					
					if ( allNodesByWordIndex[i].wordIndex != node1.wordIndex  )
						tempPathForNodeOne = findShortestPathBetweenTwoNodes(
							allNodesByWordIndex[i].wordIndex, tempPathForNodeOne, 
							new ArrayList<Integer>(), node1.wordIndex, ignoreConj);
					
					if ( tempPathForNodeOne.size() > 1 || allNodesByWordIndex[i].wordIndex == node1.wordIndex  ){
						
						if ( allNodesByWordIndex[i].wordIndex != node2.wordIndex  )
							tempPathForNodeTwo = findShortestPathBetweenTwoNodes(
									allNodesByWordIndex[i].wordIndex, tempPathForNodeTwo, 
									new ArrayList<Integer>(), node2.wordIndex, ignoreConj);
						
						// setting the shortest path
						if ( (tempPathForNodeTwo.size() > 1 || allNodesByWordIndex[i].wordIndex == node2.wordIndex ) 
								&&
							( commonHeadIndx == -1 ||
							tempPathForNodeOne.size() + tempPathForNodeTwo.size()
							< shortestPathForNodeOne.size() + shortestPathForNodeTwo.size()) ){
						
							commonHeadIndx = allNodesByWordIndex[i].wordIndex; 
						
							shortestPathForNodeOne = tempPathForNodeOne;
							shortestPathForNodeTwo = tempPathForNodeTwo;
						}
					}				
				}
			}
		}
		
		//-- create the sub-tree 
		if ( commonHeadIndx > -1 ){
			DepTreeNode root = allNodesByWordIndex[commonHeadIndx].copyExcludingChildrenAndParents();
			
			if ( commonHeadIndx == node1.wordIndex )
				root = node1;
			else if ( commonHeadIndx == node2.wordIndex )
				root = node2;
			
			DepTreeNode tempNode = root;
			
			for ( int i=1; i<shortestPathForNodeOne.size(); i++ ){
				DepTreeNode newNode;
								
				if ( node1.wordIndex == shortestPathForNodeOne.get(i) )
					newNode = node1;
				else if ( node2.wordIndex == shortestPathForNodeOne.get(i) )
					newNode = node2;
				else
					newNode = allNodesByWordIndex[shortestPathForNodeOne.get(i)].copyExcludingChildrenAndParents();
					
				tempNode.addChild(newNode, false, allNodesByWordIndex[newNode.wordIndex].getDepRelTypeByParentWI(tempNode.wordIndex));
				tempNode = newNode;
			}
			
			tempNode = root;
		
			for ( int i=1; i<shortestPathForNodeTwo.size(); i++ ){
				DepTreeNode newNode;
				
				if ( node2.wordIndex == shortestPathForNodeTwo.get(i) )
					newNode = node2;
				else if ( node1.wordIndex == shortestPathForNodeTwo.get(i) )
					newNode = node1;					
				else
					newNode = allNodesByWordIndex[shortestPathForNodeTwo.get(i)].copyExcludingChildrenAndParents();
					
				tempNode.addChild(newNode, allNodesByWordIndex[newNode.wordIndex].getDepRelTypeByParentWI(tempNode.wordIndex));
				tempNode = newNode;		
			}
			
			return root;
		}
			
		return null;
	}
	
	/**
	 * 
	 * @param boundaryEnt
	 * @param arrBoundariesByWordIndexes
	 * @return
	 */
	public DepTreeNode getHeadWordFromWordBoundaries ( ArrayList<Integer> listOfAllWordIndexes, boolean isNP, Sentence curSen ) {
		
		if ( isNP ) {
			int hwi = PhraseStructureTree.getHeadWordIndxOfNP(curSen, listOfAllWordIndexes);
			
			DepTreeNode dn = allNodesByWordIndex[hwi].clone();
			dn.removeChildrenNotInList(listOfAllWordIndexes, new ArrayList<Integer>());
			return dn;
		}
		else
			return findHeadAndSubTree( DataStrucUtility.listCopy(listOfAllWordIndexes));
	}
	
	/**
	 * 
	 * @param isSimplifyEntity
	 * @param boundaryEnt1
	 * @param boundaryEnt2
	 * @param medtType
	 * @param arrBoundariesByWordIndexes
	 * @param isNP
	 * @param curSen
	 * @return
	 */
	public DepTreeNode findMinimalSubTreeWithEntities ( boolean isSimplifyEntity, ArrayList<Integer> listOfAllWordIndexesOfEntOne,
			ArrayList<Integer> listOfAllWordIndexesOfEntTwo, int medtType,
			boolean isNP, Sentence curSen ){
		
		DepTreeNode headOfEnt1 = getHeadWordFromWordBoundaries( listOfAllWordIndexesOfEntOne, isNP, curSen), 
			headOfEnt2 = getHeadWordFromWordBoundaries( listOfAllWordIndexesOfEntTwo, isNP, curSen);
		
		return findMinimalSubTreeWithEntities(isSimplifyEntity, headOfEnt1, medtType, headOfEnt2);
	}
	
	
	/**
	 * 
	 * @param isSimplifyEntity
	 * @param headOfEnt1
	 * @param medtType
	 * @param headOfEnt2
	 * @return
	 */
	public DepTreeNode findMinimalSubTreeWithEntities ( boolean isSimplifyEntity,
				DepTreeNode headOfEnt1, int medtType, DepTreeNode headOfEnt2 ){
		
		if ( headOfEnt1 == null || headOfEnt2 == null )
			return null;
			//		/*
		//-- Start only head words of entities
		if ( isSimplifyEntity && !headOfEnt1.getChildrenWordIndexes().isEmpty() )			
			headOfEnt1.clearChildren();
		
		if ( isSimplifyEntity && headOfEnt2.getChildrenWordIndexes().isEmpty() )			
			headOfEnt2.clearChildren();
		
		//-- End only head words of entities
		//*/
		if ( headOfEnt1.wordIndex == headOfEnt2.wordIndex )
			return null;
		
		headOfEnt1.clearParents();
		headOfEnt2.clearParents();
		
		if ( TKOutputGenerator.isBlindEntity ) {
			headOfEnt1.word = "ENT_T1";
			headOfEnt1.lemma = "ENT_T1";
			headOfEnt1.pos = "ENT_T1";
		
			headOfEnt2.word = "ENT_T2";
			headOfEnt2.lemma = "ENT_T2";
			headOfEnt2.pos = "ENT_T2";
		}
		
		if ( !TKOutputGenerator.isConsiderNeCat ){
			headOfEnt1.setNEcategory(DepTreeNode.targetNEcatPrefix + "-1");
			headOfEnt2.setNEcategory(DepTreeNode.targetNEcatPrefix + "-2");
		}
		else {
			headOfEnt1.setNEcategory(DepTreeNode.targetNEcatPrefix + "-1-" + headOfEnt1.getNEcategory());
			headOfEnt2.setNEcategory(DepTreeNode.targetNEcatPrefix + "-2-" + headOfEnt1.getNEcategory());
		}
		
		// Find common parent of the heads found in earlier step.
		DepTreeNode nodeCommonHead = null;
		if ( headOfEnt1.wordIndex < headOfEnt2.wordIndex ) {
			nodeCommonHead = findCommonHead(headOfEnt1, headOfEnt2, 0, true);
			if ( nodeCommonHead == null )
				nodeCommonHead = findCommonHead(headOfEnt1, headOfEnt2, 0, false);
		}
		else {
			nodeCommonHead = findCommonHead(headOfEnt2, headOfEnt1, 0, true);
			if ( nodeCommonHead == null )
				nodeCommonHead = findCommonHead(headOfEnt2, headOfEnt1, 0, false);
		}

		if (  (medtType == 3 || medtType == 4 || medtType == 6)
				&& nodeCommonHead != null && !TextUtility.isEmptyString(nodeCommonHead.getNEcategory())
				&& nodeCommonHead.getNEcategory().contains(DepTreeNode.targetNEcatPrefix) ){
		
			 nodeCommonHead = extendShortestPathIfEntHeadIsRoot(nodeCommonHead);
		}
		
		if (  (medtType == 2 || medtType == 5 || medtType == 6)
				&& nodeCommonHead != null && nodeCommonHead.posGeneral.equals("verb") ){
			
			nodeCommonHead = extendShortestPathBySubject(nodeCommonHead);
		}
		
		if ( (medtType == 1 || medtType == 4 || medtType == 6)
				&& nodeCommonHead != null && 
				(nodeCommonHead.posGeneral == null || !nodeCommonHead.posGeneral.equals("verb") && !nodeCommonHead.posGeneral.equals("adj")) ){
		
			 nodeCommonHead = extendShortestPath(nodeCommonHead);
		}
	
		// TODO: why we are using this rule ??
		if ( nodeCommonHead != null &&  nodeCommonHead.countTotalNodesInSubTree() < 4 ){
			nodeCommonHead = extendShortestPathByAddingChildOfRoot(nodeCommonHead);
		}
	
		/*
		 // TODO: did we mention usage of this rule in previous publications ??
		// if the common head only contains the two target entities as children
		if ( nodeCommonHead != null &&  nodeCommonHead.getChildrenWordIndexes().size() == 2
				&& nodeCommonHead.getChildrenWordIndexes().contains(headOfEnt1.wordIndex) 
				&& nodeCommonHead.getChildrenWordIndexes().contains(headOfEnt2.wordIndex) ){
			nodeCommonHead = extendShortestPathByAddingChildOfRoot(nodeCommonHead);
		}
		 */
		
		return nodeCommonHead;
	}
	
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	private DepTreeNode extendShortestPathIfEntHeadIsRoot( DepTreeNode node ){
				
		//-- Basically, here we consider the first parent (if there are multiple parents) only
		
		//for ( int p1=0; p1<node.getParentsWordIndexes().size(); p1++ ){
			//if ( allNodesByWordIndex[node.getParentsWordIndexes().get(p1)].posGeneral.equals("verb") 
				//	|| allNodesByWordIndex[node.getParentsWordIndexes().get(p1)].posGeneral.equals("adj")  )
		
		int p1=0;
		if ( p1 < allNodesByWordIndex[node.wordIndex].getParentsWordIndexes().size() ) {
				DepTreeNode tmp = allNodesByWordIndex[node.wordIndex].getParents().get(p1);
				DepTreeNode nodeNew = tmp.copyExcludingChildrenAndParents();
				
				nodeNew.addChild(node, false, tmp.getDepRelTypeByChildWI(node.wordIndex));
				return nodeNew;
		}
		//}
		
		return node;
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	private DepTreeNode extendShortestPathByAddingChildOfRoot( DepTreeNode node ){
			
		for ( int p1=0; p1<node.getChildrenWordIndexes().size(); p1++ ){
			if ( allNodesByWordIndex[node.getChildrenWordIndexes().get(p1)].pos.matches("(V.*|NN.*)")  ){
				DepTreeNode nodeNew = allNodesByWordIndex[node.getChildrenWordIndexes().get(p1)].copyExcludingChildrenAndParents();
				node.addChild(nodeNew, false, allNodesByWordIndex[node.getChildrenWordIndexes().get(p1)].getDepRelTypeByParentWI(node.wordIndex));
			}
		}
			
		return node;
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	private DepTreeNode extendShortestPath( DepTreeNode node ){
	
		for ( int p1=0; p1<node.getChildren().size(); p1++ )
			if ( node.getChildren().get(p1).posGeneral.equals("verb") 
					|| node.getChildren().get(p1).posGeneral.equals("adj")  )
				return node;
			
		for ( int p1=0; p1<node.getChildrenWordIndexes().size(); p1++ ){
			if ( allNodesByWordIndex[node.getChildrenWordIndexes().get(p1)].posGeneral.equals("verb") 
					|| allNodesByWordIndex[node.getChildrenWordIndexes().get(p1)].posGeneral.equals("adj")  ){
				
				DepTreeNode tmp = allNodesByWordIndex[node.getChildrenWordIndexes().get(p1)];
				DepTreeNode nodeNew = tmp.copyExcludingChildrenAndParents();
				
				node.addChild(nodeNew, false, tmp.getDepRelTypeByParentWI(node.wordIndex));
				return node;
			}
		}
		
		for ( int p1=0; p1<allNodesByWordIndex[node.wordIndex].getParentsWordIndexes().size(); p1++ ){
			DepTreeNode tmp = allNodesByWordIndex[node.wordIndex].getParents().get(p1);
			
			if ( tmp.posGeneral.equals("verb") 
					|| tmp.posGeneral.equals("adj")  ){
				
				DepTreeNode nodeNew = tmp.copyExcludingChildrenAndParents();
				
				nodeNew.addChild(node, false, tmp.getDepRelTypeByChildWI(node.wordIndex));
				return nodeNew;
			}
		}
		
		for ( int p1=0; p1<allNodesByWordIndex[node.wordIndex].getParentsWordIndexes().size(); p1++ ){
			DepTreeNode tmp = allNodesByWordIndex[node.wordIndex].getParents().get(p1);
			DepTreeNode nodeNew = tmp.copyExcludingChildrenAndParents();
				
			nodeNew.addChild(node, false, tmp.getDepRelTypeByChildWI(node.wordIndex));
			nodeNew = extendShortestPath(nodeNew);
			
			if ( nodeNew.posGeneral.equals("verb") 
					|| nodeNew.posGeneral.equals("adj")  )
				return nodeNew;
		}
		
		return node;
	}
	
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	private DepTreeNode extendShortestPathBySubject( DepTreeNode node ){
		
		for ( int p1=0; p1<node.getRelNamesWithChildren().size(); p1++ )
			if ( node.getRelNamesWithChildren().get(p1).contains("subj") )
				return node;
			
		int indx = -1; 
		for ( int k=0; k<allNodesByWordIndex.length; k++ )
			if ( allNodesByWordIndex[k].getParentsWordIndexes() != null
					&& (indx=allNodesByWordIndex[k].getParentsWordIndexes().indexOf(node.wordIndex)) > -1
					&& allNodesByWordIndex[k].getRelNamesWithParents().get(indx).contains("subj") ){

				DepTreeNode nodeNew = allNodesByWordIndex[k].copyExcludingChildrenAndParents();
				
				node.addChild(nodeNew, false, allNodesByWordIndex[k].getDepRelTypeByParentWI(node.wordIndex));
				return node;
			}

		return node;
	}
	
	/**
	 * 
	 * @param dn
	 * @return
	 */
	public int[] boundaryWordIndexOfTree( DepTreeNode dn ) {
		
		int[] bn = new int[]{10000, -1};
		
		ArrayList<Integer> allWords = new ArrayList<Integer>();
		
		if ( dn.getParentsWordIndexes() != null )
			allWords.addAll(dn.getParentsWordIndexes());
		
		if ( !dn.getChildrenWordIndexes().isEmpty() )
			allWords.addAll(dn.getChildrenWordIndexes());
		
		allWords.addAll(dn.getGrandChildrenWIs());
		
		allWords.add(dn.wordIndex);
		
		for ( int i=0; i<allWords.size(); i++ ) {
			if ( allWords.get(i) < bn[0] )
				bn[0] = allWords.get(i);
			if ( allWords.get(i) > bn[1] )
				bn[1] = allWords.get(i);			
		}
		
		return bn;
	}
	
	
	/**
	 * 
	 * @param commonHead
	 * @param boundaryEnt1
	 * @param boundaryEnt2
	 */
	public void replaceEntitiesWithDummies ( DepTreeNode dn, int[] boundaryEnt1,
			int[] boundaryEnt2, ArrayList<Integer> listOfNodesTraversed ) {
		try {
				
		listOfNodesTraversed.add(dn.wordIndex);
		
		if ( !TextUtility.isEmptyString(dn.getNEcategory()) ) {
				 
			if	( //dn.getNEcategory().toLowerCase().matches(TKOutputGenerator.regExRelArgType) || 
					 dn.getNEcategory().contains(DepTreeNode.targetNEcatPrefix) )  {
				if ( DataStrucUtility.hasOverlap(new int[]{dn.startCharIndex, dn.endCharIndex}, boundaryEnt1) ) {
					dn.word = DepTreeNode.targetNEcatPrefix + "_T1";
					dn.lemma = DepTreeNode.targetNEcatPrefix + "_T1";
					dn.pos = DepTreeNode.targetNEcatPrefix + "_T1";				
				}
				else if ( DataStrucUtility.hasOverlap(new int[]{dn.startCharIndex, dn.endCharIndex}, boundaryEnt2) ) {
					dn.word = DepTreeNode.targetNEcatPrefix + "_T2";
					dn.lemma = DepTreeNode.targetNEcatPrefix + "_T2";
					dn.pos = DepTreeNode.targetNEcatPrefix + "_T2";				
				}
			}
			else {
				dn.word = dn.getNEcategory() + "_Entity_Type";
				dn.lemma = dn.getNEcategory() + "_Entity_Type";
				dn.pos = dn.getNEcategory() + "_Entity_Type";
			}		
		}
		
		for ( int i=0; i< dn.getChildrenWordIndexes().size(); i++ )
			if ( !listOfNodesTraversed.contains(dn.getChildren().get(i).wordIndex) ) {
				try {
				replaceEntitiesWithDummies(dn.getChildren().get(i), boundaryEnt1, boundaryEnt2, listOfNodesTraversed);
				}
				catch (Exception ex) {
					System.err.println(ex.getMessage());
					System.exit(0);
				}				
			}
		}
		catch (Exception ex) {
			System.err.println(ex.getMessage());
			System.exit(0);
		}
	}
}