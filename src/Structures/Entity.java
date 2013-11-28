package Structures;

import java.util.ArrayList;

import Utility.DataStrucUtility;


public class Entity {
	
	public String senID = "";
	public String id = "";
	public String name = "";
	public String subType = "";
	private String type = "";
	public int startIndex = -1;
	public int endIndex = -1;
	public int[] boundaries = new int[2];
	public ArrayList<Integer> clauses = new ArrayList<Integer>();
	
	private int startWordIndex = -1, endWordIndex = -1;
	
	/**
	 * 
	 * @return
	 */
	public int getStartWordIndex() {
		return startWordIndex;
	}
	
	/**
	 * 
	 * @param indx
	 */
	public void setStartWordIndex( int indx ) {
		startWordIndex = indx;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getEndWordIndex() {
		return endWordIndex;
	}
	
	/**
	 * 
	 * @param indx
	 */
	public void setEndWordIndex( int indx ) {
		endWordIndex = indx;
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Integer> getAllWordIndexes() {
		ArrayList<Integer> listTemp = new ArrayList<Integer>();
		for ( int i=this.startWordIndex; i<=this.endWordIndex; i++ )
			listTemp.add(i);
		return listTemp;
	}
	
	@SuppressWarnings("unchecked")
	public Entity clone()
    {
		Entity newNode = new Entity();

		newNode.senID = senID;
		newNode.id = id;
		newNode.name = name;
		newNode.subType = subType;
		newNode.type = type;
		newNode.startIndex = startIndex;
		newNode.endIndex = endIndex;
		newNode.boundaries = (int[]) boundaries.clone();
		newNode.clauses = (ArrayList<Integer>) clauses.clone();
		
		newNode.startWordIndex = startWordIndex;
		newNode.endWordIndex = endWordIndex;
		
		return newNode;
    }
	
	/**
	 * 
	 * @return
	 */
	public String printString() {
		StringBuilder sbTemp = new StringBuilder();
		
		sbTemp.append( this.id + " "
						+ this.startIndex + " " + this.endIndex + " " + this.name + " | ");
				
		return sbTemp.toString();
	}
	
	public Entity() {
		
	} 
	
	public String getNEcategory () {
        return type;
    }
    
    public void setNEcategory (String category) {
        type = category;           
    }

    /**
     * 
     * @param idAndBoundaries
     * @param type
     * @param name
     */
	public Entity ( String entIdAndBoundaries, String type, String name ) {
		String[] temp = entIdAndBoundaries.split("\\s+");
		
		this.id = temp[0]; // id
		this.startIndex = Integer.valueOf(temp[1]); // start index
		this.endIndex = Integer.valueOf(temp[2]); // end index
		this.type = type; // type
		this.name = name; // name
		this.boundaries = new int[] {startIndex, endIndex};
	}
	
	/**
	 * 
	 * @param type
	 * @param senIdBoundaryName
	 */
	public Entity ( String type, String senIdBoundaryName ) {
		String[] temp = senIdBoundaryName.split("\\|");
		
		this.id = null; // id
		this.senID = temp[0]; // sentence id
		this.name = temp[2]; // name
		this.type = type; // type
		
		temp = temp[1].split("\\s+");
		
		this.startIndex = Integer.valueOf(temp[0]); // start index
		this.endIndex = Integer.valueOf(temp[1]); // end index
		
		this.boundaries = new int[] {startIndex, endIndex};
	}
	
	/**
	 * 
	 * @param otherEnt
	 * @return
	 */
	public boolean hasOverlap ( Entity otherEnt ) {
		return DataStrucUtility.hasOverlap ( new int[] {startIndex, endIndex}, new int[] { otherEnt.startIndex, otherEnt.endIndex } );	
	}
}
