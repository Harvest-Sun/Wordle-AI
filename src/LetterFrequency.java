import java.io.*;
import java.util.*;

public class LetterFrequency {

	public static void main(String[] args) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader("Collins Scrabble Words (2019).txt"));
		
		int[] totalFreq = new int[26];
		
		ArrayList<String> dictionary = new ArrayList<>();
		int numFive = 0;
		String line;
		while ((line = in.readLine()) != null)
			dictionary.add(line);
		
		for (int i = 0; i < dictionary.size(); i++) {
			String s = dictionary.get(i);
			if (s.length() == 5) {
				if (s.charAt(4) == 'S') {
					boolean isPlural = false;
					String singular = s.substring(0, 4);
					for (int j = i - 1; j >= 0 && dictionary.get(j).compareTo(singular) >= 0; j--) {
//						System.out.println(dictionary.get(j));
						if (dictionary.get(j).equals(singular)) {
							isPlural = true;
							break;
						}
					}
					if (isPlural)
						continue;
					System.out.println(s);
				}
				boolean[] wordFreq = new boolean[26];
				for (int j = 0; j < s.length(); j++)
					wordFreq[s.charAt(j) - 'A'] = true;
				for (int j = 0; j < 26; j++)
					if (wordFreq[j])
						totalFreq[j]++;
				numFive++;
			}
		}
		
		in.close();
		
		TreeMap<Character, Integer> m = new TreeMap<>();
		for (int i = 0; i < 26; i++)
			m.put((char) (i + 'a'), totalFreq[i]);
		
		ArrayList<Map.Entry<Character, Integer>> list = new ArrayList<>(m.entrySet());
		list.sort(Map.Entry.comparingByValue());
		Collections.reverse(list);
		
		for (Map.Entry<Character, Integer> entry : list)
			System.out.println(entry.getKey() + " " + entry.getValue() + ' ' + Math.round((double) entry.getValue() / numFive * 1000) / 10.0 + '%');
		
		System.out.println();
		for (int i = 1; i <= 26; i++)
			System.out.print(i % 10);
		
		System.out.println();
		for (Map.Entry<Character, Integer> entry : list)
			System.out.print(entry.getKey());
	}

}
