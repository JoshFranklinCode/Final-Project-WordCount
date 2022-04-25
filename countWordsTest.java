package application;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class countWordsTest {

	/** Test method will send the wordCollection ArrayList and the words
	 * HashMap to the countWords method. It will then check whether or not the
	 * method returns the hashMap filled. If filled, it will return true as
	 * assertNotNull is found to be true.
	 * AssertNull will return false as the HashMap IS being returned with
	 * values present.
	 */
	@Test
	void test() {
		Main test = new Main();
		ArrayList<String> wordCollection = new ArrayList<String>();
		Map<String, Integer> words = new HashMap<String, Integer>();
		
		wordCollection.add("Hi");
		words.put("Hi", 0);
		
		Map<String, Integer> output = test.countWords(wordCollection, words);
		Assertions.assertNotNull(output);
	}

}