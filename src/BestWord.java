import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class BestWord extends JPanel implements KeyListener {

	final int SCREEN_WIDTH = 1000, SCREEN_HEIGHT = 750;
	
	final int TILE_SIZE = 78;
	final int SPACING = 10;
	final int WORD_LENGTH = 5;
	final int NUM_GUESSES = 6;
	final Color LIGHT_GREY = new Color(0xd3d6da), DARK_GREY = new Color(0x787c7e), YELLOW = new Color(0xc9b458), GREEN = new Color(0x6aaa64);
	
	String goalWord;
	
	char[][] guesses;
	Tile[][] tiles;
	
	int currRow, currCol;
	
	boolean gameOver = false;
	
	LinkedList<String> possibilities;
	JTextArea possibilitiesArea;
	
	public BestWord() {
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
		setLayout(null);
		
		possibilitiesArea = new JTextArea();
		possibilitiesArea.setFont(new Font("Clear Sans", Font.PLAIN, 18));
		possibilitiesArea.setEditable(false);
		possibilitiesArea.setFocusable(false);
		
		JScrollPane possibilitiesScroll = new JScrollPane(possibilitiesArea);
		possibilitiesScroll.setBounds((SCREEN_WIDTH + WORD_LENGTH * (TILE_SIZE + SPACING)) / 2 + 50, TILE_SIZE / 2, 200, NUM_GUESSES * (TILE_SIZE + SPACING) - SPACING);
		possibilitiesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		possibilitiesScroll.setFocusable(false);
		
		reset();
		
		add(possibilitiesScroll);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setFont(new Font("Clear Sans", Font.PLAIN, 60));
		for (int i = 0; i < NUM_GUESSES; i++)
			for (int j = 0; j < WORD_LENGTH; j++) {
				int x = (SCREEN_WIDTH - WORD_LENGTH * (TILE_SIZE + SPACING) - SPACING) / 2 + j * (TILE_SIZE + SPACING);
				int y = TILE_SIZE / 2 + i * (TILE_SIZE + SPACING);
				if (tiles[i][j] == null) {
					if (guesses[i][j] == 0)
						g.setColor(LIGHT_GREY);
					else
						g.setColor(Color.BLACK);
					g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
					g.setColor(Color.BLACK);
				}
				else {
					if (tiles[i][j] == Tile.PRESENT)
						g.setColor(YELLOW);
					else if (tiles[i][j] == Tile.CORRECT)
						g.setColor(GREEN);
					else
						g.setColor(DARK_GREY);
					g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
					g.setColor(Color.WHITE);
				}
				String letter = Character.toString(guesses[i][j]);
				FontMetrics fm = g.getFontMetrics();
				Rectangle2D bounds = g.getFont().createGlyphVector(fm.getFontRenderContext(), letter).getVisualBounds();
				int letterX = x + (TILE_SIZE - fm.charWidth(guesses[i][j])) / 2;
				int letterY = y + (TILE_SIZE + (int) bounds.getHeight()) / 2;
				g.drawString(letter, letterX, letterY);
			}
	}
	
	public void keyTyped(KeyEvent e) {
		char c = Character.toUpperCase(e.getKeyChar());
		if (currRow < NUM_GUESSES && currCol < WORD_LENGTH && c >= 'A' && c <= 'Z') {
			guesses[currRow][currCol] = c;
			currCol++;
			repaint();
		}
	}
	
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (currRow < NUM_GUESSES && currCol == WORD_LENGTH) {
				updateTiles();
				updatePossibilities();
				currRow++;
				currCol = 0;
				repaint();
			}
			else if (gameOver || currRow == NUM_GUESSES)
				reset();
		}
		else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && currCol > 0) {
			currCol--;
			guesses[currRow][currCol] = 0;
			repaint();
		}
	}
	
	public void keyReleased(KeyEvent e) {
	}
	
	public void reset() {
		guesses = new char[NUM_GUESSES][WORD_LENGTH];
		tiles = new Tile[NUM_GUESSES][WORD_LENGTH];
		
		currRow = currCol = 0;
		
		possibilities = new LinkedList<>();
		try {
			BufferedReader in = new BufferedReader(new FileReader("five letter words.txt"));
			String line;
			while ((line = in.readLine()) != null)
				possibilities.add(line);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		goalWord = possibilities.get((int) (Math.random() * possibilities.size()));
//		goalWord = "movie";
		goalWord = goalWord.toUpperCase();
		
		possibilitiesArea.setText(null);
		
		repaint();
	}
	
	public void updateTiles() {
		int[] freq = new int[26];
		for (int i = 0; i < goalWord.length(); i++)
			freq[goalWord.charAt(i) - 'A']++;
		
		for (int i = 0; i < WORD_LENGTH; i++)
			if (guesses[currRow][i] == goalWord.charAt(i)) {
				tiles[currRow][i] = Tile.CORRECT;
				freq[guesses[currRow][i] - 'A']--;
			}
		
		for (int i = 0; i < WORD_LENGTH; i++)
			if (tiles[currRow][i] != Tile.CORRECT){
				if (freq[guesses[currRow][i] - 'A'] == 0)
					tiles[currRow][i] = Tile.ABSENT;
				else {
					tiles[currRow][i] = Tile.PRESENT;
					freq[guesses[currRow][i] - 'A']--;
				}
			}
	}
	
	public void updatePossibilities() {
		int[] guessFreq = new int[26];
		for (int i = 0; i < WORD_LENGTH; i++)
			if (tiles[currRow][i] != Tile.ABSENT)
				guessFreq[guesses[currRow][i] - 'A']++;
		for (Iterator<String> it = possibilities.iterator(); it.hasNext();) {
			String word = it.next();
			int[] freq = new int[26];
			for (int i = 0; i < word.length(); i++)
				freq[word.charAt(i) - 'A']++;
			boolean isPossible = true;
			for (int i = 0; i < 26; i++)
				if (guessFreq[i] > freq[i]) {
					isPossible = false;
					break;
				}
			if (!isPossible) {
				it.remove();
				continue;
			}
			for (int i = 0; i < WORD_LENGTH; i++) {
				if (tiles[currRow][i] == Tile.CORRECT) {
					if (guesses[currRow][i] == word.charAt(i))
						freq[guesses[currRow][i] - 'A']--;
					else {
						isPossible = false;
						break;
					}
				}
				else if (tiles[currRow][i] == Tile.PRESENT) {
					if (guesses[currRow][i] != word.charAt(i))
						freq[guesses[currRow][i] - 'A']--;
					else {
						isPossible = false;
						break;
					}
				}
			}
			if (!isPossible) {
				it.remove();
				continue;
			}
			for (int i = 0; i < WORD_LENGTH; i++) {
				if (tiles[currRow][i] == Tile.ABSENT && freq[guesses[currRow][i] - 'A'] > 0) {
					isPossible = false;
					break;
				}
			}
			if (!isPossible)
				it.remove();	
		}
		possibilitiesArea.setText(null);
		for (String word : possibilities)
			possibilitiesArea.append(word + '\n');
		possibilitiesArea.setCaretPosition(0);
		
		boolean same = true;
		for (int i = 0; i < WORD_LENGTH; i++)
			if (guesses[currRow][i] != goalWord.charAt(i)) {
				same = false;
				break;
			}
		gameOver = same;
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Wordle");
		BestWord panel = new BestWord();
		frame.add(panel);
		frame.addKeyListener(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
