import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Wordle {

	static final int TILE_SIZE = 80;
	static final int SPACING = 10;
	
	static final int WORD_LENGTH = 5;
	static final int NUM_GUESSES = 6;
	
	static final Color GREEN = new Color(0x6aaa64);
	static final Color YELLOW = new Color(0xc9b458);
	static final Color DARK_GREY = new Color(0x787c7e);
	static final Color LIGHT_GREY = new Color(0xd3d6da);
	
	static ArrayList<String> wordList;
	static HashMap<String, HashMap<String, Tile[]>> patterns;
	
	static char[][] board;
	static Tile[][] evaluations;
	
	static String goalWord;
	static LinkedList<String> possibilities;
	
	static String currWord;
	static int currRow, currCol;
	
	static boolean inputting;
	static boolean gameOver;
	
	static int notGrey = 0;
	
	public static void reset() {
		board = new char[NUM_GUESSES][WORD_LENGTH];
		evaluations = new Tile[NUM_GUESSES][WORD_LENGTH];
		
		possibilities = new LinkedList<>(wordList);
		
		goalWord = wordList.get((int) (Math.random() * wordList.size()));
//		goalWord = "";
		
		currWord = "";
		currRow = 0;
		currCol = 0;
		
		inputting = false;
		gameOver = false;
	}
	
	public static boolean isValid(String s) {
		return Collections.binarySearch(wordList, s) >= 0;
	}
	
	public static void computeEvaluations() {
		patterns = new HashMap<>();
		for (String s1 : wordList) {
			HashMap<String, Tile[]> evals = new HashMap<>();
			for (String s2 : wordList) {
				Tile[] eval = new Tile[WORD_LENGTH];
				
				int[] freq = new int[26];
				for (int i = 0; i < s1.length(); i++)
					freq[s1.charAt(i) - 'a']++;
				
				for (int i = 0; i < WORD_LENGTH; i++)
					if (s1.charAt(i) == s2.charAt(i)) {
						eval[i] = Tile.CORRECT;
						freq[s1.charAt(i) - 'a']--;
					}
				
				for (int i = 0; i < WORD_LENGTH; i++)
					if (eval[i] == null)
						if (freq[s2.charAt(i) - 'a'] == 0)
							eval[i] = Tile.ABSENT;
						else {
							eval[i] = Tile.PRESENT;
							freq[s1.charAt(i) - 'a']--;
						}
				boolean allAbsent = true;
				for (int i = 0; i < eval.length; i++)
					if (eval[i] != Tile.ABSENT) {
						allAbsent = false;
						break;
					}
				if (!allAbsent) {
//					evals.put(s2, eval);
					++notGrey;
				}
			}
			patterns.put(s1, evals);
		}
		System.out.println(notGrey);
	}
	
	public static void updatePossibilities(Tile[] eval) {
		for (Iterator<String> it = possibilities.iterator(); it.hasNext();) {
			String word = it.next();
			for (Entry<String, Tile[]> e : patterns.get(word).entrySet()) {
				boolean match = true;
				for (int i = 0; i < WORD_LENGTH; i++)
					if (eval[i] != e.getValue()[i]) {
						match = false;
						break;
					}
				if (!match)
					it.remove();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		JFrame frame = new JFrame("Wordle");
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		
		wordList = new ArrayList<>();
		BufferedReader in = new BufferedReader(new FileReader("Collins Scrabble Words (2019).txt"));
		String line;
		while ((line = in.readLine()) != null)
			if (line.length() == WORD_LENGTH)
				wordList.add(line.toLowerCase());
		in.close();
		
		computeEvaluations();
		
		reset();
		
		JPanel boardPanel = new JPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				g.setFont(new Font("Clear Sans", Font.PLAIN, 60));
				int y = SPACING;
				for (int i = 0; i < NUM_GUESSES; i++) {
					int x = SPACING;
					for (int j = 0; j < WORD_LENGTH; j++) {
						if (evaluations[i][j] == null) {
							g.setColor(LIGHT_GREY);
							g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
							g.setColor(Color.BLACK);
						}
						else {
							if (evaluations[i][j] == Tile.CORRECT)
								g.setColor(GREEN);
							else if (evaluations[i][j] == Tile.PRESENT)
								g.setColor(YELLOW);
							else
								g.setColor(DARK_GREY);
							g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
							g.setColor(Color.WHITE);
						}
						if (board[i][j] != 0) {
							String letter = Character.toString(Character.toUpperCase(board[i][j]));
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
				if (gameOver)
					return;
				
				if (currCol < WORD_LENGTH) {
					char c = Character.toLowerCase(e.getKeyChar());
					if (inputting) {
						if (c == '1')
							evaluations[currRow][currCol++] = Tile.ABSENT;
						else if (c == '2')
							evaluations[currRow][currCol++] = Tile.PRESENT;
						else if (c == '3')
							evaluations[currRow][currCol++] = Tile.CORRECT;
					}
					else if (c >= 'a' && c <= 'z') {
						currWord += c;
						board[currRow][currCol++] = c;
					}
					boardPanel.repaint();
				}
			}
			
			public void keyPressed(KeyEvent e) {
				if (gameOver)
					return;
				
				if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && currCol > 0) {
					if (inputting)
						evaluations[currRow][--currCol] = null;
					else {
						currWord = currWord.substring(0, --currCol);
						board[currRow][currCol] = 0;
					}
					boardPanel.repaint();
				}
				else if (e.getKeyCode() == KeyEvent.VK_ENTER && currCol == WORD_LENGTH) {
					if (inputting) {
						updatePossibilities(evaluations[currRow]);
						currWord = "";
						currRow++;
						currCol = 0;
						inputting = false;
						boardPanel.repaint();
					}
					else {
						if (currCol == WORD_LENGTH && isValid(currWord)) {
							currCol = 0;
							inputting = true;
						}
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
		panel.add(boardPanel);
		
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
