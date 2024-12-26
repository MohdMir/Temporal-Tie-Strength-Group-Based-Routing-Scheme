package report.GBR_Report;

import java.util.*;
import core.ConnectionListener;
import core.DTNHost;
import core.SimClock;
import core.UpdateListener;
import report.Report;

/**
 * GBR_Kclique: This class implements the K-clique detection in temporal graphs. 
 * It listens to host connections, processes the contact data, and identifies K-cliques.
 * Author: Mohd Yaseen Mir, NCU, Taiwan.
 */
public class GBR_Kclique extends Report implements ConnectionListener, UpdateListener {
	
	// Stores contacts in Time (day/hour): Node, day/T_i, contacts
	// Key: Node_id, Value: Map<Time, ArrayList<Integer>> (contacts)
	HashMap<Integer, Map<Integer, ArrayList<Integer>>> Cont_list_perday = new HashMap<>();
	
	// Stores all the K-cliques: day/T_i, k_number, contacts
	HashMap<Integer, Map<Integer, ArrayList<Integer>>> kClique_list = new HashMap<>();
	
	int K = 3; // For group formation condition m >= K
	int T_i = 86400; // Time unit |T_i| in GBR
	int init = 0; // Whenever a new list is created, init is incremented
	int N = 11; // Time range T_i to T_N
	int n = 3000; // Upper limit for lists

	@SuppressWarnings("unchecked")
	ArrayList<Integer>[] al = new ArrayList[n]; // Temporary storage for K-clique calculation
	@SuppressWarnings("unchecked")
	ArrayList<Integer>[] f_al = new ArrayList[n]; // Final list after removing duplicates
	@SuppressWarnings("unchecked")
	ArrayList<Integer>[] Group = new ArrayList[100]; // Stores final groups

	int k_if = 0;
	int p = 0; // To track the count of unique K-cliques in f_al

	@Override
	public void hostsConnected(DTNHost host1, DTNHost host2) {
		// Convert the current simulation time into discrete intervals (e.g., days)
		int time = (int) SimClock.getIntTime() / T_i;
		Connect_time(host1.getAddress(), time, host2.getAddress());
		Connect_time(host2.getAddress(), time, host1.getAddress());
	}

	private void Connect_time(int address, int time, int address2) {
		// Update contact list with the connection details
		ArrayList<Integer> li = null;
		if (Cont_list_perday.containsKey(address)) {
			li = Cont_list_perday.get(address).get(time);
		}
		if (li == null) {
			li = new ArrayList<>();
			li.add(address2);
			Map<Integer, ArrayList<Integer>> innerkey = Cont_list_perday.get(address);
			if (innerkey == null) {
				Cont_list_perday.put(address, innerkey = new HashMap<>());
			}
			innerkey.put(time, li);
		} else {
			if (!li.contains(address2))
				li.add(address2);
		}
	}

	public void done() {
		// Sort contact lists for each day
		for (Map.Entry<Integer, Map<Integer, ArrayList<Integer>>> t : this.Cont_list_perday.entrySet()) {
			Integer key = t.getKey();
			for (Map.Entry<Integer, ArrayList<Integer>> e : t.getValue().entrySet()) {
				Collections.sort(Cont_list_perday.get(key).get(e.getKey()));
			}
		}

		// Process contacts to detect K-cliques for each day
		for (int day = 2; day < N; day++) {
			initialize(); // Initialize temporary storage
			Create_kList(day); // Create K-cliques for the day
			Rmduplicates(); // Remove duplicate K-cliques
			Updateklist(day); // Update the final K-clique list
			flush(); // Reset temporary storage for the next day
		}

		// Write final K-clique data
		for (Map.Entry<Integer, Map<Integer, ArrayList<Integer>>> t : this.kClique_list.entrySet()) {
			Integer key = t.getKey();
			for (Map.Entry<Integer, ArrayList<Integer>> e : t.getValue().entrySet()) {
				Collections.sort(kClique_list.get(key).get(e.getKey()));
				write(key + " " + e.getKey() + kClique_list.get(key).get(e.getKey()));
			}
			write("---------------------");
		}

		// Call the group formation process
		GBR_GroupFormationProcessA GFPA = new GBR_GroupFormationProcessA();
		for (int i = 0; i < 100; i++) {
			Group[i] = new ArrayList<>();
		}
		int day = 2;
		while (day < N) {
			Group = GFPA.KClqandGroupinfo(kClique_list, Group, day, K);
			day++;
		}

		super.done();
	}

	// Initialize temporary storage for K-clique calculations
	public void initialize() {
		for (int i = 0; i < n; i++) {
			al[i] = new ArrayList<>();
		}
	}

	// Create K-cliques by processing contact lists
	private void Create_kList(int day) {
		for (Map.Entry<Integer, Map<Integer, ArrayList<Integer>>> t : this.Cont_list_perday.entrySet()) {
			Integer key = t.getKey();
			for (Map.Entry<Integer, ArrayList<Integer>> e : t.getValue().entrySet()) {
				Integer key2 = e.getKey();
				if (key2 == (day - 1)) {
					for (int j = 0; j < Cont_list_perday.get(key).get(key2).size(); j++) {
						if (Cont_list_perday.get(key).get(key2).get(j) != null) {
							int node_j_x = Cont_list_perday.get(key).get(key2).get(j);
							// Call calculateKclique to process the contacts
							calculateKclique(Cont_list_perday.get(node_j_x).get(key2), key, node_j_x, key2);
						}
					}
				}
			}
		}
	}

	private void calculateKclique(ArrayList<Integer> arrayList, Integer key, int node_j_x, int key2) {
		// Find K-cliques by iteratively intersecting contact lists
		ArrayList<Integer> inList = new ArrayList<>(arrayList);
		ArrayList<Integer> temp = new ArrayList<>(Cont_list_perday.get(key).get(key2));
		al[init].add(key);
		al[init].add(node_j_x);
		temp.retainAll(inList);

		Iterator<Integer> it = temp.iterator();
		while (it.hasNext()) {
			Collections.sort(temp);
			al[init].add(temp.get(0));
			inList.clear();
			inList = new ArrayList<>(temp);
			temp.clear();
			temp = new ArrayList<>(Cont_list_perday.get(inList.get(0)).get(key2));
			temp.retainAll(inList);
			it = temp.iterator();
		}
		init++;
	}

	// Remove duplicate K-cliques from the list
	private void Rmduplicates() {
		for (int i = 0; i < init; i++) {
			int x = 0;
			Collections.sort(al[i]);
			f_al[p] = new ArrayList<>();
			for (int j = 0; j < al[i].size(); j++) {
				if (i == 0) {
					f_al[0].add(al[i].get(j));
				} else {
					if (check(al[i], i) == 1) { // Check if the list already exists
						x = 1;
						break;
					} else {
						f_al[p].add(al[i].get(j));
					}
				}
			}
			if (x == 0)
				p++;
		}
	}

	private int check(ArrayList<Integer> arrayList, int i) {
		// Check if the given list already exists in the previous lists
		while (i > 0) {
			if (al[i - 1].equals(arrayList))
				return 1;
			i--;
		}
		return 0;
	}

	// Update the final K-clique list
	private void Updateklist(int day) {
		for (int i = 0; i < p; i++) {
			ArrayList<Integer> temp = new ArrayList<>(f_al[i]);
			for (int x = i + 1; x < p; x++) {
				temp.retainAll(f_al[x]);
			}
		}
		int x = 0;
		for (int i = 0; i < p; i++) {
			if (f_al[i].size() >= K)
				Make_list_kclique(day, x++, f_al[i]);
		}
	}

	private void Make_list_kclique(int i, int j, ArrayList<Integer> arrayList) {
		// Add a new K-clique to the list
		ArrayList<Integer> li = null;
		if (kClique_list.containsKey(i)) {
			li = kClique_list.get(i).get(j);
		}
		if (li == null) {
			li = new ArrayList<>();
			li.addAll(arrayList);
			Map<Integer, ArrayList<Integer>> innerkey = kClique_list.get(i);
			if (innerkey == null) {
				kClique_list.put(i, innerkey = new HashMap<>());
			}
			innerkey.put(j, li);
		}
	}

	// Flush temporary data for the next day's processing
	private void flush() {
		p = 0;
		for (int i = 0; i < n; i++) {
			if (f_al[i] != null) {
				f_al[i].clear();
				al[i].clear();
			}
		}
	}

    
	@Override
	public void hostsDisconnected(DTNHost host1, DTNHost host2) {
		// TODO Auto-generated method stub	
	}
	

	@Override
	public void updated(List<DTNHost> hosts) {
	   // TODO Auto-generated method stub
	}
}

