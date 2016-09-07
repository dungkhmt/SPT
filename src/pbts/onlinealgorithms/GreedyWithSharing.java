package pbts.onlinealgorithms;

import pbts.entities.Arc;
import pbts.entities.ItineraryTravelTime;
import pbts.entities.ParcelRequest;
import pbts.entities.PeopleRequest;
import pbts.entities.Vehicle;
import pbts.enums.VehicleStatus;
import pbts.simulation.ServiceSequence;

import java.io.*;
import java.util.ArrayList;

import pbts.simulation.*;
public class GreedyWithSharing implements OnlinePlanner {

	public PrintWriter log = null;
	public SimulatorBookedRequests sim = null;
	public GreedyWithSharing(SimulatorBookedRequests sim){
		this.sim = sim;
		this.log = sim.log;
	}
	
	public ServiceSequence computeBestProfitsParcelInsertion(Vehicle taxi, ParcelRequest pr){
		int startIdx = 0; 
		int idxDelivery = -1;
		int rid = -1;
		int nextStartPoint = taxi.getNextStartPoint();
		int startTimePoint = taxi.getNextStartTimePoint();
		
		//if nextStartPoint is a location of a request, then startIdx must be 1
		if(taxi.remainRequestIDs.size() > 0){
			int frid = taxi.remainRequestIDs.get(0);
			frid = Math.abs(frid);
			boolean ok = false;
			PeopleRequest peopleReq = sim.mPeopleRequest.get(frid);
			if(peopleReq != null){
				if(nextStartPoint == peopleReq.pickupLocationID || nextStartPoint == peopleReq.deliveryLocationID)
					ok = true;
			}
			if(!ok){
				ParcelRequest parcelReq = sim.mParcelRequest.get(frid);
				if(parcelReq != null){
					if(nextStartPoint == parcelReq.pickupLocationID || nextStartPoint == parcelReq.deliveryLocationID)
						ok = true;
				}
			}
			if(ok) startIdx = 1;
		}
		
		PeopleRequest peopleReq = null;
		if(taxi.peopleReqIDonBoard.size() > 0){
			rid = taxi.peopleReqIDonBoard.get(0);
			peopleReq = sim.mPeopleRequest.get(rid);
			
			for(int i = 0; i < taxi.remainRequestIDs.size(); i++){
				if(taxi.remainRequestIDs.get(i) == -rid){
					idxDelivery = i; break;
				}
			}
			if(sim.countStop.get(rid) + idxDelivery >= peopleReq.maxNbStops){
				startIdx = idxDelivery+1;
			}
		}
		
		
		int[] nod = new int[taxi.remainRequestIDs.size() + 2];
		// explore all possible position i1 (pickup), i2 (delivery) among taxi.remainRequestIDs for inserting pr
		ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		double maxProfits = -sim.dijkstra.infinity;
		//int sel_pickup_index = -1;
		//int sel_delivery_index = -1;
		//int sel_pk = -1;
		ServiceSequence ss = null;
		double expectDistanceParcel = sim.dijkstra.queryDistance(pr.pickupLocationID, pr.deliveryLocationID);
		for(int i1 = startIdx; i1 <= taxi.remainRequestIDs.size(); i1++){
			for(int i2 = i1; i2 <= taxi.remainRequestIDs.size(); i2++){
				// establish new sequence of request ids stored in nod
				if(rid > 0){
					if(i1 <= idxDelivery && i2 <= idxDelivery)// && sim.countStop.get(rid) + idxDelivery + 2 > peopleReq.maxNbStops)
						continue;
				}
				int idx = -1;
				double profits = sim.getParcelRevenue(expectDistanceParcel);
				for(int k1 = 0; k1 < i1; k1++){
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(k1);
				}
				idx++;
				nod[idx] = pr.id;// insert pickup
				for(int k1 = 0; k1 < i2-i1; k1++){
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(i1 + k1);
				}
				idx++;
				nod[idx] = -pr.id;// insert delivery
				for(int k1 = i2; k1 < taxi.remainRequestIDs.size(); k1++){
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(k1);
				}
				if(!sim.checkCapacity(taxi.parcelReqIDonBoard.size(), nod, Simulator.Qk)) continue;
				
				// evaluate the insertion
				double D = sim.computeFeasibleDistance(nextStartPoint, startTimePoint, nod);
				if(D > sim.dijkstra.infinity - 1) continue;// constraints are violated
				if(D + taxi.totalTravelDistance > sim.maxTravelDistance) continue;
				for(int k = 0; k < parkings.size(); k++){
					int pk = parkings.get(k);
					//double D = computeFeasibleDistance(nextStartPoint, startTimePoint, nod, pk);
					//if(D > dijkstra.infinity - 1) continue;// constraints are violated
					int endPoint = sim.getLocationFromEncodedRequest(nod[nod.length-1]);
					D = D + sim.dijkstra.queryDistance(endPoint, pk);
					double extraDistance = D - taxi.remainDistance;
					profits = profits - sim.getCostFuel(extraDistance);
					if(profits > maxProfits){
						maxProfits = profits;
						//sel_pickup_index = i1;
						//sel_delivery_index = i2;
						//sel_pk = pk;   
						ss = new ServiceSequence(nod,profits,pk,D);
					}
				}
			}
		}
		
		return ss;
	}
	public String name(){ return "GreedyWithSharing";}
	public ItineraryServiceSequence computeBestProfitsItineraryPeopleInsertion(Vehicle taxi, int nextStartTimePoint, 
			int fromIndex, int fromPoint, PeopleRequest pr){
		// compute best added itinerary when pr is inserted into taxi
		ServiceSequence ss = computeBestProfitsPeopleInsertion(taxi, pr);
		if(ss == null) return null;
		//int taxiID = 47;
		int reqID = -1;
		if(taxi.currentItinerary != null)
			reqID = taxi.currentItinerary.getRequestID(fromIndex);
		if(taxi.ID == sim.debugTaxiID){
			sim.log.println(name() + "::computeBestProfitItineraryPeopleInsertion DEBUG, " + 
		"begin call to sim.establishItinerary(" + nextStartTimePoint + "," + fromIndex + "," + 
		reqID + "," + fromPoint + ")");
		}
		ItineraryTravelTime I = sim.establishItinerary(taxi, nextStartTimePoint,fromIndex,
			reqID,fromPoint,ss);
		if(I == null) return null;
		I.setDistance(ss.distance);
		
		if(I.getDistance() + taxi.totalTravelDistance > sim.maxTravelDistance) return null;
		
		return new ItineraryServiceSequence(taxi, I, ss);
	}
	/*
	public ItineraryServiceSequence computeBestProfitsItineraryParcelInsertion(Vehicle taxi, 
			int nextStartTimePoint, 
			int fromIndex, int fromPoint, ParcelRequest pr){
		// compute best added itinerary when pr is inserted into taxi
		ServiceSequence ss = computeBestProfitsParcelInsertion(taxi, pr);
		if(ss == null) return null;
		
		int reqID = -1;
		if(taxi.currentItinerary != null)
			reqID = taxi.currentItinerary.getRequestID(fromIndex);
		
		ItineraryTravelTime I = sim.establishItinerary(taxi, nextStartTimePoint,fromIndex,
			reqID,fromPoint,ss);
		if(I == null) return null;
		return new ItineraryServiceSequence(taxi, I, ss);
	}
	*/
	public ServiceSequence computeBestProfitsPeopleInsertion(Vehicle taxi, PeopleRequest pr){
		int startIdx = 0;
		//int taxiID = 1;
		int nextStartPoint = taxi.getNextStartPoint();
		int startTimePoint = taxi.getNextStartTimePoint();
		
		//if nextStartPoint is a location of a request, then startIdx must be 1
				if(taxi.remainRequestIDs.size() > 0){
					int frid = taxi.remainRequestIDs.get(0);
					frid = Math.abs(frid);
					boolean ok = false;
					PeopleRequest peopleReq = sim.mPeopleRequest.get(frid);
					if(peopleReq != null){
						if(nextStartPoint == peopleReq.pickupLocationID || nextStartPoint == peopleReq.deliveryLocationID)
							ok = true;
					}
					if(!ok){
						ParcelRequest parcelReq = sim.mParcelRequest.get(frid);
						if(parcelReq != null){
							if(nextStartPoint == parcelReq.pickupLocationID || nextStartPoint == parcelReq.deliveryLocationID)
								ok = true;
						}
					}
					if(ok) startIdx = 1;
				}
				
		if(taxi.peopleReqIDonBoard.size() > 0){
			// taxi is carrying a passenger
			int rid = taxi.peopleReqIDonBoard.get(0);
			for(int i = 0;  i < taxi.remainRequestIDs.size(); i++){
				if(taxi.remainRequestIDs.get(i) == -rid){
					startIdx = i+1; break;
				}
			}
			if(taxi.ID == sim.debugTaxiID){
				System.out.println("SimulatorBookedRequest::computeBestProfitsPeopleInsertion, " +
						"request on board = " + rid + ", remainRequestIDs = " + taxi.getRemainRequestID() + ", startIdx = " + startIdx + ", new people quest = " + pr.id);
				log.println("SimulatorBookedRequest::computeBestProfitsPeopleInsertion, " +
						"request on board = " + rid + ", remainRequestIDs = " + taxi.getRemainRequestID() + ", startIdx = " + startIdx + ", new people quest = " + pr.id);
				
			}
		}
		
		
		
		int[] nod = new int[taxi.remainRequestIDs.size() + 2];
		// explore all possible position i1 (pickup), i2 (delivery) among taxi.remainRequestIDs for inserting pr
		ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		//double minExtraDistance = dijkstra.infinity;
		//int sel_pickup_index = -1;
		//int sel_delivery_index = -1;
		//int sel_pk = -1;
		double maxProfits = -sim.dijkstra.infinity;
		ServiceSequence ss = null;
		double expectDistancePeople = sim.dijkstra.queryDistance(pr.pickupLocationID, pr.deliveryLocationID);
		for(int i1 = startIdx; i1 <= taxi.remainRequestIDs.size(); i1++){
			int maxI2 = taxi.remainRequestIDs.size() < i1 + pr.maxNbStops ? taxi.remainRequestIDs.size() : i1 + pr.maxNbStops;
			for(int i2 = i1; i2 <= maxI2; i2++){
			//for(int i2 = i1; i2 <= taxi.remainRequestIDs.size(); i2++){
			//for(int i2 = i1; i2 <= i1; i2++){
				// establish new sequence of request ids stored in nod
				int idx = -1;
				int pickup_idx = -1;
				int delivery_idx = -1;
				double profits = sim.getPeopleRevenue(expectDistancePeople);
				for(int k1 = 0; k1 < i1; k1++){
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(k1);
				}
				idx++;
				nod[idx] = pr.id;// insert pickup
				pickup_idx = idx;
				for(int k1 = 0; k1 < i2-i1; k1++){
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(i1 + k1);
				}
				idx++;
				nod[idx] = -pr.id;// insert delivery
				delivery_idx = idx;
				for(int k1 = i2; k1 < taxi.remainRequestIDs.size(); k1++){
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(k1);
				}
				
				if(!sim.checkCapacity(taxi.parcelReqIDonBoard.size(), nod, Simulator.Qk)) continue;
				
				// compute the distance of passenger service
				double distancePeople = 0;
				for(int k1 = pickup_idx; k1 < delivery_idx; k1++){
					int u = sim.getLocationFromEncodedRequest(nod[k1]);
					int v = sim.getLocationFromEncodedRequest(nod[k1+1]);
					distancePeople = distancePeople + sim.dijkstra.queryDistance(u,v);
				}
				if(distancePeople > pr.maxTravelDistance) continue;
				
				// check if travel distance of passenger on board exceeds maximum distance allowed
				boolean ok = true;
				for(int k1 = 0; k1 < taxi.peopleReqIDonBoard.size(); k1++){
					int pobReqID = taxi.peopleReqIDonBoard.get(k1);
					PeopleRequest pR = sim.mPeopleRequest.get(pobReqID);
					double d1 = sim.computeRemainTravelDistance(pobReqID,nod,nextStartPoint);
					Arc A = sim.map.getArc(taxi.lastPoint, nextStartPoint);
					double d2 = d1 + sim.accumulateDistance.get(pobReqID);
					if(A != null) d2 = d2 + A.w;
					if(d2 > pR.maxTravelDistance){
						ok = false; break;
					}
				}
				if(!ok) continue;
				
				if(pickup_idx < delivery_idx - 1)// not direct delivery
					profits = profits - sim.getDiscount(expectDistancePeople, distancePeople);
				
				// evaluate the insertion
				
				double D = sim.computeFeasibleDistance(nextStartPoint, startTimePoint, nod);
				if(D > sim.dijkstra.infinity - 1) continue;// constraints are violated
				if(D + taxi.totalTravelDistance > sim.maxTravelDistance) continue;
				for(int k = 0; k < parkings.size(); k++){
					int pk = parkings.get(k);
					
					//double D = computeFeasibleDistance(nextStartPoint, startTimePoint, nod, pk);
					//if(D > dijkstra.infinity - 1) continue;// constraints are violated
					int endPoint = sim.getLocationFromEncodedRequest(nod[nod.length-1]);
					D = D + sim.dijkstra.queryDistance(endPoint, pk);
					double extraDistance = D - taxi.remainDistance;
					profits = profits - sim.getCostFuel(extraDistance);
					
					if(profits > maxProfits){
						maxProfits = profits;
						//sel_pickup_index = i1;
						//sel_delivery_index = i2;
						//sel_pk = pk;
						ss = new ServiceSequence(nod,profits,pk,D);
						/*
						System.out.println("SimulatorBookedRequest::computeBestPeopleInsertion, UPDATE ss = " + ss.profitEvaluation +
								", sequence = " + ss.getSequence() + ", distance D = " + ss.distance);
						log.println("SimulatorBookedRequest::computeBestPeopleInsertion, UPDATE ss = " + ss.profitEvaluation +
								", sequence = " + ss.getSequence() + ", distance D = " + ss.distance);
								*/
					}
				}
			}
		}
		
		return ss;
	}
	
	public void processPeopleRequest(PeopleRequest pr){
		//int taxiID = 727;
		ItineraryServiceSequence sel_IS = null;
		for(int k = 0; k < sim.vehicles.size(); k++){
			Vehicle taxi = sim.vehicles.get(k);
			if(taxi.totalTravelDistance > sim.maxTravelDistance) continue;
			
			if(taxi.status == VehicleStatus.STOP_WORK) continue;
			if(taxi.remainRequestIDs.size() > sim.maxPendingStops) continue;
			if(taxi.ID == sim.debugTaxiID){
				log.println(name() + "::processPeopleRequest(" + pr.id + ") DEBUG TAXI = " + taxi.ID +
						", nextStartTimePoint = " + taxi.getNextStartTimePoint() + 
						", nextStartPointIndex = " + taxi.getNextStartPointIndex() + 
						", nextStartPoint = " + taxi.getNextStartPoint());
				
			}
			ItineraryServiceSequence IS = computeBestProfitsItineraryPeopleInsertion(taxi, 
					taxi.getNextStartTimePoint(), taxi.getNextStartPointIndex(), 
					taxi.getNextStartPoint(), pr);
			
			if(IS == null) continue;
			if(sel_IS == null){
				sel_IS = IS;
			}else{
				if(sel_IS.ss.profitEvaluation < IS.ss.profitEvaluation){
					sel_IS = IS;
					System.out.println(name() + "::processPeopleRequest, UPDATE sel_taxi " + 
					IS.taxi.ID + ", sel_ss.profits = " + sel_IS.ss.profitEvaluation);
				}
			}
		}
		
		if(sel_IS == null){
			sim.nbPeopleRejects++;
			System.out.println(name() + "::processPeopleRequest --> request " + pr.id +
					" is REJECTED, nbPeopleRejected = " + sim.nbPeopleRejects + "/" + 
					sim.allPeopleRequests.size());
			log.println(name() + "::processPeopleRequest --> request " + pr.id + " is REJECTED");
			return;
		}
		Vehicle sel_taxi = sel_IS.taxi;
		ServiceSequence sel_ss = sel_IS.ss;
		int nextStartTimePoint = sel_taxi.getNextStartTimePoint();
		int fromPoint = sel_taxi.getNextStartPoint();
		int fromIndex = sel_taxi.getNextStartPointIndex();
		System.out.println(name() + "::processPeopleRequest, sequence = " + sel_ss.getSequence() + 
				", maxProfits = " + sel_ss.profitEvaluation + 
				", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " + nextStartTimePoint + 
				", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
		
		if(sel_taxi.ID == sim.debugTaxiID)
			log.println(name() + "::processPeopleRequest(" + pr.id + ") AT " + sim.T.currentTimePoint + " engage taxi " + sel_taxi.ID + " sequence = " + sel_ss.getSequence() + 
				", maxProfits = " + sel_ss.profitEvaluation + 
				", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " + nextStartTimePoint + 
				", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
		
		//if(taxiID == sel_taxi.ID)log.println("SimulatorBookedRequest::processPeopleRequest, sequence = " + sel_ss.getSequence() + 
			//	", maxProfits = " + sel_ss.profitEvaluation + 
				//", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " + nextStartTimePoint + 
				//", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
		//sim.admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex, fromPoint, sel_ss);

		sim.admitNewItinerary(sel_IS.taxi, nextStartTimePoint, fromIndex, fromPoint, sel_IS.I, sel_IS.ss);
		
		if(sel_taxi.ID == sim.debugTaxiID)
			log.println(name() + "::processPeopleRequest(" + pr.id + ") AT " + sim.T.currentTimePoint + " insert people to taxi " + 
		sel_taxi.ID + ", peopleOnBoards = " + sel_taxi.getPeopleOnBoards() + 
		", peopleOnBoards = " + sel_taxi.getPeopleOnBoards() + 
			", remainRequestIDs = " + sel_taxi.getRemainRequestID());
		
		sim.nbPeopleWaitBoarding++;
		System.out.println(name() + "::processPeopleRequest, nbPeopleWaitBoarding = " + 
				sim.nbPeopleWaitBoarding + ", nbPeopleComplete = " + sim.nbPeopleComplete + 
				", nbPeopleOnBoard = " + sim.nbPeopleOnBoard + 
				", nbPeopleRejects = "+ sim.nbPeopleRejects + ", total PeopleRequests = " + sim.allPeopleRequests.size());

	}
	public void processPeopleRequestOld(PeopleRequest pr) {
		// TODO Auto-generated method stub
		ServiceSequence sel_ss = null;
		Vehicle sel_taxi = null;
		//int taxiID = 1;
		for(int k = 0; k < sim.vehicles.size(); k++){
			Vehicle taxi = sim.vehicles.get(k);
			if(taxi.remainRequestIDs.size() > sim.maxPendingStops) continue;
			ServiceSequence ss = computeBestProfitsPeopleInsertion(taxi, pr);
			if(ss != null){
				//System.out.println("SimulatorBookedRequest::processPeopleRequest, taxi " + taxi.ID + 
						//", profits = " + ss.profitEvaluation);
				if(sel_ss == null){
					sel_ss = ss;
					sel_taxi = taxi;
				}else{
					if(sel_ss.profitEvaluation < ss.profitEvaluation){
						sel_ss = ss;
						sel_taxi = taxi;
						System.out.println("SimulatorBookedRequest::processPeopleRequest, UPDATE sel_taxi " + sel_taxi.ID + 
								", sel_ss.profits = " + sel_ss.profitEvaluation);
						if(sim.debugTaxiID == sel_taxi.ID)
							log.println("SimulatorBookedRequest::processPeopleRequest, UPDATE sel_taxi " + sel_taxi.ID + 
								", sel_ss.profits = " + sel_ss.profitEvaluation);
					}
				}
			}
		}
		if(sel_taxi == null){
			sim.nbPeopleRejects++;
			System.out.println("SimulatorBookedRequest::processPeopleRequest --> request " + pr.id + " is REJECTED, nbPeopleRejected = " + sim.nbPeopleRejects + "/" + sim.allPeopleRequests.size());
			log.println("SimulatorBookedRequest::processPeopleRequest --> request " + pr.id + " is REJECTED");
			return;
		}
		int nextStartTimePoint = sel_taxi.getNextStartTimePoint();
		int fromPoint = sel_taxi.getNextStartPoint();
		int fromIndex = sel_taxi.getNextStartPointIndex();
		System.out.println("SimulatorBookedRequest::processPeopleRequest, sequence = " + sel_ss.getSequence() + 
				", maxProfits = " + sel_ss.profitEvaluation + 
				", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " + nextStartTimePoint + 
				", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
		if(sim.debugTaxiID == sel_taxi.ID)log.println("SimulatorBookedRequest::processPeopleRequest, sequence = " + sel_ss.getSequence() + 
				", maxProfits = " + sel_ss.profitEvaluation + 
				", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " + nextStartTimePoint + 
				", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
		sim.admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex, fromPoint, sel_ss);

		sim.nbPeopleWaitBoarding++;
		System.out.println("SimulatorBookedRequest::processPeopleRequest, nbPeopleWaitBoarding = " + 
				sim.nbPeopleWaitBoarding + ", nbPeopleComplete = " + sim.nbPeopleComplete + 
				", nbPeopleOnBoard = " + sim.nbPeopleOnBoard + 
				", nbPeopleRejects = "+ sim.nbPeopleRejects + ", total PeopleRequests = " + sim.allPeopleRequests.size());
	}
	/*
	public void processParcelRequestNew(ParcelRequest pr){
		ItineraryServiceSequence sel_IS = null;
		for(int k = 0; k < sim.vehicles.size(); k++){
			Vehicle taxi = sim.vehicles.get(k);
			if(taxi.remainRequestIDs.size() > sim.maxPendingStops) continue;
			ItineraryServiceSequence IS = computeBestProfitsItineraryParcelInsertion(taxi, 
					taxi.getNextStartTimePoint(), taxi.getNextStartPointIndex(), 
					taxi.getNextStartPoint(), pr);
			if(IS == null) continue;
			if(sel_IS == null){
				sel_IS = IS;
			}else{
				if(sel_IS.ss.profitEvaluation < IS.ss.profitEvaluation){
					sel_IS = IS;
					System.out.println(name() + "::processParcelRequest, UPDATE sel_taxi " + 
					IS.taxi.ID + ", sel_ss.profits = " + sel_IS.ss.profitEvaluation);
				}
			}
		}
		
		if(sel_IS == null){
			sim.nbPeopleRejects++;
			System.out.println(name() + "::processParcelRequest --> request " + pr.id +
					" is REJECTED, nbParcelRejected = " + sim.nbParcelRejects + "/" + 
					sim.allParcelRequests.size());
			log.println("SimulatorBookedRequest::processParcelRequest --> request " + pr.id + " is REJECTED");
			return;
		}
		Vehicle sel_taxi = sel_IS.taxi;
		ServiceSequence sel_ss = sel_IS.ss;
		int nextStartTimePoint = sel_taxi.getNextStartTimePoint();
		int fromPoint = sel_taxi.getNextStartPoint();
		int fromIndex = sel_taxi.getNextStartPointIndex();
		System.out.println("SimulatorBookedRequest::processParcelRequest, sequence = " + sel_ss.getSequence() + 
				", maxProfits = " + sel_ss.profitEvaluation + 
				", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " + nextStartTimePoint + 
				", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
		//if(taxiID == sel_taxi.ID)log.println("SimulatorBookedRequest::processPeopleRequest, sequence = " + sel_ss.getSequence() + 
			//	", maxProfits = " + sel_ss.profitEvaluation + 
				//", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " + nextStartTimePoint + 
				//", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
		//sim.admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex, fromPoint, sel_ss);

		sim.admitNewItinerary(sel_IS.taxi, nextStartTimePoint, fromIndex, fromPoint, sel_IS.I, sel_IS.ss);
		sim.nbPeopleWaitBoarding++;
		System.out.println(name() + "::processParcelRequest, nbParcelWaitBoarding = " + 
				sim.nbParcelWaitBoarding + ", nbParcelComplete = " + sim.nbParcelComplete + 
				", nbParcelOnBoard = " + sim.nbParcelOnBoard + 
				", nbParcelRejects = "+ sim.nbParcelRejects + ", total ParcelRequests = " + sim.allParcelRequests.size());

	}
	*/
	public void processParcelRequest(ParcelRequest pr) {
		// TODO Auto-generated method stub
			//int taxiID = 727;
			
			ServiceSequence sel_ss = null;
			Vehicle sel_taxi = null;
			for(int k = 0; k < sim.vehicles.size(); k++){
				Vehicle taxi = sim.vehicles.get(k);
				if(taxi.status == VehicleStatus.STOP_WORK) continue;
				if(taxi.totalTravelDistance > sim.maxTravelDistance) continue;
				if(taxi.remainRequestIDs.size() > sim.maxPendingStops) continue;
				ServiceSequence ss = computeBestProfitsParcelInsertion(taxi, pr);
				if(ss != null){
					//System.out.println("SimulatorBookedRequest::processPeopleRequest, taxi " + taxi.ID + 
							//", profits = " + ss.profitEvaluation);
					if(sel_ss == null){
						sel_ss = ss;
						sel_taxi = taxi;
					}else{
						if(sel_ss.profitEvaluation < ss.profitEvaluation){
							sel_ss = ss;
							sel_taxi = taxi;
						}
					}
				}
			}
			if(sel_taxi == null){
				sim.nbParcelRejects++;
				System.out.println("SimulatorBookedRequest::processParcelRequest --> request " + pr.id + " is REJECTED, nbParcleRejects = " + sim.nbParcelRejects + "/" + sim.allParcelRequests.size());
				log.println("SimulatorBookedRequest::processParcelRequest --> request " + pr.id + " is REJECTED");
				return;
			}
			int nextStartTimePoint = sel_taxi.getNextStartTimePoint();
			int fromPoint = sel_taxi.getNextStartPoint();
			int fromIndex = sel_taxi.getNextStartPointIndex();
			
			System.out.println("SimulatorBookedRequest::processParcelRequest, sequence = " + sel_ss.getSequence() + 
					", maxProfits = " + sel_ss.profitEvaluation + 
					", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " + nextStartTimePoint + 
					", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
			
			if(sim.debugTaxiID == sel_taxi.ID){
			log.println("SimulatorBookedRequest::processParcelRequest(" + pr.id + ") AT " + sim.T.currentTimePoint + " engage taxi " + sel_taxi.ID + " sequence = " + sel_ss.getSequence() + 
					", maxProfits = " + sel_ss.profitEvaluation + 
					", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " + nextStartTimePoint + 
					", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
			}
			sim.admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex, fromPoint, sel_ss);
			
			if(sel_taxi.ID == sim.debugTaxiID)
				log.println(name() + "::processParcelRequest(" + pr.id + ") AT " + sim.T.currentTimePoint + " insert parcel to taxi " + 
			sel_taxi.ID + ", parcelOnBoards = " + sel_taxi.getParcelOnBoards() + 
			", peopleOnBoards = " + sel_taxi.getPeopleOnBoards() + 
			", remainRequestIDs = " + sel_taxi.getRemainRequestID());
			
			
			sim.nbParcelWaitBoarding++;
			System.out.println("SimulatorBookedRequest::processParcelRequest, nbParcelWaitBoarding = " + 
			sim.nbParcelWaitBoarding + ", nbParcelComplete = " + sim.nbParcelComplete + ", nbParcelOnBoard = " + sim.nbParcelOnBoard + 
			", nbParcelRejects = "+ sim.nbParcelRejects + ", total ParcelRequests = " + sim.allParcelRequests.size());
		

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
