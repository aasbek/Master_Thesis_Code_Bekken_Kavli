import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

public class BBNode {
	public Vector<Node> pickupNodes;
	public Vector<Vehicle> vehicles;
	private BBNode parent;
	private int depth;
	private double objectiveValue;
	private int nodeId;
	int numberOfPickupsServed;
	int[][] branchingMatrix; 
	ArrayList<Double> lambdaValues;
	Hashtable<Integer, Double> MPsolutionVarsBBnode; 
	ArrayList<Integer> pickupNodesBranchedOn;
	String type;


	public BBNode(BBNode parent, int depth, int nodeId, Vector<Vehicle> vehicles, Vector<Node> pickupNodes, int[][] branchingMatrix, String type) {
		this.parent = parent;
		this.depth = depth;
		this.nodeId = nodeId;
		this.vehicles = vehicles;
		this.pickupNodes = pickupNodes;
		this.branchingMatrix = branchingMatrix;
		this.lambdaValues = new ArrayList<Double>();
		this.MPsolutionVarsBBnode = new Hashtable<Integer,Double>();
		this.pickupNodesBranchedOn = new ArrayList<Integer>();
		this.type = type;
	}
	
	public void setObjectiveValue(double objVal) {
		this.objectiveValue = objVal;
	}
	
	public BBNode getParent() {
		return this.parent;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public String getType() {
		return type;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public double getObjectiveValue() {
		return objectiveValue;
	}

	public void setParent(BBNode parent) {
		this.parent = parent;
	}
	
}
