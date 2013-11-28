package org.fbk.it.hlt.bioRE;
import org.fbk.it.hlt.bioRE.FormatConverterJSRE;
import org.fbk.it.hlt.bioRE.multiStage.MultiStageRE;
import org.itc.irst.tcc.sre.Predict;
import org.itc.irst.tcc.sre.Train;

import Utility.FileUtility;

public class BinaryRelExtractor {

	/**
	 * @param args
	 * @throws Exception 
	 */
	/*
	public static void main(String[] args) throws Exception {
	//*
	  	getFvForTrainData("../../workspace/5_PPI_corpora/aimed/folds/tk/train-203-1.BioRelEx",
				"SL",
				"../../workspace/5_PPI_corpora/aimed/folds/tk/train-203-1.BioRelEx.jsre",
				"../../workspace/5_PPI_corpora/aimed/folds/tk/train-203-1.BioRelEx.model");
		
		//* /
		//getFvForTrainData("../../data/AImed/folds_by_abstracts/tk/train-203-1.BioRelEx", "SL", "../../data/AImed/folds_by_abstracts/tk/train-203-1.BioRelEx.jsre", "../../data/AImed/folds_by_abstracts/tk/train-203-1.BioRelEx.model");
		//trainAndPredict("/media/Study/workspace/ModifiedTK/fvTrainBioRelEx", "SL", 
			//	"/media/Study/workspace/ModifiedTK/fvTestBioRelEx");
		// getFvForTestData("/media/Study/data/AImed/folds_by_abstracts/tk/train-203-1.BioRelEx", 
			//	"/media/Study/data/AImed/folds_by_abstracts/tk/train-203-1.BioRelEx.jsre", "");
	}
	*/

	/**
	 * 
	 * @throws Exception
	 */
	public static String trainAndPredict( String tokenizedTrainingFileName, 
			String kernelName, String tokenizedTestFileName ) throws Exception{
		
		String combinedOutputFile = "out/output_single";
		FileUtility.writeInFile(combinedOutputFile, "", false);
		
		String testDataJSreFileName = FormatConverterJSRE.createJSreInputFiles(tokenizedTestFileName, 
				relTypesAll, false, false, 0, DEFAULT_REL)[0];
		
		MultiStageRE clsMultiStageRE = new MultiStageRE();
		
		int[] relClassLabels = new int[relTypesAll.length];			
		int	defaultClassLabel = clsMultiStageRE.populateClassLabels(relTypesAll, 
				relClassLabels, 0, BinaryRelExtractor.DEFAULT_REL);
					
		//-- construct training data for the relations
		
		String trainingDataJSreInpFormat = new FormatConverterJSRE().posTaggedToSreFormatConverter(tokenizedTrainingFileName, 
				relTypesAll, false, relClassLabels, defaultClassLabel);
		FileUtility.writeInFile("tmp/temp", trainingDataJSreInpFormat, false);
		
		//-- call jSre to train single model
		Train.main(("-k " + kernelName + " " 
				+ "tmp/temp"  
				+ " out/model").split("\\s+"));

		//-- call jSre to tag test data using single model
		String strPredOut = Predict.getPredictionResult((testDataJSreFileName + " out/model" +
				"  out/output").split("\\s+"));
	
		System.out.println(strPredOut);
		return strPredOut;
	}
	
	
	/**
	 * 
	 * @param tokenizedTrainingFileName
	 * @param kernelName
	 * @param trainJsreFile
	 * @param modelFile
	 * @return
	 * @throws Exception
	 */
	public static String getFvForTrainData( String tokenizedTrainingFileName, 
			String kernelName, String trainJsreFile, String modelZipjSRE ) throws Exception{
		
		MultiStageRE clsMultiStageRE = new MultiStageRE();
		
		int[] relClassLabels = new int[relTypesAll.length];			
		int	defaultClassLabel = clsMultiStageRE.populateClassLabels(relTypesAll, 
				relClassLabels, 0, BinaryRelExtractor.DEFAULT_REL);
					
		//-- construct training data for the relations
		
		String trainingDataJSreInpFormat = new FormatConverterJSRE().posTaggedToSreFormatConverter(tokenizedTrainingFileName, 
				relTypesAll, false, relClassLabels, defaultClassLabel);
		FileUtility.writeInFile(trainJsreFile, trainingDataJSreInpFormat, false);
		
		//-- call jSre to train single model
		return new Train(("-k " + kernelName + " " + trainJsreFile + " " + modelZipjSRE).split("\\s+")).getFvFromData();
	}
	
	
	
	/**
	 * 
	 * @param tokenizedTestFileName
	 * @param testJsreFile
	 * @param kernelName
	 * @param outputFileName
	 * @param modelFileName
	 * @return
	 * @throws Exception
	 */
	public static String getFvForTestData( String tokenizedTestFileName, String testJsreFile, String modelZipjSRE ) throws Exception{	
	
		String tmpTestDataJSre = FormatConverterJSRE.createJSreInputFiles(tokenizedTestFileName, 
				relTypesAll, false, false, 0, DEFAULT_REL)[0];
		
		FileUtility.writeInFile(testJsreFile, FileUtility.readFileContents(tmpTestDataJSre), false);
		
		//-- call jSre to tag test data using single model
		return new Predict(tmpTestDataJSre, modelZipjSRE, "").getFvFromData();
	}

	
	static String DEFAULT_REL = "NO"; 
	int g =0;
	public static String[][] relTypesAll = new String[][]{
		 {"NO"},
		 {"YES"},
	};
}
