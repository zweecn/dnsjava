package com.dns;

import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;

public class DNSThreadServer{
	private static final int PORT = 53;
	//private static final String logFileName = "output/dns.log";
	private static final String ipFileName = "config/blockip.ini";
	private Map<InetAddress, InetAddress> ipmap;

	private DatagramSocket dataSocket;
	private DatagramPacket dataPacket;
	private byte[] receiveByte;

	public DNSThreadServer() {
		
	}
	
	public void runServer() {
		init();
	}

	private void init() {
	
		ipmap = new HashMap<InetAddress, InetAddress>();
		readBlockIp();
		ExecutorService pool = Executors.newSingleThreadExecutor();
		try {
			dataSocket = new DatagramSocket(PORT);
			receiveByte = new byte[1024];
			dataPacket = new DatagramPacket(receiveByte, receiveByte.length);
			pool.execute(new DNSThread(dataPacket, dataSocket, ipmap));
			pool.shutdown();
		} catch (SocketException e) {
			e.printStackTrace();
		}

	}
	
	private void readBlockIp() {
		ipmap = new HashMap<InetAddress, InetAddress>();
		try {
			FileReader fileReader = new FileReader(ipFileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {

				if (line.isEmpty() || line.trim().charAt(0)=='#') {
					continue;
				}
				String[] ips = line.trim().split(" ");
				if (ips == null || ips.length < 1) {
					continue;
				} else if (ips.length >= 2) {
					ipmap.put(InetAddress.getByName(ips[0]), InetAddress.getByName(ips[1]));
				} else if (ips.length == 1) {
					ipmap.put(InetAddress.getByName(ips[0]), InetAddress.getByName("127.0.0.1"));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}