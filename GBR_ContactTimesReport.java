package report.GBR_Report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import core.ConnectionListener;
import core.DTNHost;
import core.SimClock;
import core.UpdateListener;
import report.Report;

public class GBR_ContactTimesReport extends Report implements ConnectionListener, UpdateListener {

    /**
     * Contact times are mapped to integer values:
     * 3645 seconds/3600 = 1, representing hourly intervals.
     * PairWiseTime stores contact times as:
     * Key1 (host address), Key2 (another host address), [1, 1, 2, 3...]
     */
    HashMap<Integer, Map<Integer, ArrayList<Integer>>> PairWiseTime = new 
                 HashMap<Integer, Map<Integer, ArrayList<Integer>>>();

    /**
     * PairWiseTime_ndup stores contact times without duplicates:
     * Key1, Key2, [1, 2, 3...]
     */
    HashMap<Integer, Map<Integer, ArrayList<Integer>>> PairWiseTime_ndup = new 
                 HashMap<Integer, Map<Integer, ArrayList<Integer>>>();

    @Override
    public void updated(List<DTNHost> hosts) {
        // This method is required by the UpdateListener interface but is not used in this implementation.
    }

    /**
     * Records the time when two hosts (host1 and host2) are in contact.
     */
    @Override
    public void hostsConnected(DTNHost host1, DTNHost host2) {
        // Calculate the contact time in hourly intervals.
        int time = (int) SimClock.getIntTime() / 3600;
        // Record contact times bidirectionally for host1 and host2.
        Connect_time(host1.getAddress(), host2.getAddress(), time);
        Connect_time(host2.getAddress(), host1.getAddress(), time);
    }

    /**
     * Records the contact time between two hosts.
     * 
     * @param address  Address of the first host.
     * @param address2 Address of the second host.
     * @param time     Contact time in hourly intervals.
     */
    private void Connect_time(int address, int address2, int time) {
        ArrayList<Integer> li = null;
        
        // Check if a mapping for the first host exists.
        if (PairWiseTime.containsKey(address)) {
            li = PairWiseTime.get(address).get(address2);
        }

        if (li == null) {
            // If no mapping exists, initialize a new list and map it.
            li = new ArrayList<>();
            li.add(time);
            Map<Integer, ArrayList<Integer>> innerkey = PairWiseTime.get(address);
            if (innerkey == null) {
                PairWiseTime.put(address, innerkey = new HashMap<>());
            }
            innerkey.put(address2, li);
        } else {
            // If mapping exists, add the contact time to the list.
            li.add(time);
        }
    }

    @Override
    public void hostsDisconnected(DTNHost host1, DTNHost host2) {
        // This method is required by the ConnectionListener interface but is not used in this implementation.
    }

    /**
     * Called when the simulation finishes, generates a report of the contact times.
     */
    public void done() {
        // Remove duplicates from PairWiseTime and populate PairWiseTime_ndup.
        for (Map.Entry<Integer, Map<Integer, ArrayList<Integer>>> t : this.PairWiseTime.entrySet()) {
            Integer key = t.getKey();
            for (Map.Entry<Integer, ArrayList<Integer>> e : t.getValue().entrySet()) {
                removedup(key, e.getKey(), PairWiseTime.get(key).get(e.getKey()));
            }
        }

        // Write the final report based on PairWiseTime_ndup.
        for (Map.Entry<Integer, Map<Integer, ArrayList<Integer>>> t : this.PairWiseTime_ndup.entrySet()) {
            Integer key = t.getKey();
            for (Map.Entry<Integer, ArrayList<Integer>> e : t.getValue().entrySet()) {
                write(key + " " + e.getKey() + " " + PairWiseTime_ndup.get(key).get(e.getKey()));
            }
        }
    }

    /**
     * Removes duplicate contact times for a pair of hosts and sorts the list.
     * 
     * @param key       Address of the first host.
     * @param key2      Address of the second host.
     * @param arrayList List of contact times for the pair of hosts.
     */
    private void removedup(Integer key, Integer key2, ArrayList<Integer> arrayList) {
        ArrayList<Integer> li = null;

        // Check if the duplicate-free mapping for the first host exists.
        if (PairWiseTime_ndup.containsKey(key)) {
            li = PairWiseTime_ndup.get(key).get(key2);
        }

        if (li == null) {
            // Initialize a new list to hold unique contact times.
            int temp = 0;
            li = new ArrayList<>();
            for (Integer i : arrayList) {
                // Add the first contact time or any new unique time.
                if (temp == 0) {
                    li.add(arrayList.get(0));
                    temp = 1;
                } else if (!li.contains(i)) {
                    li.add(i);
                }
            }

            // Sort the list of contact times.
            Collections.sort(li);

            // Map the duplicate-free list back to the PairWiseTime_ndup structure.
            Map<Integer, ArrayList<Integer>> innerkey = PairWiseTime_ndup.get(key);
            if (innerkey == null) {
                PairWiseTime_ndup.put(key, innerkey = new HashMap<>());
            }
            innerkey.put(key2, li);
        }
    }
}
