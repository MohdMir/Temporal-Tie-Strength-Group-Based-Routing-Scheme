package routing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import util.Tuple;

/**
 * This Router is designed to work as stated in GBR paper 2021.
 * @author Mohd Yaseen Mir, NCU, Taiwan.
 */
public class GBRouter extends ActiveRouter {

    // Store the number of samples, last contact time, and last mean time (tp) between nodes i and j
    HashMap<Integer, Map<Integer, ArrayList<String>>> Lambda = new HashMap<>();

    // Stores group-based transmission rates and probabilities
    HashMap<Integer, Map<Integer, ArrayList<String>>> Groupr = new HashMap<>();

    // Static constants for message properties representing group statuses
    public static final String GbRouter_NS = "GbRouter";
    public static final String G1 = "status_group1";
    public static final String G2 = "status_group2";
    public static final String G3 = "status_group3";
    public static final String G4 = "status_group4";
    public static final String Ms = "status_message"; // Inside the same group

    // Constructor with settings
    public GBRouter(Settings s) {
        super(s);
    }

    // Copy constructor, initializing rate calculations
    protected GBRouter(GBRouter r) {
        super(r);
        try {
            initratecal();
            initGrpratecal();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new message and sets its properties.
     */
    @Override
    public boolean createNewMessage(Message msg) {
        makeRoomForNewMessage(msg.getSize()); // Ensure buffer space for the new message
        msg.setTtl(this.msgTtl); // Set the Time-To-Live (TTL)
        addToMessages(msg, true); // Add the message to the router's buffer

        // Initialize group status properties
        msg.addProperty(G1, 0);
        msg.addProperty(G2, 0);
        msg.addProperty(G3, 0);
        msg.addProperty(G4, 0);
        msg.addProperty(Ms, 0);
        return true;
    }

    /**
     * Updates the router's state and attempts message transfer.
     */
    @Override
    public void update() {
        super.update();

        if (!canStartTransfer() || isTransferring()) {
            return; // Skip update if no transfer can occur or a transfer is ongoing
        }

        // Try delivering messages to their final recipients
        if (exchangeDeliverableMessages() != null) {
            return;
        }

        // Attempt to transfer other messages
        tryOtherMessages();
    }

    /**
     * Attempts to transfer other messages based on routing decisions.
     */
    private Tuple<Message, Connection> tryOtherMessages() {
        List<Tuple<Message, Connection>> messages = new ArrayList<>(); // Candidate messages for transfer
        Collection<Message> msgCollection = getMessageCollection();

        for (Connection con : getConnections()) {
            DTNHost other = con.getOtherNode(getHost()); // Get the connected host
            GBRouter othRouter = (GBRouter) other.getRouter();

            if (othRouter.isTransferring()) {
                continue; // Skip hosts currently transferring
            }

            for (Message m : msgCollection) {
                if (othRouter.hasMessage(m.getId())) {
                    continue; // Skip messages already in the connected host's buffer
                }

                if (other.getAddress() == m.getTo().getAddress()) {
                    // If the message is destined for the connected host, add it to the transfer list
                    messages.add(new Tuple<>(m, con));
                } else {
                	
                    // Handle inter-group or intra-group message transfers
                    int g_id = getOtherNode_Groupid(other.getAddress());
                    int g_id2 = getOtherNode_Groupid(this.getHost().getAddress());

                    Integer g1 = (Integer) m.getProperty(G1);
                    Integer g2 = (Integer) m.getProperty(G2);
                    Integer g3 = (Integer) m.getProperty(G3);
                    Integer g4 = (Integer) m.getProperty(G4);
                    Integer g = (Integer) m.getProperty(Ms);

                    if (g_id == g_id2) { // Inside the same group
                        if (g == 0 && calculate_prob_dest(this.getHost().getAddress(), m) > 
                        			  calculate_prob_dest(other.getAddress(), m)) {
                            messages.add(new Tuple<>(m, con));
                        }
                    } else {
                        // Check group-based transfer conditions
                        int ret = cal_prob(this.getHost().getAddress(), other.getAddress(), m);
                        switch (g_id) {
                            case 1:
                                if (g1 == 0 && ret == 1)
                                    messages.add(new Tuple<>(m, con));
                                break;
                            case 2:
                                if (g2 == 0 && ret == 1)
                                    messages.add(new Tuple<>(m, con));
                                break;
                            case 3:
                                if (g3 == 0 && ret == 1)
                                    messages.add(new Tuple<>(m, con));
                                break;
                            case 4:
                                if (g4 == 0 && ret == 1)
                                    messages.add(new Tuple<>(m, con));
                                break;
                        }
                    }
                }
            }
        }

        if (messages.isEmpty()) {
            return null; // No eligible messages for transfer
        }

        return tryMessagesForConnected(messages); // Attempt transfer for selected messages
    }

    /**
     * Handles post-transfer updates, adjusting message properties based on routing conditions.
     */
    @Override
    protected void transferDone(Connection con) {
        DTNHost other = con.getOtherNode(getHost());
        int g_id = getOtherNode_Groupid(other.getAddress());
        int g_id2 = getOtherNode_Groupid(this.getHost().getAddress());

        String msgId = con.getMessage().getId();
        Message msg = getMessage(msgId);

        if (msg == null) {
            return; // Skip if the message was dropped during transfer
        }

        // Update group status properties
        msg.updateProperty(G1, 1);
        msg.updateProperty(G2, 1);
        msg.updateProperty(G3, 1);
        msg.updateProperty(G4, 1);

        if (g_id == g_id2 && calculate_prob_dest(this.getHost().getAddress(), msg) > 
        					 calculate_prob_dest(other.getAddress(), msg)) {
            msg.updateProperty(Ms, 1);
        }
    }
 
    /**
     * This method should be called (on the receiving host) after a message was successfully transferred. 
     * The transferred message is put to the message buffer unless this host is the final recipient of the message.
     */
	@Override
	public Message messageTransferred(String id, DTNHost from) {
		Message msg = super.messageTransferred(id, from);
		if(this.getHost().getAddress() == msg.getTo().getAddress()) {
		}else {
	    int g_id = getOtherNode_Groupid(this.getHost().getAddress());
	    int g_id2 = getOtherNode_Groupid(from.getAddress());
	    if(g_id==g_id2) {
	    	
	    }else {
	  	switch(g_id) {
	  		 case 1:
	  		    msg.updateProperty(G1, 1);
	  		    break;
	  		 case 2:
	  		    msg.updateProperty(G2, 1);
	  		    break;
	  		 case 3:
	  		    msg.updateProperty(G3, 1);
	  		    break;
	  		 case 4:
	  		    msg.updateProperty(G4, 1);
	  		    break;
	  		 }
		} 
		}
	  	return msg;
	}
	
	// Function to Calculate Lambda_(x,y) based on network parameters
	public double Calculate_lam(int N, double last_tp, double last_ct ) {
	    // Calculate Lambda as (N+1) / (N * last_tp + (SimClock.getTime() - last_ct))
	    return (N+1)/(N*last_tp + (SimClock.getTime() - last_ct));  
	}

	// Function to Calculate the probability to the destination
	public double calculate_prob_dest(int n_ipt, Message m) {
	    int n_d = m.getTo().getAddress(); // Get the destination address from the message
	    // Check if the lambda value for the destination exists
	    if(Lambda.containsKey(n_ipt) && Lambda.get(n_ipt).get(n_d) != null) {
	        // Parse Lambda value and calculate TTL in minutes, rounded to 3 decimal places
	        double lam = Double.parseDouble(Lambda.get(n_ipt).get(n_d).get(0));
	        double ttl = (double)Math.round((double)m.getTtl()/60*1000)/1000;
	        // Return probability using exponential function for message TTL
	        return (double)Math.round((1 - Math.exp(-lam*ttl)) * 10000) / 10000; 
	    } else {
	        // Return 0 if no lambda value exists
	        return 0;
	    }
	}

	// Function to calculate the probability between two nodes n_i and n_j
	public int cal_prob(int n_i, int n_j, Message m) {
	    // Calculate probability to destination using the calculate_prob_dest function
	    double P_d = calculate_prob_dest(n_i, m); 
	    // If probability to destination is 0 or no rate exists, return 1 (default path)
	    if(P_d == 0 || (Lambda.containsKey(n_j) && Lambda.get(n_j).get(m.getTo().getAddress()) == null)) {
	        return 1;
	    } else {
	        double P_q1;
	        double lam1, lam2;
	        // Calculate TTL for message and round it to 3 decimal places
	        double ttl = (double)Math.round((double)m.getTtl()/60*1000)/1000;
	        
	        // Get group IDs for both nodes
	        int g_id = getOtherNode_Groupid(n_i);
	        int g_id2 = getOtherNode_Groupid(n_j);
	        
	        // Retrieve Lambda values from Groupr based on group IDs
	        lam1 = Double.parseDouble(Groupr.get(g_id).get(g_id2).get(0));
	        lam2 = Double.parseDouble(Groupr.get(g_id2).get(5).get(0));
	        
	        // Calculate probability for the transition (P_q1)
	        P_q1 = lam2 * (1 - Math.exp(-lam1 * ttl)) / (lam2 - lam1) 
	                + lam1 * (1 - Math.exp(-lam2 * ttl)) / (lam1 - lam2);
	        
	        // Round final probability to 4 decimal places
	        double P_qf = (double)Math.round(P_q1 * 10000) / 10000;
	        
	        // Compare probabilities and return 1 or 0 based on the condition
	        if(P_qf >= P_d)
	            return 1;
	        else
	            return 0;
	    }
	}

	// Node group assignments for various group IDs
	int[] Group0 = {5};
	int[] Group1 = {2, 10, 33, 35};
	int[] Group2 = {0, 1, 3, 4, 14, 18, 19, 32};
	int[] Group3 = {6, 11, 15, 20, 21, 24, 27, 28, 31};
	int[] Group4 = {9, 17, 22, 7, 8, 12, 16, 23, 25, 26, 29, 30, 34};
	int[] GroupF = {13};

	// Function to return a group based on group ID
	public int[] getgroup(int groupid) {
	    // Return corresponding group based on input group ID
	    switch(groupid) {
	        case 1:
	            return Group1;
	        case 2:
	            return Group2;
	        case 3:
	            return Group3;
	    }
	    return Group4; // Default case, return Group4
	}

	// Function to get the group ID for a node based on node ID
	public int getOtherNode_Groupid(int node_id) {    
	    // Check if the node ID matches any group and return the corresponding group ID
	    for(int i = 0; i < Group0.length; i++) {
	        if(node_id == Group0[i]) {
	            return 0;
	        }
	    }
	    for(int i = 0; i < Group1.length; i++) {
	        if(node_id == Group1[i]) {
	            return 1;
	        }
	    }
	    for(int i = 0; i < Group2.length; i++) {
	        if(node_id == Group2[i]) {
	            return 2;
	        }
	    }
	    for(int i = 0; i < Group3.length; i++) {
	        if(node_id == Group3[i]) {
	            return 3;
	        }
	    }
	    return 4; // Return 4 if no group matches
	}

    
    private void initratecal() throws IOException{
		
    	//SigCom
	    String filePath = "C:\\Users\\Masroor\\Documents\\My_DTNs Routing _Code\\GbR2020\\"
	    		+ "GroupBasedRouting2020\\reports\\GBR2020\\Sigcomm9One\\Mean_ICT\\"
	    		+ "EpidemicRouter-987529-TTL-10-buff-10M-seed-1_GBR_MeanICT.txt"; 	
	    
		BufferedReader reader2 = new BufferedReader(new FileReader(filePath));
		String line;
		while((line = reader2.readLine()) != null){
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
	 reader2.close();				
	}
    
    private void initGrpratecal() throws IOException{
		 //SigCom
	    String filePath = "C:\\Users\\Masroor\\Documents\\My_DTNs Routing _Code\\GbR2020\\"
	    		+ "GroupBasedRouting2020\\reports\\GBR2020\\Sigcomm9One\\Mean_ICT\\"
	    		+ "Group_rate\\EpidemicRouter-987529-TTL-10-buff-10M-seed-1_GBR_MeanICT.txt"; 
	    
		BufferedReader reader2 = new BufferedReader(new FileReader(filePath));
		String line;
		while((line = reader2.readLine()) != null){
		  String[] parts = line.split(",", 3);
		  if(parts.length >= 3) {
			ArrayList<String> li = null ;
			String key1 = parts[0];
			int i = Integer.parseInt(key1);
			String key2 = parts[1];
			int j = Integer.parseInt(key2);
			String value = parts[2];
		    if(Groupr.containsKey(i)) {
			 li = Groupr.get(i).get(j);
			}if(li == null) {
			 li = new ArrayList<String>();
			 li.add(value);
			 Map<Integer, ArrayList<String>> innerkey  = Groupr.get(i);
			 if(innerkey == null) {
				 Groupr.put(i, innerkey = new HashMap<>());
			 }
			  innerkey.put(j, li);
			} else {
			  li.add(value);
			}
	     }
	   }
	 reader2.close();				
	}
    
	@Override
	public GBRouter replicate() {
		return new GBRouter(this);
	}

}

