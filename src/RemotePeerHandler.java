import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class RemotePeerHandler implements Runnable, MessageConstants 
{
	private Socket peerSocket = null;
	private InputStream in;
	private OutputStream out;
	private int connType;
	
	private HandshakeMessage handshakeMessage;
	
	String ownPeerId, remotePeerId;
	
	final int ACTIVECONN = 1;
	final int PASSIVECONN = 0;

	public void openClose(InputStream i, Socket socket)
	{
		try {
			i.close();
			i = socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public RemotePeerHandler(Socket peerSocket, int connType, String ownPeerID) {
		
		this.peerSocket = peerSocket;
		this.connType = connType;
		this.ownPeerId = ownPeerID;
		try 
		{
			in = peerSocket.getInputStream();
			out = peerSocket.getOutputStream();
		} 
		catch (Exception ex) 
		{
			peerProcess.showLog(this.ownPeerId + " Error : " + ex.getMessage());
		}
	}
	
	public RemotePeerHandler(String add, int port, int connType, String ownPeerID) 
	{	
		try 
		{
			this.connType = connType;
			this.ownPeerId = ownPeerID;
			//peerProcess.showLog(ownPeerId + " Receiving Port = " + port + " Address = "+ add);
			this.peerSocket = new Socket(add, port);			
		} 
		catch (UnknownHostException e) 
		{
			peerProcess.showLog(ownPeerID + " RemotePeerHandler : " + e.getMessage());
		} 
		catch (IOException e) 
		{
			peerProcess.showLog(ownPeerID + " RemotePeerHandler : " + e.getMessage());
		}
		this.connType = connType;
		
		try 
		{
			in = peerSocket.getInputStream();
			out = peerSocket.getOutputStream();
		} 
		catch (Exception ex) 
		{
			peerProcess.showLog(ownPeerID + " RemotePeerHandler : " + ex.getMessage());
		}
	}
	
	public boolean SendHandshake() 
	{
		try 
		{
			out.write(HandshakeMessage.encodeMessage(new HandshakeMessage(MessageConstants.HANDSHAKE_HEADER, this.ownPeerId)));
		} 
		catch (IOException e) 
		{
			peerProcess.showLog(this.ownPeerId + " SendHandshake : " + e.getMessage());
			return false;
		}
		return true;
	}

	public boolean ReceiveHandshake() 
	{
		byte[] receivedHandshakeByte = new byte[32];
		try 
		{
			in.read(receivedHandshakeByte);
			handshakeMessage = HandshakeMessage.decodeMessage(receivedHandshakeByte);
			remotePeerId = handshakeMessage.getPeerIDString();
			
			//populate peerID to socket mapping
			peerProcess.peerIDToSocketMap.put(remotePeerId, this.peerSocket);
		} 
		catch (IOException e) 
		{
			peerProcess.showLog(this.ownPeerId + " ReceiveHandshake : " + e.getMessage());
			return false;
		}
		return true;
	}		

	public boolean SendRequest(int index)
	{
		try 
		{
			out.write(DataMessage.encodeMessage(new DataMessage( DATA_MSG_REQUEST, ConversionUtil.intToByteArray(index))));
		} 
		catch (IOException e) 
		{
			peerProcess.showLog(this.ownPeerId + " SendRequest : " + e.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean SendInterested()
	{
		try 
		{
			out.write(DataMessage.encodeMessage(new DataMessage(DATA_MSG_INTERESTED)));
		} 
		catch (IOException e) 
		{
			peerProcess.showLog(this.ownPeerId + " SendInterested : " + e.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean SendNotInterested()
	{
		try 
		{
			out.write(DataMessage.encodeMessage(new DataMessage( DATA_MSG_NOTINTERESTED)));
		} 
		catch (IOException e) 
		{
			peerProcess.showLog(this.ownPeerId + " SendNotInterested : " + e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public boolean ReceiveUnchoke()
	{
		byte [] receiveUnchokeByte = null;
		
		try 
		{
			in.read(receiveUnchokeByte);
		} 
		catch (IOException e) 
		{
			peerProcess.showLog(this.ownPeerId + " ReceiveUnchoke : " + e.getMessage());
			return false;
		}
				
		DataMessage m = DataMessage.decodeMessage(receiveUnchokeByte);
		if(m.getMessageTypeString().equals(DATA_MSG_UNCHOKE))
		{
			peerProcess.showLog(ownPeerId + "is unchoked by " + remotePeerId);
			return true;
		}
		else 
			return false;
	}
	
	public boolean ReceiveChoke()
	{
		byte [] receiveChokeByte = null;
	
		// Check whether the in stream has data to be read or not.
		try 
		{
			if(in.available() == 0) return false;
		} 
		catch (IOException e) 
		{
			peerProcess.showLog(this.ownPeerId + " ReceiveChoke : " + e.getMessage());
			return false;
		}
		
		try 
		{
			in.read(receiveChokeByte);
		} 
		catch (IOException e) 
		{
			peerProcess.showLog(this.ownPeerId + " ReceiveChoke : " + e.getMessage());
			return false;
		}
		
		DataMessage m = DataMessage.decodeMessage(receiveChokeByte);
		if(m.getMessageTypeString().equals(DATA_MSG_CHOKE))
		{
			// LOG 6:
			peerProcess.showLog(ownPeerId + " is CHOKED by " + remotePeerId);
			return true;
		}
		else 
			return false;
	}
	
	public boolean receivePeice()
	{
		byte [] receivePeice = null;
		
		try 
		{
			in.read(receivePeice);
		} 
		catch (IOException e) 
		{
			peerProcess.showLog(this.ownPeerId + " receivePeice : " + e.getMessage());
			return false;
		}
				
		DataMessage m = DataMessage.decodeMessage(receivePeice);
		if(m.getMessageTypeString().equals(DATA_MSG_UNCHOKE))
		{	
			// LOG 5:
			peerProcess.showLog(ownPeerId + " is UNCHOKED by " + remotePeerId);
			return true;
		}
		else 
			return false;

	}
	
	public void run() 
	{	
		//TODO:
		//1. receive byte from remotePeer socket (how many bytes??)
		//2. decode the byte to DataMessage
		//3. create DataMessageWrapper object
		//4. put the object in the message queue
		byte []handshakeBuff = new byte[32];
		byte []dataBuffWithoutPayload = new byte[DATA_MSG_LEN + DATA_MSG_TYPE];
		byte[] msgLength;
		byte[] msgType;
		DataMessageWrapper dataMsgWrapper = new DataMessageWrapper();

		try
		{
			if(this.connType == ACTIVECONN)
			{
				//peerProcess.showLog(ownPeerId + " Active connection..");
				if(!SendHandshake())
				{
					peerProcess.showLog(ownPeerId + " HANDSHAKE sending failed.");
					System.exit(0);
				}
				else
				{
					peerProcess.showLog(ownPeerId + " HANDSHAKE has been sent...");
				}
				while(true)
				{
					in.read(handshakeBuff);
					handshakeMessage = HandshakeMessage.decodeMessage(handshakeBuff);
					if(handshakeMessage.getHeaderString().equals(MessageConstants.HANDSHAKE_HEADER))
					{
						
						remotePeerId = handshakeMessage.getPeerIDString();
						
						peerProcess.showLog(ownPeerId + " makes a connection to Peer " + remotePeerId);
						
						peerProcess.showLog(ownPeerId + " Received a HANDSHAKE message from Peer " + remotePeerId);
						
						//populate peerID to socket mapping
						peerProcess.peerIDToSocketMap.put(remotePeerId, this.peerSocket);
						break;
					}
					else
					{
						continue;
					}		
				}
				
				// Sending BitField...
				DataMessage d = new DataMessage(DATA_MSG_BITFIELD, peerProcess.ownBitField.encode());
				byte  []b = DataMessage.encodeMessage(d);  
				out.write(b);
				
				peerProcess.remotePeerInfoHash.get(remotePeerId).state = 8;
				
				//peerProcess.showLog(ownPeerId + " Bitfield has been sent (ACTIVE Conn)");
			}
			//Passive connection
			else
			{
				//peerProcess.showLog(ownPeerId + " Passive connection");
				//System.out.println("Waiting for handshake message..");
				while(true)
				{
					in.read(handshakeBuff);
					handshakeMessage = HandshakeMessage.decodeMessage(handshakeBuff);
					if(handshakeMessage.getHeaderString().equals(MessageConstants.HANDSHAKE_HEADER))
					{
						remotePeerId = handshakeMessage.getPeerIDString();
						
						peerProcess.showLog(ownPeerId + " makes a connection to Peer " + remotePeerId);
						peerProcess.showLog(ownPeerId + " Received a HANDSHAKE message from Peer " + remotePeerId);
						
						//populate peerID to socket mapping
						peerProcess.peerIDToSocketMap.put(remotePeerId, this.peerSocket);
						break;
					}
					else
					{
						continue;
					}		
				}
				if(!SendHandshake())
				{
					peerProcess.showLog(ownPeerId + " HANDSHAKE message sending failed.");
					System.exit(0);
				}
				else
				{
					peerProcess.showLog(ownPeerId + " HANDSHAKE message has been sent successfully.");
				}
				
				peerProcess.remotePeerInfoHash.get(remotePeerId).state = 2;
			}
			// receive data messages continuously 
			while(true)
			{
				
				//peerProcess.showLog(ownPeerId + " Waiting for data messages..");
				int headerBytes = in.read(dataBuffWithoutPayload);
				
				if(headerBytes == -1)
					break;

				msgLength = new byte[DATA_MSG_LEN];
				msgType = new byte[DATA_MSG_TYPE];
				
				System.arraycopy(dataBuffWithoutPayload, 0, msgLength, 0, DATA_MSG_LEN);
				System.arraycopy(dataBuffWithoutPayload, DATA_MSG_LEN, msgType, 0, DATA_MSG_TYPE);
				
				DataMessage dataMessage = new DataMessage();
				dataMessage.setMessageLength(msgLength);
				dataMessage.setMessageType(msgType);
				

				if(dataMessage.getMessageTypeString().equals(MessageConstants.DATA_MSG_CHOKE)
						||dataMessage.getMessageTypeString().equals(MessageConstants.DATA_MSG_UNCHOKE)
						||dataMessage.getMessageTypeString().equals(MessageConstants.DATA_MSG_INTERESTED)
						||dataMessage.getMessageTypeString().equals(MessageConstants.DATA_MSG_NOTINTERESTED))
				{
					dataMsgWrapper.dataMsg = dataMessage;
					dataMsgWrapper.fromPeerID = this.remotePeerId;
					peerProcess.addToMsgQueue(dataMsgWrapper);
					//peerProcess.showLog("RemotePeerHandlder: elements in the queue= " + peerProcess.messageQ.size() + " head msgType: "+ peerProcess.messageQ.peek().getDataMsg().getMessageTypeString());
					//peerProcess.showLog(ownPeerId + " RemotePeerHandlder: Received Message with NO payload, length = " + headerBytes + " from " + remotePeerId);
					//peerProcess.showLog(ownPeerId + " RemotePeerHandlder: Message type: " + dataMessage.getMessageTypeString());
				}
				else 
				{
					//System.out.println("Pay load length = " + (dataMessage.getMessageLengthInt()-1));
					int bytesAlreadyRead = 0;
					int bytesRead;
					byte []dataBuffPayload = new byte[dataMessage.getMessageLengthInt()-1];
					while(bytesAlreadyRead < dataMessage.getMessageLengthInt()-1)
					{
						bytesRead = in.read(dataBuffPayload, bytesAlreadyRead, dataMessage.getMessageLengthInt()-1-bytesAlreadyRead);
						if(bytesRead == -1)
							return;
						bytesAlreadyRead += bytesRead;
					}
					
					byte []dataBuffWithPayload = new byte [dataMessage.getMessageLengthInt()+DATA_MSG_LEN];
					System.arraycopy(dataBuffWithoutPayload, 0, dataBuffWithPayload, 0, DATA_MSG_LEN + DATA_MSG_TYPE);
					System.arraycopy(dataBuffPayload, 0, dataBuffWithPayload, DATA_MSG_LEN + DATA_MSG_TYPE, dataBuffPayload.length);
					
					DataMessage dataMsgWithPayload = DataMessage.decodeMessage(dataBuffWithPayload);
					dataMsgWrapper.dataMsg = dataMsgWithPayload;
					dataMsgWrapper.fromPeerID = remotePeerId;
					//peerProcess.showLog(ownPeerId + " RemotePeerHandlder: Received Message WITH payload, length = " + bytesAlreadyRead + " from " + remotePeerId);
					//peerProcess.showLog(ownPeerId + " RemotePeerHandlder: Message type: " + dataMsgWithPayload.getMessageTypeString());
					peerProcess.addToMsgQueue(dataMsgWrapper);
					//peerProcess.showLog("RemotePeerHandlder: elements in the queue= " + peerProcess.messageQ.size() + " head msgType: "+ peerProcess.messageQ.peek().getDataMsg().getMessageTypeString());
					dataBuffPayload = null;
					dataBuffWithPayload = null;
					bytesAlreadyRead = 0;
					bytesRead = 0;
				}
			}
		}
		catch(IOException e)
		{
			peerProcess.showLog(ownPeerId + " run exception: " + e);
		}	
		
	}
	
	public void releaseSocket() {
		try {
			if (this.connType == PASSIVECONN && this.peerSocket != null) {
				this.peerSocket.close();
			}
			if (in != null) {
				in.close();
			}
			if (out != null)
				out.close();
		} catch (IOException e) {
			peerProcess.showLog(ownPeerId + " Release socket IO exception: " + e);
		}
	}
}