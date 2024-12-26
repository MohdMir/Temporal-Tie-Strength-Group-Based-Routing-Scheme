package report.GBR_Report;

import core.ConnectionListener;
import core.DTNHost;
import core.SimClock;
import core.UpdateListener;
import report.Report;

import java.util.*;
import java.util.Map.Entry;

/**
 * EncountersReport For GBR.
 * @author Mohd Yaseen Mir, National Central University, Taiwan.
 */

public class GBR_MeanICT extends Report implements ConnectionListener, UpdateListener {

    // Stores the encounter time-stamps for each pair of nodes
    HashMap<Integer, Map<Integer, ArrayList<Double>>> Encounter_Time = 
                                new HashMap<Integer, Map<Integer, ArrayList<Double>>>();
    
    // Stores the average inter-contact time for each node-pair
    HashMap<Integer, Map<Integer, Double>> Average_Encounter_Time = 
                                new HashMap<Integer, Map<Integer, Double>>();
    
    // Placeholder for storing additional group-related rates (if required)
    HashMap<Integer, Map<Integer, Double>> GroupRate = new HashMap<Integer, Map<Integer, Double>>();
    
    // Maintains a list of contacts for each node
    HashMap<Integer, ArrayList<Integer>> ContactList = new HashMap<Integer, ArrayList<Integer>>();

    /**
     * Called when two hosts connect.
     * Records the connection time and updates contact lists for both nodes.
     * @param host1 First host in the connection
     * @param host2 Second host in the connection
     */
    public void hostsConnected(DTNHost host1, DTNHost host2) {
        if (isWarmup()) {
            // Ignore connections during the warmup phase
            return;
        }
        // Record encounter times in both directions
        Calculate_Encounter_Time(host1.getAddress(), host2.getAddress(), SimClock.getTime());
        Calculate_Encounter_Time(host2.getAddress(), host1.getAddress(), SimClock.getTime());
        
        // Update contact lists for both nodes
        updatecontactlist(host1.getAddress(), host2.getAddress());
        updatecontactlist(host2.getAddress(), host1.getAddress());
    }

    /**
     * Updates the encounter time-stamps for a node pair.
     * @param i Address of the first node
     * @param j Address of the second node
     * @param t Current simulation time
     */
    public void Calculate_Encounter_Time(Integer i, Integer j, Double t) { 
        ArrayList<Double> ti = null;
        if (Encounter_Time.containsKey(i)) {
            // Get the existing time-stamps for the pair
            ti = Encounter_Time.get(i).get(j);
        }
        if (ti == null) {
            // Initialize a new time-stamp list if none exists
            ti = new ArrayList<Double>();
            ti.add(t);
            Map<Integer, ArrayList<Double>> innerkey = Encounter_Time.get(i);
            if (innerkey == null) {
                Encounter_Time.put(i, innerkey = new HashMap<>());
            }
            innerkey.put(j, ti);
        } else {
            // Append the new time-stamp
            ti.add(t);
        }
    }

    /**
     * Finalizes and writes the average encounter times to output.
     */
    public void done() {
        // Calculate the mean encounter time for each node pair
        for (Map.Entry<Integer, Map<Integer, ArrayList<Double>>> t : this.Encounter_Time.entrySet()) {
            Integer key = t.getKey();
            for (Map.Entry<Integer, ArrayList<Double>> e : t.getValue().entrySet()) {
                // Compute the average inter-contact time
                Double ti = Calculate_Average_t(e.getValue());
                // Update the average encounter time map
                Update_Avergae_Encounter_Time(key, e.getKey(), ti);
            }
        }

        // Print the average encounter times for all node pairs
        for (Entry<Integer, Map<Integer, Double>> t : this.Average_Encounter_Time.entrySet()) {
            Integer key = t.getKey();
            for (Entry<Integer, Double> e : t.getValue().entrySet()) {		  
                write(key + "," + e.getKey() + "," + Average_Encounter_Time.get(key).get(e.getKey()));
            }
        }

        super.done();
    }

    /**
     * Calculates the average inter-contact time for a list of time-stamps.
     * @param t List of time-stamps
     * @return Average inter-contact time
     */
    private Double Calculate_Average_t(ArrayList<Double> t) {
        double sum = 0.0;
        if (t.size() < 2) {
            return 0.0; // No valid inter-contact time if fewer than two entries
        } else {
            for (int i = 0; i < t.size(); i++) {
                if (i > 0) {
                    double j = t.get(i) - t.get(i - 1);
                    sum = sum + j;
                }
            }
            return sum / (t.size() - 1); // Compute mean/Avg.
        }
    }

    /**
     * Updates the average inter-contact time for a node pair.
     * @param i Address of the first node
     * @param j Address of the second node
     * @param t Average inter-contact time
     */
    public void Update_Avergae_Encounter_Time(Integer i, Integer j, Double t) {
        Double ti = null;
        double t1;
        if (t != 0) {
            t1 = (double) 1 / Math.log10(t); // Apply a transformation to the average time
        } else {
            t1 = 0;
        }
        if (Average_Encounter_Time.containsKey(i)) {
            ti = Average_Encounter_Time.get(i).get(j);
        }
        if (ti == null) {
            // Initialize and update the average time
            Map<Integer, Double> innerkey = Average_Encounter_Time.get(i);
            if (innerkey == null) {
                Average_Encounter_Time.put(i, innerkey = new HashMap<>());
            }
            innerkey.put(j, (double) Math.round(t1 * 1000) / 1000);
        }
    }

    /**
     * Updates the contact list for a node by adding the connected node.
     * @param node1 Address of the first node
     * @param node2 Address of the second node
     */
    private void updatecontactlist(int node1, int node2) {
        ArrayList<Integer> li = ContactList.get(node1);
        if (li == null) {
            li = new ArrayList<Integer>();
            li.add(node2);
            ContactList.put(node1, li);
        } else {
            if (!li.contains(node2)) li.add(node2);
        }
    }


    public GBR_MeanICT() {
        // Default constructor
    }

	@Override
	public void updated(List<DTNHost> hosts) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hostsDisconnected(DTNHost host1, DTNHost host2) {
		// TODO Auto-generated method stub
		
	}
}
