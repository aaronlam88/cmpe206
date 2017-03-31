package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class MasterBot {
	public static void main(String[] args) {

		MasterBot master = new MasterBot();

		master.port = 65535;
		master.slaveList = new LinkedList<Slave>();

		master.slaveListFile = new File("slaveListFile.txt");

		try {
			master.slaveListFile.createNewFile();
			master.writer = new PrintWriter("the-file-name.txt", "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}

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
	private File slaveListFile;

	private PrintWriter writer;

	public void register(Slave slave) {
		synchronized (slaveList) {
			updateSlaveList();
			slaveList.add(slave);
			for (Slave slave1 : slaveList) {
				writer.println(slave1.toString());
			}
		}
	}

	public void updateSlaveList() {
		synchronized (slaveList) {
			for (Slave slave : slaveList) {
				try {
					slave.getSocket().getOutputStream().write(1);
				} catch (IOException e) {
					slaveList.remove(slave);
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

	public BufferedReader getBufferedReader() {
		return in;
	}

	public PrintWriter getPrintWriter() {
		return out;
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
