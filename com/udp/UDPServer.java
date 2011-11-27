package com.udp;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.*;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.dns.UDPRes;

public class UDPServer {
	private static final int PORT = 53;
	private static final String logFileName = "output/dns.log";
	private static final String lastQueryFileName = "output/lastquery.log";
	private static final String lastResponseFileName = "output/lastrespond.log";
	private static final String ipFileName = "config/blockip.ini";
	private Map<InetAddress, InetAddress> ipmap;
	
	private FileOutputStream outlog;
//	private FileOutputStream outLastQuery;
//	private FileOutputStream outLastResponse;
	private SimpleDateFormat tempDate;
	private DatagramSocket dataSocket;
	private DatagramPacket dataPacket;
	private byte[] receiveByte;
	private byte[] queryBytes;

	public UDPServer() {
		Init();
	}

	public void Init() {
		tempDate = new SimpleDateFormat("yyyy-MM-dd" + " " + "hh:mm:ss"); 
		ipmap = new HashMap<InetAddress, InetAddress>();
		readBlockIp();
		try {
			outlog = new FileOutputStream(logFileName, true);
			dataSocket = new DatagramSocket(PORT);
			receiveByte = new byte[1024];
			dataPacket = new DatagramPacket(receiveByte, receiveByte.length);
			int i = 0;
			while (i == 0)// 无数据，则循环
			{
				try {
					System.out.println("Waiting for query...");
					dataSocket.receive(dataPacket);
					i = dataPacket.getLength();
					// 接收数据
					if (i > 0) {
						i = 0;// 循环接收
						StringBuffer queryBuffer = getQuery();
						int queryType = getQueryType();
						doDNS(queryBuffer, queryType);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println();
			}
			outlog.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printQuery() {
		for (int j=0; j<dataPacket.getLength(); j++) {
			System.out.printf("%02x", receiveByte[j]);
			System.out.print(" ");
			if (j==15) {
				System.out.println();
			}
		}
		System.out.println();
	}

	public void printResponse(byte[] res) {
		System.out.println("\nThe response is:");
		for (int k=0; k<res.length; k++) {
			if (k%16 == 0) {
				System.out.println();
			}
			System.out.printf("%02x ", res[k]);
		}
		System.out.println();
	}

	public StringBuffer getQuery() throws IOException {
		StringBuffer queryBuffer = new StringBuffer();
		int head = 0x0c;
		int len = receiveByte[head];// = receiveStr.charAt(head);
		int curr = head+1;
		while (len != 0 && curr < dataPacket.getLength()) {
			if (curr - head > len) {
				head = curr;
				//len = receiveStr.charAt(curr++);
				len = receiveByte[curr++];
				if (len != 0) {
					queryBuffer.append(".");
				}
			} else {
				//queryBuffer.append(receiveStr.charAt(curr++));
				queryBuffer.append((char)receiveByte[curr++]);
			}
		}
		if (queryBuffer.toString().trim().endsWith("localdomain")) {
			//System.out.println("In if , querybuffer:" + queryBuffer);
			int indexTemp = queryBuffer.lastIndexOf(".localdomain");
			queryBuffer = new StringBuffer(queryBuffer.substring(0, indexTemp));
			//System.out.println("In if , querybuffer after:" + queryBuffer);
		}
		
		return queryBuffer;
	}
	
	private int getQueryType() {
		queryBytes = Arrays.copyOf(receiveByte, dataPacket.getLength());
		if (queryBytes != null && queryBytes.length > 0) {
			return queryBytes[queryBytes.length - 3];
		} else {
			return 0;
		}
	}
	
	public void doDNS(StringBuffer queryBuffer, int queryType) {
		try {
			long start = System.currentTimeMillis();
			System.out.println(tempDate.format(new java.util.Date()) + " Query:"  
					+ queryBuffer + " Type: " + queryType);
			if (queryType == Type.A) {
				Record[] aRecords = new Lookup(queryBuffer.toString(), Type.A).run();
				if (aRecords == null) {
					System.out.println("A record was not found.");
					//outlog.write((datetimeQuery + " " + queryBuffer).getBytes());
				} else {
					//System.out.println("A record was found. size: " + aRecords.length);
					queryBytes = Arrays.copyOf(receiveByte, dataPacket.getLength());
					UDPRes udpRes = new UDPRes(queryBytes, aRecords, ipmap);
					byte[] res = udpRes.getARecordResponseData();
					response(res);
					long pause = System.currentTimeMillis();
					System.out.println(tempDate.format(new java.util.Date()) + " Response A record:" 
							+ udpRes.getIPList() + " Cost " + (pause - start) + " ms.");
					
//					outlog.write((datetimeQuery + " " + queryBuffer).getBytes());
//					writeARecordToLog(udpRes.getIPList(), datetimeRes);
					/*writeQueryToLog(queryBuffer.toString(), datetimeQuery);
					writeLastOp(lastQueryFileName, receiveByte, 0, dataPacket.getLength());
					writeLastOp(lastResponseFileName, res);
					writeResponseToLog(udpRes.getIPList(), datetimeRes);*/
				}
				
			} else if (queryType == Type.PTR) {
				if (queryBuffer.toString().startsWith("110.1.168.192")) {	
					//queryBytes = Arrays.copyOf(receiveByte, dataPacket.getLength());
					UDPRes udpRes = new UDPRes(queryBytes);
					byte[] res = udpRes.getDNSNameResponseData("Wayne.com");
					response(res);
					System.out.println(tempDate.format(new java.util.Date()) 
							+ " Response DNS Name: Wayne.com Cost " 
							+ (System.currentTimeMillis() - start) + " ms.");
				} else {
					Record[] ptrRecords = new Lookup(queryBuffer.toString(), Type.PTR).run();
					if (ptrRecords == null) {
						System.out.println("PTR Record not found.");
					} else {
						UDPRes udpRes = new UDPRes(queryBytes, ptrRecords, ipmap);
						String[] tmpIp = queryBuffer.toString().split("\\.");
						String hostip = tmpIp[3] + "." + tmpIp[2] + "." + tmpIp[1] + "." + tmpIp[0];
						//System.out.println("hostip:" + hostip);
						byte[] res = udpRes.getReverseDNSResponseData(hostip);
						response(res);
						System.out.println(tempDate.format(new java.util.Date()) 
								+ " Response PTR record:" + udpRes.getDomainList() + " Cost "
								+ (System.currentTimeMillis() - start) + " ms.");
					}
				}
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
	

//	private void writeLastOp(String fileName, byte[] buffer) {
//		FileOutputStream out;
//		try {
//			out = new FileOutputStream(fileName);
//			out.write(buffer);
//			out.close();
//		} catch (FileNotFoundException e) {		
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
//	private void writeLastOp(String fileName, byte[] buffer, int offset, int length) {
//		FileOutputStream out;
//		try {
//			out = new FileOutputStream(fileName);
//			out.write(buffer, offset, length);
//			out.close();
//		} catch (FileNotFoundException e) {		
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
//	private void writeQueryToLog(String query, String datetime) {
//		try {
//			outlog = new FileOutputStream(logFileName, true);
//			String queryLog = datetime + " Query:" + query + " Client:" 
//				+ dataPacket.getAddress().getHostAddress() + " Port:" + dataPacket.getPort() + "\n";
//			System.out.println(queryLog);
//			if (query != null && !query.isEmpty()) {
//				outlog.write(queryLog.getBytes());
//				
//			}
//			outlog.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private void writeARecordToLog(List<String> ipList, String datetime) {
//		String responseLog = datetime + " Response A record: ";
//		for (String ip : ipList) {
//			responseLog += " " + ip;
//		}
//		responseLog += "\n";
//		try {
//			outlog.write(responseLog.getBytes());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}