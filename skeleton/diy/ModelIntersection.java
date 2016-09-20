package traffic.diy;

import javax.swing.JOptionPane;

import traffic.core.Intersection;

/**
 * "Manually" create an intersection by assembling the various elements. Use the
 * classes provided in the traffic packages to construct an intersection which
 * can be displayed in the monitor.
 *
 */
public class ModelIntersection {

	/**
	 * A demo intersection made fusing the packages provided.  It has 
	 * one or more pre-timed phase plans.
	 * @return the intersection I made.
	 */
	public static Intersection preTimedIntersection() {
		JOptionPane.showMessageDialog(null, "You haven't implemented this method yet.  Returning null.", "To Do",
				JOptionPane.INFORMATION_MESSAGE);
		return null;
	}
	
	/**
	 * A demo intersection made fusing the packages provided.  It has 
	 * one or more fully-actuated phase plans.
	 * @return the intersection I made.
	 */
	public static Intersection fullyActivatedIntersection() {
		JOptionPane.showMessageDialog(null, "You haven't implemented this method yet.  Returning null.", "To Do",
				JOptionPane.INFORMATION_MESSAGE);
		return null;
	}
}
