
public class Guess implements Comparable<Guess> {

	String word;
	double score;
	
	public Guess(String word, double score) {
		this.word = word;
		this.score = score;
	}
	
	public String getWord() {
		return word;
	}
	
	public int compareTo(Guess g) {
		if (score > g.score)
			return 1;
		else if (score < g.score)
			return -1;
		else
			return 0;
	}

}
