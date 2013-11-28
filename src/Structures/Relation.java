package Structures;

import java.util.ArrayList;

import Utility.TextUtility;

public class Relation {

	public String id = "";
	public String type = "", subType = "";
	public boolean isBinaryRelation = true; // default
	public String arg1 = "";
	public String arg2 = "";
	public boolean isPositive = false;
	public ArrayList<String> listOfArgumentEntities = new ArrayList<String>();

	private static String defaultRelType = "interaction";
	public static boolean printRelPolarity = false;
	
	
	public Relation () {}
	
	/**
	 * 
	 * @param arg1
	 * @param arg2
	 * @param isPositive
	 * @param type
	 */
	public Relation( String arg1, String arg2, boolean isPositive, String type, String subType ){

		setRelation(arg1, arg2, isPositive, type, subType);
	}
	
	/**
	 * 
	 * @param arg1
	 * @param arg2
	 * @param isPositive
	 * @param type
	 */
	private void setRelation ( String arg1, String arg2, boolean isPositive, String type, String subType ){
	
		if ( TextUtility.isEmptyString(type) )
			type = getDefaultRelType();
		
		this.type = type;
		this.subType = subType; 
		this.arg1 = arg1; 
		this.arg2 = arg2;
		
		this.isPositive = isPositive;
	}
	
	/**
	 * 
	 * @param e1
	 * @param e2
	 * @param isPositive
	 * @param type
	 */
	public Relation ( Entity e1, Entity e2, boolean isPositive, String type, String subType ) {
		
		setRelation( e1.id, e2.id, isPositive, type, subType);
	}
	
	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Relation clone () {
		
		Relation newRel = new Relation();
		
		newRel.id = id;
		newRel.type = type;
		newRel.subType = subType;
		newRel.isBinaryRelation = isBinaryRelation;
		newRel.arg1 = arg1;
		newRel.arg2 = arg2;
		newRel.isPositive = isPositive;
		newRel.listOfArgumentEntities = (ArrayList<String>) listOfArgumentEntities.clone();
		
		return newRel; 
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getDefaultRelType() {
		return defaultRelType;
	}
	
	/**
	 * 
	 * @param type
	 */
	public static void setDefaultRelType( String type ) {
		defaultRelType = type;
	}
	
	/**
	 * 
	 * @return
	 */
	public String printString() {
		
		return this.type + " "
			+ this.arg1 + " " + this.arg2  + (TextUtility.isEmptyString(this.subType) ? "" :  (" " + this.subType)) 
			+ (printRelPolarity ? (" " + (this.isPositive ? "true" : "false")) : "");
	}
	
	
	/**
	 * 
	 * @param typeArgsPolarity
	 * @return
	 */
	public void addRelation ( String typeArgsSubTypePolarity ) {
		String[] temp = typeArgsSubTypePolarity.split("\\s+");
		
		setRelation( temp[1], temp[2], isPositive, temp[0], temp.length > 4 ? temp[3] : "");
		
		// set polarity
		if ( temp.length > 3 && temp[temp.length-1].toLowerCase().matches("(true|false)") ) {
			this.isPositive = Boolean.valueOf(temp[temp.length-1]);
			printRelPolarity = true;
		}
		else
			// NOTE: keeping compatible with old format
			this.isPositive = true;
		
		if ( this.isPositive )
			setDefaultRelType(type);
	}
}
