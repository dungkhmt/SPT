package pbts.prediction.behrouz.runners;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import pbts.simulation.SimulatorTimeUnit;
import pbts.simulation.Simulator;
import pbts.entities.LatLng;
import pbts.prediction.behrouz.fixedrate.FixedRateSampler;

public class Runner {
	private FixedRateSampler frs;
	private SimulatorTimeUnit sim;
	String dir;
	public Runner(){
		frs = new FixedRateSampler();
		sim = new SimulatorTimeUnit();
		dir = "E:\\task2\\git_project\\SPT\\";
		String mapFileName = dir + "SanFrancisco_std\\SanfranciscoRoad-connected-contracted-5-refine-50.txt";
		sim.loadMapFromTextFile(mapFileName);
		
	}
	public ArrayList getRequestInPeriod(int period){
		if(period >= 0 && period < 96)
			return frs.getRequests(period);
		return null;
	}
	
	public int percentageAppears(ArrayList<Integer> popularRq, ArrayList<Integer> allRq){
		
		int t = 0;
		for(int i = 0; i < popularRq.size(); i++){
			if(allRq.indexOf(popularRq.get(i)) >= 0)
				t++;
		}
		/*System.out.println("\n");
		System.out.println("The number of all requests at period: " + allRq.size());
		System.out.println("The percentage appears of popular requests in all request list at period: " + t + "/" + popularRq.size());*/
		return t;
	}
	public void computeSimilarity(){
		String fileName = dir + "predictionInfo.txt";
		try{
			PrintWriter out= new PrintWriter(fileName);
			out.println("Period TotalRequest TotalPopularRequest Occurences Percent");
			for(int period = 0;period < 96; period++){
				
				ArrayList<Integer> popularRqId;
				
				popularRqId = getRequestInPeriod(period);
				ArrayList<Integer> allRqIdAtPeriod = new ArrayList<Integer>();
				
				for(int i = 1; i <= 30; i++){
					String rqFileName = dir + "SanFrancisco_std\\ins_day_" + i + "_minSpd_5_maxSpd_60.txt";
					try {
						Scanner in = new Scanner(new File(rqFileName));
						String str = in.nextLine();
						// System.out.println("people request str = " + str);
						while (true) {
							int id = in.nextInt();
							// System.out.println("people id = " + id);
							if (id == -1)
								break;
							int timePoint = in.nextInt();
							if(timePoint >= period*900 && timePoint < (period + 1)*900){
								int parcelPickupLocId = in.nextInt();
								allRqIdAtPeriod.add(parcelPickupLocId);
							}
							str = in.nextLine();
						}
						str = in.nextLine();
						str = in.nextLine();
						// System.out.println("parcel request str = " + str);
						while (true) {
							int id = in.nextInt();
							// System.out.println("parcel request id = " + id);
							if (id == -1)
								break;
							int timePoint = in.nextInt();
							if(timePoint >= period*900 && timePoint < (period + 1)*900){
								int peoplePickupLocId = in.nextInt();
								allRqIdAtPeriod.add(peoplePickupLocId);
							}
							str = in.nextLine();
						}
						in.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				int occurences = percentageAppears(popularRqId, allRqIdAtPeriod);
				out.println(period + " " + allRqIdAtPeriod.size() + " " + popularRqId.size() + " " + occurences + " " + occurences*100/popularRqId.size());
				
				/*System.out.println("\n");
				System.out.println("Popular Pickup locations at period " + period + ":");
				for (int pickup_point : PopularRequestsId)
					System.out.print(pickup_point + "\t");
				System.out.println("\n");
				System.out.println("All pickup locations at period " + period + ":");
				for (int all_pickup_point : allRqIdAtPeriod)
					System.out.print(all_pickup_point + "\t");
				System.out.println("\n");
				System.out.println("Percentage of appearance at " + period + ": " + per + "%");
				System.out.println("\n");*/
			}
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void computeShortestDistance(int day){
		String str;
		int id;
		int timePoint;
		int pickupLocationID;
		int deliveryLocationID;
		double travelDis;
		int period;
		double shortestDis = 1000000;
		int nearestPointId = -1;
		int ppId;
		double D;
		ArrayList<Integer> popularRqId;
		
		ArrayList<ArrayList<Integer>> listRqPeriod = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < 96; i++){
			listRqPeriod.add(getRequestInPeriod(i));
		}
		
		String fileName = dir + "shortestDistanceDay-" + day + ".txt";
		try{
			PrintWriter out= new PrintWriter(fileName);
			out.println("period rqPointId travelDistance popularPointId distance");
			
			String rqFileName = dir + "SanFrancisco_std\\ins_day_" + day + "_minSpd_5_maxSpd_60.txt";
			try {
				Scanner in = new Scanner(new File(rqFileName));
				str = in.nextLine();
				
				while (true) {
					id = in.nextInt();
					// System.out.println("people id = " + id);
					if (id == -1)
						break;
					timePoint = in.nextInt();
					pickupLocationID = in.nextInt();
					deliveryLocationID = in.nextInt();
					travelDis = sim.estimateTravelingDistance(pickupLocationID, deliveryLocationID);
					//double travelDis = sim.dijkstra.queryDistance(pickupLocationID, deliveryLocationID);
					
					period = timePoint / 900;
					popularRqId = listRqPeriod.get(period);
					shortestDis = 1000000;
					nearestPointId = -1;
					for(int i = 0; i < popularRqId.size(); i++){
						ppId = popularRqId.get(i);
						D = sim.estimateTravelingDistance(ppId, pickupLocationID);
						//double D = sim.dijkstra.queryDistance(ppId, pickupLocationID);
						if(shortestDis > D){
							shortestDis = D;
							nearestPointId = ppId;
						}
						//System.out.println(i);
					}
					out.println(period + " " + pickupLocationID + " " + travelDis + " " + nearestPointId + " " + shortestDis);
					System.out.println("period: " + period + " , pickupId: " + pickupLocationID + " , travel distance: " + travelDis + " , nearest Point: " + nearestPointId + " , distance: " + shortestDis);
					str = in.nextLine();
				}
				str = in.nextLine();
				str = in.nextLine();
				System.out.println("people request");
				/*while (true) {
					int id = in.nextInt();
					// System.out.println("parcel request id = " + id);
					if (id == -1)
						break;
					int timePoint = in.nextInt();
					int pickupLocationID = in.nextInt();
					int deliveryLocationID = in.nextInt();
					double travelDis = sim.dijkstra.queryDistance(pickupLocationID, deliveryLocationID);
					
					int period = timePoint / 900;
					ArrayList<Integer> popularRqId;
					popularRqId = getRequestInPeriod(period);
					double shortestDis = 1000000;
					int nearestPointId = -1;
					for(int i = 0; i < popularRqId.size(); i++){
						int ppId = popularRqId.get(i);
						double D = sim.dijkstra.queryDistance(ppId, pickupLocationID);
						if(shortestDis > D){
							shortestDis = D;
							nearestPointId = ppId;
						}
					}
					out.println(period + " " + pickupLocationID + " " + travelDis + " " + nearestPointId + " " + shortestDis);
					str = in.nextLine();
				}*/
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		String[] weekdays = {
			"Monday", "Tuesday", "Wednesday",
			"Thursday", "Friday", "Saturday",
			"Sunday"
		};
		
		FixedRateSampler frs = new FixedRateSampler();
		Runner run = new Runner();
		run.computeShortestDistance(30);
		run.computeSimilarity();
		
		/*int dayOfWeek = 2;
		int period = 0;
		ArrayList<Integer> requests;
		requests = frs.getRequests(period);
		
		
		System.out.println("Pickup locations at period " + period + ":");
		for (int pickup_point : requests)
			System.out.print(pickup_point + "\t");
		System.out.println("\n");
		
		requests = frs.getRequests(dayOfWeek, period);
		System.out.println("Pickup locations at period " + period + " on " + weekdays[dayOfWeek] + ":");
		for (int pickup_point : requests)
			System.out.print(pickup_point + "\t");
		System.out.println();*/
	}

}
