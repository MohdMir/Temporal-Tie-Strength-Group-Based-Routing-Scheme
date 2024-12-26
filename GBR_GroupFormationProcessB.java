package report.GBR_Report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GBR_GroupFormationProcessB {

    //GBR parameters for group consistency and duration-frequency conditions
    double gamma = 0.5; // Group consistency condition
    double delta = 0.5; // Node frequency condition

    /**
     * Finalizes group formation based on GBR conditions.
     * 
     * @param day        Current day (T_i)
     * @param kcliqList2 Original k-clique list
     * @param kclqNr     Index of max(k-clique)
     * @param kClique_list K-clique data for all days
     * @param nrofdays   Total number of days
     * @return Final group as an array of ArrayLists
     */
    public ArrayList<Integer>[] FinalizeGroup(
    		int day, ArrayList<Integer>[] kcliqList2, int kclqNr,
            HashMap<Integer, Map<Integer, ArrayList<Integer>>> kClique_list, int nrofdays
            ) {
        // Initialize array to store k-cliques for each day
        @SuppressWarnings("unchecked")
        ArrayList<Integer>[] KcliqList = new ArrayList[kClique_list.get(day).size()];
        for (int i = 0; i < kClique_list.get(day).size(); i++) {
            KcliqList[i] = new ArrayList<>();
        }

        // Counter for tracking occurrences of the same max(k-clique)
        int counter = 0;
        int k_i = 0;

        // Get the initial final_kClique as max(k-clique) from the input
        ArrayList<Integer> final_kClique = new ArrayList<>(kcliqList2[kclqNr]);

        // Iterate through kClique_list for all days
        for (Map.Entry<Integer, Map<Integer, ArrayList<Integer>>> t : kClique_list.entrySet()) {
            ArrayList<Integer> temp = new ArrayList<>(final_kClique);
            int p = 0;
            Integer key = t.getKey();

            if (key > day) { // Only consider future days
                for (Map.Entry<Integer, ArrayList<Integer>> e : t.getValue().entrySet()) {
                    // Check for identical group by comparing intersections
                    Collections.sort(temp);
                    Collections.sort(kClique_list.get(key).get(e.getKey()));
                    temp.retainAll(kClique_list.get(key).get(e.getKey()));

                    if (temp.equals(final_kClique)) { // Same group found
                        counter++;
                        p = 1;
                        KcliqList[k_i] = new ArrayList<>(kClique_list.get(key).get(e.getKey()));
                        k_i++;
                        break;
                    }

                    temp.clear();
                    temp = new ArrayList<>(final_kClique);
                }

                if (p == 0) { // Group consistency check
                    for (Map.Entry<Integer, ArrayList<Integer>> e : t.getValue().entrySet()) {
                        ArrayList<Integer> Union = Get_UnionList(kClique_list.get(key).get(e.getKey()), temp);
                        temp.retainAll(kClique_list.get(key).get(e.getKey()));

                        double x = temp.size();
                        double y = Union.size();

                        if ((x / y) >= gamma) { // Consistency condition met
                            counter++;
                            for (Integer i : Union) {
                                if (!final_kClique.contains(i)) {
                                    final_kClique.add(i);
                                }
                            }
                            KcliqList[k_i] = new ArrayList<>(kClique_list.get(key).get(e.getKey()));
                            k_i++;
                            temp.clear();
                            break;
                        }
                        temp.clear();
                        temp = new ArrayList<>(final_kClique);
                    }
                }
            }
        }

        // Calculate group consistency ratio
        double cnt = counter;
        double nrdys = nrofdays;

        @SuppressWarnings("unchecked")
        ArrayList<Integer>[] ret = new ArrayList[2];

        if (cnt / nrdys < gamma) { // Group consistency not met
            ret[0] = new ArrayList<>(kcliqList2[kclqNr]);
            ret[1] = new ArrayList<>();
            ret[1].add(0); // Mark as not consistent
            return ret;
        } else { // Frequency condition check for each node
            final_kClique = Check_Group_Condition(counter, final_kClique, KcliqList);
            ret[0] = new ArrayList<>(final_kClique);
            ret[1] = new ArrayList<>();
            ret[1].add(1); // Mark as consistent
            return ret;
        }
    }

    /**
     * Checks frequency conditions for each node in the final k-clique.
     * 
     * @param counter    Occurrence count of max(k-clique)
     * @param final_kClique Current k-clique being evaluated
     * @param kcliqList  List of k-cliques
     * @return Updated k-clique after frequency checks
     */
    private ArrayList<Integer> Check_Group_Condition(int counter, ArrayList<Integer> final_kClique,
                                                      ArrayList<Integer>[] kcliqList) {
        ArrayList<Integer> Temp1 = new ArrayList<>(final_kClique);
        Collections.sort(Temp1);
        final_kClique.clear();

        for (Integer i : Temp1) {
            int fr_i = 0;
            int j = 0;
            while (kcliqList[j].size() > 0) {
                if (kcliqList[j].contains(i)) {
                    fr_i++;
                }
                j++;
            }

            double x = fr_i;
            double y = counter;
            if (x / y > delta) { // Frequency condition met
                final_kClique.add(i);
            }
        }
        return final_kClique;
    }

    /**
     * Generates the union of two lists.
     * 
     * @param arrayList First list
     * @param temp      Second list
     * @return Union of both lists
     */
    private ArrayList<Integer> Get_UnionList(ArrayList<Integer> arrayList, ArrayList<Integer> temp) {
        ArrayList<Integer> temp2 = new ArrayList<>(temp);
        for (Integer i : arrayList) {
            if (!temp2.contains(i)) {
                temp2.add(i);
            }
        }
        return temp2;
    }
}
