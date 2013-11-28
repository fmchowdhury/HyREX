package Kernels;

import java.io.IOException;
import java.util.ArrayList;

import Utility.CommonUtility;
import Utility.DataStrucUtility;
import Utility.FileUtility;
import Utility.TextUtility;

public class GenericFeatVect {

	public static ArrayList<String> listOfGlobalFeatures = new ArrayList<String>();
	public static int[] arrOfGlobalFeaturesTotInstanceCount = new int[10000];
	public static ArrayList<Integer> listOfGlobalFeatWeight = new ArrayList<Integer>();
	
	public static ArrayList<Integer> listOfAllInstancePolarity = new ArrayList<Integer>();
	
	public static ArrayList<String> listOfAllInstances = new ArrayList<String>();
	
	public static ArrayList<ArrayList<Integer>> listOfAllInstancesWithFeat = new ArrayList<ArrayList<Integer>>();
	public static ArrayList<ArrayList<Integer>> listOfAllInstancesWithFeatCount = new  ArrayList<ArrayList<Integer>>();
	
	public static String featureFile = CommonUtility.OUT_DIR + "/vectFeatures";
	public static String patternFile = CommonUtility.OUT_DIR + "/vectPatterns";
	
	public static String vectOutFile = CommonUtility.OUT_DIR + "/all_vect_by_pair";
	
	public static int featInstanceCountThreshold = 1;
	
	/**
	 * 
	 * @throws IOException
	 */
	public static void init() throws IOException {
		PatternsDepRelFromGraph.listOfAllPatterns = new ArrayList<ArrayList<String>>();
		PatternsDepRelFromGraph.listOfLabelsForPatterns = new ArrayList<Boolean>();
		
		arrOfGlobalFeaturesTotInstanceCount = new int[10000];
		
		listOfGlobalFeatures = new ArrayList<String>();
		listOfGlobalFeatWeight = new ArrayList<Integer>();
		
		FileUtility.writeInFile( featureFile, "", false);
		FileUtility.writeInFile( patternFile, "", false);
		FileUtility.writeInFile( vectOutFile, "", false);
		
		listOfAllInstancePolarity = new ArrayList<Integer>();
		
		listOfAllInstancesWithFeat = new ArrayList<ArrayList<Integer>>();
		listOfAllInstancesWithFeatCount = new  ArrayList<ArrayList<Integer>>();
	}
		
	/**
	 * 
	 * @throws IOException
	 */
	public static void initForTestData() throws IOException {
		
		listOfAllInstancePolarity = new ArrayList<Integer>();
		
		listOfAllInstancesWithFeat = new ArrayList<ArrayList<Integer>>();
		listOfAllInstancesWithFeatCount = new  ArrayList<ArrayList<Integer>>();
	}
	
	
	/**
	 * Add new feature in local and global list if already doesn't exist. 
	 * 
	 * @param feature
	 * @param listFeatIndsOfCurInp
	 * @param listFeatCountOfCurInp
	 */
	public static void addNewFeatureInList ( String[] feature, int count,
			ArrayList<Integer> listFeatIndsOfCurInp, ArrayList<Integer> listFeatCountOfCurInp, int weight ) {
		
		int ind = -1;
		
		// NOTE: here all elements of the array feature[] actually represent the same feature with alternative names
		for ( int i=0; ind < 0 && i<feature.length; i++ ) {
			ind = listOfGlobalFeatures.indexOf(feature[i]);				
		}
		
		if ( ind < 0 ) {
			// we don't add any new feature from test data which is unseen in training data
			if ( TKOutputGenerator.isTestData )
				return;
			
			ind = listOfGlobalFeatures.size();
			listOfGlobalFeatures.add(feature[0]);
			//*
			if ( ind > arrOfGlobalFeaturesTotInstanceCount.length -1  ){
				int[] tmp = new int[arrOfGlobalFeaturesTotInstanceCount.length + 500];
				System.arraycopy(arrOfGlobalFeaturesTotInstanceCount, 0, tmp, 0, arrOfGlobalFeaturesTotInstanceCount.length);
				arrOfGlobalFeaturesTotInstanceCount = tmp;
			}
			
			arrOfGlobalFeaturesTotInstanceCount[ind] = count;
			//*/
			
			listOfGlobalFeatWeight.add(weight);
		}
		else
			arrOfGlobalFeaturesTotInstanceCount[ind]+=count;
		
		int indLoc = listFeatIndsOfCurInp.indexOf(ind);
		if ( indLoc < 0 ) {
			listFeatIndsOfCurInp.add(ind);
			listFeatCountOfCurInp.add(count);
		}
		else 
			listFeatCountOfCurInp.set(indLoc, listFeatCountOfCurInp.get(indLoc)+count);			
	}

	
	
	/**
	 * 
	 * @throws IOException
	 */
	public static void readFeaturesAndPatternsInFile () throws IOException {
		init();
		ArrayList<String> listOfLines = FileUtility.readNonEmptyFileLines(featureFile);
		
		for ( int i=0; i<listOfLines.size(); i++ ) {
			String[] str = listOfLines.get(i).split("\t");
			listOfGlobalFeatures.add(str[0]);
			listOfGlobalFeatWeight.add(Integer.valueOf(str[1]));
		}
		
		listOfLines = FileUtility.readNonEmptyFileLines(patternFile);
		
		for ( int i=0; i<listOfLines.size(); i++ ) {
			String[] str = listOfLines.get(i).split("\t");
			PatternsDepRelFromGraph.listOfLabelsForPatterns.add(Boolean.valueOf(str[0]));
			str = str[1].split("###");
			PatternsDepRelFromGraph.listOfAllPatterns.add(DataStrucUtility.arrayToList(str));
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public static void writeFeaturesAndPatternsInFile () throws IOException {
		
		StringBuilder sb = new StringBuilder();
		for ( int i=0; i<listOfGlobalFeatures.size(); i++ )
			sb.append(listOfGlobalFeatures.get(i) + "\t" + listOfGlobalFeatWeight.get(i) + "\n");
		
		FileUtility.writeInFile( featureFile, sb.toString(), false);
		
		sb = new StringBuilder();
		for ( int i=0; i<PatternsDepRelFromGraph.listOfLabelsForPatterns.size(); i++ ) {
			sb.append(PatternsDepRelFromGraph.listOfLabelsForPatterns.get(i) + "\t");
			for ( int j=0; j<PatternsDepRelFromGraph.listOfAllPatterns.get(i).size()-1; j++ ) {
				sb.append(PatternsDepRelFromGraph.listOfAllPatterns.get(i).get(j) + "###");
			}
			
			sb.append(PatternsDepRelFromGraph.listOfAllPatterns.get(i).get(PatternsDepRelFromGraph.listOfAllPatterns.get(i).size()-1));			
			sb.append("\n");
		}
		
		FileUtility.writeInFile( patternFile, sb.toString(), false);
	}
	
	

	/**
	 * 
	 * @param listFeatIndxsOfCurInp
	 * @param listFeatCountOfCurInp
	 * @param vectOutFileName
	 * @param listOfAllGlobalFeatures
	 * @param listOfAllGlobalFeatWeight
	 * @return
	 * @throws IOException
	 */
	public static void sortFeatValByIndx ( ArrayList<Integer> listFeatIndxsOfCurInp, ArrayList<Integer> listFeatCountOfCurInp
			 ) throws IOException {
		// sort
		for ( int i=0; i < listFeatIndxsOfCurInp.size()-1; i++ ) {
			for ( int k=i+1; k < listFeatIndxsOfCurInp.size(); k++ ) {
				if ( listFeatIndxsOfCurInp.get(k) < listFeatIndxsOfCurInp.get(i) ) {
					int t = listFeatIndxsOfCurInp.get(k);
					listFeatIndxsOfCurInp.set(k, listFeatIndxsOfCurInp.get(i));
					listFeatIndxsOfCurInp.set(i, t);
					
					t = listFeatCountOfCurInp.get(k);
					listFeatCountOfCurInp.set(k, listFeatCountOfCurInp.get(i));
					listFeatCountOfCurInp.set(i, t);
				}
			}
		}
	}

	


	/**
	 * 
	 * @param listFeatIndxsOfCurInp
	 * @param listFeatCountOfCurInp
	 * @param vectOutFileName
	 * @param listOfAllGlobalFeatures
	 * @param listOfAllGlobalFeatWeight
	 * @return
	 * @throws IOException
	 */
	public static String convertVectorOfFeatValToString ( ArrayList<Integer> listFeatIndxsOfCurInp, ArrayList<Integer> listFeatCountOfCurInp,
			String vectOutFileName, ArrayList<String> listOfAllGlobalFeatures,
			ArrayList<Integer> listOfAllGlobalFeatWeight ) throws IOException {
		
		
		StringBuilder sbOutputVect = new StringBuilder();
		
		// convert to vector
		for ( int i=0; i < listFeatIndxsOfCurInp.size(); i++ ) {
			
			if ( !TextUtility.isEmptyString(vectOutFileName) )
				FileUtility.writeInFile( vectOutFileName, listOfAllGlobalFeatures.get(listFeatIndxsOfCurInp.get(i)) + "\n", true);
			
			sbOutputVect.append((listFeatIndxsOfCurInp.get(i)+1
					) + ":" + 
					(listFeatCountOfCurInp.get(i)*listOfAllGlobalFeatWeight.get(listFeatIndxsOfCurInp.get(i))) + " ");
		}
		
		if ( !TextUtility.isEmptyString(vectOutFileName) )
			FileUtility.writeInFile( vectOutFileName, "==================================================\n", true);
		
		return sbOutputVect.toString();
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public static String getInstanceVectors () throws IOException {
				
		StringBuilder sbOutputVect = new StringBuilder();
		
		// convert to vector
		for ( int ai=0; ai < listOfAllInstancesWithFeat.size(); ai++ ) {
			ArrayList<Integer> listFeatIndexes = listOfAllInstancesWithFeat.get(ai);
			ArrayList<Integer> listFeatCount = listOfAllInstancesWithFeatCount.get(ai);
			String str = listOfAllInstancePolarity.get(ai) + " ";
			
			if ( !TextUtility.isEmptyString(vectOutFile) )
				FileUtility.writeInFile( vectOutFile, listOfAllInstances.get(ai), true);
			
			for ( int i=0; i < listFeatIndexes.size(); i++ ) {
			
				if ( featInstanceCountThreshold < 2 
						|| arrOfGlobalFeaturesTotInstanceCount[listFeatIndexes.get(i)] >= featInstanceCountThreshold ) {
					
					if ( !TextUtility.isEmptyString(vectOutFile) )
						FileUtility.writeInFile( vectOutFile, listOfGlobalFeatures.get(listFeatIndexes.get(i)) + "\n", true);
				
					str += (listFeatIndexes.get(i)+1) + ":" + 
						(listFeatCount.get(i)*listOfGlobalFeatWeight.get(listFeatIndexes.get(i))) + " ";
				}
				/*
				if ( !TextUtility.isEmptyString(vectOutFile) )
					FileUtility.writeInFile( vectOutFile, listOfGlobalFeatures.get(listFeatIndexes.get(i)) + "\n", true);
				
				str += (listFeatIndexes.get(i)+1) + ":" + 
						(listFeatCount.get(i)*listOfGlobalFeatWeight.get(listFeatIndexes.get(i))) + " ";
						*/
			}
			
			sbOutputVect.append(str.trim() + "\n");
		}
		
		if ( !TextUtility.isEmptyString(vectOutFile) )
			FileUtility.writeInFile( vectOutFile, "==================================================\n", true);
		
		return sbOutputVect.toString();
	}

}