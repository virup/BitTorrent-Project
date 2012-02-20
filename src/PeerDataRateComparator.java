import java.util.Comparator;

public class PeerDataRateComparator implements Comparator<RemotePeerInfo> {

	private boolean ascending;

	/**
	 * Default Constructor 
	 */
	public PeerDataRateComparator() {
		this.ascending = true;
	}

	/**
	 * Parameterized Constructor 
	 */
	public PeerDataRateComparator(boolean ascending) {
		this.ascending = ascending;
	}

	public int compare(RemotePeerInfo rm1, RemotePeerInfo rm2) {
		if (rm1 == null && rm2 == null)
			return 0;

		if (rm1 == null)
			return 1;

		if (rm2 == null)
			return -1;

		// Compare objects
		if (rm1 instanceof Comparable) {
			if (ascending) {
				return rm1.compareTo(rm2);
			} else {
				return rm2.compareTo(rm1);
			}
		} 
		else {
			if (ascending) {
				return rm1.toString().compareTo(rm2.toString());
			} else {
				return rm2.toString().compareTo(rm1.toString());
			}
		}
	}

}
