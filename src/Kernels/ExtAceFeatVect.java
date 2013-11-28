package Kernels;

import java.io.IOException;
import java.util.ArrayList;
import Kernels.TKOutputPST;
import Structures.Entity;
import Structures.PairFilterCriteria;
import Structures.PhraseStrucTreeNode;
import Structures.PhraseStructureTree;
import Structures.Sentence;
import Utility.FileUtility;
import Utility.TextUtility;
import Clause.ClauseAnalyser;

public class ExtAceFeatVect {
	/*
	 * public static void main ( String args[] ) throws Exception {
	 * 
	 * ArrayList<FeatVal> listOfInstances = new ArrayList<FeatVal>(); //new
	 * ExtSunEtAl2011ACL().generateFeatureVectors(parsedFileName,
	 * fullDataFileName, outputFile, isResolveOverlappingEntities,
	 * relToBeConsidered, inClauseBoundFileName, entPairFileName,
	 * depParsedFile);
	 * 
	 * new GenARFF().convertFeatVectToARFF(listOfInstances); }
	 */

	/**
	 * Additional features -
	 * - whether two entities reside in two different EDU
	 * - whether they reside in different EDU and there are other entities between them
	 * - number of EDUs separating them
	 * - each entity type after the 1st candidate entity inside the corresponding EDU
	 * - each entity type before the 2nd candidate entity inside the corresponding EDU
	 * - each entity type between the two candidate entities inside the other EDUs between their EDUs
	 * - pos tag of the last word of the 1st EDU if the two EDUs are consecutive
	 * - pos tag of the 1st word of the 2nd EDU if the two EDUs are consecutive
	 * 
	 * Chan & Roth, COLING 2010   
	 * art: Ei ∈{gpe, org, per}, Ej ∈{fac, gpe, veh, wea} 
	 * emp-org: Ei ∈{gpe, org, per}, Ej ∈{gpe, org, per} 
	 * gpe-aff: Ei ∈{gpe, org, per}, Ej ∈{gpe, loc} 
	 * other-aff: Ei ∈{gpe, org, per}, Ej ∈{gpe, loc} 
	 * per-soc: Ei ∈{per}, Ej ∈{per}
	 * 
	 * - co-referrring mentions should not be used to form a pair
	 * @throws IOException 
	 */
	
	
	public static void init() throws IOException {
		GenericFeatVect.init();
	}
	
	static String[] listOfCountires = new String[0];
	
	
	/**
	 * 
	 * @param listOfInstances
	 * @param outFileName
	 * @throws IOException 
	 */
	public void populateFeatListFromFeatValObjects( ArrayList<FeatVal> listOfInstances, String outFileName ) throws IOException {
	
		if ( !TextUtility.isEmptyString(outFileName) )
			FileUtility.writeInFile(outFileName, "", false);
		
		System.out.println("Tot. instance = " + listOfInstances.size());
			// addNewFeatureInList(feature, listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);	
		for ( int i=0; i<listOfInstances.size(); i++ ) {
	
			StringBuilder sb = new StringBuilder();
			ArrayList<Integer> listFeatIndsOfCurInp = new ArrayList<Integer>(), listFeatCountOfCurInp = new  ArrayList<Integer>();
		
			FeatVal clsCurInstance = listOfInstances.get(i);
			
			convertFeatVectToArray(clsCurInstance, listFeatIndsOfCurInp, listFeatCountOfCurInp, false);
		
			GenericFeatVect.sortFeatValByIndx(listFeatIndsOfCurInp, listFeatCountOfCurInp);
			String str = GenericFeatVect.convertVectorOfFeatValToString(listFeatIndsOfCurInp, listFeatCountOfCurInp, null, GenericFeatVect.listOfGlobalFeatures, GenericFeatVect.listOfGlobalFeatWeight);
			sb = sb.append(TKOutputDT.getOutputForSingleInstance( str + "\n", clsCurInstance.classLabel));
			
			if ( clsCurInstance.classLabel )
				TKOutputPST.totalRelPos++;
			else
				TKOutputPST.totalRelNeg++;
			
			if ( !TextUtility.isEmptyString(outFileName) )
				FileUtility.writeInFile(outFileName, sb.toString(), true);
		}
		
		if ( !TextUtility.isEmptyString(outFileName) )
			System.out.println( TextUtility.now() + ": Instances are written as feature-index:count in " + outFileName);
		//FileUtility.writeInFile(outFileName, sb.toString(), false);
	}

	
	/**
	 * 
	 * @param clsCurInstance
	 * @param listFeatIndsOfCurInp
	 * @param listFeatCountOfCurInp
	 * @param filterSomeLexicalFeatures
	 */
	private void convertFeatVectToArray ( FeatVal clsCurInstance, 
			ArrayList<Integer> listFeatIndsOfCurInp, ArrayList<Integer> listFeatCountOfCurInp, boolean filterSomeLexicalFeatures ) {
				
		if ( clsCurInstance.WBNULL )
			GenericFeatVect.addNewFeatureInList( new String[]{"WBNULL"}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);

		GenericFeatVect.addNewFeatureInList( new String[]{"WBFL="+clsCurInstance.WBFL}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);

		GenericFeatVect.addNewFeatureInList( new String[]{"WBF="+clsCurInstance.WBF}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);

		GenericFeatVect.addNewFeatureInList( new String[]{"WBL="+clsCurInstance.WBL}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"WBO="+clsCurInstance.WBO}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"BM1F="+clsCurInstance.BM1F}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"BM1L="+clsCurInstance.BM1L}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"AM2F="+clsCurInstance.AM2F}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"AM2L="+clsCurInstance.AM2L}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		if ( clsCurInstance.tot_MB > 0 )
			GenericFeatVect.addNewFeatureInList( new String[]{"#MB"}, clsCurInstance.tot_MB,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		if ( clsCurInstance.tot_WB > 0 )
			GenericFeatVect.addNewFeatureInList( new String[]{"#WB"}, clsCurInstance.tot_WB,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		if ( !filterSomeLexicalFeatures ) {					
			
			for ( int e=0; e<clsCurInstance.WM1.length; e++ )
				GenericFeatVect.addNewFeatureInList( new String[]{"WM1=" + clsCurInstance.WM1[e]}, 1,
						listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"HM1=" + clsCurInstance.HM1}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			 
			for ( int e=0; e<clsCurInstance.WM2.length; e++ )
				GenericFeatVect.addNewFeatureInList( new String[]{"WM2=" + clsCurInstance.WM2[e]}, 1,
						listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"HM2=" + clsCurInstance.HM2}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"HM12=" + clsCurInstance.HM12}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"ET12="+clsCurInstance.ET12}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"ML12="+clsCurInstance.ML12}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			if ( clsCurInstance.M1inM2 )
				GenericFeatVect.addNewFeatureInList( new String[]{"M1inM2"}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			if ( clsCurInstance.M2inM1 )
				GenericFeatVect.addNewFeatureInList( new String[]{"M2inM1"}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"ET12M1inM2="+clsCurInstance.ET12M1inM2}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"ET12M2inM1="+clsCurInstance.ET12M2inM1}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"HM12M1inM2="+clsCurInstance.HM12M1inM2}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"HM12M2inM1="+clsCurInstance.HM12M2inM1}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"ET1DW1="+clsCurInstance.ET1DW1}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"H1DW1="+clsCurInstance.H1DW1}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"ET2DW2="+clsCurInstance.ET2DW2}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"H2DW2="+clsCurInstance.H2DW2}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"ET1Country="+clsCurInstance.ET1Country}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
			GenericFeatVect.addNewFeatureInList( new String[]{"CountryET2="+clsCurInstance.CountryET2}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		}
		
		if ( clsCurInstance.CPHBNULL )
			GenericFeatVect.addNewFeatureInList( new String[]{"CPHBNULL"}, 1,
					listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
			
		GenericFeatVect.addNewFeatureInList( new String[]{"CPHBFL="+clsCurInstance.CPHBFL}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"CPHBF="+clsCurInstance.CPHBF}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"CPHBL="+clsCurInstance.CPHBL}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"CPHBO="+clsCurInstance.CPHBO}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"CPHBM1F="+clsCurInstance.CPHBM1F}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"CPHBM1L="+clsCurInstance.CPHBM1L}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"CPHBM2F="+clsCurInstance.CPHBM2F}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"CPHBM2L="+clsCurInstance.CPHBM2L}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"CPP="+clsCurInstance.CPP}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"CPPH="+clsCurInstance.CPPH}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"CPHBM2L="+clsCurInstance.CPHBM2L}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);	
		
		GenericFeatVect.addNewFeatureInList( new String[]{"ET12SameNP="+clsCurInstance.ET12SameNP}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"ET12SamePP="+clsCurInstance.ET12SamePP}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"ET12SameVP="+clsCurInstance.ET12SameVP}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"PTP="+clsCurInstance.PTP}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);
		
		GenericFeatVect.addNewFeatureInList( new String[]{"PTPH="+clsCurInstance.PTPH}, 1,
				listFeatIndsOfCurInp, listFeatCountOfCurInp, 1);	
	}

	/**
	 * 
	 * @param parsedFileName
	 * @param fullDataFileName
	 * @param outputFile
	 * @param isResolveOverlappingEntities
	 * @param relToBeConsidered
	 * @param inClauseBoundFileName
	 * @param entPairFileName
	 * @param depParsedFile
	 * @return
	 * @throws Exception
	 */
	public void generateFeatureVectors( ArrayList<Sentence> listSentence, String outputFile,
			ClauseAnalyser.eDataFilterOption relToBeConsidered,
			String inClauseBoundFileName )
			throws Exception {

		PairFilterCriteria.hasOverlap = false;
		
		FileUtility.writeInFile(outputFile, "", false);
		TKOutputPST.totalRelPos = 0;
		TKOutputPST.totalRelNeg = 0;

		ArrayList<FeatVal> listOfInstances = new ArrayList<FeatVal>();

		int[][] arrClauseBoundOfSen = new TKOutputPST()
				.getClauseBoundOfAllSen(inClauseBoundFileName);

		for (int s = 0; s < listSentence.size(); s++) {

			Sentence objCurSen = listSentence.get(s);
			
			for (int k = 0; k < objCurSen.listRels.size(); k++) {
				int xx = 0;

				if (!objCurSen.listRels.get(k).arg1.equals(objCurSen.listRels
						.get(k).arg2)) {
					for (int e = 0; xx < 2
							&& e < objCurSen.listOfEntities.size(); e++) {
						if (objCurSen.listRels.get(k).arg1
								.equals(objCurSen.listOfEntities.get(e).id)
								|| objCurSen.listRels.get(k).arg2
										.equals(objCurSen.listOfEntities.get(e).id))
							xx++;
					}

					if (xx < 2)
						// System.out.println(
						objCurSen.listRels.get(k);
				}
			}

			int senIndex = TKOutputPST.listAllSenIDs.indexOf(objCurSen.senID);

			if (objCurSen.listOfEntities.size() > 1) {
				listOfInstances.addAll(generateFeatureVectorsForSen(objCurSen,
						relToBeConsidered,
						senIndex > 0 ? arrClauseBoundOfSen[senIndex] : null));
			}
		}

		populateFeatListFromFeatValObjects(listOfInstances, outputFile);
	}

	/**
	 * 
	 * @param objCurSen
	 * @param pst
	 * @param relToBeConsidered
	 * @param arrClauseBoundOfSen
	 * @param entPairFileName
	 * @param objDepParseOfSen
	 * @return
	 * @throws IOException
	 */
	private ArrayList<FeatVal> generateFeatureVectorsForSen(Sentence objCurSen,
			ClauseAnalyser.eDataFilterOption relToBeConsidered,
			int[] arrClauseBoundOfSen) throws IOException {

		ArrayList<FeatVal> listOfInstancesForCurSen = new ArrayList<FeatVal>();

		// System.out.println(senID);
		// for each pair of entities, find minimal subtrees and output it with 1
		// or 0
		// 1 represents there exists a relation between those entities
		for (int r = 0; r < objCurSen.listRels.size(); r++) {
			Entity e1 = objCurSen.getEntityById(objCurSen.listRels.get(r).arg1);
			Entity e2 = objCurSen.getEntityById(objCurSen.listRels.get(r).arg2);
			if(e1 == null) 
				System.out.println("Null entity " + objCurSen.listRels.get(r).arg1 + " found in " + objCurSen.senID);
			else if(e2 == null)
				System.out.println("Null entity " + objCurSen.listRels.get(r).arg2 + " found in " + objCurSen.senID);
			
			if( e1 == null || e2 == null )
				continue;
				
//			System.out.println(e2);
			if ( e1.startIndex > e2.startIndex ) {
				Entity tmp = e1;
				e1 = e2;
				e2 = tmp;
			}

			// checking relation type
			if (TKOutputPST.skipInstance(arrClauseBoundOfSen,
					relToBeConsidered, e1, e2, objCurSen, r)) {
				if (objCurSen.listRels.get(r).isPositive
				// &&
				// hasAntiPositiveGovernor(e1, objCurSen, dt) != null &&
				// hasAntiPositiveGovernor(e2, objCurSen, dt) != null
				) {
					String str = objCurSen.senID + ": " + e1.name +
							 " || " + e2.name + "\n";
					 
					// skipInstance(arrClauseBoundOfSen, relToBeConsidered, e1,
					// e2, objCurSen);
					str += objCurSen.text + "\n\n";
					FileUtility.writeInFile("out/discarded", str, true);
				}
				continue;
			}

			FeatVal clsFeatVal = getFeatureValueForCurEntPairs(objCurSen, e1,
					e2);
			clsFeatVal.classLabel = objCurSen.listRels.get(r).isPositive;

			if ( clsFeatVal.classLabel )
				TKOutputPST.totalRelPos = 0;
			else
				TKOutputPST.totalRelNeg = 0;

			listOfInstancesForCurSen.add(clsFeatVal);
		}

		return listOfInstancesForCurSen;
	}

	
	
	/**
	 * This method will return features that might be merged with other features for the same pair of mentions
	 * 
	 * @param curSen
	 * @param ent1
	 * @param ent2
	 * @param listFeatIndsOfCurInp
	 * @param listFeatCountOfCurInp
	 * @return
	 * @throws IOException
	 */
	public void getZhouEtAl2005FeatVal(Sentence curSen, Entity ent1,
			Entity ent2, ArrayList<Integer> listFeatIndsOfCurInp, ArrayList<Integer> listFeatCountOfCurInp 
	) throws IOException{
				
		FeatVal clsFeatVal = getFeatureValueForCurEntPairs(curSen, ent1, ent2);
				
		convertFeatVectToArray(clsFeatVal, listFeatIndsOfCurInp, listFeatCountOfCurInp, true);
	}	
	
	
	/**
	 * 
	 * @param curSen
	 * @param ent1
	 * @param ent2
	 * @return
	 * @throws IOException
	 */
	private FeatVal getFeatureValueForCurEntPairs(Sentence curSen, Entity ent1,
			Entity ent2) throws IOException {

		ArrayList<Integer> listOfWIforEnt1 =  ent1.getAllWordIndexes();
		ArrayList<Integer> listOfWIforEnt2 =  ent2.getAllWordIndexes();

		int hWIofEnt1 = PhraseStructureTree.getHeadWordIndxOfNP(curSen, listOfWIforEnt1), 
				hWIofEnt2 = PhraseStructureTree.getHeadWordIndxOfNP(curSen, listOfWIforEnt2);

		FeatVal clsFeatVal = extractLexicalFeatures(ent1, hWIofEnt1, ent2,
				hWIofEnt2, curSen, listOfWIforEnt1, listOfWIforEnt2);

		clsFeatVal = extractPhrasalFeatures(clsFeatVal, ent1, hWIofEnt1, ent2, hWIofEnt2,
				curSen, listOfWIforEnt1, listOfWIforEnt2);
		clsFeatVal = extractDependencyFeatures(clsFeatVal, ent1, hWIofEnt1, ent2, hWIofEnt2,
				curSen, listOfWIforEnt1, listOfWIforEnt2);

		return clsFeatVal;
	}

	
	/**
	 * 
	 * @param clsFeatVal
	 * @param ent1
	 * @param hWIofEnt1
	 * @param ent2
	 * @param hWIofEnt2
	 * @param curSen
	 * @param listOfWIforEnt1
	 * @param listOfWIforEnt2
	 * @return 
	 */
	private FeatVal extractDependencyFeatures(FeatVal clsFeatVal, Entity ent1,
			int hWIofEnt1, Entity ent2, int hWIofEnt2, Sentence curSen,
			ArrayList<Integer> listOfWIforEnt1,
			ArrayList<Integer> listOfWIforEnt2) {

		String DW1 = "";

		for (int i = 0; i < listOfWIforEnt1.size(); i++)
			if(curSen.depTree.allNodesByWordIndex[listOfWIforEnt1.get(i)].getChildren() != null){
			for (int c = 0;  c < curSen.depTree.allNodesByWordIndex[listOfWIforEnt1
					.get(i)].getChildren().size(); c++)
				DW1 += " "
						+ curSen.depTree.allNodesByWordIndex[listOfWIforEnt1
								.get(i)].getChildren().get(c).lemma;}

		DW1 = DW1.trim().replaceAll(" ", "#");

		// ET1DW1: combination of the entity type and the dependent word for M1
		clsFeatVal.ET1DW1 = ent1.getNEcategory() + "*" + DW1;

		// H1DW1: combination of the head word and the dependent word for M1
		clsFeatVal.H1DW1 = clsFeatVal.HM1 + "*" + DW1;

		String DW2 = "";

		for (int i = 0; i < listOfWIforEnt2.size(); i++)
			if(curSen.depTree.allNodesByWordIndex[listOfWIforEnt2.get(i)].getChildren() != null){
			for (int c = 0; c < curSen.depTree.allNodesByWordIndex[listOfWIforEnt2
					.get(i)].getChildren().size(); c++)
				DW2 += " "
						+ curSen.depTree.allNodesByWordIndex[listOfWIforEnt2
								.get(i)].getChildren().get(c).lemma;}

		DW2 = DW2.trim().replaceAll(" ", "#");

		// ET2DW2: combination of the entity type and the dependent word for M2
		clsFeatVal.ET2DW2 = ent2.getNEcategory() + "*" + DW2;

		// H2DW2: combination of the head word and the dependent word for M2
		clsFeatVal.H2DW2 = ent2.getNEcategory() + "*" + DW2;

		/**
		 * Parse tree features
		 */

		ArrayList<Integer> allWordsOfBothMentions = listOfWIforEnt1;
		allWordsOfBothMentions.addAll(listOfWIforEnt2);

		PhraseStrucTreeNode parent = curSen.psgTree
				.getParentPhraseNode(allWordsOfBothMentions);

		// ET12SameNP: combination of ET12 and whether M1 and M2 included in the
		// same NP
		clsFeatVal.ET12SameNP = clsFeatVal.ET12 + "**"
				+ parent.pos.endsWith("NP");

		// ET12SamePP: combination of ET12 and whether M1 and M2 exist in the
		// same PP
		clsFeatVal.ET12SamePP = clsFeatVal.ET12 + "**"
				+ parent.pos.endsWith("PP");

		// ET12SameVP: combination of ET12 and whether M1 and M2 included in the
		// same VP
		clsFeatVal.ET12SamePP = clsFeatVal.ET12 + "**"
				+ parent.pos.endsWith("VP");

		//if ( xx==26 )
		//System.out.println(xx);
	
		PhraseStrucTreeNode pM1 = curSen.psgTree
				.getParentPhraseNode(listOfWIforEnt1);
		PhraseStrucTreeNode pM2 = curSen.psgTree
				.getParentPhraseNode(listOfWIforEnt2);

		ArrayList<String> listOfPhrasesFromM1toM2 = new ArrayList<String>();

		while (pM1.nodeIndex != parent.nodeIndex) {
			if (listOfPhrasesFromM1toM2.size() == 0
					|| !listOfPhrasesFromM1toM2.get(
							listOfPhrasesFromM1toM2.size() - 1).equals(pM1.pos))
				listOfPhrasesFromM1toM2.add(pM1.pos);
			pM1 = pM1.parent;
		}

		ArrayList<String> temp = new ArrayList<String>();
		while (pM2.nodeIndex != parent.nodeIndex) {
			if (temp.size() == 0 || !temp.get(temp.size() - 1).equals(pM2.pos))
				temp.add(pM2.pos);
			
			if ( pM2.parent == null )
				break;
			pM2 = pM2.parent;
		}

		for (int i = temp.size() - 2; i >= 0; i--)
			listOfPhrasesFromM1toM2.add(temp.get(i));

		// PTP: path of phrase labels (removing duplicates) connecting M1 and M2
		// in the parse tree
		for (int i = 0; i < listOfPhrasesFromM1toM2.size(); i++)
			clsFeatVal.PTP += " " + listOfPhrasesFromM1toM2.get(i);
		clsFeatVal.PTP = clsFeatVal.PTP.trim().replaceAll(" ", "*");

		ArrayList<Integer> allWordsOfParent = new ArrayList<Integer>();
		int w = 0;
		for (int i = 0; i < parent.getAllTerminalNodeIndexesUnderThisNode().size(); i++) {
			for (; w < curSen.psgTree.listOfNodesByWIs.size(); w++) {
				if (curSen.psgTree.listOfNodesByWIs.get(w).nodeIndex == parent.getAllTerminalNodeIndexesUnderThisNode()
						.get(i)) {
					allWordsOfParent
							.add(curSen.psgTree.listOfNodesByWIs.get(w).wordIndexByParser);
					break;
				}
			}
		}

		// PTPH: path of phrase labels (removing duplicates) connecting M1 and
		// M2 in the parse tree
		// augmented with the head word of the top phrase in the path.
		clsFeatVal.PTPH = clsFeatVal.PTP + "$"
				+ PhraseStructureTree.getHeadWordIndxOfNP(curSen, allWordsOfParent);

		if (listOfCountires.length == 0) {
			ArrayList<String> listTemp = FileUtility
					.readNonEmptyFileLines("db/country_names");
			listOfCountires = new String[listTemp.size()];
			for (int i = 0; i < listOfCountires.length; i++)
				listOfCountires[i] = listTemp.get(i).toLowerCase();
		}

		/**
		 * Other features
		 */
		// ET1Country: the entity type of M1 when M2 is a country name
		for (int i = 0; i < listOfCountires.length; i++)
			if (listOfCountires[i].equalsIgnoreCase(ent1.name)) {
				clsFeatVal.ET1Country = ent1.getNEcategory();
				break;
			}

		// CountryET2: the entity type of M2 when M1 is a country name
		for (int i = 0; i < listOfCountires.length; i++)
			if (listOfCountires[i].equalsIgnoreCase(ent2.name)) {
				clsFeatVal.CountryET2 = ent2.getNEcategory();
				break;
			}

		return clsFeatVal;
	}

	/**
	 * 
	 * @param clsFeatVal
	 * @param ent1
	 * @param hWIofEnt1
	 * @param ent2
	 * @param hWIofEnt2
	 * @param curSen
	 * @param listOfWIforEnt1
	 * @param listOfWIforEnt2
	 * @return 
	 */
	private FeatVal extractPhrasalFeatures(FeatVal clsFeatVal, Entity ent1,
			int hWIofEnt1, Entity ent2, int hWIofEnt2, Sentence curSen,
			ArrayList<Integer> listOfWIforEnt1,
			ArrayList<Integer> listOfWIforEnt2) {

		// CPHBNULL when no phrase in between
		clsFeatVal.CPHBNULL = true;
		ArrayList<String> listOfPhraseInBet = new ArrayList<String>();
		//System.out.println(curSen.senID);
		for (int i = listOfWIforEnt1.get(listOfWIforEnt1.size() - 1) + 1; i < listOfWIforEnt2
				.get(0); i++) {
			if (curSen.arrPhrasalChunks[i].startsWith("B-")) {
				clsFeatVal.CPHBNULL = false;
				listOfPhraseInBet.add(curSen.arrPhrasalChunks[i].replace("B-",
						""));
			}
		}

		// CPHBFL: the only phrase head when only one phrase in between
		if (listOfPhraseInBet.size() == 1)
			clsFeatVal.CPHBFL = listOfPhraseInBet.get(0);

		// CPHBF: first phrase head in between when at least two phrases in
		// between
		if (listOfPhraseInBet.size() == 2)
			clsFeatVal.CPHBF = listOfPhraseInBet.get(0);

		// CPHBL: last phrase head in between when at least two phrase heads in
		// between
		if (listOfPhraseInBet.size() == 2)
			clsFeatVal.CPHBL = listOfPhraseInBet.get(0);

		// CPHBO: other phrase heads in between except first and last phrase
		// heads when at least three phrases in between
		if (listOfPhraseInBet.size() > 2) {
			clsFeatVal.CPHBO = "";
			for (int i = 1; i < listOfPhraseInBet.size() - 1; i++)
				clsFeatVal.CPHBO += "#" + listOfPhraseInBet.get(i);
		}

		// CPHBM1F: first phrase head before M1
		clsFeatVal.CPHBM1F = "";
		for (int i = listOfWIforEnt1.get(0) - 1; i >= 0; i--) {
			if (curSen.arrPhrasalChunks[i].startsWith("B-")) {
				clsFeatVal.CPHBM1F = curSen.arrPhrasalChunks[i].replace("B-",
						"");
				break;
			}
		}

		// CPHBM1L: second phrase head before M1
		clsFeatVal.CPHBM1L = "";
		boolean nextPh = false;
		for (int i = listOfWIforEnt1.get(0) - 1; i >= 0; i--) {
			if (curSen.arrPhrasalChunks[i].startsWith("B-")) {
				if (nextPh) {
					clsFeatVal.CPHBM1F = curSen.arrPhrasalChunks[i].replace(
							"B-", "");
					break;
				} else {
					nextPh = true;
				}
			}
		}

		// CPHAM2F: first phrase head after M2
		clsFeatVal.CPHBM2F = "";
		for (int i = listOfWIforEnt2.get(listOfWIforEnt2.size() - 1) + 1; i < curSen.arrPhrasalChunks.length; i++) {
			if (curSen.arrPhrasalChunks[i].startsWith("B-")) {
				clsFeatVal.CPHBM2F = curSen.arrPhrasalChunks[i].replace("B-",
						"");
				break;
			}
		}

		// CPHAM2L: second phrase head after M2
		clsFeatVal.CPHBM2L = "";
		nextPh = false;
		for (int i = listOfWIforEnt2.get(listOfWIforEnt2.size() - 1) + 1; i < curSen.arrPhrasalChunks.length; i++) {
			if (curSen.arrPhrasalChunks[i].startsWith("B-")) {
				if (nextPh) {
					clsFeatVal.CPHBM2F = curSen.arrPhrasalChunks[i].replace(
							"B-", "");
					break;
				} else {
					nextPh = true;
				}
			}
		}

		// CPP: path of phrase labels connecting the two mentions in the
		// chunking
		clsFeatVal.CPP = "";
		clsFeatVal.CPP = curSen.arrPhrasalChunks[listOfWIforEnt1.get(0)]
				.replaceAll("[BI]-", "");
		if (clsFeatVal.CPHBNULL)
			;
		else {
			for (int i = 0; i < listOfPhraseInBet.size(); i++)
				clsFeatVal.CPP += " " + listOfPhraseInBet.get(i);

			clsFeatVal.CPP += " "
					+ curSen.arrPhrasalChunks[listOfWIforEnt2
							.get(listOfWIforEnt2.size() - 1)].replaceAll(
							"[BI]-", "");
		}

		clsFeatVal.CPP = clsFeatVal.CPP.trim().replaceAll(" ", "#");

		// CPPH: path of phrase labels connecting the two mentions in the
		// chunking augmented with head words, if at most two phrases in between
		clsFeatVal.CPPH = clsFeatVal.HM1 + "$$" + clsFeatVal.CPP + "$$"
				+ clsFeatVal.HM2;
		return clsFeatVal;
	}

	/**
	 * 
	 * @param ent1
	 * @param hWIofEnt1
	 * @param ent2
	 * @param hWIofEnt2
	 * @param curSen
	 * @param listFeatIndsOfCurInp
	 * @param listFeatCountOfCurInp
	 * @param listOfWIforEnt1
	 * @param listOfWIforEnt2
	 */
	private FeatVal extractLexicalFeatures(Entity ent1, int hWIofEnt1,
			Entity ent2, int hWIofEnt2, Sentence curSen,
			ArrayList<Integer> listOfWIforEnt1,
			ArrayList<Integer> listOfWIforEnt2) {

		FeatVal clsFeatVal = new FeatVal();

		clsFeatVal.entId_1 = ent1.id;
		clsFeatVal.entId_2 = ent2.id;
		
		if (listOfWIforEnt1.size() > 0) {
			clsFeatVal.WM1 = new String[listOfWIforEnt1.size()];
			for (int i = 0; i < listOfWIforEnt1.size(); i++)
				clsFeatVal.WM1[i] = curSen.arrWordAndPosByParser[listOfWIforEnt1.get(i)][0];
		}
		
		clsFeatVal.HM1 = curSen.arrLemmasByParser[hWIofEnt1];
		
		if (listOfWIforEnt2.size() > 0) {
			clsFeatVal.WM2 = new String[listOfWIforEnt2.size()];
			for (int i = 0; i < listOfWIforEnt2.size(); i++)
				clsFeatVal.WM2[i] = curSen.arrWordAndPosByParser[listOfWIforEnt2.get(i)][0];
		}

		clsFeatVal.HM2 = curSen.arrLemmasByParser[hWIofEnt2];

		clsFeatVal.HM12 = curSen.arrLemmasByParser[hWIofEnt1] + ":"
				+ curSen.arrLemmasByParser[hWIofEnt2];

		// System.out.println(clsFeatVal.WM1 + " " + clsFeatVal.WM2);

		if (listOfWIforEnt2.get(0)
				- listOfWIforEnt1.get(listOfWIforEnt1.size() - 1) < 2)
			clsFeatVal.WBNULL = true;

		if (listOfWIforEnt2.get(0)
				- listOfWIforEnt1.get(listOfWIforEnt1.size() - 1) == 2)
			clsFeatVal.WBFL = curSen.arrWordAndPosByParser[listOfWIforEnt2
					.get(0) - 1][0].toLowerCase();
		// System.out.println(clsFeatVal.WBFL);
		if (listOfWIforEnt2.get(0)
				- listOfWIforEnt1.get(listOfWIforEnt1.size() - 1) > 2)
			clsFeatVal.WBF = curSen.arrWordAndPosByParser[listOfWIforEnt1
					.get(listOfWIforEnt1.size() - 1) + 1][0].toLowerCase();

		if (listOfWIforEnt2.get(0)
				- listOfWIforEnt1.get(listOfWIforEnt1.size() - 1) > 2)
			clsFeatVal.WBL = curSen.arrWordAndPosByParser[listOfWIforEnt2
					.get(0) - 1][0].toLowerCase();

		if (listOfWIforEnt1.get(listOfWIforEnt1.size() - 1) + 1 < listOfWIforEnt2
				.get(0))
			clsFeatVal.WBO = "";
		for (int i = listOfWIforEnt1.get(listOfWIforEnt1.size() - 1) + 1; i < listOfWIforEnt2
				.get(0); i++)
			clsFeatVal.WBO += "&#"
					+ curSen.arrWordAndPosByParser[i][0].toLowerCase();

		clsFeatVal.WBO = clsFeatVal.WBO.trim().replaceFirst("&#", "");

		int x = 0;

		if ((x = listOfWIforEnt1.get(0) - 1) > 0)
			clsFeatVal.BM1F = curSen.arrWordAndPosByParser[x][0].toLowerCase();

		if ((x = listOfWIforEnt1.get(0) - 2) > 0)
			clsFeatVal.BM1L = curSen.arrWordAndPosByParser[x][0].toLowerCase();

		if ((x = listOfWIforEnt2.get(listOfWIforEnt2.size() - 1) + 1) < curSen.arrWordAndPosByParser.length)
			clsFeatVal.AM2F = curSen.arrWordAndPosByParser[x][0].toLowerCase();

		if ((x = listOfWIforEnt2.get(listOfWIforEnt2.size() - 1) + 2) < curSen.arrWordAndPosByParser.length)
			clsFeatVal.AM2L = curSen.arrWordAndPosByParser[x][0].toLowerCase();

		// ET12: combination of mention entity types
		clsFeatVal.ET12 = ent1.getNEcategory() + ":" + ent2.getNEcategory();
		int indexOfStart1 = listOfWIforEnt1.indexOf(0);
		// ML12: combination of mention levels
		if (listOfWIforEnt1.size() == 1
				&& indexOfStart1 >= 0
				&& TextUtility
						.isPronoun(curSen.arrWordAndPosByParser[listOfWIforEnt1
								.indexOf(0)][0],
								curSen.arrWordAndPosByParser[listOfWIforEnt1
										.indexOf(0)][1]))
			clsFeatVal.ML12 = "Pronoun:";
		else if (ent1.name.contains("'")
				|| ent1.name
						.matches("(\\d+|\\d+\\s+.*|.*\\s+\\d+\\s+.*|.*\\s+\\d+|[^a-zA-Z]*)")
				|| (indexOfStart1 >= 0 && TextUtility
						.isPronoun(curSen.arrWordAndPosByParser[listOfWIforEnt1
								.indexOf(0)][0],
								curSen.arrWordAndPosByParser[listOfWIforEnt1
										.indexOf(0)][1])))
			clsFeatVal.ML12 = "NotName:";
		else
			clsFeatVal.ML12 = "Name:";
		int indexOfStart2 = listOfWIforEnt2.indexOf(0);
		if (listOfWIforEnt2.size() == 1
				&& indexOfStart2 >= 0
				&& TextUtility
						.isPronoun(curSen.arrWordAndPosByParser[listOfWIforEnt2
								.indexOf(0)][0],
								curSen.arrWordAndPosByParser[listOfWIforEnt2
										.indexOf(0)][1]))
			clsFeatVal.ML12 = "Pronoun";
		else if (ent2.name.contains("'")
				|| ent2.name
						.matches("(\\d+|\\d+\\s+.*|.*\\s+\\d+\\s+.*|.*\\s+\\d+|[^a-zA-Z]*)")
				|| (indexOfStart2 >= 0 && TextUtility
						.isPronoun(curSen.arrWordAndPosByParser[listOfWIforEnt2
								.indexOf(0)][0],
								curSen.arrWordAndPosByParser[listOfWIforEnt2
										.indexOf(0)][1])))
			clsFeatVal.ML12 = "NotName";
		else
			clsFeatVal.ML12 = "Name";

		// #MB: number of other mentions in between
		clsFeatVal.tot_MB = curSen.getNumberOfEntInBetween(ent1, ent2);

		// #WB: number of words in between
		clsFeatVal.tot_WB = listOfWIforEnt2.get(0)
				- listOfWIforEnt1.get(listOfWIforEnt1.size() - 1) - 1;
		if (clsFeatVal.tot_WB < 0)
			clsFeatVal.tot_WB = 0;

		// M1>M2 or M1<M2: flag indicating whether M2/M1is included in M1/M2
		clsFeatVal.M1inM2 = listOfWIforEnt2.containsAll(listOfWIforEnt1);
		clsFeatVal.M2inM1 = listOfWIforEnt1.containsAll(listOfWIforEnt2);

		// ET12+M1>M2; ET12+M1<M2;
		clsFeatVal.ET12M1inM2 = clsFeatVal.ET12 + clsFeatVal.M1inM2;
		clsFeatVal.ET12M2inM1 = clsFeatVal.ET12 + clsFeatVal.M2inM1;

		// HM12+M1>M2; HM12+M1<M2.
		clsFeatVal.HM12M1inM2 = clsFeatVal.HM12 + clsFeatVal.M1inM2;
		clsFeatVal.HM12M2inM1 = clsFeatVal.HM12 + clsFeatVal.M2inM1;

		return clsFeatVal;
	}
}

// ===========================================

class FeatVal {

	boolean classLabel = false;
	String entId_1 ="", entId_2 ="";

	String[] WM1 = new String[0];// : bag-of-words in M1
	String HM1 = "";// : head word of M1

	String[] WM2 = new String[0];// : bag-of-words in M2
	String HM2 = "";// : head word of M2
	String HM12 = "";// : combination of HM1 and HM2
	boolean WBNULL = false;// : when no word in between
	String WBFL = "";// : the only word in between when only one word in between
	String WBF = "";// : first word in between when at least two words in
					// between
	String WBL = "";// : last word in between when at least two words in between
	String WBO = "";// : other words in between except first and last words when
					// at least three words in between
	String BM1F = "";// : first word before M1
	String BM1L = "";// : second word before M1
	String AM2F = "";// : first word after M2
	String AM2L = "";// : second word after M2

	// ET12: combination of mention entity types
	String ET12 = "";

	// ML12: combination of mention levels
	String ML12 = "";

	// #MB: number of other mentions in between
	int tot_MB = 0;

	// #WB: number of words in between
	int tot_WB = 0;

	// M1>M2 or M1<M2: flag indicating whether M2/M1is included in M1/M2
	boolean M1inM2 = false;
	boolean M2inM1 = false;

	// ET12+M1>M2; ET12+M1<M2;
	String ET12M1inM2 = "";
	String ET12M2inM1 = "";

	// HM12+M1>M2; HM12+M1<M2.
	String HM12M1inM2 = "";
	String HM12M2inM1 = "";

	/**
	 * Phrase level features
	 */

	// CPHBNULL when no phrase in between
	boolean CPHBNULL = false;

	// CPHBFL: the only phrase head when only one phrase in between
	String CPHBFL = "";

	// CPHBF: first phrase head in between when at least two phrases in between
	String CPHBF = "";

	// CPHBL: last phrase head in between when at least two phrase heads in
	// between
	String CPHBL = "";

	// CPHBO: other phrase heads in between except first and last phrase heads
	// when at least three phrases in between
	String CPHBO = "";

	// CPHBM1F: first phrase head before M1
	String CPHBM1F = "";

	// CPHBM1L: second phrase head before M1
	String CPHBM1L = "";

	// CPHAM2F: first phrase head after M2
	String CPHBM2F = "";

	// CPHAM2L: second phrase head after M2
	String CPHBM2L = "";

	// CPP: path of phrase labels connecting the two mentions in the chunking
	String CPP = "";

	// CPPH: path of phrase labels connecting the two mentions in the chunking
	// augmented with head words, if at most two phrases in between
	String CPPH = "";

	/**
	 * Dependency tree features
	 */
	// ET1DW1: combination of the entity type and the dependent word for M1
	String ET1DW1 = "";

	// H1DW1: combination of the head word and the dependent word for M1
	String H1DW1 = "";

	// ET2DW2: combination of the entity type and the dependent word for M2
	String ET2DW2 = "";

	// H2DW2: combination of the head word and the dependent word for M2
	String H2DW2 = "";

	/**
	 * Parse tree features
	 */

	// ET12SameNP: combination of ET12 and whether M1 and M2 included in the
	// same NP
	String ET12SameNP = "";

	// ET12SamePP: combination of ET12 and whether M1 and M2 exist in the same
	// PP
	String ET12SamePP = "";

	// ET12SameVP: combination of ET12 and whether M1 and M2 included in the
	// same VP
	String ET12SameVP = "";

	// PTP: path of phrase labels (removing duplicates) connecting M1 and M2 in
	// the parse tree
	String PTP = "";

	// PTPH: path of phrase labels (removing duplicates) connecting M1 and M2 in
	// the parse tree
	// augmented with the head word of the top phrase in the path.
	String PTPH = "";

	/**
	 * Other features
	 */
	// ET1Country: the entity type of M1 when M2 is a country name
	String ET1Country = "";

	// CountryET2: the entity type of M2 when M1 is a country name
	String CountryET2 = "";
	
	
	/**
	 * Features taken from Chan & Roth, COLING 2010
	 */
	// TODO:
}