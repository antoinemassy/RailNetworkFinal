import java.util.Collection;
import java.util.HashSet;

public interface Network<Vertex, Edge> {
	

	public void addVertex(Vertex v);
	
	public void nameVertex(String name, Vertex v);

	public void setWeight(Edge e, Integer weight);
	
	public void addEdge(Edge e, Vertex[] v0v1);

	public void setFlow(Edge e, Integer flow);
	
	
	
	public Vertex getVertexByName(String name);

	public String getNameOrNullByVertex(Vertex v);

	public Collection<String> getNames();
	
	public Integer getWeight(Edge e);

	public Collection<Vertex> getVertices();

	public Collection<Edge> getEdges();

	public Vertex getSource(Edge e);

	public Vertex getDest(Edge e);

	public Edge getEdgeFromVertices(Vertex v0, Vertex v1);

	public Integer getFlow(Edge e);

	public Integer getResidualCapacity(Edge e);
	

	
	public boolean areConnected(HashSet<Edge> removedEdges);

	public void computeMaxFlow();

	public boolean findAugmentingPath();

	public void computeMinCut();

	public int getMaxFlow();
	
	public HashSet<Edge> getOptimalCut();


}
