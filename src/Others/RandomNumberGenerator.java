package Others;

import java.util.ArrayList;
import java.util.Random;

public class RandomNumberGenerator {

	/**
	 * 
	 * @param numberOfElements
	 * @param maxValueRange
	 */
	public static void generateRandomNumberList( int numberOfElements, int maxValueRange ) {		
		Random randomGenerator = new Random();
		listOfDiscardedNegPairs = new ArrayList<Integer>();
		
	    for (int idx = 1; idx <= numberOfElements; ++idx){
	    	listOfDiscardedNegPairs.add(randomGenerator.nextInt(maxValueRange));
	    }
	}
	
	public static ArrayList<Integer> listOfDiscardedNegPairs = new ArrayList<Integer>();
}
