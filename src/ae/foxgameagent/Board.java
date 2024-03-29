package ae.foxgameagent;

import java.util.ArrayList;

import se.miun.chrfin.foxgame.logic.Position;

/**
 * @author Albin Engström
 */
public class Board {

	/**
	 * Holds the positions of the foxes
	 */
	private ArrayList<Position> foxPositions = new ArrayList<Position>();

	/**
	 * Holds the positions of the sheep
	 */
	private ArrayList<Position> sheepPositions = new ArrayList<Position>();

	/**
	 * Holds the Positions that have changed to create the current state
	 */
	private ArrayList<Position> changedPositions = new ArrayList<Position>();

	/**
	 * Constructor
	 */
	public Board() {

		// Set the starting positions of the foxes
		foxPositions.add(new Position(3, 1));
		foxPositions.add(new Position(5, 1));

		// Set the starting positions of the sheep
		for (int y = 4; y <= 5; y++) {
			for (int x = 1; x <= 7; x++) {
				sheepPositions.add(new Position(x, y));
			}
		}
		for (int y = 6; y <= 7; y++) {
			for (int x = 3; x <= 5; x++) {
				sheepPositions.add(new Position(x, y));
			}
		}
	}

	/**
	 * Copy constructor
	 */
	public Board(Board board) {

		// Fill sheepPositions with new Positions with the same values as the
		// ones in board
		for (Position position : board.getSheepPositions()) {
			sheepPositions.add(new Position(position));
		}

		// Fill foxPositions with new Positions with the same values as the ones
		// in board
		for (Position position : board.getFoxPositions()) {
			foxPositions.add(new Position(position));
		}

		// Fill changedPositions with new Positions with the same values as the
		// ones
		// in board
		for (Position position : board.getChangedPositions()) {
			changedPositions.add(new Position(position));
		}
	}

	/**
	 * Check if Board is in a terminal state, if so, who won?
	 * 
	 * @return 1 of
	 */
	public int isTerminal() {

		// If there are no foxes left, the Sheep won, return 1
		if (foxPositions.isEmpty()) {
			return 1;
		}

		// If there aren't enough sheep to fill the pen, the foxes won, return 2
		if (sheepPositions.size() < 9) {
			return 2;
		}

		// Holds if the pen is full
		boolean penFull = true;

		// Check if the pen is full, penFull will be false if it isn't
		for (int x = 3; x <= 5; x++) {
			for (int y = 1; y <= 3; y++) {
				if (isOccupied(new Position(x, y)) == 0) {
					penFull = false;
				}
			}
		}

		// The pen is full, Sheep won, return 1
		if (penFull) {
			return 1;
		}

		// Return 0, state isn't terminal
		return 0;
	}

	/**
	 * Get the utility value of the current state
	 * 
	 * @return The utility value
	 */
	public double getUtility() {

		// Features:
		// h(s) = total distance from top for each sheep
		// f(s) = nr of foxes
		// s(s) = nr of sheep
		// rs(s) = removed sheep
		// rf(s) = removed foxes
		// fp(s) = foxes total proximity to a position
		// sp(s) = sheep total proximity to a position
		// ss(s) = total "safety" of the sheep, Positions with few or no ways to
		// jump over it
		// t(s) = is it terminal
		// sPen(s) = sheep in pen

		// Holds the utility value
		double value = 0;

		int foxProximityX = 4;
		int foxProximityY = 3;

		int sheepProximityX = 4;
		int sheepProximityY = 1;

		// Add sp(s) to to value
		// Iterate through sheepPositions
		for (Position position : getSheepPositions()) {
			if (Math.abs(sheepProximityX - position.getX()) > 2) {
				value += Math.abs(sheepProximityX - position.getX());
			}
			value += Math.pow(1.1, Math.abs(sheepProximityY - position.getY()));
		}

		// Subtract ss(s) from value
		ArrayList<Position> adjacentPositions = new ArrayList<Position>();

		// Iterate through sheepPositions
		for (Position position : getSheepPositions()) {

			// Clear adjecentPositions
			adjacentPositions.clear();

			// Add all adjacent Positions to adjecentPositions,
			// the Positions opposite to each other are added together
			adjacentPositions.add(new Position(position.getX() + 1, position
					.getY()));
			adjacentPositions.add(new Position(position.getX() - 1, position
					.getY()));

			adjacentPositions.add(new Position(position.getX(),
					position.getY() + 1));
			adjacentPositions.add(new Position(position.getX(),
					position.getY() - 1));

			adjacentPositions.add(new Position(position.getX() + 1, position
					.getY() + 1));
			adjacentPositions.add(new Position(position.getX() - 1, position
					.getY() - 1));

			adjacentPositions.add(new Position(position.getX() + 1, position
					.getY() - 1));
			adjacentPositions.add(new Position(position.getX() - 1, position
					.getY() + 1));

			// The value of one safe attack angle
			double valueOfBeingSafe = 0.01;

			// Iterate through adjacentPositions
			for (int i = 0; i < adjacentPositions.size(); i = i + 2) {

				// If both Positions are on the board
				if (isPositionValid(adjacentPositions.get(i).getX(),
						adjacentPositions.get(i).getY())
						&& isPositionValid(adjacentPositions.get(i + 1).getX(),
								adjacentPositions.get(i + 1).getY())) {

					// If one of the positions are occupied by a sheep,
					// angle is safe
					if (isOccupied(adjacentPositions.get(i)) == 1
							|| isOccupied(adjacentPositions.get(i)) == 1) {
						value -= valueOfBeingSafe;
					}

				} else {

					// Both positions are not on the board, angle is safe
					value -= valueOfBeingSafe;
				}
			}
		}

		// Add rs(s) to to value
		value += (20 - getSheepPositions().size()) * 3;

		// Subtract rf(s) to to value
		value -= (2 - getFoxPositions().size()) * 30;

		// Subtract fp(s) to to value
		// Iterate through foxPositions
		for (Position position : getFoxPositions()) {

			if (Math.abs(foxProximityX - position.getX()) > 2) {
				value -= Math.abs(foxProximityX - position.getX()) * 1.1;
			}

			value -= Math.abs(foxProximityY - position.getY());
		}

		// Subtract sPen(s) from value
		for (Position position : getSheepPositions()) {
			if (position.getY() == 1) {
				value -= 2;
			} else if (position.getY() == 2) {
				value -= 1.5;
			} else if (position.getY() == 2) {
				value -= 1;
			}
		}

		// Add or subtract t(s) from value depending on who won
		if (isTerminal() == 1) {
			value -= 1000;
		} else if (isTerminal() == 2) {
			value += 1000;
		}

		// Return value
		return value;
	}

	/**
	 * Check if an x, y position is on the board
	 * 
	 * @param x
	 *            The x value of the position
	 * @param y
	 *            The y value of the position
	 * @return true if the position x, y is on the board, else false
	 */
	public boolean isPositionValid(int x, int y) {

		if (y < 1 || y > 7) {
			return false;
		} else if (y <= 2 || y >= 6) {
			if (x <= 2 || x >= 6) {
				return false;
			}
		} else if (x < 1 || x > 7) {
			return false;
		}
		return true;
	}

	/**
	 * Check if a position is occupied, and by what
	 * 
	 * @param position
	 *            The Position to check
	 * @return 0 if free, 1 if sheep, 2 if fox
	 */
	public int isOccupied(Position position) {

		// Check if something is in position and return an int depending on what
		if (getFoxPositions().contains(position)) {
			return 2;
		} else if (getSheepPositions().contains(position)) {
			return 1;
		}
		return 0;
	}

	/**
	 * Remove a fox from the board
	 * 
	 * @param position
	 *            The Position of the fox
	 */
	public void removeFox(Position position) {

		// Remove it if it exists
		if (foxPositions.contains(position)) {
			foxPositions.remove(position);
		}
	}

	/**
	 * Changes the position of an sheep or fox
	 * 
	 * @param oldPosition
	 *            The position it's currently in
	 * @param newPosition
	 *            The position it shall move to
	 */
	public void changePostition(Position oldPosition, Position newPosition,
			boolean jump) {

		// Find oldPostion in sheepPositions, if it exists, and changes it to
		// match newPosition
		if (sheepPositions.contains(oldPosition)) {
			for (Position position : sheepPositions) {
				if (position.equals(oldPosition)) {
					position.x = newPosition.x;
					position.y = newPosition.y;

					// Add Positions to changedPositions
					changedPositions.add(oldPosition);
					changedPositions.add(newPosition);
				}
			}

			return;
		}

		// Find oldPostion in foxPositions, if it exists, and changes it to
		// match newPosition
		if (foxPositions.contains(oldPosition)) {
			for (Position position : foxPositions) {
				if (position.equals(oldPosition)) {
					position.x = newPosition.x;
					position.y = newPosition.y;

					if (!jump) {
						// Add Positions to changedPositions
						changedPositions.add(oldPosition);
						changedPositions.add(newPosition);
					} else {

						// Get the x and y values of the Position jumped over
						int x = (newPosition.x + oldPosition.x) / 2;
						int y = (newPosition.y + oldPosition.y) / 2;

						// Removes the sheep at that Position
						sheepPositions.remove(new Position(x, y));

						// If changedPositions is empty, add both new and old
						// Positions
						if (changedPositions.isEmpty()) {
							changedPositions.add(oldPosition);
							changedPositions.add(newPosition);
						} else {
							// If it isn't add only newPosition, since the old
							// Position is already added

							changedPositions.add(newPosition);
						}
					}
				}
			}
		}
	}

	/**
	 * Update the board
	 */
	public void updateBoard(Position oldPosition, Position newPosition,
			boolean jump) {

		// Call changePosition and clear changedPositions
		changePostition(new Position(oldPosition), new Position(newPosition),
				jump);
		clearChangedPositions();
	}

	/**
	 * Clear changedPositions
	 */
	public void clearChangedPositions() {
		changedPositions.clear();
	}

	/**
	 * @return the foxPositions
	 */
	public ArrayList<Position> getFoxPositions() {
		return foxPositions;
	}

	/**
	 * @param foxPositions
	 *            the foxPositions to set
	 */
	public void setFoxPositions(ArrayList<Position> foxPositions) {
		this.foxPositions = foxPositions;
	}

	/**
	 * @return the sheepPositions
	 */
	public ArrayList<Position> getSheepPositions() {
		return sheepPositions;
	}

	/**
	 * @param sheepPositions
	 *            the sheepPositions to set
	 */
	public void setSheepPositions(ArrayList<Position> sheepPositions) {
		this.sheepPositions = sheepPositions;
	}

	/**
	 * @return the changedPositions
	 */
	public ArrayList<Position> getChangedPositions() {
		return changedPositions;
	}

	/**
	 * @param changedPositions
	 *            the changedPositions to set
	 */
	public void setChangedPositions(ArrayList<Position> changedPositions) {
		this.changedPositions = changedPositions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		// Return false if obj is null
		if (obj == null) {
			return false;
		}

		// Return false if obj isn't a Position object
		if (getClass() != obj.getClass()) {
			return false;
		}

		// Cast obj to Board
		Board board = (Board) obj;

		// If sheepPositions and foxPositions of board and this Board contains
		// the same things, return true
		if (getFoxPositions().containsAll(board.getFoxPositions())
				&& getSheepPositions().containsAll(board.getSheepPositions())) {
			return true;
		}

		// Return false
		return false;
	}

}
