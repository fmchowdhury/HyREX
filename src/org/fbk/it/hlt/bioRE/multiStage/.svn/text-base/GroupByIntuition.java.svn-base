package org.fbk.it.hlt.bioRE.multiStage;

import java.util.ArrayList;

import org.fbk.it.hlt.bioRE.FormatConverterJSRE;
import org.fbk.it.hlt.bioRE.SemEvalDataProcessor;
import org.itc.irst.tcc.sre.Predict;
import org.itc.irst.tcc.sre.Train;

import Utility.DataStrucUtility;
import Utility.FileUtility;

public class GroupByIntuition {

	
	
	public String[] multiStageClassification_by_intuition( String[] tokenizedTrainingDataByRelFileNames,
			String[][] relTypes, String kernelName, int level, String testDataJSreFileName,
			String combinedOutputFile ) throws Exception{
		
		String suf = "level_" + level;
		MultiStageRE clsMultiStageRE = new MultiStageRE();
		
		int[] relClassLabels = new int[relTypes.length];			
		int	defaultClassLabel = clsMultiStageRE.populateClassLabels(relTypes, 
				relClassLabels, 1, MultiStageRE.DEFAULT_REL);
					
		//-- construct training data for the relations
		ArrayList<String> allRelNames = new ArrayList<String>(); 
		for ( int i=0; i<relTypes.length; i++ )
			for ( int j=0; j<relTypes[i].length; j++ )
				allRelNames.add(relTypes[i][j]);

		String tokenizedTrainingData = clsMultiStageRE.addMultipleFileContents(tokenizedTrainingDataByRelFileNames, 
				allRelNames);
		FileUtility.writeInFile("tmp/temp", tokenizedTrainingData, false);
		
		String trainingDataJSreInpFormat = new FormatConverterJSRE().posTaggedToSreFormatConverter("tmp/temp", 
				relTypes, false, relClassLabels, defaultClassLabel);
		FileUtility.writeInFile("tmp/temp", trainingDataJSreInpFormat, false);
		
		//-- call jSre to train single model
		Train.main(("-k " + kernelName + " " 
				+ "tmp/temp"  + " out/model_" + suf).split("\\s+"));

		//-- call jSre to tag test data using single model
		String strPredOut = Predict.getPredictionResult((testDataJSreFileName + " out/model_" + suf +
				"  out/output_" + suf).split("\\s+"));
	
		//-- Separate test sentences by labelled relation types
		String[] testSreFilesSeparated = clsMultiStageRE.separateSrePredOfTestInputByRel(relTypes, 
				FileUtility.readFileContents(testDataJSreFileName), 
				"_out_pred", FileUtility.readFileContents("out/output_" + suf), 0 );
	
		ArrayList<String> tempTestSreFiles = new ArrayList<String>(); 
		for ( int i=0; i<relTypes.length; i++  ){
			if ( !relTypes[i][0].toLowerCase().contains("others") ){
				if ( relTypes[i].length == 1 && !relTypes[i][0].contains(",") )
					clsMultiStageRE.writeSemevalFormatOutput( null, combinedOutputFile, true, 
						testSreFilesSeparated[i], relTypes[i][0], false);
				else
					tempTestSreFiles.add(testSreFilesSeparated[i]);
			}
		}
		
		return DataStrucUtility.listToStringArray(tempTestSreFiles);
	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public void trainAndPredict( String tokenizedTrainingFileName, 
			String kernelName, String tokenizedTestFileName ) throws Exception{
		
		String combinedOutputFile = "out/output_single";
		FileUtility.writeInFile(combinedOutputFile, "", false);
		String testDataJSreFileName = FormatConverterJSRE.createJSreInputFiles(tokenizedTestFileName, 
				relTypesAll, false, false, 1, DEFAULT_REL)[0];
		//"tmp/preprocessedTest.txt.sre";//
		
		MultiStageRE clsMultiStageRE = new MultiStageRE();
		
		String[] tokenizedTrainingDataByRelFileNames = //clsMultiStageRE.separateTokenizedTrainingDataByRel(
			new SemEvalDataProcessor().createBalancedCorpusByElimination(
					tokenizedTrainingFileName, relTypesAll);
			
		clsMultiStageRE.mapOrigSenIdWithSysGenSenId(tokenizedTestFileName);
		
		String[] testFile = multiStageClassification_by_intuition(tokenizedTrainingDataByRelFileNames, 
				relTypesAll,
				//relTypesByLevels[1], 
				kernelName, 
					1, testDataJSreFileName, combinedOutputFile);
	/*
		testFile = multiStageClassification_by_intuition(tokenizedTrainingDataByRelFileNames, 
				relTypesByLevels[1], kernelName, 
				2, testFile[0], combinedOutputFile);
	*//*
		multiStageClassification_by_intuition(tokenizedTrainingDataByRelFileNames, 
				relTypesFinalLevel[0], kernelName, 
				31, testFile[0], combinedOutputFile);
	
		multiStageClassification_by_intuition(tokenizedTrainingDataByRelFileNames, 
				relTypesFinalLevel[1], kernelName, 
				32, testFile[1], combinedOutputFile);	
		/*
		multiStageClassification_by_intuition(tokenizedTrainingDataByRelFileNames, 
				relTypesFinalLevel[2], kernelName, 
				33, testFile[2], combinedOutputFile);*/
	}

	static String DEFAULT_REL = "Other"; 
	
	public static String[][] relTypesAll = new String[][]{
		// {"Other"},
		 {"Entity-Destination"}, 
		 {"Cause-Effect"},
		 {"Member-Collection"},
		 {"Content-Container"}, 
		 {"Entity-Origin"},
		 {"Message-Topic"},
		 {"Component-Whole"},
		 {"Instrument-Agency"}, 
		 {"Product-Producer"},
	};
	
	String[][][] relTypesByLevels =
		{
			{//{"Other"},
	 {"Entity-Destination", 
	 "Cause-Effect",
	 "Member-Collection",
	 "Content-Container", 
	"Entity-Origin",
	 "Message-Topic",
	 "Component-Whole",
	 "Instrument-Agency", 
	 "Product-Producer"}},
	 
	{//{"Other"},
		 {"Cause-Effect"},
	 {"Instrument-Agency"}, 
	 {"Product-Producer"},
	 {"Entity-Origin", "Entity-Destination"},
	 {"Content-Container", "Component-Whole", "Member-Collection"},	 
	 {"Message-Topic"}},
	 };
	

	String[][][] relTypesFinalLevel =
	 	{{{"Entity-Origin"}, {"Entity-Destination"}},
		 
		{{"Content-Container"}, {"Component-Whole"}, {"Member-Collection"}}
	 	};
	
	
	/*
	//- grouping by distribution
	
	public static String[][] relTypesAll = new String[][]{
		 {"Other"},
		 
		 {"Cause-Effect"},
		 
		 {"Component-Whole"},
		 
		 {"Entity-Destination"}, 
		 {"Product-Producer"},
		 
		 {"Entity-Origin"},
		 {"Member-Collection"},
		 
		 {"Message-Topic"},
		 {"Content-Container"}, 
		 {"Instrument-Agency"}, 
		 
	};

	String[][][] relTypesByLevels =
		{
			{{"Other"},
	 {"Entity-Destination", 
	 "Cause-Effect",
	 "Member-Collection",
	 "Content-Container", 
	"Entity-Origin",
	 "Message-Topic",
	 "Component-Whole",
	 "Instrument-Agency", 
	 "Product-Producer"}},
	 
	{{"Other"},
		 {"Cause-Effect"},
		 {"Component-Whole"},
		 
		 {"Entity-Destination", 
		 "Product-Producer"},
		 
		 {"Entity-Origin",
		 "Member-Collection"},
	 
		 {"Message-Topic",
		 "Content-Container", 
		 "Instrument-Agency"}},
	 };
	

	String[][][] relTypesFinalLevel =
	 	{{{"Entity-Destination"}, {"Product-Producer"}},
		 
		{{"Entity-Origin"}, {"Member-Collection"}},
		
		{{"Message-Topic"}, {"Content-Container"}, {"Instrument-Agency"}}
	 	};
	 	*/
}
