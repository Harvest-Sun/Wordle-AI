import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Fibble {

	static final int TILE_SIZE = 75;
	static final int SPACING = 10;
	
	static final String DICTIONARY_FILE = "Collins Scrabble Words (2019)";
	
	static final int WORD_LENGTH = 5;
	static final int NUM_GUESSES = 9;
	
	static final Color GREEN = new Color(0x6aaa64);
	static final Color YELLOW = new Color(0xc9b458);
	static final Color DARK_GREY = new Color(0x787c7e);
	static final Color LIGHT_GREY = new Color(0xd3d6da);
	
	static JPanel boardPanel;
	static JTextArea candidateArea;
	static JTextArea guessArea;
	
	static String[] dictionary;
	static HashMap<String, Integer> indices;
	static int[][] patternTable;
	static LinkedList<String> candidates;
	static LinkedList<Guess> bestGuesses;
	static LinkedList<Guess> firstGuesses;
	
	static char[][] letters;
	static int[] evaluations;
	static String currWord;
	static int currRow, currCol;
	
	static boolean evaluating;
	static boolean gameOver;
	
	public static String[] readDictionary() {
		ArrayList<String> dictionary = new ArrayList<>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(DICTIONARY_FILE + ".txt"));
			String line;
			while ((line = in.readLine()) != null)
				if (line.length() == WORD_LENGTH)
					dictionary.add(line.toLowerCase());
			in.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return dictionary.toArray(new String[0]);
	}
	
	public static HashMap<String, Integer> assignIndices() {
		HashMap<String, Integer> indices = new HashMap<>();
		for (int i = 0; i < dictionary.length; i++)
			indices.put(dictionary[i], i);
		return indices;
	}
	
	public static boolean isEmpty(int mask, int pos) {
		return (mask & 3 << pos * 2) == 0;
	}
	
	public static boolean isAbsent(int mask, int pos) {
		return (mask & 3 << pos * 2) >> pos * 2 == 1;
	}
	
	public static boolean isPresent(int mask, int pos) {
		return (mask & 3 << pos * 2) >> pos * 2 == 2;
	}
	
	public static boolean isCorrect(int mask, int pos) {
		return (mask & 3 << pos * 2) >> pos * 2 == 3;
	}
	
	public static int setEmpty(int mask, int pos) {
		return mask & (-1 ^ 3 << pos * 2);
	}
	
	public static int setAbsent(int mask, int pos) {
		return mask | 1 << pos * 2;
	}
	
	public static int setPresent(int mask, int pos) {
		return mask | 2 << pos * 2;
	}
	
	public static int setCorrect(int mask, int pos) {
		return mask | 3 << pos * 2;
	}
	
	public static int[][] computePatterns() {
		long start = System.nanoTime();
		int[][] patternTable = new int[dictionary.length][dictionary.length];
		for (String guess : dictionary) {
			int guessIndex = indices.get(guess);
			for (String target : dictionary) {
				int pattern = 0;
				int[] targetFreq = new int[26];
				for (int i = 0; i < WORD_LENGTH; i++)
					if (guess.charAt(i) == target.charAt(i))
						pattern = setCorrect(pattern, i);
					else
						targetFreq[target.charAt(i) - 'a']++;
				
				for (int i = 0; i < WORD_LENGTH; i++)
					if (isEmpty(pattern, i))
						if (targetFreq[guess.charAt(i) - 'a'] == 0)
							pattern = setAbsent(pattern, i);
						else {
							pattern = setPresent(pattern, i);
							targetFreq[guess.charAt(i) - 'a']--;
						}
				
				patternTable[guessIndex][indices.get(target)] = pattern;
			}
		}
		long end = System.nanoTime();
		System.out.println((end - start) / 1e9 + " s");
		return patternTable;
	}
	
	public static LinkedList<Guess> computeFirstGuesses() {
		long start = System.nanoTime();
		LinkedList<Guess> bestGuesses = new LinkedList<>();
		for (String guess : dictionary) {
			HashMap<Integer, Integer> freq = new HashMap<>();
			int guessIndex = indices.get(guess);
			for (String target : dictionary)
				freq.merge(patternTable[guessIndex][indices.get(target)], 1, Integer::sum);
			int total = 0;
			for (int remaining : freq.values())
				total += remaining * remaining;
			if (total != dictionary.length)
				bestGuesses.add(new Guess(guess, (double) total / dictionary.length));
		}
		bestGuesses.sort(null);
		
		long end = System.nanoTime();
		System.out.println((end - start) / 1e9 + " s");
		
		return bestGuesses;
	}
	
	public static void reset() {
		letters = new char[NUM_GUESSES][WORD_LENGTH];
		evaluations = new int[NUM_GUESSES];
//		for (int i = 0; i < evaluations.length; i++)
//			evaluations[i] = new Evaluation(); 
		candidates = new LinkedList<>(Arrays.asList(dictionary));
		updateCandidateArea();
		bestGuesses = new LinkedList<>(firstGuesses);
		updateGuessArea();
		
		currWord = "";
		currRow = 0;
		currCol = 0;
		
		evaluating = false;
		gameOver = false;
		
		boardPanel.repaint();
	}
	
	public static void addLetter(char letter) {
		currWord += letter;
		letters[currRow][currCol++] = letter; 
	}
	
	public static void deleteLetter() {
		currWord = currWord.substring(0, --currCol);
		letters[currRow][currCol] = 0;
	}
	
	public static boolean isValid(String word) {
		return indices.containsKey(word);
	}
	
	public static void updateCandidates() {
		int guessIndex = indices.get(currWord);
		for (Iterator<String> it = candidates.iterator(); it.hasNext();) {
			int currPattern = evaluations[currRow];
			int correctPattern = patternTable[guessIndex][indices.get(it.next())];
			int fibs = 0;
			for (int i = 0; i < 5; i++) {
				if (currPattern % 4 != correctPattern % 4) {
					fibs++;
					if (fibs > 1)
						break;
				}
				currPattern >>= 2;
				correctPattern >>= 2;
			}
			if (fibs != 1)
				it.remove();
		}
		
		if (evaluations[currRow] == 2047)
			gameOver = true;
		
		updateCandidateArea();
	}
	
	public static void updateCandidateArea() {
		candidateArea.setText("Candidate Words\n");
		for (String word : candidates)
			candidateArea.append('\n' + word);
		candidateArea.setCaretPosition(0);
	}
	
	public static void updateBestGuesses() {
		bestGuesses.clear();
		if (!gameOver) {
			for (String guess : dictionary) {
				HashMap<Integer, Integer> freq = new HashMap<>();
				int guessIndex = indices.get(guess);
				for (String target : candidates)
					freq.merge(patternTable[guessIndex][indices.get(target)], 1, Integer::sum);
				int total = 0;
				for (int remaining : freq.values())
					total += remaining * remaining;
				if (total != candidates.size())
					bestGuesses.add(new Guess(guess, (double) total / candidates.size()));
			}
			bestGuesses.sort(null);
		}
		updateGuessArea();
	}
	
	public static void updateGuessArea() {
		guessArea.setText("Best Guesses\n");
		if (candidates.size() == 1)
			guessArea.append('\n' + candidates.getFirst() + ' ' + 1.0);
		for (Guess guess : bestGuesses)
			guessArea.append('\n' + guess.getWord() + ' ' + Math.round(guess.score * 1000) / 1000.0);
		guessArea.setCaretPosition(0);
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Fibble");
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		
		boardPanel = new JPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				g.setFont(new Font("Clear Sans", Font.PLAIN, 60));
				int y = SPACING;
				for (int i = 0; i < NUM_GUESSES; i++) {
					int x = SPACING;
					for (int j = 0; j < WORD_LENGTH; j++) {
						if (isEmpty(evaluations[i], j)) {
							g.setColor(LIGHT_GREY);
							g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
							g.setColor(Color.BLACK);
						}
						else {
							if (isCorrect(evaluations[i], j))
								g.setColor(GREEN);
							else if (isPresent(evaluations[i], j))
								g.setColor(YELLOW);
							else
								g.setColor(DARK_GREY);
							g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
							g.setColor(Color.WHITE);
						}
						if (letters[i][j] != 0) {
							String letter = Character.toString(Character.toUpperCase(letters[i][j]));
							FontMetrics fm = g.getFontMetrics();
							Rectangle2D bounds = g.getFont().createGlyphVector(fm.getFontRenderContext(), letter).getVisualBounds();
							int letterX = x + (TILE_SIZE - fm.stringWidth(letter)) / 2;
							int letterY = y + (TILE_SIZE + (int) bounds.getHeight()) / 2;
							g.drawString(letter, letterX, letterY);
						}
						x += TILE_SIZE + SPACING;
					}
					y += TILE_SIZE + SPACING;
				}
			}
		};
		boardPanel.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				if (gameOver || currCol == WORD_LENGTH)
					return;
				
				char c = Character.toLowerCase(e.getKeyChar());
				if (evaluating) {
					if (c == '1')
						evaluations[currRow] = setAbsent(evaluations[currRow], currCol++);
					else if (c == '2')
						evaluations[currRow] = setPresent(evaluations[currRow], currCol++);
					else if (c == '3')
						evaluations[currRow] = setCorrect(evaluations[currRow], currCol++);
				}
				else if (c >= 'a' && c <= 'z')
					addLetter(c);
				boardPanel.repaint();
			}
			
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && currCol > 0 && !gameOver) {
					if (evaluating)
						evaluations[currRow] = setEmpty(evaluations[currRow], --currCol);
					else
						deleteLetter();
					boardPanel.repaint();
				}
				else if (e.getKeyCode() == KeyEvent.VK_ENTER && currCol == WORD_LENGTH) {
					if (gameOver)
						reset();
					else if (evaluating) {
						updateCandidates();
						updateBestGuesses();
						updateGuessArea();
						currWord = "";
						currRow++;
						currCol = 0;
						evaluating = false;
						boardPanel.repaint();
					}
					else if (isValid(currWord)) {
						currCol = 0;
						evaluating = true;
					}
				}
			}
			
			public void keyReleased(KeyEvent e) {
			}
		});
		boardPanel.setFocusable(true);
		boardPanel.requestFocusInWindow();
		boardPanel.setPreferredSize(new Dimension(WORD_LENGTH * (TILE_SIZE + SPACING) + SPACING, NUM_GUESSES * (TILE_SIZE + SPACING) + SPACING));
		boardPanel.setOpaque(false);
		
		candidateArea = new JTextArea();
		candidateArea.setFont(new Font("Clear Sans", Font.PLAIN, 18));
		candidateArea.setEditable(false);
		candidateArea.setFocusable(false);
		
		JScrollPane candidateScroll = new JScrollPane(candidateArea);
		candidateScroll.setPreferredSize(new Dimension(250, 500));
		candidateScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		candidateScroll.setFocusable(false);
		
		guessArea = new JTextArea();
		guessArea.setFont(new Font("Clear Sans", Font.PLAIN, 18));
		guessArea.setEditable(false);
		guessArea.setFocusable(false);
		
		JScrollPane guessScroll = new JScrollPane(guessArea);
		guessScroll.setPreferredSize(new Dimension(250, 500));
		guessScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		guessScroll.setFocusable(false);
		
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		resetButton.setPreferredSize(new Dimension(150, 50));
		resetButton.setFocusable(false);
		resetButton.setFont(new Font("Clear Sans", Font.PLAIN, 36));
		
		panel.add(resetButton);
		panel.add(boardPanel);
		panel.add(guessScroll);
		panel.add(candidateScroll);
		
		dictionary = readDictionary();
		indices = assignIndices();
		patternTable = computePatterns();
		firstGuesses = computeFirstGuesses();
		reset();
		
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
