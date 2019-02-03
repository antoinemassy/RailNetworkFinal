import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

public class RailNetwork {

	public static void main(String[] args) throws IOException {

		try (InputStream is = args.length == 0 ? System.in : new FileInputStream(new File(args[0]))) {

			try (InputStream is2 = args.length == 1 ? System.in : new FileInputStream(new File(args[1]))) {

				if (args.length > 2) {

					if (args[2].equals("--quiet")) {

						Game game = new Game(is, is2);
						Network<Node, Integer> network = game.getSolutionNetwork();
						System.out.println("The max flow value of the network is "
								+ network.getMaxFlow() + ".\n");
						System.out.println("The flow distribution accross edges is : \n");

						for (int j = 0; j < network.getEdges().size(); j++) {
							System.out.println(network
									.getNameOrNullByVertex(network.getSource(j))
									+ " -> "
									+ network
											.getNameOrNullByVertex(network.getDest(j))
									+ "		capacity : " + network.getWeight(j) + "		flow : "
									+ network.getFlow(j));
						}

						HashSet<Integer> edgesToCut = network.getOptimalCut();
						System.out.print("\nHere are the edges to obtain minimum cut : ");
						for (int i : edgesToCut) {
							System.out.print(network.getNameOrNullByVertex(network.getSource(i)) + " -> "
									+ network.getNameOrNullByVertex(network.getDest(i)) + "  ");
						}
					}
				}

				else {
					UserInterface gui = new UserInterface(new Game(is, is2));
					gui.runUI();
				}
			}
		}
	}
}
