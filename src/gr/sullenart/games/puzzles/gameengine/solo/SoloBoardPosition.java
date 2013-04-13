package gr.sullenart.games.puzzles.gameengine.solo;

import java.util.ArrayList;
import java.util.List;

public class SoloBoardPosition {

	private List<List<Integer>> neighbors;

	private List<int []> diagonalNeighbors;

	private int position = 0;

	private int distance = 0;

	private int cornerDistance = 0;

	private int mobility = 0;

	public List<List<Integer>> getNeighbors() {
		return neighbors;
	}

	public List<int []> getDiagonalNeighbors() {
		return diagonalNeighbors;
	}

	public int getPosition() {
		return position;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getCornerDistance() {
		return cornerDistance;
	}

	public void setCornerDistance(int cornerDistance) {
		this.cornerDistance = cornerDistance;
	}

	public int getMobility() {
		return mobility;
	}

	public void setMobility(int mobility) {
		this.mobility = mobility;
	}

	public void incrementMobility() {
		mobility++;
	}

	public SoloBoardPosition(int position) {
		this.position = position;

		neighbors = new ArrayList<List<Integer>>();
		neighbors.add(new ArrayList<Integer>());
		neighbors.add(new ArrayList<Integer>());
		neighbors.add(new ArrayList<Integer>());
		neighbors.add(new ArrayList<Integer>());

		diagonalNeighbors = new ArrayList<int []>();
	}
}
