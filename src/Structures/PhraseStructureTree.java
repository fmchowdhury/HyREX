package Structures;

import java.util.ArrayList;

import Kernels.TKOutputGenerator;
import Utility.*;

public class PhraseStructureTree {

	PhraseStrucTreeNode root = new PhraseStrucTreeNode();	
	ArrayList<int[]> listOfBoundariesByWordIndexes = new ArrayList<int[]>();
	ArrayList<String> listOfNodesString = new ArrayList<String>();
	
	public String pst = "";
	
	public ArrayList<PhraseStrucTreeNode> listOfNodesByWIs = new ArrayList<PhraseStrucTreeNode>();
	
	public PhraseStructureTree() {
		
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public PhraseStructureTree clone( Sentence objCurSen )
    {
		/*
		PhraseStructureTree newNode = new PhraseStructureTree();
		
		newNode.root = root.clone();	
		newNode.listOfBoundariesByWordIndexes = (ArrayList<int[]>) listOfBoundariesByWordIndexes.clone();
		newNode.listOfNodesString = (ArrayList<String>) listOfNodesString.clone();
		newNode.listOfWordAndNodeIndexes = (ArrayList<int[]>) listOfWordAndNodeIndexes.clone();
		
		return newNode;
		*/
		PhraseStructureTree pst = new PhraseStructureTree(objCurSen.psgTree.pst, 
				objCurSen.arrWordAndPosByParser, objCurSen.getWordsAndNE());
		
		for ( int e=0; e<objCurSen.listOfEntities.size(); e++ )			
			pst.setNEcatToPhraseInsteadOfWords(objCurSen.listOfEntities.get(e).getAllWordIndexes(), "");
		
		return pst;
    }
	
	/**
	 * 
	 * @param ent
	 * @param curSen
	 * @return
	 */
	public static int getHeadWordIndxOfNP(Sentence curSen, ArrayList<Integer> listOfWI) {

		listOfWI = DataStrucUtility.sort(listOfWI);
		
		/*
		 * Rule 1a: For terms fitting the pattern X of... (where of represents
		 * any preposition) the term X was taken as the head noun. Rule 1b: For
		 * terms fitting the pattern X , ... the term X was taken as the head
		 * noun.
		 */
		ArrayList<Integer> listOfWIforChecking = new ArrayList<Integer>();
		listOfWIforChecking.add(listOfWI.get(0));

		// the 1st word of the NP could be wrongly annotated as preposition by
		// the parser. So, we skip it.
		for (int w = listOfWI.get(0)+1; w <= listOfWI.get(listOfWI.size()-1); w++) {
			if (curSen.arrWordAndPosByParser[w][1]
					.equalsIgnoreCase("IN"))
				break;

			listOfWIforChecking.add(w);
		}

		// Rule 2: For all other terms, the rightmost word was taken as the head
		// noun.
		for (int i = 0; i < listOfWIforChecking.size()
				&& listOfWIforChecking.size() > 1; i++) {
			// this to make sure the rightmost alphanumeric word gets selected
			if (!curSen.arrWordAndPosByParser[listOfWIforChecking.get(i)][0]
					.matches(".*[A-Za-z0-9].*")) {
				listOfWIforChecking.remove(i);
				i--;
				continue;
			}
		}

		if (listOfWIforChecking.size() > 0)
			return listOfWIforChecking.get(listOfWIforChecking.size() - 1);
		else
			return 0;
	}



	/**
	 * 
	 * @param pst
	 * @return
	 */
	public String[][] getWordAndPosFromParseTree ( String pst ){
		return readParseTreeString(pst, null, null);
	}
	
	
	/**
	 * 
	 * @param wordIndex
	 * @return
	 */
	public PhraseStrucTreeNode getParentPhraseNode ( ArrayList<Integer> wordIndexes ) {
	
		ArrayList<Integer> listOfNodeIndexes = new ArrayList<Integer>();
		
		for ( int i=0; i< wordIndexes.size(); i++ ) {
			listOfNodeIndexes.add(listOfNodesByWIs.get(wordIndexes.get(i)).nodeIndex);			
		}
		
		if ( listOfNodeIndexes.size() > 0 )
			return getParentPhraseNode(root, listOfNodeIndexes);
		
		return null;
	}
	
	/**
	 * 
	 * @param wordIndex
	 * @return
	 */
	public String getParentPhraseType ( int wordIndex ) {
		
		return getParentPhraseNode(wordIndex).pos;
	}
	
	/**
	 * 
	 * @param wordIndex
	 * @return
	 */
	public PhraseStrucTreeNode getParentPhraseNode ( int wordIndex ) {
		
		ArrayList<Integer> temp = new ArrayList<Integer>();
		temp.add(listOfNodesByWIs.get(wordIndex).nodeIndex);
		
		return getParentNode(root, temp);
	}
	
	/**
	 * 
	 * @param curNode
	 * @param nodeIndexes
	 * @return
	 */
	private PhraseStrucTreeNode getParentNode ( PhraseStrucTreeNode curNode, ArrayList<Integer> nodeIndexes ) {
			
		boolean isFound = false;
		while ( curNode.getAllTerminalNodeIndexesUnderThisNode().containsAll(nodeIndexes) ){
			isFound = true;
			
			int i = 0, limit = curNode.listOfChildren.size();
			for ( ; i<limit; i++ ){
				if ( curNode.listOfChildren.get(i).
						getAllTerminalNodeIndexesUnderThisNode().containsAll(nodeIndexes) ) {	
					
					curNode = curNode.listOfChildren.get(i);
					break;
				}
			}
			
			if ( i == limit )
				break;
		}
		
		if ( isFound )			
			return curNode;		
		
		return null;
	}
	
	/**
	 * 
	 * @param curNode
	 * @param nodeIndex
	 * @return
	 */
	private PhraseStrucTreeNode getParentPhraseNode ( PhraseStrucTreeNode curNode, ArrayList<Integer> nodeIndexes ) {
	
		curNode = getParentNode(curNode, nodeIndexes);
		
		// if it is the last non-terminal node but not a phrase (e.g. NNP)
		if ( curNode != null ) {
			if ( curNode.listOfChildren.isEmpty() )		
				return curNode.parent;
			
			return curNode;
		}
			
		return null;
	}
	
	/**
	 * 
	 * @param pst
	 * @param arrWordWithPosByParser
	 * @return
	 */
	private String[][] readParseTreeString ( String pst, String[][] arrWordWithPosByParser, String[][] arrWordsAndNEs ){
		this.pst = pst;
		String[] temp = pst.replaceAll("\\s+", " ").replaceAll("\\)\\s+\\)", "))").trim().split("\\(");
		ArrayList<String> tagAndWords = new ArrayList<String>();
	
		for ( int i=0; i<temp.length; i++ ){
			temp[i] = temp[i].trim();
			
			if ( temp[i].isEmpty() )
				continue;
			
			String[] str = temp[i].replaceAll("\\)", ")#@%#").split("#@%#"); 
			for ( int k=0; k<str.length; k++ )
				if ( !(str[k]=str[k].trim()).isEmpty() )
					tagAndWords.add(str[k]);
		}
		
		PhraseStrucTreeNode node = new PhraseStrucTreeNode(), prevNode = null;
		ArrayList<String[]> listTemp = new ArrayList<String[]>();
		
		int sCharIndex = 0, wi = 0, ni=0;
		for ( int ii=0; ii<tagAndWords.size(); ii++ ){
		
			String[] str = tagAndWords.get(ii).split("\\s+");
			node = new PhraseStrucTreeNode();
			node.pos = str[0];
			node.nodeIndex = ni;
		
			if ( tagAndWords.get(ii).contains(")") ){
				
				if ( tagAndWords.get(ii).equals(")") ){
					if ( prevNode == null )
						break;
					
					prevNode = prevNode.parent;
					continue;
				}
				
				str[1] = str[1].replace(")", "");
				node.startCharIndex = sCharIndex;
				
				// if the tokenized words are provided
				if ( arrWordWithPosByParser != null && arrWordWithPosByParser.length > 0 )
					node.word = arrWordWithPosByParser[wi][0];
				else {
					str[1] = ParseOutputUtility.reconstructOrigTokensFromPrasedToken(str[1]);					
					node.word = str[1];
					listTemp.add( new String[]{str[1], str[0]});
				}
						
				node.wordIndexByParser = wi;

				if ( arrWordsAndNEs != null && arrWordsAndNEs.length > 0 
						&& !TextUtility.isEmptyString(arrWordsAndNEs[wi][1]))
					node.setNEcategory(arrWordsAndNEs[wi][1] + "_ENTITY" );
				
				wi++;
								
				sCharIndex = sCharIndex + node.word.length();
				node.endCharIndex = sCharIndex  - 1;				
								
				node.lemma = SyntacticParser.getLemma(node.word, node.pos);
				
				if ( !node.lemma.equalsIgnoreCase(node.word) ) 
					node.lemma = node.lemma.trim();
				
				prevNode.addChild(node);
			}	
			else if ( prevNode == null ){
				
				prevNode = node;
				root = prevNode;
			}
			else{
				prevNode.addChild(node);
				prevNode = node;
			}
			
			ni++;
			listOfBoundariesByWordIndexes.add(new int[]{ node.startCharIndex, node.endCharIndex });
			if ( node.word != null )
				listOfNodesString.add( node.pos + " " + node.word);
			else
				listOfNodesString.add( node.pos );
			
			if ( node.wordIndexByParser > -1 )
				listOfNodesByWIs.add(node);
		}
		
		populateTerminalChildrenList(root);
		return DataStrucUtility.listToArrayOfString(listTemp); 
	}
	
	/**
	 * 
	 * @param listOfWIsForCurEnt
	 */
	public void setNEcatToPhraseInsteadOfWords ( ArrayList<Integer> listOfWIsForCurEnt, String catType ) {
		
		PhraseStrucTreeNode parentPhraseNode = getParentPhraseNode(listOfWIsForCurEnt);
		
		if ( !TextUtility.isEmptyString(catType) ) {
			parentPhraseNode.setNEcategory(catType);
			return;
		}			
		
		ArrayList<String> listOfNEs = new ArrayList<String>();
		ArrayList<Integer> wordIndexes = parentPhraseNode.getAllWordIndexesUnderThisNode();
		
		for ( int i=0; i<wordIndexes.size(); i++ ) {
			String neCat = listOfNodesByWIs.get(wordIndexes.get(i)).getNEcategory();
			if ( !TextUtility.isEmptyString(neCat) && !listOfNEs.contains(neCat) )
				listOfNEs.add(neCat);
		}
		
		if ( listOfNEs.size() == 1 ) {
			parentPhraseNode.setNEcategory(listOfNEs.get(0));
						
			for ( int i=0; i<wordIndexes.size(); i++ ) {
				listOfNodesByWIs.get(wordIndexes.get(i)).setNEcategory("");
			}
		}
	}
	
	/**
	 * 
	 * @param pst
	 * @param arrWordWithPosByParser
	 */
	public PhraseStructureTree ( String pst, String[][] arrWordWithPosByParser, String[][] arrWordsAndNEs ){
		  readParseTreeString(pst, arrWordWithPosByParser, arrWordsAndNEs);
	}
	
	
	/**
	 * 
	 * @param currentNode
	 * @return
	 */
	private void populateTerminalChildrenList( PhraseStrucTreeNode currentNode ){
		
		if ( currentNode.listOfChildren.isEmpty() ){
			currentNode.addTerminalNodeIndexesUnderThisNode(currentNode.nodeIndex);
			currentNode.addWordIndexesUnderThisNode(currentNode.wordIndexByParser);
			return;
		}
		
		for ( int i=0; i<currentNode.listOfChildren.size(); i++ ){
			populateTerminalChildrenList(currentNode.listOfChildren.get(i));
			
			if ( !currentNode.listOfChildren.get(i).getAllTerminalNodeIndexesUnderThisNode().isEmpty() ) {
				currentNode.addTerminalNodeIndexesUnderThisNode(currentNode.listOfChildren.get(i).getAllTerminalNodeIndexesUnderThisNode());
				currentNode.addWordIndexesUnderThisNode(currentNode.listOfChildren.get(i).getAllWordIndexesUnderThisNode());
			}
		}
	}
	
	
	/**
	 * 
	 * @param curNode
	 * @param listEntNodeIndexes
	 * @return
	 */
	private PhraseStrucTreeNode findPetRootWithAllNodesOfEntities ( PhraseStrucTreeNode curNode, ArrayList<Integer> listEntNodeIndexes ){
		
		for ( int i=0; i<curNode.listOfChildren.size(); i++ ){
			if ( curNode.listOfChildren.get(i).getAllTerminalNodeIndexesUnderThisNode().containsAll(listEntNodeIndexes) ){
				curNode = curNode.listOfChildren.get(i);
				i=-1;
			}	
		}
		
		if ( curNode.getAllTerminalNodeIndexesUnderThisNode().containsAll(listEntNodeIndexes) )
			return curNode;
		
		return null;
	}
	
	/**
	 * 
	 * @param curNode
	 * @param listEntNodeIndexes
	 * @return
	 */
	private boolean hasTerminalNodeOverlap ( PhraseStrucTreeNode curNode, ArrayList<Integer> listEntNodeIndexes ){
		
		for ( int i=0; i<listEntNodeIndexes.size(); i++ ){
			if ( curNode.getAllTerminalNodeIndexesUnderThisNode().contains(listEntNodeIndexes.get(i)) )		
			return true;	
		}
		
		return false;
	}

	/**
	 * 
	 * @param curNode
	 * @param listEntNodeIndexes
	 * @return
	 */
	private void pruneLeft ( PhraseStrucTreeNode curNode, ArrayList<Integer> listEntNodeIndexes ){

		for ( int i=0; i<curNode.listOfChildren.size(); i++ ){
			if ( hasTerminalNodeOverlap ( curNode.listOfChildren.get(i), listEntNodeIndexes ) ){
				if ( !listEntNodeIndexes.containsAll(curNode.listOfChildren.get(i).getAllTerminalNodeIndexesUnderThisNode()) ) {
					pruneLeft( curNode.listOfChildren.get(i), listEntNodeIndexes );					
				}
				
				break;
			}
			else{
				curNode.removeTerminalNodeIndexesUnderThisNode(curNode.listOfChildren.get(i).getAllTerminalNodeIndexesUnderThisNode());
				curNode.removeWordIndexesUnderThisNode(curNode.listOfChildren.get(i).getAllWordIndexesUnderThisNode());
				curNode.listOfChildren.remove(i);
				i--;
			}
		}
		
		curNode.setTerminalNodeIndexesUnderThisNode(new ArrayList<Integer>());
		curNode.setWordIndexesUnderThisNode(new ArrayList<Integer>());
		populateTerminalChildrenList(curNode);
	}
	
	
	/**
	 * 
	 * @param curNode
	 * @param listEntNodeIndexes
	 * @return
	 */
	private void pruneRight ( PhraseStrucTreeNode curNode, ArrayList<Integer> listEntNodeIndexes ){
		
		for ( int i=curNode.listOfChildren.size()-1; i>=0; i-- ){
			if ( hasTerminalNodeOverlap ( curNode.listOfChildren.get(i), listEntNodeIndexes ) ){
				if ( !listEntNodeIndexes.containsAll(curNode.listOfChildren.get(i).getAllTerminalNodeIndexesUnderThisNode()) ) {
					pruneRight(curNode.listOfChildren.get(i), listEntNodeIndexes );
				}
				
				break;
			}
			else{
				curNode.removeTerminalNodeIndexesUnderThisNode(curNode.listOfChildren.get(i).getAllTerminalNodeIndexesUnderThisNode());
				curNode.removeWordIndexesUnderThisNode(curNode.listOfChildren.get(i).getAllWordIndexesUnderThisNode());
				curNode.listOfChildren.remove(i);
			}
		}	
		
		curNode.setTerminalNodeIndexesUnderThisNode(new ArrayList<Integer>());
		curNode.setWordIndexesUnderThisNode(new ArrayList<Integer>());
		populateTerminalChildrenList(curNode);
	}
	
	/**
	 * 
	 * @param entityName1
	 * @param boundaryEnt1
	 * @param entityName2
	 * @param boundaryEnt2
	 * @param isConsiderNeCat
	 * @return
	 */
	public PhraseStrucTreeNode findPathEnclosedTreeWithEntities ( Entity e1, Entity e2 ){
		
		ArrayList<Integer> listNodeIndexForBothEntities = new ArrayList<Integer>(),
				temp = new ArrayList<Integer>();;
		
		for ( int k=0; k<this.listOfNodesByWIs.size(); k++ ) {
			if ( this.listOfNodesByWIs.get(k).wordIndexByParser >= e1.getStartWordIndex()
					&& this.listOfNodesByWIs.get(k).wordIndexByParser <= e1.getEndWordIndex() )
					listNodeIndexForBothEntities.add(this.listOfNodesByWIs.get(k).nodeIndex);
			else if ( this.listOfNodesByWIs.get(k).wordIndexByParser >= e2.getStartWordIndex()
						&& this.listOfNodesByWIs.get(k).wordIndexByParser <= e2.getEndWordIndex() )
					temp.add(this.listOfNodesByWIs.get(k).nodeIndex);
		}
		
	//	System.out.println("E2 " + e2.printString());
	/*	
		if ( DataStrucUtility.hasOverlappingItems( DataStrucUtility.listToArray(listNodeIndexForBothEntities), 
				DataStrucUtility.listToArray(temp)) )
			return null;
		else
		*/ {
		
			if ( TKOutputGenerator.isConsiderNeCat ){
				setNEcatToPhraseInsteadOfWords( e1.getAllWordIndexes(), DepTreeNode.targetNEcatPrefix + "-1");
				setNEcatToPhraseInsteadOfWords( e2.getAllWordIndexes(), DepTreeNode.targetNEcatPrefix + "-2");
			} 
			else {
				setNEcatToPhraseInsteadOfWords( e1.getAllWordIndexes(), DepTreeNode.targetNEcatPrefix + "-1-" + e1.getNEcategory());
				setNEcatToPhraseInsteadOfWords( e2.getAllWordIndexes(), DepTreeNode.targetNEcatPrefix + "-2-" + e2.getNEcategory());
			}
			
			listNodeIndexForBothEntities.addAll(temp);
		}
		
		
		PhraseStrucTreeNode petRoot = findPetRootWithAllNodesOfEntities( root, listNodeIndexForBothEntities);
		
		// Prune left part of PET
		pruneLeft(petRoot, listNodeIndexForBothEntities);
		
		// Prune right part of PET
		pruneRight(petRoot, listNodeIndexForBothEntities);
		
		return petRoot;
	}
	
	
}