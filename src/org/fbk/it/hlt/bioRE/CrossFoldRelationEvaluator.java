package org.fbk.it.hlt.bioRE;
import java.text.DecimalFormat;

import org.itc.irst.tcc.sre.Predict;
import org.itc.irst.tcc.sre.Train;

import Utility.FileUtility;


class PredResultPerClass{ 
	int tp = 0, fp = 0, fn = 0, total = 0;
	double prec = 0.0, recall = 0.0, F1 = 0.0, acc = 0.0;
}

public class CrossFoldRelationEvaluator {


	public static void call(String[] arrKernelCombinations, int noFolds, int noMultiLineEntriesPerSen,
			String tokenizedSenFileName, String evalResultFileName,
			String[][] relTypes) throws Exception {
		/** 
		 * 	call the fold creator and create 10 folds of sen indexes				 
			create sre files for each relType
			set array of different kernel combination
			for each relType sre file 
			- use the 10 sen index folds to create actual sre file folds
			- for each kernel combination			
			-- call jSre to train with appropriate parameters of the config file
			-- call jSre to test with appropriate parameters of the config file
		*/

		
		
		String[] sreFiles = null; 
			
		//separateSenByRel(tokenizedSenFile, relTypesFine, relTypesCoarse, relArgsFine);
		//*
		//-- call the fold creator and create 10 folds of sen indexes		
		FoldCreator clsFoldCreator = new FoldCreator(tokenizedSenFileName, noFolds, noMultiLineEntriesPerSen);
		
		/*
		Utility.writeInFile(evalResult, "Considering direction\n", true);
		//-- create sre files for each relType
		sreFiles = FormatConverterJSRE.createJSreInputFiles(tokenizedSenFile, Loader.relTypesCoarse, true, false, true);
		
		//-- for each relType sre file 
		for ( int i=0; i<sreFiles.length; i++ )			
			doCrossFoldExp(sreFiles[i], arrKernelCombinations, tokenizedSenFile, evalResult, Loader.relTypesCoarse.length,
					clsFoldCreator, noMultiLineEntriesPerSen, noFolds);

		
		Utility.writeInFile(evalResult, "Considering direction\n", true);
		sreFiles = createJSreInputFiles(tokenizedSenFile, Loader.relTypesFine, true, true);
		
		//-- for each relType sre file 
		for ( int i=0; i<sreFiles.length; i++ )			
			doCrossFoldExp(sreFiles[i], arrKernelCombinations, tokenizedSenFile, evalResult, relTypesFine.length,
					clsFoldCreator, noMultiLineEntriesPerSen, noFolds);
		
		*/
		FileUtility.writeInFile(evalResultFileName, "Not considering direction\n", true);
		//-- create sre files for each relType
		sreFiles = FormatConverterJSRE.createJSreInputFiles(tokenizedSenFileName, relTypes, false, false, 1, Loader.DEFAULT_REL);
		
		//-- for each relType sre file 
		for ( int i=0; i<sreFiles.length; i++ )	
			doCrossFoldExp(sreFiles[i], arrKernelCombinations, tokenizedSenFileName, evalResultFileName, relTypes.length,
					clsFoldCreator, noMultiLineEntriesPerSen, noFolds);

		/*
		Utility.writeInFile(evalResult, "Not considering direction\n", true);
		sreFiles = FormatConverterJSRE.createJSreInputFiles(tokenizedSenFile, Loader.relTypesFine, false, true, true);
	
		//-- for each relType sre file 
		for ( int i=0; i<sreFiles.length; i++ )			
			doCrossFoldExp(sreFiles[i], arrKernelCombinations, tokenizedSenFile, evalResult, Loader.relTypesFine.length,
					clsFoldCreator, noMultiLineEntriesPerSen, noFolds);
				
		//*/
		//new String[]{"/media/Study/workspace/BioDigger/tmp/Member-Collection.sre"};
		
		
		//trainAndPredict("/media/Study/workspace/BioDigger/tmp/Member-Collection.sre", "/media/Study/workspace/BioDigger/tmp/Member-Collection.sre", "WNA");
	}
	

	
	private static void doCrossFoldExp( String sreFile, 
			String[] arrKernelCombinations, String tokenizedSenFile, String evalResult, int noCL, 
			FoldCreator clsFoldCreator, int noMultiLineEntriesPerSen, int noFolds ) throws Exception{
		
		DecimalFormat decFormatter = new DecimalFormat("0.000");
		
		noMultiLineEntriesPerSen = 1;			
	
		FileUtility.writeInFile(evalResult, 
				"\n\n--------------------\nDoing 10-fold cross validation on "
				+ sreFile + "\n--------------------\n", true);
			
		//-- use the 10 sen index folds to create actual sre file folds
		if ( clsFoldCreator != null )
			clsFoldCreator.splitDataInto_n_Folds(sreFile, null, noFolds, noMultiLineEntriesPerSen, false);
		
		//-- for each kernel combination
		for ( int k=0; k<arrKernelCombinations.length; k++ ){
		
			FileUtility.writeInFile(evalResult, 
					"\nKernel : " + arrKernelCombinations[k] + "\n\n", true);
			
			PredResultPerClass[] clsPredResultPerClass = new PredResultPerClass[noCL+1];
			
			for ( int cl=0; cl <= noCL; cl++ )
				clsPredResultPerClass[cl] = new PredResultPerClass();
			
			for ( int fold=0; fold < noFolds; fold++ ){
		
				String trainingFile = sreFile + "_train",
					testFile = sreFile + "_" + fold;
		
				FileUtility.writeInFile(trainingFile, "", false);
				//-- merge files to create training file
				for ( int nextMerge=0; nextMerge < noFolds; nextMerge++ )
					if ( nextMerge != fold )
						FileUtility.mergeFiles(trainingFile, sreFile + "_" + nextMerge, trainingFile);
								
				//-- call jSre to train
				Train.main(("-k " + arrKernelCombinations[k] + " " 
						+ trainingFile + " model" + arrKernelCombinations[k]).split("\\s"));
						
				//-- call jSre to test
				String predOutput = Predict.getPredictionResult((testFile + " model" + arrKernelCombinations[k] + " output" +
						arrKernelCombinations[k]).split("\\s"));
				
				//-- get formatted output
				String[] temp = predOutput.trim().split("\\n");
				
				for ( int t = 1; t < temp.length; t++ ){
					String[] str = temp[t].split("\\t");
					int classLabel = str[0].contains("micro") ? 0 : Integer.valueOf(str[0].trim());
					
						clsPredResultPerClass[classLabel].tp += Integer.valueOf(str[1].trim());
						clsPredResultPerClass[classLabel].fp += Integer.valueOf(str[2].trim());
						clsPredResultPerClass[classLabel].fn += Integer.valueOf(str[3].trim());
						clsPredResultPerClass[classLabel].total += Integer.valueOf(str[4].trim());
						clsPredResultPerClass[classLabel].prec += Double.valueOf(str[5].trim());
						clsPredResultPerClass[classLabel].recall += Double.valueOf(str[6].trim());
				}
			}
		
			FileUtility.writeInFile(evalResult, 
					"c\ttp\tfp\tfn\ttotal\tprec\trecall\tF1\n", true);
			
			int ttp=0, tfp=0, tfn=0;
			double tprec=0, trec=0;
			
			int add = 0;
			
			for ( int cl = 1; cl <= noCL; cl++ ){
				
				if ( add == 1 && noCL > 10 )
					FileUtility.writeInFile(evalResult,
							  "\n[" + (cl/2+1) + "]\t" + (clsPredResultPerClass[cl].tp+clsPredResultPerClass[cl+1].tp) 
							  + "\t" + (clsPredResultPerClass[cl].fp+clsPredResultPerClass[cl+1].fp) 
							+ "\t" + (clsPredResultPerClass[cl].fn + clsPredResultPerClass[cl+1].fn)
							+ "\t" + clsPredResultPerClass[cl].total
							 + "\t" + decFormatter.format(((clsPredResultPerClass[cl].prec+clsPredResultPerClass[cl+1].prec)/10)) 
							 + "\t" + decFormatter.format(((clsPredResultPerClass[cl].recall+clsPredResultPerClass[cl+1].recall)/10))
							 + "\t" + decFormatter.format(((2 * ((clsPredResultPerClass[cl].prec+clsPredResultPerClass[cl+1].prec)/20) * 
									 ((clsPredResultPerClass[cl].recall+clsPredResultPerClass[cl+1].recall)/20)) 
									 / ((clsPredResultPerClass[cl].prec+clsPredResultPerClass[cl+1].prec)/20 + (clsPredResultPerClass[cl].recall+clsPredResultPerClass[cl+1].recall)/20)))
							+ "\n\n", true);
						
				FileUtility.writeInFile(evalResult,
					cl + "\t" + clsPredResultPerClass[cl].tp + "\t" + clsPredResultPerClass[cl].fp 
					+ "\t" + clsPredResultPerClass[cl].fn + "\t" + clsPredResultPerClass[cl].total
					 + "\t" + decFormatter.format((clsPredResultPerClass[cl].prec/10)) + "\t" + decFormatter.format((clsPredResultPerClass[cl].recall/10))
					 + "\t" + decFormatter.format(((2 * (clsPredResultPerClass[cl].prec/10) * (clsPredResultPerClass[cl].recall/10)) 
							 / (clsPredResultPerClass[cl].prec/10 + clsPredResultPerClass[cl].recall/10)))
					+ "\n", true);

				if ( add == 1 )
					add = 0;
				else
					add = 1;
				
				ttp += clsPredResultPerClass[cl].tp;
				tfp += clsPredResultPerClass[cl].fp;
				tfn += clsPredResultPerClass[cl].fn; 
				tprec += clsPredResultPerClass[cl].prec;
				trec += clsPredResultPerClass[cl].recall;
			}
			
			tprec = tprec / (noCL * noFolds);
			trec = trec / (noCL * noFolds);
			
			FileUtility.writeInFile(evalResult,
					"\n\t" + ttp + "\t" + tfp 
					+ "\t" + tfn + "\t\t" + decFormatter.format(tprec) + "\t" + decFormatter.format(trec)
					 + "\t" + decFormatter.format((2 * tprec * trec) 
							 / (tprec + trec))
					+ "\n", true);
			
			
		}
		
		System.out.println(Loader.totalRel);
	}
	
}
