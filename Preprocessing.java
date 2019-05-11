import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

public class Preprocessing {
	
	public Vector<Node> pickupNodes;
	public Vector<Node> deliveryNodes;
	public Vector<Node> depot;
	public InstanceData inputdata;
	public ArrayList<double[]> unreachableNodesFromNode;
	public ArrayList<double[]> unreachableDelNodesFromNode;
	public Hashtable<String, Double> unreachableDelPairs;
	public Vector<Vehicle> vehicles;
	public Vector<Node> nodes;
	
	public Preprocessing(Vector<Node> pickupNodes, Vector<Node> deliveryNodes, Vector<Vehicle> vehicles, InstanceData inputdata, Vector<Node> nodesWithoutDepot, Vehicle vehicle) {
		this.pickupNodes = pickupNodes;
		this.deliveryNodes = deliveryNodes;
		this.depot = depot;
		this.inputdata = inputdata;
		nodes = vehicle.nodes;
	}
	
	// Computing different combinations in which a node may get unreachable
	public void unreachableNodeCombination() {
		this.unreachableNodesFromNode = new ArrayList<double[]>();
		for(int i = 0; i < nodes.size();i++) {
			unreachableNodesFromNode.add(new double[nodes.size()]);
		}

		for(Node pickup : pickupNodes) {
			for(Node pickup2 : pickupNodes) {
				if(pickup!=pickup2) {
					Node delivery2 = nodes.get(pickup2.number+1);
					Node delivery = nodes.get(pickup.number+1);
					double time = pickup2.lateTimeWindow-(pickup.weight*inputdata.timeTonService)-
							inputdata.getTime(pickup, pickup2); // Checking if the time for traveling from pickup 1 to another pickup 2 is so large that the time window in pickup 2 will be violated
					double time2= delivery2.lateTimeWindow-pickup2.weight *inputdata.timeTonService-inputdata.getTime(pickup2, delivery2)-
							-pickup.weight *inputdata.timeTonService-inputdata.getTime(pickup, pickup2); // Checking whether when going from pickup 1 to pickup 2, and then to delivery 2 will violate the TW in delivery 2 
					double time3 = delivery.lateTimeWindow-delivery2.weight*inputdata.timeTonService -inputdata.getTime(delivery2, delivery)-
							pickup2.weight *inputdata.timeTonService-inputdata.getTime(pickup2, delivery2)-
							-pickup.weight *inputdata.timeTonService-inputdata.getTime(pickup, pickup2); // Checking whether going from pickup 1 to pickup 2, then to delivery 2, and at last to delivery 1 will violate the TW in delivery 1
					double time4 = delivery2.lateTimeWindow-delivery.weight*inputdata.timeTonService -inputdata.getTime(delivery, delivery2)-
							pickup2.weight *inputdata.timeTonService-inputdata.getTime(pickup2, delivery)-
							-pickup.weight *inputdata.timeTonService-inputdata.getTime(pickup, pickup2); // Checking whether going from pickup 1 to pickup 2, from pickup 2 to delivery 1, and then from delivery 1 to delivery 2 will violate the TW in delivery 2
					double time5 = delivery2.lateTimeWindow-pickup.weight*inputdata.timeTonService -inputdata.getTime(pickup2, delivery2)-
							delivery.weight *inputdata.timeTonService-inputdata.getTime(delivery, pickup2)-
							-pickup.weight *inputdata.timeTonService-inputdata.getTime(pickup, delivery); // Checking whether going from pickup 1 to delivery 1, from delivery 1 to pickup 2 and from pickup 2 to delivery 2 will violate the TW in delivery 2
					unreachableNodesFromNode.get(pickup.number)[pickup2.number] = Math.min(time,Math.min(time2, Math.max(time3, Math.max(time4, time5))));

					time = pickup2.lateTimeWindow-(pickup.weight*inputdata.timeTonService)-
							inputdata.getTime(delivery, pickup2);
					time2= delivery2.lateTimeWindow-pickup2.weight *inputdata.timeTonService-inputdata.getTime(pickup2, delivery2)-
							 -pickup.weight *inputdata.timeTonService-inputdata.getTime(delivery, pickup2);
					unreachableNodesFromNode.get(delivery.number)[pickup2.number] = Math.min(time,time2);	
				}
			}	
		}
	}
	
	// Computing different combinations in which a delivery node may get unreachable
	public void unreachableDeliveryNode() {
		this.unreachableDelNodesFromNode = new ArrayList<double[]>();
		for(int i = 0; i < nodes.size();i++) {
			unreachableDelNodesFromNode.add(new double[nodes.size()]);
		}
		
		for(Node delivery : deliveryNodes) {
			for(Node delivery2 : deliveryNodes) {
				if(delivery==delivery2) {
					unreachableDelNodesFromNode.get(delivery.number)[delivery2.number] = 1000;
				}
				else {
					Node pickup = nodes.get(delivery.number-1);
					double time = delivery2.lateTimeWindow-(pickup.weight*inputdata.timeTonService)-inputdata.getTime(delivery, delivery2); // Checking if going from delivery 1 to delivery 2 violates TW in delivery 2
					unreachableDelNodesFromNode.get(delivery.number)[delivery2.number] = time;

			
					time = delivery2.lateTimeWindow-(pickup.weight*inputdata.timeTonService)-inputdata.getTime(pickup, delivery2); // Checking if going from pickup 1 to delivery 2 violates TW in delivery 2
					unreachableDelNodesFromNode.get(pickup.number)[delivery2.number] = time;
				}
			}
			
		}
	}
	
	// Computing different combinations in which pairs of delivery nodes may get unreachable
	public void unreachableDeliveryPairs() {
		unreachableDelPairs = new Hashtable<String, Double>();
		for(Node node : nodes) {
			for(Node delivery : deliveryNodes) {
				for(Node delivery2 : deliveryNodes) {
					if(delivery!=delivery2 && delivery!=node && delivery2!=node && delivery.number <delivery2.number ) {
					
	
						double time = delivery2.lateTimeWindow-(delivery2.weight*inputdata.timeTonService)-
								inputdata.getTime(delivery, delivery2) - (delivery.weight*inputdata.timeTonService)-
								inputdata.getTime(node, delivery); // If going from any node to delivery 1, and then to delivery 2 violates TW in delivery 2
						double time2 = delivery.lateTimeWindow-(delivery.weight*inputdata.timeTonService)-
								inputdata.getTime(delivery2, delivery) - (delivery2.weight*inputdata.timeTonService)-
								inputdata.getTime(node, delivery2); // If going from any node to delivery 2, and then to delivery 1 violates TW in delivery 1

						String temp = "";
						if(node.number<10) {
							temp+="0"+node.number;
						}
						else {
							temp+=node.number;
						}
						if(delivery.number<10) {
							temp+="0"+delivery.number;
						}
						else {
							temp+=delivery.number;
						}
						if(delivery2.number<10) {
							temp+="0"+delivery2.number;
						}
						else {
							temp+=delivery2.number;
						}
						unreachableDelPairs.put(temp, Math.max(time, time2));
						if(node.lateTimeWindow>Math.max(time, time2)) {
							
						}	
					}	
				}
			}
		}	
	}
	

}
