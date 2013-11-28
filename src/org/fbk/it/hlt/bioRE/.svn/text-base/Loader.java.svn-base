package org.fbk.it.hlt.bioRE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Properties;

//import org.itc.irst.tcc.sre.*;

import org.fbk.it.hlt.bioRE.multiStage.MultiStageRE;
import org.itc.irst.tcc.sre.*;

public class Loader {

	/*
	 * In JSre, a token is any sequence of adjacent characters

		each token is represented with 6 attributes separated by the special
		character sequence "&&"
		e.g. tokenid&&token&&lemma&&POS&&entity_type&&entity_label\s
		
		an entity must be represented as a single token where all whitespaces are
		substituted by the "_" character (e.g. “Vitamin_D_receptor")
		
		example -> label\tid\tbody\n
		body -> [tokenid&&token&&lemma&&POS&&entity_type&&entity_label\s]+
		
		tokenid incremental position of the token in the sentence
		token the actual token "Also" "being" "Vitamin_D_receptor"
		lemma lemma "also" be" "Vitamin_D_receptor"
		POS part of speech tag "RB" "VBG" "NN"
		entity_type possible type of the token “PROT”, "O" for token that are not entities
		entity_label T|O this attribute is to label the candidate pair. Each candidate
		entity is labelled T (target) any other entity islabelled "O".
		
		
		lawsonite	NN1	lawsonite	B-e1
		was	VBD	be	O
		contained	VVN	contain	O
		in	PRP	in	O
		a	AT0	a	O
		platinum	NN1	platinum	B-e2
		crucible	NN1	crucible	I-e2
		
		Content-Container(e1,e2)
	 */
	
	
	static int totalRel = 0;
	static ArrayList<Integer> listMappedOrigSenIdWithSysGenSenId = null;
	static String evalResultFileName = "tmp/evalResult";
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		 doCrossFoldExp();
		//trainAndPredict();
	}
	
	
	public static void doCrossFoldExp() throws Exception{
		//String str = "/media/Study/phd_courses/tp_2/preprocessedTrain.TD1.txt";
		String tokenizedSenFileName = "/media/Study/workspace/BioRelEx/others/preprocessedTrain.TD4.txt_without_other";
		
		//-- set array of different kernel combination
		// TODO: In future, the following combinations have to be computed automatically 
		String[] arrKernelCombinations = new String[]{
			/*"BOW",
					"LC",
					"GC",
				"BOW_LC",
				"BOW_GC",
			*/	"SL",  // SL = LC + GC
			/*	"BOW_SL",
				/*	
				"WNA",
				"BOW", "WN",
				"LC", "WN",
				"GC", "WN",
				"BOW", "LC", "WN",
				"BOW", "GC", "WN",
				"LC", "GC", "WN",	
				"BOW", "LC", "GC", "WN"*/ 
				};
	
		int noFolds = 10, noMultiLineEntriesPerSen = 2;
		CrossFoldRelationEvaluator.call(arrKernelCombinations, noFolds, noMultiLineEntriesPerSen, 
				tokenizedSenFileName, evalResultFileName, Loader.relTypesCoarse);
	}
	
	
	
	private static void twoStageClassification( String trainSreFileCoarse,
			String[] trainSreFilesSeparatedFine, String testSreFile, 
			String kernel, String evalResult, int z ) throws Exception{
		
		//-- call jSre to train single model
		Train.main(("-k " + kernel + " " 
				+ trainSreFileCoarse  + " model_single").split("\\s"));

		//-- call jSre to tag test data using single model
		Predict.getPredictionResult((testSreFile + " model_single output_single").split("\\s"));
				
		String[] testSreFilesSeparated = separateSrePredOfTestInputByRel(relTypesCoarse, Utility.readFileContents(testSreFile), 
				"_out_pred", Utility.readFileContents("output_single") ); 
			
		boolean isAppend = false;
		
		for ( int i=0; i<trainSreFilesSeparatedFine.length; i++ )
			if ( !relTypesFine[i].equals(DEFAULT_REL) ) {
				/*
				 * -- train separate models for each relation using the separated sre inputs where
				 * 		each file contains (examples of the corresponding relations) only two classes 
				 * 		which basically represent different	order of the arguments  
				 */
				
				Train.main(("-k " + kernel + " " 
					+ trainSreFilesSeparatedFine[i]  + " model_" + i).split("\\s"));
							
				String predOutput = Predict.getPredictionResult((testSreFilesSeparated[i] + " model_" + i + " output_" + i).split("\\s"));
					
				Utility.writeInFile(evalResult, predOutput + "\n", true);
			
				
				//-- convert and merge output according to the semval format
				writeSemevalFormatOutput( "output_"+i, "semval_out_" + z, 
						isAppend, testSreFilesSeparated[i], i);
				isAppend = true;
			}
			else
				addOtherRelToSemevalFormatOutput( "semval_out_" + z, testSreFilesSeparated[i]);
	}
	
	// remember: system generated ids start from 1.
	private static void mapOrigSenIdWithSysGenSenId ( String tokenizedFile ) throws Exception{
		BufferedReader input =  new BufferedReader(new FileReader(new File(tokenizedFile)));  
	    String line = null;
	    listMappedOrigSenIdWithSysGenSenId = new ArrayList<Integer>();  
	    listMappedOrigSenIdWithSysGenSenId.add(-1);
	    
	    while (( line = input.readLine()) != null)
	    	if ( line.contains("Sentence:") )
	    		listMappedOrigSenIdWithSysGenSenId.add(Integer.valueOf(line.replace("Sentence:", "").trim()));
	    
	    input.close();
	}
	
	
	private static void writeSemevalFormatOutput( String origOuputFile, 
			String formattedOutputFile, boolean isAppend,
			 String testSreFile, int coarseRelIndex) throws Exception{
		
		BufferedReader input =  new BufferedReader(new FileReader(new File(origOuputFile))),  
			inputSre =  new BufferedReader(new FileReader(new File(testSreFile)));
	    String line = null, senId = null, contents = "", relClass = null;
	    
	    while (( line = input.readLine()) != null){
	    	senId = inputSre.readLine().split("\\t")[1];
	    	int cl = (int)(Double.valueOf(line)/1);
	 
	    	// TODO:
	    	if ( cl == 1 )
	    		relClass = relTypesCoarse[coarseRelIndex] + "(e2,e1)\n";
	    	else
	    		relClass = relTypesCoarse[coarseRelIndex] + "(e1,e2)\n";
	    		    	
	    	contents = contents + listMappedOrigSenIdWithSysGenSenId.get(Integer.valueOf(senId)) + "\t" + relClass; 
	    }
	    
	    input.close();
	    inputSre.close();
	    
	    Utility.writeInFile(formattedOutputFile, contents, isAppend);
	}
	
	private static void addOtherRelToSemevalFormatOutput( String formattedOutputFile, String testSreFile) throws Exception{
		
		BufferedReader inputSre =  new BufferedReader(new FileReader(new File(testSreFile)));
	    String senId = null, contents = "";
	    
	    while (( senId = inputSre.readLine()) != null){
	    	senId = senId.split("\\t")[1];	    	
	    	// TODO:
	    	contents = contents + listMappedOrigSenIdWithSysGenSenId.get(Integer.valueOf(senId)) + "\t" + relTypesCoarse[0] + "\n"; 
	    }
	    
	    inputSre.close();
	    
	    Utility.writeInFile(formattedOutputFile, contents, true);
	}
	
	
	
	private static String[] separateSrePredOfTestInputByRel( String[][] relTypesCoarse, String sreInpForAllRel, 
			String outputFileSuf, String predTestOutput ) throws Exception{
		
		int senId = 1;
		
		//-- separate senIds of sre input lines by relations
		ArrayList<ArrayList<Integer>> listSrePredTestInpByRel = new ArrayList<ArrayList<Integer>>();
					
		for ( int i=0; i<relTypesCoarse.length; i++ )
			listSrePredTestInpByRel.add(new ArrayList<Integer>());
				
		String[] arrSrePredForAllRel = predTestOutput.split("\n");
		
		for ( int s=0; s<arrSrePredForAllRel.length; s++ ){
			
			arrSrePredForAllRel[s] = arrSrePredForAllRel[s].trim();
			if ( !arrSrePredForAllRel[s].isEmpty() ){
				int index = (int)(Double.valueOf(arrSrePredForAllRel[s])/1); 
				listSrePredTestInpByRel.get(index).add(senId-1);	
				senId++; 
			}
		}

		//-- separate sre input lines by relations
		String[] arrSreInpForAllRel = sreInpForAllRel.split("\n");
		String[] arrSreInpByRel = new String[relTypesCoarse.length];
		
		for ( int i=0; i<relTypesCoarse.length; i++ )
			arrSreInpByRel[i] = "";
		
		for ( int x=0; x<listSrePredTestInpByRel.size(); x++ ){
			for ( int z=0; z<listSrePredTestInpByRel.get(x).size(); z++ ){
					
				arrSreInpByRel[x] = arrSreInpByRel[x].concat(
						arrSreInpForAllRel[listSrePredTestInpByRel.get(x).get(z)]).concat("\n"); 
			}
		}
		
		//-- write sre input files separated by relations 
		String[] sreFiles = new String[relTypesCoarse.length];
		for ( int i=0; i<relTypesCoarse.length; i++ ){
			sreFiles[i] = "tmp/" + relTypesCoarse[i][0] + outputFileSuf + ".sre";
			
			Utility.writeInFile(sreFiles[i], arrSreInpByRel[i], false);
		}
		
		return sreFiles;
	}
		
	
	
	private static String[] createSeparateSreInputByRel ( String mainFile, String[][] relTypesFine, 
			String[][] relTypesCoarse, boolean isConsiderDirection ) throws Exception{
			
		//-- create sre format of the original file with all relations with directions  
		FormatConverterJSRE clsFormatConverterJSRE = new FormatConverterJSRE();
		int[] relClassLabels = new int[relTypesFine.length];

		int defaultClassLabel = new MultiStageRE().populateClassLabels(relTypesFine, relClassLabels, 1, mainFile);
					
		String sreInpForAllRel = clsFormatConverterJSRE.posTaggedToSreFormatConverter(mainFile, 
				relTypesFine, isConsiderDirection, relClassLabels, defaultClassLabel);
		
		Utility.writeInFile("tmp/see", sreInpForAllRel, false);
		return separateSreInputByRel(sreInpForAllRel, relTypesCoarse, "_only");
	}
	
	
	
	private static String[] separateSreInputByRel( String sreInpForAllRel, 
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
		String[] sreFiles = new String[relTypesCoarse.length];
		for ( int i=1; i<relTypesCoarse.length; i++ ){
			sreFiles[i] = "tmp/" + relTypesCoarse[i][0] + outputFileSuffix + ".sre";
			
			Utility.writeInFile(sreFiles[i], arrSreInpByRel[i], false);
		}
		
		return sreFiles;
	}
	
	static String DEFAULT_REL = "Other"; 
	public static String[][] relTypesCoarse = new String[][]{
		{"Entity-Destination"}, 
		 {"Cause-Effect"},
		 {"Member-Collection"},
		 {"Content-Container"}, 
		 {"Entity-Origin"},
		 {"Message-Topic"},
		 {"Component-Whole"},
		 {"Instrument-Agency"}, 
		 {"Product-Producer"},
		 {"Other"},
	};
	

	public static String[][] relArgsCoarse = new String[][]{
		{"T", "T"},
		{"T", "T"},
		{"T", "T"},
		{"T", "T"},
		{"T", "T"},
		{"T", "T"},
		{"T", "T"},
		{"T", "T"},
		{"T", "T"},
		{"T", "T"},
	};
	
	public static String[][] relTypesFine = new String[][]{ 
		{"Entity-Destination(e1,e2)"}, {"Entity-Destination(e2,e1)"}, 
		 {"Cause-Effect(e1,e2)"}, {"Cause-Effect(e2,e1)"},
		 {"Member-Collection(e1,e2)"}, {"Member-Collection(e2,e1)"},
		 {"Content-Container(e1,e2)"}, {"Content-Container(e2,e1)"},
		 {"Entity-Origin(e1,e2)"}, {"Entity-Origin(e2,e1)"},
		 {"Message-Topic(e1,e2)"}, {"Message-Topic(e2,e1)"},
		 {"Component-Whole(e1,e2)"}, {"Component-Whole(e2,e1)"},
		 {"Instrument-Agency(e1,e2)"},	{"Instrument-Agency(e2,e1)"}, 
		 {"Product-Producer(e1,e2)"}, {"Product-Producer(e2,e1)"},
		 {"Other", "Other"},	 
		};
	
	public static String[][] relArgsFine = new String[][]{
		{"T", "T"}, {"T", "T"},
		{"T", "T"}, {"T", "T"},
		{"T", "T"}, {"T", "T"},
		{"T", "T"}, {"T", "T"},
		{"T", "T"}, {"T", "T"},
		{"T", "T"}, {"T", "T"},
		{"T", "T"}, {"T", "T"},
		{"T", "T"}, {"T", "T"},
		{"T", "T"}, {"T", "T"},
		{"T", "T"}, {"T", "T"}		
	};
	
	public static String[][] relArgsFine_dir = new String[][]{
		{"T", "T"},
		{"A", "T"}, {"T", "A"},
		{"A", "T"}, {"T", "A"},
		{"A", "T"}, {"T", "A"},
		{"A", "T"}, {"T", "A"},
		{"A", "T"}, {"T", "A"},
		{"A", "T"}, {"T", "A"},
		{"A", "T"}, {"T", "A"},
		{"A", "T"}, {"T", "A"},
		{"A", "T"}, {"T", "A"}		
	};
	

		
	
	private static void trainAndPredict() throws Exception{
		
		boolean isConsiderDirection = false;
		
		String tesTokenizedFile = "/media/Study/workspace/BioRelEx/others/preprocessedTest.txt";
		String testSreFile = FormatConverterJSRE.createJSreInputFiles(tesTokenizedFile, 
					relTypesCoarse, isConsiderDirection, false, 1, DEFAULT_REL)[0];

		
		for ( int i=4; i<=4; i++ ){
			String trainTokenizedFile = "/media/Study/workspace/BioRelEx/others/preprocessedTrain.TD" + i + ".txt";
		
	//	trainAndPredict( "tmp/preprocessedTrain.TD1.txt.sre", "tmp/preprocessedTrain.TD1.txt.sre", "SL");
		
			mapOrigSenIdWithSysGenSenId(tesTokenizedFile);
			
			String trainSreFileCoarse = FormatConverterJSRE.createJSreInputFiles(trainTokenizedFile, 
					relTypesCoarse, isConsiderDirection, false, 1, DEFAULT_REL)[0];
			
			String[] trainSreFilesSeparatedFine = createSeparateSreInputByRel(trainTokenizedFile, relTypesFine, 
					relTypesCoarse, isConsiderDirection);
			
			
			twoStageClassification(trainSreFileCoarse, trainSreFilesSeparatedFine, testSreFile, "SL", evalResultFileName, i);
		}
	}
	
	
	private static void aimedTest() throws Exception{
		//-- call jSre to train
		Train.main(("-k BOW_SL /media/Study/workspace/jsre/examples/aimed.train modelSL").split("\\s"));
				
		//-- call jSre to test
		String predOutput = Predict.getPredictionResult(
				("/media/Study/workspace/jsre/examples/aimed.test modelSL output").split("\\s"));
	}
}



/*
StatSignfTester sst = new StatSignfTester();
int x =0;
sst.createStatTestInputFiles( "/media/Study/workspace/FbkBioEntityExtractor/tmp/disease.sorted.nonOverlapped.annotations.azdc_fold_", 
		"/media/Study/workspace/FbkBioEntityExtractor/exp_results/without_DB/output/formattedAndProcessedTaggedResult_fold_",
		"/media/Study/workspace/FbkBioEntityExtractor/output/formattedAndProcessedTaggedResult_fold_", 
		x, x+10);
*/