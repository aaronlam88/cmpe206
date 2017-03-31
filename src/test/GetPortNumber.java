package test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class GetPortNumber {

	public static void main(String[] args) throws UnknownHostException, IOException {

		Socket socket = new Socket("localhost", 9001);

		InetAddress addr = socket.getInetAddress();
		int port = socket.getPort();

		System.out.println(addr);
		System.out.println(port);

		socket.close();

	}

}
