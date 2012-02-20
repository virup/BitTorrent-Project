/**
 * 
 */

import java.io.UnsupportedEncodingException;

/**
 * @Description Entity class that deals with the attributes of Actual Message
 * 
 * @author Clint P. George
 * 
 */
public class DataMessage implements MessageConstants 
{
	// Attributes
	private byte[] len = null;
	private byte[] type = null;
	private byte[] payload = null;
	private String messageLength;
	private String messageType;
	private int dataLength = DATA_MSG_TYPE;

	public DataMessage()
	{
		
	}
	/**
	 * Class constructor
	 * 
	 * @param Type - Message Type
	 * @param Payload - Message content
	 */
	public DataMessage(String Type, byte[] Payload) 
	{

		try 
		{
			if (Payload == null) // Pay load is null
			{
				if (Type == DATA_MSG_CHOKE || Type == DATA_MSG_UNCHOKE
						|| Type == DATA_MSG_INTERESTED
						|| Type == DATA_MSG_NOTINTERESTED) 
				{
					this.setMessageLength(1);
					this.payload = null;
				} 
				else 
					throw new Exception("DataMessage:: Constructor - Pay load should not be null");

			} 
			else // Pay load has some value
			{
				this.setMessageLength(Payload.length + 1);
				if (this.len.length > DATA_MSG_LEN)
					throw new Exception("DataMessage:: Constructor - message length is too large.");
				
				this.setPayload(Payload);
			}

			this.setMessageType(Type);
			if (this.getMessageType().length > DATA_MSG_TYPE)
				throw new Exception("DataMessage:: Constructor - Type length is too large.");

		} catch (Exception e) {
			peerProcess.showLog(e.toString());
		}

	}

	/**
	 * Class constructor
	 * 
	 * @param Type
	 *            - Message Type
	 */
	public DataMessage(String Type) {

		try {

			if (Type == DATA_MSG_CHOKE || Type == DATA_MSG_UNCHOKE
					|| Type == DATA_MSG_INTERESTED
					|| Type == DATA_MSG_NOTINTERESTED) 
			{
				this.setMessageLength(1);
				this.setMessageType(Type);
				this.payload = null;
			} 
			else 
				throw new Exception("DataMessage:: Constructor - Wrong constructor selection.");


		} catch (Exception e) {
			peerProcess.showLog(e.toString());
		}

	}


	/**
	 * @param len
	 *            the length to set
	 */
	public void setMessageLength(byte[] len) {

		Integer l = ConversionUtil.byteArrayToInt(len);
		this.messageLength = l.toString();
		this.len = len;
		this.dataLength = l;  
	}

	/**
	 * @param messageLength the messageLength to set
	 */
	public void setMessageLength(int messageLength) {
		this.dataLength = messageLength;
		this.messageLength = ((Integer)messageLength).toString();
		this.len = ConversionUtil.intToByteArray(messageLength);
	}	
	
	/**
	 * @return the length
	 */
	public byte[] getMessageLength() {
		return len;
	}
	/**
	 * @return the messageLength
	 */
	public String getMessageLengthString() {
		return messageLength;
	}

	/**
	 * @return the messageLength
	 */
	public int getMessageLengthInt() {
		return this.dataLength; // Integer.parseInt(messageLength);
	}
	
	
	/**
	 * @param type
	 *            the type to set
	 */
	public void setMessageType(byte[] type) {
		try {
			this.messageType = new String(type, MSG_CHARSET_NAME); // ((Integer)ConversionUtil.byteArrayToInt(type)).toString(); //
			this.type = type;// this.messageType.getBytes();
		} catch (UnsupportedEncodingException e) {
			peerProcess.showLog(e.toString());
		}
	}
	/**
	 * @param messageType 
	 */
	public void setMessageType(String messageType) {
		try {
			this.messageType = messageType.trim();
			this.type = this.messageType.getBytes(MSG_CHARSET_NAME); // ConversionUtil.intToByteArray(Integer.parseInt(messageType)); // 
		} catch (UnsupportedEncodingException e) {
			peerProcess.showLog(e.toString());
		}
	}
	/**
	 * @return the type
	 */
	public byte[] getMessageType() {
		return type;
	}

	/**
	 * @param payload
	 *            the Pay load to set
	 */
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	/**
	 * @return the Pay load
	 */
	public byte[] getPayload() {
		return payload;
	}




	/**
	 * @return the messageType
	 */
	public String getMessageTypeString() {
		return messageType;
	}

	/**
	 * Overrides the toString method of the Object
	 */
	public String toString() {
		String str = null;
		try {
			str = "[DataMessage] : Message Length - "
					+ this.messageLength
					+ ", Message Type - "
					+ this.messageType
					+ ", Data - "
					+ (new String(this.payload, MSG_CHARSET_NAME)).toString()
							.trim();
		} catch (UnsupportedEncodingException e) {
			peerProcess.showLog(e.toString());
		}
		return str;
	}

	/**
	 * This function decodes the byte array and loads to the object DataMessage
	 * 
	 * @param PeerId - Peer ID of the receiving or sending messages
	 * @param Message - Message in byte array format
	 */
	public static DataMessage decodeMessage(byte[] Message) {

		// VAR initialization
		DataMessage msg = new DataMessage();
		byte[] msgLength = new byte[DATA_MSG_LEN];
		byte[] msgType = new byte[DATA_MSG_TYPE];
		byte[] payLoad = null;
		int len;

		try 
		{
			// Initial check
			if (Message == null)
				throw new Exception("Invalid data.");
			else if (Message.length < DATA_MSG_LEN + DATA_MSG_TYPE)
				throw new Exception("Byte array length is too small...");

			// 1. Decode the received message
			System.arraycopy(Message, 0, msgLength, 0, DATA_MSG_LEN);
			System.arraycopy(Message, DATA_MSG_LEN, msgType, 0, DATA_MSG_TYPE);

			msg.setMessageLength(msgLength);
			msg.setMessageType(msgType);
			
			len = ConversionUtil.byteArrayToInt(msgLength);
			
			if (len > 1) 
			{
				payLoad = new byte[len-1];
				System.arraycopy(Message, DATA_MSG_LEN + DATA_MSG_TYPE,	payLoad, 0, Message.length - DATA_MSG_LEN - DATA_MSG_TYPE);
				msg.setPayload(payLoad);
			}
			
			payLoad = null;
		} 
		catch (Exception e) 
		{
			peerProcess.showLog(e.toString());
			msg = null;
		}
		return msg;
	}

	/**
	 * This function encodes the object DataMessage to a byte array for
	 * transmission
	 * 
	 * @param m - DataMessage object to be converted into byte array
	 */

	public static byte[] encodeMessage(DataMessage msg) 
	{
		byte[] msgStream = null;
		int msgType;

		try 
		{
			// Encode message type, length, pay load field
			msgType = Integer.parseInt(msg.getMessageTypeString());

			// Encode message length field
			if (msg.getMessageLength() == null)
				throw new Exception("Invalid message length.");
			else if (msg.getMessageLength().length > DATA_MSG_LEN)
				throw new Exception("Invalid message length.");
			else if (msg.getMessageType() == null)
				throw new Exception("Invalid message type.");
			else if (msgType < 0 || msgType > 7)
				throw new Exception("Invalid message type.");

			if (msg.getPayload() != null) {
				msgStream = new byte[DATA_MSG_LEN + DATA_MSG_TYPE + msg.getPayload().length];

				System.arraycopy(msg.getMessageLength(), 0, msgStream, 0, msg.getMessageLength().length);
				System.arraycopy(msg.getMessageType(), 0, msgStream, DATA_MSG_LEN, DATA_MSG_TYPE);
				System.arraycopy(msg.getPayload(), 0, msgStream, DATA_MSG_LEN + DATA_MSG_TYPE, msg.getPayload().length);
			} else {
				msgStream = new byte[DATA_MSG_LEN + DATA_MSG_TYPE];

				System.arraycopy(msg.getMessageLength(), 0, msgStream, 0, msg.getMessageLength().length);
				System.arraycopy(msg.getMessageType(), 0, msgStream, DATA_MSG_LEN, DATA_MSG_TYPE);

			}

		} 
		catch (Exception e) 
		{
			peerProcess.showLog(e.toString());
			msgStream = null;
		}

		return msgStream;
	}

}
