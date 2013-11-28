package Structures;

import java.util.ArrayList;

import Utility.DataStrucUtility;
import Utility.TextUtility;


public class DepTreeNode {

	public String word = null, pos = null, lemma = null, posGeneral = "", pharasalCat = null;
	private String NEcategory = "";
	public int wordIndex = -1, startCharIndex = -1, endCharIndex = -1;

	private ArrayList<Integer>  parentWordIndexes = new ArrayList<Integer>();
	private ArrayList<Integer> childrenWordIndex = new ArrayList<Integer>();
	private ArrayList<String> relNamesWithChildren = new ArrayList<String>();
	private ArrayList<String> relNameWithParents = new ArrayList<String>();

	// index of all lower tree nodes in the same branch
	private ArrayList<Integer> allGrandChildrenWordIndex = new ArrayList<Integer>();

	private ArrayList<DepTreeNode> children = new ArrayList<DepTreeNode>();
	private ArrayList<DepTreeNode> parents = new ArrayList<DepTreeNode>();
	
	public final static String targetNEcatPrefix = "NE-Type";

	/**
	 * 
	 * @param listOfWIs
	 * @return
	 */
	public boolean governAllWIsInList ( ArrayList<Integer> listOfWIs ) {
		ArrayList<Integer> listTemp = new ArrayList<Integer>();
		listTemp.addAll(this.getGrandChildrenWIs());
		listTemp.addAll(this.getChildrenWordIndexes());
		return listTemp.containsAll(listOfWIs);
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Integer> getGrandChildrenWIs( ){
		return allGrandChildrenWordIndex;
	}
	
	/**
	 * 
	 * @param gcWI
	 */
	public void addGrandChildWI( int gcWI ){
		
		if ( this.allGrandChildrenWordIndex.contains(gcWI) )
			return;
		
		this.allGrandChildrenWordIndex.add(gcWI);
	}
	
	/**
	 * 
	 * @param listOfGcWI
	 */
	public void addGrandChildWI( ArrayList<Integer> listOfGcWI ){
		
		this.allGrandChildrenWordIndex.addAll(DataStrucUtility.getUnCommonItems(this.allGrandChildrenWordIndex, listOfGcWI));
		
		this.allGrandChildrenWordIndex = DataStrucUtility.getUniqueItems(this.allGrandChildrenWordIndex);
		
		this.allGrandChildrenWordIndex.remove((Integer) this.wordIndex);
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<DepTreeNode> getChildren( ){
		return children;
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Integer> getChildrenWordIndexes( ){
		return childrenWordIndex;
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<String> getRelNamesWithChildren( ){
		return relNamesWithChildren;
	}
	
	/**
	 * 
	 * @param childWI
	 * @return
	 */
	public String getDepRelTypeByChildWI( int childWI ){
		childWI = childrenWordIndex.indexOf(childWI);
		return relNamesWithChildren.get(childWI);
	}
	
	/**
	 * 
	 * @param parentWI
	 * @return
	 */
	public String getDepRelTypeByParentWI( int parentWI ){
		parentWI = parentWordIndexes.indexOf(parentWI);
		return relNameWithParents.get(parentWI);
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<DepTreeNode> getParents(){
		return parents;
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Integer> getParentsWordIndexes(){
		return parentWordIndexes;
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<String> getRelNamesWithParents(){
		return relNameWithParents;
	}
	
	/**
	 * 
	 * @param child
	 * @param depRelType
	 */
	public void addChild( DepTreeNode child, String depRelType ){
	
		if ( this.childrenWordIndex.contains(child.wordIndex) )
			return;
		
		this.childrenWordIndex.add(child.wordIndex);
		this.children.add(child);
		this.relNamesWithChildren.add(depRelType);
		
		child.parentWordIndexes.add(this.wordIndex);
		child.parents.add(this);
		child.relNameWithParents.add(depRelType);
		
		populateGrandChildList(this, new ArrayList<Integer>());
		populateGrandChildListUpward(this);
	}

	/**
	 * 
	 * @param parent
	 * @param depRelType
	 */
	public void addParent( DepTreeNode parent, String depRelType ){
		
		if ( this.parentWordIndexes.contains(parent.wordIndex) )
			return;
		
		this.parentWordIndexes.add(parent.wordIndex);
		this.parents.add(parent);
		this.relNameWithParents.add(depRelType);
		
		parent.childrenWordIndex.add(this.wordIndex);
		parent.children.add(this);
		parent.relNamesWithChildren.add(depRelType);
		
		populateGrandChildList(parent, new ArrayList<Integer>());
		populateGrandChildListUpward(parent);
	}

	/**
	 *  
	 */
	public void populateGrandChildList(){
		populateGrandChildList(this, new ArrayList<Integer>());
	}
	
	/**
	 * 
	 * @param node
	 * @param listAlreadyTraversed
	 * @return
	 */
	private ArrayList<Integer> populateGrandChildList( DepTreeNode node, ArrayList<Integer> listAlreadyTraversed ){
		
		ArrayList<Integer> temp = new ArrayList<Integer>();
		listAlreadyTraversed.add(node.wordIndex);
			
		for ( int k=0; k<node.getChildrenWordIndexes().size(); k++ ){
			if ( !listAlreadyTraversed.contains(node.getChildrenWordIndexes().get(k)) ){
				temp = populateGrandChildList( node.getChildren().get(k),
						DataStrucUtility.listCopy(listAlreadyTraversed) );
				
				// if there is grand children
				if ( temp != null )
					node.addGrandChildWI(temp);
			}	
		}
		
		temp = new ArrayList<Integer>();
		
		temp.addAll(node.getChildrenWordIndexes());
		temp.addAll(node.getGrandChildrenWIs());
		
		if ( temp.size() > 0 )
			return temp;
		else
			return null;
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	private void populateGrandChildListUpward( DepTreeNode node ){
			
		for ( int k=0; k<node.getParentsWordIndexes().size(); k++ ){
			if ( !node.getChildrenWordIndexes().contains(node.getParentsWordIndexes().get(k)) 
					&& !node.getGrandChildrenWIs().contains(node.getParentsWordIndexes().get(k)) ) {
				node.getParents().get(k).addGrandChildWI(node.getChildrenWordIndexes());
				node.getParents().get(k).addGrandChildWI(node.getGrandChildrenWIs());
		
				populateGrandChildListUpward(node.getParents().get(k));
			}
		}
	}

	/**
	 * 
	 * @param child
	 * @param isAddBefore
	 */
	public void addChild( DepTreeNode child, boolean isAddBefore, String depRelType ){
		
		int index = this.childrenWordIndex.indexOf(child.wordIndex);
		if ( index >= 0 ){
			children.remove(index);
			relNamesWithChildren.remove(index);
			childrenWordIndex.remove(index);
		}
		
		index = children.size();
		if ( isAddBefore )
			index = 0;
				
		this.children.add(index, child);
		this.childrenWordIndex.add(index, child.wordIndex);
		this.relNamesWithChildren.add(index, depRelType);
		
		if ( !child.parentWordIndexes.contains(this.wordIndex) ) {
			child.relNameWithParents.add(depRelType);
			child.parents.add(this);
			child.parentWordIndexes.add(this.wordIndex);
		}
		
		populateGrandChildList( this, new ArrayList<Integer>());
		populateGrandChildListUpward(this);
	}

	
	/**
	 * 
	 * @param child
	 */
	public void removeChild( DepTreeNode child ){
		int index = this.childrenWordIndex.indexOf(child.wordIndex);
		if ( index > -1 ) {
			this.relNamesWithChildren.remove(index);
			this.childrenWordIndex.remove(index);
			this.children.remove(index);
		}
		
		this.allGrandChildrenWordIndex.removeAll(child.getChildrenWordIndexes());
		this.allGrandChildrenWordIndex.removeAll(child.getGrandChildrenWIs());
		/*
		index = child.parentWordIndexes.indexOf(this.wordIndex);
		
		if ( index > -1 ) {
			child.parentWordIndexes.remove(index);
			child.relNameWithParents.remove(index);
			child.parents.remove(index);
		}
		*/
		populateGrandChildListUpward(this);
	}
	
	/**
	 * 
	 * @param index
	 */
	public void removeChild( int index ){
		DepTreeNode child = this.children.get(index);
		removeChild(child);
	}
	
	/**
	 * 
	 * @param index
	 */
	public void removeParent( int index ){
		DepTreeNode parent = this.parents.get(index);
		removeParent(parent);
	}
	
	/**
	 * 
	 * @param parent
	 */
	public void removeParent( DepTreeNode parent ){
		int index = this.parentWordIndexes.indexOf(parent.wordIndex);
		if ( index > -1 ) {
			this.relNameWithParents.remove(index);
			this.parentWordIndexes.remove(index);
			this.parents.remove(index);
		}
		
		parent.allGrandChildrenWordIndex.removeAll(this.getChildrenWordIndexes());
		parent.allGrandChildrenWordIndex.removeAll(this.getGrandChildrenWIs());
		
		/*
		index = parent.childrenWordIndex.indexOf(this.wordIndex);
		
		if ( index > -1 ) {
			parent.childrenWordIndex.remove(index);
			parent.relNamesWithChildren.remove(index);
			parent.children.remove(index);
		}
		
		populateGrandChildList( parent, new ArrayList<Integer>());
		*/
	}
	
	
	
	/**
	 * 
	 */
	public void clearChildren() {
		this.children.clear();
		this.childrenWordIndex.clear();
		this.relNamesWithChildren.clear();
	}

	/**
	 * 
	 */
	public void clearParents(){
		this.parents = new ArrayList<DepTreeNode>();
		this.parentWordIndexes = new ArrayList<Integer>();
		this.relNameWithParents = new ArrayList<String>();
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public DepTreeNode clone()
    {
		DepTreeNode newNode = new DepTreeNode();
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
		newNode.relNamesWithChildren = (ArrayList<String>) relNamesWithChildren.clone();
		newNode.relNameWithParents = (ArrayList<String>) relNameWithParents.clone();

		// index of all lower tree nodes in the same branch
		newNode.allGrandChildrenWordIndex = (ArrayList<Integer>) allGrandChildrenWordIndex.clone();

		newNode.children = (ArrayList<DepTreeNode>) children.clone();
		newNode.parents = (ArrayList<DepTreeNode>) parents.clone();
		
		return newNode;
		/*
		 // The following code is unstable. 
        try {
            return (DepTreeNode) super.clone();
        }
        catch( CloneNotSupportedException e ) {
            return null;
        }*/
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
		
		if ( allGrandChildrenWordIndex != null )
			for( int i=0; i<allGrandChildrenWordIndex.size(); i++ )
				if ( !listOfNodes.contains(allGrandChildrenWordIndex.get(i)) )
					listOfNodes.add(allGrandChildrenWordIndex.get(i));
		
		listOfNodes = DataStrucUtility.getUniqueItems(listOfNodes);
		
		if ( listOfNodes.contains(wordIndex) )
			return listOfNodes.size();
			
		return listOfNodes.size() + 1;
	}

	/**
	 * 
	 * @return
	 */
	public DepTreeNode copyExcludingChildrenAndParents(){
		DepTreeNode node = this.clone(); 
		
		// NOTE: children are not copied
		
		node.childrenWordIndex = new ArrayList<Integer>(); 
		
		node.allGrandChildrenWordIndex = new ArrayList<Integer>();

		node.relNamesWithChildren = new ArrayList<String>();
		
		node.children = new ArrayList<DepTreeNode>(); 

		node.parents = new ArrayList<DepTreeNode>();
		node.parentWordIndexes = new ArrayList<Integer>();
		
		return node;
	}
	
	/**
	 * 
	 * @param entWIndx
	 * @param listOfVisitedNodeWIs
	 */
	public void removeChildrenNotInList ( ArrayList<Integer> entWIndx, ArrayList<Integer> listOfVisitedNodeWIs ) {
		DepTreeNode dn = this;
		listOfVisitedNodeWIs.add(dn.wordIndex);
		
		for ( int i=0; i<dn.allGrandChildrenWordIndex.size(); i++ ) {
			if ( !entWIndx.contains(dn.allGrandChildrenWordIndex.get(i)) ) {
				dn.allGrandChildrenWordIndex.remove(i);
				i--;
			}
		}
		
		for ( int i=0; i<dn.childrenWordIndex.size(); i++ ) {
			if ( !entWIndx.contains(dn.childrenWordIndex.get(i)) ) {
				dn.removeChild(i);
				i--;
			}
			else if ( listOfVisitedNodeWIs.contains(dn.childrenWordIndex.get(i)) && dn.children != null && dn.children.size() > i ) {
				listOfVisitedNodeWIs.add(dn.childrenWordIndex.get(i));
				dn.children.get(i).removeChildrenNotInList(entWIndx, listOfVisitedNodeWIs);
			}
		}
		
		if ( dn.wordIndex != listOfVisitedNodeWIs.get(0) ) {
			for ( int i=0; i<dn.parentWordIndexes.size(); i++ ) {
				if ( !entWIndx.contains(dn.parentWordIndexes.get(i)) ) {
					dn.removeParent(i);					
					i--;
				}
				else if ( listOfVisitedNodeWIs.contains(dn.parentWordIndexes.get(i)) && dn.parents != null && dn.parents.size() > i ) {
					listOfVisitedNodeWIs.add(dn.parentWordIndexes.get(i));
					dn.parents.get(i).removeChildrenNotInList(entWIndx, listOfVisitedNodeWIs);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param isTrucEtAl2009Format
	 * @param isIncludeWord
	 * @param isIncludePOS
	 * @param isIncludeRelName
	 * @param isIncludeLemma
	 * @param isIncludePOSGeneral
	 * @param isIncludePharasalCat
	 * @return
	 */
	public String printTree( boolean isTrucEtAl2009Format, boolean isIncludeWord, boolean isIncludePOS, boolean isIncludeRelName, boolean isIncludeLemma,
			boolean isIncludePOSGeneral, boolean isIncludePharasalCat){

		if ( isTrucEtAl2009Format )
			return //"(root " +
				consturctPrintTreeTrucEtAl2009(isIncludeWord, isIncludePOS, isIncludeRelName, isIncludeLemma, 
					isIncludePOSGeneral, isIncludePharasalCat)
					//+ ")"
					;
		else
			return //"(root " + 
			consturctPrintTree(isIncludeWord, isIncludePOS, isIncludeRelName, isIncludeLemma, 
				isIncludePOSGeneral, isIncludePharasalCat) 
				//+ ")"
				;
	}

	/**
	 * 
	 * @param isIncludeWord
	 * @param isIncludePOS
	 * @param isIncludeRelName
	 * @param isIncludeLemma
	 * @param isIncludePOSGeneral
	 * @param isIncludePharasalCat
	 * @return
	 */
	private String consturctPrintTreeTrucEtAl2009( boolean isIncludeWord, boolean isIncludePOS, boolean isIncludeRelName, boolean isIncludeLemma,
			boolean isIncludePOSGeneral, boolean isIncludePharasalCat){
		String str = "";
		
		// in case, the root of the subtree is the head of one of the enitites
		if ( this.parents.isEmpty() && !TextUtility.isEmptyString(this.NEcategory) )
			str += " (" + this.NEcategory;
				
		
		if ( isIncludePharasalCat )
			str += " (" + this.pharasalCat;
		
		if ( isIncludePOS )
			str += " (" + this.pos;
		
		if ( isIncludeWord )
			str += " (" + this.word.replaceAll("\\(", "<LB>").replaceAll("\\)", "<RB>"); 
		
		if ( isIncludeLemma )
			str += " (" + this.lemma;		
		
		
		if ( isIncludePOSGeneral ){
			if ( !TextUtility.isEmptyString(this.posGeneral) )
				str += " (" + this.posGeneral;
			else
				str += " (" + this.pos;				
		}
				
		if ( children != null ){
			for ( int i=0; i<children.size(); i++ ){
							
				if ( !TextUtility.isEmptyString(children.get(i).NEcategory) )
					str += " (" + children.get(i).NEcategory;
				
				if ( isIncludeRelName )
					str += " (" + relNamesWithChildren.get(i) + " "; 
				
				str += children.get(i).consturctPrintTreeTrucEtAl2009( isIncludeWord, isIncludePOS, isIncludeRelName, isIncludeLemma, isIncludePOSGeneral, isIncludePharasalCat);

				if ( isIncludeRelName )
					str += ")";				
				
				if ( !TextUtility.isEmptyString(children.get(i).NEcategory) )
					str += ")";		
			}
		}
		
		if ( isIncludePOSGeneral ){
			//if (this.posGeneral != null && !this.posGeneral.equals("") )
				str += ")";
		}
		
		if ( isIncludePharasalCat )
			str += ")";
				
		if ( isIncludeLemma )
			str += ")";
		
		if ( isIncludeWord )
			str += ")";
		
		if ( isIncludePOS )
			str += ")";
		
		if ( this.parents.isEmpty() && !TextUtility.isEmptyString(this.NEcategory) )
			str += ")";
		
		return str;
	}

	/**
	 * 
	 * @param isIncludeWord
	 * @param isIncludePOS
	 * @param isIncludeRelName
	 * @param isIncludeLemma
	 * @param isIncludePOSGeneral
	 * @param isIncludePharasalCat
	 * @return
	 */
	private String consturctPrintTree( boolean isIncludeWord, boolean isIncludePOS, boolean isIncludeRelName, boolean isIncludeLemma,
			boolean isIncludePOSGeneral, boolean isIncludePharasalCat){
		String str = "";
		
		// in case, the root of the subtree is the head of one of the enitites
		if ( this.parents.isEmpty() && !TextUtility.isEmptyString(this.NEcategory) )
			str += " (" + this.NEcategory;
				
		
		if ( isIncludePharasalCat )
			str += " (" + this.pharasalCat;
		
		if ( isIncludePOS )
			str += " (" + this.pos;
		
		if ( isIncludeWord )
			str += " (" + this.word.replaceAll("\\(", "<LB>").replaceAll("\\)", "<RB>"); 
		
		if ( isIncludeLemma )
			str += " (" + this.lemma;		
		
		
		if ( isIncludePOSGeneral ){
			if ( !TextUtility.isEmptyString(this.posGeneral) )
				str += " (" + this.posGeneral;
			else
				str += " (" + this.pos;				
		}
				
		if ( children != null ){
			for ( int i=0; i<children.size(); i++ ){
								
				if ( isIncludeRelName )
					str += " (" + relNamesWithChildren.get(i) + " "; 
				
				if ( !TextUtility.isEmptyString(children.get(i).NEcategory) )
					str += " (" + children.get(i).NEcategory;
				
				str += children.get(i).consturctPrintTree( isIncludeWord, isIncludePOS, isIncludeRelName, isIncludeLemma, isIncludePOSGeneral, isIncludePharasalCat);

				if ( !TextUtility.isEmptyString(children.get(i).NEcategory) )
					str += ")";				
				
				if ( isIncludeRelName )
					str += ")";				
						
			}
		}
		
		if ( isIncludePOSGeneral ){
			//if (this.posGeneral != null && !this.posGeneral.equals("") )
				str += ")";
		}
		
		if ( isIncludePharasalCat )
			str += ")";
				
		if ( isIncludeLemma )
			str += ")";
		
		if ( isIncludeWord )
			str += ")";
		
		if ( isIncludePOS )
			str += ")";
		
		if ( this.parents.isEmpty() && !TextUtility.isEmptyString(this.NEcategory) )
			str += ")";
		
		return str;
	}

}
