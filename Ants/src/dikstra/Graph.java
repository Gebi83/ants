package dikstra;

import java.util.List;


public class Graph {
  private final List<Vertex> vertexes;
  private final List<Edge> edges;

  public Graph(List<Vertex> vertexes, List<Edge> edges) {
    this.vertexes = vertexes;
    this.edges = edges;
  }

  public List<Vertex> getVertexes() {
    return vertexes;
  }

  public List<Edge> getEdges() {
    return edges;
  }

  public void setOccupied(int row, int column) {
	  for (Edge edge : edges){
		  Vertex source = edge.getSource();
		  Vertex dest = edge.getDestination();
		  if (source.getRow() == row && source.getCol() == column){
			  edge.setOccupied(true);
			  source.setOccupied(true);
		  }else if (dest.getRow() == row && dest.getCol() == column){
			  edge.setOccupied(true);
			  dest.setOccupied(true);
		  }
	  }
  }

  @Override
  public String toString(){
	  StringBuffer buf = new StringBuffer();
	  int row = 0;
	  for (Vertex ver : vertexes){
		  if (ver.getRow() != row){
			  buf.append("\n");
		  }
		  row = ver.getRow();
		  buf.append(ver.isOccupied() ? "X" : "0");
		  buf.append(" ");
	  }
	  
	  buf.append("\n-----------\n");
	  buf.append("#edge: "+this.edges.size());
	  return buf.toString();
  }


}