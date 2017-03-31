package test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GetHostName {

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		String hostname = "Unknown";

		try {
			InetAddress addr;
			addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
			System.out.println(addr);
		} catch (UnknownHostException ex) {
			System.out.println("Hostname can not be resolved");
		}
	}
}