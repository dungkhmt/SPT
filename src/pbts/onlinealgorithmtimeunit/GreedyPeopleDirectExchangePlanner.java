package pbts.onlinealgorithmtimeunit;

import java.io.PrintWriter;
import java.util.ArrayList;

import pbts.entities.ParcelRequest;
import pbts.entities.PeopleRequest;
import pbts.itineraryoptimizer.ItineraryOptimizer;
import pbts.simulation.SimulatorTimeUnit;

public class GreedyPeopleDirectExchangePlanner implements OnlinePlanner {
	public PrintWriter log = null;
	public SimulatorTimeUnit sim = null;

	public OnlinePeopleInsertion peopleInserter = null;
	public OnlineParcelInsertion parcelInserter = null;
	public ItineraryOptimizer IO = null;
	
	public GreedyPeopleDirectExchangePlanner(SimulatorTimeUnit sim){
		this.sim = sim;
		if(sim != null)this.log = sim.log;
		
		peopleInserter = new PeopleInsertionDirectFirstServiceGreedy(sim);
		parcelInserter = new ParcelInsertionPeopleDirectServiceGreedy(sim);
		IO = new ItineraryOptimizer(sim);
	}
	public void setSimulator(SimulatorTimeUnit sim){
		this.sim = sim;
		if(sim != null) log = sim.log;
	}
	public String name(){ return "PeopleDirectServiceGreedy";}
	public void processPeopleRequest(PeopleRequest pr) {
		// TODO Auto-generated method stub
		System.out.println(name() + "::processPeopleRequest NOT IMPLEMENTED");
		sim.exit();
	}

	public void processParcelRequest(ParcelRequest pr) {
		// TODO Auto-generated method stub
		System.out.println(name() + "::processParcelRequest NOT IMPLEMENTED");
		sim.exit();
	}

	public void processPeopleRequests(ArrayList<PeopleRequest> pr) {
		// TODO Auto-generated method stub
		double startDecideTime = System.currentTimeMillis();
		peopleInserter.processPeopleRequests(pr, startDecideTime);
		double t = System.currentTimeMillis() - startDecideTime;
		t = t *0.001;
		if(sim.maxDecideTimePeopleRequests < t) sim.maxDecideTimePeopleRequests = t;
		IO.moveGreedyExchange(startDecideTime);
	}

	public void processParcelRequests(ArrayList<ParcelRequest> pr) {
		// TODO Auto-generated method stub
		double startDecideTime = System.currentTimeMillis();
		parcelInserter.processParcelRequests(pr, startDecideTime);
		double t = System.currentTimeMillis() - startDecideTime;
		t = t * 0.001;
		if(sim.maxDecideTimeParcelRequests < t) sim.maxDecideTimeParcelRequests = t;
		IO.moveGreedyExchange(startDecideTime);
	}

}
