
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Starter bot implementation.
 */
public class GebiBot extends Bot {

	/**
	 * all new locations for my ants for one turn.
	 */
	private Map<Tile, Tile> orders = new HashMap<Tile, Tile>();

	/**
	 * locations that haven�t been "seen".
	 */
	private Set<Tile> unseenTiles;//TODO other technique to watch this.
	
	private Set<Tile> enemyHills = new HashSet<Tile>();
	
	private List<Ant> myAntList = new ArrayList<Ant>();

	/**
	 * Main method executed by the game engine for starting the bot.
	 * 
	 * @param args command line arguments
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public static void main(String[] args) throws IOException {
		new GebiBot().readSystemInput();
	}

	/**
	 * For every ant check every direction in fixed order (N, E, S, W) and move it if the tile is
	 * passable.
	 */
	@Override
	public void doTurn() {

		Ants ants = getAnts();
		
		createAntList(ants);
		
		orders.clear();

		// prevent stepping on own hill
		for (Tile myHill : ants.getMyHills()) {
			orders.put(myHill, null);
		}


		Map<Tile, Tile> foodTargets = new HashMap<Tile, Tile>();

		// add all locations to unseen tiles set, run once
		if (unseenTiles == null) {
			unseenTiles = new HashSet<Tile>();
			for (int row = 0; row < ants.getRows(); row++) {
				for (int col = 0; col < ants.getCols(); col++) {
					unseenTiles.add(new Tile(row, col));
				}
			}
		}
		// remove any tiles that can be seen, run each turn
		for (Iterator<Tile> locIter = unseenTiles.iterator(); locIter.hasNext(); ) {
			Tile next = locIter.next();
			if (ants.isVisible(next)) {
				locIter.remove();
			}
		}

		// find close food
		List<Route> foodRoutes = new ArrayList<Route>();
		TreeSet<Tile> sortedFood = new TreeSet<Tile>(ants.getFoodTiles());
		TreeSet<Tile> sortedAnts = new TreeSet<Tile>(ants.getMyAnts());
		for (Tile foodLoc : sortedFood) {
			for (Tile antLoc : sortedAnts) {
				int distance = ants.getDistance(antLoc, foodLoc);
				Route route = new Route(antLoc, foodLoc, distance);
				foodRoutes.add(route);
			}
		}
		Collections.sort(foodRoutes);
		for (Route route : foodRoutes) {
			if (!foodTargets.containsKey(route.getEnd())
					&& !foodTargets.containsValue(route.getStart())
					&& doMoveLocation(route.getStart(), route.getEnd())) {
				foodTargets.put(route.getEnd(), route.getStart());
			}
		}
		
		// add new hills to set
        for (Tile enemyHill : ants.getEnemyHills()) {
            if (!enemyHills.contains(enemyHill)) {
                enemyHills.add(enemyHill);
            }
        }
        // attack hills
        List<Route> hillRoutes = new ArrayList<Route>();
        for (Tile hillLoc : enemyHills) {
            for (Tile antLoc : sortedAnts) {
                if (!orders.containsValue(antLoc)) {
                    int distance = ants.getDistance(antLoc, hillLoc);
                    Route route = new Route(antLoc, hillLoc, distance);
                    hillRoutes.add(route);
                }
            }
        }
        Collections.sort(hillRoutes);
        for (Route route : hillRoutes) {
            doMoveLocation(route.getStart(), route.getEnd());
        }

		// explore unseen areas
		for (Tile antLoc : sortedAnts) {
			if (!orders.containsValue(antLoc)) {
				List<Route> unseenRoutes = new ArrayList<Route>();
//TODO better technique here. at the moment all distances to all unseen locations are calculated
				for (Tile unseenLoc : unseenTiles) {
					int distance = ants.getDistance(antLoc, unseenLoc);
					Route route = new Route(antLoc, unseenLoc, distance);
					unseenRoutes.add(route);
				}
				Collections.sort(unseenRoutes);
				for (Route route : unseenRoutes) {
					if (doMoveLocation(route.getStart(), route.getEnd())) {
						break;
					}
				}
			}
		}


		// unblock hills
		for (Tile myHill : ants.getMyHills()) {
			if (ants.getMyAnts().contains(myHill) && !orders.containsValue(myHill)) {
				for (Aim direction : Aim.values()) {
					if (doMoveDirection(myHill, direction)) {
						break;
					}
				}
			}
		}
	}

	/**
	 * creates or modifies the list with all my ants.
	 * checks if new ants are alive.
	 * checks if old ants are still alive.
	 * deletes dead ants or adds new ants to myAntList.
	 * @param ants
	 */
	private void createAntList(Ants ants) {
		
		List<Tile> oldAnts = getAntPositions();
		
		TreeSet<Tile> sortedAnts = new TreeSet<Tile>(ants.getMyAnts());
		
		
	}

	/**
	 * 
	 * @return the position of all ants in myAntList.
	 */
	private List<Tile> getAntPositions() {
		
		List<Tile> antPositions = new ArrayList<Tile>();//TODO
		return null;
	}

	/**
	 * checks if a new location is unoccupied and if no other own ant wants to go to this location.
	 * @param antLoc
	 * @param direction
	 * @return
	 */
	private boolean doMoveDirection(Tile antLoc, Aim direction) {
		Ants ants = getAnts();
		// Track all moves, prevent collisions
		Tile newLoc = ants.getTile(antLoc, direction);
		if (ants.getIlk(newLoc).isUnoccupied() && !orders.containsKey(newLoc)) {
			ants.issueOrder(antLoc, direction);
			orders.put(newLoc, antLoc);
			return true;
		} else {
			return false;
		}
	}

	private boolean doMoveLocation(Tile antLoc, Tile destLoc) {
		Ants ants = getAnts();
		// Track targets to prevent 2 ants to the same location
		List<Aim> directions = ants.getDirections(antLoc, destLoc);
		for (Aim direction : directions) {
			if (doMoveDirection(antLoc, direction)) {
				return true;
			}
		}
		return false;
	}
}