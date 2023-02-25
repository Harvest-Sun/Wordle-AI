import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class UnlimitedLetterFrequency {

	public static void main(String[] args) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader("Unlimited Five Letter Words.txt"));
		
		int[] freq = new int[26];
		
		String s;
		while ((s = in.readLine()) != null)
			for (int i = 0; i < s.length(); i++)
				freq[s.charAt(i) - 'A']++;
		
		in.close();
		
		TreeMap<Character, Integer> m = new TreeMap<>();
		for (int i = 0; i < 26; i++)
			m.put((char) (i + 'a'), freq[i]);
		
		ArrayList<Map.Entry<Character, Integer>> list = new ArrayList<>(m.entrySet());
		list.sort(Map.Entry.comparingByValue());
		Collections.reverse(list);
		
		for (Map.Entry<Character, Integer> entry : list)
			System.out.println(entry.getKey() + " " + entry.getValue());
		
		System.out.println();
		for (int i = 1; i <= 26; i++)
			System.out.print(i % 10);
		
		System.out.println();
		for (Map.Entry<Character, Integer> entry : list)
			System.out.print(entry.getKey());
	}

}
