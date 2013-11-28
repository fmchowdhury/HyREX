package DataProcessor;

import java.io.IOException;
import java.util.ArrayList;

import Utility.FileUtility;
import Utility.TextUtility;

public class MergeMultiClassOutput {
	
	public static void main( String[] args ) throws Exception{
	
		//produceGoldOutputFile(args);
		produceSemEval2013OutputFile(args);
	}

	

	public static void produceGoldOutputFile ( String[] args ) throws IOException {	
		args = (" -all  ../../data/SemEval2013/task-9-ddi/Train/all_train_pairs  -allPosi ../../data/SemEval2013/task-9-ddi/Train/interaction_all  ").split("\\s+");
		
		ArrayList<String[]> listOfMultiClassOutputFiles = new ArrayList<String[]>();
		String allPositivePredFile = "", allInstanceFile = ""; 
		
		ArrayList<String> listPredClassLabels = new ArrayList<String>();
		
		for ( int i=0; i<args.length; i++ ) {
			if ( args[i].equalsIgnoreCase("-all") )
				allInstanceFile = args[i+1];
			else if ( args[i].equalsIgnoreCase("-allPosi") )
				allPositivePredFile = args[i+1];
			else if ( args[i].startsWith("-") ) {
				listOfMultiClassOutputFiles.add(new String[]{ args[i].substring(1), args[i+1] });
			}
		}
				
		ArrayList<String> listOfPredictedPositiveInstances = new ArrayList<String>();
		if ( !TextUtility.isEmptyString(allPositivePredFile) ) {
			ArrayList<String> listTemp = FileUtility.readNonEmptyFileLines(allPositivePredFile);
			
			for ( int i=0; i<listTemp.size(); i++ ) {
				String[] str = listTemp.get(i).split("\\s+");
				listOfPredictedPositiveInstances.add( str[0] + "|" + str[1]);
				
				listPredClassLabels.add(str[2]);
			}
		}
		
		StringBuilder sb = new StringBuilder();
				
		if ( !TextUtility.isEmptyString(allPositivePredFile) ) {
			// read all relation instance mentioned inside the data
			ArrayList<String> listTemp = FileUtility.readNonEmptyFileLines(allInstanceFile);
		
			for ( int v=0; v<listTemp.size(); v++ ) {
				String pair = listTemp.get(v);
				String senID = pair.split("\\|")[0];
				senID = senID.substring(0, senID.lastIndexOf("."));				
				
				int pi = listOfPredictedPositiveInstances.indexOf(pair);
				
				if ( pi > -1 )
					sb.append(senID + "|" + pair + "|1|" + listPredClassLabels.get(pi) + "\n");
				else
					sb.append(senID + "|" + pair + "|0|null\n");
			}
		}
		
		FileUtility.writeInFile("task9.2-train-gold.txt", sb.toString(), false);
	}

	
	
	public static void produceSemEval2013OutputFile ( String[] args ) throws IOException {	
		args = (" -all  ../../data/SemEval2013/task-9-ddi/test_data/task9.2/all_test_pairs  -allPosi out/extracted_relations.txt  " +
				" -advise DDI_2013_submission/out_advise/extracted_relations.txt  -effect DDI_2013_submission/out_effect/extracted_relations.txt  " +
				" -mechanism DDI_2013_submission/out_mechanism/extracted_relations.txt  -int DDI_2013_submission/out_int/extracted_relations.txt ").split("\\s+");
		
		ArrayList<String[]> listOfMultiClassOutputFiles = new ArrayList<String[]>();
		String allPositivePredFile = "", allInstanceFile = ""; 
		
		ArrayList<String> listPredClassLabels = new ArrayList<String>();
		ArrayList<Double> listPredScores = new ArrayList<Double>();
		
		for ( int i=0; i<args.length; i++ ) {
			if ( args[i].equalsIgnoreCase("-all") )
				allInstanceFile = args[i+1];
			else if ( args[i].equalsIgnoreCase("-allPosi") )
				allPositivePredFile = args[i+1];
			else if ( args[i].startsWith("-") ) {
				listOfMultiClassOutputFiles.add(new String[]{ args[i].substring(1), args[i+1] });
			}
		}
				
		ArrayList<String> listOfPredictedPositiveInstances = new ArrayList<String>();
		if ( !TextUtility.isEmptyString(allPositivePredFile) ) {
			ArrayList<String> listTemp = FileUtility.readNonEmptyFileLines(allPositivePredFile);
			
			for ( int i=0; i<listTemp.size(); i++ ) {
				String[] str = listTemp.get(i).split("\\s+");
				listOfPredictedPositiveInstances.add( str[1] + "|" + str[2]);
				
				// default
				listPredClassLabels.add("effect");
				listPredScores.add(-100.0);
			}
		}
		
		// for each class label
		for ( int f=0; f<listOfMultiClassOutputFiles.size(); f++ ) {
			ArrayList<String> listTemp = FileUtility.readNonEmptyFileLines(listOfMultiClassOutputFiles.get(f)[1]);
			String label = listOfMultiClassOutputFiles.get(f)[0];
			double score = 0.0;
			
			for ( int k=0; k<listTemp.size(); k++ ) {
				String[] str = listTemp.get(k).split("\\s+");
				score = Double.valueOf(str[0]);
				
				// locate the instance
				int pi = listOfPredictedPositiveInstances.indexOf(str[1] + "|" + str[2]);
				if ( pi > -1 ) {
					// check if the predicted score for the current class is higher than 
					//  the score of the previous assigned class 
					if ( listPredScores.get(pi) < score ) {
						listPredClassLabels.set(pi, label);
						listPredScores.set(pi, score);
					}
				}
				else {
					System.out.println(label + " " + listTemp.get(k));
				}
			}	
		}
		
		StringBuilder sb = new StringBuilder();
				
		if ( !TextUtility.isEmptyString(allPositivePredFile) ) {
			// read all relation instance mentioned inside the data
			ArrayList<String> listTemp = FileUtility.readNonEmptyFileLines(allInstanceFile);
		
			for ( int v=0; v<listTemp.size(); v++ ) {
				String pair = listTemp.get(v);
				String senID = pair.split("\\|")[0];
				senID = senID.substring(0, senID.lastIndexOf("."));				
				
				int pi = listOfPredictedPositiveInstances.indexOf(pair);
				
				if ( pi > -1 )
					sb.append(senID + "|" + pair + "|1|" + listPredClassLabels.get(pi) + "\n");
				else
					sb.append(senID + "|" + pair + "|0|null\n");
			}
		}
		
		FileUtility.writeInFile("task9.2-FBK-irst-3.txt", sb.toString(), false);
	}
}
