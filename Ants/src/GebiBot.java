
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Starter bot implementation.
 */
public class GebiBot extends Bot {
	
	private static Logger logger;

	/**
	 * all new locations for my ants for one turn.
	 */
	private Map<Tile, Tile> orders = new HashMap<Tile, Tile>();
	
	/**
	 * list with positions where ants should not go (own hills, walls, ...)
	 */
	private List<Tile> forbiddenPositions = new ArrayList<Tile>();

	/**
	 * locations that haven´t been "seen".
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
		
		PropertyConfigurator.configure(".\\src\\log4j.properties");
		logger = Logger.getLogger(GebiBot.class.getCanonicalName());
		logger.error("Test");
		
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
		letAntsMove();
		
		orders.clear();

		// prevent stepping on own hill
		for (Tile myHill : ants.getMyHills()) {
			orders.put(myHill, null);//TODO remove old version
			forbiddenPositions.add(myHill);
		}

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
//			for (Tile antLoc : sortedAnts) {
//				int distance = ants.getDistance(antLoc, foodLoc);
//				Route route = new Route(antLoc, foodLoc, distance);
//				foodRoutes.add(route);
//			}
			
			List<Ant> antsWithNoDestination = getAntsWithoutDestination();
			for (Iterator<Ant> it = antsWithNoDestination.iterator(); it.hasNext();) {
				Ant ant = it.next();
					Tile antLoc = ant.getPosition();
					int distance = ants.getDistance(antLoc, foodLoc);
					Route route = new Route(antLoc, foodLoc, distance);
					foodRoutes.add(route);
				
				
			}
		}
		Collections.sort(foodRoutes);
		
		Map<Tile, Ant> foodTargets = new HashMap<Tile, Ant>();
		for (Route route : foodRoutes) {
			if (!foodTargets.containsKey(route.getEnd())
					&& !foodTargets.containsValue(route.getStart())
					&& doMoveLocation(route.getStart(), route.getEnd())) {
				
				getAntByLocation(route.getStart()).setDestination(route.getEnd());
				foodTargets.put(route.getEnd(), getAntByLocation(route.getStart()));
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
        	
        	List<Ant> antsWithNoDestination = getAntsWithoutDestination();
			for (Iterator<Ant> it = antsWithNoDestination.iterator(); it.hasNext();) {
				Ant ant = it.next();
				
                
                    int distance = ants.getDistance(ant.getPosition(), hillLoc);
                    Route route = new Route(ant.getPosition(), hillLoc, distance);
                    hillRoutes.add(route);
                
            }
        }
        Collections.sort(hillRoutes);
        for (Route route : hillRoutes) {
        	if (doMoveLocation(route.getStart(), route.getEnd())) {
        		getAntByLocation(route.getStart()).setDestination(route.getEnd());
        	}
        }

		// explore unseen areas
        List<Ant> antsWithNoDestination = getAntsWithoutDestination();
		for (Iterator<Ant> it = antsWithNoDestination.iterator(); it.hasNext();) {
			Ant ant = it.next();
				List<Route> unseenRoutes = new ArrayList<Route>();
//TODO better technique here. at the moment all distances to all unseen locations are calculated
				
				for (Tile unseenLoc : unseenTiles) {
					int distance = ants.getDistance(ant.getPosition(), unseenLoc);
					Route route = new Route(ant.getPosition(), unseenLoc, distance);
					unseenRoutes.add(route);
				}
				Collections.sort(unseenRoutes);
				for (Route route : unseenRoutes) {
					if (doMoveLocation(route.getStart(), route.getEnd())) {
						getAntByLocation(route.getStart()).setDestination(route.getEnd());
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
	 * moves all ants with destination one step. If moving to destination is not possible, 
	 * remove destination.
	 */
	private void letAntsMove() {
		
		for (Iterator<Ant> it = myAntList.iterator(); it.hasNext(); ) {
			Ant ant = it.next();
			if (ant.getDestination() != null){
				
				if (!doMoveLocation(ant.getPosition(), ant.getDestination())) {
					ant.setDestination(null);
				}
			}
					
		}
		
	}

	private List<Ant> getAntsWithoutDestination() {
		
		List<Ant> antsWithoutDestination = new ArrayList<Ant>();
		
		for (Iterator<Ant> it = myAntList.iterator(); it.hasNext();) {
			Ant ant = it.next();
			if (ant.getDestination() == null) {
				antsWithoutDestination.add(ant);
			}
		}
		
		return antsWithoutDestination;
	}

	/**
	 * creates or modifies the list with all my ants.
	 * checks if new ants are alive.
	 * checks if old ants are still alive.
	 * deletes dead ants or adds new ants to myAntList.
	 * @param ants
	 */
	private void createAntList(Ants ants) {
		
//		List<Tile> antDestinations = getAntDestinations();
		TreeSet<Tile> sortedAnts = new TreeSet<Tile>(ants.getMyAnts());
	
		for (Iterator<Ant> it = myAntList.iterator();it.hasNext();) {
			
			
			Ant ant = it.next();
			
			//ant did not move in last turn
			if (ant.getNextPosition() == null
					&& !sortedAnts.contains(ant.getPosition())) {
				it.remove();//ant is dead (no ant at its destination)
				continue;
			}
			
			//check if ant is still alive (walked to its destination)
			if (!sortedAnts.contains(ant.getNextPosition())) {
				it.remove();//ant is dead (no ant at its destination)
			} else {
				ant.setPosition(ant.getNextPosition());//now ant position = ant nextPosition
				sortedAnts.remove(ant.getPosition());//remove ant position from list
			}
		}
		
		//add new ants to myAntList
		for (Iterator<Tile> it = sortedAnts.iterator();it.hasNext();){
			Tile newAntPosition = it.next();
			myAntList.add(new Ant(newAntPosition));
		}
		
		
			
	}


	/**
	 * 
	 * @return the positions of all ants in myAntList.
	 */
	private List<Tile> getAntPositions() {
		
		List<Tile> antPositions = new ArrayList<Tile>();
		
		for (Ant ant : myAntList) {
			antPositions.add(ant.getPosition());
		}
		return antPositions;
	}
	
	/**
	 * 
	 * @return the destinations of all ants in myAntList.
	 */
	private List<Tile> getAntDestinations() {
		
		List<Tile> antDestinations = new ArrayList<Tile>();
		
		for (Ant ant : myAntList) {
			antDestinations.add(ant.getDestination());
		}
		return antDestinations;
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
		if (ants.getIlk(newLoc).isUnoccupied() && !orders.containsKey(newLoc)
				&& !forbiddenPositions.contains(newLoc)) {
			ants.issueOrder(antLoc, direction);
			logger.debug("orders-keys: " + orders.keySet() + " || newLoc: " + newLoc);
			orders.put(newLoc, antLoc);//TODO remove old version
			
			//set new next position for ant
			Ant ant = getAntByLocation(antLoc);
			if (ant != null) {
				ant.setNextPosition(newLoc);
			}
			
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param antLoc
	 * @return ant at given position. null if no ant at position.
	 */
	private Ant getAntByLocation(Tile antLoc) {
		Ant ant = null;
		for (Iterator<Ant> it = myAntList.iterator(); it.hasNext();) {
			Ant a = it.next();
			if (a.getPosition().equals(antLoc)) {
				ant = a;
				break;
			}
		}
		
		if (ant == null) {
			logger.debug("no ant found for position");
		}
		return ant;
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
