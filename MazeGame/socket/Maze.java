package MazeGame.socket;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

public class Maze extends JPanel implements Serializable, TreasureFoundEventListener {

	class Cell extends JPanel {
		private int x;
		private int y;
		private boolean hasTreasure;
		private Player player;
		private TreasureFoundEventListener listener;

		public Cell() {

		}

		public Cell(int x, int y, boolean hasTreasure) {
			this.x = x;
			this.y = y;
			this.hasTreasure = hasTreasure;
			this.player = null;
		}

		public void setTreasureFoundEventListener(TreasureFoundEventListener listener) {
			this.listener = listener;
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(50, 50);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			// Draw Text
			if (this.hasTreasure) {
				g.drawString("*", 10, 20);
			}
			if (this.player != null) {
				g.drawString(this.player.getName(), 10, 20);
			}
		}

		public int getRow() {
			return x;
		}

		public int getCol() {
			return y;
		}

		public boolean getHasTreasure() {
			return this.hasTreasure;
		}

		public boolean buryTreasure() {
			if (getHasTreasure()) {
				return false;
			}
			this.hasTreasure = true;
			return true;
		}

		public boolean getHasPlayer() {
			return this.player != null;
		}

		public boolean getHasPlayer(Player player) {
			return this.player != null && this.player.getName().equals(player.getName());
		}

		// returns true: player meets treasure
		public boolean enter(Player player) throws Exception {
			synchronized (Cell.class) {
				if (this.player != null) {
					// the cell has been occupied by a player, you cannot enter
					throw new Exception("The cell has been occupied!");
				}
				this.player = player;
				if (this.hasTreasure) {
					this.hasTreasure = false;
					player.findTreasure();
					if (listener != null) {
						listener.onTreasureFoundEvent();
					}
					return true;
				}
				return false;
			}
		}

		public boolean leaveAndEnter(Cell newCell, Player player) throws Exception {
			synchronized (Cell.class) {
				if (newCell.player != null) {
					// the cell has been occupied by a player, you cannot enter
					throw new Exception("The cell has been occupied!");
				}
				if (this.player == null) {
					throw new Exception("The cell is not occupied with any player!");
				}
				newCell.player = player;
				this.player = null;
				if (newCell.hasTreasure) {
					newCell.hasTreasure = false;
					player.findTreasure();
					if (listener != null) {
						listener.onTreasureFoundEvent();
					}
					return true;
				}
				return false;
			}
		}

		public String toString() {
			String result = x + "," + y + "," + this.hasTreasure;
			if (this.player != null) {
				result += "," + player.toStr();
			}
			return result;
		}

		public void fromString(String input) {
			String[] cellInfo = input.split(",");
			this.x = Integer.parseInt(cellInfo[0]);
			this.y = Integer.parseInt(cellInfo[1]);
			this.hasTreasure = Boolean.parseBoolean(cellInfo[2]);
			if (cellInfo.length == 4) {
				this.player = new Player(cellInfo[3]);
			} else {
				this.player = null;
			}
		}
	}

	class PlayersScorePanel extends JPanel {
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			// Draw Text
			int count = 0;
			for (int i = 0; i < cells.length; i++) {
				for (int j = 0; j < cells[0].length; j++) {
					if (cells[i][j].getHasPlayer()) {
						g.drawString(cells[i][j].player.getName() + ": " + cells[i][j].player.getScore(), 0,
								10 + 10 * count);
						count++;
					}
				}
			}
		}
	}

	protected Cell[][] cells;

	public Maze() {
		setLayout(new BorderLayout());
	}

	public Maze(int mazeSize) {
		this();

		JPanel mazePanel = new JPanel();
		mazePanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		this.cells = new Cell[mazeSize][mazeSize];
		for (int row = 0; row < mazeSize; row++) {
			for (int col = 0; col < mazeSize; col++) {
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.gridx = row;
				gbc.gridy = col;

				this.cells[row][col] = new Cell(row, col, false);
				this.cells[row][col].setTreasureFoundEventListener(this);
				Border border = null;
				if (row < mazeSize - 1) {
					if (col < mazeSize - 1) {
						border = new MatteBorder(1, 1, 0, 0, Color.BLACK);
					} else {
						border = new MatteBorder(1, 1, 1, 0, Color.BLACK);
					}
				} else {
					if (col < mazeSize - 1) {
						border = new MatteBorder(1, 1, 0, 1, Color.BLACK);
					} else {
						border = new MatteBorder(1, 1, 1, 1, Color.BLACK);
					}
				}
				this.cells[row][col].setBorder(border);
				mazePanel.add(this.cells[row][col], gbc);
			}
		}
		PlayersScorePanel scorePanel = new PlayersScorePanel();
		scorePanel.setPreferredSize(new Dimension(100, 500));
		add(mazePanel, BorderLayout.CENTER);
		add(scorePanel, BorderLayout.WEST);
	}

	// first server will init with this
	public Maze(int mazeSize, int treasureCount) throws Exception {
		this(mazeSize);
		if (treasureCount > mazeSize * mazeSize) {
			throw new Exception("Treasure size is too big!");
		}
		buryTreasures(treasureCount);
	}

	public int getMazeSize() {
		return this.cells.length;
	}

	private void buryTreasures(int k) {
		// need better logic here to exclude cells with treasure
		for (int i = 0; i < k; i++) {
			buryTreasure();
		}
	}

	private void buryTreasure() {
		Random rand = new Random();
		boolean success = false;
		do {
			int x = rand.nextInt(getMazeSize());
			int y = rand.nextInt(getMazeSize());
			success = this.cells[x][y].buryTreasure();
		} while (!success);
	}

	public boolean JoinGame(Player player) {
		synchronized (this.cells) {
			try {
				Cell firstAvailableCell = getRandomUnOccupiedCell();
				boolean treasureFound = firstAvailableCell.enter(player);
				System.out.println("Player " + player.getName() + " has joined the maze.");
				if (treasureFound) {
					// generate another treasure in maze
					buryTreasure();
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	public boolean ExitGame(Player player) {
		synchronized (this.cells) {
			try {
				Cell cell = getCellWithPlayer(player);
				cell.player = null;
				System.out.println("Player " + player.getName() + " exit.");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	
	private Cell getRandomUnOccupiedCell() throws Exception {
		synchronized (this.cells) {
			int width = getMazeSize();
			Random r = new Random();
			int w = 0, h = 0;
			do {
				w = r.nextInt(width);
				h = r.nextInt(width);
			} while (this.cells[w][h].getHasPlayer());
			return this.cells[w][h];
		}
	}

	private Cell getCellWithPlayer(Player player) throws Exception {
		for (int i = 0; i < getMazeSize(); i++) {
			for (int j = 0; j < getMazeSize(); j++) {
				if (this.cells[i][j].getHasPlayer(player)) {
					return this.cells[i][j];
				}
			}
		}
		throw new Exception("The player does not exist in the maze!");
	}
	
	

	public boolean MoveWest(Player player) {
		System.out.println("Player " + player.getName() + " moving west");
		try {
			Cell cell = getCellWithPlayer(player);
			int x = cell.getRow();
			int y = cell.getCol();
			if (x > 0) {
				cell.leaveAndEnter(this.cells[x - 1][y], player);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean MoveSouth(Player player) {
		System.out.println("Player " + player.getName() + " moving south");
		try {
			Cell cell = getCellWithPlayer(player);
			int x = cell.getRow();
			int y = cell.getCol();
			if (y < getMazeSize() - 1) {
				cell.leaveAndEnter(this.cells[x][y + 1], player);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean MoveEast(Player player) {
		System.out.println("Player " + player.getName() + " moving east");
		try {
			Cell cell = getCellWithPlayer(player);
			int x = cell.getRow();
			int y = cell.getCol();
			if (x < getMazeSize() - 1) {
				cell.leaveAndEnter(this.cells[x + 1][y], player);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean MoveNorth(Player player) {
		System.out.println("Player " + player.getName() + " moving north");
		try {
			Cell cell = getCellWithPlayer(player);
			int x = cell.getRow();
			int y = cell.getCol();
			if (y > 0) {
				cell.leaveAndEnter(this.cells[x][y - 1], player);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void onTreasureFoundEvent() {
		buryTreasure();
		this.repaint();
	}

	public void fromString(String input) throws Exception {
		input = input.substring(3);// remove MZ; tag
		String[] info = input.split(";");
		if (info.length != this.cells.length * this.cells[0].length) {
			throw new Exception("input string is not correct.");
		}
		for (int i = 0; i < this.cells.length; i++) {
			for (int j = 0; j < this.cells[i].length; j++) {
				this.cells[i][j].fromString(info[j + i * this.cells.length]);
			}
		}
		this.repaint();
	}

	public String toString() {
		String result = "";
		for (int i = 0; i < this.cells.length; i++) {
			for (int j = 0; j < this.cells[i].length; j++) {
				result += this.cells[i][j].toString() + ";";
			}
		}
		result = result.substring(0, result.length() - 1);
		return "MZ;" + result;
	}

}
