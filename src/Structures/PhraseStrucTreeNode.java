package Structures;

import java.util.ArrayList;

import Utility.DataStrucUtility;
import Utility.TextUtility;

public class PhraseStrucTreeNode{

	public String word = null, pos = null, lemma = null, posGeneral = "", pharasalCat = null;
	public int nodeIndex = -1, startCharIndex = -1, endCharIndex = -1, wordIndexByParser = -1;
	
	private String NEcategory = "";
	
	public ArrayList<PhraseStrucTreeNode> listOfChildren = new ArrayList<PhraseStrucTreeNode>();
		
	public PhraseStrucTreeNode parent = null;
	
	// index of all lower tree nodes in the same branch
	private ArrayList<Integer> allTerminalNodeIndexesUnderThisNode = new ArrayList<Integer>();
	
	private ArrayList<Integer> allWordIndexesUnderThisNode = new ArrayList<Integer>();

	public ArrayList<Integer> getAllTerminalNodeIndexesUnderThisNode () {
		return this.allTerminalNodeIndexesUnderThisNode;
	}
	
	public void addTerminalNodeIndexesUnderThisNode ( ArrayList<Integer> listOfIndexes ) {
		
		this.allTerminalNodeIndexesUnderThisNode.addAll(listOfIndexes);
		this.allTerminalNodeIndexesUnderThisNode = DataStrucUtility.getUniqueItems(this.allTerminalNodeIndexesUnderThisNode);
	}
	
	public void setTerminalNodeIndexesUnderThisNode ( ArrayList<Integer> listOfIndexes ) {
		this.allTerminalNodeIndexesUnderThisNode = listOfIndexes;
	}
	
	public void addTerminalNodeIndexesUnderThisNode ( int wi ) {
		if ( !this.allTerminalNodeIndexesUnderThisNode.contains(wi) )
			this.allTerminalNodeIndexesUnderThisNode.add(wi);
	}
	
	public void removeTerminalNodeIndexesUnderThisNode ( ArrayList<Integer> listOfIndexes ) {
		this.allTerminalNodeIndexesUnderThisNode.removeAll(listOfIndexes);
	}
	
	
	
	public ArrayList<Integer> getAllWordIndexesUnderThisNode () {
		return this.allWordIndexesUnderThisNode;
	}
	
	public void addWordIndexesUnderThisNode ( ArrayList<Integer> listOfIndexes ) {
		
		this.allWordIndexesUnderThisNode.addAll(listOfIndexes);
		this.allWordIndexesUnderThisNode = DataStrucUtility.getUniqueItems(this.allWordIndexesUnderThisNode);
	}
	
	public void setWordIndexesUnderThisNode ( ArrayList<Integer> listOfIndexes ) {
		this.allWordIndexesUnderThisNode = listOfIndexes;
	}
	
	public void addWordIndexesUnderThisNode ( int wi ) {
		if ( !this.allWordIndexesUnderThisNode.contains(wi) )
			this.allWordIndexesUnderThisNode.add(wi);
	}
	
	public void removeWordIndexesUnderThisNode ( ArrayList<Integer> listOfIndexes ) {
		this.allWordIndexesUnderThisNode.removeAll(listOfIndexes);
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public PhraseStrucTreeNode clone() {
		
		PhraseStrucTreeNode pstNode = new PhraseStrucTreeNode();
		
		pstNode.word = word;
		pstNode.pos = pos;
		pstNode.lemma = lemma; 
		pstNode.posGeneral = posGeneral; 
		pstNode.pharasalCat = pharasalCat;
		pstNode.nodeIndex = nodeIndex;
		pstNode.startCharIndex = startCharIndex;
		pstNode.endCharIndex = endCharIndex;
		pstNode.wordIndexByParser = wordIndexByParser;
		
		pstNode.NEcategory = NEcategory;
		
		for ( int i=0; i<this.listOfChildren.size(); i++ )
			pstNode.listOfChildren.add(this.listOfChildren.get(i).clone());
		
		if ( parent != null )
			pstNode.parent = parent.clone();
		
		pstNode.allTerminalNodeIndexesUnderThisNode = (ArrayList<Integer>) this.getAllTerminalNodeIndexesUnderThisNode().clone();
		
		return pstNode;
    }

	/**
	 * 
	 * @return
	 */
	public String getNEcategory () {
        return NEcategory;
    }
    
	/**
	 * 
	 * @param category
	 */
    public void setNEcategory (String category)
    {
    	if ( category == null )
    		category = "";
        NEcategory = category;           
    }
	
	/**
	 * 
	 * @param child
	 */
	public void addChild( PhraseStrucTreeNode child ){
		
		child.parent = this;		
		listOfChildren.add(child);
	}

	/**
	 * 
	 * @param isIncludeWord
	 * @param isIncludePOS
	 * @param isIncludeLemma
	 * @param isIncludePOSGeneral
	 * @param isIncludePharasalCat
	 * @return
	 */
	public String printTree( boolean isIncludeWord, boolean isIncludePOS, boolean isIncludeLemma,
			boolean isIncludePOSGeneral, boolean isIncludePharasalCat){

		return consturctPrintTree(isIncludeWord, isIncludePOS,  
				isIncludeLemma, isIncludePOSGeneral, isIncludePharasalCat);
	}
	
	/**
	 * 
	 * @param isIncludeWord
	 * @param isIncludePOS
	 * @param isIncludeLemma
	 * @param isIncludePOSGeneral
	 * @param isIncludePharasalCat
	 * @return
	 */
	private String consturctPrintTree( boolean isIncludeWord, boolean isIncludePOS, boolean isIncludeLemma,
			boolean isIncludePOSGeneral, boolean isIncludePharasalCat){
		String str = "";
	
		if ( !TextUtility.isEmptyString(this.NEcategory) )
			str += " (" + this.NEcategory;
		
		if ( isIncludePOS && !TextUtility.isEmptyString(this.pos) )
			str += " (" + this.pos;
		
		if ( isIncludeWord && !TextUtility.isEmptyString(this.word) )
			str += " (" + this.word.replaceAll("\\(", "<LB>").replaceAll("\\)", "<RB>"); 
		
		if ( isIncludeLemma && !TextUtility.isEmptyString(this.lemma) )
			str += " (" + this.lemma;
		
		for ( int i=0; i<listOfChildren.size(); i++ )
			str += listOfChildren.get(i).consturctPrintTree( isIncludeWord, isIncludePOS, isIncludeLemma, isIncludePOSGeneral, isIncludePharasalCat);
			
		if ( isIncludeLemma && !TextUtility.isEmptyString(this.lemma) )
			str += ")";
		
		if ( isIncludeWord && !TextUtility.isEmptyString(this.word) )
			str += ")";
		
		if ( isIncludePOS && !TextUtility.isEmptyString(this.pos) )
			str += ")";
				
		if ( !TextUtility.isEmptyString(this.NEcategory) )
			str += ")";
		
		return str;
	}

}
