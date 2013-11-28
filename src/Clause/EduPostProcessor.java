package Clause;

import java.util.ArrayList;

import Utility.*;


public class EduPostProcessor {

	
	/**
	 * 
	 * @param senSegmentedFileName
	 * @param outputFile
	 * @throws Exception
	 */
	public void fixEduBoundariesOfAllSen( String senSegmentedFileName, String outputFile ) throws Exception{
			
		StringBuilder sb = new StringBuilder();		
		String[] arrSenSegmented = new ClauseAnalyser().readSegmentedData(senSegmentedFileName);
		
		for ( int i=0; i<arrSenSegmented.length; i++ ){
			String[] tempArr = arrSenSegmented[i].replaceAll("<C>", "").replaceAll("<breakWithinParens>", "").trim().split("</C>");
			
			tempArr = removeMarkerTag(tempArr);
						
			int len = tempArr.length-1;
			// we leave the last item of the array as it is empty
			for ( int k=0; k<len; k++ ){
			
				// System.out.println(i + " " + k);
									
				String[][] wp = Common.separateTokenAndPos(tempArr[k].trim(), true);
				String[][] wpNext = Common.separateTokenAndPos(tempArr[k+1].trim(), true);

				boolean isMerge = false;
				//int lenWpN = wpNext.length;
				/*
				// exception 1: ..... , when severely hampered .... 
				// exception 1: ..... , when reduced efficiency is ....
				if ( //wp[wp.length-1][0].matches("(,|:|\\?|!)") && 
						lenWpN > 1 &&
						( (//wpNext[0][1].matches("W.*") || 
								wpNext[0][0].toLowerCase().matches("(and|but|or|because|since|although|therefore|hence)")) 
								&& !wpNext[1][1].matches("(V.*|M.*|IN|TO)") 
						//|| wpNext[1][0].matches("(that|though)") 
						)
				)
				{ 
						if ( ( lenWpN > 2 && wpNext[2][1].matches("(V.*|M.*)"))
								|| ( lenWpN > 3 && wpNext[3][1].matches("(V.*|M.*)"))
								|| ( lenWpN > 4 && wpNext[4][1].matches("(V.*|M.*)"))
								|| ( lenWpN > 5 && wpNext[5][1].matches("(V.*|M.*)"))
								|| ( lenWpN > 6 && wpNext[6][1].matches("(V.*|M.*)")) 
							 )
							;
						
						isMerge = false;
				}
				//*/
				if ( wp[wp.length-1][0].matches("(\\.|;)") ) {
					isMerge = false;
					/*	
					for ( int tok=0; tok<wpNext.length; tok++ ){
						if ( wpNext[tok][1].matches("(V.*|M.*)")  ) {
							isMerge = false;
							break;
						}						
					}
					
					if ( !isMerge ) {
						isMerge = true;
						for ( int tok=0; tok<wp.length; tok++ ){
							if ( wp[tok][1].matches("(V.*|M.*)")  ) {
								isMerge = false;
								break;
							}						
						}
					}				*/	
				}
				//-- last word of the 1st clause has pos VB*
				//--	not (1st word of the 2nd clause has pos W* or CC)
				
				if ( wpNext[0][1].matches("(TO|IN|WRB|RB)") || wp[0][1].matches("(TO|IN|WRB|RB)") )
					isMerge = true;
				
				if ( (wpNext[1][1].matches("(VB[DPNGZ]|IN|RB)")   
						&& wpNext[0][1].matches("(CC|WDT)"))
						|| (wp[1][1].matches("(VB[DPNGZ]|IN|RB)")   
						&& wp[0][1].matches("(CC|WDT)"))						
				)
					isMerge = true;
				
				if ( wpNext[0][1].matches("(VB[DPNGZ]|IN)") || wp[0][1].matches("(VB[DPNGZ]|IN)") )
					isMerge = true;
			
				
				if ( wp[wp.length-1][1].matches(":") )
						isMerge = true;
				/*
				
				if ( wp[wp.length-1][1].matches("VB[DPZ]")   
						&& !wpNext[0][1].matches("(CC|IN|W.*)") )
					isMerge = true;
				/*
				else if ( wp.length > 1 &&  wp[wp.length-2][1].equals("NN")  
							&& wp[wp.length-1][0].equals(":")
							&& wpNext[0][1].equals("NN") )
					isMerge = true;
				//*/
				if ( isMerge ) {
					//System.out.println(tempArr[k].trim() + "\n\n");
					//System.out.println(tempArr[k+1].trim() + "\n\n------\n");
					
					
					tempArr[k] = tempArr[k].trim() + " " + tempArr[k+1].trim();
				
					for ( int x=k+1; x<len; x++ )
						tempArr[x] = tempArr[x+1];
					len--;
					k--;
				}
			}
			
			sb.append("<S>\n");
			for ( int k=0; k<len+1; k++ )
				sb.append("\n<C>").append(tempArr[k].trim()).append(" </C>");
			
			sb.append("\n</S>\n");
		}
		
		FileUtility.writeInFile(outputFile, sb.toString(), false);
	}
	
	
	/**
	 * Remove <M> ... </M> portion
	 * @param text
	 * @return
	 */
	private String[] removeMarkerTag( String[] text ) {
		//-- remove <M> ... </M> portion
		 		
		boolean isM = false;
		for ( int i=0; i<text.length; i++ ) {
			int mStart = text[i].indexOf("<M>");
			
			while ( mStart >= 0 ) {
				int mEnd = text[i].indexOf("</M>", mStart + 3);
				
				if ( mEnd < mStart )
					break;
				
				String str = text[i].substring(mStart + 3, mEnd )
						.replaceAll(" ,/,", "");
				
				for ( int k=0; k<text.length; k++ ) {
					if ( k != i ) {
						if ( text[k].contains(str) ) {
							//System.out.println(i + " - " + k);							
							//		System.out.println(text[k].trim() + "\n\n");
						//	System.out.println(text[i].trim() + "\n------\n");
							text[k] = text[k].replace(str, "");
							str = text[i].substring(text[i].indexOf("</M>")+4);
							text[i] = text[i].substring(0, mStart) + text[i].substring(mStart + 3, mEnd ) + str;
										
							if ( k-i == 1 ) {
								text[i] = text[i] + " " + text[k];
								text[k] = "";
								isM = true;
							}
							
							break;
						}						
					}
				}
				
				if ( text[i].length() > mStart + 1 ) 
					mStart = text[i].indexOf("<M>", mStart + 1);
				else 
					break;
			}
		}
		
		if ( isM ) {
			ArrayList<String> tempLines = new ArrayList<String>();
			for ( int k=0; k<text.length; k++ ) {
				if ( text[k].length() > 0 )
					tempLines.add(text[k]);
			}
		
			text = DataStrucUtility.listToStringArray(tempLines);
		}
		
		return text;
	}
}
