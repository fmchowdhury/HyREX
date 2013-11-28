package DataProcessor;

import java.io.IOException;

import Utility.FileUtility;


public class BioInferDataProcessor {

	public static void main ( String args[]) throws Exception{
		/*
		new UnifiedFormatDataProcessor().extractAndMergeText("/media/Study/data/BioInfer/bioinfer-1.2.0b-unified-format.xml", 
				"/media/Study/data/BioInfer/bioinfer.txt",
				"/media/Study/data/BioInfer/bioinferFull.txt");
		new UnifiedFormatDataProcessor().addAbstractIdWtihData("/media/Study/data/BioInfer/bioinferFull.txt");
		
		/**
		 * -- NOTE: This line is required as we need to indicate the parser that a full stop
		 * at the end of sentence is not part of an abbreviation.
		 */
		
		//Utility.addExtraSpaceBeforeSenEnding("/media/Study/data/BioInfer/bioinfer.txt");
		
		new BioInferDataProcessor().createFolds();
	}
	
	
	public void createFolds () throws IOException {
		//String fileName = "/media/Study/data/BioInfer/folds/fold_";
		//String[] filePref = new String[] {"train-203-", "test-203-"};
		
		for ( int i=1; i<=10; i++ ) {
			StringBuilder sbTrain = new StringBuilder(), sbTest = new StringBuilder();;
			int x = 0;
			
			for ( int k=1; k<=10; k++ ){
				int c = 4;
				if ( k>7 )
					c++;
				for ( int d=0; d<c; d++ ) {
					
					if ( i != k )
						sbTrain.append("HPRD50.d" + x + "\n");
					else
						sbTest.append("HPRD50.d" + x + "\n");
					x++;
				}
			}
				
			FileUtility.writeInFile( "../../data/converted_PPI_corpora/hprd50/folds/train-203-" + i,
					sbTrain.toString(), false);
					
			FileUtility.writeInFile( "../../data/converted_PPI_corpora/hprd50/folds/test-203-" + i,
					sbTest.toString(), false);
			
		}
	}
}
