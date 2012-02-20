/**
 * @author shantonu
 *
 */
public class ConversionUtil 
{
	public static byte[] intToByteArray(int value) 
	{
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) 
        {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }
	
	/**
     * Convert the byte array to an int.
     *
     * @param b The byte array
     * @return The integer
     */
    public static int byteArrayToInt(byte[] b) {
        return byteArrayToInt(b, 0);
    }

    /**
     * Convert the byte array to an int starting from the given offset.
     *
     * @param b The byte array
     * @param offset The array offset
     * @return The integer
     */
    public static int byteArrayToInt(byte[] b, int offset) 
    {
        int value = 0;
        for (int i = 0; i < 4; i++) 
        {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
    
    static String byteArrayToHexString(byte in[]) 
	 {
	    byte ch = 0x00;

	    int i = 0; 

	    if (in == null || in.length <= 0)
	        return null;
	    String hexDigits[] = {"0", "1", "2","3", "4", "5", "6", "7", "8","9", "A", "B", "C", "D", "E","F"};

	    StringBuffer out = new StringBuffer(in.length * 2);

	    for (i = 0; i < in.length; i++) 
	    {
	        ch = (byte) (in[i] & 0xF0); 
	        ch = (byte) (ch >>> 4);
	        // shift the bits down

	        ch = (byte) (ch & 0x0F);    
	        // must do this is high order bit is on!

	        out.append(hexDigits[ (int) ch]);

	        ch = (byte) (in[i] & 0x0F); 

	        out.append(hexDigits[ (int) ch]); 

	    }

	    String rslt = new String(out);

	    return rslt;
	}
}
