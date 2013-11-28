package org.fbk.it.hlt.bioRE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Random;

import Utility.FileUtility;


public class FoldCreator {
	  
	int gTotalSen = 0;
	ArrayList<Integer>[] gListTrainFoldsWithSenIndexes;
		
	public FoldCreator(String tokenizedSenFile, int noFolds, int noMultiLineEntriesPerSen) throws Exception{
		gTotalSen = getDataSize(tokenizedSenFile, true) / noMultiLineEntriesPerSen;
		gListTrainFoldsWithSenIndexes = createFoldsOfSenIndexesRandomly(gTotalSen, noFolds);
		
		// sort training folds
		// TODO: The following codes should be replaced with merge or heap sort algorithm
		for ( int i=0; i<gListTrainFoldsWithSenIndexes.length; i++ ){
			for ( int x=0; x<gListTrainFoldsWithSenIndexes[i].size()-1; x++ ){
				int a = gListTrainFoldsWithSenIndexes[i].get(x), b = 0;
				
				for ( int y=x+1; y<gListTrainFoldsWithSenIndexes[i].size(); y++ ){
					b = gListTrainFoldsWithSenIndexes[i].get(y);
					
					if ( b < a ){
						gListTrainFoldsWithSenIndexes[i].set(x, b);
						gListTrainFoldsWithSenIndexes[i].set(y, a);
						a = b;
					}
				}
			}		
		}
	}
	
	
	/**
	 * Assumption: if senIdFile exists then the ids corresponds to the
	 * order of the multi-token sentences in the data file. 
	 * 
	 * @param tokenizedSenFile
	 * @param senIdFile
	 * @param noFolds
	 * @throws Exception
	 */
	public void splitDataInto_n_Folds( String tokenizedSenFile, 
			String senIdFile, int noFolds, int noMultiLineEntriesPerSen, boolean isEntryMultiLine
			) throws Exception{

		/**
		 * Idea:
		 * - split senId file and trainSen file
		 * - get the number of total sen in trainSen file
		 * - populate 0...total_sen into an array list
		 * - randomly select items from the array list to create n-folds
		 * - sort items (i.e. sentence indexes) of the folds
		 * - save the n-folds of the sentences and ids accordingly
		 * - for each of the trainSen fold file, create a file in which labels would be discarded
		 */
		
		String dirName = "tmp";//Common.TEMP_DIR;
		File file=new File(dirName);
		
		if ( !file.exists() ){
			// Create directory named "tmp" inside current directory
		    boolean success = (new File(dirName)).mkdir();
		    if (success)
		      System.out.println("Directory: '" + dirName + "' created");		    
		}		
			
		// create sen folds 
		ArrayList<ArrayList<String[]>>[] arrayOfFolds = createFolds(tokenizedSenFile, 
				gListTrainFoldsWithSenIndexes, gTotalSen, isEntryMultiLine, noMultiLineEntriesPerSen);
		
		String[] temp = tokenizedSenFile.split("/+");
		
		// save training folds
		saveFolds(arrayOfFolds, isEntryMultiLine, dirName + "/" + temp[temp.length-1], false);
		
		// save test folds
		//saveFolds(arrayOfFolds, true, dirName + "/" + temp[temp.length-1], true);
		
		// create sen id folds
		if ( senIdFile != null && !(senIdFile = senIdFile.trim()).isEmpty() ){
			arrayOfFolds = createFolds(senIdFile, 
					gListTrainFoldsWithSenIndexes, gTotalSen, isEntryMultiLine, 1);
			
			temp = senIdFile.split("/+");
			saveFolds(arrayOfFolds, false, dirName + "/" + temp[temp.length-1], false);
		}
	}
	
	
	/**
	 * Return the number of sentences in the file.
	 * 	 * 
	 * @param fileName		Data file name
	 * @param isMultiLine   Whether the sentences consist of multi-line tokens.
	 * @return				Total no. of sentences
	 * @throws IOException
	 */
	private int getDataSize( String fileName, boolean isMultiLine ) throws IOException{
		BufferedReader input =  new BufferedReader(new FileReader(new File(fileName)));
		int totalData = 0;    
		String line = "";
		
		// count number of sentences in fold s
		do{
			// skip blank lines
			while ( (line = input.readLine()) != null){
				line = line.trim();
				
				if ( !line.isEmpty() )
					break;
			}
			
			if ( line != null )
				totalData++;
			
			if ( isMultiLine )
				// skip all the following non-blank lines of the current sentence
				while ( (line = input.readLine()) != null){
					line = line.trim();
					
					if ( line.isEmpty() )
						break;
				}
		}while ( line != null);
		    
		input.close();

		return totalData;
	}
	
	
	
	/**
	 * Create n-folds of the indexes of the sentence ids by random selection.
	 * 
	 * @param totalSen
	 * @param noFolds
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<Integer>[] createFoldsOfSenIndexesRandomly( int totalSen, int noFolds ) throws Exception{
		ArrayList<Integer> listIndexOfSenIds = new ArrayList<Integer>();
		
		// initializing folds for sentence ids
		ArrayList<Integer>[] listTrainFoldsWithSenIndexes = new ArrayList[noFolds];
		for ( int i=0; i<noFolds; i++ )
			listTrainFoldsWithSenIndexes[i] = new ArrayList<Integer>();
		
		// create index list for sen ids
		for ( int i=0; i<totalSen; i++ )
				listIndexOfSenIds.add(i);
		
		// create folds of sen id indexes by random index selection
	    Random randomGenerator = new Random();
	    int foldSize = totalSen/noFolds, tempInt = totalSen;
	    
	    ArrayList<String> listRandSenIndx = FileUtility.readFileLines("randSenIndx");
	    StringBuilder sb = new StringBuilder("");
	    
	    int kk = 0;
	    
	    for ( int f=0; f<noFolds; f++ ){
		    for (int idx = 0; idx <foldSize; idx++){
		    	int randomInt = //Integer.valueOf(listRandSenIndx.get(kk));// 
		    		randomGenerator.nextInt(tempInt);
		    	// get the randomly selected sen id index
		      	listTrainFoldsWithSenIndexes[f].add( listIndexOfSenIds.remove(randomInt) );
		    	kk++;
		    	tempInt--;
		      
		      sb.append(randomInt + "\n");
		    }
	    }
	    
	    FileUtility.writeInFile("randSenIndx", sb.toString(), false);
	    
	    return listTrainFoldsWithSenIndexes;
	}
	
	
	/**
	 *
	 * 
	 * @param senIdFile
	 * @return
	 * @throws IOException
	 */
	private ArrayList<ArrayList<String[]>>[] createFolds( String senFile, ArrayList<Integer>[] listTrainFoldsWithSenIndexes, 
			int totalSen, boolean isMultiline, int noMultiLineEntriesPerSen ) throws IOException{
		String line = "";
		
		
		// initialize folds		
		ArrayList<ArrayList<String[]>>[] arrayOfSenFolds =  new ArrayList[listTrainFoldsWithSenIndexes.length];
		for ( int i=0; i<arrayOfSenFolds.length; i++ )
			arrayOfSenFolds[i] = new ArrayList<ArrayList<String[]>>();
		
		// populate each fold
		for ( int i=0; i<listTrainFoldsWithSenIndexes.length; i++ ){			
			if ( listTrainFoldsWithSenIndexes[i].size() == 0 )
				i =i-1+1;

			BufferedReader input =  new BufferedReader(new FileReader(new File(senFile)));
			int k = 0, senNo = 0, nextCandidateSenIndex = listTrainFoldsWithSenIndexes[i].get(0),
				multLineEntryTracker = 0;
			
			do{
				// skip blank lines
				while ( (line = input.readLine()) != null){
					line = line.trim();
					
					if ( !line.isEmpty() )
						break;
				}
				
				//if ( multLineEntryTracker == 0 )
				ArrayList<String[]> tempList = new ArrayList<String[]>();
				//else
				//	tempList.add(new String[]{"\n"});
				
				// collect all the following non-blank lines of the current sentence
				do{
					if ( line == null )
						break;
					
					line = line.trim();
										
					if ( line.isEmpty() )
						break;
					
					tempList.add(line.split("\\s+"));
				}while ( isMultiline == true && (line = input.readLine()) != null);
				
				multLineEntryTracker++;
				
				// if the current sen index belongs to the current fold
				if ( senNo == nextCandidateSenIndex && tempList.size() > 0 ){
					arrayOfSenFolds[i].add(tempList);
					
					if ( multLineEntryTracker == noMultiLineEntriesPerSen ){
						k++;
						
						if ( k >= listTrainFoldsWithSenIndexes[i].size() )
							break;
						
						nextCandidateSenIndex = listTrainFoldsWithSenIndexes[i].get(k);
					}
				}
				
				if ( isMultiline == false || multLineEntryTracker == noMultiLineEntriesPerSen ){
					senNo++;
					multLineEntryTracker = 0;
				}
			}while ( line != null );
			
			input.close();
		}
		
		return arrayOfSenFolds;
	}
	
	
	private void saveFolds( ArrayList<ArrayList<String[]>>[] arrayOfFolds, boolean isMultiline,
			String outFileName, boolean isExcludeTokenLabel ) throws IOException{
		
		String line = null;
		int z = 0;
		if ( isExcludeTokenLabel )
			z = 1;
			
		// for each fold
		for ( int i=0; i<arrayOfFolds.length; i++ ){
			Writer output = new BufferedWriter(new FileWriter(outFileName + "_" + i));
			
			// for each sentence
			for ( int k=0; k<arrayOfFolds[i].size(); k++ ){
								
				// for each id / token & features
				for ( int x=0; x<arrayOfFolds[i].get(k).size(); x++ ){
					line = arrayOfFolds[i].get(k).get(x)[0];
									
					line = line.concat("\t").concat(arrayOfFolds[i].get(k).get(x)[1])
							.concat("\t").concat(arrayOfFolds[i].get(k).get(x)[2]);
					for ( int y=3; y<arrayOfFolds[i].get(k).get(x).length-z; y++ )
						line = line.concat(" ").concat(arrayOfFolds[i].get(k).get(x)[y]);
						    						
					output.write(line.concat("\n"));
				}
				
				if ( isMultiline )
					output.write("\n");
			}
			
		    output.close();
		}	    
	}
}
