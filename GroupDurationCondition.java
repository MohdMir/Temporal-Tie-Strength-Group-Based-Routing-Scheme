import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;


public class GroupDurationCondition {

        /**
        * This class applies duration condition to filter groups further.
        * It removes members from the group if the average duration of their connections 
          does not meet the specified threshold (beta).
        */

        // Stores duration per time period T_i for node pairs
        HashMap<Integer, Map<Integer, ArrayList<Double>>> Duration_perT = new HashMap<>();

        int T_i ;
        int N ;
        double beta ;

        public void CheckDuration(ArrayList<Integer>[] Group, int T_i, int N, double beta){
        
        this.T_i = T_i ;
        this.N = N ;
        this.beta = beta ;
        // Path to the file
        String filePath = "file2.txt"; // using @GBR_MeanDuration.java
        try {

            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {

                // Split the line by commas
                String[] parts = line.split(",", 3);
                if (parts.length >= 3) {

                    String key1 = parts[0].trim(); // First key
                    String key2 = parts[1].trim(); // Second key
                    String valueList = parts[2].trim(); // The third part, which should be a list

                    // Parse the keys
                    int i = Integer.parseInt(key1);
                    int j = Integer.parseInt(key2);

                    // Parse the list of integers in the third part
                    valueList = valueList.replaceAll("[\\[\\] ]", ""); 
                    String[] valueArray = valueList.split(","); 
                    
                    ArrayList<Double> valueList_update = new ArrayList<>();
                    for (String val : valueArray) {
                        valueList_update.add(Double.parseDouble(val));
                    }
                    // Retrieve or initialize the inner map
                    Map<Integer, ArrayList<Double>> innerMap = Duration_perT.get(i);
                    if (innerMap == null) {
                        innerMap = new HashMap<>();
                        Duration_perT.put(i, innerMap);
                    }
                    ArrayList<Double> li = innerMap.get(j);
                    if (li == null) {
                        li = new ArrayList<>();
                        innerMap.put(j, li);
                    }
                    li.addAll(valueList_update); // Add all values to the list
                }
            }
            reader.close();
        } catch (IOException e) {
                e.printStackTrace();
        }  
    
      int x = 0 ;
	  double sum = 0.0 ;
	  while(Group[x] != null) {
		  double ret = 0.0 ;
          Iterator<Integer> iterator = Group[x].iterator();
            while (iterator.hasNext()) {
                Integer el = iterator.next();
                    for (int i = 0; i < Group[x].size(); i++) {
                        if (Group[x].get(i) != el && Duration_perT.get(el).get(Group[x].get(i)) != null) {
                            ret = Calculate_Eq12(Duration_perT.get(el).get(Group[x].get(i)));
                        }
                    }
                sum = sum + ret;
                if (sum / (Group[x].size() - 1) < beta) {
                    iterator.remove();  // Safely remove the element
                }
            }
        x++;
	  }  
    }  
    
    // Duration calculation eq 12 from our paper
	private double Calculate_Eq12(ArrayList<Double> arrayList) {
        double value = 0.0 ;
        for(Double i : arrayList) {
            value = value + (double)Math.round((i/T_i)*10000)/10000 ;
        }
       return value/(N+1) ;
      }
}
