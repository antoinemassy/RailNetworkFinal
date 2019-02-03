import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

// when an object of this class is created, two networks are created : the solution network runs both maxFlow and minCut algorithms
// at the instantiation of the object and later serves to compare the user input.

public class Game {

	private Network<Node, Integer> solutionNetwork = new AdjacencyNetwork<Node, Integer>();
	private ArrayList<Integer> userCutsHistory;
	private double xmax;
	private double ymax;

	public Game(InputStream is, InputStream is2) throws NumberFormatException, IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String str = null;
		float xmax =0 , ymax =0;
		while ((str = br.readLine()) != null) {
			String[] parts = str.split("\\s+");
			xmax = (Float.parseFloat(parts[1])>xmax) ? Float.parseFloat(parts[1]) : xmax;
			ymax = (Float.parseFloat(parts[2])>ymax) ? Float.parseFloat(parts[2]) : ymax;
			Node v = new Node(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));
			solutionNetwork.addVertex(v); 
			solutionNetwork.nameVertex((String)parts[0], v);
		}
		this.xmax = xmax;
		this.ymax = ymax;
		BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
		String str2 = null;
		int i = 0;
		while ((str2 = br2.readLine()) != null) {
			String[] parts = str2.split("\\s+");
			Node[] list = { solutionNetwork.getVertexByName(parts[0]) , solutionNetwork.getVertexByName(parts[1]) };
			Integer currentWeight = Integer.parseInt(parts[2]);
			solutionNetwork.addEdge(i, list);
			solutionNetwork.setWeight(i, currentWeight);
			i++;
		}

		solutionNetwork.computeMaxFlow();
		solutionNetwork.computeMinCut();
		userCutsHistory = new ArrayList<Integer>();
	}
	
	public double getMaxX() {
		return this.xmax;
	}
	
	public double getMaxY() {
		return this.ymax;
	}

	public void addUserCut(Integer e) {
		userCutsHistory.add(e);
	}

	public boolean verifyCut(HashSet<Integer> userCut) {
		HashSet<Integer> optimalCut = solutionNetwork.getOptimalCut();
		if (userCut.equals(optimalCut)) {
			return true;
		} else {
			return false;
		}
	}

	public Network<Node, Integer> getSolutionNetwork() {
		return solutionNetwork;
	}

	public ArrayList<Integer> getCutsHistory() {
		return userCutsHistory;
	}
}
