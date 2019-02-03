import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JOptionPane;

public class UserInterface {
	private Network<Node, Integer> railNetwork;
	private HashSet<Integer> edgesRemovedByUser = new HashSet<Integer>();
	private Game game;
	private int historyCursor;
	private double xmax;
	private double ymax;

	public UserInterface(Game game) {
		this.game = game;
		this.railNetwork = game.getSolutionNetwork();
		this.xmax = this.game.getMaxX();
		this.ymax = this.game.getMaxY();
	}

	public void runUI() {
		setUI();
		play();
	}

	private void setUI() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		StdDraw.setCanvasSize((int) (width - (width / 12)), (int) (height - (height / 10)));
		StdDraw.setScale(-.03, 1.03);
		StdDraw.setPenRadius(0.10);
		drawEdges();
		drawNodes();
		writeNames();
		drawOkButton();
		drawForwardButton();
		drawBackwardButton();
		writeCurrentlyRemovedCapacity();
		displayCurrentlyRemovedCapacity(0);
		showInstructions();
	}

	private void play() {
		Boolean bool = false;
		historyCursor = 1;

		while (!bool) {
			double[] xy1 = getXY();
			if (xy1 != null) {
				if (Math.abs(xy1[0] - (0.97)) < 0.04 && Math.abs(xy1[1] - (0.97)) < 0.04) { // Ok Button pressed
					bool = checkUserSelection();
				} else if (Math.abs(xy1[0] - (0.08)) < 0.04 && Math.abs(xy1[1] - (0.03)) < 0.04) { // previous button
																									// pressed
					ArrayList<Integer> temp = game.getCutsHistory();
					if (!temp.isEmpty() && historyCursor <= temp.size()) {
						addOrRemoveEdge(temp.get(temp.size() - historyCursor++));
					}
				} else if (Math.abs(xy1[0] - (0.20)) < 0.04 && Math.abs(xy1[1] - (0.03)) < 0.04) { // next button
																									// pressed
					ArrayList<Integer> temp = game.getCutsHistory();
					if (!temp.isEmpty() && historyCursor > 1) {
						addOrRemoveEdge(temp.get(temp.size() - --historyCursor));
					}
				} else { // A node might have been selected
					bool = tryToSelectEdge(xy1);
				}
				displayCurrentlyRemovedCapacity(getCurrentRemovedCapacity());
			}
		}
		System.exit(0);
	}

	// This method is used to select an edge.
	// It calls the method selectedEdge(Node n1, Node n2) to check if the edge
	// exists or not.
	// Then if the edge exists it will call addOrRemoveEdge(Integer edge) to add or
	// remove the edge from the selected edges (i.e. HashSet<Integer> edgesAdded).
	private boolean tryToSelectEdge(double[] xy1) {
		for (Node n1 : railNetwork.getVertices()) {
			if (Math.abs(xy1[0] - (n1.x / xmax)) < 0.025 && Math.abs(xy1[1] - (1 - n1.y / ymax)) < 0.025) { // first
																											// node has
																											// been
																											// found
				Node na = n1;
				selectedVertex(n1);
				Boolean bool2 = false;
				while (!bool2) { // wait until a second node is clicked
					double[] xy2 = getXY();
					if (xy2 != null) {
						if (Math.abs(xy2[0] - (0.97)) < 0.04 && Math.abs(xy2[1] - (0.97)) < 0.04) { // ok button pressed
							drawVertex(n1, StdDraw.BLUE);
							return checkUserSelection();
						} else if (Math.abs(xy2[0] - (0.08)) < 0.04 && Math.abs(xy2[1] - (0.03)) < 0.04) { // previous
																											// button
																											// pressed
							ArrayList<Integer> temp = game.getCutsHistory();
							if (!temp.isEmpty() && historyCursor <= temp.size()) {
								addOrRemoveEdge(temp.get(temp.size() - historyCursor++));
							}
							drawVertex(n1, StdDraw.BLUE);
							return false;
						} else if (Math.abs(xy2[0] - (0.20)) < 0.04 && Math.abs(xy2[1] - (0.03)) < 0.04) { // next
																											// button
																											// pressed
							ArrayList<Integer> temp = game.getCutsHistory();
							if (!temp.isEmpty() && historyCursor > 1) {
								addOrRemoveEdge(temp.get(temp.size() - --historyCursor));
							}
							drawVertex(n1, StdDraw.BLUE);
							return false;
						} else { // A second node might have been selected
							for (Node n2 : railNetwork.getVertices()) {
								if (Math.abs(xy2[0] - (n2.x / xmax)) < 0.025
										&& Math.abs(xy2[1] - (1-n2.y / ymax)) < 0.025) { // second node found
									Node nb = n2;
									selectedEdge(na, nb);
									return false;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	// Method to return the current cut edges
	public HashSet<Integer> getCut() {
		return edgesRemovedByUser;
	}

	// Method to return the current capacity of selected edges
	public Integer getCurrentRemovedCapacity() {
		int currentRemovedCapacity = 0;
		for (Object i : edgesRemovedByUser.toArray()) {
			currentRemovedCapacity += railNetwork.getWeight((Integer) i);
		}
		return currentRemovedCapacity;
	}

	// Method that returns coordinates of a click
	private double[] getXY() {
		while (true) {
			if (StdDraw.isMousePressed()) {
				double x = StdDraw.mouseX();
				double y = StdDraw.mouseY();
				double[] xy = { x, y };
				StdDraw.pause(100);
				return xy;
			}
			StdDraw.pause(10);
		}
	}

	// Method used to check if the edge selected by two nodes exists or not : if it
	// exists add or remove it in the edgesRemovedByUser hashset. Else we try to
	// select an edge with the second node selected by the user.
	private void selectedEdge(Node n1, Node n2) {
		Integer edge = railNetwork.getEdgeFromVertices(n1, n2);
		if (railNetwork.getEdges().contains(edge)) {
			if (historyCursor != 1) {
				ArrayList<Integer> temp = game.getCutsHistory();
				temp.subList(temp.size() - --historyCursor, temp.size()).clear();
				historyCursor = 1;
			}
			addOrRemoveEdge(edge);
			game.addUserCut(edge);
		} else {
			drawVertex(n1, StdDraw.BLUE);
			tryToSelectEdge(new double[] { n2.x / xmax, 1-n2.y / ymax });
		}
	}

	private void selectedVertex(Node node) {
		drawVertex(node, StdDraw.GREEN);
	}

	// Method used to add or remove an edge from the selected edges (i.e.
	// HashSet<Integer> edgesAdded)
	private void addOrRemoveEdge(Integer edge) {
		if (!edgesRemovedByUser.contains(edge)) {
			edgesRemovedByUser.add(edge);
			drawEdge(edge, StdDraw.RED);
		} else {
			edgesRemovedByUser.remove(edge);
			drawEdge(edge, StdDraw.CYAN);
		}
		drawNodes();
		writeNames();
	}

	private boolean checkUserSelection() {
		if (game.verifyCut(edgesRemovedByUser) == true) {
			JOptionPane.showMessageDialog(null, "GGWP : you found the optimal cut for the network.");
			return true;
		} else {
			if (!railNetwork.areConnected(edgesRemovedByUser)) {
				JOptionPane.showMessageDialog(null,
						"You have successfully cut the the source from the destination but your cut isn't optimal.\n\nHint : the optimal cut removes a total capacity of "
								+ railNetwork.getMaxFlow() + " in the network.");
			} else {
				JOptionPane.showMessageDialog(null,
						"The source and the destination are still linked ! come on, you can do better than that.");
			}
			return false;
		}
	}

	// Method used to write the names for each Node (Vertex)
	private void writeNames() {
		StdDraw.setPenColor(StdDraw.BLACK);
		Font font = new Font("Arial", Font.BOLD, 15);
		StdDraw.setFont(font);
		for (String name : railNetwork.getNames()) {
			Node n = railNetwork.getVertexByName(name);
			StdDraw.text((n.x) / xmax, (1-n.y / ymax) + 0.017, name);
		}
	}

	private void drawEdges() {
		for (int edge = 0; edge < railNetwork.getEdges().size(); edge++) {
			drawEdge(edge, StdDraw.CYAN);
		}
	}

	private void drawNodes() {
		for (Node n : railNetwork.getVertices()) {
			if (railNetwork.getNameOrNullByVertex(n).equals("Source")
					|| railNetwork.getNameOrNullByVertex(n).equals("Sink")) {
				drawVertex(n, StdDraw.MAGENTA);
			} else {
				drawVertex(n, StdDraw.BLUE);
			}
		}
	}

	private void showInstructions() {
		JOptionPane.showMessageDialog(null,
				"In this simple game, you will play the role of Hitler. Exciting right ? Your goal is to bombard some parts of the soviet rail network to isolate Stalingrad from Moscow.\n "
						+ "However, you want to spend as little effort as possible for this mission. As a result, the sum of the capacities of the rails that you remove must be minimal.\n\n"
						+ "- pick a source and a destination to remove a rail ; do the same to add it back.\n"
						+ "- current total capacity removed is displayed at the bottom right of the screen.\n"
						+ "- use previous and next buttons to navigate through your decisions.\n"
						+ "- click OK to check if your current selection is the best.\n\n" + "Good luck !");
	}

	private void drawOkButton() {
		StdDraw.setPenColor(StdDraw.GREEN);
		StdDraw.square(0.97, 0.97, 0.02);
		StdDraw.setPenColor(StdDraw.BLACK);
		StdDraw.text(0.97, 0.97, "OK");
	}

	private void drawBackwardButton() {
		StdDraw.setPenColor(StdDraw.GREEN);
		StdDraw.square(0.08, 0.03, 0.02);
		StdDraw.setPenColor(StdDraw.BLACK);
		StdDraw.text(0.08, 0.03, "Previous");
	}

	private void drawForwardButton() {
		StdDraw.setPenColor(StdDraw.GREEN);
		StdDraw.square(0.20, 0.03, 0.02);
		StdDraw.setPenColor(StdDraw.BLACK);
		StdDraw.text(0.20, 0.03, "Next");
	}

	private void writeCurrentlyRemovedCapacity() {
		StdDraw.setPenColor(StdDraw.BLACK);
		StdDraw.text(0.88, 0.03, "currently removed capacity :");
	}

	private void displayCurrentlyRemovedCapacity(Integer i) {
		StdDraw.setPenRadius(0.10);
		StdDraw.setPenColor(StdDraw.RED);
		StdDraw.square(0.98, 0.03, 0.02);
		StdDraw.setPenColor(StdDraw.BLACK);
		StdDraw.text(0.98, 0.03, i.toString());
	}

	private void drawVertex(Node n, Color color) {
		StdDraw.setPenColor(color);
		StdDraw.setPenRadius(0.03);
		StdDraw.point((n.x) / xmax, 1- n.y / ymax);
	}

	// Method used to draw an edge represented by an arrow with its weight
	// (capacity) on it.
	private void drawEdge(Integer edge, Color color) {
		StdDraw.setPenRadius(0.007);
		StdDraw.setPenColor(color);
		StdDraw.line(railNetwork.getSource(edge).x / xmax, 1-railNetwork.getSource(edge).y / ymax,
				railNetwork.getDest(edge).x / xmax, 1-railNetwork.getDest(edge).y / ymax);
		double mx = (railNetwork.getSource(edge).x / xmax + (3 * railNetwork.getDest(edge).x / xmax)) / 4;
		double my = (1-railNetwork.getSource(edge).y / ymax + (3 * (1-railNetwork.getDest(edge).y / ymax))) / 4;
		double nx = (railNetwork.getSource(edge).x / xmax + (railNetwork.getDest(edge).x / xmax)) / 2;
		double ny = (1-railNetwork.getSource(edge).y / ymax + (1-railNetwork.getDest(edge).y / ymax)) / 2;
		double angle = Math.atan2((double) 1-railNetwork.getSource(edge).y / ymax - my,
				(double) railNetwork.getSource(edge).x / xmax - mx);
		double arrowMagnitude = 0.01;
		StdDraw.setPenRadius(0.005);
		double gx = mx + arrowMagnitude * Math.cos(angle + Math.PI / 6);
		double gy = my + arrowMagnitude * Math.sin(angle + Math.PI / 6);
		StdDraw.line(mx, my, gx, gy);
		double hx = mx + arrowMagnitude * Math.cos(angle - Math.PI / 6);
		double hy = my + arrowMagnitude * Math.sin(angle - Math.PI / 6);
		StdDraw.line(mx, my, hx, hy);
		StdDraw.setPenColor(StdDraw.BLACK);
		Font font = new Font("Arial", Font.ITALIC, 15);
		StdDraw.setFont(font);
		String weight = String.valueOf(railNetwork.getWeight(edge));
		if (weight.equals("999999")) {
			weight = "inf";
		}
		StdDraw.setPenRadius(0.007);
		StdDraw.text((nx), (ny), weight);
	}

}
