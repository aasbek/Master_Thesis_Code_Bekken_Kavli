import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;


public class Main {

	public static void main(String[] args) throws Exception {
		
		for (int r = 10 ; r < 50 ;  r+= 2) {
			int t = 8;
			int w = 10;
			if(r >= 24 && r<= 34) {
				for(t = 8; t < 13; t +=4) {
					for(w = 10; w < 31 ; w+=10) {

						long startTime = System.nanoTime();
						
						
						String datafile = "11D" + r + "R3V" + t + "T" + w + "W.txt";

						// Writing result to files
						File file = new File ("11D" + r + "R3V" +t+ "T" + w+ "W_results.txt");
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
			}
			else {
				long startTime = System.nanoTime();
				
				
				String datafile = "11D" + r + "R3V8T20W.txt";

				// Writing result to files
				File file = new File ("11D" + r + "R3V8T20W_results.txt");
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
	}
}
