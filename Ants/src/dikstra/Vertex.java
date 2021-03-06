package dikstra;

public class Vertex {
	  final private String id;
	  final private int row;
	  final private int col;
	  private boolean isOccupied;


	  public Vertex(int row, int col) {
	    this.row = row;
	    this.col = col;
	    id = "Node_" + row + "_" + col;
	  }

	  public int getRow(){
		  return row;
	  }
	  public int getCol(){
		  return col;
	  }

	  public void setOccupied(boolean isOccupied){
		  this.isOccupied = isOccupied;
	  }

	  public boolean isOccupied(){
		  return isOccupied;
	  }
	  public String getId() {
	    return id;
	  }

	  public String getName() {
	    return id;
	  }

	  @Override
	  public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((id == null) ? 0 : id.hashCode());
	    return result;
	  }

	  @Override
	  public boolean equals(Object obj) {
	    if (this == obj) {
			return true;
		}
	    if (obj == null) {
			return false;
		}
	    if (getClass() != obj.getClass()) {
			return false;
		}
	    Vertex other = (Vertex) obj;
	    if (id == null) {
	      if (other.id != null) {
			return false;
		}
	    } else if (!id.equals(other.id)) {
			return false;
		}
	    return true;
	  }

	  @Override
	  public String toString() {
	    return id;
	  }

	}