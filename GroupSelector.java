import java.util.*;

public class GroupSelector {

   /**
    * This class is responsible for displaying the final groups formed after filtering out overlapping elements.
    * It processes the input groups, ensures no element is duplicated across groups, and outputs the modified groups.
    
    * The final groups are displayed only if the group contains at least K non-overlapping elements. 
    * @param Group An array of ArrayLists where each ArrayList represents a group containing integer elements.
   */

    public void MakeFinalGroup(ArrayList<Integer>[] Group) {

        // Set to track selected elements across all groups
        Set<Integer> selectedElements = new HashSet<>();

        // Iterate through each group and remove overlapping elements
        for (int i = 0; i < Group.length; i++) {
            if (Group[i] != null) {
                // Create a new list to hold non-overlapping elements
                ArrayList<Integer> nonOverlapping = new ArrayList<>();

                for (int element : Group[i]) {
                    // Add elements that are not already selected
                    if (!selectedElements.contains(element)) {
                        nonOverlapping.add(element);
                        selectedElements.add(element);
                    }
                }

                // Replace the original group with the non-overlapping elements
                Group[i] = nonOverlapping;
            }
        }

        // Remove empty groups to ensure proper display
        for (int i = 0; i < Group.length; i++) {
            if (Group[i] != null && Group[i].size() < 3) {
                Group[i] = null; // Mark group as null if it has less than 3 elements
            }
        }
    }


}
