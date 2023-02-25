import java.io.*;
import java.util.*;

public class ComplimentFinder {

	public static void main(String[] args) throws IOException {
		ArrayList<String> words = new ArrayList<>();
		
		BufferedReader in = new BufferedReader(new FileReader("Collins Scrabble Words (2019).txt"));
		String s;
		while ((s = in.readLine()) != null)
			if (s.length() == 5)
				words.add(s);
		in.close();
		
		String letters = "earoilstnud";
		int[] letterFreq = new int[26];
		for (int i = 0; i < letters.length(); i++)
			letterFreq[letters.charAt(i) - 'a']++;
		
		for (String word1 : words) {
			int[] word1Freq = new int[26];
			for (int i = 0; i < word1.length(); i++)
				word1Freq[word1.charAt(i) - 'A']++;
			
			for (String word2 : words) {
				int[] word2Freq = word1Freq.clone();
				for (int i = 0; i < word2.length(); i++)
					word2Freq[word2.charAt(i) - 'A']++;
				
				boolean compliment = true;
				for (int i = 0; i < 26; i++)
					if (word2Freq[i] > 0 && word2Freq[i] != letterFreq[i] ) {
						compliment = false;
						break;
					}
				
				String searl = "searl";
				for (int i = 0; i < 5; i++)
					if (word1Freq[searl.charAt(i) - 'a'] == 0) {
						compliment = false;
						break;
					}
				
				if (compliment) {
					word1 = word1.toLowerCase();
					word2 = word2.toLowerCase();
					
					System.out.print(word1 + ' ');
				
					for (int i = 0; i < letters.length(); i++)
						if (word1.indexOf(letters.charAt(i)) != -1)
							System.out.print(Character.toUpperCase(letters.charAt(i)));
						else
							System.out.print('_');
					
					System.out.print('\n' + word2 + ' ');
					
					for (int i = 0; i < letters.length(); i++)
						if (word2.indexOf(letters.charAt(i)) != -1)
							System.out.print(Character.toUpperCase(letters.charAt(i)));
						else
							System.out.print('_');
					
					System.out.println('\n');
				}
			}
		}
	}

}
