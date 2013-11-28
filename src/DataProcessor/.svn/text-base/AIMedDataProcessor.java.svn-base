package DataProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import Utility.*;


public class AIMedDataProcessor {

	
	public void processData () throws Exception {
/*
		new UnifiedFormatDataProcessor().extractAndMergeText("/media/Study/data/AImed/aimed.xml", 
				"/media/Study/data/AImed/aimed.txt",
				"/media/Study/data/AImed/aimedFull.txt");
		new AIMedDataProcessor().addAbstractIdWtihData("/media/Study/data/AImed/aimedFull.txt",
				"/media/Study/data/AImed/orig/corpus");
		
		/**
		 * -- NOTE: This line is required as we need to indicate the parser that a full stop
		 * at the end of sentence is not part of an abbreviation.
		 */
		
		CommonUtility.addExtraSpaceBeforeSenEnding("/media/Study/data/AImed/aimed.txt");
	}
	
	/**
	 * 
	 * @param inSenWithEntRelFileName
	 * @param abstractFileFolder
	 * @throws Exception
	 */
	public void addAbstractIdWtihData( String inSenWithEntRelFileName, String abstractFileFolder ) throws Exception{
		
		ArrayList<String[]> listFolderFirstLine = readAllAbtsractFirstLine(abstractFileFolder);
		
		ArrayList<String> listLines = FileUtility.readFileLines(inSenWithEntRelFileName);	
		StringBuilder sbIdSenEntityRel = new StringBuilder();
		String abstractId = "", docId = "NONE";
		
		for ( int i=0; i<listLines.size()-1; i++ ){
			
			if ( listLines.get(i).contains("Sentence Id:") ) {
					
				if ( !listLines.get(i).contains(docId) ) {
					int t = listLines.get(i+1).indexOf(".");
					for ( int a=0; a<listFolderFirstLine.size(); a++ ){
						if ( listFolderFirstLine.get(a)[1].contains(
								listLines.get(i+1).substring(0,
										 t >= 0 ? t : listLines.get(i+1).length())
								.replaceAll("\\s+", "")
								) ){
							abstractId = listFolderFirstLine.get(a)[0];
							docId = listLines.get(i).substring(0, listLines.get(i).lastIndexOf("."))
										.replace("Sentence Id:", "").trim();
							break;
						}
					}
				}

				sbIdSenEntityRel.append("Abstract Id: " + abstractId + "\n");
				sbIdSenEntityRel.append(listLines.get(i) + "\n");
				i++;
			}
								
			sbIdSenEntityRel.append(listLines.get(i) + "\n");
		}
				
		FileUtility.writeInFile(inSenWithEntRelFileName, sbIdSenEntityRel.toString(), false);	
	}
	
	
	/**
	 * 
	 * @param abstractFileFolder
	 * @return
	 * @throws Exception
	 */
	private ArrayList<String[]> readAllAbtsractFirstLine( String abstractFileFolder ) throws Exception{
		String[] arrFolderFiles = FileUtility.getFileNamesFromDir(abstractFileFolder);
		ArrayList<String[]> listFolderFirstLine = new ArrayList<String[]>();
		String line = null;
		
		for ( int i=0; i<arrFolderFiles.length; i++ ){
			BufferedReader input = new BufferedReader(new FileReader(new File(abstractFileFolder 
					+ "/" + arrFolderFiles[i])));
			
			while (( line = input.readLine()) != null && !(line = line.trim()).isEmpty() ){
				line = line.replaceAll("\\s+", "");
				String str = "";
				boolean isTag = false;
				for ( int k=0; k<line.length(); k++ ){
					if ( line.charAt(k) == '<' )
						isTag = true;
					else if ( line.charAt(k) == '>' )
						isTag = false;
					else if ( !isTag )
						str += line.charAt(k);					
				}
						
				listFolderFirstLine.add(new String[]{arrFolderFiles[i], TextUtility.replaceSepcialXmlCharsWithOriginals(str)});
				break;
			}
			
			input.close();
		}
		
		return listFolderFirstLine;
	}
}
