package ants;

/**
 * represents one ant with its position, next step (next position) and its destination.
 * @author Gebi
 *
 */
public class Ant {
	/**
	 * current position
	 */
	private Tile position;
	
	/**
	 * next step
	 */
	private Tile nextPosition;
	
	/**
	 * destination
	 */
	private Tile destination;

	private Route route;

	public Ant(Tile position) {
		this.position = position;
	}

	public Tile getDestination() {
		return destination;
	}

	public void setDestination(Tile destination) {
		this.destination = destination;
	}

	public Tile getPosition() {
		return position;
	}

	public void setPosition(Tile position) {
		this.position = position;
	}

	public Tile getNextPosition() {
		return nextPosition;
	}

	public void setNextPosition(Tile nextPosition) {
		this.nextPosition = nextPosition;
	}

	public void setRoute(Route route) {
		this.route = route;	
	}
	
	
	public Route getRoute() {
		return route;
	}

	
}
