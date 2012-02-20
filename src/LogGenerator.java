import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * @author shantonu
 *
 */
public class LogGenerator {
	static FileOutputStream fos;
	static OutputStreamWriter os;

	public static void start(String f) throws IOException {
		fos = new FileOutputStream(f);
		os = new OutputStreamWriter(fos, "UTF-8");

	}

	// Restores the original settings.
	public static void stop() {
		try {
			os.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeLog(String str) {
		try {
			os.write(str + '\n');
			//System.out.println(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
