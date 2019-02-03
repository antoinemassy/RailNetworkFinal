import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class AdjacencyNetwork<Vertex, Edge> implements Network<Vertex, Edge> {

	private Set<Edge> edges = new HashSet<Edge>();
	private Map<String, Vertex> nameToVertex = new HashMap<String, Vertex>();
	private Map<Edge, Integer> edgeToWeight = new HashMap<Edge, Integer>();
	private Map<Edge, Integer> edgeToFlow = new HashMap<Edge, Integer>();
	private Map<Edge, Vertex[]> edgeToVertices = new HashMap<Edge, Vertex[]>();
	private Map<Vertex, HashSet<Edge>> vertexToForwardEdges = new HashMap<Vertex, HashSet<Edge>>();
	private Map<Vertex, HashSet<Edge>> vertexToBackwardEdges = new HashMap<Vertex, HashSet<Edge>>();
	private HashSet<Edge> optimalCut = new HashSet<Edge>();

	public void addVertex(Vertex v) {
		if (!vertexToForwardEdges.containsKey(v) && !vertexToBackwardEdges.containsKey(v)) {
			vertexToForwardEdges.put(v, new HashSet<Edge>());
			vertexToBackwardEdges.put(v, new HashSet<Edge>());
		}
	}

	public void addEdge(Edge e, Vertex[] v0v1) {
		edges.add(e);
		setFlow(e, 0);
		edgeToVertices.put(e, v0v1);
		vertexToForwardEdges.get(v0v1[0]).add(e);
		vertexToBackwardEdges.get(v0v1[1]).add(e);
	}

	public Collection<Vertex> getVertices() {
		return Collections.unmodifiableCollection(vertexToForwardEdges.keySet());
	}

	public Collection<Edge> getEdges() {
		return Collections.unmodifiableCollection(edgeToVertices.keySet());
	}

	public Vertex getSource(Edge e) {
		return edgeToVertices.get(e)[0];
	}

	public Vertex getDest(Edge e) {
		return edgeToVertices.get(e)[1];
	}

	@Override
	public Edge getEdgeFromVertices(Vertex src, Vertex dest) {
		for (Edge e : vertexToForwardEdges.get(src)) {
			if (getDest(e) == dest) {
				return e;
			}
		}
		return null;
	}

	@Override
	public void nameVertex(String name, Vertex v) {
		nameToVertex.put(name, v);
	}

	@Override
	public Vertex getVertexByName(String name) {
		return nameToVertex.get(name);
	}

	@Override
	public String getNameOrNullByVertex(Vertex v) {
		for (Map.Entry<String, Vertex> e : nameToVertex.entrySet()) {
			if (e.getValue().equals(v)) {
				return e.getKey();
			}
		}
		return null;
	}

	@Override
	public Collection<String> getNames() {
		return Collections.unmodifiableCollection(nameToVertex.keySet());
	}

	@Override
	public void setWeight(Edge e, Integer weight) {
		edgeToWeight.put(e, weight);
	}

	@Override
	public Integer getWeight(Edge e) {
		return edgeToWeight.get(e);
	}

	@Override
	public void setFlow(Edge e, Integer flow) {
		edgeToFlow.put(e, flow);
	}

	@Override
	public Integer getFlow(Edge e) {
		return edgeToFlow.get(e);
	}

	public Integer getResidualCapacity(Edge e) {
		return (edgeToWeight.get(e) - edgeToFlow.get(e));
	}

	public HashSet<Edge> getOptimalCut() {
		return optimalCut;
	}

	public boolean areConnected(HashSet<Edge> removedEdges) {
		Vertex src = getVertexByName("Source");
		Vertex dest = getVertexByName("Sink");
		if (src.equals(dest)) {
			return true;
		}

		LinkedList<Vertex> toVisit = new LinkedList<Vertex>();
		toVisit.add(src);
		Set<Vertex> visited = new HashSet<Vertex>();
		Vertex visiting = null;

		while (!toVisit.isEmpty()) {
			visiting = toVisit.pop();
			visited.add(visiting);
			for (Edge temp : vertexToForwardEdges.get(visiting)) {
				if (removedEdges.contains(temp)) {
					continue;
				}
				Vertex currentDest = getDest(temp);
				if (currentDest.equals(dest)) {
					return true;
				}
				if (!visited.contains(currentDest)) {
					toVisit.add(currentDest);
				}
			}
		}
		return false;
	}

	public void computeMaxFlow() {
		boolean temp;
		do {
			temp = findAugmentingPath();
		} while (temp != false);
	}

	// this following algorithm is the Ford-Fulkerson algorithm which consists of
	// finding an augmenting path through the network (including the backward edges
	// in our research). An augmenting path means that we can only consider edges
	// that still have remaining capacity before adding them : remaining capacity
	// equals capacity - flow for a forward edge and flow for a backward edge. Once
	// we found a path we add the bottleneck capacity on forward edges and remove
	// it on backward edges and then start the algorithm again until no augmenting
	// path can be found.

	public boolean findAugmentingPath() {

		LinkedList<Vertex> toVisit = new LinkedList<Vertex>();
		HashSet<Vertex> visited = new HashSet<Vertex>();
		toVisit.add(getVertexByName("Source"));
		Vertex visiting = null;
		LinkedHashMap<Vertex, Object[]> link = new LinkedHashMap<Vertex, Object[]>(); // this map links a visited vertex
																						// to an array containing the
																						// previous vertex in the path,
																						// 1 or -1 according to edge
																						// direction (backward or
																						// forward), the actual edge and
																						// the remaining capacity of the
																						// edge (i.e capacity - flow for
																						// a forward edge and flow for a
																						// backward edge). We later use
																						// this information to compute
																						// the path and update flow
																						// faster and have cleaner code

		while (toVisit.isEmpty() != true) {

			visiting = toVisit.pop();

			// The two maps vertexToForwardEdges and vertexToBackwardEdges allow us to
			// separate the cases in two different for loops. This make the algorithm faster
			// since we don't need to check at each iteration of the for loop if the edge is
			// a backward or forward edge. Also, the number of iterations remains the same
			// even though we have two for loops because we treat the same number of edges,
			// just in a more ordered manner.

			for (Edge e : vertexToForwardEdges.get(visiting)) {

				Vertex v = getDest(e);
				int residual = getResidualCapacity(e);
				if (residual > 0 && !visited.contains(v)) {
					link.put(v, new Object[] { visiting, 1, e, residual });

					if (v.equals(getVertexByName("Sink"))) {
						return computePathAndUpdateFlow(link, v);
					}
					toVisit.add(v);
				}
			}

			for (Edge e : vertexToBackwardEdges.get(visiting)) {

				Vertex v = getSource(e);
				int residual = getFlow(e);

				if (residual > 0 && !visited.contains(v)) {
					link.put(v, new Object[] { visiting, -1, e, residual });

					if (v.equals(getVertexByName("Sink"))) {
						return computePathAndUpdateFlow(link, v);
					}
					toVisit.add(v);
				}
			}
			visited.add(visiting);
		}
		return false;
	}

	@SuppressWarnings("unchecked") // this is just to remove the Java warnings about the Object[] we previously
									// built in the link LinkedHashMap : everytime we access an object in the array
									// and use it, we get a warning that this object might be invalid as a
									// parameter. That's why we need to cast the types (Vertex) (Edge) (int) before
									// using the array objects

	public boolean computePathAndUpdateFlow(LinkedHashMap<Vertex, Object[]> link, Vertex destination) {

		LinkedList<Vertex> path = new LinkedList<Vertex>();
		path.add(destination);
		LinkedHashMap<Edge, Integer> returnPath = new LinkedHashMap<Edge, Integer>();
		int bottleNeck = Integer.MAX_VALUE;
		int tempBottleNeck;

		while (path.get(0) != getVertexByName("Source")) {

			Object[] currentLink = link.get(path.get(0));
			path.addFirst((Vertex) currentLink[0]);
			returnPath.put((Edge) currentLink[2], (int) currentLink[1]); // we compute the path by adding the edges as
																			// well as 1 or -1 according to forward or
																			// backward.
			tempBottleNeck = (int) currentLink[3];

			if (tempBottleNeck < bottleNeck) { // we can compute the bottleneck at the same as we build the path because
												// we transmitted the information in the fourth argument of the link
												// array
				bottleNeck = tempBottleNeck;
			}
		}

		Set<Edge> edgesPath = returnPath.keySet();
		for (Edge e1 : edgesPath) {
			setFlow(e1, (getFlow(e1) + bottleNeck * returnPath.get(e1))); // updating the flow is now a one-liner
																				// thanks
																				// to the previous transmission of the
																				// information through the whole process
		}
		return true;
	}

	// this is the minimumCut algorithm which is based on the results of the
	// maximumFlow algorithm. In the "maxed network" (i.e the flow is maximum and
	// optimal thanks to maxflow algorithm) we search for a path and mark all the
	// vertices that we can reach while searching. Once there are no more vertex to
	// visit, the minimum cut edges are those that go from a marked vertex towards a
	// non marked vertex thanks to a forward edge.

	public void computeMinCut() {
		LinkedList<Vertex> toVisit = new LinkedList<Vertex>();
		HashSet<Vertex> visited = new HashSet<Vertex>();
		toVisit.add(getVertexByName("Source"));
		HashSet<Vertex> marked = new HashSet<Vertex>();
		marked.add(getVertexByName("Source"));

		while (toVisit.isEmpty() != true) {

			Vertex visiting = toVisit.pop();

			for (Edge e : vertexToForwardEdges.get(visiting)) {
				Vertex v = getDest(e);
				if (getResidualCapacity(e) > 0 && !visited.contains(v)) {
					marked.add(v);
					toVisit.add(v);
				}
			}

			for (Edge e : vertexToBackwardEdges.get(visiting)) {
				Vertex v = getSource(e);
				if (getFlow(e) > 0 && !visited.contains(v)) {
					marked.add(v);
					toVisit.add(v);
				}
			}
			visited.add(visiting);
		}

		for (Vertex v : marked) {
			for (Edge e : vertexToForwardEdges.get(v)) {
				if (!marked.contains(getDest(e))) {
					optimalCut.add(e);
				}
			}
		}
	}

	public int getMaxFlow() {
		int maxFlow = 0;
		for (Edge e : vertexToForwardEdges.get(getVertexByName("Source"))) {
			maxFlow += getFlow(e);
		}
		return maxFlow;
	}
}
