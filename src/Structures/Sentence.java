package Structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import Kernels.TKOutputGenerator;
import Utility.*;

public class Sentence {

	public String senID = "", absID = "", text = "", tokenWithPosByParser = "";
	public int  senIndx = -1;
	
	private int noOfMentionsOfInterest = 0;
	
	public ArrayList<Relation> listRels = new ArrayList<Relation>();
	public ArrayList<Entity> listOfEntities = new ArrayList<Entity>();
	
	public int[][] arrBoundariesByWordIndexes = new int[0][];
	public String[][] arrWordAndPosByParser = new String[0][];
	public String[] arrLemmasByParser = new String[0], arrWordBySpace = new String[0], arrPhrasalChunks = new String[0];
	
	public ArrayList<String> listOfWordIDsBySpace = new ArrayList<String>();
	
	public PhraseStructureTree psgTree = null; 
	public DependencyGraph depGraph = null;
	public DependencyTree depTree = null;
	
		
	/**
	 * 
	 */
	public Sentence () {}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public Sentence clone()
    {
		Sentence newNode = new Sentence();
		newNode.senID = senID;
		newNode.absID = absID;
		newNode.text = text;
		newNode.tokenWithPosByParser = tokenWithPosByParser;
		newNode.senIndx = senIndx;
		
		newNode.listRels = (ArrayList<Relation>) listRels.clone();
		newNode.listOfEntities = (ArrayList<Entity>) listOfEntities.clone();
		
		newNode.arrBoundariesByWordIndexes = (int[][]) arrBoundariesByWordIndexes.clone();
		newNode.arrWordAndPosByParser = (String[][]) arrWordAndPosByParser.clone();
		newNode.arrLemmasByParser = (String[]) arrLemmasByParser.clone(); 
		newNode.arrWordBySpace = (String[]) arrWordBySpace.clone();
		newNode.arrPhrasalChunks = (String[]) arrPhrasalChunks.clone();
		
		newNode.listOfWordIDsBySpace = (ArrayList<String>) listOfWordIDsBySpace.clone();
		
		newNode.psgTree = psgTree.clone(this); 
		newNode.depGraph = depGraph.clone();
		newNode.depTree = depTree.clone();
		
		return newNode;
    }
	
	
	/**
	 * 
	 * @param e
	 * @return
	 */
	public static boolean satisfyRelArgConstraint ( Entity e ) {
		
		if ( TextUtility.isEmptyString(TKOutputGenerator.regExRelArgType) && 
				TextUtility.isEmptyString(TKOutputGenerator.regExFirstRelArgType) &&
				TextUtility.isEmptyString(TKOutputGenerator.regExSecondRelArgType) )
			return true;
				
		if ( !TextUtility.isEmptyString(TKOutputGenerator.regExRelArgType)
				&& e.getNEcategory().toLowerCase().matches(TKOutputGenerator.regExRelArgType) )
			return true;
		
		else if ( !TextUtility.isEmptyString(TKOutputGenerator.regExFirstRelArgType) 
				&& e.getNEcategory().toLowerCase().matches(TKOutputGenerator.regExFirstRelArgType) )
			return true;
		else if ( !TextUtility.isEmptyString(TKOutputGenerator.regExSecondRelArgType) 
				&& e.getNEcategory().toLowerCase().matches(TKOutputGenerator.regExSecondRelArgType) )
			return true;
				
		return false;
	}
	
	/**
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 */
	public static boolean satisfyRelArgConstraint ( Entity e1, Entity e2 ) {
		return satisfyRelArgConstraint(e1) && satisfyRelArgConstraint(e2);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getNoOfMentionsOfInterest() {
		return this.noOfMentionsOfInterest;
	}
	
	/**
	 * 
	 * @param noMI
	 */
	public void setNoOfMentionsOfInterest( int noMI ) {
		this.noOfMentionsOfInterest = noMI;
	}
	
	
	/**
	 * 
	 * @param psg
	 */
	private void detectPhrasalChunks ( PhraseStructureTree psg ) {
		PhraseStrucTreeNode[] arrNode = new PhraseStrucTreeNode[arrWordAndPosByParser.length];
		arrPhrasalChunks = new String[arrWordAndPosByParser.length];

		for ( int i=0; i<arrWordAndPosByParser.length; i++ )
			arrNode[i] = psg.getParentPhraseNode(i);
		
		for ( int i=0; i<arrNode.length; i++ ) {
			if ( i > 0 && arrNode[i-1].getAllTerminalNodeIndexesUnderThisNode().contains(i) )
				arrPhrasalChunks[i] = "I-" + arrNode[i-1].pos;
			else if ( arrNode[i].getAllTerminalNodeIndexesUnderThisNode().contains(i-1) )
				arrPhrasalChunks[i] = "O";
			else
				arrPhrasalChunks[i] = "B-" + arrNode[i].pos;
		}		
	}
	
	/**
	 * 
	 * @return
	 */
	public String[][] getWordsAndNE () {
		
		String[][] wordsAndNEs = new String[arrWordAndPosByParser.length][2];
		
		for ( int i=0; i<wordsAndNEs.length; i++ ) {
			wordsAndNEs[i][0] = arrWordAndPosByParser[i][0];
			
			for ( int e=0; e<listOfEntities.size(); e++ ) {
				if ( DataStrucUtility.hasOverlap(arrBoundariesByWordIndexes[i], listOfEntities.get(e).boundaries) ) {
					wordsAndNEs[i][1] = listOfEntities.get(e).getNEcategory();
					break;
				}
			}
		}
		
		return wordsAndNEs;
	}
	
	/**
	 * 
	 * @param charIndex
	 * @return
	 */
	public boolean isCharPartOfAnEntity ( int charIndex ) {
		
		for ( int i=0; i<listOfEntities.size(); i++ ) {
			if ( listOfEntities.get(i).startIndex <= charIndex &&
					listOfEntities.get(i).endIndex >= charIndex )
				return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param str
	 */
	public Sentence ( String str ) {
		
		this.text = str.trim();
		String[] tmp = this.text.split("\\s+");
		this.arrWordBySpace = new String[tmp.length];
		
		for ( int i=0; i<tmp.length; i++ )
			this.arrWordBySpace[i] = tmp[i];
	}
	
	/**
	 * 
	 * @param isBioCreative2Format
	 * @return
	 */
	public String printString ( boolean isBioCreative2Format ) {
		return printString(isBioCreative2Format);
	}
	
	/**
	 * 
	 * @param addOnlyRelTypeX
	 * @return
	 */
	public String printString ( String addOnlyRelTypeX ) {
		return printString(addOnlyRelTypeX, false);
	}
	
	/**
	 * 
	 * @param addOnlyRelTypeX
	 * @param isBioCreative2Format
	 * @return
	 */
	public String printString ( String addOnlyRelTypeX, boolean isBioCreative2Format ) {
		
		if ( isBioCreative2Format )
			return (this.senID + " " + this.text + "\n");

		StringBuilder sbTemp = new StringBuilder();
		
		// write abstract id, sentence id and sentence			
		sbTemp.append( "Abstract Id: " + this.absID + "\n");			
		sbTemp.append( "Sentence Id: " + this.senID + "\n");		
		sbTemp.append(this.text + "\n\n");
		
		// write entities
		if ( this.listOfEntities.size() < 1 )
			// no entity
			sbTemp.append("\n\n");
		else {				
			for ( int e=0; e<this.listOfEntities.size(); e++ ) {
				sbTemp.append( this.listOfEntities.get(e).id + " "
						+ this.listOfEntities.get(e).startIndex + " " + this.listOfEntities.get(e).endIndex + "\n");
				sbTemp.append( this.listOfEntities.get(e).getNEcategory() + "\n");
				sbTemp.append( this.listOfEntities.get(e).name + "\n");
			}
			
			sbTemp.append("\n");
		}			
			
		// write relations
		if ( this.listRels.size() < 1 )
			// no relation
			sbTemp.append("\n\n");
		else {
			int totR = 0;
			for ( int r=0; r<this.listRels.size(); r++ ) {
				// if the relation type matches
				if ( addOnlyRelTypeX.isEmpty() || this.listRels.get(r).type.equalsIgnoreCase(addOnlyRelTypeX) ) {
					if ( Relation.printRelPolarity || 
							this.listRels.get(r).isPositive ) {
						sbTemp.append( this.listRels.get(r).printString() + "\n");
						totR++;
					}
				}
			}
			
			if ( totR > 0 )
				sbTemp.append("\n");
			else
				sbTemp.append("\n\n");
		}
		
		return sbTemp.toString();
	}

		
	/**
	 * 
	 * @return
	 */
	public String printString() {
		return printString("", false);
	}
	
	/**
	 * 
	 * @return
	 */
	public static String printStringAllSentences( ArrayList<Sentence> listOfAllSentences ) {
		
		StringBuilder sb = new StringBuilder();
		for ( int s=0; s<listOfAllSentences.size(); s++ )
			sb.append(listOfAllSentences.get(s).printString());
		
		return sb.toString();
	}
	
	
	/**
	 * 
	 * @param arrWordAndPosByParser
	 */
	public void detectBoundariesAndLemmas ( String[][] arrWordAndPosByParser ){
		this.arrWordAndPosByParser = arrWordAndPosByParser;
		detectBoundariesAndLemmas();
	}
		
	/**
	 * 
	 * @return
	 */
	public int[][] detectBoundariesBySpaceSeparatedWords (){
		
		int sCharIndex = 0, eCharIndex = 0;
		int[][] arrBoundariesOfWord = new int[arrWordBySpace.length][2];
		for ( int i=0; i<arrWordBySpace.length; i++ ){
			
			eCharIndex = sCharIndex + arrWordBySpace[i].length();
						
			arrBoundariesOfWord[i] = new int[]{ sCharIndex, eCharIndex - 1 };
			sCharIndex = eCharIndex;
		}
		
		return arrBoundariesOfWord;
	}
	
	/**
	 * 
	 */
	public void detectBoundariesAndLemmas ( ){
		
		this.arrWordAndPosByParser = ParseOutputUtility.checkTokensInOriginalSentence(arrWordAndPosByParser, this.text);
		
		arrBoundariesByWordIndexes = new int[arrWordAndPosByParser.length][2];
		arrLemmasByParser = new String[arrWordAndPosByParser.length];
		
		int sCharIndex = 0, eCharIndex = 0;
		
		for ( int i=0; i<arrWordAndPosByParser.length; i++ ){
			
			eCharIndex = sCharIndex + arrWordAndPosByParser[i][0].length();
						
			arrBoundariesByWordIndexes[i] = new int[]{ sCharIndex, eCharIndex - 1 };
			sCharIndex = eCharIndex;

			arrLemmasByParser[i] = SyntacticParser.getLemma( arrWordAndPosByParser[i][0], arrWordAndPosByParser[i][1]);
			if ( !arrLemmasByParser[i].equals("-") )
				arrLemmasByParser[i] = arrLemmasByParser[i].replaceAll("-", "");
		}
	}
	
	/**
	 * 
	 * @param tokenWithPos
	 */
	public void detectBoundariesAndLemmas ( String tokenWithPosByParser ){
		
		this.tokenWithPosByParser = tokenWithPosByParser;
		this.arrWordAndPosByParser = ParseOutputUtility.separateTokenAndPos(tokenWithPosByParser, true);
		detectBoundariesAndLemmas();
	}
	
	
	/**
	 * 
	 * @param fullDataFileName
	 * @param psgParsedFileName
	 * @param depParsedFileName
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<Sentence> readFullData ( String fullDataFileName,
			String psgParsedFileName, String depParsedFileName, String entPairFileName, boolean isBioCreative2Format ) throws Exception {
		
		ArrayList<Sentence> listSentence = Sentence.readFullData(fullDataFileName, entPairFileName, isBioCreative2Format);
		
		if ( TextUtility.isEmptyString(depParsedFileName) && TextUtility.isEmptyString(depParsedFileName) )
			return listSentence;
			
		ArrayList<DependencyParseOfSen> listDepParseOfAllSen = null;
		ArrayList<CFGParseOfSen> listCFGParseOfAllSen = null;
		
		if ( !TextUtility.isEmptyString(depParsedFileName) )
			listDepParseOfAllSen = DependencyParseOfSen.readDepParseForAllSen(depParsedFileName);
		
		if ( !TextUtility.isEmptyString(psgParsedFileName) )
				listCFGParseOfAllSen = CFGParseOfSen.readCFGParseForAllSen(psgParsedFileName);
		
		for ( int s=0; s<listSentence.size(); s++ ) {			
			
			Sentence objCurSen = listSentence.get(s);
			
			if ( !TextUtility.isEmptyString(depParsedFileName) ) {
				DependencyParseOfSen objDepParseOfSen = listDepParseOfAllSen.get(s);
				objCurSen.detectBoundariesAndLemmas(objDepParseOfSen.tokAndPos);
			
				objCurSen.depTree = objCurSen.getDependencyTree(listDepParseOfAllSen.get(s));
				// initialize graph
				objCurSen.depGraph = new DependencyGraph( objCurSen.arrWordAndPosByParser,
					DataStrucUtility.listToStringArray(objDepParseOfSen.listOfDeps), objCurSen.getWordsAndNE(), objCurSen.depTree);
			}
			
			if ( !TextUtility.isEmptyString(psgParsedFileName) ) {
				CFGParseOfSen objCFGParseOfSen = listCFGParseOfAllSen.get(s);
				objCurSen.psgTree = new PhraseStructureTree(objCFGParseOfSen.psgParse, objCurSen.arrWordAndPosByParser,	objCurSen.getWordsAndNE());
				objCurSen.detectPhrasalChunks(objCurSen.psgTree);
			}
			
			if ( !TextUtility.isEmptyString(depParsedFileName) ) {	
				for ( int e=0; e<objCurSen.listOfEntities.size(); e++ ) {
					Entity objEnt = objCurSen.listOfEntities.get(e);
					
					int[] bnWIs = objCurSen.getBoundaryWordIndexFromCharIndex(objEnt.boundaries);
					objEnt.setStartWordIndex(bnWIs[0]);
					objEnt.setEndWordIndex(bnWIs[1]);
					
					if ( !TextUtility.isEmptyString(psgParsedFileName) )
						objCurSen.psgTree.setNEcatToPhraseInsteadOfWords(objEnt.getAllWordIndexes(), "");
				}
			}
		}
		
		return listSentence;
	}
			
	/**
	 * 	
	 * @param fullDataFileName
	 * @param entPairFileName
	 * @param isBioCreative2Format
	 * @return
	 * @throws Exception
	 */
	private static ArrayList<Sentence> readFullData ( String fullDataFileName, String entPairFileName, boolean isBioCreative2Format ) throws Exception {
	
		ArrayList<String> listOfFoundTypesOfEntities = new ArrayList<String>();
		
		String line = "";
		BufferedReader inputFullData = new BufferedReader(new FileReader(new File(fullDataFileName)));
		ArrayList<Sentence> listSentence = new ArrayList<Sentence>();
		
		ArrayList<String> listOfPairs = new ArrayList<String>();
		
		if ( !TextUtility.isEmptyString(entPairFileName) )
			FileUtility.writeInFile(entPairFileName, "", false);
		
		while (( line = inputFullData.readLine()) != null){
		
			Sentence objSen = new Sentence();
			boolean generateNegativeInstancesAutomatically = false;
			
			/**
			 * ---########--- Reading full data
			 */				
			// read abstract id, sentence id and sentence
			String[] temp = new String[0];
			line = line.trim();
			
			// i.e. each line contains sentence id followed by the sentence and no empty lines
			if ( isBioCreative2Format ) {
				
				if ( line.isEmpty() )
					break;
				
				int spaceCharFirstInd = line.indexOf(" "); 
				objSen.absID = line.substring(0, spaceCharFirstInd);
				objSen.senID = objSen.absID;
				objSen.text = line.substring(spaceCharFirstInd+1);
				continue;
			}
						
			temp = line.split("\\s+");
			objSen.absID = temp[temp.length-1];
			temp = inputFullData.readLine().trim().split("\\s+");
			objSen.senID = temp[temp.length-1];
			objSen.text = inputFullData.readLine().trim().replaceAll("\\s+", " ");
			inputFullData.readLine();
		
			//System.out.println("Reading sentence " + objSen.senID);
	
			// read entities
			line = inputFullData.readLine().trim();
	
			// no entity
			if ( line.isEmpty() )
				inputFullData.readLine();
			else {
				while ( line != null && !(line = line.trim()).isEmpty() ){
					Entity objEntity = new Entity(line, inputFullData.readLine(), inputFullData.readLine());
					
					if ( !listOfFoundTypesOfEntities.contains(objEntity.getNEcategory().toLowerCase()) )
						listOfFoundTypesOfEntities.add(objEntity.getNEcategory().toLowerCase());
						
					if ( satisfyRelArgConstraint(objEntity) )
						objSen.noOfMentionsOfInterest++;
					objSen.listOfEntities.add(objEntity);
					line = inputFullData.readLine();
				}
			}
			
			// read relations
			line = inputFullData.readLine().trim();
			
			// no relation
			if ( line.isEmpty() )
				inputFullData.readLine();
			else {
				while ( line != null && !(line = line.trim()).isEmpty() ){
					// avoiding self-interactions of pairs
				//	String[] ss = line.split("\\s+");
					//if ( !ss[1].equals(ss[2]) ) {					
					Relation tmp = new Relation();
					tmp.addRelation(line);
					generateNegativeInstancesAutomatically = !Relation.printRelPolarity ? true :
						generateNegativeInstancesAutomatically;
					
					// if the relation type matches
					if ( TextUtility.isEmptyString(TKOutputGenerator.regExTypeOfRel)
							|| tmp.type.toLowerCase().matches(TKOutputGenerator.regExTypeOfRel) ) {

							// if the relation sub-type matches
							if ( !TextUtility.isEmptyString(TKOutputGenerator.subTypeRelConst)
								&& !tmp.subType.toLowerCase().matches(TKOutputGenerator.subTypeRelConst) )
								tmp.isPositive = false;
							
							if ( objSen.getEntityById(tmp.arg1) != null && objSen.getEntityById(tmp.arg2) != null )
								objSen.listRels.add( tmp );						
					}
					
					line = inputFullData.readLine();
				//	}
				}
			}
			
			if ( generateNegativeInstancesAutomatically && 
					objSen.listOfEntities.size() > 1
							//&& objSen.listRels.isEmpty() ) 
					) {
				
				for ( int i=0; i < objSen.listOfEntities.size()-1; i++ ){
					for ( int j=i+1; j < objSen.listOfEntities.size(); j++ ){
						if ( !objSen.getPolarityOfRelation(objSen.listOfEntities.get(i), objSen.listOfEntities.get(j)) ) {
							Entity e1 = objSen.listOfEntities.get(i);
							Entity e2 = objSen.listOfEntities.get(j);
							
							if ( satisfyRelArgConstraint(e1, e2) ) {
								// automatically added negative instances have no sub-type
								objSen.listRels.add( new Relation( objSen.listOfEntities.get(i), objSen.listOfEntities.get(j), 
										false, "", ""));
							}
						}	
					}
				}
			}			
			
			listSentence.add(objSen);
			
			// generate negative instances
			if ( !TextUtility.isEmptyString(entPairFileName) ) {
				
				for ( int i=0; i < objSen.listRels.size(); i++ ){
					String pair = objSen.listRels.get(i).arg1 + "\t" + objSen.listRels.get(i).arg2;
					if ( !listOfPairs.contains(pair) )
						listOfPairs.add(pair);
					else
						System.out.println("Already added: " + pair);
					
					FileUtility.writeInFile(entPairFileName, pair + "\n", true);
				}
			}
		}
		
		if ( listOfPairs.size() > 0 )
			listOfPairs = new ArrayList<String>();
		
		String entTypesFound = "";
		for ( int i=0; i<listOfFoundTypesOfEntities.size(); i++ )
			entTypesFound += listOfFoundTypesOfEntities.get(i) + " ";
		
		System.out.println("Following entity types are detected in the data:\n" + entTypesFound + "\n");
		
		return listSentence;
    }
	
	/**
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 * @throws Exception
	 */
	public int getNumberOfEntInBetween ( Entity e1, Entity e2 ) {
		
		int fi = -1, si = -1;
		
		for ( int i=0; i<listOfEntities.size(); i++ )
			if ( listOfEntities.get(i).id.equals(e1.id) ) {
				fi = i;
				break;
			}
		
		for ( int i=0; i<listOfEntities.size(); i++ )
			if ( listOfEntities.get(i).id.equals(e2.id) ) {
				si = i;
				break;
			}
		
		return Math.abs(si-fi) - 1; 
	}
	
	/**
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 * @throws Exception 
	 */
	public boolean getPolarityOfRelation ( Entity e1, Entity e2 ) throws Exception {
		
		return getPolarityOfRelation( new String[] {e1.id, e2.id} ); 
	}
	
	/**
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 * @throws Exception 
	 */
	public boolean getPolarityOfRelation ( String[] relArgs ) throws Exception {
		
		for ( int r=0; r<listRels.size(); r++ ) {
			if ( ( listRels.get(r).arg1.equals(relArgs[0]) && listRels.get(r).arg2.equals(relArgs[1]) )
					|| ( listRels.get(r).arg1.equals(relArgs[1]) && listRels.get(r).arg2.equals(relArgs[0]) ) ) {
				return listRels.get(r).isPositive;
			}
		}
		
		if ( !Relation.printRelPolarity )
			return false;
		else
			throw new Exception("Relation not found");
	}
	
	/**
	 * 
	 * @param eId
	 * @return
	 */
	public Entity getEntityById ( String eId ) {
		
		for ( int e=0; e<listOfEntities.size(); e++ ) {
			if ( listOfEntities.get(e).id.equals(eId)  )					
				return listOfEntities.get(e);
		}
		
		return null; 
	}
	
	
	/**
	 * 
	 * @param objDepParseOfSen
	 * @return
	 */
	public DependencyTree getDependencyTree ( DependencyParseOfSen objDepParseOfSen ) {
		
		detectBoundariesAndLemmas(objDepParseOfSen.tokAndPos);
		return new DependencyTree( arrWordAndPosByParser, 
				DataStrucUtility.listToStringArray(objDepParseOfSen.listOfDeps), getWordsAndNE());
	}

	/**
	 * 
	 */
	public void sortEntitiesByBoundary() {
		
		// sort entities
		for ( int e=0; e<listOfEntities.size()-1; e++ ) {
			for ( int d=e+1; d<listOfEntities.size(); d++ ) {
				if ( listOfEntities.get(e).startIndex > listOfEntities.get(d).startIndex ) {
					Entity tmp = listOfEntities.get(e).clone();
					Entity tmpX = listOfEntities.get(d).clone();
					
					listOfEntities.set(d, tmp);
					listOfEntities.set(e, tmpX);
				}
			}
		}
	}
	

	/**
	 * 
	 * @param boundaryCharIndexes
	 * @return
	 */
	public int[] getBoundaryWordIndexFromCharIndex( int[] boundaryCharIndexes ) {

		int[] bn = new int[2];
		
		for ( int i=0; i<arrBoundariesByWordIndexes.length; i++ ) {
			if ( arrBoundariesByWordIndexes[i][0] >= boundaryCharIndexes[0] || 
					(arrBoundariesByWordIndexes[i][0] < boundaryCharIndexes[0] && arrBoundariesByWordIndexes[i][1] >= boundaryCharIndexes[0] ) ) {
				bn[0] = i;
				break;
			}			
		}
		
		for ( int i=arrBoundariesByWordIndexes.length-1; i>=0; i-- ) {
			if ( arrBoundariesByWordIndexes[i][1] <= boundaryCharIndexes[1] || 
					(arrBoundariesByWordIndexes[i][0] <= boundaryCharIndexes[1] && arrBoundariesByWordIndexes[i][1] >= boundaryCharIndexes[0] ) ) {
				bn[1] = i;
				break;
			}			
		}
		
		return bn;
	}
}