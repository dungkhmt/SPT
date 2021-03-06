package pbts.entities;

import java.util.ArrayList;

public class Itinerary {

	/**
	 * @param args
	 */
	public ArrayList<Integer> path;
	
	public double distance;
	
	public Itinerary(){
		this.path = new ArrayList<Integer>();
	}
	public Itinerary(ArrayList<Integer> path){
		this.path = path;
		
	}
	public Itinerary(int[] L){
		path = new ArrayList<Integer>();
		for(int i = 0; i < L.length; i++)
			path.add(L[i]);
	}
	
	public double getDistance(){ return this.distance;}
	public void setDistance(double distance){ this.distance = distance;}
	public int get(int idx){ return path.get(idx);}
	public void remove(int idx){ path.remove(idx);}
	public int size(){ return path.size();}
	public String toString(){
		String s = "";
		for(int i = 0; i < path.size()-1; i++)
			s = s + path.get(i) + " -> ";
		s = s + path.get(path.size()-1);
		return s;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
