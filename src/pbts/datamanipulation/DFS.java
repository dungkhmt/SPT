package pbts.datamanipulation;

import java.io.PrintWriter;
import java.util.*;

import pbts.simulation.RoadMap;
public class DFS {

	/**
	 * @param args
	 */
	private int[] par;
	private int n;
	private HashSet[] A;
	private int[] d;
	private int[] f;
	private int t;
	private void dfs(int v){
		t++;
		d[v] = t;
		//System.out.println("DFS::dfs(" + v + ")");
		Iterator it = A[v].iterator();
		while(it.hasNext()){
			int u = (Integer)it.next();
			if(par[u] == -1){
				par[u] = v;
				dfs(u);
			}
		}
		t++;
		f[v] = t;
	}
	private void printGraph(int K, HashSet[] A){
		for(int k = 0; k < K; k++){
			System.out.print("A[" + k + "] = ");
			Iterator iter = A[k].iterator();
			while(iter.hasNext()){
				int vv = (Integer)iter.next();
				System.out.print(vv + ",");
			}
			System.out.println();
		}
	}
	public ArrayList<Integer> detectCycle(int K, HashSet[] A){
		this.n = K;
		this.A = A;
		
		return null;
	}
	
	private void dfs2(int v, HashSet[] At, HashSet scc){
		scc.add(v);
		Iterator it = At[v].iterator();
		while(it.hasNext()){
			int u = (Integer)it.next();
			if(par[u] == -1){
				par[u] = v;
				dfs2(u,At,scc);
			}
		}
	}
	public ArrayList<HashSet> computeSCC(int K, HashSet[] A){
		par = new int[K];
		d = new int[K];
		f = new int[K];
		this.A = A;
		this.n = K;
		for(int k = 0; k < K; k++)
			par[k] = -1;
		t = 0;
		for(int k = 0; k < K; k++){
			if(par[k] == -1)
				dfs(k);
		}
		int[] sv = new int[K];
		for(int v = 0; v < K; v++)
			sv[v] = v;
		//sort sv in an decreasing order of f
		for(int i = 0; i < K-1; i++)
			for(int j = i+1; j < K; j++)
				if(f[i] < f[j]){
					int tg = f[i]; f[i] = f[j]; f[j] = tg;
					tg = sv[i]; sv[i] = sv[j]; sv[j] = tg;
				}
		// compute G^t
		HashSet[] At = new HashSet[K];
		for(int i = 0; i < K; i++)
			At[i] = new HashSet<Integer>();
		
		for(int i = 0; i < K; i++){
			Iterator it = A[i].iterator();
			while(it.hasNext()){
				int v = (Integer)it.next();
				At[v].add(i);
			}
		}
		
		// perform dfs on At with order sv
		for(int i = 0; i < K; i++)
			par[i] = -1;
		ArrayList<HashSet> SCC = new ArrayList<HashSet>();
		for(int i = 0; i < K; i++){
			int v = sv[i];
			if(par[v] == -1){
				par[v] = v;
				HashSet scc = new HashSet<Integer>(); 
				dfs2(v,At,scc);
				SCC.add(scc);
			}
		}
		return SCC;
	}
	public ArrayList<Integer> extractCycle(HashSet S, HashSet[] A){
		if(S.size() == 1) return null;
		ArrayList<Integer> cycle = new ArrayList<Integer>();
		int[] p = new int[A.length];
		Iterator it = S.iterator();
		int v = (Integer)it.next();
		//cycle.add(v);
		int start = v;
		int iter = 0;
		boolean exception = false;
		int startC = -1;
		int endC = -1;
		for(int i = 0; i < A.length; i++) p[i] = -1;
		//System.out.println("DFS::extractCycle start = " + start);
		p[v] = v;
		while(true){
			iter++;
			if(iter > 30){
				exception = true;  break;
			}
			Iterator itv = A[v].iterator();
			int u = -1;
			while(itv.hasNext()){
				int x = (Integer)itv.next();
				//if(S.contains(x) && (x == start || x != start && !cycle.contains(x))){
				if(S.contains(x)){
					
					u = x;
					break;
				}
			}
			//System.out.println("DFS::extractCycle, v = " + v + ", u = " + u);
			if(u == -1){
				exception = true;
				break;
			}
			//if(cycle.contains(u)){
			if(p[u] != -1){
				startC = v; endC = u;
				break;
			}
			p[u] = v;
			
			//if(cycle.contains(u)) break;
			//if(u == start) break;
			//cycle.add(u);
			v = u;
		}
		if(exception){
			System.out.println("DFS::extractCycle --> exception");
			System.out.print("S = ");
			it = S.iterator();
			while(it.hasNext()){
				int x = (Integer)it.next();
				System.out.print(x + ",");
			}
			System.out.println();
			for(int h = 0; h < A.length; h++){
				System.out.print("A[" + h + "] = ");
				it = A[h].iterator();
				while(it.hasNext()){
					int x = (Integer)it.next();
					System.out.print(x + ",");
				}
				System.out.println();
			}
			return null;
		}
		int x = startC;
		iter = 0;
		exception = false;
		while(x != endC){
			iter++;
			if(iter > 20){
				exception = true;
				break;
			}
			cycle.add(x);
			x = p[x];
		}
		cycle.add(x);
		cycle.add(startC);
		ArrayList<Integer> rc = new ArrayList<Integer>();
		for(int i = cycle.size()-1; i >= 0; i--)
			rc.add(cycle.get(i));
		//if(cycle.size() == 1) return null;
		if(exception) return null;
		return rc;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RoadMap M = new RoadMap();
		//M.loadData("/Users/dungpq/research/projects/prediction-based-transport-scheduling/data/map-hanoi.txt");
		M.loadData("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFranciscoRoad.txt");
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for(int i = 0; i < M.V.size(); i++){
			int v = M.V.get(i);
			map.put(v, i);
		}
		int K = M.V.size();
		HashSet[] A = new HashSet[K];
		for(int v = 0; v < K; v++){
			A[v] = new HashSet<Integer>();
		}
		for(int i = 0; i < M.V.size(); i++){
			int v = M.V.get(i);
			int iv = map.get(v);
			ArrayList<pbts.entities.Arc> Av = M.A.get(v);
			for(int j = 0; j < Av.size(); j++){
				pbts.entities.Arc a = Av.get(j);
				int u = a.end;
				int iu = map.get(u);
				A[iv].add(iu);
			}
		}
		/*
		int K = 8;
		HashSet[] A = new HashSet[K];
		for(int i = 0; i < K; i++)
			A[i] = new HashSet<Integer>();
		A[1].add(2);
		A[2].add(3);
		A[2].add(5);
		A[2].add(7);
		A[3].add(1);
		A[3].add(4);
		A[4].add(5);
		A[5].add(4);
		A[5].add(6);
		A[6].add(7);
		A[7].add(5);
		A[7].add(6);
		*/
		DFS a = new DFS();
		ArrayList<HashSet> SCC = a.computeSCC(K, A);
		int maxSz = 0;
		int maxic = -1;
		for(int ic = 0; ic < SCC.size(); ic++){
			HashSet scc = SCC.get(ic);
			System.out.print("SCC[" + ic + "] = " + scc.size());
			/*
			Iterator it = scc.iterator();
			while(it.hasNext()){
				int v= (Integer)it.next();
				System.out.print(v + ",");
				
			}
			*/
			if(maxSz < scc.size()){
				maxSz = scc.size();
				maxic = ic;
			}
			System.out.println();
		}
		
		try{
			//PrintWriter out = new PrintWriter("/Users/dungpq/research/projects/prediction-based-transport-scheduling/data/map-hanoi-connected.txt");
			PrintWriter out = new PrintWriter("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFranciscoRoad-connected.txt");
			HashSet S = SCC.get(maxic);
			Iterator it = S.iterator();
			while(it.hasNext()){
				int iv = (Integer)it.next();
				int v = M.V.get(iv);
				pbts.entities.LatLng ll = M.mLatLng.get(v);
				out.println(v + " " + ll.lat + " " + ll.lng);
			}
			out.println(-1);
			for(int i= 0; i < M.Arcs.size(); i++){
				pbts.entities.Arc arc = M.Arcs.get(i);
				int u = map.get(arc.begin);
				int v = map.get(arc.end);
				if(S.contains(u) && S.contains(v)){
					out.println(arc.begin + " " + arc.end + " " + arc.w);
				}
				
			}
			out.println(-1);
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
