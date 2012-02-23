package com.test;

//import com.dns.DNSServer;
import com.dns.DNSThreadServer;

public class TestMain {

	public static void main(String args[]) {
		//DNSServer server = new DNSServer();
		DNSThreadServer server = new DNSThreadServer();
		server.runServer();
	}
}
