package org.fbk.it.hlt.bioRE.multiStage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import Utility.FileUtility;

public class MultiStageRE {

	static int totalRel = 0;
	static ArrayList<Integer> listMappedOrigSenIdWithSysGenSenId = null;
	static String evalResultFileName = "tmp/evalResult";

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
	
		/*
		new GroupByIntuition().trainAndPredict("/media/Study/workspace/BioRelEx/others/preprocessedTrain.TD4.txt_without_other",
				"SL", "/media/Study/workspace/BioRelEx/others/preprocessedTest.txt_without_other");
		*/
		new GroupByDifficulty().trainAndPredict("/media/Study/workspace/BioRelEx/others/preprocessedTrain.TD4.txt_without_other", 
				relTypesCoarseSortedByDifficulty, 
				"SL", "/media/Study/workspace/BioRelEx/others/preprocessedTest.txt_without_other", 1);
		
	}
	
	/* split tokenized training data by relations
	 * Note: this method should get as parameters relTypes[n][1] where n is the total no. of
	 * individual relations.
	 */
	
	protected String[] separateTokenizedTrainingDataByRel ( String mainFile, String[][] relTypes ) throws Exception{

		//-- separate sre input lines by relations
		String[] arrTrainInpByRel = new String[relTypes.length];
		
		for ( int i=0; i<relTypes.length; i++ )
			arrTrainInpByRel[i] = "";
		
		BufferedReader input = new BufferedReader(new FileReader(new File(mainFile)));
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
			
			arrTrainInpByRel[re] = arrTrainInpByRel[re] + tokenLines;
			ln++;
						
		}while ( line != null);
		    
		input.close();

		//-- write sre input files separated by relations 
		String[] sreFiles = new String[relTypes.length];
		for ( int i=0; i<relTypes.length; i++ ){
			sreFiles[i] = "tmp/" + relTypes[i][0] + "_orig.sre";
			
			FileUtility.writeInFile(sreFiles[i], arrTrainInpByRel[i].trim(), false);
		}
		
		return sreFiles;
	}
	
	public int populateClassLabels ( String[][] relTypes, int[] relClassLabels, int laeblStartIndx, String DEFAULT_REL ){
		int defaultClassLabel = 0;
		for ( int i=0; i<relClassLabels.length; i++, laeblStartIndx++ ){
			relClassLabels[i] = laeblStartIndx;
			for ( int j=0; j<relTypes[i].length && defaultClassLabel < 1; j++ )
				if ( relTypes[i][j].equals(DEFAULT_REL) ){
					defaultClassLabel = i;
					break;
				}				
		}
		
		return defaultClassLabel;
	}
	
	
	public String addMultipleFileContents( String[] fileNames, int startFileIndex){
		StringBuilder sb = new StringBuilder();
		
		for ( int i=startFileIndex; i<fileNames.length; i++ ){
			if ( i!=startFileIndex )
				sb.append("\n");
			sb.append(FileUtility.readFileContents(fileNames[i]));
		}
		
		return sb.toString();
	}
	
	
	public String addMultipleFileContents( String[] fileNames, ArrayList<String> relNames){
		StringBuilder sb = new StringBuilder();
		
		for ( int i=0; i<fileNames.length; i++ ){
			for ( int j=0; j<relNames.size(); j++ )
				if ( fileNames[i].contains(relNames.get(j)) ){
					if ( i!=0 )
						sb.append("\n");
			
					sb.append(FileUtility.readFileContents(fileNames[i]));
					break;
				}
		}
		
		return sb.toString();
	}
	
	
	/**
	 * Remember: system generated ids start from 1.
	 * 
	 * @param tokenizedFile
	 * @throws Exception
	 */
	protected void mapOrigSenIdWithSysGenSenId ( String tokenizedFile ) throws Exception{
		BufferedReader input =  new BufferedReader(new FileReader(new File(tokenizedFile)));  
	    String line = null;
	    listMappedOrigSenIdWithSysGenSenId = new ArrayList<Integer>();  
	    listMappedOrigSenIdWithSysGenSenId.add(-1);
	    
	    while (( line = input.readLine()) != null)
	    	if ( line.contains("Sentence:") )
	    		listMappedOrigSenIdWithSysGenSenId.add(Integer.valueOf(line.replace("Sentence:", "").trim()));
	    
	    input.close();
	}
	
	
	/**
	 * 
	 * @param origOuputFile
	 * @param formattedOutputFile
	 * @param isAppend
	 * @param testSreFile
	 * @param coarseRelIndex
	 * @throws Exception
	 */
	protected void writeSemevalFormatOutput( String origOuputFile, 
			String formattedFinalOutputFile, boolean isAppend,
			 String testSreFile, String relType, boolean isConsiderDirection) throws Exception{
		
		BufferedReader input = null, inputSre =  new BufferedReader(new FileReader(new File(testSreFile)));
	    String line = null, senId = null, contents = "", relClass = null;
	    
	    if ( isConsiderDirection )
	    	input =  new BufferedReader(new FileReader(new File(origOuputFile)));
	    
	    if ( relType.equals(MultiStageRE.DEFAULT_REL) )
	    	relType = "";
	    
	    while (( line = inputSre.readLine()) != null){
	    	senId = line.split("\\t")[1];
	    	int cl = 0;
	    	
	    	if ( isConsiderDirection ){
	    		line = input.readLine();
	    		cl = (int)(Double.valueOf(line)/1);
	    	}
	 
	    	// TODO:
	    	if ( relType.isEmpty() )
	    		relClass = MultiStageRE.DEFAULT_REL + "\n";
	    	else if ( cl == 1 )
	    		relClass = relType + "(e2,e1)\n";
	    	else
	    		relClass = relType + "(e1,e2)\n";
	    	
	    	contents = contents + listMappedOrigSenIdWithSysGenSenId.get(Integer.valueOf(senId)) + "\t" + relClass;
	    }
	    
	    if ( isConsiderDirection )
		    input.close();
	    
	    inputSre.close();
	    
	    FileUtility.writeInFile(formattedFinalOutputFile, contents, isAppend);
	}
	
	/*
	/**
	 * 
	 * @param formattedOutputFile
	 * @param testSreFile
	 * @throws Exception
	 * /
	private static void addOtherRelToSemevalFormatOutput( String formattedOutputFile, String testSreFile) throws Exception{
		
		BufferedReader inputSre =  new BufferedReader(new FileReader(new File(testSreFile)));
	    String senId = null, contents = "";
	    
	    while (( senId = inputSre.readLine()) != null){
	    	senId = senId.split("\\t")[1];	    	
	    	// TODO:
	    	contents = contents + listMappedOrigSenIdWithSysGenSenId.get(Integer.valueOf(senId)) + "\t" + 
	    	relTypesCoarseSortedByDifficulty[0] + "\n"; 
	    }
	    
	    inputSre.close();
	    
	    Utility.writeInFile(formattedOutputFile, contents, true);
	}
	*/
	
	/**
	 * 
	 * @param relTypesCoarse
	 * @param sreInpForAllRel
	 * @param outputFileSuf
	 * @param predTestOutput
	 * @return
	 * @throws Exception
	 */
	protected String[] separateSrePredOfTestInputByRel( String[][] relTypes, String sreInpForAllRel, 
			String outputFileSuf, String predTestOutput, int offset ) throws Exception{
		
		int senId = 1;
		
		//-- separate senIds of sre input lines by relations
		ArrayList<ArrayList<Integer>> listSrePredTestInpByRel = new ArrayList<ArrayList<Integer>>();
					
		for ( int i=0; i<relTypes.length; i++ )
			listSrePredTestInpByRel.add(new ArrayList<Integer>());
				
		String[] arrSrePredForAllRel = predTestOutput.split("\n");
		
		for ( int s=0; s<arrSrePredForAllRel.length; s++ ){
			System.out.println(s);
			arrSrePredForAllRel[s] = arrSrePredForAllRel[s].trim();
			if ( !arrSrePredForAllRel[s].isEmpty() ){
				int index = (int)(Double.valueOf(arrSrePredForAllRel[s])/1) - 1; 
				listSrePredTestInpByRel.get(index-offset).add(senId-1);	
				senId++; 
			}
		}

		//-- separate sre input lines by relations
		String[] arrSreInpForAllRel = sreInpForAllRel.split("\n");
		String[] arrSreInpByRel = new String[relTypes.length];
		
		for ( int i=0; i<relTypes.length; i++ )
			arrSreInpByRel[i] = "";
		
		for ( int x=0; x<listSrePredTestInpByRel.size(); x++ ){
			for ( int z=0; z<listSrePredTestInpByRel.get(x).size(); z++ ){
					
				arrSreInpByRel[x] = arrSreInpByRel[x].concat(
						arrSreInpForAllRel[listSrePredTestInpByRel.get(x).get(z)]).concat("\n"); 
			}
		}
		
		//-- write sre input files separated by relations 
		String[] sreFiles = new String[relTypes.length];
		for ( int i=0; i<relTypes.length; i++ ){
			sreFiles[i] = "tmp/" + relTypes[i][0] + outputFileSuf + ".sre";
			
			FileUtility.writeInFile(sreFiles[i], arrSreInpByRel[i], false);
		}
		
		return sreFiles;
	}
		
	/*
	/**
	 * This method converts pos-tagged original training data into jSre input format.
	 * Then, the converted data is splitted into separate training data files based on the
	 * relations and returned
	 * 
	 * @param mainFile
	 * @param relTypesFine
	 * @param relTypesCoarse
	 * @param isConsiderDirection
	 * @param sreInpForAllRel
	 * @return
	 * @throws Exception
	 * /
	private String[] createSeparateSreInputByRel ( String mainFile, String[][] relTypesFine, 
			String[][] relTypesCoarse, boolean isConsiderDirection ) throws Exception{
			
		//-- create sre format of the original file with all relations with directions  
		FormatConverterJSRE clsFormatConverterJSRE = new FormatConverterJSRE();
		int[] relClassLabels = new int[relTypesFine.length];
		int defaultClassLabel = populateClassLabels(relTypesFine, relClassLabels, 1, MultiStageRE.DEFAULT_REL);
		
		String sreInpForAllRel = clsFormatConverterJSRE.posTaggedToSreFormatConverter(mainFile, 
				relTypesFine, isConsiderDirection, relClassLabels, defaultClassLabel);
		
		return separateSreInputByRel(sreInpForAllRel, relTypesCoarse, "_only");
	}
	
	
	/**
	 * This method separate training data of jSre input sentences by relations, save them in
	 * separate files return the file names in array. The last entry of the array is the 
	 * file name which contains all input sentences for all relations.
	 * 	
	 * @param sreInpForAllRel
	 * @param relTypesCoarse
	 * @param outputFileSuffix
	 * @return
	 * @throws Exception
	 * /
	private String[] separateSreInputByRel( String sreInpForAllRel, 
			String[][] relTypesCoarse, String outputFileSuffix ) throws Exception{
		
		//-- separate sre input lines by relations
		String[] arrSreInpByRel = new String[relTypesCoarse.length];
		
		for ( int i=0; i<relTypesCoarse.length; i++ )
			arrSreInpByRel[i] = "";
		
		String[] arrSreInpForAllRel = sreInpForAllRel.split("\n");
		
		for ( int s=0; s<arrSreInpForAllRel.length; s++ ){
			
			arrSreInpForAllRel[s] = arrSreInpForAllRel[s].trim();
			if ( !arrSreInpForAllRel[s].isEmpty() ){
				String[] temp = arrSreInpForAllRel[s].split("\t");
				int index = (int)(Double.valueOf(temp[0])/1);
				arrSreInpForAllRel[s] = (index%2) + "\t" + temp[1] + "\t" + temp[2]; 
				index = (index + 1)/2;
				
				// i%2 == 1 means <e1,e2> otherwise <e2,e1>
				arrSreInpByRel[index] = arrSreInpByRel[index].concat(arrSreInpForAllRel[s]).concat("\n"); 
			}
		}

		//-- write sre input files separated by relations 
		String[] sreFiles = new String[relTypesCoarse.length+1];
		for ( int i=1; i<relTypesCoarse.length; i++ ){
			sreFiles[i] = "tmp/" + relTypesCoarse[i][0] + outputFileSuffix + ".sre";
			
			Utility.writeInFile(sreFiles[i], arrSreInpByRel[i], false);
		}
		
		sreFiles[relTypesCoarse.length] = "tmp/coarse.sre";
		
		Utility.writeInFile(sreFiles[relTypesCoarse.length], sreInpForAllRel, false);
		
		return sreFiles;
	}
	*/
	
	static String DEFAULT_REL = "Other"; 

	public static String[][] relTypesCoarseSortedByDifficulty = new String[][]{
		
		// according to 10-fold cross validation of the whole
		/* 
		 {"Other"},
		 {"Entity-Destination"}, 
		 {"Cause-Effect"},
		 {"Member-Collection"},
		 {"Content-Container"}, 
		 {"Entity-Origin"},
		 {"Message-Topic"},
		 {"Component-Whole"},
		 {"Instrument-Agency"}, 
		 {"Product-Producer"},*/
		
		// according to 10-fold cross validation		 
		 {"Entity-Destination"}, 
		 {"Member-Collection"},
		 {"Cause-Effect"},
		 {"Entity-Origin"},
		 {"Content-Container"}, 
		 {"Message-Topic"},
		 {"Product-Producer"},
		 {"Instrument-Agency"}, 
		 {"Component-Whole"},

		 
		// according to Pos in Lorenza's paper
	/*	 {"Cause-Effect"},
		 {"Component-Whole"},
		{"Entity-Destination"}, 
		 {"Member-Collection"},
		 {"Message-Topic"},
		 {"Entity-Origin"},
		 {"Product-Producer"},
		 {"Instrument-Agency"}, 
		 {"Content-Container"}, 

		// according to IAA in Lorenza's paper		 
		 {"Content-Container"}, 
		 {"Product-Producer"},
		 {"Cause-Effect"},
		{"Entity-Destination"}, 
		 {"Message-Topic"},
		 {"Component-Whole"},
		 {"Member-Collection"},
		 {"Instrument-Agency"}, 
		 {"Entity-Origin"},
*/
	};
	

	public static String[][] relArgsCoarse = new String[][]{
		{"T", "T"},
		{"T", "T"},
	//	{"T", "T"},
		{"T", "T"},
		{"T", "T"},
		{"T", "T"},
		{"T", "T"},
		{"T", "T"},
		{"T", "T"},
		{"T", "T"},
	};
			
	
	
}
