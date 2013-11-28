package DataProcessor;

import java.util.ArrayList;

import Kernels.TKOutputGenerator;
import Structures.DepTreeNode;
import Structures.DependencyParseOfSen;
import Structures.DependencyTree;
import Structures.Sentence;
import Utility.CommonUtility;
import Utility.DataStrucUtility;

public class CueDetector {

	public static ArrayList<String> listOfGovernWordsForNonRelatedEnt = new ArrayList<String>();
	
	ArrayList<String> listOfGovernRelTypeForNonRelatedEnt = new ArrayList<String>();
	ArrayList<Integer> listOfCoundOfNonRelatedEnt = new ArrayList<Integer>();
	
	ArrayList<String> listOfGovernWordsForRelatedEnt = new ArrayList<String>();
	ArrayList<String> listOfGovernRelTypeForRelatedEnt = new ArrayList<String>();
	
	
	/**
	 * Detect the governor words of entities which if present then the corresponding entities do have relation with other entities.   
	 * @throws Exception 
	 */
	
	public static void main ( String[] args ) throws Exception {
		/*
		 * Read the dependency tree of the sentence
		 * Find the entities which do not have any relation with any other entities
		 * Add their governing words and dependency types in list X, if not already added
		 * Find the entities which have at least one relation with one of the other entities
		 * Add their governing words and dependency types in list Y, if not already added
		 * Once the above steps are done for all sentences, remove any element in X that is in Y
		 * 
		 * The remaining governors must have at least once alphabetic character 
		 */

		String corpus="lll";
		String fullDataFileName ="../../data/5_PPI_corpora/" + corpus + "/" + corpus + ".full";
		String parsedText ="../../data/5_PPI_corpora/" + corpus + "/" + corpus + ".parsed.bllip.complete";
		
		String trainDepParsedFile = CommonUtility.OUT_DIR + parsedText.substring(parsedText.lastIndexOf("/"))  + "_dt";
		
		ArrayList<Sentence> listOfSentences = Sentence.readFullData(fullDataFileName, "", "", "", false);
		new TKOutputGenerator().prepareData(parsedText, listOfSentences, "");
		
		new CueDetector().extractAntiPositiveEntityGovernerWords( fullDataFileName, trainDepParsedFile); 
	}
	
	/**
	 * 
	 * @param fullDataFileName
	 * @param depParsedFile
	 * @throws Exception
	 */
	public void extractAntiPositiveEntityGovernerWords ( String fullDataFileName, String depParsedFile ) throws Exception {
		
		listOfGovernRelTypeForNonRelatedEnt = new ArrayList<String>();
		
		listOfCoundOfNonRelatedEnt = new ArrayList<Integer>();
		listOfGovernWordsForNonRelatedEnt = new ArrayList<String>();
		
		listOfGovernWordsForRelatedEnt = new ArrayList<String>();
		listOfGovernRelTypeForRelatedEnt = new ArrayList<String>();
		
		/*
		String[] pre = new String[]{"lll", "hprd50", "iepa", "aimed", "bioinfer"};
		
		for ( int i=0; i<pre.length; i++ ) {
			
			if ( corpFuleFileName.contains(pre[i]))
				continue;
			
			String corpus = pre[i];
			
			String tmpFullDataFileName ="/hardmnt/amosoz0/tcc/chowdhury/workspace/5_PPI_corpora/" + corpus + "/" + corpus + ".full";
			String parsedText ="/hardmnt/amosoz0/tcc/chowdhury/workspace/5_PPI_corpora/" + corpus + "/" + corpus + ".parsed.bllip.complete";
			String tmpDepParsedFile = CommonUtility.OUT_DIR + parsedText.substring(parsedText.lastIndexOf("/"))  + "_dt";
			new TKOutputGenerator().prepareData(parsedText, tmpFullDataFileName, "");
			
			ArrayList<Sentence> listSentence = Sentence.readFullData(tmpFullDataFileName);
			ArrayList<DependencyParseOfSen> listDepParseOfAllSen = DependencyParseOfSen.readDepParseForAllSen(tmpDepParsedFile);
			
			extractAntiPositiveEntityGovernerWords(listSentence, listDepParseOfAllSen);
		}
		*/
		listOfGovernRelTypeForNonRelatedEnt = new ArrayList<String>();		
		listOfCoundOfNonRelatedEnt = new ArrayList<Integer>();
		listOfGovernWordsForNonRelatedEnt = new ArrayList<String>();
		
		ArrayList<Sentence> listSentence = Sentence.readFullData( fullDataFileName, "", depParsedFile, "", false);		
		extractAntiPositiveEntityGovernerWords(listSentence);
		
		/*
		Triggers.readTriggersAndNegativeWord();
		
		ArrayList<String> tmpList = new ArrayList<String>();
		
		for ( int i=0; i<listOfGovernWordsForNonRelatedEnt.size(); i++ ) {
			String word = listOfGovernWordsForNonRelatedEnt.get(i);
		
			int tgCharIndex = Triggers.listOf1stChar.indexOf(word.charAt(0));
			
			if ( tgCharIndex < 0 || Triggers.listOfTriggers.get(tgCharIndex).indexOf(word) < 0  )
				tmpList.add(listOfGovernWordsForNonRelatedEnt.get(i));
		}
		
		listOfGovernWordsForNonRelatedEnt = tmpList;
		*/		
	}
	
	/**
	 * 
	 * @param listSentence
	 * @param listDepParseOfAllSen
	 */
	private void extractAntiPositiveEntityGovernerWords ( ArrayList<Sentence> listSentence ) {
	//	int totalEnt = 0, totalOrigEnt = 0, singEntSent = 0;
		
		for ( int s=0; s<listSentence.size(); s++ ) {			
			/*
			 if ( listSentence.get(s).listOfEntities.size() < 2 ) {
			 
				if ( s ==0 || !listSentence.get(s).senID.equals(listSentence.get(s-1).senID) )
				singEntSent++;
			}
			//else
				if ( s ==0 || !listSentence.get(s).senID.equals(listSentence.get(s-1).senID) ) {
				totalEnt+=listSentence.get(s).listOfEntities.size();
				String[] arr = listSentence.get(s).text.split("Xxx Entity");
				totalOrigEnt+=arr.length-1;
			}
			*/
			Sentence objCurSen = listSentence.get(s);
			DependencyTree dt = objCurSen.depTree;
			
			for ( int e=0; e<objCurSen.listOfEntities.size(); e++ ) {
			
				DepTreeNode headOfEnt1 = dt.getHeadWordFromWordBoundaries( objCurSen.listOfEntities.get(e).getAllWordIndexes(), true, objCurSen);
				
				if ( headOfEnt1 == null ) {
					System.out.println("Empty head word found for   " + objCurSen.listOfEntities.get(e).printString());
					continue;
				}
				
				boolean hasNoRel = true;
				for ( int r=0; r<objCurSen.listRels.size(); r++ ) {
					if ( objCurSen.listRels.get(r).isPositive && 
							( objCurSen.listRels.get(r).arg1.equals(objCurSen.listOfEntities.get(e).id)
								|| objCurSen.listRels.get(r).arg1.equals(objCurSen.listOfEntities.get(e).id)) ) {
						hasNoRel = false;
						break;
					}					
				}
				
				if ( hasNoRel ) {
					for ( int p=0; headOfEnt1.getParentsWordIndexes() != null && p<headOfEnt1.getParentsWordIndexes().size(); p++ ) {
						int x =0;		
						if ( dt.allNodesByWordIndex[headOfEnt1.getParentsWordIndexes().get(p)].lemma.toLowerCase().matches("(entity[0-9]+)") )
							;
						else if ( (x=listOfGovernWordsForNonRelatedEnt.indexOf( dt.allNodesByWordIndex[headOfEnt1.getParentsWordIndexes().get(p)].lemma.toLowerCase())) > -1
								//&& String.valueOf(listOfGovernRelTypeForNonRelatedEnt.indexOf(x)).equals(headOfEnt1.getRelNamesWithParents().get(p))
								) {
							; // do nothing
							listOfCoundOfNonRelatedEnt.set(x, listOfCoundOfNonRelatedEnt.get(x)+1);
						}
						else {
							listOfGovernWordsForNonRelatedEnt.add( dt.allNodesByWordIndex[headOfEnt1.getParentsWordIndexes().get(p)].lemma.toLowerCase());
							listOfGovernRelTypeForNonRelatedEnt.add(headOfEnt1.getRelNamesWithParents().get(p));
							listOfCoundOfNonRelatedEnt.add(1);
						}
					}
				}
				else {
					for ( int p=0; headOfEnt1.getParentsWordIndexes() != null && p<headOfEnt1.getParentsWordIndexes().size(); p++ ) {
						int x =0;					
						if ( dt.allNodesByWordIndex[headOfEnt1.getParentsWordIndexes().get(p)].lemma.toLowerCase().matches("(entity[0-9]+)") )
							;
						else if ( (x=listOfGovernWordsForRelatedEnt.indexOf( dt.allNodesByWordIndex[headOfEnt1.getParentsWordIndexes().get(p)].lemma.toLowerCase())) > -1
								//&& String.valueOf(listOfGovernRelTypeForRelatedEnt.indexOf(x)).equals(headOfEnt1.getRelNamesWithParents().get(p)) 
								)
							x++; // do nothing
						else {
							listOfGovernWordsForRelatedEnt.add( dt.allNodesByWordIndex[headOfEnt1.getParentsWordIndexes().get(p)].lemma.toLowerCase());
							listOfGovernRelTypeForRelatedEnt.add(headOfEnt1.getRelNamesWithParents().get(p));
						}
						
					//	ArrayList<Integer> listOfGrandParentIndexes = dt.allNodesByWordIndex[headOfEnt1.getParentsWordIndexes().get(p)].getParentsWordIndexes();
						/*
						// add grand-parents as well
						for ( int gp=0; listOfGrandParentIndexes != null && gp<listOfGrandParentIndexes.size(); gp++ ) {
							if ( dt.allNodesByWordIndex[listOfGrandParentIndexes.get(gp)].lemma.toLowerCase().matches("(entity[0-9]+)") )
								;
							else if ( (x=listOfGovernWordsForRelatedEnt.indexOf( dt.allNodesByWordIndex[listOfGrandParentIndexes.get(gp)].lemma.toLowerCase())) > -1
									//&& String.valueOf(listOfGovernRelTypeForRelatedEnt.indexOf(x)).equals(headOfEnt1.getRelNamesWithParents().get(p)) 
									)
								; // do nothing
							else {
								listOfGovernWordsForRelatedEnt.add( dt.allNodesByWordIndex[listOfGrandParentIndexes.get(gp)].lemma.toLowerCase());
								//listOfGovernRelTypeForRelatedEnt.add(headOfEnt1.getRelNamesWithParents().get(p));
							}
						}
						*/
					}
				}
				
			}
		}
		
		String[] arrOfGovernWordsForRelatedEnt = DataStrucUtility.listToStringArray(listOfGovernWordsForRelatedEnt);
		
		for ( int z=0; z<listOfGovernWordsForNonRelatedEnt.size(); z++ ) {
			int x = 0;
			
			for ( x=0; x<arrOfGovernWordsForRelatedEnt.length; x++ )
				if ( arrOfGovernWordsForRelatedEnt[x].contains(listOfGovernWordsForNonRelatedEnt.get(z)) 
						|| listOfGovernWordsForNonRelatedEnt.get(z).contains(arrOfGovernWordsForRelatedEnt[x]) )
					break;
			
			if ( x<arrOfGovernWordsForRelatedEnt.length
					//(x=listOfGovernWordsForRelatedEnt.indexOf( listOfGovernWordsForNonRelatedEnt.get(z))) > -1
				//	&& String.valueOf(listOfGovernRelTypeForRelatedEnt.indexOf(x)).equals(listOfGovernRelTypeForNonRelatedEnt.get(z)) 
			//	|| listOfCoundOfNonRelatedEnt.get(z) < 2 
					|| !listOfGovernWordsForNonRelatedEnt.get(z).matches(".*[a-z]+.*")
				) {
				listOfGovernWordsForNonRelatedEnt.remove(z);
				listOfGovernRelTypeForNonRelatedEnt.remove(z);
				listOfCoundOfNonRelatedEnt.remove(z);
				z--;
			}
		}
		/*
		for ( int z=0; z<listOfGovernWordsForNonRelatedEnt.size(); z++ ) {
			System.out.println(listOfGovernWordsForNonRelatedEnt.get(z) + "  " + listOfGovernRelTypeForNonRelatedEnt.get(z));
		}
		*/
		
		//System.out.println( totalEnt + "  " + totalOrigEnt + "  " + singEntSent);
	}
}
