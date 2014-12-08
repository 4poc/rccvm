package cc.apoc.rccvm.qemu;

import java.io.*;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorClient {
	private static final Logger logger = LoggerFactory.getLogger("rccvm.qemu");
    
    private int port;
    private Socket socket;
    private BufferedReader inputStream;
    private BufferedWriter outputStream;
    
    public MonitorClient(int port) {
        this.port = port;
    }
    
    public boolean connect() {
        try {
            if (port <= 0) {
                return false;
            }
            socket = new Socket("127.0.0.1", port);
            outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            handshake();
            return true;
        }
        catch (java.net.ConnectException e) {
            return false;
        }
        catch (IOException e) {
            return false;
        }
    }

    private void handshake() throws IOException {
        readLine(); // ignore the welcome banner
    }

    
    public boolean isConnected() {
        return socket.isConnected();
    }
    
    
    private String execute(String command) {
        return execute(command, false);
    }
    
    private String execute(String command, boolean noResponse) {
        String response = "";
        try {
            writeLine(command + "\n");
            readLine(); // the monitor echos the command

            String buffer = "";
            String line = "";
            do {
                buffer += line;
                line = readLine();
                // break when prompt read:
                if (line.contains("(qemu)")) {
                    if (!buffer.equals("") || noResponse) break;
                    else line = "";
                }
            }
            while (line != null);

            response = buffer;
        }
        catch (IOException e) {
        	e.printStackTrace();
        	logger.info("IOException in execute for " + command);
        }
        return response;
    }

    private void writeLine(String line) throws IOException {
    	logger.debug(String.format("send to monitor port %d: %s", port, line));
        outputStream.write(line + "\r\n");
        outputStream.flush();
    }
    
    private String readLine() throws IOException {
        return inputStream.readLine();
    }

    public void close() {
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
            }
            catch (IOException e) {
            	logger.info("IOException: " + e.toString());
            }
        }
    }
    
	public String status() {
	    return execute("info status");
	}
	
    public void sendSaveVM(String tag) {
        execute("savevm " + tag, true);
    }
	
	public void sendLoadVM(String tag) {
	    execute("loadvm " + tag, true);
	}
	
	public void sendPowerdown() {
		execute("system_powerdown", true);
	}
	
	public void sendReset() {
		execute("system_reset", true);
	}
	
	public void sendStop() {
		execute("stop", true);
	}
	
	public void sendCont() {
		execute("cont", true);
	}
}

