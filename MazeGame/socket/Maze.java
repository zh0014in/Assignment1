package MazeGame.socket;

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
				g.drawString("X", 10, 20);
			}
			if (this.player != null) {
				g.drawString("P", 10, 20);
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

		public void leave() throws Exception {
			if (this.player != null) {
				this.player = null;
				return;
			}
			throw new Exception("The cell is not occupied with any player!");
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
			}else{
				this.player = null;
			}
		}
	}

	private Cell[][] cells;

	public Maze() {
		setLayout(new GridBagLayout());
	}

	public Maze(int mazeSize) {
		this();

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
				add(this.cells[row][col], gbc);
			}
		}
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
				Cell firstAvailableCell = getFirstUnOccupiedCell();
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

	private Cell getFirstUnOccupiedCell() throws Exception {
		synchronized (this.cells) {
			int width = getMazeSize();
			int height = getMazeSize();
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if (!this.cells[i][j].getHasPlayer()) {
						return this.cells[i][j];
					}
				}
			}
			throw new Exception("The maze is full!");
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
		try {
			Cell cell = getCellWithPlayer(player);
			int x = cell.getRow();
			int y = cell.getCol();
			if (x > 0) {
				cell.leave();
				this.cells[x - 1][y].enter(player);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean MoveSouth(Player player) {
		try {
			Cell cell = getCellWithPlayer(player);
			int x = cell.getRow();
			int y = cell.getCol();
			if (y < getMazeSize() - 1) {
				cell.leave();
				this.cells[x][y + 1].enter(player);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean MoveEast(Player player) {
		try {
			Cell cell = getCellWithPlayer(player);
			int x = cell.getRow();
			int y = cell.getCol();
			if (x < getMazeSize() - 1) {
				cell.leave();
				this.cells[x + 1][y].enter(player);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean MoveNorth(Player player) {
		try {
			Cell cell = getCellWithPlayer(player);
			int x = cell.getRow();
			int y = cell.getCol();
			if (y > 0) {
				cell.leave();
				this.cells[x][y - 1].enter(player);
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
		System.out.println("client maze update with maze msg: " + input);
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
