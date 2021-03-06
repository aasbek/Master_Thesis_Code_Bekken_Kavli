import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
// import java.util.ArrayList;
// import java.util.Arrays;
import java.util.Vector;


public class InputReader {
	public static void inputReader(String datafile, InstanceData inputdata, Vector<Node> pickupNodes, Vector<Node> deliveryNodes, Vector<Node>startDepots, Vector<Vehicle>vehicles, PrintWriter fw, PrintWriter cw ) {
		try {
			File file = new File(datafile);
			FileReader reader = new FileReader(file);
			BufferedReader fr = new BufferedReader(reader);
			
			// Reading the time window setting of the problem
			String line = fr.readLine();
			String[] list1 = line.split(",");
			inputdata.timeWindowSetting = Integer.parseInt(list1[1].trim());
			fw.print(inputdata.timeWindowSetting + ";");
			cw.print(inputdata.timeWindowSetting + ";");
		
			// Reading the weight generation setting of the problem
			line = fr.readLine();
			list1 = line.split(",");
			inputdata.weightSetting = Integer.parseInt(list1[1].trim());
			fw.print(inputdata.weightSetting + ";");
			cw.print(inputdata.weightSetting + ";");
			
			// Reading the number of vehicles in the problem
			line = fr.readLine();
			list1 = line.split(",");
			inputdata.numberOfVehicles = Integer.parseInt(list1[1].trim());
			for (int k = 0; k<inputdata.numberOfVehicles; k++)  {
				Vehicle v = new Vehicle ();
				v.number = k;
				vehicles.add(v);
			}
			
			//Giving each node a number, corresponding to the location in the vector in the data file
			line = fr.readLine();
			list1 =line.split(",");
			for (int k = 0; k<inputdata.numberOfVehicles; k++)  {
				vehicles.get(k).nodes = new Vector<Node>();
				for (int i = 1; i < list1.length; i++) {
					int number = Integer.parseInt(list1[i].trim());
					Node hello = new Node(number);
					vehicles.get(k).nodes.add(hello);
					if(number == 0 || number == 1) {
						hello.type = "Depot";
					}
					else if((number%2)==0 && number > 1) {
						hello.type = "PickupNode";	
					}
					else if((number%2)!=0 && number > 1){
						hello.type = "DeliveryNode";
					}
					
				}
			}
			
			for (int i = 3; i < list1.length; i++) {
				int number = Integer.parseInt(list1[i].trim());
				Node hello = new Node(number);
				if((number%2)==0 && number > 1) {
					hello.type = "PickupNode";
					pickupNodes.add(hello);
				}
				else if((number%2)!=0 && number > 1){
					hello.type = "DeliveryNode";
					deliveryNodes.add(hello);
				}
			}
			
			
			// Volume capacity
			line = fr.readLine();
			list1 = line.split(",");
			inputdata.volumeCap = Integer.parseInt(list1[1].trim());
			
			
			//Weight capacity
			line = fr.readLine();
			list1 = line.split(",");
			inputdata.weightCap = Integer.parseInt(list1[1].trim());	
			
	
			//Early time window
			line = fr.readLine();
			list1 = line.split(",");
			for (int k = 0; k<inputdata.numberOfVehicles; k++)  {
				for(int i = 1; i < list1.length; i++){
					double number = Double.parseDouble(list1[i].trim());
					vehicles.get(k).nodes.get(i-1).earlyTimeWindow = number;
				}
			}
			int count = 0;
			for(int i = 3; i < list1.length-1; i+= 2){
				double number = Double.parseDouble(list1[i].trim());
				pickupNodes.get(count).earlyTimeWindow = number;
				count++;
			}
			count = 0;
			for(int i = 4; i < list1.length; i+= 2){
				double number = Double.parseDouble(list1[i].trim());
				deliveryNodes.get(count).earlyTimeWindow = number;
				count ++;
			}
			
			
			//Late time window
			line = fr.readLine();
			list1 = line.split(",");
			for (int k = 0; k<inputdata.numberOfVehicles; k++)  {
				for(int i = 1; i < list1.length; i++){
					double number = Double.parseDouble(list1[i].trim());
					vehicles.get(k).nodes.get(i-1).lateTimeWindow = number;
				}
			}
			count = 0;
			for(int i = 3; i < list1.length; i+= 2){
				double number = Double.parseDouble(list1[i].trim());
				pickupNodes.get(count).lateTimeWindow = number;
				count++;
			}
			count = 0;
			for(int i = 4; i < list1.length; i+= 2){
				double number = Double.parseDouble(list1[i].trim());
				deliveryNodes.get(count).lateTimeWindow = number;
				count ++;
			}
			
			
			//Assigning a weight to each node
			line = fr.readLine();
			list1 = line.split(",");
			for(int i = 1; i < list1.length; i++){
				int number = Integer.parseInt(list1[i].trim());
				pickupNodes.get(i-1).weight = number;
				deliveryNodes.get(i-1).weight = number;
			}
			for(Vehicle k : vehicles) {
				int t = 2;
				for(int i = 1; i < list1.length; i ++){
					int number = Integer.parseInt(list1[i].trim());
					k.nodes.get(t).weight = number;
					k.nodes.get(t+1).weight = number;
					t += 2;
				}
			}
			
	
			//Assigning a volume to each node
			line = fr.readLine();
			list1 = line.split(",");
			for(int i = 1; i < list1.length; i++){
				int number = Integer.parseInt(list1[i].trim());
				pickupNodes.get(i-1).volume = number;
				deliveryNodes.get(i-1).volume = number;
			}
			for(Vehicle k : vehicles) {
				int t = 2;
				for(int i = 1; i < list1.length; i ++){
					int number = Integer.parseInt(list1[i].trim());
					k.nodes.get(t).volume = number;
					k.nodes.get(t+1).volume = number;
					t += 2;
				}
			}
			
			
			//Assigning location to each pickup node
			line = fr.readLine();
			list1 = line.split(",");
			for(Vehicle k : vehicles) {
				for(int i = 1; i < list1.length; i++){
					int number = Integer.parseInt(list1[i].trim());
					k.nodes.get(i*2).location = number;
					k.nodes.get(i*2).getLocation(number);
				}
			}
			for(int i = 1; i < list1.length; i++){
				int number = Integer.parseInt(list1[i].trim());
				pickupNodes.get(i-1).location = number;
				pickupNodes.get(i-1).getLocation(number);
			}
			
			
			//Assigning location to each delivery node
			line = fr.readLine();
			list1 = line.split(",");
			for(Vehicle k : vehicles) {
				for(int i = 1; i < list1.length; i++){
					int number = Integer.parseInt(list1[i].trim());
					k.nodes.get(i*2+1).location = number;
					k.nodes.get(i*2+1).getLocation(number);
					
				}
			}
			for(int i = 1; i < list1.length; i++){
				int number = Integer.parseInt(list1[i].trim());
				deliveryNodes.get(i-1).location = number;
				deliveryNodes.get(i-1).getLocation(number);
			}
			
		
			// Assigning location to the start depot of each vehicle
			line = fr.readLine();
			list1 = line.split(",");
			for(int i = 1; i < list1.length; i++){
				int number2 = Integer.parseInt(list1[i].trim());
				vehicles.get(i-1).nodes.get(0).location = number2;
				vehicles.get(i-1).nodes.get(0).type = "Depot";
				vehicles.get(i-1).nodes.get(0).getLocation(number2);
				vehicles.get(i-1).startDepot = vehicles.get(i-1).nodes.get(0);
			}
			
			// Finding the number of vehicles in the data file
			inputdata.numberOfVehicles = vehicles.size();
			
			// Assigning location to the end depot (zero time and distance to every other node)
			line = fr.readLine();
			list1 = line.split(",");
			int number = Integer.parseInt(list1[1].trim());
			for (int k = 0; k<inputdata.numberOfVehicles; k++)  {
				vehicles.get(k).nodes.get(1).type = "Depot";
				vehicles.get(k).nodes.get(1).location = number;
				vehicles.get(k).nodes.get(1).getLocation(number);
			}
			
			
			// Counting the number of cities
			line = fr.readLine();
			list1 = line.split(",");
			inputdata.numberOfCities = Integer.parseInt(list1[1].trim());
			
			
			// Creating empty time and distance matrices
			inputdata.times = new double[inputdata.numberOfCities][inputdata.numberOfCities];
			inputdata.distances = new double[inputdata.numberOfCities][inputdata.numberOfCities];
	
			
			fr.readLine();
			
			// Filling the time matrix
			for(int i = 0; i < inputdata.numberOfCities; i++) {
				line = fr.readLine();
				for(int j = 0; j < inputdata.numberOfCities; j++){
				list1 = line.split(",");
				inputdata.times[i][j] = Double.parseDouble(list1[j].trim());
				
				}
			}
	
			
			fr.readLine();
			
			// Filling the distance matrix
			for(int i = 0; i < inputdata.numberOfCities; i++) {
				line = fr.readLine();
				for(int j = 0; j < inputdata.numberOfCities; j++){
				list1 = line.split(",");
				inputdata.distances[i][j] = Double.parseDouble(list1[j].trim());
				}
			}
			
			
			// Assigning industry specific parameters
			line = fr.readLine();
			list1 = line.split(",");
			inputdata.fuelPrice = Double.parseDouble(list1[1].trim());
			
			line = fr.readLine();
			list1 = line.split(",");
			inputdata.fuelConsumptionEmptyTruckPerKm = Double.parseDouble(list1[1].trim());
			
			line = fr.readLine();
			list1 = line.split(",");
			inputdata.fuelConsumptionPerTonKm = Double.parseDouble(list1[1].trim());
			
			line = fr.readLine();
			list1 = line.split(",");
			inputdata.laborCostperHour = Integer.parseInt(list1[1].trim());
			
			line = fr.readLine();
			list1 = line.split(",");
			inputdata.otherDistanceDependentCostsPerKm = Double.parseDouble(list1[1].trim());
			
			line = fr.readLine();
			list1 = line.split(",");
			inputdata.otherTimeDependentCostsPerKm = Integer.parseInt(list1[1].trim());
			
			line = fr.readLine();
			list1 = line.split(",");
			inputdata.timeTonService = Double.parseDouble(list1[1].trim());
			
			line = fr.readLine();
			list1 = line.split(",");
			inputdata.revenue = Integer.parseInt(list1[1].trim());
			
			fr.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
