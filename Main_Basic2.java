import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

public class Main_Basic2 {
	
	
		
	public static void main(String[] args) throws Exception {
		
		int fileNumber = 2;
		String type = "heuristic";
		int[] RequestDataSetNumbers = {10, 13, 16, 19, 22, 25, 28, 31, 34, 37, 40, 43, 46, 49};
//		int[] RequestDataSetNumbers = {22, 25, 28, 31, 34, 37, 40, 43, 46, 49};
		
		for(int numVehicles = 10; numVehicles < 11; numVehicles ++) {
			for (int r : RequestDataSetNumbers) {
				int t = 12;
				int w = 15;
				if((r == 10 || r == 19 || r == 28 || r == 37 || r == 46) && numVehicles < 6) {	
					for(t = 8; t < 17; t +=4) {
						for(w = 10; w < 21 ; w+=5) {
							if(!((t == 8 || t == 16) && (w == 10 || w == 20))) {
								long startTime = System.nanoTime();
								
								String datafile = fileNumber + "D" + r + "R" + numVehicles + "V" + t + "T" + w + "W.txt";

								// Writing result to files
								File file = new File (fileNumber + "D" + r + "R" + numVehicles + "V" +t+ "T" + w+ "W_results_"+type+".txt");
								FileWriter resultWriter = new FileWriter(fileNumber+"D"+numVehicles+"V_all_results_"+type+".txt", true);
								FileWriter resultWriter2 = new FileWriter(fileNumber+"D"+numVehicles+"V_all_results2_"+type+".txt", true);
								
								if (!file.exists()) {
									try { file.createNewFile(); 
									} catch (IOException e) {
										e.printStackTrace();
									}
								}

								// Initializing sets 
								PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));	
								PrintWriter fw = new PrintWriter(resultWriter);
								PrintWriter cw = new PrintWriter(resultWriter2);
								Vector<Node> pickupNodes = new Vector<Node>();
								Vector<Node> deliveryNodes = new Vector<Node>();
								Vector<Node> startDepots = new Vector<Node>();
								Vector<Vehicle> vehicles = new Vector<Vehicle>();
								Vector<Double> dualVisitedPickupsCon = new Vector<Double>();  
								Vector<Double> dualOneVisitCon = new Vector<Double>();
								InstanceData inputdata = new InstanceData(datafile);
								
							
								// Reading the input file
								InputReader.inputReader(datafile, inputdata, pickupNodes, deliveryNodes, startDepots, vehicles, fw, cw);
								
								// Writing the name of the current data instance to file
								fw.print(datafile + ";");
								fw.print(pickupNodes.size() + ";");
								fw.print(vehicles.size() + ";");

								// Calling the solver 
								GurobiInterface_Heuristic solver = new GurobiInterface_Heuristic(inputdata, deliveryNodes,  pickupNodes, vehicles, dualVisitedPickupsCon, dualOneVisitCon, pw, fw);
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
				else if((r == 10 || r == 19 || r == 28 || r == 37 || r == 46) && numVehicles >= 6) {
					long startTime = System.nanoTime();
					
					
					String datafile = fileNumber +"D" + r + "R" + numVehicles +"V12T15W.txt";

					// Writing result to files
					File file = new File (fileNumber + "D" + r + "R" + numVehicles +"V12T15W_results_"+type+".txt");
					FileWriter resultWriter = new FileWriter(fileNumber+"D"+numVehicles+"V_all_results_"+type+".txt", true);
					FileWriter resultWriter2 = new FileWriter(fileNumber+"D"+numVehicles+"V_all_results2_"+type+".txt", true);
				
					if (!file.exists()) {
						try { file.createNewFile(); 
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					// Initializing sets 
					PrintWriter pw = new PrintWriter(file);	
					PrintWriter fw = new PrintWriter(resultWriter);
					PrintWriter cw = new PrintWriter(resultWriter2);
					Vector<Node> pickupNodes = new Vector<Node>();
					Vector<Node> deliveryNodes = new Vector<Node>();
					Vector<Node> startDepots = new Vector<Node>();
					Vector<Vehicle> vehicles = new Vector<Vehicle>();
					Vector<Double> dualVisitedPickupsCon = new Vector<Double>();  
					Vector<Double> dualOneVisitCon = new Vector<Double>();
					InstanceData inputdata = new InstanceData(datafile);
					
					
					// Reading the input file
					InputReader.inputReader(datafile, inputdata, pickupNodes, deliveryNodes, startDepots, vehicles, fw, cw);
					
					// Writing the name of the current data instance to file
					fw.print(datafile + ";");
					fw.print(pickupNodes.size() + ";");
					fw.print(vehicles.size() + ";");

					// Calling the solver 
					GurobiInterface_Heuristic solver = new GurobiInterface_Heuristic(inputdata, deliveryNodes,  pickupNodes, vehicles, dualVisitedPickupsCon, dualOneVisitCon, pw, fw);
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
				else if((r != 10 || r != 19 || r != 28 || r != 37 || r != 46) && numVehicles < 6) {
					long startTime = System.nanoTime();
					
					
					String datafile = fileNumber +"D" + r + "R" + numVehicles +"V12T15W.txt";

					// Writing result to files
					File file = new File (fileNumber + "D" + r + "R" + numVehicles +"V12T15W_results_"+type+".txt");
					FileWriter resultWriter = new FileWriter(fileNumber+"D"+numVehicles+"V_all_results_"+type+".txt", true);
					FileWriter resultWriter2 = new FileWriter(fileNumber+"D"+numVehicles+"V_all_results2_"+type+".txt", true);
				
					if (!file.exists()) {
						try { file.createNewFile(); 
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					// Initializing sets 
					PrintWriter pw = new PrintWriter(file);	
					PrintWriter fw = new PrintWriter(resultWriter);
					PrintWriter cw = new PrintWriter(resultWriter2);
					Vector<Node> pickupNodes = new Vector<Node>();
					Vector<Node> deliveryNodes = new Vector<Node>();
					Vector<Node> startDepots = new Vector<Node>();
					Vector<Vehicle> vehicles = new Vector<Vehicle>();
					Vector<Double> dualVisitedPickupsCon = new Vector<Double>();  
					Vector<Double> dualOneVisitCon = new Vector<Double>();
					InstanceData inputdata = new InstanceData(datafile);
					
					
					// Reading the input file
					InputReader.inputReader(datafile, inputdata, pickupNodes, deliveryNodes, startDepots, vehicles, fw, cw);
					
					// Writing the name of the current data instance to file
					fw.print(datafile + ";");
					fw.print(pickupNodes.size() + ";");
					fw.print(vehicles.size() + ";");

					// Calling the solver 
					GurobiInterface_Heuristic solver = new GurobiInterface_Heuristic(inputdata, deliveryNodes,  pickupNodes, vehicles, dualVisitedPickupsCon, dualOneVisitCon, pw, fw);
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
}
