package pbts.onlinealgorithmtimeunit;

import java.io.PrintWriter;
import java.util.ArrayList;

import pbts.entities.ErrorMSG;
import pbts.entities.ItineraryTravelTime;
import pbts.entities.LatLng;
import pbts.entities.ParcelRequest;
import pbts.entities.PeopleRequest;
import pbts.entities.TaxiTimePointIndex;
import pbts.entities.TimePointIndex;
import pbts.entities.Vehicle;
import pbts.enums.ErrorType;
import pbts.enums.VehicleStatus;
import pbts.simulation.ItineraryServiceSequence;
import pbts.simulation.ServiceSequence;
import pbts.simulation.SimulatorTimeUnit;
import pbts.simulation.Utility;
import pbts.simulation.SimulatorTimeUnit;

public class ParcelInsertionBasedOnPopularPoint implements OnlineParcelInsertion {
	public SimulatorTimeUnit sim;
	public PrintWriter log;
	
	public ParcelInsertionBasedOnPopularPoint(SimulatorTimeUnit sim){
		this.sim = sim;
		this.log = sim.log;
	}
	public String name(){
		return "ParcelInsertionLastSequenceNotChangeDecisionTimeLimit";
	}
	
	/****[SonNV]
	 * Process parcel request inserted: 
	 * 		+ Find nearest taxi: status of all taxis is updated in decision time and the shortest distance from a point of remain points to pickup and delivery point is calculated.
	 * 		+ Insert parcel:
	 * 			* Insert new pickup point after the nearest point in remain point.
	 * 			* Establish itinerary: 	- Compute shortest path from i-point to j-point (i, j in remain points)
	 * 									- Adjust path: 	Get cluster of popular points which contains a point in the path.
	 * 													Insert center of cluster to path.
	 * @param:
	 * 		parReq			:		list parcels need insert.
	 * 		startDecideTime :		decided time.
	 */
	public void processParcelRequests(ArrayList<ParcelRequest> parReq, double startDecideTime) {
		// TODO Auto-generated method stub
		System.out.println(name() + "::processParcelRequests(parReq.sz = " + parReq.size() + ")");
		for(int i = 0; i < parReq.size(); i++){
			//System.out.println("AAAAAAAAAAAAAAAAAAAAAA");
			double t = (System.currentTimeMillis()-startDecideTime)*0.001; 
			if(t > sim.maxTimeAllowedOnlineDecision){
				sim.nbParcelRejects++;
				System.out.println(name() + "::processParcelRequests, nbParcelRejects = " + sim.nbParcelRejects + 
						" DUE TO online decision time expired t = " + t + ", sim.maxTimeAllowedOnlineDecision = " + sim.maxTimeAllowedOnlineDecision);
				continue;
			}
			ParcelRequest pr = parReq.get(i);
			//TaxiTimePointIndex ttpi = findTaxiForParcelInsertion(pr,sim.maxTimeAllowedFindingBestTaxiForParcel);
			//System.out.println(name() + "::processParcelRequests(pr = " + pr.id + " --> found taxi " + ttpi.taxi.ID + ")");
			double t0 = System.currentTimeMillis();
			/*if(ttpi == null){
				System.out.println(name() + "::processParcelRequests, nbParcelRejects = " + sim.nbParcelRejects + 
						"DUE TO no available taxi found");
			}else{
				//insertParcelRequest(pr,ttpi.taxi,ttpi.tpi,ttpi.keptRequestIDs, ttpi.remainRequestIDs,
				//	sim.maxTimeAllowedInsertOneParcel);
			}*/
			t = System.currentTimeMillis() - t0;
			t = t*0.001;
			if(t > sim.maxTimeOneParcelInsertion) sim.maxTimeOneParcelInsertion = t;
			
			System.out.println(name() + "::procesParcelRequests, sim.status = " + sim.getAcceptRejectStatus());
		}
	}
}
