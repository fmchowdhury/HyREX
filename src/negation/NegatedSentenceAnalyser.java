package negation;

import Kernels.*;

import java.io.IOException;
import java.util.ArrayList;
import Kernels.TKOutputPST;
import Structures.Entity;
import Structures.Sentence;
import Utility.CommonUtility;
import Utility.FileUtility;
import Utility.TextUtility;

public class NegatedSentenceAnalyser {

	/*
	 * Two or more drugs after "but" which appears after negation
	 * sentence has keywords (e.g. increase/reduce) which 
	 *    precedes/follows negation
	 * sentence contains "should be"
	 * word difference between negation and keyword
	 * while precedes negation
	 */
	
	/**
	 * This class will try to identify whether a sentence with a negation clue qualify for further processing
	 * for the extraction of DDIs.
	 * 
	 * 
	 * whether the negation cue is on the before/between/after of the pairs.
	 * is the clause containing the negation appears before and there is a comma in between it and the pairs
	 * is the clause containing the negation appears after and there is a comma in between it and the pairs
	 * 
	 * 
	 * if the sentence has only two drugs then whether the negation cue is on the before/between/after of the pairs.
	 * immediate governor of the negation
	 * the nearest verb governor of the negation without being connected by a "conj"
	 * is the verb governor main verb/root of the sentence
	 * are all drug mentions dependent on that verb governor
	 * are all but one drug mentions dependent on that verb governor
	 * is the negation preceded by "although" in the same clause
	 * is there any drug mentions after the negation inside the sentence
	 * 
	 * are all drug mentions fall under the scope of the (automatically identified) negation scope
	 * are all but one drug mentions fall under the scope of the (automatically identified) negation scope
	 * 
	 * @throws IOException 
	 */
	
	
	String keywords = "(increase|reduce|enhance|reduction|decrement|prolong|decrease|accelerate|diminish|reuptake|increment|interaction|metabolize|report|evidence|risk|effect)";
	// 
	
//*
	public static void main ( String[] args ) throws Exception {
		
		//args = "out/best.base.stat.in_ddi_sen  out/allSenIdsForTest  neg".split("\\s+");
		
		String mlPredictions = args[0]; 
		String senIdFile = args[1];
		String out_op = args[2];
					
		ArrayList<String> tempSenIds = FileUtility.readNonEmptyFileLines(senIdFile);
		ArrayList<String> tempLinesPred = FileUtility.readNonEmptyFileLines(mlPredictions);
		
		StringBuilder sb = new StringBuilder();
		
		for ( int i=0; i<tempLinesPred.size(); i++ ) {
				
			if ( tempLinesPred.get(i).trim().length() > 0 ) {
				String[] temp = tempLinesPred.get(i).trim().split("\\s+");
				
				if ( temp[2].equals("1") && out_op.equalsIgnoreCase("posi") ) {
				//	sb.append(temp[3] + "\t");
					sb.append(tempSenIds.get(i) + "\n");
				}
				else if ( temp[2].equals("0") && out_op.equalsIgnoreCase("neg") ) {
				//	sb.append(temp[3] + "\t");
					sb.append(tempSenIds.get(i) + "\n");
				}
				else if ( temp[2].equals("1") && temp[0].equals("0.0") && out_op.equalsIgnoreCase("fp") ) {
				//	sb.append(temp[3] + "\t");
					sb.append(tempSenIds.get(i) + "\n");
				}
				else if ( temp[2].equals("1") && temp[0].equals("1.0") && out_op.equalsIgnoreCase("tp") ) {
				//	sb.append(temp[3] + "\t");
					sb.append(tempSenIds.get(i) + "\n");
				}
				else if ( temp[2].equals("0") && temp[0].equals("0.0") && out_op.equalsIgnoreCase("fn") ) {
				//	sb.append(temp[3] + "\t");
					sb.append(tempSenIds.get(i) + "\n");
				}
				else if ( temp[2].equals("0") && temp[0].equals("-1") && out_op.equalsIgnoreCase("tn") ) {
			//		sb.append(temp[3] + "\t");
					sb.append(tempSenIds.get(i) + "\n");
				}
			}
		}
		/*
		ArrayList<String> arr = DataStrucUtility.arrayToList(sb.toString().trim().split("\\s+"));
		ArrayList<Sentence> allSentence = Sentence.readFullData("../../data/DDIExtraction2011/train.full");
		int totR = 0;
		for ( int i=0; i<allSentence.size(); i++ )
			if ( arr.contains(allSentence.get(i).senID) )
				totR+=allSentence.get(i).listRels.size();
				
		System.out.println(totR);
		*/
		
		FileUtility.writeInFile(CommonUtility.OUT_DIR + "/sentence_predictions.txt", sb.toString(), false);
	}
	
	//*/
	
	public static void init() throws IOException {
		GenericFeatVect.init();		
	}
	
	
	/**
	 * 
	 * @param listOfInstances
	 * @param outFileName
	 * @throws IOException 
	 */
	private void populateFeatListFromFeatValObjects( ArrayList<FeatValNegSenSelector> listOfInstances, String outFileName ) throws IOException {
		
		FileUtility.writeInFile(outFileName, "", false);
		
		System.out.println("Tot. instance = " + listOfInstances.size());
			// addNewFeatureInList(feature, listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);	
		for ( int i=0; i<listOfInstances.size(); i++ ) {
		//	System.out.println("instance " + i);
			StringBuilder sb = new StringBuilder();
			ArrayList<Integer> listFeatIndsOfCurInp = new ArrayList<Integer>(), listFeatCountOfCurInp = new  ArrayList<Integer>();
			ArrayList<String[]> listOfAllFeaturesForCurInst = new ArrayList<String[]>();
			FeatValNegSenSelector clsCurInstance = listOfInstances.get(i);
		//*	
			//if ( clsCurInstance.immediateGovernorIsVerbGovernor && clsCurInstance.areAllDrugsDependOnVerbGovernor )
			//	listOfAllFeaturesForCurInst.add( new String[]{"allDrugsAreInsideScopeOfNegation"});
			//else 
			{
				/*
			if ( clsCurInstance.has2Drugs && clsCurInstance.allPairsOnRight )
				listOfAllFeaturesForCurInst.add( new String[]{"has2Drugs&allPairsOnRight"});
			else if ( clsCurInstance.has2Drugs && clsCurInstance.allPairsOnLeft )
				listOfAllFeaturesForCurInst.add( new String[]{"has2Drugs&allPairsOnLeft"});
			
			//*/
			
			if ( clsCurInstance.has2MentionOfInterest )
				listOfAllFeaturesForCurInst.add( new String[]{"has2MentionOfInterest"});
			else 
				listOfAllFeaturesForCurInst.add( new String[]{"hasMoreThan2MentionOfInterest"});
			
			//if ( clsCurInstance.immediateGovernorIsVerbGovernor )
				//listOfAllFeaturesForCurInst.add( new String[]{"immediateGovernorIsVerbGovernor"});
			
			
			//if ( clsCurInstance.hasLessThan2Drugs )
				//listOfAllFeaturesForCurInst.add( new String[]{"hasLessThan2Drugs"});
			
			if ( clsCurInstance.allMentionOfInterestOnRight )
				listOfAllFeaturesForCurInst.add( new String[]{"allMentionOfInterestOnRight"});
			else if ( clsCurInstance.allMentionOfInterestOnLeft )
				listOfAllFeaturesForCurInst.add( new String[]{"allMentionOfInterestOnLeft"});
			else 
				listOfAllFeaturesForCurInst.add( new String[]{"NiehterAllMentionOfInterestOnLeftOrRight"});
			
			//if ( clsCurInstance.containsAlone )
				//listOfAllFeaturesForCurInst.add( new String[]{"containsAlone"});
			
			if ( !TextUtility.isEmptyString(clsCurInstance.negWord) )
				listOfAllFeaturesForCurInst.add( new String[]{"negWord="+clsCurInstance.negWord});
			
			if ( !TextUtility.isEmptyString(clsCurInstance.immediateGovernor) )
				listOfAllFeaturesForCurInst.add( new String[]{"immediateGovernor="+clsCurInstance.immediateGovernor});
			/*
			if ( !TextUtility.isEmptyString(clsCurInstance.immediateGovernor)
					&& clsCurInstance.immediateGovernor.matches(keywords) )
				listOfAllFeaturesForCurInst.add( new String[]{"immediateGovernorIsKeyWord"});
			
			if ( !TextUtility.isEmptyString(clsCurInstance.nearestVerbGovernor)
					&& clsCurInstance.nearestVerbGovernor.matches(keywords) )
				listOfAllFeaturesForCurInst.add( new String[]{"nearestVerbGovernorIsKeyWord"});
			*/
			
			if ( !TextUtility.isEmptyString(clsCurInstance.nearestVerbGovernor) )
				listOfAllFeaturesForCurInst.add( new String[]{"nearestVerbGovernor="+clsCurInstance.nearestVerbGovernor});
			
			if ( clsCurInstance.isVerbGovernorRoot )
				listOfAllFeaturesForCurInst.add( new String[]{"isVerbGovernorRoot"});
			
			if ( clsCurInstance.areAllMentionOfInterestDependOnVerbGovernor )
				listOfAllFeaturesForCurInst.add( new String[]{"areAllMentionOfInterestDependOnVerbGovernor"});			
			else if ( clsCurInstance.areAllButOneMentionOfInterestDependOnVerbGovernor )
				listOfAllFeaturesForCurInst.add( new String[]{"areAllButOneMentionOfInterestDependOnVerbGovernor"});
			//else
				//listOfAllFeaturesForCurInst.add( new String[]{"noDrugsDependOnVerbGovernor"});

			//if ( clsCurInstance.areAllDrugsDependOnImmeidateGovernor )
				//listOfAllFeaturesForCurInst.add( new String[]{"areAllDrugsDependOnImmeidateGovernor"});			
			
			if ( clsCurInstance.isAlthoughPrecedeNegationInSameClause )
				listOfAllFeaturesForCurInst.add( new String[]{"isAlthoughPrecedeNegationInSameClause"});
			/*
			for ( int kw=0; kw<clsCurInstance.listOfWordsBefore.size(); kw++ ){
				listOfAllFeaturesForCurInst.add( new String[]{"WB="+clsCurInstance.listOfWordsBefore.get(kw)});
			}
			
			for ( int kw=0; kw<clsCurInstance.listOfWordsAfter.size(); kw++ ){
				listOfAllFeaturesForCurInst.add( new String[]{"WA="+clsCurInstance.listOfWordsAfter.get(kw)});
			}
			*/
			
		//	if ( clsCurInstance.listOfKeyWordsFound.size() == 0 )
			//	listOfAllFeaturesForCurInst.add( new String[]{"noKeyWordsFoundInSen"});
			
			/*
			if ( clsCurInstance.listOfKeyWordsFound.size() > 0 ) {
				int diff = 0;
				for ( int kw=0; kw<clsCurInstance.listOfKeyWordsFound.size(); kw++ ){
					if ( clsCurInstance.listOfWIofKeyWords.get(kw) < clsCurInstance.wiOfNegation 
							|| clsCurInstance.listOfWIofKeyWords.get(kw) - clsCurInstance.wiOfNegation < 5 ) {
						
								listOfAllFeaturesForCurInst.add( new String[]{"KW="+clsCurInstance.listOfKeyWordsFound.get(kw)});
					}
					diff += Math.abs(clsCurInstance.wiOfNegation - clsCurInstance.listOfWIofKeyWords.get(kw));
				}
				
				//listOfAllFeaturesForCurInst.add( new String[]{"AvgDistOfKW=" + diff/clsCurInstance.listOfKeyWordsFound.size()});
			}
			//*/
			//if ( clsCurInstance.hasAnyDrugOnRight )
				//listOfAllFeaturesForCurInst.add( new String[]{"hasAnyDrugOnRight"});
			
			if ( clsCurInstance.hasCommaBeforeNextMentionOfInterest )
				listOfAllFeaturesForCurInst.add( new String[]{"hasCommaBeforeNextMentionOfInterest"});
			
			if ( clsCurInstance.hasCommaAfterPrevMentionOfInterest )
				listOfAllFeaturesForCurInst.add( new String[]{"hasCommaAfterPrevMentionOfInterest"});
			
			if ( clsCurInstance.sentenceHasBut )
				listOfAllFeaturesForCurInst.add( new String[]{"sentenceHasBut"});
			
			//if ( clsCurInstance.hasNoEntities )
				//listOfAllFeaturesForCurInst.add( new String[]{"hasNoEntities"});
			
			if ( clsCurInstance.wiOfNegation < 0 )
				listOfAllFeaturesForCurInst.add( new String[]{"hasNoNegation"});
			}
			// add all features in the global list and also create corresponding feature indexes 
			for ( int f=0; f<listOfAllFeaturesForCurInst.size(); f++ ) {
//			 System.out.println("ent1id: " + clsCurInstance.entId_1 + " ent2id: " + clsCurInstance.entId_2 + " " + listOfAllFeaturesForCurInst.get(f));
				GenericFeatVect.addNewFeatureInList(listOfAllFeaturesForCurInst.get(f), 1,
						listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			}
			
			FileUtility.writeInFile(GenericFeatVect.featureFile, clsCurInstance.senID + "\n", true);
						
			GenericFeatVect.sortFeatValByIndx(listFeatIndsOfCurInp, listFeatCountOfCurInp);
			String str = GenericFeatVect.convertVectorOfFeatValToString(listFeatIndsOfCurInp, listFeatCountOfCurInp, 
					GenericFeatVect.featureFile, GenericFeatVect.listOfGlobalFeatures, GenericFeatVect.listOfGlobalFeatWeight);
			sb = sb.append(TKOutputDT.getOutputForSingleInstance( str + "\n", clsCurInstance.classLabel));
			
			if ( clsCurInstance.classLabel )
				TKOutputPST.totalRelPos++;
			else
				TKOutputPST.totalRelNeg++;

			FileUtility.writeInFile(outFileName, sb.toString(), true);
		}
		
		System.out.println( TextUtility.now() + ": Instances are written as feature-index:count in " + outFileName);
		//FileUtility.writeInFile(outFileName, sb.toString(), false);
	}


	/**
	 * 
	 * @param parsedFileName
	 * @param fullDataFileName
	 * @param outputFile
	 * @param depParsedFile
	 * @throws Exception
	 */
	public void generateFeatureVectors(String parsedFileName,
			String fullDataFileName, String outputFile, String depParsedFile)
			throws Exception {

		if ( !TKOutputGenerator.isTestData )
			init();
		
		FileUtility.writeInFile(outputFile, "", false);
		TKOutputPST.totalRelPos = 0;
		TKOutputPST.totalRelNeg = 0;

		ArrayList<FeatValNegSenSelector> listOfInstances = new ArrayList<FeatValNegSenSelector>();

		ArrayList<Sentence> listSentence = Sentence
				.readFullData(fullDataFileName, parsedFileName, depParsedFile, "", false);
		
		StringBuilder sbSenIds = new StringBuilder(); 
		
		for (int s = 0; s < listSentence.size(); s++) {

			Sentence objCurSen = listSentence.get(s);
					
			if ( objCurSen.getNoOfMentionsOfInterest() < 2 || objCurSen.text.matches(".*(not recommended|should not be|must not be" +
					//"|should be ([a-z]*\\s+){0,3}not" +
					").*"))
				// |should be
				continue;

			FeatValNegSenSelector clsFeatVal = extractFeatures(objCurSen);
						
			if ( clsFeatVal.wiOfNegation < 0  || !clsFeatVal.hasAnyMentionOfInterestOnRight )
				continue;
			
			// checking whether the sentence has at least one DDI 
			for ( int r=0; r<objCurSen.listRels.size(); r++ ) {
				if ( objCurSen.listRels.get(r).isPositive ) {
					clsFeatVal.classLabel = true;
					break;
				}
			}

			if ( TKOutputGenerator.isTestData )
				sbSenIds = sbSenIds.append(objCurSen.senID + "\n");
			
			listOfInstances.add(clsFeatVal);
		}

		populateFeatListFromFeatValObjects(listOfInstances, outputFile);
		if ( TKOutputGenerator.isTestData )
			FileUtility.writeInFile(CommonUtility.OUT_DIR + "/allSenIdsForTest", sbSenIds.toString(), true);
	}


	/**
	 * 
	 * @param curSen
	 * @return
	 */
	private FeatValNegSenSelector extractFeatures( Sentence curSen ) {

		FeatValNegSenSelector clsFeatVal = new FeatValNegSenSelector();
		
		clsFeatVal.senID = curSen.senID;
		
		String wordsBeforeNeg = "", wordsAfterNeg = "";
		
		
		for ( int w=0; w<curSen.arrWordAndPosByParser.length; w++ ) {
			
			
			if ( curSen.arrWordAndPosByParser[w][0].toLowerCase().matches("(no|not|.*n't)") ) {
				clsFeatVal.wiOfNegation = w;
				w++;
				for ( ; w<curSen.arrWordAndPosByParser.length; w++ ) { 
					wordsAfterNeg += curSen.arrWordAndPosByParser[w][0] + " ";
					
					if ( curSen.arrLemmasByParser[w].toLowerCase().matches(keywords) ) {
						clsFeatVal.listOfKeyWordsFound.add(curSen.arrLemmasByParser[w]);
						clsFeatVal.listOfWIofKeyWords.add(w);
					}
				}
				
				break;
			}
			else if ( curSen.arrLemmasByParser[w].toLowerCase().matches(keywords) ) {
				clsFeatVal.listOfKeyWordsFound.add(curSen.arrLemmasByParser[w]);
				clsFeatVal.listOfWIofKeyWords.add(w);
			}
			
			wordsBeforeNeg += curSen.arrWordAndPosByParser[w][0] + " ";
		}
		
		if ( clsFeatVal.wiOfNegation < 0 )
			return clsFeatVal;
/*
		for ( int w=clsFeatVal.wiOfNegation-1; w>=0 && clsFeatVal.wiOfNegation - w <= 1; w-- ) {
			clsFeatVal.listOfWordsBefore.add(curSen.arrLemmasByParser[w].toLowerCase());
		}
		
		for ( int w=clsFeatVal.wiOfNegation+1; w<curSen.arrLemmasByParser.length && w - clsFeatVal.wiOfNegation <= 1; w++ ) {
			clsFeatVal.listOfWordsAfter.add(curSen.arrLemmasByParser[w].toLowerCase());
		}
	*/
		// TODO: this needs to be changed. currently, it is considering all the entities instead of entities of interest.
		if ( curSen.getNoOfMentionsOfInterest() == 2 )
			clsFeatVal.has2MentionOfInterest = true;
		
		if ( curSen.getNoOfMentionsOfInterest() < 2 ) {
			clsFeatVal.hasLessThan2MentionOfInterest = true;
			return clsFeatVal;
		}
		
	//	if (  curSen.text.contains(" but") )
		if ( curSen.text.matches(".*[^a-zA-Z]*but[^a-zA-Z]*.*") )
			clsFeatVal.sentenceHasBut = true;
		
		if ( curSen.text.matches(".*[^a-zA-Z]*alone[^a-zA-Z]*.*") )
			clsFeatVal.containsAlone = true;
		
		wordsBeforeNeg = wordsBeforeNeg.trim();
		wordsAfterNeg = wordsAfterNeg.trim();
		
		//System.out.println(curSen.senID);
		
		if ( !curSen.depTree.allNodesByWordIndex[clsFeatVal.wiOfNegation].getParentsWordIndexes().isEmpty() )
			clsFeatVal.wiOfImmediateGovernor = TPWF.getNonConjGovernorIndex(curSen, clsFeatVal.wiOfNegation);
		
		if ( clsFeatVal.wiOfImmediateGovernor > -1 )
			clsFeatVal.wiOfNearestVerbGovernor = TPWF.getNearestVerbGovernor(clsFeatVal.wiOfImmediateGovernor, curSen);
			
		int negSI = wordsBeforeNeg.replaceAll("\\s+", "").length(), noOfEntOnLeft = 0, noOfEntOnRight = 0;
		/*
		int x = -1;
		while( (x=clsFeatVal.listOfWIofKeyWords.indexOf(clsFeatVal.wiOfImmediateGovernor)) >= 0
					|| (x=clsFeatVal.listOfWIofKeyWords.indexOf(clsFeatVal.wiOfNearestVerbGovernor)) >= 0 ) {
			clsFeatVal.listOfKeyWordsFound.remove(x);
			clsFeatVal.listOfWIofKeyWords.remove(x);
		}
*/
		if ( clsFeatVal.wiOfNearestVerbGovernor > -1 &&
				curSen.depTree.allNodesByWordIndex[clsFeatVal.wiOfNearestVerbGovernor].getParentsWordIndexes().isEmpty() )
			clsFeatVal.isVerbGovernorRoot = true;
		
		//System.out.println(curSen.senID);
		//if ( curSen.listOfEntities == null || curSen.listOfEntities.size() == 0 )
			//clsFeatVal.hasNoEntities = true;
		//else
		{
			ArrayList<ArrayList<Integer>> listOfWIofEntities = new ArrayList<ArrayList<Integer>>();
			for ( int e=0; e<curSen.listOfEntities.size(); e++ ) {
				Entity objEntity = curSen.listOfEntities.get(e);
				if ( Sentence.satisfyRelArgConstraint(objEntity) ) {
					if ( objEntity.startIndex < negSI )
						noOfEntOnLeft++;
					else if ( objEntity.startIndex > negSI )
						noOfEntOnRight++;
				
					listOfWIofEntities.add(objEntity.getAllWordIndexes());
				}
			}
	
			if ( noOfEntOnLeft == 0 )
				clsFeatVal.allMentionOfInterestOnRight = true;
			else if ( noOfEntOnRight == 0 )
				clsFeatVal.allMentionOfInterestOnLeft = true;
			
			if ( noOfEntOnRight > 0 )
				clsFeatVal.hasAnyMentionOfInterestOnRight = true;
		
			// detect whether drugs are dependent of the verb governor
			if ( clsFeatVal.isVerbGovernorRoot )
				clsFeatVal.areAllMentionOfInterestDependOnVerbGovernor = true;
			else if ( clsFeatVal.wiOfNearestVerbGovernor > -1 ) {
				int count = 0;
				
				for ( int e=0; e<listOfWIofEntities.size(); e++ ) {
					if ( curSen.depTree.allNodesByWordIndex[clsFeatVal.wiOfNearestVerbGovernor]
				             .governAllWIsInList(listOfWIofEntities.get(e)) )
						count++;
				}
				
				if ( count == curSen.getNoOfMentionsOfInterest() )
					clsFeatVal.areAllMentionOfInterestDependOnVerbGovernor = true;
				else if ( count == curSen.getNoOfMentionsOfInterest()-1 )
					clsFeatVal.areAllButOneMentionOfInterestDependOnVerbGovernor = true;			
			}
/*			
			// detect whether drugs are dependent of the immediate governor
			if ( clsFeatVal.wiOfImmediateGovernor > -1 ) {
				int count = 0;
				for ( int e=0; e<curSen.listOfEntities.size(); e++ )
					if ( curSen.depTree.allNodesByWordIndex[clsFeatVal.wiOfImmediateGovernor]
							.allGrandChildrenWordIndex.containsAll(listOfWIofEntities.get(e)) )
						count++;
				
				if ( count == curSen.listOfEntities.size() )
					clsFeatVal.areAllDrugsDependOnImmeidateGovernor = true;
				else if ( count == curSen.listOfEntities.size()-1 )
					clsFeatVal.areAllButOneDrugsDependOnImmeidateGovernor = true;			
			}			
	*/	}
		
		if ( clsFeatVal.wiOfImmediateGovernor == clsFeatVal.wiOfNearestVerbGovernor && clsFeatVal.wiOfImmediateGovernor > -1 )
			clsFeatVal.immediateGovernorIsVerbGovernor = true;			
		
		if ( wordsBeforeNeg.toLowerCase().matches(".*[^a-zA-Z,;]*(although|though|despite|in spite)[^a-zA-Z,;]*.*") )
			clsFeatVal.isAlthoughPrecedeNegationInSameClause = true;
		
		if ( wordsBeforeNeg.contains(",") )
			clsFeatVal.hasCommaAfterPrevMentionOfInterest = true;
		if ( wordsAfterNeg.contains(",") )
			clsFeatVal.hasCommaBeforeNextMentionOfInterest = true;
		
		if ( clsFeatVal.wiOfNegation > -1 )
			clsFeatVal.negWord = curSen.arrLemmasByParser[clsFeatVal.wiOfNegation].toLowerCase();
		if ( clsFeatVal.wiOfImmediateGovernor > -1 )
			clsFeatVal.immediateGovernor = curSen.arrLemmasByParser[clsFeatVal.wiOfImmediateGovernor].toLowerCase();
		if ( clsFeatVal.wiOfNearestVerbGovernor > -1 )
			clsFeatVal.nearestVerbGovernor = curSen.arrLemmasByParser[clsFeatVal.wiOfNearestVerbGovernor].toLowerCase();
		
		return clsFeatVal;
	}
}