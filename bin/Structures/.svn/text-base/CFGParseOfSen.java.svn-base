package Structures;
	
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;


public class CFGParseOfSen {

	public String senID = "", tokAndPos = "";
	public String psgParse = "";
	
	/**
	 * 
	 * @param fullDataFileName
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<CFGParseOfSen> readCFGParseForAllSen ( String cfgParsedFileName ) throws Exception {
		
		String line = "";
		BufferedReader inputCfgParse = new BufferedReader(new FileReader(new File(cfgParsedFileName)));
		ArrayList<CFGParseOfSen> listCFGParseOfAllSen = new ArrayList<CFGParseOfSen>(); 
		
		/**
		 *  read CFG parsed data
		 */		
		while (( line = inputCfgParse.readLine()) != null){
			
			CFGParseOfSen objCFGParseOfSen = new CFGParseOfSen();
			
			String[] temp = line.trim().split("\\s+");
			objCFGParseOfSen.senID = temp[temp.length-1];
			
			objCFGParseOfSen.tokAndPos = inputCfgParse.readLine().trim();
			inputCfgParse.readLine();
						
			// first line of the parse
			line = inputCfgParse.readLine().trim();
					
			if ( line.isEmpty() )
				inputCfgParse.readLine();
			while ( line != null && !(line = line.trim()).isEmpty() ){
				objCFGParseOfSen.psgParse += line + " ";
				line = inputCfgParse.readLine();				
			}
					
			objCFGParseOfSen.psgParse = objCFGParseOfSen.psgParse.trim();
			listCFGParseOfAllSen.add(objCFGParseOfSen);
		}
		
		inputCfgParse.close();
		
		return listCFGParseOfAllSen;
    }
	
	
	public String printString () {
		StringBuilder sbTemp = new StringBuilder();
		
		sbTemp.append( "Sentence Id: " + this.senID + "\n");		
		sbTemp.append(this.tokAndPos + "\n\n");
		sbTemp.append(this.psgParse + "\n\n");
				
		return sbTemp.toString();
	}
	
	
	public static CFGParseOfSen getBySenId ( ArrayList<CFGParseOfSen> listCFGParseOfAllSen, String id ) {
		id = id.split("\\s+")[1];
		for ( int i=0; i<listCFGParseOfAllSen.size(); i++ )
			 if ( listCFGParseOfAllSen.get(i).senID.equals(id) )
				 return listCFGParseOfAllSen.get(i);
		
		return null;
	}
}

