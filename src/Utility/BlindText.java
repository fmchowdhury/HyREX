package Utility;

import java.util.ArrayList;

import Others.CommonExtra;
import Structures.*;


public class BlindText {
/*
	public static void main( String[] args) throws Exception {

		String corp = "aimed";
		String fullDataFileName = "../../data/converted_PPI_corpora/" + corp + "/" + corp + ".full", 
			senFileName = "../../data/converted_PPI_corpora/" + corp + "/" + corp + ".sen";
		
		blindEntitiesInSentences(fullDataFileName, senFileName);
	}
	*/
	
	public static String blindPrefix = "Entity", blindSuffix = "Xxx";
	
	/**
	 * 
	 * @param fullDataFileName
	 * @param senFileName
	 * @throws Exception
	 */
	public static void blindEntitiesInSentences ( String fullDataFileName, String senFileName ) throws Exception {
		
		StringBuilder sbFull = new StringBuilder(), sbSen = new StringBuilder();
		//blindPrefix = blindPrefix.toUpperCase();
		//blindSuffix = blindSuffix.toUpperCase();
		
		ArrayList<Sentence> listSentence = Sentence.readFullData(fullDataFileName, "", "", "", false);
		
		for ( int s=0; s<listSentence.size(); s++ ) {
			
			Sentence objCurSen = blindEntities(listSentence.get(s));
			sbFull.append(objCurSen.printString());
			sbSen.append(objCurSen.text + "\n\n");
		}
		
		FileUtility.writeInFile( fullDataFileName, sbFull.toString(), false);
		FileUtility.writeInFile(senFileName, sbSen.toString(), false);
	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	private static Sentence blindEntities( Sentence objCurSen) throws Exception {
	
		CommonExtra.resolveOverlappingEntities(objCurSen.listOfEntities, true);
		
		//System.out.println(objCurSen.senID);
		
		// sort entities by their end index
		for ( int e=0; e<objCurSen.listOfEntities.size()-1; e++ ) {
			for ( int d=e+1; d<objCurSen.listOfEntities.size(); d++ ) {
				if ( objCurSen.listOfEntities.get(e).endIndex > objCurSen.listOfEntities.get(d).endIndex ) {
					
					Entity et = objCurSen.listOfEntities.get(e);
					objCurSen.listOfEntities.set(e, objCurSen.listOfEntities.get(d));
					objCurSen.listOfEntities.set(d, et);
				}
			}
		}
	
		ArrayList<String> listOfEntNames = new ArrayList<String>();
		for ( int e=0; e<objCurSen.listOfEntities.size(); e++ )
			listOfEntNames.add(objCurSen.listOfEntities.get(e).name);
		
		for ( int e=objCurSen.listOfEntities.size()-1; e>=0; e-- ) {
			
			int curInd = -1;
			objCurSen.text = objCurSen.text.replaceAll("\\s+", " ");
			StringBuilder newText = new StringBuilder();
			boolean isFound = false;
			for ( int i=0; i<objCurSen.text.length(); i++ ) {
		
				// counting the non-space characters
				if ( objCurSen.text.charAt(i) != ' ' )
					curInd++;
				
				// if the current character belong to an entity which is not blinded yet
				if ( curInd >= objCurSen.listOfEntities.get(e).startIndex 
						&& curInd <= objCurSen.listOfEntities.get(e).endIndex && !isFound ) {
					
					isFound = true;
					i += objCurSen.listOfEntities.get(e).name.length();
					
					// The following lines make sure that clue of the entities having same name is retained.
					int firstOcInd = listOfEntNames.indexOf(listOfEntNames.get(e));					
					//int firstOcInd = e;
					newText.append( blindPrefix + firstOcInd + blindSuffix);
					objCurSen.listOfEntities.get(e).name = blindPrefix + firstOcInd + blindSuffix;
					
					newText.append(objCurSen.text.substring(i));
					break;
				}
				else if ( curInd > objCurSen.listOfEntities.get(e).endIndex ) {
					newText.append(objCurSen.text.substring(i));
					break;
				}
				else
					newText.append(objCurSen.text.charAt(i));
			}
			
			objCurSen.text = newText.toString().replaceAll("\\s+", " ");
		}
		
		String[] textSplitByEnt = objCurSen.text.split(blindPrefix + "[0-9]+" + blindSuffix); 
		
		String tmpText = "";
		// update entity word boundaries
		for ( int e=0; e<objCurSen.listOfEntities.size(); e++ ) {
			System.out.println(objCurSen.listOfEntities.get(e).id);
			
			tmpText = tmpText + textSplitByEnt[e].replaceAll("\\s+", "");
			objCurSen.listOfEntities.get(e).startIndex = tmpText.length();
			tmpText = tmpText + objCurSen.listOfEntities.get(e).name; 
			objCurSen.listOfEntities.get(e).endIndex = tmpText.length() -1;
		}
		
		
		// remove relations that has removed entities
		for ( int r=0; r<objCurSen.listRels.size(); r++ ) {
			
			int tot = 0;
			
			for ( int e=0; tot<2 && e<objCurSen.listOfEntities.size(); e++ ) {
				if ( objCurSen.listRels.get(r).printString().contains(objCurSen.listOfEntities.get(e).id + " ") )
					tot++;
			}
			
			if ( tot < 2 ) {
				objCurSen.listRels.remove(r);
				r--;
			}
		}
		
		return objCurSen;
	}
	
}
