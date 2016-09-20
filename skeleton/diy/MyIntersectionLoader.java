package traffic.diy;

import java.io.BufferedReader;

import javax.swing.JOptionPane;

import traffic.core.Intersection;

/**
 * Read an intersection description file and build an intersection from the data
 * it contains.
 *
 */
public class MyIntersectionLoader {
	/**
	 * Constructor for class.
	 * 
	 * @param br
	 *            where to read data from.
	 */
	public MyIntersectionLoader(BufferedReader br) {
		JOptionPane.showMessageDialog(null, "You haven't implemented this method yet.", "To Do",
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Build intersection from description in file. Read line at a time and
	 * process rather than parse using grammar.
	 * 
	 * @return the intersection or null if something went wrong
	 */
	public Intersection buildIntersection() {
		JOptionPane.showMessageDialog(null, "You haven't implemented this method yet.  Returning null.", "To Do",
				JOptionPane.INFORMATION_MESSAGE);
		return null;
	}
}
