import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;


public class Main {

	public static void main(String[] args) throws Exception {
		
		long startTime = System.nanoTime();
		
		String datafile = "11D16R4V8T20W.txt";

		// Writing result to files
		File file = new File ("55P30R4V_java_results.txt");
		FileWriter resultWriter = new FileWriter("All_Results.txt", true);
		
		if (!file.exists()) {
			try { file.createNewFile(); 
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Initializing sets 
		PrintWriter pw = new PrintWriter(file);	
		PrintWriter fw = new PrintWriter(resultWriter);
		Vector<Node> pickupNodes = new Vector<Node>();
		Vector<Node> deliveryNodes = new Vector<Node>();
		Vector<Node> startDepots = new Vector<Node>();
		Vector<Vehicle> vehicles = new Vector<Vehicle>();
		Vector<Double> dualVisitedPickupsCon = new Vector<Double>();  
		Vector<Double> dualOneVisitCon = new Vector<Double>();
		InstanceData inputdata = new InstanceData(datafile);
		
		// Writing the name of the current data instance to file
		fw.print(datafile + ";");
		fw.print(pickupNodes.size() + ";");
		fw.print(vehicles.size() + ";");
		
		// Reading the input file
		InputReader.inputReader(datafile, inputdata, pickupNodes, deliveryNodes, startDepots, vehicles, fw);

		// Calling the solver 
		GurobiInterface solver = new GurobiInterface(inputdata, deliveryNodes,  pickupNodes, vehicles, dualVisitedPickupsCon, dualOneVisitCon, pw, fw);
		solver.columnGenerator();
	
		long endTime = System.nanoTime();
		
		System.out.println("Took "+(endTime - startTime)/1000000 + " milli seconds"); 
		pw.println ("Took "+(endTime - startTime)/1000000 + " milli seconds");
		
		solver.printResults();
		
		fw.println(" ");
		
		pw.close();
		fw.close();
	}
}
