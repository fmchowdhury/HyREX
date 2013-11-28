package Kernels;

import java.io.IOException;
import java.util.ArrayList;

import org.fbk.it.hlt.bioRE.BinaryRelExtractor;
import org.fbk.it.hlt.bioRE.FormatConverterJSRE;
import org.fbk.it.hlt.bioRE.FormatConverterJSRE.eTokPosLemmaTagTarget;
import org.fbk.it.hlt.bioRE.multiStage.MultiStageRE;

import Structures.*;
import Utility.*;

import Clause.ClauseAnalyser;


public class FeatVecCreatorForSLkernel {
	
	public StringBuilder createSemEval2task8FormatInpPerSen( StringBuilder sbSemEval2task8Format, 
			Sentence objSen, boolean isResolveOverlappingEntities,
			ClauseAnalyser.eDataFilterOption relToBeConsidered, int[] arrClauseBoundOfSen, String entPairFileName ) throws IOException{
		
		// NOTE: jSRE can't handle overlapping entities.		
	
		String[][] wordAndPos = Common.checkTokensInOriginalSentence(objSen.arrWordAndPosByParser, objSen.text);
		//wordAndPos = new WordPosTokenizer().segmentPuncSymbolsWithinTokenizedData(wordAndPos);
		
		// for each pair of entities, create separate entry of the same sentence.
		// if there is a relation then relation type is YES, otherwise NO
	
		for ( int r=0; r < objSen.listRels.size(); r++ ){
			Entity e1 = objSen.getEntityById(objSen.listRels.get(r).arg1);
			Entity e2 = objSen.getEntityById(objSen.listRels.get(r).arg2);
			
		//	if ( (e1.id + " " + e2.id ).contains("APW20001127.1346.0419-E48.48-103 APW20001127.1346.0419-E44.44-105") )
			//	e1.name.trim();
			
		//	System.out.print("jsre " + e1.id);
		//	System.out.println( " " + e2.id);
	
			// NOTE: jSRE doesn't support RE from overlapping entities
			if ( DataStrucUtility.hasOverlap( e1.boundaries, e2.boundaries) )
				continue;
				
			// checking relation type
			if ( TKOutputPST.skipInstance(arrClauseBoundOfSen, relToBeConsidered, e1, e2, objSen, r) )
				continue;
			
			int lenOfAddedPart = 0, totTargetEnt = 0;
			
			String[] arrEntMarkers = new String[objSen.listOfEntities.size()];
			for ( int y=0; y<arrEntMarkers.length; y++ )
				arrEntMarkers[y] = "B-e";
		
			ArrayList<String[]> listOfWordPosLemmaTagTarget = new ArrayList<String[]>();
			ArrayList<Integer> listSindex = new ArrayList<Integer>();
			for ( int w=0; w<wordAndPos.length; w++ ){
				String[] wordPosLemmaTagTarget = new String[eTokPosLemmaTagTarget.values().length];
					
				wordPosLemmaTagTarget[eTokPosLemmaTagTarget.Token.ordinal()] = wordAndPos[w][0];
				wordPosLemmaTagTarget[eTokPosLemmaTagTarget.PoS.ordinal()] = wordAndPos[w][1];
				wordPosLemmaTagTarget[eTokPosLemmaTagTarget.Lemma.ordinal()] = SyntacticParser.getLemma( wordAndPos[w][0], wordAndPos[w][1]);
									
				boolean isNotEnt = true;
				for ( int z=0; z < objSen.listOfEntities.size(); z++ ) {
					if ( 
						( !objSen.listOfEntities.get(z).id.equals(e1.id) && DataStrucUtility.hasOverlap( objSen.listOfEntities.get(z).boundaries, e1.boundaries) )
						|| ( !objSen.listOfEntities.get(z).id.equals(e2.id) && DataStrucUtility.hasOverlap( objSen.listOfEntities.get(z).boundaries, e2.boundaries) )
					)
						continue;
					
					if ( lenOfAddedPart >= objSen.listOfEntities.get(z).startIndex && lenOfAddedPart <= objSen.listOfEntities.get(z).endIndex  ){
						wordPosLemmaTagTarget[eTokPosLemmaTagTarget.TagOfEntBoundary.ordinal()] = arrEntMarkers[z] + z;
						
						if ( objSen.listOfEntities.get(z).id.equals(e1.id) || objSen.listOfEntities.get(z).id.equals(e2.id) ) {
							wordPosLemmaTagTarget[eTokPosLemmaTagTarget.TargetEntIndicator.ordinal()] = "T";
							if ( arrEntMarkers[z].equals("B-e") )
								totTargetEnt++;
						}
						else
							wordPosLemmaTagTarget[eTokPosLemmaTagTarget.TargetEntIndicator.ordinal()] = "O";
						
						arrEntMarkers[z] = "I-e";
						isNotEnt = false;
						
						wordPosLemmaTagTarget[eTokPosLemmaTagTarget.SemanticTypeOfEnt.ordinal()] = 
							TextUtility.isEmptyString(objSen.listOfEntities.get(z).getNEcategory())
								? Common.DEFAULT_ENT_TYPE : objSen.listOfEntities.get(z).getNEcategory();
						break;
					}
				}
				
				if ( isNotEnt ) {
					wordPosLemmaTagTarget[eTokPosLemmaTagTarget.TagOfEntBoundary.ordinal()] = "O";
					wordPosLemmaTagTarget[eTokPosLemmaTagTarget.TargetEntIndicator.ordinal()] = "O";
					wordPosLemmaTagTarget[eTokPosLemmaTagTarget.SemanticTypeOfEnt.ordinal()] = "NONE";
				}
				
				listSindex.add(lenOfAddedPart);
				lenOfAddedPart += wordAndPos[w][0].length();
				
				listOfWordPosLemmaTagTarget.add(wordPosLemmaTagTarget);
			}
			
			/*
			 * Checking if an entity is not annotated because of the reason that it is part of a token but 
			 * itself not a token.
			 */
			for ( int z=0; z<arrEntMarkers.length; z++ ){
				if ( 
						( !objSen.listOfEntities.get(z).id.equals(e1.id) && DataStrucUtility.hasOverlap( objSen.listOfEntities.get(z).boundaries, e1.boundaries) )
						|| ( !objSen.listOfEntities.get(z).id.equals(e2.id) && DataStrucUtility.hasOverlap( objSen.listOfEntities.get(z).boundaries, e2.boundaries) )							
					)
						continue;
				
				if ( arrEntMarkers[z].equals("B-e") ){					
					for ( int w=0; w<listSindex.size(); w++ ){
						if ( !listOfWordPosLemmaTagTarget.get(w)[eTokPosLemmaTagTarget.TargetEntIndicator.ordinal()].equals("T")
								&&
								arrEntMarkers[z].equals("B-e") && listSindex.get(w) <= objSen.listOfEntities.get(z).startIndex && 
								( w == listSindex.size()-1 || listSindex.get(w+1) > objSen.listOfEntities.get(z).startIndex ) ){
							
							listOfWordPosLemmaTagTarget.get(w)[eTokPosLemmaTagTarget.TagOfEntBoundary.ordinal()] 
							                                   = arrEntMarkers[z] + z;
							
							if ( objSen.listOfEntities.get(z).id.equals(e1.id) || objSen.listOfEntities.get(z).id.equals(e2.id) )  {
								listOfWordPosLemmaTagTarget.get(w)[eTokPosLemmaTagTarget.TargetEntIndicator.ordinal()] 
								                                   = "T";
								if ( arrEntMarkers[z].equals("B-e") )
									totTargetEnt++;                                   
							}
							else
								listOfWordPosLemmaTagTarget.get(w)[eTokPosLemmaTagTarget.TargetEntIndicator.ordinal()] = "O";
							
							arrEntMarkers[z] = "I-e";							
						}
						else if ( arrEntMarkers[z].equals("I-e") && listSindex.get(w) <= objSen.listOfEntities.get(z).endIndex ){
							listOfWordPosLemmaTagTarget.get(w)[3] = arrEntMarkers[z] + z;
							
							if ( objSen.listOfEntities.get(z).id.equals(e1.id) || objSen.listOfEntities.get(z).id.equals(e2.id) )
								listOfWordPosLemmaTagTarget.get(w)[eTokPosLemmaTagTarget.TargetEntIndicator.ordinal()] 
								                                   = "T";
							else
								listOfWordPosLemmaTagTarget.get(w)[eTokPosLemmaTagTarget.TargetEntIndicator.ordinal()] = "O";
						}
						
						listOfWordPosLemmaTagTarget.get(w)[eTokPosLemmaTagTarget.SemanticTypeOfEnt.ordinal()] = 
							TextUtility.isEmptyString(objSen.listOfEntities.get(z).getNEcategory()) 
								? Common.DEFAULT_ENT_TYPE : objSen.listOfEntities.get(z).getNEcategory();
					}
				}
			}
			
			
			//-----------------
				
			String convertedData = "Sentence: " + objSen.senID + "\n";
			for ( int w=0; w<listOfWordPosLemmaTagTarget.size(); w++ ) {
				convertedData = convertedData.concat(listOfWordPosLemmaTagTarget.get(w)[0]);
				for ( int tok=1; tok<listOfWordPosLemmaTagTarget.get(w).length; tok++ ) 
					convertedData = convertedData.concat("\t").concat(listOfWordPosLemmaTagTarget.get(w)[tok]);
				
				convertedData = convertedData.concat("\n");
			}
		

			
/*
			if ( (convertedData.contains("B-e" + i) && !convertedData.contains("B-e" + k))
					|| (convertedData.contains("B-e" + k) && !convertedData.contains("B-e" + i)))
			{
				System.out.println(convertedData);
				System.exit(0);
			}					
			
			if ( arrEntMarkers[i].equals("B-e" + i) || arrEntMarkers[k].equals("B-e" + k) ){
			//	System.out.println(convertedData);
				System.exit(0);
			}
*/
			if ( totTargetEnt >= 2 ) {

				TKOutputDT.total_rel++;
				if ( !objSen.listRels.get(r).isPositive )
					TKOutputPST.totalRelNeg++;		
				else
					TKOutputPST.totalRelPos++;
			
				if ( !objSen.listRels.get(r).isPositive )
					convertedData += "\nNO\n\n";
				else
					convertedData +="\nYES\n\n";
				
				if ( entPairFileName != null && !entPairFileName.isEmpty() )
					FileUtility.writeInFile(entPairFileName, e1.id + "\t" + e2.id + "\n", true);				
			
				sbSemEval2task8Format.append(convertedData);
			}
			else
				System.err.println("Less than 2 T arguments for " + e1.id + "\t" + e2.id);
		}
		
		return sbSemEval2task8Format;
	}
	
	/**
	 * 
	 * @param bioRelExInpFileName
	 * @param mergedVectorForTK
	 * @return
	 * @throws Exception
	 */
	public String getTrainFVforJsreKernel ( String bioRelExInpFileName, 
			String jsreKernel, String trainJsreFile, String modelZipjSRE ) throws Exception{
		
		//return "";
		return readjustNegLabel(BinaryRelExtractor.getFvForTrainData(bioRelExInpFileName, jsreKernel, 
				trainJsreFile, modelZipjSRE)).trim();
	}

	
	/**
	 * 
	 * @param fvOld
	 * @param allLinesFVnew
	 * @return
	 */
	public StringBuilder mergeLinesOfFVs( String[] allLinesFVnew, String[] allLinesFVold ){
		
		StringBuilder mergedVectorForTK = new StringBuilder();
		
		if ( allLinesFVold.length != allLinesFVnew.length ){
			System.err.println("FV Size mismatch. " + allLinesFVold.length + " " + allLinesFVnew.length );
			System.exit(0);
		}
		int x = -1;
		for ( int i=0; i<allLinesFVold.length; i++ ) {
			allLinesFVnew[i] = allLinesFVnew[i].replaceAll("\\s+", " ");
			allLinesFVold[i] = allLinesFVold[i].replaceAll("\\s+", " ");
			              
			x = allLinesFVnew[i].indexOf(" ");
				
			if ( x >= 0 ) {
				int y = allLinesFVold[i].lastIndexOf("EV");
				// if there is another feature vector already added
				if ( y > 0 ) {
					mergedVectorForTK.append(allLinesFVold[i].substring(0, y)).append("BV|")
					.append(allLinesFVnew[i].substring(x)).append(" |EV|\n");
				}
				else
					mergedVectorForTK.append(allLinesFVold[i]).append(" |BV|")
					.append(allLinesFVnew[i].substring(x)).append(" |EV|\n");
			}
		}
				
		return mergedVectorForTK;
	}

	
	/**
	 * 
	 * @param fv
	 * @param mergedVectorForTK
	 * @return
	 */
	public StringBuilder mergeLines( String[] allLinesFV, String[] allLinesTK ){
		
		StringBuilder mergedVectorForTK = new StringBuilder();
		
		if ( allLinesFV.length != allLinesTK.length ){
			System.err.println("Size mismatch. " + allLinesFV.length + " " + allLinesTK.length );
			System.exit(0);
		}
		int x = -1;
		for ( int i=0; i<allLinesFV.length; i++ ) {
			allLinesTK[i] = allLinesTK[i].trim();
			
			x = allLinesFV[i].indexOf(" ");
			allLinesFV[i] = allLinesFV[i].substring(x).trim();
			
			// if there are vector features for the instance
			if ( x >= 0 ) {
				int y = allLinesTK[i].lastIndexOf("|ET|");
				// if there is ending sign of tree kernel
				if ( y > 0 ) {
					mergedVectorForTK.append(allLinesTK[i]).append(" ")
					.append(allLinesFV[i]).append("\n");
				}
				else {
					y = allLinesTK[i].indexOf(" ");
					// if there are tree structure features for the instance
					if ( y>=0 )				
						mergedVectorForTK.append(allLinesTK[i]).append(" |ET| ")
							.append(allLinesFV[i]).append("\n");
					else
						mergedVectorForTK.append(allLinesTK[i]).append(" ")
						.append(allLinesFV[i]).append("\n");
				}
			}
			else
				mergedVectorForTK.append(allLinesTK[i]).append("\n");
		}
				
		return mergedVectorForTK;
	}
	
	
	/**
	 * 
	 * @param bioRelExInpFileName
	 * @param allLinesTK
	 * @param jsreKernel
	 * @param jsreOutputFileName
	 * @param testJsreFile
	 * @param modelFileName
	 * @return
	 * @throws Exception
	 */
	public String getTestFVforJsreKernel ( String bioRelExInpFileName, String jsreKernel,
			String testJsreFile, String modelZipjSRE) throws Exception{
		
		//return "";
		return readjustNegLabel(BinaryRelExtractor.getFvForTestData(bioRelExInpFileName, testJsreFile, 
				modelZipjSRE)).trim();				
	}
	
	
	/**
	 * 
	 * @param fv
	 * @return
	 */
	private String readjustNegLabel( String fv ){
		fv = fv.trim();
		
		if ( fv.length() > 0 && fv.charAt(0) == '0' )
			fv = "-1" + fv.substring(1);
		
		return fv.replaceAll("\\n0", "\n-1");
	}
	
	
	public void writeJSREformatFileTrain( String tokenizedTrainingFileName, String trainJsreFile) throws Exception {
		MultiStageRE clsMultiStageRE = new MultiStageRE();
		
		int[] relClassLabels = new int[relTypesAll.length];			
		int	defaultClassLabel = clsMultiStageRE.populateClassLabels(relTypesAll, 
				relClassLabels, 0, DEFAULT_REL);
					
		//-- construct training data for the relations
		
		String trainingDataJSreInpFormat = new FormatConverterJSRE().posTaggedToSreFormatConverter(tokenizedTrainingFileName, 
				relTypesAll, false, relClassLabels, defaultClassLabel);
		FileUtility.writeInFile(trainJsreFile, trainingDataJSreInpFormat, false);	
	}
	
	public void writeJSREformatFileTest( String tokenizedTestFileName, String testJsreFile) throws Exception {	
		
		String tmpTestDataJSre = FormatConverterJSRE.createJSreInputFiles(tokenizedTestFileName, 
				relTypesAll, false, false, 0, DEFAULT_REL)[0];
		
		FileUtility.writeInFile(testJsreFile, FileUtility.readFileContents(tmpTestDataJSre), false);
	}
	
	static String DEFAULT_REL = "NO";
	public static String[][] relTypesAll = new String[][]{
		 {"NO"},
		 {"YES"},
	};
	
}

