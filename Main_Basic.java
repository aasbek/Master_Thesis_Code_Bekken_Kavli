import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

public class Main_Basic {


	public static void main(String[] args) throws Exception {
		long startTime = System.nanoTime();
		String type = "_cost_allocation_LTL";
	//	int[] HeuristicTestDataSetNumbers = {22, 25, 28, 31, 34, 37};
	//	int[] MoselTestDataSetNumbers = {1,2,3,4,5,6,7,9,10};
			
	//	for (int r : MoselTestDataSetNumbers) {
			//String datafile =  "1D" + r + "R3V12T15W.txt";
			String datafile = "3D34R3V12T15W.txt";
	
			// Writing result to files
			//File file = new File ( "1D"+r+"R3V12T15W_results.txt");
			//FileWriter resultWriter = new FileWriter("1D3V_all_results_heuristic_DOM8.txt", true);
			File file = new File ("3D10R3V12T15W"+type+".txt");
			FileWriter resultWriter = new FileWriter("3D3V_all_results"+type+".txt", true);
			FileWriter resultWriter2 = new FileWriter("3D3V_all_results2"+type+".txt", true);
			
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
			GurobiInterface solver = new GurobiInterface(inputdata, deliveryNodes,  pickupNodes, vehicles, dualVisitedPickupsCon, dualOneVisitCon, pw, fw, cw);
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
			cw.close();
			
		}
	}
//}


