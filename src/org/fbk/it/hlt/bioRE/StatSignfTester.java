package org.fbk.it.hlt.bioRE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import Utility.FileUtility;

public class StatSignfTester {

	// <senID, <"start_index end_index", truth_value (i.e., 0 or 1)>> 
	private TreeMap<Integer, TreeMap<String, Double>> mapEntityVoca = new TreeMap<Integer, TreeMap<String,Double>>();
	
	public void createStatTestInputFiles( String goldFile, String baseFile, String preferredFile, int x, int z ) throws Exception{
		for ( int i=x; i<z; i++ )
			createVocabulary(true, goldFile + i);
		
		for ( int i=x; i<z; i++ )
			createVocabulary(false, baseFile + i);
		
		for ( int i=x; i<z; i++ )
			createVocabulary(false, preferredFile + i);
		
		
		writeStatTestInputFileFromData("tmp/gold.stat.in", goldFile, x, z);
		writeStatTestInputFileFromData("tmp/base.stat.in", baseFile, x, z);
		writeStatTestInputFileFromData("tmp/preferred.stat.in", preferredFile, x, z);
	}
	
	private void createVocabulary( boolean isGoldData, String inFileName ) throws Exception{
		
		String line = "";
		double val = 0.0;
		BufferedReader input = new BufferedReader(new FileReader(new File(inFileName)));
		
		if ( isGoldData )
			val = 1.0;

		while ( (line = input.readLine()) != null){
			line = line.trim().replaceAll("\\s+", " ");
			if ( line.isEmpty() )
				break;
			
			String[] temp = line.split("\\|");
			put( Integer.valueOf(temp[0]), temp[1], val); 
		}
		
		input.close();
	}
	
	
	/**
	 * Add new entry in the vocabulary
	 * 
	 * @param senID
	 * @param entMention
	 * @param val
	 */
	private void put( int senID, String entMention, double val)
	{
		TreeMap<String, Double> tempMap = mapEntityVoca.get(senID); 
		if ( tempMap == null )
			tempMap = new TreeMap<String, Double>();
		else if ( tempMap.get(entMention) != null )
			return;
		
		tempMap.put( entMention, val);
		mapEntityVoca.put(senID, tempMap);
	}
	
	
	/**
	 * Create jSre Stat. test input file from original input data file
	 * 
	 * @param outFileName
	 * @param inFileName
	 * @throws Exception
	 */
	private void writeStatTestInputFileFromData( String outFileName, String inFileName, int x, int z ) throws Exception{
		
		String line = "";
		StringBuilder sb = new StringBuilder();
		
		ArrayList<String> listOrigInp = new ArrayList<String>();
		for ( int i=x; i<z; i++ ){
			BufferedReader input = new BufferedReader(new FileReader(new File(inFileName+i)));
			
			while ( (line = input.readLine()) != null){
				line = line.trim().replaceAll("\\s+", " ");
				if ( line.isEmpty() )
					break;
				
				String[] temp = line.split("\\|");
				listOrigInp.add( temp[0] + "|" + temp[1]);
			}
					
			input.close();
		}
		
		Iterator itSen = mapEntityVoca.keySet().iterator();
		while (itSen.hasNext()) {
			int senID = (Integer) itSen.next();
			Iterator itSenVoc = mapEntityVoca.get(senID).keySet().iterator();
			int entryIndex = 0;
			
			while (itSenVoc.hasNext()) {
				String entMention = (String) itSenVoc.next();
				
				if ( listOrigInp.contains( senID + "|" + entMention) )
					sb.append( "1.0" + "\t" + senID + "-" + entryIndex + "\n" );
				else
					sb.append( "0.0 \t" + senID + "-" + entryIndex + "\n" );
				
				entryIndex++;
			}
		}
		
		FileUtility.writeInFile(outFileName, sb.toString(), false);
	}
	
	/**
	 * Get the appropriate jSre Stat. test format for the corresponding entity mention input data file 
	 * 
	 * @param senID
	 * @param entMentionInData
	 * @param val
	 * @return
	 */
	private String getStatInput( int senID, String entMentionInData )
	{
		Iterator it = mapEntityVoca.get(senID).keySet().iterator();
		int entryIndex = 0;
		
		while (it.hasNext())
		{
			if ( entMentionInData.equals((String) it.next()) )
				break;
			
			entryIndex++;
		}
		
		return mapEntityVoca.get(senID).get(entMentionInData) + "\t" + senID + "-" + entryIndex + "\n";
	}
	
}
