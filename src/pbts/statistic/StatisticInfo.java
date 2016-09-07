package pbts.statistic;

import java.util.ArrayList;

public class StatisticInfo {
	ArrayList<Integer> timePoints = null;//new ArrayList<Integer>();
	ArrayList<Double> benefits = null;//new ArrayList<Double>();
	ArrayList<Double> travelDistances = null;//new ArrayList<Double>();
	ArrayList<Double> revenuePeoples = null;//new ArrayList<Double>();
	ArrayList<Double> revenueParcels = null;//new ArrayList<Double>();
	ArrayList<Double> costs = null;//new ArrayList<Double>();
	ArrayList<Double> discounts = null;//new ArrayList<Double>();
	ArrayList<Integer> peopleProcesseds = null;//new ArrayList<Integer>();
	ArrayList<Integer> parcelProcesseds = null;//new ArrayList<Integer>();
	
	public StatisticInfo(ArrayList<Integer> timePoints, 
			ArrayList<Double> benefits,
			ArrayList<Double> travelDistances, 
			ArrayList<Double> revenuePeoples,
			ArrayList<Double> revenueParcels, 
			ArrayList<Double> costs,
			ArrayList<Double> discounts, 
			ArrayList<Integer> peopleProcesseds, 
			ArrayList<Integer> parcelProcesseds){
		
		this.timePoints = timePoints;
		this.benefits = benefits;
		this.travelDistances = travelDistances;
		this.revenueParcels = revenueParcels;
		this.revenuePeoples = revenuePeoples;
		this.costs = costs;
		this.discounts = discounts;
		this.peopleProcesseds = peopleProcesseds;
		this.parcelProcesseds = parcelProcesseds;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
