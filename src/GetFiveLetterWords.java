import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class GetFiveLetterWords {

	public static void main(String[] args) throws IOException {
		BufferedReader dictionary = new BufferedReader(new FileReader("Unlimited Dictionary.txt"));
		PrintWriter out = new PrintWriter("Unlimited Five Letter Words.txt");
		
		String s;
		while ((s = dictionary.readLine()) != null)
			if (s.length() == 5)
				out.append(s + '\n');
		
		dictionary.close();
		out.close();
	}

}
