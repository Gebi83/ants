package ants;
import java.util.Arrays;
import java.util.LinkedList;

import dikstra.Dikstra;
import dikstra.Vertex;

/**
 * Represents a route from one tile to another.
 */
public class Route implements Comparable<Route> {
    private final Tile start;

    private final Tile end;

    private LinkedList<Vertex> tiles;

    private int currentStep = 1;
    private int[] currentPosition;


    public Route(Tile start, Tile e, Dikstra dikstra) {
        this.start = start;
        currentPosition = new int[]{start.getRow(), start.getCol()};
        this.end = e;
        
        GebiBot.getLogger().debug("start: " + start + " | ziel: " +end);
        

        //Calculate Tiles
        int posStart = (start.getRow() * dikstra.getColumn()) + start.getCol();
        int posEnd = (end.getRow() * dikstra.getColumn()) + end.getCol();

        dikstra.execute(dikstra.getGraph().getVertexes().get(posStart));
        tiles = dikstra.getPath(dikstra.getGraph().getVertexes().get(posEnd));
        
    }

    public Aim doNextStep(){
    	if (tiles == null || currentStep >= tiles.size()) {
			return null;
		}

    	while(true){
    		Aim aim = null;
    		Vertex ver = tiles.get(currentStep - 1);
        	currentStep++;

        	if (ver.getRow() > currentPosition[0]) {
        		aim = Aim.SOUTH;
    		}
        	if (ver.getRow() < currentPosition[0]) {
    			aim = Aim.NORTH;
    		}
        	if (ver.getCol() > currentPosition[1]) {
    			aim = Aim.EAST;
    		}
        	if (ver.getCol() < currentPosition[1]){
				aim = Aim.WEST;
        	}
        	if (aim != null){
        		currentPosition[0] = ver.getRow();
        		currentPosition[1] = ver.getCol();

        		return aim;
        	}

        	//Next and current are equal
        	//continue...
    	}
    }

    public boolean hasNextStep(){
    	return currentStep < tiles.size();
    }

    public int getNumberOfRemainingSteps(){
    	int tmp = tiles.size() - currentStep - 1;
    	if (tmp < 0) {
			return 0;
		}
    	return tmp;
    }

    public Tile getStart() {
        return start;
    }

    public Tile getEnd() {
        return end;
    }


    @Override
    public int compareTo(Route route) {
    	LinkedList<Vertex> otherTiles = route.getTiles();
    	
    	if (otherTiles== null && tiles == null) {
    		return 0;
    	} else if (otherTiles== null && tiles != null) {
    		return 1;
    	} else if (otherTiles != null && tiles == null) {
    		return -1;
    	} else if (otherTiles.size() != tiles.size()) {
    		if (otherTiles.size() > tiles.size()) {
    			return -1;
    		} else {
    			return 1;
    		}
    		
    	} else {
    		return 0;
    	}
    	
    }

    @Override
    public int hashCode() {
        return start.hashCode() * Ants.MAX_MAP_SIZE * Ants.MAX_MAP_SIZE + end.hashCode();
    }
    
    public LinkedList<Vertex> getTiles() {
    	return tiles;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Route) {
            Route route = (Route)o;
            result = start.equals(route.start) && end.equals(route.end);
        }
        return result;
    }
    
    public String toString(){
    	if (tiles == null) {
    		return "tiles == null";
    	}
    	return Arrays.toString(tiles.toArray());
    }
}