package DataProcessor;

public class DDI2013Processor {
	
	public static void main ( String[] args ) throws Exception {
		
		String path = "../../data/SemEval2013/task-9-ddi/test_data/task9.2/combined";
		
		new UniXmlToHyRexDataConvertor().transformSeveralFilesIntoUnifiedFormat(path, path + "/../test.ddi2013.xml", "ddi=\"true\"");
		new UniXmlToHyRexDataConvertor().extractDataFromXml( path + "/../test.ddi2013.xml", 
				path + "/../test.sen", path + "/../test.ddi2013.full", "", true);
		
		
	//	new UniXmlToHyRexDataConvertor().splitTrainingDocuments("ddi");
		
	}

}
