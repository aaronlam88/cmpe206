package test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GetPrivateIP {

	public static String getPrivateIP() {
		String ip = null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ip;
	}

	public static void main(String[] args) throws Exception {
		String ip = getPrivateIP();
		System.out.println(ip);
	}
}
