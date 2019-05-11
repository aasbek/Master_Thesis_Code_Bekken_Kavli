import java.util.ArrayList;


public class InstanceData {
	public String instanceName;
	public double fuelPrice;
	public double fuelConsumptionEmptyTruckPerKm;
	public double fuelConsumptionPerTonKm;
	public int laborCostperHour;
	public double otherDistanceDependentCostsPerKm;
	public int otherTimeDependentCostsPerKm;
	public double timeTonService;
	public int revenue;
	public ArrayList<Integer> nodesDepot;
	public ArrayList<Integer> nodes;
	public int volumeCap;
	public int weightCap;
	public double[][] times;
	public double[][] distances;
	public int numberOfCities;
	public int numberOfVehicles; 

	
	public InstanceData(String datafile) {
		this.instanceName = datafile;
	}
	
	// Getting distance from one node to the other from the distance matrix in the data file
	public double getDistance (Node i, Node j){
	int iLocation = i.location;
	int jLocation = j.location;
	return distances[iLocation-1][jLocation-1];
	}
	
	// Getting time from one node to the other from the time matrix in the data file
	public double getTime (Node i, Node j){
	int iTime = i.location;
	int jTime = j.location;
	return times[iTime-1][jTime-1];
	}
}