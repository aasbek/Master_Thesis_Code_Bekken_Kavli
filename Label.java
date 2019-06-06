import java.util.Vector;

public class Label {
		public int routeNumber;
		public Vehicle vehicle;
		public Vector<Node> path;
		public Vector<Integer> pickupNodesVisited;
		public double pickupDual;
		public double totalPickupDual;
		public double vehicleDual;
		public double reducedCost;
		public double time; 
		public double profit;
		public double weightCapacityUsed;
		public double volumeCapacityUsed;
		public Label predesessor;
		public Node node;
		public Vector<Integer> unreachablePickupNodes;
		public double dailyDrivingTime;
		public double consecutiveDrivingTime;
		public double startTimeDailyRest;
		public double startTimeIntermediateBreak;
		public Vector<Integer> openNodes; 
		public int numberDailyRests;
		public double consecutiveWorkingTime;
		public double totalDistance;
		public double capacityUtilization;
		public double totalCosts;


	// Print information about the label
	public String toString() {
		String string = "Node: " + node.number + ", Location: " + node.location + " , " + node.locationName + ", Time: " + time + ", Profit: "+ profit + ", Reduced cost: " + reducedCost +
				", unreachablePickupNodes: " + unreachablePickupNodes + ", openNodes: " + openNodes + ", dailyDrivingTime: " + dailyDrivingTime + ", startTimeDailyRest: " + startTimeDailyRest + ", numberDailyRests: " + numberDailyRests + ", StartTimeIntermediateBreak: "+ startTimeIntermediateBreak + ", consecutiveDrivingTime: " + consecutiveDrivingTime + ", workingTime: " + consecutiveWorkingTime 
				+ ", totalDistance: " + totalDistance+ ", WeightCapacityUsed: " + weightCapacityUsed + ", VolumeCapacityUsed: " + volumeCapacityUsed;
		

		Label temp = predesessor;
		
		while(temp!=null) {
			string+=", Predessesor: "+temp.node.number;
			temp=temp.predesessor;
		}
		return string;
	}
}
