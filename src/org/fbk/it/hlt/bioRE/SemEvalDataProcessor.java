package org.fbk.it.hlt.bioRE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import Utility.FileUtility;

public class SemEvalDataProcessor {

	public static void main ( String[] args ) throws Exception{
	
		new SemEvalDataProcessor().
			//removeDefaultRelationsFromAnswerKey("/media/Study/workspace/BioRelEx/evaluate/TEST_ANSWER.TXT");
		
		//removeDefaultRelationsFromData("/media/Study/workspace/BioRelEx/others/preprocessedTest.txt", 
			//	"/media/Study/workspace/BioRelEx/others/TEST_FILE_FULL.TXT");
		
		removeDefaultRelationsFromData("/media/Study/workspace/BioRelEx/others/preprocessedTrain.TD4.txt", 
					"/media/Study/workspace/BioRelEx/others/TD4.txt");
	}
	
	
	public void removeDefaultRelationsFromData( String tokenizedFileName, String senWithRelFileName ) throws Exception{
		
		BufferedReader inputTokens =  new BufferedReader(new FileReader(new File(tokenizedFileName))),
			inputSenWithRel =  new BufferedReader(new FileReader(new File(senWithRelFileName)));
		
		StringBuilder sb = new StringBuilder();
		
	    String line = null, lineSen = null;
	    
	    while (( line = inputSenWithRel.readLine()) != null) {
	    
	    	String senID = line.trim().split("\\s+")[0];
	    	if ( senID.matches("[0-9]+") ){
	    		System.out.println(senID);
	    		if ( !(line = inputSenWithRel.readLine()).contains("Other") ){
	    	    			
	    			while ( (lineSen != null && lineSen.contains("Sentence"))
	    					||  ( lineSen = inputTokens.readLine()) != null) {
	    		    	if ( lineSen.replaceAll("\\s+", "").contains("Sentence:" + senID) ){
	    		    		sb.append(lineSen + "\n");
	    		    		
	    		    		while ( (lineSen = inputTokens.readLine()) != null 
	    		    				&& !lineSen.contains("Sentence") ){
	    		    			sb.append(lineSen + "\n");
	    		    		}
	    		    		
	    		    		break;
	    		    	}
	    		    	else
	    		    		lineSen = "";
	    			}
	    		}
	    		
	    	}
	    }
	    	
	    inputSenWithRel.close();
	    inputTokens.close();
	    
	    FileUtility.writeInFile(tokenizedFileName + "_without_other", sb.toString(), false);
	}

	public void removeDefaultRelationsFromAnswerKey( String inFileName ) throws Exception{
		
		BufferedReader input =  new BufferedReader(new FileReader(new File(inFileName)));
		
		StringBuilder sb = new StringBuilder();
		
	    String line = null;
	    
	    while (( line = input.readLine()) != null) {
	    
	    	if ( !line.contains("Other") )	    	    		
	    		sb.append(line + "\n");
	    }
	    	
	    input.close();
	    
	    FileUtility.writeInFile(inFileName + "_without_other", sb.toString(), false);
	}
	
	
	
	public String[] createBalancedCorpusByElimination( String tokenizedFileName, String[][] relTypes ) throws Exception{

		//-- separate sre input lines by relations
		ArrayList<ArrayList<String>> listDataByRel = new ArrayList<ArrayList<String>>();
		
		for ( int i=0; i<relTypes.length; i++ )
			listDataByRel.add(new ArrayList<String>());
		
		BufferedReader input = new BufferedReader(new FileReader(new File(tokenizedFileName)));
		String line = null;
		String tokenLines = "";
		
		int ln = 0;
		// create input file in jSre format
		do{
			tokenLines = "";
			
			// read token lines of the next sentence
			while ( (line = input.readLine()) != null){
				line = line.trim();
				
				if ( line.isEmpty() ){
					tokenLines = tokenLines + "\n";
					break;
				}
				
				tokenLines = tokenLines + "\n" + line;
			}
			
			if ( line == null )
				break;
			
			// read relation type
			line = input.readLine();
			tokenLines = tokenLines + "\n" + line + "\n" ;
			String relation = line.replaceAll("\\s+", "");
			
			int re = 0;
			boolean isRelTypeMatched = false;
			for ( ; re < relTypes.length && !isRelTypeMatched; re++ ){
				isRelTypeMatched = false;
				for ( int rti=0; rti<relTypes[re].length && !isRelTypeMatched; rti++ )
					if ( relation.contains(relTypes[re][rti]) ){
						isRelTypeMatched = true;
						break;
					}
				
				if ( isRelTypeMatched )
					break;			
			}
				
			line = input.readLine();
			if ( line != null )
				tokenLines = tokenLines + line;
			
			listDataByRel.get(re).add(tokenLines);
			ln++;
						
		}while ( line != null);
		    
		input.close();

		int minLen = 100000;
		
		for ( int i=0; i<listDataByRel.size(); i++ )
			if ( listDataByRel.get(i).size() < minLen )
				minLen = listDataByRel.get(i).size();
			
		String[] arrTrainInpByRel = new String[relTypes.length];
			
		for ( int i=0; i<relTypes.length; i++ ){
				arrTrainInpByRel[i] = "";
		
			for ( int k=0; k<minLen; k++ )
				arrTrainInpByRel[i] = arrTrainInpByRel[i] + listDataByRel.get(i).get(k);				
		}
			
		//-- write sre input files separated by relations 
		String[] sreFiles = new String[relTypes.length];
		for ( int i=0; i<relTypes.length; i++ ){
			sreFiles[i] = "tmp/" + relTypes[i][0] + "_orig.sre";
			
			FileUtility.writeInFile(sreFiles[i], arrTrainInpByRel[i].trim(), false);
		}
		

		return sreFiles;
	}

	
	public String[] createBalancedCorpusByReplication( String tokenizedFileName, String[][] relTypes ) throws Exception{

		//-- separate sre input lines by relations
		ArrayList<ArrayList<String>> listDataByRel = new ArrayList<ArrayList<String>>();
		
		for ( int i=0; i<relTypes.length; i++ )
			listDataByRel.add(new ArrayList<String>());
		
		BufferedReader input = new BufferedReader(new FileReader(new File(tokenizedFileName)));
		String line = null;
		String tokenLines = "";
		
		int ln = 0;
		// create input file in jSre format
		do{
			tokenLines = "";
			
			// read token lines of the next sentence
			while ( (line = input.readLine()) != null){
				line = line.trim();
				
				if ( line.isEmpty() ){
					tokenLines = tokenLines + "\n";
					break;
				}
				
				tokenLines = tokenLines + "\n" + line;
			}
			
			if ( line == null )
				break;
			
			// read relation type
			line = input.readLine();
			tokenLines = tokenLines + "\n" + line + "\n" ;
			String relation = line.replaceAll("\\s+", "");
			
			int re = 0;
			boolean isRelTypeMatched = false;
			for ( ; re < relTypes.length && !isRelTypeMatched; re++ ){
				isRelTypeMatched = false;
				for ( int rti=0; rti<relTypes[re].length && !isRelTypeMatched; rti++ )
					if ( relation.contains(relTypes[re][rti]) ){
						isRelTypeMatched = true;
						break;
					}
				
				if ( isRelTypeMatched )
					break;			
			}
				
			line = input.readLine();
			if ( line != null )
				tokenLines = tokenLines + line;
			
			listDataByRel.get(re).add(tokenLines);
			ln++;
						
		}while ( line != null);
		    
		input.close();

		int maxLen = 0;
		
		for ( int i=0; i<listDataByRel.size(); i++ )
			if ( listDataByRel.get(i).size() > maxLen )
				maxLen = listDataByRel.get(i).size();
			
		String[] arrTrainInpByRel = new String[relTypes.length];
		Random randomGenerator = new Random();
		 
		for ( int i=0; i<relTypes.length; i++ ){
				arrTrainInpByRel[i] = "";
		
			if ( listDataByRel.get(i).size() < maxLen ){
				int x = maxLen - listDataByRel.get(i).size(), orig = listDataByRel.get(i).size();
				
				for ( int d=0; d<x; d++ ){
					int randomInt = randomGenerator.nextInt(orig);
					listDataByRel.get(i).add(listDataByRel.get(i).get(randomInt));
				}
			}
				
			for ( int k=0; k<maxLen; k++ )
				arrTrainInpByRel[i] = arrTrainInpByRel[i] + listDataByRel.get(i).get(k);				
		}
			
		//-- write sre input files separated by relations 
		String[] sreFiles = new String[relTypes.length];
		for ( int i=0; i<relTypes.length; i++ ){
			sreFiles[i] = "tmp/" + relTypes[i][0] + "_orig.sre";
			
			FileUtility.writeInFile(sreFiles[i], arrTrainInpByRel[i].trim(), false);
		}
		

		return sreFiles;
	}
}
