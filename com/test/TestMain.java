package com.test;

import com.dns.DNSServer;

public class TestMain {

	public static void main(String args[]) {
		DNSServer server = new DNSServer();
		server.runServer();
	}
}
