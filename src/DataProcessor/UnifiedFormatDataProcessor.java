package DataProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import Structures.Entity;
import Structures.Relation;
import Structures.Sentence;
import Utility.*;



public class UnifiedFormatDataProcessor {

	
	//*
	/*
	public static void main( String[] args ) throws Exception{
		 // new AIMedDataProcessor().processData();
	/ *
		String parsedFileName = "/media/Study/data/AImed/aimed_parsed_sp1.6.5", 
			fullDataFileName = "/media/Study/data/AImed/aimedFull.txt",
			senSegmentedFileName = "/media/Study/data/AImed/aimed_parsed_psg_merged_segmented";
		
		 new UnifiedFormatDataProcessor().mergeSepratedParsedPartsOfSameSentences(parsedFileName, fullDataFileName);
		
		/*
		  new SyntacticParser().callStanfordParser("/media/Study/data/AImed/aimed.txt", 
				"all_bnews_nwire_sentences_parsed",
				"/hltsrv0/chowdhury/installed_programs/stanford-parser-2010-07-09/englishPCFG.ser.gz");
		* /
	}
*/
	

	/**
	 * 
	 * @param inSenWithEntRelFileName
	 * @param abstractFileFolder
	 * @throws Exception
	 */
	public void addAbstractIdWtihData( String inSenWithEntRelFileName ) throws Exception{
		
		ArrayList<String> listLines = FileUtility.readFileLines(inSenWithEntRelFileName);	
		StringBuilder sbIdSenEntityRel = new StringBuilder();
		String docId = "NONE";
		
		for ( int i=0; i<listLines.size()-1; i++ ){
			
			if ( listLines.get(i).contains("Sentence Id:") ) {
					
				docId = listLines.get(i).substring(0, listLines.get(i).lastIndexOf("."))
					.replace("Sentence Id:", "").trim();
				
				sbIdSenEntityRel.append("Abstract Id: " + docId + "\n");
				sbIdSenEntityRel.append(listLines.get(i) + "\n");
				i++;
			}
								
			sbIdSenEntityRel.append(listLines.get(i) + "\n");
		}
				
		FileUtility.writeInFile(inSenWithEntRelFileName, sbIdSenEntityRel.toString(), false);	
	}
	
	
	/**
	 * 
	 * @param sentence
	 * @param boundaryOfTarget
	 * @return
	 */
	public static String extractStringByBoundary ( String sentence, int[] boundaryOfTarget ){
		int z = 0, v = 0;		
		while ( v != boundaryOfTarget[0]  ){
			
			z++;
			if ( !String.valueOf(sentence.charAt(z)).matches("\\s")  )
				v++;
		}
		
		int w = z;		
		while ( v != boundaryOfTarget[1]  ){
			
			z++;
			if ( !String.valueOf(sentence.charAt(z)).matches("\\s")  )
				v++;
		}

		z++;
		
		return sentence.substring(w,z);
	}
	
	
	/**
	 * 
	 * 
	 * @param inUnifiedXmlDataFilePath
	 * @param outSenFileName
	 * @param outSenWithEntRelFileName
	 * @throws Exception
	 */
	public void extractAndMergeText( String inUnifiedXmlDataFilePath, String outSenFileName, 
			String outSenWithEntRelFileName, boolean bInsertSpacesAtEntBoundary, 
			boolean bRemoveDashAtWordBoundary, boolean bAddCommaBeforeConj,
			boolean bRemoveCommentsWithNoEntInParentheses,
			boolean bAutoGenerateNegativeInstance) throws Exception{
		
		ArrayList<String> listLines = FileUtility.readFileLines(inUnifiedXmlDataFilePath);	
		StringBuilder sbSen = new StringBuilder(), sbIdSenEntityRel = new StringBuilder();
		PreProcessor clsPP = new PreProcessor();
				
		int j = 0;
		for ( int i=0; i<listLines.size(); i++ ){
			
			if ( listLines.get(i).contains("<sentence") ){
					
				String str = listLines.get(i);
			
				// separating sentence
				j = str.indexOf("text=\"");
				String txtSentence = str.substring(j+6);
				j = txtSentence.indexOf("seqId=");
				
				// if "seqId" attribute is not present, then we assume "text" attribute is the last value
				if ( j < 0 )
					j = txtSentence.indexOf(">");
				System.out.println(txtSentence);				
				txtSentence = txtSentence.substring(0, j);
				j = txtSentence.lastIndexOf("\"");
				txtSentence = TextUtility.replaceSepcialXmlCharsWithOriginals(txtSentence.substring(0, j)).trim();
				
				if ( TextUtility.isEmptyString(txtSentence) )
					continue;
				
				// replace all ? characters which are inside the sentence because of text encoding problem
				// for any non-ASCII special symbol
				char lastChar = txtSentence.charAt(txtSentence.length()-1);
				txtSentence = txtSentence.substring(0, txtSentence.length()-1).replace("?", "@") + lastChar;
				
				// ensuring that the sentence has an ending symbol
				if ( !txtSentence.matches(".*[.?!]$") )
					txtSentence = txtSentence + " .";				
				
				if ( txtSentence.length() < 2 )
					continue;
				
				// removing any sentence ending symbol at the beginning of the sentence
				int delPrefChar = 0;

				while ( String.valueOf(txtSentence.charAt(0)).matches("^.*[.?!,:;]") ) {
					txtSentence = txtSentence.substring(1);
					delPrefChar++;				
				}
				
				// separating sentence id
				j = str.indexOf("id=\"");
				String sId = str.substring(j+4);
				j = sId.indexOf("\"");
				sId = sId.substring(0, j);
				
				/* 
				 * TODO: If a sentence is not ended by "</sentence>", but with "/>", then it is not
				 * taken on consideration. Needs to modify it for simplicity.
				 * 
				 */
				if ( !str.contains("/>") ){
					i++;				
				
					ArrayList<String> entList = new ArrayList<String>(),
						entIdList = new ArrayList<String>(),
						relList = new ArrayList<String>(),
						entTypeList = new ArrayList<String>();
					ArrayList<int[]> entBoundaryList = new ArrayList<int[]>();
					
					while ( i<listLines.size() && !listLines.get(i).contains("</sentence>") ){
						str = listLines.get(i);						
						
						if ( str.contains("<entity") ){
							// separating entity
							j = str.indexOf("text=\"");
							String entity = str.substring(j+6);
							j = entity.indexOf("seqId=");
				
							if ( j < 0 )
								j = entity.indexOf("type=");
								 		
							// if "seqId" or "type" attribute is not present, then we assume "text" attribute is the last value
							if ( j<0 )
								j = entity.indexOf(">");
														
							entity = entity.substring(0, j);
							j = entity.lastIndexOf("\"");
							entity = TextUtility.replaceSepcialXmlCharsWithOriginals(entity.substring(0, j));
							
							// removing any sentence ending symbol at the beginning of entity name
							int delPrefCharInEnt = 0;
							while ( String.valueOf(entity.charAt(0)).matches("^.*[.?!,:;]") ) {
								entity = entity.substring(1);
								delPrefCharInEnt++;				
							}
														
							// separating entity id
							j = str.indexOf("id=\"");
							String eId = str.substring(j+4);
							j = eId.indexOf("\"");
							eId = eId.substring(0, j);
							entIdList.add(eId);
										
							// separating charOffset
							j = str.indexOf("charOffset=\"");
							String charOffset = str.substring(j+12);
							j = charOffset.indexOf("\"");
							charOffset = charOffset.substring(0, j);
							
							j = Integer.valueOf(charOffset.split("-")[0]) - delPrefChar + delPrefCharInEnt;
							/**
							 *  Checking whether there is any other character other than - and digits
							 *  NOTE: some of the entities may consist of words which have other words in between them  
							 */
							if ( charOffset.matches(".*[^-0-9].*") ) {
								charOffset = charOffset.split("[^-0-9]")[0];
								String[] tmpBoundaries = charOffset.split("-");
																
								entity = txtSentence.substring(j, Integer.valueOf(tmpBoundaries[1])+1);
							}
							// calculate the start index after removing space characters
							j = txtSentence.substring(0, j).replaceAll("\\s+", "").length();
							entBoundaryList.add(new int[] {
									j,   (j+entity.replaceAll("\\s+", "").length()-1)});
							

							entList.add(entity);
							
							// separating type
							j = str.indexOf("type=\"");
							if ( j> 0 ) {
								String type = str.substring(j+6);
								j = type.indexOf("\"");
								type = type.substring(0, j);
								entTypeList.add(type);
							}
							else
								entTypeList.add("NOT_GIVEN");
						}
						
						if ( str.contains("<interaction") ){
							// separating relation entities
							j = str.indexOf("e1=\"");
							String e1 = str.substring(j+4);
							j = e1.indexOf("\"");
							e1 = e1.substring(0, j);
							
							j = str.indexOf("e2=\"");
							String e2 = str.substring(j+4);
							j = e2.indexOf("\"");
							e2 = e2.substring(0, j);
							
							j = str.indexOf("type=\"");
							String subType = "";
							
							if ( j >= 0 ) {
								subType = str.substring(j+6);
								j = subType.indexOf("\"");
								subType = subType.substring(0, j);
							}
							
							if ( str.contains("\"false\"") )
								relList.add("interaction " + e1 + " " + e2 + (TextUtility.isEmptyString(subType) ? "" : (" " + subType)) + " false");
							else
								relList.add("interaction " + e1 + " " + e2 + (TextUtility.isEmptyString(subType) ? "" : (" " + subType)) + " true");
						}
						
						i++;
					}						
					
					if ( !entList.isEmpty() ){
						sortEntitiesByBoundary(entList, entIdList, entTypeList, entBoundaryList);
							
						/**
						 * NOTE: We insert space before and after of each entity name. 
						 * 	This is necessary as sometimes entities (especially, proteins) are not clearly separated,
						 * 	but have "-" or "/" etc in between them.
						 */
						if ( bInsertSpacesAtEntBoundary )
							for ( int k=0; k<entList.size(); k++ ) {
								txtSentence = TextUtility.insertSpaceAtStringAbsBoundary(txtSentence, entBoundaryList.get(k));
							}
						
						if ( bRemoveDashAtWordBoundary )
							txtSentence = clsPP.removeDashAtWordBoundaryAndUpdateEntBoundaries(txtSentence, entBoundaryList);
							//preProcessWordWithDashEndingAndUpdateEntBoundaries( txtSentence, entBoundaryList);
						
						//-- re-populate entity names
						for ( int k=0; k<entList.size(); k++ )							
							entList.set(k, extractStringByBoundary(txtSentence, entBoundaryList.get(k)));
						
						//if ( bRemoveDashAtWordBoundary )
							//txtSentence = clsPP.removeDashAtEndOfEntNamesAndUpdateEntBoundaries( txtSentence, entBoundaryList);
						if ( bAddCommaBeforeConj )
							txtSentence = clsPP.addCommaBeforeConj( txtSentence, entBoundaryList);
						if ( bRemoveCommentsWithNoEntInParentheses )
							txtSentence = clsPP.removeCommentsWithNoEntInParentheses(txtSentence, entBoundaryList);
					}					
					
					/**
					 * Insert "false" entity pairs
					 */		
					if ( bAutoGenerateNegativeInstance ) {
						for ( int e=0; e<entIdList.size()-1; e++ ) {
							for ( int eOther=e+1; eOther<entIdList.size(); eOther++ ) {
								int r = 0;
								
								for ( ; r<relList.size(); r++ ) {
									if ( relList.get(r).matches("interaction\\s+" + entIdList.get(e) + "\\s+" + entIdList.get(eOther) + ".*")
											|| relList.get(r).matches("interaction\\s+" + entIdList.get(eOther) + "\\s+" + entIdList.get(e) + ".*") )
										break;
								}
								
								if ( r==relList.size() ) {
									relList.add("interaction " + entIdList.get(e) + " " + entIdList.get(eOther) + " false");
								}
							}
						}
					}
					
					txtSentence = txtSentence.replaceAll("\\s+", " ").trim();
					sbSen.append( txtSentence + "\n\n" );
					sbIdSenEntityRel.append("Sentence Id: " + sId + "\n" + txtSentence + "\n\n");
					
					if ( !entList.isEmpty() ){
						for ( int k=0; k<entList.size(); k++ )
							sbIdSenEntityRel.append(
									entIdList.get(k) + " " + entBoundaryList.get(k)[0] + " " +
									entBoundaryList.get(k)[1] + "\n" + entTypeList.get(k) + "\n" + entList.get(k) + "\n");
					}
					else
						sbIdSenEntityRel.append("\n");
					
					sbIdSenEntityRel.append("\n");
					
					if ( !relList.isEmpty() ){
						for ( int k=0; k<relList.size(); k++ )
							sbIdSenEntityRel.append(relList.get(k) + "\n");
					}
					else
						sbIdSenEntityRel.append("\n");		
					
					sbIdSenEntityRel.append("\n");					
				}
			}
		}
				
		FileUtility.writeInFile(outSenFileName, sbSen.toString(), false);
		FileUtility.writeInFile(outSenWithEntRelFileName, sbIdSenEntityRel.toString(), false);
	}


	
	
	/**
	 * 
	 * @param outSenWithEntRelFileName
	 * @param outSenFileName
	 * @throws Exception
	 */
	public void resolveOverlappedAnnotationsByDuplicatingSentences ( String outSenWithEntRelFileName, String outSenFileName ) throws Exception {
	
		ArrayList<Sentence> allSentences = Sentence.readFullData(outSenWithEntRelFileName, "", "", "", false);	
		StringBuilder sbIdSenEntityRel = new StringBuilder(), sbSen = new StringBuilder();
		
		/**
		 * Idea:
		 * a. Lets X is a list of list which is initially empty
		 * b. add the 1st entity of the entity set E in a list and add the list in X
		 * c. select the next entity e from E
		 * c.1. for each list x in X
		 * c.1.1. if any of the elements in x overlaps with e, copy x as x'; 
		 * 			remove the overlapped elements from x'; add e in x';
		 * 			add x' in X 
		 * c.1.2. else
		 * 			add e in X 
		 * c.2. continue until no entity is left in E to be added to X
		 */
		
		for ( int s=0; s<allSentences.size(); s++ ) {
			allSentences.get(s).text = allSentences.get(s).text.replaceAll("\\s+", " ").trim();
			ArrayList<Entity> listOfOriginalEntities = allSentences.get(s).listOfEntities;
			
			// if there is at least one entity
			if ( listOfOriginalEntities.size() > 0 ) {
				ArrayList<ArrayList<Entity>> listOfListOfNonOverlappedEntities = new ArrayList<ArrayList<Entity>>();
			
				ArrayList<Entity> listTmp = new ArrayList<Entity>();
				listTmp.add(listOfOriginalEntities.get(0));
				listOfListOfNonOverlappedEntities.add(listTmp);
			
				for ( int e=1; e<listOfOriginalEntities.size(); e++ ) {
					ArrayList<ArrayList<Entity>> listOfListsTemp = new ArrayList<ArrayList<Entity>>();
					
					for ( int x=0; x<listOfListOfNonOverlappedEntities.size(); x++ ) {
						ArrayList<Entity> listX = listOfListOfNonOverlappedEntities.get(x);
						listTmp = new ArrayList<Entity>();
						for ( int ex=0; ex<listX.size(); ex++ ) {
							if ( !listOfOriginalEntities.get(e).hasOverlap(listX.get(ex)) )
								listTmp.add(listX.get(ex));
						}
						
						if ( listTmp.size() == listX.size() ) {
							listOfListOfNonOverlappedEntities.get(x).add(listOfOriginalEntities.get(e));
						}
						else {
							listTmp.add(listOfOriginalEntities.get(e));
							listOfListsTemp.add(listTmp);
						}
					}
					
					if ( listOfListsTemp.size() > 0 ) {
						listOfListOfNonOverlappedEntities.addAll(listOfListsTemp);
					}
				}
				
				if ( listOfListOfNonOverlappedEntities.size()  > 1 ) {
					//make copies of the sentence according to the size of listOfListOfNonOverlappedEntities, change entity list in them, add in string builder
					ArrayList<String> listAlreadyAddedRels = new ArrayList<String>();
					
					for ( int i=0; i<listOfListOfNonOverlappedEntities.size(); i++ ) {
						// the object is copied so that any changes in copied object is not reflected in the original object
						Sentence objSen = allSentences.get(s).clone();
						objSen.listOfEntities = listOfListOfNonOverlappedEntities.get(i);
						
						ArrayList<String> listAllEntIds = new ArrayList<String>();
						for ( int k=0; k<objSen.listOfEntities.size(); k++ ) {
							listAllEntIds.add(objSen.listOfEntities.get(k).id);
						}
							
						
						//-- remove relations where one of the entities is missing, or has been already added by the previous copies for the sentence
						for ( int r=0; r<objSen.listRels.size(); r++ ) {
						
							
							if ( listAlreadyAddedRels.contains(objSen.listRels.get(r).printString()) ) {
								objSen.listRels.remove(r);
								r--;
							}
							else {								
								if ( !listAllEntIds.contains(objSen.listRels.get(r).arg1) || !listAllEntIds.contains(objSen.listRels.get(r).arg2) ) {
									objSen.listRels.remove(r);
									r--;
								}
								else if ( Relation.printRelPolarity || objSen.listRels.get(r).isPositive )
									listAlreadyAddedRels.add(objSen.listRels.get(r).printString());
							}
						}
						
						sbSen.append( objSen.text + "\n\n" );
						sbIdSenEntityRel.append(objSen.printString());
					}
				}
				else {
					sbSen.append( allSentences.get(s).text + "\n\n" );
					sbIdSenEntityRel.append(allSentences.get(s).printString());
				}
			}
			// if there is no entity
			else {
				sbSen.append( allSentences.get(s).text + "\n\n" );
				sbIdSenEntityRel.append(allSentences.get(s).printString());
			}
			
		}
	
		FileUtility.writeInFile(outSenWithEntRelFileName, sbIdSenEntityRel.toString(), false);
		FileUtility.writeInFile(outSenFileName, sbSen.toString(), false);
	}
	
	
	/**
	 * 
	 * @param entList
	 * @param entIdList
	 * @param entTypeList
	 * @param entBoundaryList
	 */
	private void sortEntitiesByBoundary( ArrayList<String> entList, ArrayList<String> entIdList,
			ArrayList<String> entTypeList, ArrayList<int[]> entBoundaryList ) {
		
		// sort entities
		for ( int e=0; e<entBoundaryList.size()-1; e++ ) {
			for ( int d=e+1; d<entBoundaryList.size(); d++ ) {
				if ( entBoundaryList.get(e)[0] > entBoundaryList.get(d)[0] ) {
					int[] tm = entBoundaryList.get(e);
					entBoundaryList.set(e, entBoundaryList.get(d));
					entBoundaryList.set(d, tm);
					
					String temp = entList.get(e);
					entList.set(e, entList.get(d));
					entList.set(d, temp);
					
					temp = entIdList.get(e);
					entIdList.set(e, entIdList.get(d));
					entIdList.set(d, temp);
					
					temp = entTypeList.get(e);
					entTypeList.set(e, entTypeList.get(d));
					entTypeList.set(d, temp);
				}
			}
		}
	}

	/**
	 	x = sentence from aimed full data
		y = parsed sentence
		ny = no of tokens of y
		if all the tokens of y equals all the tokens of x
		then do nothing
		else {
		read next y
		add ny to the token index of each of them
		merge token/tags with previous sentence
		merge dep_rel with previous sentence
		}	
	 * 
	 * @param parsedFileName
	 * @param fullDataFileName
	 * @param senSegmentedFileName
	 * @throws Exception
	 */
	public void mergeSepratedParsedPartsOfSameSentences( String parsedFileName,
			ArrayList<Sentence> listSentence ) throws Exception{
		
		boolean existsEOLmarker = doesEOLmarkerExist(parsedFileName);
		System.out.println("existsEOLmarker = " + existsEOLmarker);
		BufferedReader inputParse = new BufferedReader(new FileReader(new File(parsedFileName)));
		
		int senIndx = 0, tokIndexAdd = 0;
		
		String EOLmarkerWithoutSpace = CommonUtility.EOLmarker.replaceAll("\\s+", "");
		String outputDepFile = CommonUtility.OUT_DIR + parsedFileName.substring(parsedFileName.lastIndexOf("/")) + "_dt";
		
		// write
		FileUtility.writeInFile( outputDepFile, "", false);
		
		boolean isReadNextSentenceFromAimed = true;
		String tokenWithPos = "", tempParseTree = "", parseTree = "", tempTokenWithPos = "",
			line = "";
		ArrayList<String> listDependencies = new ArrayList<String>(), tempListDependencies = new ArrayList<String>();
		
		StringBuilder sb = new StringBuilder(), sbOnlyParseTrees = new StringBuilder(), 
			sbOrigCFGParse = new StringBuilder();
				
		while (( line = inputParse.readLine()) != null){
			// read tokenWithPos
			line = line.trim();
		//	System.out.println(line);
			
			if ( line.isEmpty() )
				continue;
			tempTokenWithPos += " " + line;
						
			String allTokens = "";
			String[][] tokenPosList = Common.separateTokenAndPos(line, true);
			
			for ( int i=0; i<tokenPosList.length; i++ )
				allTokens += tokenPosList[i][0];
			
			inputParse.readLine();
			tempParseTree = inputParse.readLine();
			while (( line = inputParse.readLine()) != null){
				tempParseTree += "\n" + line;
				if ( line.trim().isEmpty() )
					break;
			}
			
			// read dependencies
			line = inputParse.readLine().trim();
			
			// no dep rel
			if ( line.isEmpty() )
				;//line = inputParse.readLine();
			else {
				ArrayList<String> listTemp = new ArrayList<String>();
				while ( line != null && !(line = line.trim()).isEmpty() ){					
					listTemp.add(line);
					line = inputParse.readLine();
				}
				
				String[][] allSeparatedRelAndArgs = new SyntacticParser().
					separateRelationAndArgs(DataStrucUtility.listToStringArray(listTemp));
				
				for ( int i=0; i<allSeparatedRelAndArgs.length; i++ ){
					int argOneIndx = Integer.valueOf(allSeparatedRelAndArgs[i][2]) + tokIndexAdd,
						argTwoIndx = Integer.valueOf(allSeparatedRelAndArgs[i][4]) + tokIndexAdd;
					
					tempListDependencies.add(allSeparatedRelAndArgs[i][0] + "(" + allSeparatedRelAndArgs[i][1]
					      + "-" + argOneIndx + ", " + allSeparatedRelAndArgs[i][3] + "-"
					      + argTwoIndx + ")");
				}
			}
								
			if ( isReadNextSentenceFromAimed && senIndx < listSentence.size() ){
				sb.append(listSentence.get(senIndx).absID + "\n");				
				sb.append(listSentence.get(senIndx).senID + "\n");
				sb.append(listSentence.get(senIndx).text + "\n");
				
				FileUtility.writeInFile(outputDepFile, "Sentence Id: " + listSentence.get(senIndx).senID + "\n", true);
				sbOnlyParseTrees.append("Sentence Id: " + listSentence.get(senIndx).senID + "\n");
				//System.out.println(listSentence.get(senIndx).senID);
				senIndx++;
			}			
			
			if ( allTokens.equals(EOLmarkerWithoutSpace) || !existsEOLmarker ) {
				isReadNextSentenceFromAimed = true;
				
				if ( !existsEOLmarker ) {
					parseTree = tempParseTree ;
					tokenWithPos = tempTokenWithPos;
					listDependencies.addAll(tempListDependencies);
				}
				
			}
			else {
				isReadNextSentenceFromAimed = false;
				tokIndexAdd += tokenPosList.length;
		
				// collecting original CFG parses to use for clause splitting
				sbOrigCFGParse.append(tempParseTree.trim().replaceAll("\\n", " ") + "\n\n");
			
				// 1st line of psg tree
				if ( parseTree.isEmpty() )
					parseTree += "\n";
				parseTree += tempParseTree;
									
				tokenWithPos += tempTokenWithPos;
				listDependencies.addAll(tempListDependencies);
			}
			
			tempParseTree = "";
			tempTokenWithPos = "";
			tempListDependencies.clear();
			
			if ( isReadNextSentenceFromAimed == true) {
				
				parseTree = parseTree.trim();
				if ( parseTree.indexOf("(ROOT", 5) > -1 )
					parseTree = "(SUPERROOT " + parseTree + ")";
				parseTree = parseTree.replaceAll("\\n", " ");
				
				
				tokenWithPos = tokenWithPos.trim();
				FileUtility.writeInFile(outputDepFile, tokenWithPos + "\n\n", true);
				sb.append("\n" + tokenWithPos.trim() + "\n\n\n");
				
				sbOnlyParseTrees.append(tokenWithPos + "\n\n");
				sbOnlyParseTrees.append(parseTree + "\n\n");
				tokIndexAdd = 0;
				
				if ( listDependencies.size() == 0 ) 
					FileUtility.writeInFile(outputDepFile, "\n", true);
				
				for ( int i=0; i<listDependencies.size(); i++ )
					FileUtility.writeInFile(outputDepFile, listDependencies.get(i) + "\n", true);
				
				FileUtility.writeInFile(outputDepFile, "\n", true);
				
				tokenWithPos = "";
				parseTree = "";
				listDependencies.clear();
			}
			
		}
		
		inputParse.close();
		FileUtility.writeInFile( CommonUtility.OUT_DIR + outputDepFile.substring(outputDepFile.lastIndexOf("/")) + "_check", sb.toString(), false);
		FileUtility.writeInFile(CommonUtility.OUT_DIR + parsedFileName.substring(parsedFileName.lastIndexOf("/")) + "_psg", sbOnlyParseTrees.toString(), false);
		FileUtility.writeInFile(CommonUtility.OUT_DIR + parsedFileName.substring(parsedFileName.lastIndexOf("/")) + "_psg_orig", sbOrigCFGParse.toString(), false);
	}
	
	
	/**
	 * 
	 * @param parsedFileName
	 * @return
	 * @throws IOException
	 */
	private boolean doesEOLmarkerExist ( String parsedFileName ) throws IOException {

		String EOLmarkerWithoutSpace = CommonUtility.EOLmarker.replaceAll("\\s+", "");
		String line = "";
		BufferedReader inputParse = new BufferedReader(new FileReader(new File(parsedFileName)));
		int senIndex = 0;
		
		while (( line = inputParse.readLine()) != null && senIndex < 5){
			// read tokenWithPos
			line = line.trim();
			
			if ( line.isEmpty() )
				continue;
						
			String allTokens = "";
			String[][] tokenPosList = Common.separateTokenAndPos(line, true);
			
			for ( int i=0; i<tokenPosList.length; i++ )
				allTokens += tokenPosList[i][0];
			
			if ( senIndex > 0 && allTokens.equalsIgnoreCase(EOLmarkerWithoutSpace) ) {
				inputParse.close();
				return true; 
			}
			
			inputParse.readLine();
			inputParse.readLine();
			while (( line = inputParse.readLine()) != null){
				if ( line.trim().isEmpty() )
					break;
			}
			
			// read dependencies
			line = inputParse.readLine().trim();
			
			// no dep rel
			if ( line.isEmpty() )
				;//line = inputParse.readLine();
			else {
				while ( line != null && !(line = line.trim()).isEmpty() ){					
					line = inputParse.readLine();
				}				
			}
						
			senIndex++;
		}
				
		inputParse.close();
		return false;
	}
	
}
