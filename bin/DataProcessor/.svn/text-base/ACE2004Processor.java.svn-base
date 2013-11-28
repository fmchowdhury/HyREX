package DataProcessor;

import Utility.*;



class ACE2004Processor {
	
	public static void main( String[] args ) throws Exception{
		/*
		new ACE2004Processor().extractAndMergeText("/media/Study/data_other/ACE 2004/data/English", 
				"/media/Study/data_other/ACE 2004/data/English/all_bnews_nwire_text");
		
		new ACE2004Processor().detectSentenceBoundaries("/media/Study/data_other/ACE 2004/data/English/all_bnews_nwire_text", 
				"/media/Study/data_other/ACE 2004/data/English/all_bnews_nwire_sentences");
		* /
		new SyntacticParser().callStanfordParser("all_bnews_nwire_text", 
				"all_bnews_nwire_sentences_parsed",
				"/hltsrv0/chowdhury/installed_programs/stanford-parser-2010-07-09/englishPCFG.ser.gz");
		*/
	}
	

	public void extractAndMergeText( String aceEngDataFilePath, String combinedTextFileName ) throws Exception{
		String[] temp1 = FileUtility.getFileNamesFromDir(aceEngDataFilePath + "/bnews"),
				temp2 = FileUtility.getFileNamesFromDir(aceEngDataFilePath + "/nwire");
	
		String[] arrFileNames = new String[temp1.length + temp2.length];
		
		int j = 0;
		for ( int i=0; i<temp1.length; i++ ){
			arrFileNames[j] = aceEngDataFilePath + "/bnews/" + temp1[i];
			j++;
		}
		
		for ( int i=0; i<temp2.length; i++ ){
			arrFileNames[j] = aceEngDataFilePath + "/nwire/" + temp2[i];
			j++;
		}
		
		FileUtility.writeInFile(combinedTextFileName, "", false);
		
		for ( int i=0; i<arrFileNames.length; i++ )
			if ( arrFileNames[i].contains(".sgm") ){
				String contents = FileUtility.readFileContents(arrFileNames[i]);
				
				int sIndex = contents.indexOf("<TEXT>") + 6, eIndex = contents.indexOf("</TEXT>", sIndex);
				contents = contents.substring(sIndex, eIndex).replaceAll("<.+>", " ").replaceAll("\\s+", " ");
				contents = TextUtility.replaceSepcialXmlCharsWithOriginals(contents).trim();
				
				FileUtility.writeInFile(combinedTextFileName, contents + "\n\n", true);
			}
		
	}
	
	/*
	static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
    static final SentenceModel SENTENCE_MODEL  = new IndoEuropeanSentenceModel();
    
	public void detectSentenceBoundaries( String inFileName, String outFileName) throws Exception{
		
		String text = Files.readFromFile(new File(inFileName),"ISO-8859-1");
		StringBuilder sb = new StringBuilder();
		
		ArrayList<String> tokenList = new ArrayList<String>();
		ArrayList<String> whiteList = new ArrayList<String>();
		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(text.toCharArray(),0,text.length());
		tokenizer.tokenize(tokenList,whiteList);

		System.out.println(tokenList.size() + " TOKENS");
		System.out.println(whiteList.size() + " WHITESPACES");

		String[] tokens = new String[tokenList.size()];
		String[] whites = new String[whiteList.size()];
		tokenList.toArray(tokens);
		whiteList.toArray(whites);
	
		int[] sentenceBoundaries = SENTENCE_MODEL.boundaryIndices(tokens,whites);

		System.out.println(sentenceBoundaries.length 
				   + " SENTENCE END TOKEN OFFSETS");
			
		if (sentenceBoundaries.length < 1) {
		    System.out.println("No sentence boundaries found.");
		    return;
		}
		
		int sentStartTok = 0, sentEndTok = 0;
		for (int i = 0; i < sentenceBoundaries.length; ++i) {
		    sentEndTok = sentenceBoundaries[i];
		    		    
		    for (int j=sentStartTok; j<=sentEndTok; j++) {
		    	sb.append(tokens[j]+whites[j+1]);
		    }
		    
		    sb.append("\n\n");
		    sentStartTok = sentEndTok+1;
		}
		
		Utility.writeInFile(outFileName, sb.toString(), false);
	}
	*/
	
	public void detectSentenceBoundaries( String inFileName, String outFileName) throws Exception{
		
		
	}
}
