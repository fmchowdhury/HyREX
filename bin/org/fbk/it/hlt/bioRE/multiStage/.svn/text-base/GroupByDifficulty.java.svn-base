package org.fbk.it.hlt.bioRE.multiStage;

import java.util.ArrayList;

import org.fbk.it.hlt.bioRE.FormatConverterJSRE;
import org.fbk.it.hlt.bioRE.Loader;
import org.fbk.it.hlt.bioRE.SemEvalDataProcessor;
import org.itc.irst.tcc.sre.Predict;
import org.itc.irst.tcc.sre.Train;

import Utility.FileUtility;
import Utility.TextUtility;

/**
 * Lets assume there are total 2N relations. 
 * Sort them based on the prediction result of 10-fold cross validation. 
 * Group the N relations in the lower half into one meta relation.
 * Now, there are N+1 relations. 
 * Train model with N+1 relations and predict result on test data.
 * Separate the items labelled with the meta relation.
 * Sort the N relations of the meta relation based on 10-fold cross validation result.
 * Group the N/2 relations in the lower half into a new meta relation.
 * ......
 * ........
 * Continue the process until there remains only 1 relation to be grouped as meta relation.
 *   
 */
public class GroupByDifficulty {

	/**
	 * 
	 * @param tokenizedTrainingFileName
	 * @param relTypesSortedByDiffiulty
	 * @param mergeNoOfRel
	 * @param mergeStopThreshold
	 * @param kernelName
	 * @param level
	 * @param testDataJSreFileName
	 * @param appendResultFromRelIndx
	 * @param combinedOutputFile
	 * @return
	 * @throws Exception
	 */
	public String multiStageClassification_by_unfolding_half_each_stage( String[] tokenizedTrainingDataByRelFileName,
			String[][] relTypesSortedByDiffiulty, int mergeNoOfRel, 
			int mergeStopThreshold, String kernelName, int level, String testDataJSreFileName,
			int appendResultFromRelIndx, String combinedOutputFile ) throws Exception{
		
		int n = 2;
		String suf = "level_" + level;
		boolean lastStage = false;
		MultiStageRE clsMultiStageRE = new MultiStageRE();
		
		String[][] relTypesForCurrentStage = createHiererchyOfRelsOfPrevStage(relTypesSortedByDiffiulty, mergeNoOfRel);
		if ( mergeStopThreshold >= n*mergeNoOfRel )
			testDataJSreFileName = multiStageClassification_by_unfolding_half_each_stage( tokenizedTrainingDataByRelFileName, relTypesSortedByDiffiulty, 
					n*mergeNoOfRel, mergeStopThreshold, kernelName, level+1,
					testDataJSreFileName, relTypesForCurrentStage.length-1, combinedOutputFile);
		else
			lastStage = true;
					
		int[] relClassLabels = null;
		int defaultClassLabel = 0;

		if ( lastStage ){
			relClassLabels = new int[relTypesForCurrentStage.length];			
			defaultClassLabel = clsMultiStageRE.populateClassLabels(relTypesForCurrentStage, relClassLabels, 
					1, MultiStageRE.DEFAULT_REL);
		
			String tokenizedTrainingData = clsMultiStageRE.addMultipleFileContents(tokenizedTrainingDataByRelFileName, 0);
			FileUtility.writeInFile("tmp/temp", tokenizedTrainingData, false);
			
			String trainingDataJSreInpFormat = new FormatConverterJSRE().posTaggedToSreFormatConverter("tmp/temp", 
					relTypesForCurrentStage, false, relClassLabels, defaultClassLabel);
			FileUtility.writeInFile("tmp/temp", trainingDataJSreInpFormat, false);

			//-- call jSre to train single model
			Train.main(("-k " + kernelName + " " 
					+ "tmp/temp"  + " out/model_" + suf).split("\\s+"));

			//-- call jSre to tag test data using single model
			String strPredOut = Predict.getPredictionResult((testDataJSreFileName + " out/model_" + suf +
					"  out/output_" + suf).split("\\s+"));

		//	Utility.writeInFile(Loader.evalResultFileName, "\n\n" + Utility.now() + "\n" + strPredOut + "\n\n", true);

			//-- Separate test sentences by labelled relation types
			String[] testSreFilesSeparated = clsMultiStageRE.separateSrePredOfTestInputByRel(relTypesForCurrentStage, 
					FileUtility.readFileContents(testDataJSreFileName), 
					"_out_pred", FileUtility.readFileContents("out/output_" + suf), 0 );
		
			//-- merge output of the upper half relations in the final output file
			for ( int i=0; i<relTypesForCurrentStage.length-1; i++  )
				clsMultiStageRE.writeSemevalFormatOutput( null, combinedOutputFile, true, 
						testSreFilesSeparated[i], relTypesForCurrentStage[i][0], false);
				
			return testSreFilesSeparated[testSreFilesSeparated.length-1];
		}
		else{
			relClassLabels = new int[n*mergeNoOfRel - mergeNoOfRel + 1];
			int labelStartIndex =  relTypesForCurrentStage.length - relClassLabels.length + 1;
			defaultClassLabel = clsMultiStageRE.populateClassLabels(relTypesForCurrentStage, 
					relClassLabels, labelStartIndex, MultiStageRE.DEFAULT_REL);
			
			String[][] relTypes = new String[relClassLabels.length][];
			/*-- populate relTypes from relTypesForCurrentStage by considering only the relations whose output were
			 *-- merged in the previous stage
			 **/
			for ( int i=0; i<relClassLabels.length; i++ )
				relTypes[i] = relTypesForCurrentStage[relClassLabels[i]-1];

			
			//	merge tokenized training data of those relations that were merged in the previous step
			String tokenizedTrainingData = clsMultiStageRE.addMultipleFileContents(tokenizedTrainingDataByRelFileName, relClassLabels[0]-1);
			FileUtility.writeInFile("tmp/temp", tokenizedTrainingData, false);
			
			//	convert it into jSre format
			String trainingDataJSreInpFormat = new FormatConverterJSRE().posTaggedToSreFormatConverter("tmp/temp", 
				relTypes, false, relClassLabels, defaultClassLabel);
			FileUtility.writeInFile("tmp/temp", trainingDataJSreInpFormat, false);
			
			//-- call jSre to train single model
			Train.main(("-k " + kernelName + " " 
					+ "tmp/temp"  + " out/model_" + suf).split("\\s+"));

			//-- call jSre to tag test data using single model
			String strPredOut = Predict.getPredictionResult((testDataJSreFileName + " out/model_" + suf +
					" out/output_" + suf).split("\\s+"));
			
		//	Utility.writeInFile(Loader.evalResultFileName, "\n\n" + Utility.now() + "\n" + strPredOut + "\n\n", true);
			
			//-- Separate test sentences by labelled relation types
			String[] testSreFilesSeparated = clsMultiStageRE.separateSrePredOfTestInputByRel(relTypes, 
					FileUtility.readFileContents(testDataJSreFileName), 
					"_out_pred", FileUtility.readFileContents("out/output_" + suf), labelStartIndex-1 );
		
			//-- merge output of the upper half relations in the final output file
			for ( int i=0; i<relTypes.length-1; i++  )
				clsMultiStageRE.writeSemevalFormatOutput( null, combinedOutputFile, true, 
						testSreFilesSeparated[i], relTypes[i][0], false);
			

			return testSreFilesSeparated[testSreFilesSeparated.length-1];
		}
		
	}
	
	
	
	public String multiStageClassification_by_excluding_other_and_unfolding_half_each_stage( String[] tokenizedTrainingDataByRelFileNames,
			String[][] relTypesSortedByDiffiulty, int mergeNoOfRel, 
			int mergeStopThreshold, String kernelName, int level, String testDataJSreFileName,
			int appendResultFromRelIndx, String combinedOutputFile ) throws Exception{
		
		String suf = "level_" + level;
		MultiStageRE clsMultiStageRE = new MultiStageRE();
		
		String[][] relTypesWithoutDefault = new String[relTypesSortedByDiffiulty.length-1][];
		String[][] relTypesForCurrentStage = new String[2][];
		String[] tempTrainingFiles = new String[tokenizedTrainingDataByRelFileNames.length-1];
		
		int j = 0;
		for ( int i=0; i<relTypesSortedByDiffiulty.length; i++ )
			if ( !relTypesSortedByDiffiulty[i][0].equals(MultiStageRE.DEFAULT_REL) ){
				relTypesWithoutDefault[j] = relTypesSortedByDiffiulty[i];
				// remove default relation's training data file
				tempTrainingFiles[j] = tokenizedTrainingDataByRelFileNames[j];
				j++;				
			}
			else
				relTypesForCurrentStage[0] = relTypesSortedByDiffiulty[i];
		
		relTypesSortedByDiffiulty = createHiererchyOfRelsOfPrevStage(relTypesWithoutDefault, 
				relTypesWithoutDefault.length);
				
		relTypesForCurrentStage[1] = relTypesSortedByDiffiulty[0];		
					
		int[] relClassLabels = new int[relTypesForCurrentStage.length];			
		int	defaultClassLabel = clsMultiStageRE.populateClassLabels(relTypesForCurrentStage, relClassLabels, 1, MultiStageRE.DEFAULT_REL);
		
		String tokenizedTrainingData = clsMultiStageRE.addMultipleFileContents(tokenizedTrainingDataByRelFileNames, 0);
		FileUtility.writeInFile("tmp/temp", tokenizedTrainingData, false);
		
		String trainingDataJSreInpFormat = new FormatConverterJSRE().posTaggedToSreFormatConverter("tmp/temp", 
				relTypesForCurrentStage, false, relClassLabels, defaultClassLabel);
		FileUtility.writeInFile("tmp/temp", trainingDataJSreInpFormat, false);
		
		//-- call jSre to train single model
		Train.main(("-k " + kernelName + " " 
				+ "tmp/temp"  + " out/model_" + suf).split("\\s+"));

		//-- call jSre to tag test data using single model
		String strPredOut = Predict.getPredictionResult((testDataJSreFileName + " out/model_" + suf +
				"  out/output_" + suf).split("\\s+"));
		
		FileUtility.writeInFile(MultiStageRE.evalResultFileName, "\n\n" + TextUtility.now() + "\n" + strPredOut + "\n\n", true);

		//-- Separate test sentences by labelled relation types
		String[] testSreFilesSeparated = clsMultiStageRE.separateSrePredOfTestInputByRel(relTypesForCurrentStage, 
				FileUtility.readFileContents(testDataJSreFileName), 
				"_out_pred", FileUtility.readFileContents("out/output_" + suf), 0 );
	
		//-- merge output of the upper half relations in the final output file
		for ( int i=0; i<relTypesForCurrentStage.length-1; i++  )
			clsMultiStageRE.writeSemevalFormatOutput( null, combinedOutputFile, true, 
					testSreFilesSeparated[i], relTypesForCurrentStage[i][0], false);
			
		String lastFile = multiStageClassification_by_unfolding_half_each_stage(tempTrainingFiles, relTypesWithoutDefault, 
				1, relTypesWithoutDefault.length/2, "SL", level+1, 
				testSreFilesSeparated[testSreFilesSeparated.length-1], relTypesWithoutDefault.length, combinedOutputFile);
		
		
		clsMultiStageRE.writeSemevalFormatOutput( null, combinedOutputFile, true, 
			lastFile, relTypesWithoutDefault[relTypesWithoutDefault.length-1][0], false);
		
		return null;
	}
	
	/**
	 * 
	 * @param relTypesSortedByDiffiulty
	 * @param mergeNoOfRel
	 * @return
	 */
	public String[][] createHiererchyOfRelsOfPrevStage( String[][] relTypesSortedByDiffiulty, int mergeNoOfRel ){
		//-- Group relations of the lower half in to one meta relation.
		int newLen = relTypesSortedByDiffiulty.length + 1 - mergeNoOfRel;
		String[][] relTypesEarlierStage = new String[newLen][];
		
		for ( int i=0; i<newLen-1; i++ )
			relTypesEarlierStage[i] = relTypesSortedByDiffiulty[i]; 
		
		ArrayList<String> listTemp = new ArrayList<String>();
				
		for ( int i=newLen-1; i<relTypesSortedByDiffiulty.length; i++ )
			for ( int k=0; k<relTypesSortedByDiffiulty[i].length; k++ )
				listTemp.add(relTypesSortedByDiffiulty[i][k]);
		
		relTypesEarlierStage[newLen-1] = new String[listTemp.size()];
		for ( int i=0; i<listTemp.size(); i++ )
			relTypesEarlierStage[newLen-1][i] = listTemp.get(i);
		
		return relTypesEarlierStage;
	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public void trainAndPredict( String tokenizedTrainingFileName, 
			String[][] relTypesCoarseSortedByDifficulty,
			String kernelName, String tokenizedTestFileName, int mergeNoOfRel ) throws Exception{
		
		String combinedOutputFile = "out/output_single";
		FileUtility.writeInFile(combinedOutputFile, "", false);
		String testDataJSreFileName = FormatConverterJSRE.createJSreInputFiles(tokenizedTestFileName, 
				relTypesCoarseSortedByDifficulty, false, false, 1, DEFAULT_REL)[0];
		
		MultiStageRE clsMultiStageRE = new MultiStageRE();
		
		String[] tokenizedTrainingDataByRelFileNames = new SemEvalDataProcessor().
		//createBalancedCorpusByElimination(
		createBalancedCorpusByReplication(
			//clsMultiStageRE.separateTokenizedTrainingDataByRel(
					tokenizedTrainingFileName, relTypesCoarseSortedByDifficulty);
			
		clsMultiStageRE.mapOrigSenIdWithSysGenSenId(tokenizedTestFileName);
		
		String lastFile = multiStageClassification_by_unfolding_half_each_stage(
				tokenizedTrainingDataByRelFileNames, relTypesCoarseSortedByDifficulty, 
				mergeNoOfRel, relTypesCoarseSortedByDifficulty.length/2, kernelName, 1, 
				testDataJSreFileName, relTypesCoarseSortedByDifficulty.length, combinedOutputFile);
		
		clsMultiStageRE.writeSemevalFormatOutput( null, combinedOutputFile, true, 
				lastFile, relTypesCoarseSortedByDifficulty[relTypesCoarseSortedByDifficulty.length-1][0], false);
	}
	
	static String DEFAULT_REL = "Other"; 
	
}
