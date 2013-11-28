package DataProcessor;

import java.util.ArrayList;

import Utility.*;



public class MmtxDataProcessor {

	
	public static void main ( String args[]) throws Exception{
		
			ArrayList<String> listOfDocs = FileUtility.readNonEmptyFileLines("/media/Study/data/DDIExtraction2011/folds/training_docs");
			listOfDocs.addAll(FileUtility.readNonEmptyFileLines("/media/Study/data/DDIExtraction2011/folds/test_docs"));
			
			String[][] arrDocWithId = new String[listOfDocs.size()][];
			for ( int i=0; i<listOfDocs.size(); i++ ) {
				arrDocWithId[i] = listOfDocs.get(i).split("\\t+");
				arrDocWithId[i][0] = "/media/Study/data/DDIExtraction2011/DrugDDI_MTMX/" + arrDocWithId[i][0];
			}
			
			new MmtxDataProcessor().extractTags(arrDocWithId, 
			"/media/Study/data/DDIExtraction2011/folds/training_with_pos_mtmx.txt");
		}
		
		
	/**
	 * 
	 * 
	 * @param inUnifiedXmlDataFilePath
	 * @param outSenFileName
	 * @param outSenWithEntRelFileName
	 * @throws Exception
	 */
	public void extractTags( String[][] arrDocWithId, 
			String outSenWithPosFileName ) throws Exception{
		
		StringBuilder sbSen = new StringBuilder();
		
		for ( int d=0; d<arrDocWithId.length; d++ ){
			ArrayList<String> listTemp = FileUtility.readFileLines(arrDocWithId[d][0]);	
			ArrayList<String> listLines = new ArrayList<String>();
			
			for ( int i=0; i<listTemp.size(); i++ ){
				String[] temp = listTemp.get(i).replaceAll("\"<\"", "\"&lt;\"").replaceAll("\">\"", "\"&gt;\"").split(">");
				
				for ( int j=0; j<temp.length; j++ ){
					listLines.add(temp[j]);
				}
			}
			
			int j = 0;
			for ( int i=0; i<listLines.size(); i++ ){
					
				if ( listLines.get(i).toUpperCase().contains("<SENTENCE ") ){
			
					// read id
					String ID=listLines.get(i);
					//TEXT=
					j = ID.indexOf("ID=\"");
					ID = ID.substring(j+4);
					j = ID.indexOf("TEXT=");
											
					ID = ID.substring(0, j);
					j = ID.lastIndexOf("\"");
					ID = ID.substring(0, j).trim();
					
					sbSen.append("Sentence Id: " + arrDocWithId[d][1] + "." + ID + "\n" );
					
					// read tokens
					for ( i=i+1; !listLines.get(i).toUpperCase().contains("</SENTENCE"); i++ ){
												
						if ( listLines.get(i).toUpperCase().contains("<TOKEN ") ) {
					
							String str = listLines.get(i);
							
							// separating POS
							j = str.indexOf("POS=\"");
							String pos = str.substring(j+5);
							j = pos.indexOf("=\"");
												
							if  ( j > 0 )
								pos = pos.substring(0, j);
							
							j = pos.lastIndexOf("\"");
							pos = TextUtility.replaceSepcialXmlCharsWithOriginals(pos.substring(0, j)).trim();
							
							// separating WORD
							j = str.indexOf("WORD=\"");
							String word = str.substring(j+6);
							j = word.indexOf("=\"");
													
							if  ( j > 0 )
								word = word.substring(0, j);
							
							j = word.lastIndexOf("\"");
							word = TextUtility.replaceSepcialXmlCharsWithOriginals(word.substring(0, j)).trim();
							 
							sbSen.append(word + "/" + pos + " " );
						}
					}
					
					sbSen.append("\n\n");					
				}
			}
		}
		
		FileUtility.writeInFile(outSenWithPosFileName, sbSen.toString(), false);	
	}
}
