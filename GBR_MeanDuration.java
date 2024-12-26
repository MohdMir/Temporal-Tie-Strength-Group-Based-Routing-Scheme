package report.GBR_Report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.ConnectionListener;
import core.DTNHost;
import core.SimClock;
import core.UpdateListener;
import report.Report;

/**
 * GBR_MeanDuration calculates the mean duration of contact between hosts
 * and provides statistical insights about inter-contact times in a DTN simulation.
 */
public class GBR_MeanDuration extends Report implements ConnectionListener, UpdateListener {

    // Maps to store connection start times (Time_up) and disconnection times (Time_down)
    HashMap<Integer, Map<Integer, ArrayList<Double>>> Time_up = 
                                new HashMap<>();
    HashMap<Integer, Map<Integer, ArrayList<Double>>> Time_down = 
                                new HashMap<>();
    
    // Stores the average duration of contact between pairs of nodes
    HashMap<Integer, Map<Integer, Double>> Average_Dur_Time = 
                                new HashMap<>();
    
    // Stores duration per time period T_i for node pairs
    HashMap<Integer, Map<Integer, ArrayList<Double>>> Duration_perT = 
                                new HashMap<>();

    /**
     * Triggered when two hosts connect.
     * Stores the connection start time.
     */
    @Override
    public void hostsConnected(DTNHost host1, DTNHost host2) {
        Connected_Time(host1.getAddress(), host2.getAddress(), SimClock.getTime()); // step 1
        Connected_Time(host2.getAddress(), host1.getAddress(), SimClock.getTime()); // step 1
    }

    /**
     * Records the connection start time between two nodes.
     */
    private void Connected_Time(int address, int address2, double time) {
        ArrayList<Double> li = null;
        if (Time_up.containsKey(address)) {
            li = Time_up.get(address).get(address2);
        }
        if (li == null) {
            li = new ArrayList<>();
            li.add(time);
            Map<Integer, ArrayList<Double>> innerkey = Time_up.get(address);
            if (innerkey == null) {
                Time_up.put(address, innerkey = new HashMap<>());
            }
            innerkey.put(address2, li);
        } else {
            li.add(time);
        }
    }

    /**
     * Triggered when two hosts disconnect.
     * Stores the disconnection time.
     */
    @Override
    public void hostsDisconnected(DTNHost host1, DTNHost host2) {
        Disconnected_Time(host1.getAddress(), host2.getAddress(), SimClock.getTime()); // step 2
        Disconnected_Time(host2.getAddress(), host1.getAddress(), SimClock.getTime()); // step 2
    }

    /**
     * Records the disconnection time between two nodes.
     */
    private void Disconnected_Time(int address, int address2, double time) {
        ArrayList<Double> li = null;
        if (Time_down.containsKey(address)) {
            li = Time_down.get(address).get(address2);
        }
        if (li == null) {
            li = new ArrayList<>();
            li.add(time);
            Map<Integer, ArrayList<Double>> innerkey = Time_down.get(address);
            if (innerkey == null) {
                Time_down.put(address, innerkey = new HashMap<>());
            }
            innerkey.put(address2, li);
        } else {
            li.add(time);
        }
    }
    
    /**
     * Finalizes calculations and writes results to the output.
     */
    public void done() {
        // Calculate durations and average inter-contact times
        for (Map.Entry<Integer, Map<Integer, ArrayList<Double>>> t : this.Time_up.entrySet()) {
            Integer key = t.getKey();
            for (Map.Entry<Integer, ArrayList<Double>> e : t.getValue().entrySet()) {
                Calculate_Duration(key, e.getKey(), Time_up.get(key).get(e.getKey()), 
                                    Time_down.get(key).get(e.getKey()));
            }
        }

        // Write average durations to output
        for (Entry<Integer, Map<Integer, Double>> t : this.Average_Dur_Time.entrySet()) {
            Integer key = t.getKey();
            for (Entry<Integer, Double> e : t.getValue().entrySet()) {
                write(key + "," + e.getKey() + "," + Average_Dur_Time.get(key).get(e.getKey())); 
            }
        }

        // Write durations per time period T_i to output
        for (Map.Entry<Integer, Map<Integer, ArrayList<Double>>> t : this.Duration_perT.entrySet()) {
            Integer key = t.getKey();
            for (Map.Entry<Integer, ArrayList<Double>> e : t.getValue().entrySet()) {
                write(key + "," + e.getKey() + "," + Duration_perT.get(key).get(e.getKey()));
            }
        }
        super.done();
    }

    /**
     * Calculates durations, average durations, and updates results.
     */
    private void Calculate_Duration(Integer key, Integer key2, ArrayList<Double> arrayList,
                                    ArrayList<Double> arrayList2) {
        double sum = 0;
        double val = 0;
        double dur_ti = 0;
        int dur_length = 0;
        int T_i = 86400; // 24 hours in seconds

        if (arrayList.size() > 1) {
            for (int i = 0; i < arrayList2.size(); i++) {
                if (i == 0) {
                    dur_ti = arrayList2.get(i) - arrayList.get(i);
                    val = arrayList.get(i);
                    dur_length++;
                } else {
                    if (arrayList.get(i) - val < T_i) {
                        dur_ti += (arrayList2.get(i) - arrayList.get(i));
                        dur_length++;
                    } else {
                        update_dur(key, key2, dur_ti / dur_length);
                        dur_ti = arrayList2.get(i) - arrayList.get(i);
                        val = arrayList.get(i);
                        dur_length = 1;
                    }
                }
                sum += (arrayList2.get(i) - arrayList.get(i));
            }
            Updat_avg_cont_dur(key, key2, (sum / (arrayList.size() - 1)));
        }
    }

    /**
     * Updates durations per time period T_i for node pairs.
     */
    private void update_dur(Integer key, Integer key2, double d) {
        ArrayList<Double> li = null;
        if (Duration_perT.containsKey(key)) {
            li = Duration_perT.get(key).get(key2);
        }
        if (li == null) {
            li = new ArrayList<>();
            li.add(d);
            Map<Integer, ArrayList<Double>> innerkey = Duration_perT.get(key);
            if (innerkey == null) {
                Duration_perT.put(key, innerkey = new HashMap<>());
            }
            innerkey.put(key2, li);
        } else {
            li.add(d);
        }
    }

    /**
     * Updates the average inter-contact duration for node pairs.
     */
    private void Updat_avg_cont_dur(Integer i, Integer j, double d) {
        Double ti = null;
        if (Average_Dur_Time.containsKey(i)) {
            ti = Average_Dur_Time.get(i).get(j);
        }
        if (ti == null) {
            Map<Integer, Double> innerkey = Average_Dur_Time.get(i);
            if (innerkey == null) {
                Average_Dur_Time.put(i, innerkey = new HashMap<>());
            }
            innerkey.put(j, d);
        }
    }

    /**
     * Periodic update method (currently not implemented).
     */
    @Override
    public void updated(List<DTNHost> hosts) {
        // TODO Auto-generated method stub
    }
}
