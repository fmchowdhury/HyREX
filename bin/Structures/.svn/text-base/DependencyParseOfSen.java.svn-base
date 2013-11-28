package Structures;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;


public class DependencyParseOfSen {

	public String senID = "";
	public String tokAndPos = "";
	public ArrayList<String> listOfDeps = new ArrayList<String>();
	
	/**
	 * 
	 * @param fullDataFileName
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<DependencyParseOfSen> readDepParseForAllSen ( String depParsedFileName ) throws Exception {
		
		String line = "";
		BufferedReader inputDepParse = new BufferedReader(new FileReader(new File(depParsedFileName)));
		ArrayList<DependencyParseOfSen> listDepParseOfAllSen = new ArrayList<DependencyParseOfSen>(); 
		
		/**
		 *  read dependency parsed data
		 */		
		while (( line = inputDepParse.readLine()) != null){
			
			DependencyParseOfSen objDepParseOfSen = new DependencyParseOfSen();
			
			String[] temp = line.trim().split("\\s+");
			objDepParseOfSen.senID = temp[temp.length-1];
						
			objDepParseOfSen.tokAndPos = inputDepParse.readLine().trim();
			inputDepParse.readLine();
						
			// read dependencies
			line = inputDepParse.readLine().trim();
					
			if ( line.isEmpty() )
				inputDepParse.readLine();
			while ( line != null && !(line = line.trim()).isEmpty() ){
				objDepParseOfSen.listOfDeps.add(line);
				line = inputDepParse.readLine();				
			}

			listDepParseOfAllSen.add(objDepParseOfSen);
		}
		
		inputDepParse.close();
		
		return listDepParseOfAllSen;
    }
	
	
	/**
	 * 
	 */
	public String printString () {
		StringBuilder sbTemp = new StringBuilder();
		
		sbTemp.append( "Sentence Id: " + this.senID + "\n");		
		sbTemp.append(this.tokAndPos + "\n\n");
		
		// read dependencies
		if ( this.listOfDeps.size() < 1 )
			// no dependency
			sbTemp.append("\n\n");
		else {				
			for ( int e=0; e<this.listOfDeps.size(); e++ )
				sbTemp.append( this.listOfDeps.get(e) + "\n");
			sbTemp.append("\n");
		}
		
		return sbTemp.toString();
	}
}
