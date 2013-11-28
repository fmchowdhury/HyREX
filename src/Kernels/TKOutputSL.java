package Kernels;

import java.io.File;
import java.util.ArrayList;

import Structures.Sentence;
import Utility.Common;

import Clause.ClauseAnalyser;
import Clause.ClauseAnalyser.eDataFilterOption;


public class TKOutputSL {

	static int totalRel = 0;
	
	public void generateTKoutputForSL( ArrayList<Sentence> listSentence,
			String bioRelExInpFile, boolean isResolveOverlappingEntities,
			ClauseAnalyser.eDataFilterOption relToBeConsidered,
			String inClauseBoundFileName, String entPairFileName, boolean isTrainData 
			) throws Exception{
			
		totalRel = 0;
		TKOutputPST.totalRelNeg = 0;		
		TKOutputPST.totalRelPos = 0;
		
		ArrayList<String> listAllSenIDs = new ArrayList<String>();
		int[][] arrClauseBoundOfSen = null;
		
		// if clause segmented data exists
		if ( relToBeConsidered != eDataFilterOption.DATA_ALL &&
				inClauseBoundFileName != null && new File(inClauseBoundFileName).exists() ) {
			ArrayList<String[]> listClauseBoundOfAllSen = new ClauseAnalyser().readClauseBoundaries(inClauseBoundFileName);		 
			listAllSenIDs = new Common().separateSenIDsFromClauseBound(listClauseBoundOfAllSen);
			arrClauseBoundOfSen = new Common().separateClauseBoundFromSenIDs(listClauseBoundOfAllSen);
			listClauseBoundOfAllSen.clear();
		}
	
		new TKOutputDT().generateTK(false, listSentence, 
				bioRelExInpFile, -1, isResolveOverlappingEntities, relToBeConsidered, listAllSenIDs, 
				arrClauseBoundOfSen, entPairFileName );
		
		//removeCommentsHavingNoEnt(bioRelExInpFile);
		//removeOtherWordsInLeastNPofEnt(bioRelExInpFile, psgParsedFileName);
		/*
		if ( isTrainData )
			new FeatVecCreatorForSLkernel().writeJSREformatFileTrain( bioRelExInpFile, bioRelExInpFile + ".jsre");
		else
			new FeatVecCreatorForSLkernel().writeJSREformatFileTest( bioRelExInpFile, bioRelExInpFile + ".jsre");
			*/
	}
	
	
	
}
