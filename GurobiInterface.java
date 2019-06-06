import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import gurobi.*;

	public class GurobiInterface {
		
		// Maximal execution time (3 hours)
		long maxExecutionTime = 10800000;
		boolean timeLimitReached;
		
		private Hashtable<Integer, Label> pathList;
		double[] dualVisitedPickupsCon;  
		double[] dualOneVisitCon;
	
		// Variables to be registered and printed to results file 
		long totalTimeInSub;
		long totalTimeInMaster;
		long rootNodeTime;
		double optimalObjective = 0;
		double rootNodeObjective = 0;
		double bestLeafNodeObjective = 0;
		double bestIntegerLeafNodeObjective = 0;
		int numberBBnodes = 0;
		int numberOfSubproblemCalls;
		double numberOfPickupsServedInBestNode;
		double numberOfPickupsServedInRootNode;

		
		// Creating Gurobi environment
	    GRBEnv    env   = new GRBEnv();
	    GRBModel  model;
	    int status;
	    
	    // Creating Gurobi variables
		public ArrayList<GRBVar>[] lambdaVars;
		ArrayList<BBNode> leafNodes; 
	    
	    // Creating Gurobi constraints
		public GRBConstr[] visitedPickupsCon;
		public GRBConstr[] oneVisitCon; 
		
		// Creating Gurobi objective function
		public GRBLinExpr objective;
		
		// Sets utilized
	    public InstanceData inputdata;
		public Vector<Node> pickupNodes;
		public Vector<Node> deliveryNodes;
		public Vector<Vehicle> vehicles;
		public PathBuilder2 builder;
		public PrintWriter pw;
		public PrintWriter fw;
		public PrintWriter cw;

		// Initializing values
		public double zeroTol = 0.0001;
		public double profit = 0;
		public int numberOfRoutes = 0;
		int BBNodeIDcounter = 1;
		long solutionStartTime;
		
		
		
		public GurobiInterface(InstanceData inputdata, Vector<Node> deliveryNodes, Vector<Node> pickupNodes, Vector<Vehicle> vehicles, Vector<Double> dualVisitedPickupsCon, Vector<Double> dualOneVisitCon, PrintWriter pw, PrintWriter fw, PrintWriter cw) throws Exception {
			env.set(GRB.IntParam.Presolve, 0);
			env.set(GRB.DoubleParam.OptimalityTol, 0.000000001);
			env.set(GRB.DoubleParam.FeasibilityTol, 0.000000001);
			model = new GRBModel(env);
			
			this.vehicles = vehicles; 
			this.inputdata = inputdata;
			this.pickupNodes = pickupNodes;
			this.deliveryNodes = deliveryNodes;
			this.pw = pw;
			this.fw = fw;
			this.cw = cw;
			this.vehicles = vehicles;
			this.pathList = new Hashtable<Integer, Label>();
			this.leafNodes = new ArrayList<>();	
		}
		
		
		
		// Building the master problem with constraints and variables
		public void buildProblem() throws Exception {
			this.objective = new GRBLinExpr();
			this.lambdaVars = new ArrayList[vehicles.size()];
			this.visitedPickupsCon = new GRBConstr[pickupNodes.size()];
			this.oneVisitCon = new GRBConstr[vehicles.size()];
		
			// Creating an initial label with profit 0 (path from depot to depot)
			Label firstLabel = new Label();
			firstLabel.profit = 0;
		
			for(int k = 0; k < vehicles.size(); k++) {
				this.lambdaVars[k] = new ArrayList<GRBVar>();
				this.vehicles.get(k).vehicleRoutes = new Vector<Integer>();
				vehicles.get(k).vehicleRoutes.add(numberOfRoutes);
				// Adding a lambda variable per vehicle and creates the objective function and the first constraint 
				for (int r : vehicles.get(k).vehicleRoutes) {
					GRBVar temp = model.addVar(0, GRB.INFINITY, firstLabel.profit, GRB.CONTINUOUS, "lambda_"+k+"_"+r); 
					this.lambdaVars[k].add(temp);
					pathList.put(numberOfRoutes, firstLabel);
					this.objective.addTerm(firstLabel.profit, temp);
					GRBLinExpr temp2 = new GRBLinExpr();
					temp2.addTerm(1, temp);
					this.oneVisitCon[k] = model.addConstr(temp2, GRB.EQUAL, 1, "oneVisitCon"+k);		
				}
				numberOfRoutes += 1;
			}
			
			model.setObjective(objective, GRB.MAXIMIZE);
			
			// Creates the second constraint 
			for(int i = 0; i < pickupNodes.size(); i++) {
				GRBLinExpr temp = new GRBLinExpr();
				this.visitedPickupsCon[i] = model.addConstr(temp, GRB.LESS_EQUAL,1,"visitedPickupCon"+i);	
			}
			model.update();
		}
		

		// Method initiating the problem by solving the problem for the only columns generated, which are from depot to depot
		public void initiateProblem() throws Exception {
			buildProblem();
			model.optimize();
			
			
			builder = new PathBuilder2(pickupNodes, deliveryNodes, inputdata, pw, vehicles);
			
			// Print initial solution
			for(int k = 0; k < vehicles.size(); k++) {
				for (GRBVar var : lambdaVars[k]) {
			//		System.out.println(var.get(GRB.StringAttr.VarName)  + " " +var.get(GRB.DoubleAttr.X));
					pw.append(var.get(GRB.StringAttr.VarName)  + " " +var.get(GRB.DoubleAttr.X) + "\n");
				}
			}
			
			// Creating sets of duals and finds initial dual values
			double[] dualVisitedPickupsCon = new double[pickupNodes.size()];  
			double[] dualOneVisitCon = new double[vehicles.size()];
			
			for(int i = 0; i < pickupNodes.size(); i++) {
				double dualPickup_i = (double) visitedPickupsCon[i].get(GRB.DoubleAttr.Pi);
				dualVisitedPickupsCon[i] = dualPickup_i;
			}
			
			for(int k = 0; k < vehicles.size(); k++) {
				double dualVehicle_k = (double) oneVisitCon[k].get(GRB.DoubleAttr.Pi);
				dualOneVisitCon[k]=dualVehicle_k;
			}

	//		System.out.println("Objective value initial problem: " +model.get(GRB.DoubleAttr.ObjVal));
			pw.append("Objective value initial problem: " +model.get(GRB.DoubleAttr.ObjVal));
			model.update();
		}
		
		
		// Method adding a new column to the problem and setting its corresponding parameters 
		public void addRoute(Label l) throws Exception{
			GRBVar tempVar = model.addVar(0, GRB.INFINITY, l.profit, GRB.CONTINUOUS,  "lambda_"+l.vehicle.number + "_" + numberOfRoutes);
			lambdaVars[l.vehicle.number].add(tempVar);
			this.objective.addTerm(l.profit, tempVar);
			for(int i = 0; i < pickupNodes.size(); i++) {	
				if(l.pickupNodesVisited.contains(pickupNodes.get(i).number)) {
					model.chgCoeff(visitedPickupsCon[i], tempVar, 1);
				}	
			}
				model.chgCoeff(oneVisitCon[l.vehicle.number], tempVar, 1);
				pathList.put(numberOfRoutes, l);
		}
		
		
		
		// The main method solving the root node, and if the root node is fractional, calls the branch and bound tree builder
		public void columnGenerator() throws Exception {
			timeLimitReached = false;
			solutionStartTime = System.currentTimeMillis();
			// Problem is initiated 
			initiateProblem();
			// The root node is created with an empty branching matrix
			int[][] initialBranchingMatrix = new int[vehicles.size()][pickupNodes.size()];
			BBNode rootNode = new BBNode(null, 0, 0, vehicles, pickupNodes, initialBranchingMatrix, "root");
			// Solves the root node and counts the time
			long startRootNodeTime = System.currentTimeMillis();
			solveProblem(rootNode);
			long endRootNodeTime = System.currentTimeMillis();
			rootNodeTime =  (endRootNodeTime - startRootNodeTime);
			pw.append("Total time to solve rootnode: " + rootNodeTime + "\n");
			pw.append("\n---- ROOTNODE FINISHED ----\n ");
			System.out.println("  ");
			System.out.println("---- ROOTNODE FINISHED ----");
			System.out.println("  ");
			rootNodeObjective = rootNode.getObjectiveValue();
			optimalObjective = rootNode.getObjectiveValue();
			numberOfPickupsServedInRootNode = findNumberOfPickupsServed(rootNode);
			// Checks if the root node contains fractional pickup nodes 
			ArrayList<Node> fractionalPickupNodes = findFractionalNodes(rootNode.MPsolutionVarsBBnode);
			
			// If the root node contains fractional pickup nodes, do branching 
			if(checkPickupFractionality(fractionalPickupNodes) != null) {
				pw.append("\n ---- BRANCHING ---- \n");
				System.out.println("  ");
				System.out.println("---- BRANCHING ----");
				System.out.println("  ");
				buildTree(rootNode);
				pw.append("Total time in subproblem: " + totalTimeInSub + "\n");
				System.out.println("Total time in subproblem: " + totalTimeInSub);
				pw.append("Total time in master problem " + totalTimeInMaster + "\n");
				System.out.println("Total time in master problem " + totalTimeInMaster);
			}
			else {
				// printing capacity utilization and costs
				double totalDistanceAllVehicles = 0;
				double totalCostsAllVehicles = 0;
				for(int route : rootNode.MPsolutionVarsBBnode.keySet()) {
					printSolution(route);
					totalDistanceAllVehicles += pathList.get(route).totalDistance;	
				}
				double totalCapacityUtilization = 0;
				for(int route : rootNode.MPsolutionVarsBBnode.keySet()) {
					totalCapacityUtilization += (pathList.get(route).capacityUtilization * pathList.get(route).totalDistance)/totalDistanceAllVehicles;
					totalCostsAllVehicles += pathList.get(route).totalCosts;
				}
			//	System.out.println(totalCapacityUtilization);
				cw.print(totalDistanceAllVehicles);
				cw.print(";");
				cw.print(totalCapacityUtilization);
				cw.print(";");
				cw.print(totalCostsAllVehicles);
				cw.print(";");
			}
			model.dispose();
		    env.dispose();
		}
		
		
		
		// Solving the problem for a branch and bound node (first for the root node) - iterates between the master and the subproblem
		public void solveProblem(BBNode bbNode) throws Exception {
			// Sets all lambdas that must be zero because of branching to zero 
			removeIllegalLambdaVars(bbNode, this.lambdaVars);
			ArrayList<Label> bestLabels = new ArrayList<Label>();
			boolean addedLabel = true;
			int counter = 0;
			dualVisitedPickupsCon = new double[pickupNodes.size()];  
			dualOneVisitCon = new double[vehicles.size()];
			// While new columns are still added (while there are labels in the subproblem with positive reduced cost)
			while(addedLabel && counter<10000) {
				pw.append("\n----- New master problem ---- \n");
				System.out.println(" ");
				System.out.println("---- New master problem ----");
				System.out.println(" ");
				counter++;
				addedLabel = false;
				// Solving the subproblem for each vehicle in the problem
				for(int k = 0; k < vehicles.size(); k++) {
					// Checking if maximal execution time is met
					if(System.currentTimeMillis() - solutionStartTime > maxExecutionTime) {
						timeLimitReached = true;
						pw.append("\n--- Maximal execution time met ----\n");
						System.out.println(" ");
						System.out.println("--- Maximal execution time met ----");
						BBNode bestIntegerLeafNode = null;
						for(BBNode leafNode : leafNodes) {
							// Finding the objective of the best leaf node that is integer
							if(!checkLambdaFractionality(leafNode) && leafNode.getObjectiveValue() > bestIntegerLeafNodeObjective) {
								bestIntegerLeafNodeObjective = leafNode.getObjectiveValue();
								bestIntegerLeafNode = leafNode;
							}
						}
						double totalDistanceAllVehicles = 0;
						double totalCostsAllVehicles = 0;
						double totalCapacityUtilization = 0;
						if(bestIntegerLeafNode != null) {
							for(int route : bestIntegerLeafNode.MPsolutionVarsBBnode.keySet()) {
								printSolution(route);
								totalDistanceAllVehicles += pathList.get(route).totalDistance;	
								totalCostsAllVehicles += pathList.get(route).totalCosts;
							}
							
							for(int route : bestIntegerLeafNode.MPsolutionVarsBBnode.keySet()) {
								totalCapacityUtilization += (pathList.get(route).capacityUtilization * pathList.get(route).totalDistance)/totalDistanceAllVehicles;
							}
						}
						//System.out.println(totalCapacityUtilization);
						cw.print(totalDistanceAllVehicles);
						cw.print(";");
						cw.print(totalCapacityUtilization);
						cw.print(";");
						cw.print(totalCostsAllVehicles);
						cw.print(";");
						
						optimalObjective = 0;
						printResults();
						fw.print(maxExecutionTime);
						fw.println(" ");
						pw.flush();
						fw.close();
						pw.close();
						return;
					}
				//	System.out.println("");
				//	System.out.println("Solving subproblem for vehicle " + k);
					pw.append("\n Solving subproblem for vehicle " + k + "\n");
					// Calls subproblem and counts the time
					long startTime = System.nanoTime();
					Vector<Label> list = builder.BuildPaths(vehicles.get(k), dualVisitedPickupsCon, dualOneVisitCon, bbNode);
					bestLabels = builder.findBestLabel(list, bbNode, pathList, vehicles.get(k));
					long endTime = System.nanoTime();
			//		System.out.println("Subproblem took "+(endTime-startTime)/1000000 + " milli seconds"); 
					pw.append("\n Subproblem took "+(endTime-startTime)/1000000 + " milli seconds \n"); 
					totalTimeInSub += (endTime-startTime)/1000000;
					numberOfSubproblemCalls += 1;
					
					// If calling the subproblem gives back a list of labels with positive reduced cost, add the labels as columns in the master problem
					if(bestLabels != null) {
						for(Label bestLabel : bestLabels) {
							bestLabel.routeNumber = numberOfRoutes;
								vehicles.get(k).vehicleRoutes.add(numberOfRoutes);
								addRoute(bestLabel);
								addedLabel=true;
								numberOfRoutes += 1;
						}
					} 
				}	
				// Solving the master problem with new columns and counts the time
				model.update();
				long startTimeSolveMaster = System.nanoTime(); 
				model.optimize();
				long endTimeSolveMaster = System.nanoTime();
				totalTimeInMaster += (endTimeSolveMaster-startTimeSolveMaster)/1000000;
				//if(model.feasibility() )
				status = model.get(GRB.IntAttr.Status);
				//error = GRBgetintattr (model , GRB_INT_ATTR_STATUS , & optimstatus );
				if(status == GRB.INFEASIBLE) {
					// If the model is infeasible for a BBnode, set the objective value to a negative number
					pw.append("Model not feasible.");
					bbNode.setObjectiveValue(-10000);
					return;
				}
				
				bbNode.setObjectiveValue(model.get(GRB.DoubleAttr.ObjVal));		
				pw.append("\n Objective value: " + model.get(GRB.DoubleAttr.ObjVal) + "\n");
				
				// Finding duals for the two constraints in the master problem
				for(int i = 0; i < pickupNodes.size(); i++) {
					double dualPickup_i =  visitedPickupsCon[i].get(GRB.DoubleAttr.Pi);
					dualVisitedPickupsCon[i] = dualPickup_i;
				}
				
				for(int k = 0; k < vehicles.size(); k++) {
					double dualVehicle_k = oneVisitCon[k].get(GRB.DoubleAttr.Pi);
					dualOneVisitCon[k]=dualVehicle_k;
				}
		
				model.update();
			}
			
			// Adding results from the column generation to the current branch and bound node's list of results and prints results to file
			for(int k = 0; k < vehicles.size(); k++) {
				int number = 0;
				int routeNumber = 0; 
				
				for (GRBVar var : lambdaVars[k]) {
					routeNumber = vehicles.get(k).vehicleRoutes.get(number);
					number ++;
					// If the variable is other than zero
					if(var.get(GRB.DoubleAttr.X)>0.01 ) {
					//	System.out.println("");
					//	System.out.println(var.get(GRB.StringAttr.VarName)  + " " +var.get(GRB.DoubleAttr.X));
						pw.append("\n");
						pw.append("\n" + var.get(GRB.StringAttr.VarName)  + " " +var.get(GRB.DoubleAttr.X) + "\n");
						bbNode.lambdaValues.add(var.get(GRB.DoubleAttr.X));
						bbNode.MPsolutionVarsBBnode.put(routeNumber, var.get(GRB.DoubleAttr.X));
						printSolution(routeNumber);
					}	
				}
			}
			
			// Reset all lambda variables that are set to zero before the next call of this method
			resetIllegalLambdaVars(this.lambdaVars);
		}
			
		 
		
		// Method for printing the solution of the master problem
		public void printSolution(int route) throws Exception{
			for(Vehicle v : vehicles ) {
				if(v.vehicleRoutes.contains(route)) {
				//	System.out.println("Vehicle " + v.number + " drives route " + route);
					pw.append("\n Vehicle " + v.number + " drives route " + route);
				//	System.out.println("Profit: " + pathList.get(route).profit);
					pw.append("\n Profit: " + pathList.get(route).profit);
					
					// If the route has a path (the first route for each vehicle is only artificial from depot to depot)
					if(route >= vehicles.size()) {
						// Print the label in the end depot and all predecessor labels 
					//	System.out.println(pathList.get(route).toString());
						pw.append("\n" + pathList.get(route).toString() + "\n");
						Label temp = pathList.get(route).predesessor;
						Label temp2 = pathList.get(route).predesessor;
						while(temp!=null) {
					//		System.out.println(temp.toString());
							pw.append(temp.toString() + "\n");	
						temp=temp.predesessor;
						}
						while(temp2!=null) {
							if(temp2.node.type == "PickupNode") {
								double directDistance = inputdata.getDistance(temp2.node, temp2.node.getCorrespondingNode(temp2.node, v.nodes)); 
								double directTime = inputdata.getTime(temp2.node, temp2.node.getCorrespondingNode(temp2.node, v.nodes));
								pw.append("\n" + temp2.node.number + ";" + temp2.node.locationName + ";" + temp2.node.getCorrespondingNode(temp2.node, v.nodes).locationName + ";" + directDistance + ";" + directTime + ";" + temp2.node.weight + ";" + temp2.node.volume);
							}
							temp2 = temp2.predesessor;
						}
						
					}
				//	System.out.println(" ");
					pw.append("\n");
				}
			}
		}
		
		
		// Finding all fractional pickup nodes (a pickup node that is taken a total of 1 time with a vehicle, but on two different routes, is not fractional)
		public ArrayList<Node> findFractionalNodes (Hashtable<Integer, Double> MPsolutionVars) throws Exception {
			ArrayList<Node> fractionalPickupNodes = new ArrayList<Node>();
			// Go through all pickup nodes
			for (Node node : pickupNodes) {
				node.fraction = 0;
				for(Vehicle vehicle : vehicles) {
					double vehicleFraction = 0;
					for(int route : vehicle.vehicleRoutes) {
						// If the variable is more than one and less than zero (= fractional)
						if (MPsolutionVars.containsKey(route) && MPsolutionVars.get(route) < 1) {
							// If the route has a path (the first route for each vehicle is artificial going from depot to depot)
							if(route >= vehicles.size()){
								if (pathList.get(route).path.contains(node)) {
									vehicleFraction += MPsolutionVars.get(route);
								}
							}
						}
					}	
					// If the vehicle fraction for this vehicle is higher than the current fraction for the pickup node, set this vehicle as the vehicle to branch on for this pickup
					if (vehicleFraction > node.fraction + zeroTol) {
						node.fraction = vehicleFraction;
						node.branchingVehicle = vehicle;
					}
				}
				// Add all fractional pickup nodes to the list 
				if (node.fraction < 1 - zeroTol && node.fraction > 0 + zeroTol ) {
					fractionalPickupNodes.add(node);
				}	
			}
			return fractionalPickupNodes;
		}
		
		
		// Finds the number of pickup nodes served (sums fractional pickups) - this method is called for the root node and for the final solution for comparison
		public double findNumberOfPickupsServed (BBNode bbNode) throws Exception {
			double currentNumber = 0;
			double numberOfPickupsServed = 0;
			for(int key : bbNode.MPsolutionVarsBBnode.keySet()) {
				if (key >= vehicles.size()) {
					currentNumber = ( bbNode.MPsolutionVarsBBnode.get(key) * pathList.get(key).pickupNodesVisited.size()) ;
					numberOfPickupsServed += currentNumber;
				}
			}
			return numberOfPickupsServed;
		} 
		
		
		// Finds the most fractional pickup node (closest to 0.5) of all fractional pickup nodes 
		public Node checkPickupFractionality (ArrayList<Node> fractionalPickupNodes) throws Exception {
			Node mostFractionalNode = null;
			double biggestFraction = 1;
			double fraction;
			for (Node n : fractionalPickupNodes) {
				if (n.fraction >= 0.5) {
					fraction =  1 - n.fraction;
				}
				else {
					fraction = 0.5 -  n.fraction ;
				}
				if (fraction < biggestFraction) {
					biggestFraction = fraction;
					mostFractionalNode = n;	
				}				
			}
			// Print to file
			if(!fractionalPickupNodes.isEmpty()) {	
			//	System.out.println(" ");
			//	System.out.println("Branching on pickup node   " + mostFractionalNode.number + "   and vehicle   " + mostFractionalNode.branchingVehicle.number );
				pw.append("\n Branching on pickup node " + mostFractionalNode.number + " and vehicle " + mostFractionalNode.branchingVehicle.number + "\n");
			//	System.out.println(" ");
			}
			return mostFractionalNode;
		}
		
		// Creating branching matrix that says which pickups that must be contained in paths and which that cannot be contained in subproblem 
		public int[][] branchingMatrixMaker (BBNode bbNode, Node branchingPickupNode, String type) throws Exception {
			
			int[][] branchingMatrix2 = new int[vehicles.size()][pickupNodes.size()];
			
			// Adding all elements in the branching matrix of the parent node to the branching matrix of the child node
			for (int i = 0; i < bbNode.branchingMatrix.length; i++) {
			    for (int j = 0; j < bbNode.branchingMatrix[i].length; j++) {
			    	branchingMatrix2[i][j] = bbNode.branchingMatrix[i][j];
			    }
			}
			
			// If the child is a left node, add -1 for the vehicle (cannot be visited in path)
			if(type == "left") {
				branchingMatrix2[branchingPickupNode.branchingVehicle.number][(branchingPickupNode.number/2) - 1] = -1;
			}
			// If the child is a right node, add 1 for the vehicle (must be visited by the vehicle in the path), and add -1 for all other vehicles
			else if(type == "right") {
				for(Vehicle v : vehicles) {
					if(branchingPickupNode.branchingVehicle.number == v.number) {
						branchingMatrix2[branchingPickupNode.branchingVehicle.number][(branchingPickupNode.number/2) - 1] = 1;
					}
					else {
						branchingMatrix2[v.number][(branchingPickupNode.number/2) - 1] = -1;
					}	
				}
			}
			return branchingMatrix2;
		}
		
		
		// Forcing lambdas that must be removed due to branching restrictions, to zero
		public void removeIllegalLambdaVars(BBNode bbNode, ArrayList<GRBVar>lambdaVars[]) throws Exception{
			for (Vehicle v : vehicles) {
				int number = 0;
				int routeNumber; 
				for (GRBVar var : lambdaVars[v.number]) {			
					routeNumber = vehicles.get(v.number).vehicleRoutes.get(number);
					
					
				
					
					
					// Going through all pickup nodes 
					for(Node pickupNumber : pickupNodes) {
						
						// If there is a right child (forcing some pickup to 1), the initialization route for the vehicle (going from depot to depot) must be set to zero 
						if(number == 0 && bbNode.branchingMatrix[v.number][pickupNumber.number/2 - 1] == 1) {
							var.set(GRB.DoubleAttr.LB, 0);
							var.set(GRB.DoubleAttr.UB, 0);
							model.update();
						}
						
						// If the pickup node is contained in the route of a lambda and the branching matrix of the current node says that this pickup node cannot be visited (= -1), force the lambda to zero 
						if (pathList.get(routeNumber).pickupNodesVisited != null && pathList.get(routeNumber).pickupNodesVisited.contains(pickupNumber.number) && bbNode.branchingMatrix[v.number][pickupNumber.number/2 - 1]  == -1) {
							var.set(GRB.DoubleAttr.LB, 0);
							var.set(GRB.DoubleAttr.UB, 0);
							model.update();
							break;
						}
						// If the pickup node is not contained in the route a lambda and the branching matrix of the current node says that this pickup node cannot be visited (= 1), force the lambda to zero 
						else if (pathList.get(routeNumber).pickupNodesVisited != null && !pathList.get(routeNumber).pickupNodesVisited.contains(pickupNumber.number) && bbNode.branchingMatrix[v.number][pickupNumber.number/2 - 1] == 1) {
							var.set(GRB.DoubleAttr.LB, 0);
							var.set(GRB.DoubleAttr.UB, 0);
							model.update();
							break;
						}
					}
					number ++;
				}
			} 
		}
		
		
		// If the root node is fractional, this method is called in order to build the branch and bound tree to find the optimal integer solution
		public void buildTree (BBNode rootNode) throws Exception {
			// Adds the leaf node to the set of leaf nodes
			leafNodes.add(rootNode);
			boolean fractional = true;	
			BBNode bestBBNode = null;
			
			// Stay in the while as long as the bestBBNode (BBnode with best objective value) is fractional 
			while(fractional && !timeLimitReached) {
				double bestProfit = 0;
				
				// Go through all leaf nodes and check which has best objective value 
				for(BBNode leafNode : leafNodes) {
					if(leafNode.getObjectiveValue() > bestProfit) {
						bestProfit = leafNode.getObjectiveValue();
						bestBBNode = leafNode;
					}
				}
				
				// Print the information about the current best BBnode to file
				pw.append("BESTNODE: " + bestBBNode.getNodeId() + " " + bestBBNode.getObjectiveValue() + "\n");
				pw.append("Branching matrix bestBBnode: \n");
				for (int i = 0; i < bestBBNode.branchingMatrix.length; i++) {
				    for (int j = 0; j < bestBBNode.branchingMatrix[i].length; j++) {
				        pw.append(bestBBNode.branchingMatrix[i][j] + " ");
				    }
				    pw.append("\n");
				}
				
				// Check if the current best BBNode is integer, and if so, terminate and retur the current best BBNode as the solution
				if(!checkLambdaFractionality(bestBBNode)) {	
				//	System.out.println("");
				//	System.out.println("BESTNODE has ID " + bestBBNode.getNodeId() + " and profit " + bestBBNode.getObjectiveValue());
					pw.append("\n BESTNODE has ID " + bestBBNode.getNodeId() + " and profit " + bestBBNode.getObjectiveValue() + "\n");
				//	System.out.println(" ");
					optimalObjective = bestBBNode.getObjectiveValue();
					double totalDistanceAllVehicles = 0;
					double totalCostsAllVehicles = 0;
					for(int route : bestBBNode.MPsolutionVarsBBnode.keySet()) {
						printSolution(route);
						totalDistanceAllVehicles += pathList.get(route).totalDistance;	
					}
					double totalCapacityUtilization = 0;
					for(int route : bestBBNode.MPsolutionVarsBBnode.keySet()) {
						totalCapacityUtilization += (pathList.get(route).capacityUtilization * pathList.get(route).totalDistance)/totalDistanceAllVehicles;
						totalCostsAllVehicles += pathList.get(route).totalCosts;
					}
					//System.out.println(totalCapacityUtilization);
					cw.print(totalDistanceAllVehicles);
					cw.print(";");
					cw.print(totalCapacityUtilization);
					cw.print(";");
					cw.print(totalCostsAllVehicles);
					cw.print(";");
					
					numberOfPickupsServedInBestNode = findNumberOfPickupsServed(bestBBNode);
					fractional = false;
				}
				
				// If the current best BBNode is fractional, branch and create two new child nodes
				else {
					// Finds which pickup nodes in the solution of the current BBnode that are fractional, and identifies the branchingPickupNode as the node that is most fractional
					ArrayList<Node> fractionalPickupNodes = findFractionalNodes(bestBBNode.MPsolutionVarsBBnode);
					Node branchingPickupNode = checkPickupFractionality(fractionalPickupNodes);
					
					// Remove bestBBnode from leaf nodes before branching
					leafNodes.remove(bestBBNode);
					
					// Add the branching node to the list of pickup nodes branched of the current BBNode
					bestBBNode.pickupNodesBranchedOn.add(branchingPickupNode.number);
					
					// Creating the left child based on the branching pickup node 
					BBNode leftChild = new BBNode(bestBBNode, bestBBNode.getDepth()+1, BBNodeIDcounter, vehicles, pickupNodes, branchingMatrixMaker(bestBBNode, branchingPickupNode, "left"), "left");
					
					BBNodeIDcounter += 1;
					
					// Adding the pickup nodes branched on from the parent node to the pickup nodes branched on of the child node
					for (int pickup : bestBBNode.pickupNodesBranchedOn) {
						leftChild.pickupNodesBranchedOn.add(pickup);	
					}
					
					// Print information about left child to file
					pw.append("-------------- LEFT CHILD RESULT ------------------- \n");
					pw.append("ID left child " + leftChild.getNodeId() + "\n");
					pw.append("Branching matrix left child: \n");
					for (int i = 0; i < leftChild.branchingMatrix.length; i++) {
					    for (int j = 0; j < leftChild.branchingMatrix[i].length; j++) {
					        pw.append(leftChild.branchingMatrix[i][j] + " ");
					    }
					    pw.append("\n");
					}
					
					// Solve the problem for the left child
					solveProblem(leftChild);
				
					// Creating the right child based on the branching pickup node 
					BBNode rightChild = new BBNode(bestBBNode, bestBBNode.getDepth()+1, BBNodeIDcounter, vehicles, pickupNodes, branchingMatrixMaker(bestBBNode, branchingPickupNode, "right"), "right");
					
					BBNodeIDcounter += 1;
					
					// Adding the pickup nodes branched on from the parent node to the pickup nodes branched on of the child node
					for (int pickup : bestBBNode.pickupNodesBranchedOn) {
						rightChild.pickupNodesBranchedOn.add(pickup);	
					}
					
					// Print information about right child to file
					pw.append("---------------- RIGHT CHILD RESULT ------------------- \n");
					pw.append("ID right child " + rightChild.getNodeId() + "\n");
					pw.append("Branching matrix right child: \n");
					for (int i = 0; i < rightChild.branchingMatrix.length; i++) {
					    for (int j = 0; j < rightChild.branchingMatrix[i].length; j++) {
					        pw.append(rightChild.branchingMatrix[i][j] + " ");
					    }
					    pw.append("\n");
					}
				
					// Solve the problem for the right child
					solveProblem(rightChild);
					
					// Add left and right child to leaf nodes
					leafNodes.add(leftChild);
					leafNodes.add(rightChild);
				}
			}
		}
			

		// Method resetting lambda variables that are forced to 0, back to between 0 and 1 (used in the end of the solveProblem method)
		public void resetIllegalLambdaVars(ArrayList<GRBVar>lambdaVars[]) throws Exception {
			for (Vehicle v : vehicles) {
				for (GRBVar var : lambdaVars[v.number]) {
					var.set(GRB.DoubleAttr.LB, 0);
					var.set(GRB.DoubleAttr.UB, 1);
					model.update();	
				}
			}
		}
		
		
		// Method checking whether a one or more of the lambda variables of a branch and bound node is fractional
		public boolean checkLambdaFractionality (BBNode bbNode) {
			for (double lambda : bbNode.lambdaValues) {
				if (lambda > 0 + zeroTol && lambda < 1 - zeroTol) {
					return true;
				}	
			}
			return false;	
		}
		
		
		// Method printing key numbers from each column generation run to a common file for all runs 
		public void printResults () {
			for(BBNode leafNode : leafNodes) {
				// Finding the objective of the best leaf node that is not integer
				if(checkLambdaFractionality(leafNode) && leafNode.getObjectiveValue() > bestLeafNodeObjective ) {
					bestLeafNodeObjective = leafNode.getObjectiveValue();
				}
				// Finding the objective of the best leaf node that is integer
				else if(!checkLambdaFractionality(leafNode) && leafNode.getObjectiveValue() > bestIntegerLeafNodeObjective) {
					bestIntegerLeafNodeObjective = leafNode.getObjectiveValue();
				}
			}
			
			
			
			// Print to results file
			fw.print(optimalObjective + ";" + rootNodeObjective + ";" + bestLeafNodeObjective + ";" + bestIntegerLeafNodeObjective + ";" + BBNodeIDcounter + ";" + numberOfRoutes + ";" + numberOfSubproblemCalls + ";" + builder.numberOfDominatedLabels + ";" + builder.numberOfPaths + ";" + numberOfPickupsServedInRootNode + ";" + numberOfPickupsServedInBestNode + ";" + totalTimeInMaster + ";" + totalTimeInSub + ";" + rootNodeTime + ";");
		}	
		
}

