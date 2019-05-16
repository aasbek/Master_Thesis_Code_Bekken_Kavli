import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;


public class Main {

	public static void main(String[] args) throws Exception {
		
		int fileNumber = 2;
		int numVehicles = 6;
		int[] RequestDataSetNumbers = {10, 13, 16, 19, 22, 25, 28, 31, 34, 37, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90};
		
		for (int r : RequestDataSetNumbers) {
			int t = 8;
			int w = 20;
			if(r == 10 || r == 22 || r == 34 || r == 45 || r == 60 || r == 75 || r == 90) {
				for(t = 6; t < 11; t +=2) {
					for(w = 10; w < 31 ; w+=10) {
						if(!((t == 6 || t == 10) && (w == 10 || w == 30))) {
							long startTime = System.nanoTime();
							
							String datafile = fileNumber + "D" + r + "R" + numVehicles + "V" + t + "T" + w + "W.txt";

							// Writing result to files
							File file = new File (fileNumber + "D" + r + "R" + numVehicles + "V" +t+ "T" + w+ "W_results.txt");
							FileWriter resultWriter = new FileWriter(fileNumber+"D"+numVehicles+"V_all_results.txt", true);
							
							if (!file.exists()) {
								try { file.createNewFile(); 
								} catch (IOException e) {
									e.printStackTrace();
								}
							}

							// Initializing sets 
							PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));	
							PrintWriter fw = new PrintWriter(resultWriter);
							Vector<Node> pickupNodes = new Vector<Node>();
							Vector<Node> deliveryNodes = new Vector<Node>();
							Vector<Node> startDepots = new Vector<Node>();
							Vector<Vehicle> vehicles = new Vector<Vehicle>();
							Vector<Double> dualVisitedPickupsCon = new Vector<Double>();  
							Vector<Double> dualOneVisitCon = new Vector<Double>();
							InstanceData inputdata = new InstanceData(datafile);
							
						
							// Reading the input file
							InputReader.inputReader(datafile, inputdata, pickupNodes, deliveryNodes, startDepots, vehicles, fw);
							
							// Writing the name of the current data instance to file
							fw.print(datafile + ";");
							fw.print(pickupNodes.size() + ";");
							fw.print(vehicles.size() + ";");

							// Calling the solver 
							GurobiInterface solver = new GurobiInterface(inputdata, deliveryNodes,  pickupNodes, vehicles, dualVisitedPickupsCon, dualOneVisitCon, pw, fw);
							solver.columnGenerator();
						
							long endTime = System.nanoTime();
							
							System.out.println("Took "+(endTime - startTime)/1000000 + " milli seconds"); 
							pw.append ("\n Took "+(endTime - startTime)/1000000 + " milli seconds");
							
							solver.printResults();
							fw.print((endTime - startTime)/1000000);
							fw.println(" ");
							pw.flush();
							pw.close();
							fw.close();
						}
						
					}
				}
			}
			else {
				long startTime = System.nanoTime();
				
				
				String datafile = fileNumber +"D" + r + "R" + numVehicles +"V8T20W.txt";

				// Writing result to files
				File file = new File (fileNumber + "D" + r + "R" + numVehicles +"V8T20W_results.txt");
				FileWriter resultWriter = new FileWriter(fileNumber+"D"+numVehicles+"V_all_results.txt", true);
			
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
				
				
				// Reading the input file
				InputReader.inputReader(datafile, inputdata, pickupNodes, deliveryNodes, startDepots, vehicles, fw);
				
				// Writing the name of the current data instance to file
				fw.print(datafile + ";");
				fw.print(pickupNodes.size() + ";");
				fw.print(vehicles.size() + ";");

				// Calling the solver 
				GurobiInterface solver = new GurobiInterface(inputdata, deliveryNodes,  pickupNodes, vehicles, dualVisitedPickupsCon, dualOneVisitCon, pw, fw);
				solver.columnGenerator();
			
				long endTime = System.nanoTime();
				
				System.out.println("Took "+(endTime - startTime)/1000000 + " milli seconds"); 
				pw.append("\n Took "+(endTime - startTime)/1000000 + " milli seconds");
				
				solver.printResults();
				fw.print((endTime - startTime)/1000000);
				fw.println(" ");
				pw.flush();
				pw.close();
				fw.close();
			}
		
		}
	}
}
