package MazeGame.rmi;

import java.io.Serializable;
import java.util.Random;

public class Maze implements IMaze, Serializable {

	class Cell {
		private int x;
		private int y;
		private boolean hasTreasure;
		private Player player;

		public Cell() {

		}

		public Cell(int x, int y, boolean hasTreasure) {
			this.x = x;
			this.y = y;
			this.hasTreasure = hasTreasure;
			this.player = null;
		}

		public int getX() {
			return x;
		}

		public int getY() {
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
		
		public boolean getHasPlayer(Player player){
			return this.player != null && this.player.getName() == player.getName();
		}

		// returns true: player meets treasure
		public boolean enter(Player player) throws CellOccupiedException {
			synchronized (Cell.class) {
				if (this.player != null) {
					// the cell has been occupied by a player, you cannot enter
					throw new CellOccupiedException("The cell has been occupied!");
				}
				this.player = player;
				if (this.hasTreasure) {
					this.hasTreasure = false;
					player.findTreasure();
					return true;
				}
				return false;
			}
		}

		public void leave() throws Exception {
			if (this.player != null) {
				this.player = null;
			}
			throw new Exception("The cell is not occupied with any player!");
		}

	}

	private static Cell[][] cells;

	public Maze() {

	}

	public Maze(int mazeSize, int treasureCount) throws Exception {
		if (treasureCount > mazeSize * mazeSize) {
			throw new Exception("Treasure size is too big!");
		}
		this.cells = new Cell[mazeSize][mazeSize];
		for(int i = 0; i < mazeSize; i++){
			for(int j = 0; j < mazeSize; j++){
				this.cells[i][j] = new Cell(i,j,false);
			}
		}
		buryTreasures(treasureCount);
	}

	public int getWidth() {
		return this.cells.length;
	}

	public int getHeight() {
		return this.cells[0].length;
	}

	private void buryTreasures(int k) {
		// need better logic here to exclude cells with treasure
		for (int i = 0; i < k; i++) {
			buryTreasure();
		}
	}
	
	private void buryTreasure(){
		Random rand = new Random();
		boolean success = false;
		do {
			int x = rand.nextInt(getWidth());
			int y = rand.nextInt(getHeight());
			success = this.cells[x][y].buryTreasure();
		} while (!success);
	}

	@Override
	public boolean JoinGame(Player player) {
		synchronized (this.cells) {
			try {
				Cell firstAvailableCell = getFirstUnOccupiedCell();
				boolean treasureFound = firstAvailableCell.enter(player);
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
			int width = getWidth();
			int height = getHeight();
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


	private Cell getCellWithPlayer(Player player) throws Exception{
		for(int i = 0; i < getWidth(); i++){
			for(int j = 0; j < getHeight(); j++){
				if(this.cells[i][j].getHasPlayer(player)){
					return this.cells[i][j];
				}
			}
		}
		throw new Exception("The player does not exist in the maze!");
	}
	@Override
	public boolean MoveWest(Player player) {
		try {
			Cell cell = getCellWithPlayer(player);
			int x = cell.getX();
			int y = cell.getY();
			if(x > 0){
				cell.leave();
				this.cells[x-1][y].enter(player);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean MoveSouth(Player player) {
		try {
			Cell cell = getCellWithPlayer(player);
			int x = cell.getX();
			int y = cell.getY();
			if(y < getHeight()-1){
				cell.leave();
				this.cells[x][y+1].enter(player);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean MoveEast(Player player) {
		try {
			Cell cell = getCellWithPlayer(player);
			int x = cell.getX();
			int y = cell.getY();
			if(x < getWidth()-1){
				cell.leave();
				this.cells[x+1][y].enter(player);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean MoveNorth(Player player) {
		try {
			Cell cell = getCellWithPlayer(player);
			int x = cell.getX();
			int y = cell.getY();
			if(y > 0){
				cell.leave();
				this.cells[x][y-1].enter(player);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
