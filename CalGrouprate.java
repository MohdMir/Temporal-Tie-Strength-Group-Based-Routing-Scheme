
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CalGrouprate {

    // Here Average_Encounter_Time will store the Average inter-contact time
    HashMap<Integer, Map<Integer, ArrayList<String>>> Lambda = new HashMap<>();
    HashMap<Integer, Map<Integer, Double>> GroupRate = new HashMap<>();

    int N ;
    public void Calculate_Grate(ArrayList<Integer>[] Group, int N){

        String filePath = "file3.txt"; // using @GBR_MeanICT.java
        this.N = N ;
        try {

            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                
                if(parts.length >= 3) {

                    ArrayList<String> li = null ;
                    String key1 = parts[0];
                    int i = Integer.parseInt(key1);

                    String key2 = parts[1];
                    int j = Integer.parseInt(key2);
                    
                    String value = parts[2];  
                    
                    if(Lambda.containsKey(i)) {
                       li = Lambda.get(i).get(j);
                    }if(li == null) {
                       li = new ArrayList<String>();
                       li.add(value);
                       Map<Integer, ArrayList<String>> innerkey  = Lambda.get(i);
                       if(innerkey == null) {
                        Lambda.put(i, innerkey = new HashMap<>());
                       }
                        innerkey.put(j, li);
                      } else {
                        li.add(value);
                      }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calculate Group rate
        int i = 0 ;
        while (Group[i] != null) {
            
            int j = 0 ;
            while(Group[j] != null){
                
                if(i != j) {
                   double val = calculate_rate(Group[i], Group[j]);
                   Update_Grouprate(i,j,val);
                }
                j++;
            }
            i++ ;
        }

        // Output
        writeToFile();

    }

    private double calculate_rate(ArrayList<Integer> arrayList, 
			                           ArrayList<Integer> arrayList2) {
		double sum = 0 ;
		for(int i = 0 ; i < arrayList.size();i++) {
		int ni = arrayList.get(i);
		  for(int j= 0 ; j < arrayList2.size();j++) {
			int nj = arrayList2.get(j);
			if(Lambda.containsKey(ni)&& Lambda.get(ni).get(nj) != null)
			   sum = sum + Double.parseDouble(Lambda.get(ni).get(nj).get(0));  
		  }
		}
	  return (double)Math.round((sum/arrayList.size())*10000)/10000;
	}

    private void Update_Grouprate(int i, int j, double val) {
		
		Double ti = null ;
		if(GroupRate.containsKey(i)) {
			ti = GroupRate.get(i).get(j);
		}if(ti == null) {
			 Map<Integer, Double> innerkey = GroupRate.get(i);
 	          if(innerkey == null) {
 	        	 GroupRate.put(i, innerkey = new HashMap<>());
 	          	}
 	         	innerkey.put(j, val) ;
		}
	}

    // Write o/p

    private void writeToFile() {
        
        String filePath = "C:\\Users\\Kifayat\\Desktop\\Yaseen\\GBR_2024_07\\GBR_CodeJava\\file4.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<Integer, Map<Integer, Double>> outerEntry : GroupRate.entrySet()) {
                int i = outerEntry.getKey();
                for (Map.Entry<Integer, Double> innerEntry : outerEntry.getValue().entrySet()) {
                    int j = innerEntry.getKey();
                    double val = innerEntry.getValue();
                    writer.write(i + ", " + j + ", " + val);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}


