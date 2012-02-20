
public class DataMessageWrapper
{
	DataMessage dataMsg;
	String fromPeerID;
	
	public DataMessageWrapper() 
	{
		dataMsg = new DataMessage();
		fromPeerID = null;
	}
	/**
	 * @return the dataMsg
	 */
	public DataMessage getDataMsg() {
		return dataMsg;
	}
	/**
	 * @param dataMsg the dataMsg to set
	 */
	public void setDataMsg(DataMessage dataMsg) {
		this.dataMsg = dataMsg;
	}
	/**
	 * @return the fromPeerID
	 */
	public String getFromPeerID() {
		return fromPeerID;
	}
	/**
	 * @param fromPeerID the fromPeerID to set
	 */
	public void setFromPeerID(String fromPeerID) {
		this.fromPeerID = fromPeerID;
	}
	
	
}
