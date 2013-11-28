package Kernels;

import java.io.File;
import java.util.ArrayList;

import negation.NegatedSentenceAnalyser;

import Clause.*;
import DataProcessor.*;
import Structures.*;
import Utility.*;

public class TKOutputGenerator {
			
	public static boolean isDT = false, isPST = false, isJSRE = false, isTPWF = false, isWeka = false,
			isUseZhou2005 = false;
	
	public static String jsreKernel = "SL", evalType = "", trainingData = "",
		testData = "", trainingParsedData = "", testParsedData = "", 
		trainClauseBoundFileName = "", testClauseBoundFileName = "", modelFileName = "";
	public static int medtType = 6;
	public static int  tr = 0, nFolds = 0;
	public static ClauseAnalyser.eDataFilterOption relToBeConsideredForTest = ClauseAnalyser.eDataFilterOption.DATA_ALL,
		relToBeConsideredForTrain = ClauseAnalyser.eDataFilterOption.DATA_ALL;
	
	public static String triggerFileName = "";
	
	public static boolean isTestData = false, classifySentences = false;
	
	public static boolean returnRecall = false;
	
	public static boolean isConsiderNeCat= false;
	public static boolean isBlindEntity = false;
	
	public static boolean doNotPrepareData = false;
	
	public static String 
		regExFirstRelArgType = "", regExSecondRelArgType = "", 
		regExRelArgType = "",
			regExTypeOfRel = "",
			subTypeRelConst = "";
	
	public static ArrayList<String> listOfSenIdsToIgnore = new ArrayList<String>();
		
	public static void main( String[] args ) throws Exception{
	
		//args = "-pst -jsre -wv -kjsre SL -medtType 6 -trainParse /media/Study/workspace/HyREX/sample-data/sample.parsed.bllip.complete -train /media/Study/workspace/HyREX/sample-data/sample.full -testParse /media/Study/workspace/HyREX/sample-data/sample.parsed.bllip.complete -train /media/Study/workspace/HyREX/sample-data/sample.full -nf 1 -trigFile /media/Study/workspace/HyREX/db/ddi_trigger".split("\\s+");
		
		String foldFilesFolder = "",
		evalOutFileName = "";
		
		for ( int i=0; i<args.length; i++ ) {
			if ( args[i].equalsIgnoreCase("-recall") )
				returnRecall = true;
			
			else if ( args[i].equalsIgnoreCase("-doNotPrepareData") )
				doNotPrepareData = true;
			
			else if ( args[i].equalsIgnoreCase("-classifySentences") )
				classifySentences = true;
				
			else if ( args[i].equalsIgnoreCase("-ev") )
				evalType = args[i+1];
			else if ( args[i].equalsIgnoreCase("-model") )
				modelFileName = args[i+1];
			else if ( args[i].equalsIgnoreCase("-dt") )
				isDT = true;
			else if ( args[i].equalsIgnoreCase("-pst") )
				isPST = true;
			else if ( args[i].equalsIgnoreCase("-jsre") )
				isJSRE = true;
			else if ( args[i].equalsIgnoreCase("-wv") )
				isTPWF = true;
			else if ( args[i].equalsIgnoreCase("-kjsre") )
				jsreKernel = args[i+1];
		
			else if ( args[i].equalsIgnoreCase("-zhou2005") )
				isUseZhou2005 = true;
			
			else if ( args[i].equalsIgnoreCase("-foldFilesFolder") )
				foldFilesFolder = args[i+1];
		
			else if ( args[i].equalsIgnoreCase("-nf") )
				nFolds = Integer.valueOf(args[i+1]);
		
			else if ( args[i].equalsIgnoreCase("-trigFile") )
				triggerFileName = args[i+1];
		
			else if ( args[i].equalsIgnoreCase("-evalOutFile") )
				evalOutFileName = args[i+1];
			else if ( args[i].equalsIgnoreCase("-medtType") )
				medtType = Integer.valueOf(args[i+1]);
			
			else if ( args[i].equalsIgnoreCase("-train") )
				trainingData = args[i+1];
			else if ( args[i].equalsIgnoreCase("-test") )
				testData = args[i+1];
			else if ( args[i].equalsIgnoreCase("-trainParse") )
				trainingParsedData = args[i+1];
			else if ( args[i].equalsIgnoreCase("-testParse") )
				testParsedData = args[i+1];
		
			else if ( args[i].equalsIgnoreCase("-cbTrain") )
				trainClauseBoundFileName = args[i+1];
		
			else if ( args[i].equalsIgnoreCase("-cbTest") )
				testClauseBoundFileName = args[i+1];
		
			else if ( args[i].equalsIgnoreCase("-icrTrain") )
				relToBeConsideredForTrain = ClauseAnalyser.eDataFilterOption.DATA_WITH_INTRA_CLAUSAL_REL;
			else if ( args[i].equalsIgnoreCase("-ccrTrain") )
				relToBeConsideredForTrain = ClauseAnalyser.eDataFilterOption.DATA_WITH_CROSS_CLAUSAL_REL;
		
			else if ( args[i].equalsIgnoreCase("-icrTest") )
				relToBeConsideredForTest = ClauseAnalyser.eDataFilterOption.DATA_WITH_INTRA_CLAUSAL_REL;
			else if ( args[i].equalsIgnoreCase("-ccrTest") )
				relToBeConsideredForTest = ClauseAnalyser.eDataFilterOption.DATA_WITH_CROSS_CLAUSAL_REL;
			
			else if ( args[i].equalsIgnoreCase("-rgxArgType") )
				regExRelArgType = args[i+1]; 
			else if ( args[i].equalsIgnoreCase("-rgxRelType") ) {
				regExTypeOfRel = args[i+1];
				Relation.setDefaultRelType(regExTypeOfRel);
			}
			
			else if ( args[i].equalsIgnoreCase("-relSubType") ) 
				subTypeRelConst = args[i+1];			
			
			else if ( args[i].equalsIgnoreCase("-reg1stArgType") )
				regExFirstRelArgType = args[i+1]; 
			else if ( args[i].equalsIgnoreCase("-reg2ndArgType") )
				regExSecondRelArgType = args[i+1];		
				
			else if ( args[i].equalsIgnoreCase("-weka") )
				isWeka = true;			
		}
		

		if ( classifySentences )
			FileUtility.writeInFile(CommonUtility.OUT_DIR + "/allSenIdsForTest", "", false);
		
		// CommonUtility.calculateAUC( "", CommonUtility.OUT_DIR + "/best.base.stat.in", "");
		//CommonUtility.calculateAUC( trainingData, CommonUtility.OUT_DIR + "/best.base.stat.in",
			//	 CommonUtility.OUT_DIR + "/entPairFileName_TK");

		ArrayList<Sentence> listOfAllSentences = new ArrayList<Sentence>();
		
		if ( isPST || isDT || isJSRE || isTPWF || isWeka || isUseZhou2005 || classifySentences ) {
			if ( FileUtility.isFileExists(CommonUtility.OUT_DIR + "/sentence_predictions.txt") )
				listOfSenIdsToIgnore = FileUtility.readNonEmptyFileLines(CommonUtility.OUT_DIR + "/sentence_predictions.txt");
			
			//-- prepare data
			String trainDepParsedFile="", trainPsgParsedFile="", testDepParsedFile="", testPsgParsedFile="";
				
			if ( !trainingParsedData.isEmpty() ) {
				trainDepParsedFile = CommonUtility.OUT_DIR + trainingParsedData.substring(trainingParsedData.lastIndexOf("/"))  + "_dt";
				trainPsgParsedFile = CommonUtility.OUT_DIR + trainingParsedData.substring(trainingParsedData.lastIndexOf("/")) + "_psg";
		
				listOfAllSentences = Sentence.readFullData( trainingData, "", "", CommonUtility.OUT_DIR + "/allEntPair", false);
				
				if ( !doNotPrepareData )
					trainClauseBoundFileName = new TKOutputGenerator().prepareData(trainingParsedData, listOfAllSentences, trainClauseBoundFileName);
			}
						
			if ( !testParsedData.isEmpty() ) {
				listOfAllSentences = Sentence.readFullData(testData, "", "", CommonUtility.OUT_DIR + "/allEntPair", false);
				
				testDepParsedFile = CommonUtility.OUT_DIR + testParsedData.substring(testParsedData.lastIndexOf("/")) + "_dt";
				testPsgParsedFile = CommonUtility.OUT_DIR + testParsedData.substring(testParsedData.lastIndexOf("/")) + "_psg";
				
				testClauseBoundFileName = new TKOutputGenerator().prepareData(testParsedData, listOfAllSentences, testClauseBoundFileName);
			}			
			
			// System.out.println(relToBeConsidered);					

			//new Common().readTokenPhrasalCategory();
			System.out.println("pst=" + isPST + ", dt=" + isDT + ", jsre=" + isJSRE + ", wv=" + isTPWF);			

			FileUtility.writeInFile( CommonUtility.OUT_DIR + "/allEntPair", "", false);
			
			String outputDir = foldFilesFolder.isEmpty() ? CommonUtility.OUT_DIR : CommonUtility.OUT_DIR + "/tk";
			FileUtility.createDirectory(CommonUtility.OUT_DIR + "/tk" );
			
			if ( !trainingData.isEmpty() && testData.isEmpty() ) {
				
				new TKOutputGenerator().crossFoldValidation(foldFilesFolder, trainDepParsedFile, trainPsgParsedFile, 
						listOfAllSentences, outputDir, isPST, isDT, isJSRE, jsreKernel,
					 trainClauseBoundFileName, isTPWF, 
					isWeka, nFolds, classifySentences, isUseZhou2005);
			}
				
			else if ( (!trainingData.isEmpty() || !modelFileName.isEmpty() 
					|| new File(CommonUtility.OUT_DIR + "/model").exists() ) && !testData.isEmpty() ) {
				
				new TKOutputGenerator().trainAndTest(modelFileName, trainingData, testData, trainDepParsedFile, trainPsgParsedFile,
						testDepParsedFile, testPsgParsedFile, outputDir, isPST, isDT, isJSRE, isTPWF, jsreKernel, 
						 trainClauseBoundFileName, testClauseBoundFileName, isWeka, classifySentences, isUseZhou2005);
			}			
		}
		
		if ( evalType.equalsIgnoreCase("tk") )
			CommonUtility.calculateCrossFoldResult(evalOutFileName, returnRecall);
		else if ( evalType.equalsIgnoreCase("full") )
			CommonUtility.calculateCrossFoldResult_full( trainingData, CommonUtility.OUT_DIR + "/best.base.stat.in",
					 CommonUtility.OUT_DIR + "/entPairFileName_TK", evalOutFileName);
		else if ( !evalOutFileName.isEmpty() )
			CommonUtility.calculateCrossFoldResult_oth(evalOutFileName);
	}
	
		
	/**
	 * 	
	 * @param parsedData
	 * @param fullData
	 * @param inClauseBoundFileName
	 * @throws Exception 
	 */
	public String prepareData( String parsedData, ArrayList<Sentence> listOfAllSentences, String inClauseBoundFileName ) throws Exception{
		
		//FileUtility.createTmpFile(inClauseBoundFileName + "_merged");
		
		new UnifiedFormatDataProcessor().mergeSepratedParsedPartsOfSameSentences( parsedData, listOfAllSentences);
				
		if ( !inClauseBoundFileName.isEmpty() ) {
			
			//FileUtility.createTmpFile(inClauseBoundFileName + "_corrected");
			//FileUtility.createTmpFile(inClauseBoundFileName + "_boun");
			
			new EduPostProcessor().fixEduBoundariesOfAllSen(inClauseBoundFileName, inClauseBoundFileName + "_corrected");
			inClauseBoundFileName = inClauseBoundFileName + "_corrected";
			
			new ClauseAnalyser().findClauseBoundariesOfAllSen(inClauseBoundFileName, 
					inClauseBoundFileName + "_boun", listOfAllSentences);
			inClauseBoundFileName = inClauseBoundFileName + "_boun";
		}
		
		return inClauseBoundFileName;
	}
	
	/**
	 * 
	 * @param fullTrainingDataFile
	 * @param fullTestDataFile
	 * @param outputFilesFolder
	 * @param isUsePST
	 * @param isUseDT
	 * @param isUseSL
	 * @param jsreKernel
	 * @param relToBeConsidered
	 * @param inClauseBoundFileName
	 * @throws Exception
	 */
	public void trainAndTest( String modelFileName, String fullTrainingDataFile, String fullTestDataFile, 
			String trainDepParsedFile, String trainPsgParsedFile, String testDepParsedFile, 
			String testPsgParsedFile, String outputFilesFolder,
			boolean isUsePST, boolean isUseDT, boolean isUseSL, boolean isUseTPWF, String jsreKernel,
			String trainClauseBoundFileName, String testClauseBoundFileName,
			boolean isWeka, boolean classifySentences, boolean isUseZhou2005
	) throws Exception{
		
		if ( modelFileName.isEmpty() && !fullTrainingDataFile.isEmpty() ) {
			createTkInpfile(fullTrainingDataFile, outputFilesFolder, trainDepParsedFile, trainPsgParsedFile, "train", 
				isUsePST, isUseDT, isUseSL, isUseTPWF, jsreKernel, trainClauseBoundFileName, 
				isWeka, classifySentences, isUseZhou2005,
				false, false,
				0);
			GenericFeatVect.writeFeaturesAndPatternsInFile();
		}
		else {
			if ( isUseTPWF )
				GenericFeatVect.readFeaturesAndPatternsInFile();
		}
		
		if ( !fullTestDataFile.isEmpty() )
			createTkInpfile(fullTestDataFile, outputFilesFolder, testDepParsedFile, testPsgParsedFile, "test", 
					isUsePST, isUseDT, isUseSL, isUseTPWF, jsreKernel, testClauseBoundFileName,
					isWeka, classifySentences, isUseZhou2005,
					true, true,
					0);
	}
	
	
	/**
	 * 
	 * @param fullDataFile
	 * @param outputFilesFolder
	 * @param depParsedFile
	 * @param psgParsedFile
	 * @param fileNamePrefix
	 * @param isTestFile
	 * @param isUsePST
	 * @param isUseDT
	 * @param isUseSL
	 * @param isUseTPWF
	 * @param jsreKernel
	 * @param relToBeConsidered
	 * @param inClauseBoundFileName
	 * @param writeEntPairs
	 * @param isTrainData
	 * @throws Exception
	 */
	private void createTkInpfile ( String fullDataFile, String outputFilesFolder, 
			String depParsedFile, String psgParsedFile, String fileNamePrefix, 
		boolean isUsePST, boolean isUseDT, boolean isUseSL, boolean isUseTPWF, String jsreKernel,
		String inClauseBoundFileName, boolean isWeka, boolean classifySentences, boolean isUseZhou2005,
		boolean writeEntPairs, boolean isTestFile, int fold
	) throws Exception{
		
		String modelZipjSRE = outputFilesFolder + "/train.BioRelEx.model";
				
		totalPosRel = 0;
		totalNegRel = 0;
				
		FileUtility.writeInFile(CommonUtility.EntPairFileName_TK, "", false);
		
		boolean isResolveOverlappingEntities = false;//j==0 ? true : false;
		
		trainAndClassify(fullDataFile, depParsedFile, psgParsedFile, outputFilesFolder, 
				fileNamePrefix, isUsePST, isUseDT, isUseSL, jsreKernel, inClauseBoundFileName, isUseTPWF, isWeka, classifySentences,
				isUseZhou2005, isTestFile, isResolveOverlappingEntities, modelZipjSRE, fold);	
	}
	
	
	int totalPosRel = 0, totalNegRel = 0;
	
	/**
	 * 
	 */
	private void updateRelCount () {
		totalPosRel = TKOutputPST.totalRelPos > totalPosRel ? TKOutputPST.totalRelPos : totalPosRel;
		totalNegRel = TKOutputPST.totalRelNeg > totalNegRel ? TKOutputPST.totalRelNeg : totalNegRel;
		TKOutputPST.totalRelPos = 0;
		TKOutputPST.totalRelNeg = 0;
	}
	
	/**
	 * 
	 * @param foldFilesFolder
	 * @param depParsedFileName
	 * @param aimedDataFileName
	 * @param psgParsedFileName
	 * @param outputFilesFolder
	 * @param isUsePST
	 * @param isUseDT
	 * @param isUseSL
	 * @throws Exception
	 */
	public void crossFoldValidation( String foldFilesFolder, String depParsedFileName, String psgParsedFileName, 
			ArrayList<Sentence> listOfAllSentences, String outputFilesFolder,
			boolean isUsePST, boolean isUseDT, boolean isUseSL, String jsreKernel, String inClauseBoundFileName,
			boolean isUseTPWF, boolean isWeka, 
			int nfolds, boolean classifySentences, boolean isUseZhou2005
	) throws Exception{
		
		double percentageOfTrainDataToBeUsed = 1.00; 
		
		if ( nfolds < 1 )
			nfolds = 10;
		
		FileUtility.writeInFile(CommonUtility.EntPairFileName_TK, "", false);
		
		
		String[] filePref = new String[] {"train-203-", "test-203-"};
			
		
		// separate train and test data
		// read train and test files for n-th fold
		for ( int i=1; i<=nFolds; i++ ){
					
			int fold = i;
			String modelZipjSRE = outputFilesFolder + "/" + filePref[0] + i + ".BioRelEx.model"; 
			
			ArrayList<DependencyParseOfSen> listDepParseOfAllSen = DependencyParseOfSen.readDepParseForAllSen(depParsedFileName);
			ArrayList<CFGParseOfSen> listCFGParseOfAllSen = CFGParseOfSen.readCFGParseForAllSen(psgParsedFileName);
		
			for ( int j=0; j<2; j++ ){
				totalPosRel = 0;
				totalNegRel = 0;
				
				boolean isTestFile = false;
				
				if ( j == 1 )
					isTestFile = true;
				
				System.out.println(TextUtility.now() + " Processing fold " + i + " " + (isTestFile ? "test" : "train"));
				
				String fileName = filePref[j] + i;
				ArrayList<String> listAbstracts = FileUtility.readFileLines(foldFilesFolder + "/" + fileName);
				boolean isResolveOverlappingEntities = false;//j==0 ? true : false;
				
				String tmpFullDataFileName = outputFilesFolder + "/" + fileName + ".tk.full";
				String tmpDepParsedFileName = outputFilesFolder + "/" + fileName + ".tk.parsed.dep";
				String tmpPstParsedFileName = outputFilesFolder + "/" + fileName + ".tk.parsed.pst";
				
				if ( !doNotPrepareData )
					new TKOutputDT().createFilesFor_i_th_Fold(listAbstracts, listDepParseOfAllSen, 
						listCFGParseOfAllSen, tmpFullDataFileName, tmpDepParsedFileName, tmpPstParsedFileName, 
						percentageOfTrainDataToBeUsed, listOfAllSentences);
				
				trainAndClassify(tmpFullDataFileName, tmpDepParsedFileName, tmpPstParsedFileName, outputFilesFolder, 
						fileName, isUsePST, isUseDT, isUseSL, jsreKernel,  inClauseBoundFileName, isUseTPWF, isWeka, classifySentences,
						isUseZhou2005, isTestFile, isResolveOverlappingEntities, modelZipjSRE, fold);				
			}
		}
	}

	/**
	 * 
	 * @param tmpFullDataFileName
	 * @param tmpDepParsedFileName
	 * @param tmpPstParsedFileName
	 * @param outputFilesFolder
	 * @param fileName
	 * @param isUsePST
	 * @param isUseDT
	 * @param isUseSL
	 * @param jsreKernel
	 * @param relToBeConsideredForTrain
	 * @param relToBeConsideredForTest
	 * @param inClauseBoundFileName
	 * @param isUseTPWF
	 * @param isWeka
	 * @param classifySentences
	 * @param isUseZhou2005
	 * @param isTestFile
	 * @param isResolveOverlappingEntities
	 * @param modelZipjSRE
	 * @param fold
	 * @throws Exception
	 */
	private void trainAndClassify ( String tmpFullDataFileName, String tmpDepParsedFileName, String tmpPstParsedFileName, 
			String outputFilesFolder, String fileName,
			boolean isUsePST, boolean isUseDT, boolean isUseSL, String jsreKernel, String inClauseBoundFileName,
			boolean isUseTPWF, boolean isWeka, 
			boolean classifySentences, boolean isUseZhou2005, 
			boolean isTestFile, boolean isResolveOverlappingEntities,
			String modelZipjSRE, int fold ) throws Exception {
		
		CommonUtility.EntPairFileName_JSRE = CommonUtility.OUT_DIR + "/entPairFileName_JSRE" + "_" + isTestFile + "_" + fold;
		CommonUtility.EntPairFileName_WV = CommonUtility.OUT_DIR + "/entPairFileName_WV"  + "_" + isTestFile + "_" + fold;
		CommonUtility.EntPairFileName_PST = CommonUtility.OUT_DIR + "/entPairFileName_PST"  + "_" + isTestFile + "_" + fold;
		
		FileUtility.writeInFile(CommonUtility.EntPairFileName_WV, "", false);
		FileUtility.writeInFile(CommonUtility.EntPairFileName_PST, "", false);
		FileUtility.writeInFile(CommonUtility.EntPairFileName_DT, "", false);
		FileUtility.writeInFile(CommonUtility.EntPairFileName_JSRE, "", false);
			
		ArrayList<String[]> listTkFilesAndKernelType = new ArrayList<String[]>();
		String bioRelExInpFile = null;
		
		{
			
			TKOutputPST.negSenIgnored = 0;
			TKOutputPST.listOfDiscardedNegatedSentence = new ArrayList<String>();
			
			isTestData = isTestFile;
			
			// if less informative sentences are to be separated
			if ( classifySentences ) {
				String tmpOutputFileName = outputFilesFolder + "/" + fileName + ".tk";
				
				new NegatedSentenceAnalyser().generateFeatureVectors( tmpPstParsedFileName, tmpFullDataFileName, 
						tmpOutputFileName, tmpDepParsedFileName);
				return;
			}
			
			ArrayList<Sentence> listOfSentenceOfCurrentFold = Sentence.readFullData( tmpFullDataFileName, tmpPstParsedFileName, tmpDepParsedFileName, "", false);
				
			if ( !isTestFile && PairFilterCriteria.antiPositiveGovernors )
				new CueDetector().extractAntiPositiveEntityGovernerWords(tmpFullDataFileName, tmpDepParsedFileName);
			
			if ( isUseDT || isUseSL || isUseTPWF || isUseZhou2005 ){
				
				if ( isUseZhou2005 ){
					if ( !isTestFile )
						ExtAceFeatVect.init();
					else
						GenericFeatVect.initForTestData();
					
					String tmpWVoutputFileName = outputFilesFolder + "/" + fileName + ".tk.wv";
					
					new ExtAceFeatVect().generateFeatureVectors( listOfSentenceOfCurrentFold, tmpWVoutputFileName, 
							!isTestFile ? relToBeConsideredForTrain : relToBeConsideredForTest,
									inClauseBoundFileName );
					
					listTkFilesAndKernelType.add(new String[]{tmpWVoutputFileName, "V", CommonUtility.EntPairFileName_WV});
					updateRelCount();
				}
				
				if ( isUseDT ){
					String tmpOutputFileName = outputFilesFolder + "/" + fileName + ".tk.dep";
					
					new TKOutputDT().generateTKoutput(Common.isSimplifyEntity, listOfSentenceOfCurrentFold, 
							tmpOutputFileName, medtType, isResolveOverlappingEntities,
							!isTestFile ? relToBeConsideredForTrain : relToBeConsideredForTest, 
							inClauseBoundFileName, CommonUtility.EntPairFileName_DT);
					listTkFilesAndKernelType.add(new String[]{tmpOutputFileName, "T", CommonUtility.EntPairFileName_DT});
					updateRelCount();
				}
				
				if ( isUseTPWF ){
					if ( !isTestFile )
						TPWF.init();
					else
						GenericFeatVect.initForTestData();
					
					String tmpWVoutputFileName = outputFilesFolder + "/" + fileName + ".tk.wv";
					
					new TPWF().generateTPWFvectorOutput( listOfSentenceOfCurrentFold, tmpWVoutputFileName, medtType, 
						CommonUtility.EntPairFileName_WV,
								!isTestFile ? relToBeConsideredForTrain : relToBeConsideredForTest, 
										inClauseBoundFileName );
					
					listTkFilesAndKernelType.add(new String[]{tmpWVoutputFileName, "V", CommonUtility.EntPairFileName_WV});
					updateRelCount();
				}
								
				if ( isUseSL ){
					bioRelExInpFile = outputFilesFolder + "/" + fileName + ".BioRelEx";
					new TKOutputSL().generateTKoutputForSL( listOfSentenceOfCurrentFold, 
							bioRelExInpFile, isResolveOverlappingEntities,
							!isTestFile ? relToBeConsideredForTrain : relToBeConsideredForTest, 
							inClauseBoundFileName, CommonUtility.EntPairFileName_JSRE, !isTestFile
									 );
		
					// if jSRE kernels (i.e., SL, LC or GC) are used
					if ( !isTestFile )
							FileUtility.writeInFile(bioRelExInpFile+".jsre.tk",  
								new FeatVecCreatorForSLkernel().getTrainFVforJsreKernel(bioRelExInpFile,
									jsreKernel, bioRelExInpFile+".jsre", modelZipjSRE),
									false);
						else
							FileUtility.writeInFile(bioRelExInpFile+".jsre.tk",
								new FeatVecCreatorForSLkernel().getTestFVforJsreKernel(bioRelExInpFile,
									jsreKernel, bioRelExInpFile+".jsre", modelZipjSRE),
									false);
					
					listTkFilesAndKernelType.add(new String[]{bioRelExInpFile + ".jsre.tk", "V", CommonUtility.EntPairFileName_JSRE});
					updateRelCount();
				}
			}
		
			if ( isUsePST ){
				String tmpOutputFileName = outputFilesFolder + "/" + fileName + ".tk.pst";					
				
				new TKOutputPST().generateTKoutputForPST( listOfSentenceOfCurrentFold, 
						tmpOutputFileName, isResolveOverlappingEntities,
						!isTestFile ? relToBeConsideredForTrain : relToBeConsideredForTest, 
						inClauseBoundFileName, CommonUtility.EntPairFileName_PST );
				
				listTkFilesAndKernelType.add(new String[]{tmpOutputFileName, "T", CommonUtility.EntPairFileName_PST});
				
				updateRelCount();
			}
			
			System.out.println("Posi: " +  totalPosRel + "  Neg: " + totalNegRel);
			System.out.println("Sentence with negations ignored: " +  TKOutputPST.negSenIgnored);
			
			if ( isTestFile )
				System.out.println("\n\n");
			
			FileUtility.writeInFile(outputFilesFolder + "/" + fileName + ".tk", 
					mergeDifferentTkOutputFiles(listTkFilesAndKernelType, 
							isTestFile).toString(), false);											
		}		

	}

	/**
	 * 
	 * @param listTkFilesAndKernelType
	 * @param isTestFile
	 * @return
	 * @throws Exception
	 */
	public String mergeDifferentTkOutputFiles ( ArrayList<String[]> listTkFilesAndKernelType, 
			boolean isTestFile) throws Exception{
		
		ArrayList<String> listAllEntPairs = new ArrayList<String>();
		
		// get all entity pairs for which at least one input is generated by one of the kernels
		for ( int i=0; i<listTkFilesAndKernelType.size(); i++ )
			listAllEntPairs = DataStrucUtility.mergeLists(listAllEntPairs, 
					FileUtility.readNonEmptyFileLines(listTkFilesAndKernelType.get(i)[2]));
		
		String[][] arrInpForAllPairs = new String[listAllEntPairs.size()][listTkFilesAndKernelType.size()+1];
		
		// initialize
		for ( int i=0; i<listAllEntPairs.size(); i++ ){
			for ( int k=0; k<listTkFilesAndKernelType.size()+1; k++ ){
				arrInpForAllPairs[i][k] = "";
			}
		}
		
		for ( int i=0; i<listTkFilesAndKernelType.size(); i++ ){
			String[] arrCurPairs = DataStrucUtility.listToStringArray(FileUtility.readNonEmptyFileLines(listTkFilesAndKernelType.get(i)[2]));
			String[] arrCurInps = new String[0];
			
			if ( listTkFilesAndKernelType.get(i)[1].equals("T") ) 
				arrCurInps = normalizeRedundantBrackets(FileUtility.
					readFileContents(listTkFilesAndKernelType.get(i)[0]).trim().split("\\n"));
			else
				arrCurInps = DataStrucUtility.listToStringArray(FileUtility.readFileLines(listTkFilesAndKernelType.get(i)[0]));
			
			// search for each pair and add the corresponding input in the corresponding column
			for ( int k=0; k<arrCurPairs.length; k++ ){
				if ( arrCurInps[k].length() > 2 ) {
					int z = listAllEntPairs.indexOf(arrCurPairs[k]);
					arrInpForAllPairs[z][0] = arrCurInps[k].substring(0,2).trim();
					arrInpForAllPairs[z][i+1] = arrCurInps[k].substring(2).trim().replaceAll("\\|[BE][TV]\\|", "");
				}
			}			
		}
		
		
		int firstTreeInputIndex = -1, firstVectInputIndex = -1;
		boolean moreThanTwoFV = false;
		
		// merge all tree kernel data
		for ( int i=0; i<listTkFilesAndKernelType.size(); i++ ){
			if ( listTkFilesAndKernelType.get(i)[1].equals("T") ) {
				firstTreeInputIndex = i+1;
				i++;
				for ( ; i<listTkFilesAndKernelType.size(); i++ ){
					if ( listTkFilesAndKernelType.get(i)[1].equals("T") ) {
						int curInpIndex = i+1;
						for ( int a=0; a<arrInpForAllPairs.length; a++ )			
							arrInpForAllPairs[a][firstTreeInputIndex] += " |BT| " + arrInpForAllPairs[a][curInpIndex];
					}
				}
			}
		}
				
		// merge all feature vector kernel data
		for ( int i=0; i<listTkFilesAndKernelType.size(); i++ ){
			if ( listTkFilesAndKernelType.get(i)[1].equals("V") ) {
				firstVectInputIndex = i+1;
				i++;
				for ( ; i<listTkFilesAndKernelType.size(); i++ ){
					if ( listTkFilesAndKernelType.get(i)[1].equals("V") ) {
						moreThanTwoFV = true;
						int curInpIndex = i+1;
						for ( int a=0; a<arrInpForAllPairs.length; a++ ) {
							arrInpForAllPairs[a][firstVectInputIndex] += " |BV| " + arrInpForAllPairs[a][curInpIndex];
						}
					}
				}
			}
		}
		
		String[] allLinesTkOne = new String[arrInpForAllPairs.length];
		
		if ( firstTreeInputIndex > -1 ) {
			for ( int a=0; a<arrInpForAllPairs.length; a++ )			
				allLinesTkOne[a] = arrInpForAllPairs[a][0] + 
						" |BT|" + arrInpForAllPairs[a][firstTreeInputIndex] + " |ET|";
		}
		
		if ( firstTreeInputIndex > -1 && firstVectInputIndex > -1 ) {
			for ( int a=0; a<arrInpForAllPairs.length; a++ )			
				allLinesTkOne[a] += " " + arrInpForAllPairs[a][firstVectInputIndex] + " |EV|";
		}
		else if ( moreThanTwoFV && firstVectInputIndex > -1 ) {
			for ( int a=0; a<arrInpForAllPairs.length; a++ )
				allLinesTkOne[a] = arrInpForAllPairs[a][0] + " " + arrInpForAllPairs[a][firstVectInputIndex] + " |EV|";
		}

		else if ( firstVectInputIndex > -1 ) {
			for ( int a=0; a<arrInpForAllPairs.length; a++ )
				allLinesTkOne[a] = arrInpForAllPairs[a][0] + " " + arrInpForAllPairs[a][firstVectInputIndex];
		}
		
		StringBuilder sbPairs = new StringBuilder(), sbOutLines = new StringBuilder();		
		// detecting whether there is any instance with empty feature set
		for ( int i=0; i<allLinesTkOne.length; i++ ){
			String temp = allLinesTkOne[i].replaceAll("\\|[BE][TV]\\|", "").replaceAll("\\s+", "")
					.replaceAll("\\|\\s+\\|EV\\|", "|");
						
			if ( temp.length() > 2 ){
				sbOutLines.append(allLinesTkOne[i].replaceAll("\\s+", " ")).append("\n");
	
				if ( listAllEntPairs.size() > 1 )
					sbPairs.append(listAllEntPairs.get(i)).append("\n");
			}
		}
		
		if ( listAllEntPairs.size() > 1 && isTestFile )
			FileUtility.writeInFile(CommonUtility.EntPairFileName_TK, sbPairs.toString(), true);		
		
		return sbOutLines.toString();
	}
	
	
	/**
	 * 
	 * @param output
	 * @return
	 */
	private String[] normalizeRedundantBrackets( String[] allLines ){
		
		for ( int i=0; i<allLines.length; i++ ){
			allLines[i] = allLines[i].replaceAll("\\s+", " ")
			.replaceAll("\\( \\(", "((").replaceAll(" \\)", ")").replaceAll("\\(\\)", "");			
		}
		
		return allLines;
	}


	
}
