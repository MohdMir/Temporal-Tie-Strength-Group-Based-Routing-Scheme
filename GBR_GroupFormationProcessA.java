package report.GBR_Report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class helps in extracting Groups from KClique_lists formed in Time T_i (day) according 
 * to GBRouting scheme. We divide it into two Classes: GBR_getMaxKClique and GBR_makeKclique  
 * @author Mohd Yaseen Mir
 */
public class GBR_GroupFormationProcessA {
	
	static int gbn = 0; // Group number tracker
	int K; // Minimum size of a K-clique
           
	/**
	 * Processes kClique_list to extract groups based on GBR scheme.
	 * @param kClique_list Input kClique lists grouped by days.
	 * @param Group Initially empty groups to be formed.
	 * @param day Current day (T_i).
	 * @param K Minimum size of k-cliques.
	 * @return Updated Group array containing the formed groups.
	 */
	public ArrayList<Integer>[] KClqandGroupinfo(HashMap<Integer, Map<Integer, ArrayList<Integer>>> 
	       kClique_list, ArrayList<Integer>[] Group, int day, int K) {  
		  
		this.K = K; // Initialize minimum K-clique size
		int nrofdays = 6; // Total time duration
	    int KclqNr = -1; // Index of the largest K-clique in the list
	    int step = 0; // Step in the process
	    int daydone = 0; // Completion flag for the current day
	      
	    @SuppressWarnings("unchecked")
	    ArrayList<Integer>[] ret = new ArrayList[2]; // Temporary storage for results
		  
	    // Store kClique_list in KcliqList
	    @SuppressWarnings("unchecked")
		ArrayList<Integer>[] KcliqList = new ArrayList[kClique_list.get(day).size()]; 
		
	    for (int i = 0; i < kClique_list.get(day).size(); i++) {
			KcliqList[i] = new ArrayList<Integer>(kClique_list.get(day).get(i));
		}
		
		int upd = 0; // Update counter
		GBR_GroupFormationProcessB gm = new GBR_GroupFormationProcessB();
		while (daydone != 1) {
			  
			if (step == 0) {
				  
				 // Step 0: Find the largest K-clique for the first time
			     KclqNr = GBR_getMaxKClique(day, kClique_list, KcliqList, step, Group);
			     step = 1;
			     
			} else {
				  
				 // Update K-clique list and find the next largest K-clique
				 upd++;
			     KcliqList = Update(KclqNr, KcliqList); // Update the K-clique list
				 KclqNr = GBR_getMaxKClique(day, kClique_list, KcliqList, step, Group);
				 
				 if (KclqNr == -1) {
				   // Exit if no more valid K-cliques exist
				  break;
				 }
			}
			  
			// Finalize the group based on the current largest K-clique
			ret = gm.FinalizeGroup(day, KcliqList, KclqNr, kClique_list, nrofdays);
			if (ret[1].get(0) == 1) { // Successful group formation
			  
				if (gbn == 0) {
		        	Group[gbn] = new ArrayList<Integer>(ret[0]);
		        	gbn++;
				} else {
					// Check if the group already exists
					if (Chek_Grop_Exists(Group, ret[0]) == 1) {
						Group[gbn] = new ArrayList<Integer>(ret[0]);
						gbn++;
					}
				}
			}
			  
			if (upd > 3) // Stop after too many updates
				 daydone = 1;
		}
		
	  return Group;
    }

	/**
	 * Checks if a group already exists in the formed groups.
	 * @param Group Existing groups.
	 * @param ret Candidate group to check.
	 * @return 1 if the group does not exist, 0 otherwise.
	 */
	private int Chek_Grop_Exists(ArrayList<Integer>[] Group, ArrayList<Integer> ret) {
		int x = 0;
		while (Group[x].size() != 0) {
			ArrayList<Integer> temp = new ArrayList<Integer>(ret); 	
			temp.removeAll(Group[x]);	
			if (temp.size() <= K) 
				return 0;
			x++;		
		}
		return 1;
	}

	/**
	 * Finds the largest K-clique that meets the GBR conditions.
	 * @param day Current day.
	 * @param kClique_list Input kClique lists grouped by days.
	 * @param KcliqList List of K-cliques to search.
	 * @param step Current step in the process.
	 * @param Group Existing groups.
	 * @return Index of the largest K-clique or -1 if none found.
	 */
	private int GBR_getMaxKClique(int day, HashMap<Integer, Map<Integer, ArrayList<Integer>>> 
					kClique_list, ArrayList<Integer>[] KcliqList, int step, ArrayList<Integer>[] Group) {
		  int max = 0;
		  int KclqNr = -1; 
		  
		  if (step == 0) { // No group formed yet
			for (Map.Entry<Integer, Map<Integer, ArrayList<Integer>>> t : kClique_list.entrySet()) {
			  if (t.getKey() == day) {
				  for (Map.Entry<Integer, ArrayList<Integer>> e : t.getValue().entrySet()) {
					  if (kClique_list.get(day).get(e.getKey()).size() > max) {
						  max =  kClique_list.get(day).get(e.getKey()).size();
						  KclqNr = e.getKey();
					  }
				  }
			  }
			} 
		 } else {
			 // Check intersection of K-cliques with existing groups
			 // Logic for subsequent K-clique selection
			 @SuppressWarnings("unchecked")
			   ArrayList<Integer> [] temp =  new ArrayList[KcliqList.length];
			 
			   for(int t = 0 ; t < KcliqList.length; t++) {
				  temp[t] = new ArrayList<Integer>(KcliqList[t]);
			   }
			   
			   int done = 0 ; // When Next Kcliq selected
			   int Kclq_delt = 0 ; // used for updating temp
			   
			   while (done != 1) {
				   
				   max = 0 ;
				   int Gr_n = 0 ; // Group Number
				   for(int t = 0 ; t < (temp.length-Kclq_delt); t++){ // select max first
			    	   if(temp[t].size()>max) {
			    		   max = temp[t].size();
			    		   KclqNr = t ;
			    	   }
			       }
				   
				 //Check selected List(Max) - (List intersect Group[0])
		 	       ArrayList<Integer> origlist = new ArrayList<Integer>(temp[KclqNr]);
		 	       ArrayList<Integer> Comelm = new ArrayList<Integer>(temp[KclqNr]);   
		 	       Comelm.retainAll(Group[Gr_n]);
		 	       origlist.removeAll(Comelm);
		 	       
		 	       if(origlist.size() >= K) { // K-clique condition for Group
		 	    	   done = 1 ;
		 	    	  break ;
		 	       }else {
		 	    	   temp[KclqNr].removeAll(temp[KclqNr]); // here we try to remove the Max obtained
		 	           int tempx = 0 ;
		 	           for(int t = 0 ; t < temp.length-Kclq_delt ; t++) {
		 	              if(temp[t].size()>0) {
		 	            	  temp[tempx] = new ArrayList<Integer>(temp[t]);
		 	            	  tempx++ ;
		 	              }
		 	            }
		 	            Kclq_delt++ ;
		 	            temp[(temp.length-Kclq_delt)].removeAll(temp[(temp.length-Kclq_delt)]); 
		 	       }
		 	       if(temp[0].size() == 0) {
		 	    	   KclqNr =  -1 ;
		 	    	  break ;
		 	       }
			   }
			   
			   if(KclqNr != -1) {
				   for(int y = 0 ; y <= KcliqList.length ; y++) {
					   if(KcliqList[y].equals(temp[KclqNr]))  {
						   KclqNr = y ;
						   break ;
					   }
				   }
			   }
		 }
		  
		return KclqNr;
	}
	
	/**
	 * Updates the K-clique list by removing the used clique.
	 * @param kclqNr Index of the used clique.
	 * @param kcliqList Existing K-clique list.
	 * @return Updated K-clique list.
	 */
	private ArrayList<Integer>[] Update(int kclqNr, ArrayList<Integer>[] kcliqList) {
		   @SuppressWarnings("unchecked")
		   ArrayList<Integer>[] temp = new ArrayList[kcliqList.length - 1];
		   int t = 0;
		   for (int i = 0; i < kcliqList.length; i++) {
			 if ((i != kclqNr)) { 
				 temp[t] =  new ArrayList<Integer>(kcliqList[i]);
				 t++;
			 }
		   }
		return temp;
	}
}
