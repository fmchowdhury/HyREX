package Structures;

import java.util.ArrayList;

public class DepGraphVertex {

	public String word = null, pos = null, lemma = null, posGeneral = "", pharasalCat = null;
	private String NEcategory = "";
	public int wordIndex = -1, startCharIndex = -1, endCharIndex = -1;

	public ArrayList<Integer>  parentWordIndexes = new ArrayList<Integer>();
	public ArrayList<Integer> childrenWordIndex = new ArrayList<Integer>();
	public ArrayList<Integer> grandChildrenWordIndex = new ArrayList<Integer>();
	public ArrayList<String> relNamesWithChildren = new ArrayList<String>();
	public ArrayList<String> relNameWithParents = new ArrayList<String>();

	public DepGraphVertex( ){
		
	}
	
	@SuppressWarnings("unchecked")
	public DepGraphVertex clone () {
		DepGraphVertex newNode = new DepGraphVertex();

		newNode.word = word; 
		newNode.pos = pos; 
		newNode.lemma = lemma; 
		newNode.posGeneral = posGeneral; 
		newNode.pharasalCat = pharasalCat;
		newNode.NEcategory = NEcategory;
		newNode.wordIndex = wordIndex; 
		newNode.startCharIndex = startCharIndex; 
		newNode.endCharIndex = endCharIndex;

		newNode.parentWordIndexes = (ArrayList<Integer>) parentWordIndexes.clone();
		newNode.childrenWordIndex = (ArrayList<Integer>) childrenWordIndex.clone();
		newNode.grandChildrenWordIndex = (ArrayList<Integer>) grandChildrenWordIndex.clone();
		newNode.relNamesWithChildren = (ArrayList<String>) relNamesWithChildren.clone();
		newNode.relNameWithParents = (ArrayList<String>) relNameWithParents.clone();

		return newNode;
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
	 * @return
	 */
	public int countTotalNodesInSubTree() {
		
		ArrayList<Integer> listOfNodes = new ArrayList<Integer>();
		
		if ( parentWordIndexes != null )
			for( int i=0; i<parentWordIndexes.size(); i++ )
				if ( !listOfNodes.contains(parentWordIndexes.get(i)) )
					listOfNodes.add(parentWordIndexes.get(i));
		
		if ( childrenWordIndex != null )
			for( int i=0; i<childrenWordIndex.size(); i++ )
				if ( !listOfNodes.contains(childrenWordIndex.get(i)) )
					listOfNodes.add(childrenWordIndex.get(i));
		
		if ( listOfNodes.contains(wordIndex) )
			return listOfNodes.size();
			
		return listOfNodes.size() + 1;
	}

	/**
	 * 
	 * @param child
	 * @param isAddBefore
	 */
	public void addChild( DepGraphVertex child, boolean isAddBefore ){
		
		if ( this.childrenWordIndex.contains(child.wordIndex) ){
			for ( int i=0; i<childrenWordIndex.size(); i++ )
				if ( childrenWordIndex.get(i) == child.wordIndex ){
					childrenWordIndex.remove(i);				
					break;
				}
		}
				
		int index = childrenWordIndex.size();
		if ( isAddBefore )
			index = 0;
				
		this.childrenWordIndex.add(index, child.wordIndex);
		
		int ind = child.parentWordIndexes.indexOf(this.wordIndex);
		
		this.relNamesWithChildren.add(index, child.relNameWithParents.get(ind));		
	}


}
