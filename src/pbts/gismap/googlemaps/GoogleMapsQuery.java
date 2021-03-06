package pbts.gismap.googlemaps;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import Direction;
//import StepDirection;;
public class GoogleMapsQuery {

	/**
	 * @param args
	 */
	public static final int SPEED = 40;// average speed is 40km/h
	public static final double RATIO = 1.5;// ratio between gMap distance and Havsine distance

	public GoogleMapsQuery(){
		/*
		System.getProperties().put("http.proxyHost", "HL-proxyA");
		System.getProperties().put("http.proxyPort", "8080");
		System.getProperties().put("http.proxyUser", "antq");
		System.getProperties().put("http.proxyPassword", "01239991234");
		*/
	}
	public double getApproximateTravelTimeSecond(double lat1, double long1, double lat2, double long2){
		double t = computeDistanceHaversine(lat1, long1, lat2, long2);
		t = t*3600.0/SPEED;
		t = t*RATIO;
		return t;
	}
	public double getApproximateDistanceMeter(double lat1, double long1, double lat2, double long2){
		double t = computeDistanceHaversine(lat1, long1, lat2, long2)*1000;// in meters
		t = t*RATIO;
		return t;
	}
	
	public double computeDistanceHaversine(double lat1, double long1, double lat2, double long2){
		double SCALE = 1;
		double PI  = 3.14159265;
		long1 = long1*1.0/SCALE;
		lat1 = lat1*1.0/SCALE;
		long2 = long2*1.0/SCALE;
		lat2 = lat2*1.0/SCALE;

		double dlat1 = lat1*PI/180;
		double dlong1 = long1*PI/180;
		double dlat2 = lat2*PI/180;
		double dlong2 = long2*PI/180;

		double dlong = dlong2 - dlong1;
		double dlat = dlat2 - dlat1;
		
		double aHarv = Math.pow(Math.sin(dlat/2),2.0) + Math.cos(dlat1)*Math.cos(dlat2)*Math.pow(Math.sin(dlong/2),2.0);
		double cHarv = 2*Math.atan2(Math.sqrt(aHarv),Math.sqrt(1.0-aHarv));
		
		double R = 6378.137; // in km

		//return R*cHarv; // in km
		return R*cHarv*SCALE; // in km
		
	}
	public static double computeDistanceHaversineStatic(double lat1, double long1, double lat2, double long2){
		double SCALE = 1;
		double PI  = 3.14159265;
		long1 = long1*1.0/SCALE;
		lat1 = lat1*1.0/SCALE;
		long2 = long2*1.0/SCALE;
		lat2 = lat2*1.0/SCALE;

		double dlat1 = lat1*PI/180;
		double dlong1 = long1*PI/180;
		double dlat2 = lat2*PI/180;
		double dlong2 = long2*PI/180;

		double dlong = dlong2 - dlong1;
		double dlat = dlat2 - dlat1;
		
		double aHarv = Math.pow(Math.sin(dlat/2),2.0) + Math.cos(dlat1)*Math.cos(dlat2)*Math.pow(Math.sin(dlong/2),2.0);
		double cHarv = 2*Math.atan2(Math.sqrt(aHarv),Math.sqrt(1.0-aHarv));
		
		double R = 6378.137; // in km

		//return R*cHarv; // in km
		return R*cHarv*SCALE; // in km
		
	}
	
	public double getSpeed(double lat1, double lng1, double lat2, double lng2){
		double d = getDistance(lat1, lng1, lat2, lng2)*1000;
		double t = getTravelTimeOnePost(lat1, lng1, lat2, lng2, "driving");
		return (d*1.0/t)*3.6;
	}
	public double getDistance(double lat1, double lng1, double lat2, double lng2){
		System.getProperties().put("http.proxyHost", "HL-proxyA");
		System.getProperties().put("http.proxyPort", "8080");
		System.getProperties().put("http.proxyUser", "antq");
		System.getProperties().put("http.proxyPassword", "0123999123");
		URL url = null;
        try {
            //url = new URL("http://" + server + "/we-expect-post/data");
        	//double lat1 = 21.019137;
        	//double lng1 = 105.769787;
        	//double lat2 = 21.027469;
        	//double lng2 = 105.902309;
        	url = new URL("http://maps.google.com/maps/api/directions/xml?origin=" + lat1 + "," + lng1 + "&destination=" + lat2 + "," + lng2 + "&sensor=false&units=metric");
        	System.out.println("HTTP POST = " + url);
        } catch (MalformedURLException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        	ex.printStackTrace();
        }

        HttpURLConnection urlConn = null;
        try {
            // URL connection channel.
            urlConn = (HttpURLConnection) url.openConnection();
        } catch (IOException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

        // Let the run-time system (RTS) know that we want input.
        urlConn.setDoInput (true);

        // Let the RTS know that we want to do output.
        urlConn.setDoOutput (true);

        // No caching, we want the real thing.
        urlConn.setUseCaches (false);

        try {
            urlConn.setRequestMethod("POST");
        } catch (ProtocolException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

        try {
            urlConn.connect();
        } catch (IOException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        	ex.printStackTrace();
        }

        DataOutputStream output = null;
        DataInputStream input = null;

        try {
            output = new DataOutputStream(urlConn.getOutputStream());
        } catch (IOException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        	ex.printStackTrace();
        }

        // Specify the content type if needed.
        //urlConn.setRequestProperty("Content-Type",
        //  "application/x-www-form-urlencoded");

        
        /*
        // Construct the POST data.
        String content =
          "name=" + URLEncoder.encode("Greg") +
          "&email=" + URLEncoder.encode("greg at code dot geek dot sh");

        // Send the request data.
        try {
            output.writeBytes(content);
            output.flush();
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
		*/
        
        // Get response data.
        String str = null;
        double d = -1;
        try {
        	PrintWriter out = new PrintWriter(new FileWriter("http-post-log.txt"));
            input = new DataInputStream (urlConn.getInputStream());
            //while (null != ((str = input.readLine()))) {
              ///  System.out.println(str);
              //  out.println(str);
            //}

            //input = new DataInputStream (urlConn.getInputStream());
            
            try{
            	DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            	Document doc = builder.parse(input);
            	doc.getDocumentElement().normalize();
            	
            	NodeList nl = doc.getElementsByTagName("leg");
            	//System.out.println("Element leg has size = " + nl.getLength());
            	Node nod = nl.item(0);
            	Element e = (Element)nod;
            	if(e == null){
            		//System.out.println("GoogleMapsQuery::getDistanec(" + lat1 + "," + lng1 + "," + lat2+ "," + lng2 + ") --> exception");
            		return -1;
            	}
            	nl = e.getElementsByTagName("step");
            	//System.out.println("Elements step has size = " + nl.getLength());
            	nl = e.getElementsByTagName("distance");
            	nod = nl.item(nl.getLength()-1);
            	//System.out.println("Distance sz = " + nl.getLength() + ": name = " + nod.getNodeName() + " value = " + nod.getNodeValue());
            	
            	e = (Element)nod;
            	nl = e.getElementsByTagName("text");
            	nod = nl.item(0);
            	
            	//System.out.println("Text: name = " + nod.getNodeName() + " value = " + nod.getChildNodes().item(0).getNodeValue());
            	
            	String d_s = nod.getChildNodes().item(0).getNodeValue();
            	int idx = d_s.indexOf("km");
            	if(idx < 0) {
            		idx = d_s.indexOf("m");
            		if(idx == -1){
            			//System.out.println("GoogleMapsQuery::getDistance(...) --> exception, cannot find km, m of the distance tag in the returned xml");
            			return -1;
            		}
            		d_s = d_s.substring(0,idx);
            		d = Double.valueOf(d_s)*0.001; // convert into km
            	}else{
            		d_s = d_s.substring(0,idx);
            		d = Double.valueOf(d_s);
            	}
            	
            }catch(Exception e){
            	e.printStackTrace();
            }
            
            input.close ();
            out.close();
        } catch (IOException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        	ex.printStackTrace();
        }
 		
        return d;
	}

	
	public int getTravelTime(double lat1, double lng1, double lat2, double lng2, String mode){
		//try to probe maximum 20 times 
		int t = -1;
		int maxTrials = 2;
		for(int i = 0; i < maxTrials; i++){
			t = getTravelTimeOnePost(lat1,lng1,lat2,lng2,mode);
			//System.out.println("GoogleMapsQuery.getTravelTime, probe " + i + ", travel time = " + t);
			if(t > -1) break;
		}
		
		return t;
	}
	private int getTravelTimeOnePost(double lat1, double lng1, double lat2, double lng2, String mode){
	
		System.getProperties().put("http.proxyHost", "HL-proxyA");
		System.getProperties().put("http.proxyPort", "8080");
		System.getProperties().put("http.proxyUser", "antq");
		System.getProperties().put("http.proxyPassword", "0123999123");
		URL url = null;
        try {
            //url = new URL("http://" + server + "/we-expect-post/data");
        	//double lat1 = 21.019137;
        	//double lng1 = 105.769787;
        	//double lat2 = 21.027469;
        	//double lng2 = 105.902309;
        	//url = new URL("http://maps.google.com/maps/api/directions/xml?origin=" + lat1 + "," + lng1 + "&destination=" + lat2 + "," + lng2 + "&sensor=false&units=metric" + "&mode=" + mode);
        	url = new URL("http://maps.google.com/maps/api/directions/xml?origin=" + lat1 + "," + lng1 + "&destination=" + lat2 + "," + lng2 + "&sensor=false&units=metric");
        	//System.out.println("HTTP POST = " + url);
        } catch (MalformedURLException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        	ex.printStackTrace();
        }

        HttpURLConnection urlConn = null;
        try {
            // URL connection channel.
            urlConn = (HttpURLConnection) url.openConnection();
        } catch (IOException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        	System.out.println("openConnection failed");
            ex.printStackTrace();
        }

        // Let the run-time system (RTS) know that we want input.
        urlConn.setDoInput (true);

        // Let the RTS know that we want to do output.
        urlConn.setDoOutput (true);

        // No caching, we want the real thing.
        urlConn.setUseCaches (false);

        try {
            urlConn.setRequestMethod("POST");
        } catch (ProtocolException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

        try {
            urlConn.connect();
        } catch (IOException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        	ex.printStackTrace();
        }

        DataOutputStream output = null;
        DataInputStream input = null;

        try {
            output = new DataOutputStream(urlConn.getOutputStream());
        } catch (IOException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        	ex.printStackTrace();
        }

        // Specify the content type if needed.
        //urlConn.setRequestProperty("Content-Type",
        //  "application/x-www-form-urlencoded");

        
        /*
        // Construct the POST data.
        String content =
          "name=" + URLEncoder.encode("Greg") +
          "&email=" + URLEncoder.encode("greg at code dot geek dot sh");

        // Send the request data.
        try {
            output.writeBytes(content);
            output.flush();
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
		*/
        
        // Get response data.
        String str = null;
        int duration = -1;// in seconds
        try {        	
            input = new DataInputStream (urlConn.getInputStream());
           // System.out.println("Print Respone Data From Google Map API");
          
            //input = new DataInputStream (urlConn.getInputStream());           
            try{
            	DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            	Document doc = builder.parse(input);   
            
            	doc.getDocumentElement().normalize();
            	         	
            	//System.out.println("GoogleMapsQuery.getTravelingTime, -> xml = " + 	doc.toString());
            	
            	NodeList nl = doc.getElementsByTagName("leg");
            	Node nod = nl.item(0);
            	Element e = (Element)nod;
            	if(e == null){
            		//System.out.println("GoogleMapsQuery::getTravelTime(" + lat1 + "," + lng1 + "," + lat2+ "," + lng2 + ") --> exception");
            		return -1;
            	}
            	/*
            	nl = e.getElementsByTagName("step");
            	for(int k =0;k < nl.getLength();k++){
            		nod = nl.item(k);
            		e = (Element)nod;
            		
            	//	System.out.println("dd"+e.getChildNodes().item(0).getNodeValue());
            	}
            	*/
            	//System.out.println("Elements step has size = " + nl.getLength());
            	nl = e.getElementsByTagName("duration");
            	nod = nl.item(nl.getLength()-1);
            	//System.out.println("Duration sz = " + nl.getLength() + ": name = " + nod.getNodeName() + " value = " + nod.getNodeValue());
            	
            	e = (Element)nod;
            	nl = e.getElementsByTagName("value");
            	nod = nl.item(0);
            	
            	e = (Element)nod;
            	
            	duration = Integer.valueOf(e.getChildNodes().item(0).getNodeValue());
            	
            	
            }catch(Exception e){
            	e.printStackTrace();
            }
            
            input.close ();
        } catch (IOException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        	ex.printStackTrace();
        }
 		
        return duration;
	}
	
	public Direction getDirection(double lat1, double lng1, double lat2, double lng2, String mode){
		Direction  direction = null;
		ArrayList<StepDirection> steps = new ArrayList<StepDirection>();
		URL url = null;
		int durations =0;
		int distances =0;
        try {
        	url = new URL("http://maps.google.com/maps/api/directions/xml?origin=" + lat1 + "," + lng1 + "&destination=" + lat2 + "," + lng2 + "&sensor=false&units=metric&model="+mode);
        } catch (MalformedURLException ex) {
        	ex.printStackTrace();
        }
        //System.out.println("URL: "+url);

        HttpURLConnection urlConn = null;
        try {
            // URL connection channel.
            urlConn = (HttpURLConnection) url.openConnection();
        } catch (IOException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        	System.out.println("openConnection failed");
            ex.printStackTrace();
        }

        // Let the run-time system (RTS) know that we want input.
        urlConn.setDoInput (true);
        urlConn.setDoOutput (true);
        // No caching, we want the real thing.
        urlConn.setUseCaches (false);

        try {
            urlConn.setRequestMethod("POST");
        } catch (ProtocolException ex) {
            ex.printStackTrace();
        }

        try {
            urlConn.connect();
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
        DataOutputStream output = null;
        DataInputStream input = null;
        try {
            output = new DataOutputStream(urlConn.getOutputStream());
        } catch (IOException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        	ex.printStackTrace();
        }
        // Get response data.
        String str = null;
        try {        	
            input = new DataInputStream (urlConn.getInputStream());
            //System.out.println("Print Respone Data From Google Map API");
          
            //input = new DataInputStream (urlConn.getInputStream());           
            try{
            	DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            	Document doc = builder.parse(input);             
            	doc.getDocumentElement().normalize();
  
            	
            	// read steps
            	NodeList nl = doc.getElementsByTagName("step");
            	int szLocation = nl.getLength();
            	String lat_start_location;
            	String lng_start_location;
            	
        		String lat_end_location;
        		String lng_end_location;
        		
        		int duration;
        		float distance;
        		String modeStep;
        		String html_instruction;
        		
        		//System.out.println("Elements step has size = " + szLocation);
        		for (int i = 0; i < szLocation; i++) {
        			//System.out.println("\nStep: "+i);
        			// read start locations
        			NodeList nlStart = doc.getElementsByTagName("start_location");
        			Element e = (Element) nlStart.item(i);
        			lat_start_location = e.getElementsByTagName("lat")
        					.item(0).getChildNodes().item(0).getNodeValue();
        			lng_start_location = e.getElementsByTagName("lng")
        					.item(0).getChildNodes().item(0).getNodeValue();
        			
        			//System.out.println("lat_start_location["+i+"]: "+ lat_start_location);
        			//System.out.println("lng_start_location["+i+"]: "+ lng_start_location);
        			
        			//read end locations
        			NodeList nlEnd = doc.getElementsByTagName("end_location");
        			 e = (Element) nlEnd.item(i);
        			lat_end_location = e.getElementsByTagName("lat")
        					.item(0).getChildNodes().item(0).getNodeValue();
        			lng_end_location = e.getElementsByTagName("lng")
        					.item(0).getChildNodes().item(0).getNodeValue();
        			
        			//System.out.println("lat_end_location["+i+"]: "+ lat_end_location);
        			//System.out.println("lng_end_location["+i+"]: "+ lng_end_location);
        			
        			//read duration 
        			NodeList nlDuration = doc.getElementsByTagName("duration");
        			e = (Element) nlDuration.item(i);
        			duration = Integer.parseInt(e.getElementsByTagName("value")
        					.item(0).getChildNodes().item(0).getNodeValue());  
        			durations += duration;
        			//System.out.println("duration["+i+"]: "+ duration);
        			
        			//read distance 
        			NodeList nlDistance = doc.getElementsByTagName("distance");
        			e = (Element) nlDistance.item(i);
        			distance = Float.parseFloat(e.getElementsByTagName("value")
        					.item(0).getChildNodes().item(0).getNodeValue());
        			distances += distance;     			
        			//System.out.println("distance["+i+"]: "+ distance);
        			
        			// read mode
        			NodeList nlModeStep = doc.getElementsByTagName("travel_mode");
        			e = (Element) nlModeStep.item(i);
        			modeStep =  e.getChildNodes().item(0).getNodeValue(); 		     			
        			//System.out.println("trave mode["+i+"]: "+ modeStep);
        			
        			//read html instruction
        			NodeList nlHTML_instructions = doc.getElementsByTagName("html_instructions");
        			e = (Element)nlHTML_instructions.item(i);
        			html_instruction =  e.getChildNodes().item(0).getNodeValue(); 		     			
        			//System.out.println("nlHTML_instructions["+i+"]: "+ html_instruction);
        			
                    StepDirection step = new StepDirection(lat_start_location, lng_start_location,
                    		lat_end_location,lng_end_location, duration, distance,modeStep,html_instruction);
                    steps.add(step);
        		}
        		
        		// read start address
        		String startAdd  = null;
        		String endAdd  = null;
				if (doc.getElementsByTagName("start_address") != null) {
					NodeList nlStartAdd = doc
							.getElementsByTagName("start_address");
					Element e = (Element) nlStartAdd.item(0);
					if(e!= null){
				     	startAdd = e.getChildNodes().item(0).getNodeValue();
					   //System.out.println("\nstart address: " + startAdd);
					}

					// read end address
					NodeList nlEndAdd = doc.getElementsByTagName("end_address");
					e = (Element) nlEndAdd.item(0);
					if(e!= null){
					    endAdd = e.getChildNodes().item(0).getNodeValue();
					    //System.out.println("end address: " + endAdd);
					}
				}
        		//System.out.println("\nSumary Duration: "+durations);
        		//System.out.println("Sumary Distance: "+distances);
        		
        		direction = new Direction(steps, startAdd, endAdd, lat1, lng1, lat2, lng2, durations, distances,mode);
                //System.out.println(direction.getDistances());
            }catch(Exception e){
            	e.printStackTrace();
            }
            
            input.close ();
        } catch (IOException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        	ex.printStackTrace();
        } 		
        return direction;
	}
	
	public void analyzeSpeed(String fi, String fo){
		try{
			Scanner in = new Scanner(new File(fi));
			String line;
			ArrayList<Double> lats = new ArrayList<Double>();
			ArrayList<Double> lngs = new ArrayList<Double>();
			while(true){
				line = in.nextLine();
				if(line.compareTo("-1") == 0) break;
				String[] s= line.split(",");
				double lat = Double.valueOf(s[0]);
				double lng = Double.valueOf(s[1]);
				lats.add(lat);
				lngs.add(lng);
			}
			in.close();
			DecimalFormat df = new DecimalFormat("#.##");
			PrintWriter out = new PrintWriter(fo);
			double avg_sp = 0;
			int N = 0;
			for(int i = 0; i < lats.size()-1; i++){
				for(int j = i+1;j < lats.size(); j++){
					//double sp = getSpeed(lats.get(i), lngs.get(i), lats.get(j), lngs.get(j));
					double d = getDistance(lats.get(i), lngs.get(i), lats.get(j), lngs.get(j));
					d = d * 1000.0;// in meters
					double t = getTravelTimeOnePost(lats.get(i), lngs.get(i), lats.get(j), lngs.get(j),"driving");// in seconds
					if(d <= 0 || t <= 0) continue;
					double sp = (d*1.0/t)*3.6; //km/h
					avg_sp += sp;
					N++;
					out.println(i + " --> " + j + "(" + lats.get(i) + "," + lngs.get(i) + ") --> (" + lats.get(j) + "," + lngs.get(j) + ") "
							+ "distance = " + df.format(d) + "m, time = " + df.format(t) + "s, speed = \t" + 
					df.format(sp) + "\t km/h");
					System.out.println(i + " --> " + j + " (" + lats.get(i) + "," + lngs.get(i) + ") --> (" + lats.get(j) + "," + lngs.get(j) + ") "
							+ "distance = " + df.format(d) + "m, time = " + df.format(t) + "s, speed = \t" + 
					df.format(sp) + "\tkm/h");
				}
			}
			avg_sp = avg_sp*1.0/N;
			out.println("avg_sp = " + df.format(avg_sp));
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GoogleMapsQuery gq = new GoogleMapsQuery();
		
		gq.analyzeSpeed("points-Sanfrancisco.txt", "Speed-Sanfrancisco.txt");
		/*
		double d = gq.getDistance(37.791667,-122.421182,37.766295,-122.421011);
		double t = gq.getTravelTimeOnePost(37.791667,-122.421182,37.766295,-122.421011,"driving");
		double sp = gq.getSpeed(37.791667,-122.421182,37.766295,-122.421011);
		
		System.out.println("distance = " + d + ", time = " + t + ", speed = " + sp + "km/h");
		*/
		//int duration = gq.getTravelTime(21.02284234072558,105.85752010345459,21.018696198229126, 105.84932327270508);
	/*	double d = gq.computeDistanceHaversine(10.771128,106.652784,10.775744,106.633494);
		//double dbicycling = gq.getTravelTime(10.771128,106.652784,10.775744,106.633494,"bicycling");
		double ddriving = gq.getTravelTime(10.771128,106.652784,10.775744,106.633494,"driving");
		double duration = d*3600.0/40;
		System.out.println("d = " + d + " duration = " + duration);
		System.out.println("ddriving = " + ddriving);
		*/
		if(true) return;
		Direction dir = gq.getDirection(10.8504245,	106.7547494	, 10.8494341,	106.7473888,"driving");
		System.out.println("Duration = " + dir.getDurations());
		System.out.println("Distance = " + dir.getDistances());
		ArrayList<StepDirection> steps = dir.getStepsDirection();
		String s = "";
		for(int m = 0;m< steps.size();m++){
			StepDirection step = steps.get(m);
			String stepString = step.getStartLat();
			stepString += ","+step.getStartLng();
			stepString += ","+step.getEndlat();
			stepString += ","+step.getEndLng();
			stepString +=","+step.getDistance();
			stepString += ","+step.getDuaration();
			stepString += ","+step.getMode();
			stepString += ";";
			s += stepString;
		}
		System.out.println("Step = " + s);
		
		//double ddriving = gq.getTravelTime(10.784202,106.678914,10.786131,106.681574,"driving");
		//System.out.println("ddriving = " + ddriving);
	}
	

}
