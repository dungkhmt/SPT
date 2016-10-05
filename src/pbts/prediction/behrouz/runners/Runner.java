package pbts.prediction.behrouz.runners;

import java.util.ArrayList;

import pbts.prediction.behrouz.fixedrate.FixedRateSampler;

public class Runner {

	public static void main(String[] args) {
		
		String[] weekdays = {
			"Monday", "Tuesday", "Wednesday",
			"Thursday", "Friday", "Saturday",
			"Sunday"
		};
		
		FixedRateSampler frs = new FixedRateSampler();
		
		int dayOfWeek = 2;
		int period = 40;
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
		System.out.println();
		
		
	}

}
