package Others;

import java.util.ArrayList;

import Structures.Entity;
import Utility.DataStrucUtility;


public class CommonExtra {

	/**
	 * 
	 * @param listEnt
	 * @param listBoundariesOfEnt
	 */
	public static void resolveOverlappingEntities( ArrayList<Entity> listEnt, boolean isRemoveOverlaps ){
				
		int countOverlaps = 1;
		// removing overlapping entities
		for ( int x=0; x < listEnt.size() - 1; x++ ) {
			for ( int y=x+1; y < listEnt.size(); y++ ){
				if ( DataStrucUtility.hasOverlap( listEnt.get(x).boundaries, listEnt.get(y).boundaries) ){
					
					if ( listEnt.get(x).endIndex - listEnt.get(x).startIndex 
					        > listEnt.get(y).endIndex - listEnt.get(y).startIndex ){
					
						if ( isRemoveOverlaps )
							listEnt.remove(y);
						else {
							listEnt.get(y).boundaries = new int[] {-1*countOverlaps,-1*countOverlaps};
						}
					}
					else {
						if ( isRemoveOverlaps )
							listEnt.remove(x);
						else
							listEnt.get(x).boundaries = new int[] {-1*countOverlaps,-1*countOverlaps};
					}
					
					x = -1;
					countOverlaps++;
					break;
				}
			}
		}
	}
	
}
