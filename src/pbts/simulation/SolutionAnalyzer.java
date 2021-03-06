package pbts.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import pbts.entities.*;
import pbts.enums.VehicleAction;

import java.io.*;

class TimePeriod{
	int e;
	int l;
	public TimePeriod(int e, int l){ this.e = e; this.l = l;}
}
public class SolutionAnalyzer {

	/**
	 * @param args
	 */
	
	PrintWriter log = null;
	Simulator sim = null;
	
	HashMap<Integer, ArrayList<TimePeriod>> mParking2TaxiStop = null;
	
	public SolutionAnalyzer(Simulator sim){
		this.sim = sim;
		try{
			log = new PrintWriter("SolutionAnalyzer-log.txt");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void finalize(){
		log.close();
	}
	public String name(){ return "SolutionAnalyzer";}
	public AnalysisTemplate analyze(Vehicle taxi, ItineraryTravelTime I){
		//if(taxi.ID == 1){
			//System.out.println(I.get(0));
			//System.exit(-1);
		//}
		double benefits = 0;
		double distance = 0;
		double totalDiscount = 0;
		double revenuePassengers = 0;
		double revenueParcels = 0;
		int nbServedPassengers = 0;
		int nbServedParcels = 0;
		int nbSharedPeopleServices = 0;
		
		ArrayList<Integer> servedPeopleRequests = new ArrayList<Integer>();
		ArrayList<Integer> servedParcelRequests = new ArrayList<Integer>();
		
		double distancePeople = -1;
		int nbStopsBetweenPeopleService = 0;
		PeopleRequest lastPeopleRequest = null;
		for(int i = 0; i < I.size()-1; i++){
			int u = I.get(i);
			int v = I.get(i+1);
			Arc a = sim.map.getArc(u, v);
			if(a != null){
				//if(taxi.ID == 58 && i == 613){
					//System.out.println("SolutionAnalyzer::analyze, a = (" + u + "," + v + ")");
				//}
				distance = distance + a.w;
				// check travel time
				int t0 = I.getArrivalTime(i);
				int t1 = I.getDepartureTime(i);
				int t2 = I.getArrivalTime(i+1);
				int mint = sim.getTravelTime(a, sim.maxSpeedms)-1;
				int maxt = sim.getTravelTime(a, sim.minSpeedms)+1;
				if(t0 > 0 && t1 < t0){
					log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent at " + i + "th point " + u + ", " +
							"arrival time = " + t0 + " > departure time = " + t1);
					System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent at " + i + "th point " + u + ", " +
							"arrival time = " + t0 + " > departure time = " + t1);
					return null;
				}
				int t = t2 - t1;
				
				if(t < mint || t > maxt){
					log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent travel time of segment (" + u + "," + v 
							+ ") is " + t + " not in the bound [" + mint + "," + maxt + "]");
					System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent travel time of segment (" + u + "," + v 
							+ ") is " + t + " not in the bound [" + mint + "," + maxt + "], t1 = " + t1 + ", t2 = " + t2 + ", t = " + t);
					//return null;
				}
				
				if(distancePeople > -1) distancePeople = distancePeople + a.w;
			}else{// a is null, no arc (u,v)
				if(u != v){
					log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent, segment(" + u + "," + v + ") is not realistic");
					System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent, segment(" + u + "," + v + ") is not realistic");
					return null;
				}
			}
			/*
			if(a == null && u != v){
				log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent, segment(" + u + "," + v + ") is not realistic");
				System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent, segment(" + u + "," + v + ") is not realistic");
				return null;
			}
			if(a != null)distance = distance + a.w;
			VehicleAction act = I.getAction(i);
			
			// check travel time
			int t0 = I.getArrivalTime(i);
			int t1 = I.getDepartureTime(i);
			int t2 = I.getArrivalTime(i+1);
			int mint = sim.getTravelTime(a, sim.maxSpeedms);
			int maxt = sim.getTravelTime(a, sim.minSpeedms);
			if(t0 > 0 && t1 < t0){
				log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent at " + i + "th point " + u + ", " +
						"arrival time = " + t0 + " > departure time = " + t1);
				System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent at " + i + "th point " + u + ", " +
						"arrival time = " + t0 + " > departure time = " + t1);
				return null;
			}
			int t = t2 - t1;
			
			if(t < mint || t > maxt){
				log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent travel time of segment (" + u + "," + v 
						+ ") is " + t + " not in the bound [" + mint + "," + maxt + "]");
				System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent travel time of segment (" + u + "," + v 
						+ ") is " + t + " not in the bound [" + mint + "," + maxt + "], t1 = " + t1 + ", t2 = " + t2 + ", t = " + t);
				return null;
			}
			
			if(distancePeople > -1) distancePeople = distancePeople + a.w;
			*/
			
			
			VehicleAction act = I.getAction(i);
			if(act == VehicleAction.PICKUP_PEOPLE){
				int rid = I.getRequestID(i);
				servedPeopleRequests.add(rid);
				nbStopsBetweenPeopleService = 0;
				if(distancePeople > -1){
					log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent mor than one people served at the same time");
					System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent mor than one people served at the same time");
					return null;// inconsistent (more than two people in the taxi at the same time)
				}
				distancePeople = 0;
				if(a != null)// maybe pickup and delivery at the same point
					distancePeople = a.w;//0;
				PeopleRequest pr = sim.getPeopleRequest(rid);//sim.allPeopleRequests.get(rid);
				
				// check constraint on time window of pickup 
				int ta = I.getArrivalTime(i);
				int td = I.getDepartureTime(i);
				//if(td < pr.earlyPickupTime || td > pr.latePickupTime){
				if(ta < pr.earlyPickupTime || ta > pr.latePickupTime){
					System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + ", inconsistent of time window of pickup of people " +
							"" + pr.id + " arrival at " + u + " is " + ta + ", departure from " + u + " is " + td + 
							", WHILE earlyPickupsTime = " + pr.earlyPickupTime + " and latePickupTime = " + pr.latePickupTime);
					log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + ", inconsistent of time window of pickup of people " + pr.id);
					//return null;
				}
			
				if(lastPeopleRequest != null){
					System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + ", "
							+ " people request " + rid + ", inconsistent people-people sharing appears");
					
					return null;// people-people sharing appear
				}
				else lastPeopleRequest = pr;
			}else if(act == VehicleAction.DELIVERY_PEOPLE){
				int rid = I.getRequestID(i);
				PeopleRequest pr = sim.getPeopleRequest(rid);//sim.mPeopleRequest.get(rid);
				if(pr == null){
					log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent people request " + rid + " is not in the input");
					System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent people request " + rid + " is not in the input");
					return null;
				}
				/*
				// check constraint on maximum travel distance of a people request
				if(distancePeople > pr.maxTravelDistance){
					log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent travel distance of people " + rid + " = " +
							distancePeople + " > maxTravelDistance = " + pr.maxTravelDistance);
					System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent travel distance of people " + rid + " = " +
							distancePeople + " > maxTravelDistance = " + pr.maxTravelDistance);
					return null;
				}
				*/
				//if(a == null){
					//System.out.println("SolutionAnalyzer::analyze taxi " + taxi.ID + ", arc a = null at delivery point " + i + ", rid = " + rid);
				//}
				if(a != null)// maybe delivery and pickup at the same point
				distancePeople -= a.w;//important
				
				// check constraint on time window of delivery 
				int ta = I.getArrivalTime(i);
				int td = I.getDepartureTime(i);
				if((ta < pr.earlyDeliveryTime || ta > pr.lateDeliveryTime)){// &&
						//(td > pr.lateDeliveryTime || td < pr.earlyDeliveryTime)){
					log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent of time window of delivery of people request " + rid);
					System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + 
							" inconsistent of time window of delivery of people request " + rid + ", ta = "+ ta + 
							", par.earlyDeliveryTime = " + pr.earlyDeliveryTime + ", pr.lateDeliveryTime = " + pr.lateDeliveryTime);
					//return null;
				}
				
				Itinerary Ii = sim.dijkstra.queryShortestPath(pr.pickupLocationID, pr.deliveryLocationID);
				
				if(Ii == null){
					log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent no path from " + pr.pickupLocationID + 
							" to " + pr.deliveryLocationID + " of people request " + rid);
					System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent no path from " + pr.pickupLocationID + 
							" to " + pr.deliveryLocationID + " of people request " + rid);
					return null;
				}
				if(nbStopsBetweenPeopleService > 0) nbSharedPeopleServices++;
				//if(Math.abs(distancePeople-Ii.getDistance()) > 0.00001)
				totalDiscount += (distancePeople/Ii.getDistance()-1)*sim.gamma4;
				//System.out.println(name() + "::analyze(taxi = " + taxi.ID + 
						//", real distancePeople = " + distancePeople + ", direct distance people from " + 
						//pr.pickupLocationID + " to " + pr.deliveryLocationID + " is " + 
						//Ii.getDistance() + ", totalDiscount = " + totalDiscount);
				revenuePassengers = revenuePassengers + sim.alpha + Ii.getDistance()*sim.gamma1;
				distancePeople = -1;
				nbStopsBetweenPeopleService = -1;
				lastPeopleRequest = null;
			}else if(act == VehicleAction.PICKUP_PARCEL){
				int rid = I.getRequestID(i);
				servedParcelRequests.add(rid);
				
				//check constraint on time window of pickup
				int ta = I.getArrivalTime(i);
				int td = I.getDepartureTime(i);
				ParcelRequest pr = sim.getParcelRequest(rid);//sim.allParcelRequests.get(rid);
				boolean ok = (pr.earlyPickupTime <= ta && ta <= pr.latePickupTime) || 
						(pr.earlyPickupTime <= td && td <= pr.latePickupTime);
				if(!ok){
					log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent of time window of " +
							"pickup of parcel request " + rid + ", ta = " + ta + ", td = " + td + ", " +
									"early_pickup = " + pr.earlyPickupTime + ", late_pickup = " + pr.latePickupTime);
					System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent of time window of " +
							"pickup of parcel request " + rid + ", ta = " + ta + ", td = " + td + ", " +
							"early_pickup = " + pr.earlyPickupTime + ", late_pickup = " + pr.latePickupTime);
					//return null;
				}
				
				//check constraint on maximum stops between a people request
				if(nbStopsBetweenPeopleService >= 0) nbStopsBetweenPeopleService++;
				
				if(lastPeopleRequest != null){
					if(nbStopsBetweenPeopleService > lastPeopleRequest.maxNbStops){
						log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent number of stops between people request " + lastPeopleRequest.id + 
								" = " +
								nbStopsBetweenPeopleService + " > maxNbStop = " + lastPeopleRequest.maxNbStops);
						System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent number of stops between people request " + lastPeopleRequest.id + 
								" = " +
								nbStopsBetweenPeopleService + " > maxNbStop = " + lastPeopleRequest.maxNbStops);
						return null;
					}
					//if(nbStopsBetweenPeopleService > 0) nbSharedPeopleServices++;
				}
			}else if(act == VehicleAction.DELIVERY_PARCEL){
				int rid = I.getRequestID(i);
				//check constraint on time window of delivery 
				int ta = I.getArrivalTime(i);
				int td = I.getDepartureTime(i);
				ParcelRequest pr = sim.getParcelRequest(rid);// sim.allParcelRequests.get(rid);
				boolean ok = (pr.earlyDeliveryTime <= ta && ta <= pr.lateDeliveryTime) || 
						(pr.earlyDeliveryTime <= td && td <= pr.lateDeliveryTime);
				if(!ok){
					log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent of time window of " +
							"delivery of parcel request " + rid + ", ta = " + ta + ", td = " + td + ", early_delivery = " + pr.earlyDeliveryTime + 
							", late_delivery = " + pr.lateDeliveryTime);
					System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent of time window of " +
							"delivery of parcel request " + rid + ", ta = " + ta + ", td = " + td + ", early_delivery = " + pr.earlyDeliveryTime + 
							", late_delivery = " + pr.lateDeliveryTime);
					//return null;
				}

				//check constraint on maximum stops between a people request
				if(nbStopsBetweenPeopleService >= 0) nbStopsBetweenPeopleService++;
				if(lastPeopleRequest != null)
					if(nbStopsBetweenPeopleService > lastPeopleRequest.maxNbStops){
						log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent number of stops between people request " + lastPeopleRequest.id + 
								" = " +
								nbStopsBetweenPeopleService + " > maxNbStop = " + lastPeopleRequest.maxNbStops);
						System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent number of stops between people request " + lastPeopleRequest.id + 
								" = " +
								nbStopsBetweenPeopleService + " > maxNbStop = " + lastPeopleRequest.maxNbStops);
						
						return null;
					}
			}else if(act == VehicleAction.STOP){
				// process late
			}else if(act == VehicleAction.PASS){
				//System.out.println("SolutionAnalyzer::analyzeItinerary --> action = PASS");
				
			}else{
				
				log.println("SolutionAnalyzer::analyzeItinerary exception unknown action");
				System.out.println("SolutionAnalyzer::analyzeItinerary exception unknown action " + taxi.getActionDescription(act));
				//return null;
			}
		}
		
		//collect information of taxi-time stopping at parkings
		for(int i = 0; i < I.size(); i++){
			int u = I.get(i);
			VehicleAction act = I.getAction(i);
			if(act == VehicleAction.STOP){
				ArrayList<TimePeriod> L = mParking2TaxiStop.get(u);
				if(L == null){
					L = new ArrayList<TimePeriod>();
					mParking2TaxiStop.put(u, L);
				}
				int ta = I.getArrivalTime(i);
				int td = I.getDepartureTime(i);
				if(td == -1) td = sim.terminateWorkingTime;
				L.add(new TimePeriod(ta,td));
			}
		}
		
		for(int i = 0; i < servedParcelRequests.size(); i++){
			int rid = servedParcelRequests.get(i);
			ParcelRequest pr = sim.mParcelRequest.get(rid);
			Itinerary Ii = sim.dijkstra.queryShortestPath(pr.pickupLocationID, pr.deliveryLocationID);;//sim.dijkstra.solve(pr.pickupLocationID, pr.deliveryLocationID);
			
			if(Ii == null){
				log.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent no path from " + pr.pickupLocationID + " to " + 
						pr.deliveryLocationID + " of parcel request " + pr.id);
				System.out.println("SolutionAnalyzer::analyzeItinerary, taxi " + taxi.ID + " inconsistent no path from " + pr.pickupLocationID + " to " + 
						pr.deliveryLocationID + " of parcel request " + pr.id);
				return null;
			}
			revenueParcels = revenueParcels + Ii.getDistance()*sim.gamma2 + sim.beta;
		}
		double fuel = distance*sim.gamma3;
		//System.out.println("distance = " + distance + ", gamma3 = " + sim.gamma3 + ", fuel = " + fuel);
		//System.exit(-1);
		benefits = revenuePassengers + revenueParcels - fuel - totalDiscount;
		
		AnalysisTemplate at = new AnalysisTemplate();
		at.travelDistance = distance*0.001;// in km
		at.discount = totalDiscount*0.001;// in K
		at.revenuePassengers = revenuePassengers*0.001;// in K
		at.revenueParcels = revenueParcels*0.001;// in K
		at.fuelCost = fuel*0.001;// in K
		at.benefits = benefits*0.001;
		at.nbServedParcels = servedParcelRequests.size();
		at.nbServedPassengers = servedPeopleRequests.size();
		at.nbSharedPeopleService = nbSharedPeopleServices;
		return at;
	}
	
	private boolean check(ArrayList<TimePeriod> L, int cap){
		for(int i = 0; i < L.size(); i++){
			TimePeriod p = L.get(i);
			int ce = 0;
			int cl = 0;
			for(int j = 0; j < L.size(); j++){
				TimePeriod pj = L.get(j);
				if(pj.e <= p.e && p.e <= pj.l) ce++;
				if(pj.e <= p.l && p.l <= pj.l) cl++;
			}
			if(ce > cap || cl > cap){
				System.out.println("SolutionAnalyzer::check failed timeperiod " + p.e + "-" + p.l + " --> ce = " + ce + ", cl = " + cl + ", cap = " + cap);
				return false;
			}
		}
		return true;
	}
	public AnalysisTemplate analyzeSolution(HashMap<Integer, ItineraryTravelTime> itineraries){
		double totalBenefits = 0;
		mParking2TaxiStop = new HashMap<Integer, ArrayList<TimePeriod>>();
		AnalysisTemplate AT = new AnalysisTemplate();
		for(int k = 0; k < sim.vehicles.size(); k++){
			Vehicle taxi = sim.vehicles.get(k);
			ItineraryTravelTime I = itineraries.get(taxi.ID);
			if(I != null){
				AnalysisTemplate at = analyze(taxi,I);
				if(at == null){
					System.out.println("SolutionAnalyzer::analyzeSolution at = null????");
					return null;
				}
				AT.benefits += at.benefits;
				AT.discount += at.discount;
				AT.travelDistance += at.travelDistance;
				AT.fuelCost += at.fuelCost;
				AT.nbServedParcels += at.nbServedParcels;
				AT.nbServedPassengers += at.nbServedPassengers;
				AT.revenueParcels += at.revenueParcels;
				AT.revenuePassengers += at.revenuePassengers;
				AT.nbSharedPeopleService += at.nbSharedPeopleService;
				
				
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", benefits = " + at.benefits);
				/*
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", discount = " + at.discount);
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", travel distance = " + at.travelDistance);
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", fuel = " + at.fuelCost);
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", served parcels = " + at.nbServedParcels);
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", served people = " + at.nbServedPassengers);
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", revenue parcel = " + at.revenueParcels);
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", revenue people = " + at.revenuePassengers);
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", number of shared people services = " + at.nbSharedPeopleService);
				*/
			}
		}
		
		//check compatibility of capacity of parkings
		for(int i = 0; i < sim.lstParkings.size(); i++){
			Parking P = sim.lstParkings.get(i);
			ArrayList<TimePeriod> L = mParking2TaxiStop.get(P.locationID);
			if(L != null){
				if(!check(L,P.capacity)){
					System.out.print("Checking Parking " + P.locationID + ", cap " + P.capacity + " : ");
					for(int j = 0; j < L.size(); j++){
						TimePeriod tp = L.get(j);
						System.out.print(tp.e + "-" + tp.l + "\t");
					}
					System.out.println("");
				//if(!check(L,1000000)){
					System.out.println("SolutionAnalyzer::analyzeSolution check capacity constraint of parking failed?????");
					//return null;
				}
			}
		}
		log.println("total bnefits = " + totalBenefits);
		
		System.out.println("analyze solution --> total benefits = " + AT.benefits);
		System.out.println("analyze solution --> total discount = " + AT.discount);
		System.out.println("analyze solution --> total travel distance = " + AT.travelDistance);
		System.out.println("analyze solution --> total fuel = " + AT.fuelCost);
		System.out.println("analyze solution --> total served parcels = " + AT.nbServedParcels);
		System.out.println("analyze solution --> total served people = " + AT.nbServedPassengers);
		System.out.println("analyze solution --> total revenue parcel = " + AT.revenueParcels);
		System.out.println("analyze solution --> total revenue people = " + AT.revenuePassengers);
		System.out.println("analyze solution --> number of shared people services = " + AT.nbSharedPeopleService);
		System.out.println("analyze solution --> number of taxis pick up people when on board = " + sim.nbTaxisPickUpPeopleOnBoard);
		System.out.println("analyze solution --> number of taxis pick up parcel when on board = " + sim.nbTaxisPickUpParcelOnBoard);
		
		return AT;
	}

	public AnalysisTemplate analyzeSolution(HashMap<Integer, ItineraryTravelTime> itineraries, String filename){
		double totalBenefits = 0;
		mParking2TaxiStop = new HashMap<Integer, ArrayList<TimePeriod>>();
		AnalysisTemplate AT = new AnalysisTemplate();
		for(int k = 0; k < sim.vehicles.size(); k++){
			Vehicle taxi = sim.vehicles.get(k);
			ItineraryTravelTime I = itineraries.get(taxi.ID);
			if(I != null){
				AnalysisTemplate at = analyze(taxi,I);
				if(at == null){
					System.out.println("SolutionAnalyzer::analyzeSolution at = null????");
					return null;
				}
				AT.benefits += at.benefits;
				AT.discount += at.discount;
				AT.travelDistance += at.travelDistance;
				AT.fuelCost += at.fuelCost;
				AT.nbServedParcels += at.nbServedParcels;
				AT.nbServedPassengers += at.nbServedPassengers;
				AT.revenueParcels += at.revenueParcels;
				AT.revenuePassengers += at.revenuePassengers;
				AT.nbSharedPeopleService += at.nbSharedPeopleService;
				
				
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", benefits = " + at.benefits);
				/*
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", discount = " + at.discount);
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", travel distance = " + at.travelDistance);
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", fuel = " + at.fuelCost);
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", served parcels = " + at.nbServedParcels);
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", served people = " + at.nbServedPassengers);
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", revenue parcel = " + at.revenueParcels);
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", revenue people = " + at.revenuePassengers);
				System.out.println("analyze solution --> taxi "+ taxi.ID + ", number of shared people services = " + at.nbSharedPeopleService);
				*/
			}
		}
		
		//check compatibility of capacity of parkings
		for(int i = 0; i < sim.lstParkings.size(); i++){
			Parking P = sim.lstParkings.get(i);
			ArrayList<TimePeriod> L = mParking2TaxiStop.get(P.locationID);
			if(L != null){
				if(!check(L,P.capacity)){
					System.out.print("Checking Parking " + P.locationID + ", cap " + P.capacity + " : ");
					for(int j = 0; j < L.size(); j++){
						TimePeriod tp = L.get(j);
						System.out.print(tp.e + "-" + tp.l + "\t");
					}
					System.out.println("");
				//if(!check(L,1000000)){
					System.out.println("SolutionAnalyzer::analyzeSolution check capacity constraint of parking failed?????");
					//return null;
				}
			}
		}
		log.println("total bnefits = " + totalBenefits);
		
		System.out.println("analyze solution --> total benefits = " + AT.benefits);
		System.out.println("analyze solution --> total discount = " + AT.discount);
		System.out.println("analyze solution --> total travel distance = " + AT.travelDistance);
		System.out.println("analyze solution --> total fuel = " + AT.fuelCost);
		System.out.println("analyze solution --> total served parcels = " + AT.nbServedParcels);
		System.out.println("analyze solution --> total served people = " + AT.nbServedPassengers);
		System.out.println("analyze solution --> total revenue parcel = " + AT.revenueParcels);
		System.out.println("analyze solution --> total revenue people = " + AT.revenuePassengers);
		System.out.println("analyze solution --> number of shared people services = " + AT.nbSharedPeopleService);
		System.out.println("analyze solution --> number of taxis pick up people when on board = " + sim.nbTaxisPickUpPeopleOnBoard);
		System.out.println("analyze solution --> number of taxis pick up parcel when on board = " + sim.nbTaxisPickUpParcelOnBoard);
		
		try{
			PrintWriter out= new PrintWriter(filename);
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<results>");
			out.println("<total-benefits>" + AT.benefits + "</total-benefits>");
			out.println("<total-discount>" + AT.discount + "</total-discount>");
			out.println("<travel-distance>" + AT.travelDistance + "</travel-distance>");
			out.println("<fuel>" + AT.fuelCost + "</fuel>");
			out.println("<served-parcels>" + AT.nbServedParcels + "</served-parcels>");
			out.println("<served-people>" + AT.nbServedPassengers + "</served-people>");
			out.println("<total-parcels>" + sim.totalParcelRequests + "</total-parcels>");
			out.println("<total-people>" + sim.totalPeopleRequests + "</total-people>");
			out.println("<wait-parcel>" + sim.nbParcelWaitBoarding + "</wait-parcel>");
			out.println("<revenue-parcel>" + AT.revenueParcels + "</revenue-parcel>");
			out.println("<revenue-people>" + AT.revenuePassengers + "</revenue-people>");
			out.println("<shared-people-services>" + AT.nbSharedPeopleService + "</shared-people-services>");
			out.println("<nbTaxisPickUpOnBoard-people>" + sim.nbTaxisPickUpPeopleOnBoard + "</nbTaxisPickUpOnBoard-people>");
			out.println("<nbTaxisPickUpOnBoard-parcel>" + sim.nbTaxisPickUpParcelOnBoard + "</nbTaxisPickUpOnBoard-parcel>");
			out.println("</results>");
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return AT;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//SolutionAnalyzer SA = new SolutionAnalyzer(null);
		//SA.genPlotFile("statistic-greedy-sharing.txt","statistic_benefits.m","statistic_cost.m", "statistic_distance.m", "statistic_requests.m", 500);
	}
	
}
