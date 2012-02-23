package com.dns;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class DNSThread implements Runnable{
	private DatagramPacket dataPacket;
	private DatagramSocket dataSocket;
	private byte[] queryBytes;
	private Map<InetAddress, InetAddress> ipmap;
	
	public DNSThread(DatagramPacket dataPacket, DatagramSocket dataSocket, Map<InetAddress, InetAddress> ipmap) {
		this.dataPacket = dataPacket;
		this.dataSocket = dataSocket;
		this.ipmap = ipmap;
	}

	@Override
	public void run() {
		int i = 0;
		while (i == 0)// 无数据，则循环
		{
			System.out.println("\nWaiting for query...");
			try {
				dataSocket.receive(dataPacket);
				i = dataPacket.getLength();
				// 接收数据
				if (i > 0) {
					i = 0;// 循环接收
					StringBuffer queryBuffer = getQuery(dataPacket.getData());
					int queryType = getQueryType(dataPacket.getData());
					dnsCheck(queryBuffer, queryType);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private StringBuffer getQuery(byte[] receiveByte) {
		StringBuffer queryBuffer = new StringBuffer();
		int head = 0x0c;
		int len = receiveByte[head];
		int curr = head+1;
		while (len != 0 && curr < dataPacket.getLength()) {
			if (curr - head > len) {
				head = curr;
				len = receiveByte[curr++];
				if (len != 0) {
					queryBuffer.append(".");
				}
			} else {
				queryBuffer.append((char)receiveByte[curr++]);
			}
		}
		if (queryBuffer.toString().trim().endsWith("localdomain")) {
			int indexTemp = queryBuffer.lastIndexOf(".localdomain");
			queryBuffer = new StringBuffer(queryBuffer.substring(0, indexTemp));
		}

		return queryBuffer;
	}

	private int getQueryType(byte[] receiveByte) {
		queryBytes = Arrays.copyOf(receiveByte, dataPacket.getLength());
		if (queryBytes != null && queryBytes.length > 0) {
			return queryBytes[queryBytes.length - 3];
		} else {
			return 0;
		}
	}
	
	
	
	private void dnsCheck(StringBuffer queryBuffer, int queryType) {
		SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd" + " " + "hh:mm:ss"); 
		System.out.println(tempDate.format(new java.util.Date()) + " Query:"  
				+ queryBuffer + " Type: " + queryType);
		if (queryType == Type.A) {
			doARecord(queryBuffer.toString());
		} else if (queryType == Type.PTR) {
			doRTPRecord(queryBuffer.toString());
		}
	}
	
	private void doARecord(String query) {
		SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd" + " " + "hh:mm:ss"); 
		long start = System.currentTimeMillis();
		Record[] aRecords;
		try {
			aRecords = new Lookup(query, Type.A).run();

			if (aRecords == null) {
				System.out.println("A record was not found.");
			} else {
				DNSPacker udpRes = new DNSPacker(queryBytes, aRecords, ipmap);
				byte[] res = udpRes.getARecordResponseData();
				response(res);
				long pause = System.currentTimeMillis();
				System.out.println(tempDate.format(new java.util.Date()) + " Response A record:" 
						+ udpRes.getIPList() + " Cost " + (pause - start) + " ms.");

//				writeLastOp(lastQueryFileName, queryBytes);
//				writeLastOp(lastResponseFileName, res);
			}
		} catch (TextParseException e) {
			e.printStackTrace();
		}
	}
	
	private void response(byte[] res) {
		DatagramPacket dp = new DatagramPacket(res, res.length, 
				dataPacket.getAddress(), dataPacket.getPort());
		try {
			dataSocket.send(dp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void doRTPRecord(String query) {
		SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd" + " " + "hh:mm:ss"); 
		long start = System.currentTimeMillis();
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			String localIP = addr.getHostAddress().toString();
			String localHostName = addr.getHostName().toString();
			String[] ipTemp = localIP.split("\\.");
			String prefix = ipTemp[3] + "." + ipTemp[2] + "." + ipTemp[1] + "." + ipTemp[0];
			if (query.startsWith(prefix)) {
				DNSPacker udpRes = new DNSPacker(queryBytes);
				byte[] res = udpRes.getDNSServerNameData(localHostName);
				response(res);
				System.out.println(tempDate.format(new java.util.Date()) 
						+ " Response DNS Name: Wayne.com Cost " 
						+ (System.currentTimeMillis() - start) + " ms.");

//				writeLastOp(lastQueryFileName, queryBytes);
//				writeLastOp(lastResponseFileName, res);
			} else {
				Record[] ptrRecords = new Lookup(query, Type.PTR).run();
				if (ptrRecords == null) {
					System.out.println("PTR Record not found.");
				} else {
					DNSPacker udpRes = new DNSPacker(queryBytes, ptrRecords, ipmap);
					String[] tmpIp = query.split("\\.");
					String hostip = tmpIp[3] + "." + tmpIp[2] + "." + tmpIp[1] + "." + tmpIp[0];
					byte[] res = udpRes.getPTRRecordResponseData(hostip);
					response(res);
					System.out.println(tempDate.format(new java.util.Date()) 
							+ " Response PTR record:" + udpRes.getDomainList() + " Cost "
							+ (System.currentTimeMillis() - start) + " ms.");

//					writeLastOp(lastQueryFileName, queryBytes);
//					writeLastOp(lastResponseFileName, res);
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (TextParseException e) {
			e.printStackTrace();
		}
	}
}