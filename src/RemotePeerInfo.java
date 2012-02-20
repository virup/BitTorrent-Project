import java.util.Date;



public class RemotePeerInfo implements Comparable<RemotePeerInfo>
{
	public String peerId;
	public String peerAddress;
	public String peerPort;
	public int isFirstPeer;
	public double dataRate = 0;
	public int isInterested = 1;
	public int isPreferredNeighbor = 0;
	public int isOptUnchokedNeighbor = 0;
	public int isChoked = 1;
	public BitField bitField;
	public int state = -1;
	public int peerIndex;
	public Date startTime;
	public Date finishTime;
	public int isCompleted = 0;
	public int isHandShaked = 0;
	
	public RemotePeerInfo(String pId, String pAddress, String pPort, int pIndex)
	{
		peerId = pId;
		peerAddress = pAddress;
		peerPort = pPort;
		bitField = new BitField();
		peerIndex = pIndex;
	}
	public RemotePeerInfo(String pId, String pAddress, String pPort, int pIsFirstPeer, int pIndex)
	{
		peerId = pId;
		peerAddress = pAddress;
		peerPort = pPort;
		isFirstPeer = pIsFirstPeer;
		bitField = new BitField();
		peerIndex = pIndex;
	}
	/**
	 * @return the peerId
	 */
	public String getPeerId() {
		return peerId;
	}
	/**
	 * @param peerId the peerId to set
	 */
	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}
	/**
	 * @return the peerAddress
	 */
	public String getPeerAddress() {
		return peerAddress;
	}
	/**
	 * @param peerAddress the peerAddress to set
	 */
	public void setPeerAddress(String peerAddress) {
		this.peerAddress = peerAddress;
	}
	/**
	 * @return the peerPort
	 */
	public String getPeerPort() {
		return peerPort;
	}
	/**
	 * @param peerPort the peerPort to set
	 */
	public void setPeerPort(String peerPort) {
		this.peerPort = peerPort;
	}
	/**
	 * @return the isFirstPeer
	 */
	public int getIsFirstPeer() {
		return isFirstPeer;
	}
	/**
	 * @param isFirstPeer the isFirstPeer to set
	 */
	public void setIsFirstPeer(int isFirstPeer) {
		this.isFirstPeer = isFirstPeer;
	}
	public int compareTo(RemotePeerInfo o1) {
		
		if (this.dataRate > o1.dataRate) 
			return 1;
		else if (this.dataRate == o1.dataRate) 
			return 0;
		else 
			return -1;
	}

}
