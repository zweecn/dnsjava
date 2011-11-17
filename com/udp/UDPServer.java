package com.udp;

import java.net.*;
import java.util.Arrays;
import java.io.*;

import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import com.dns.UDPRes;

public class UDPServer {
	private static final int PORT = 53;
	private DatagramSocket dataSocket;
	private DatagramPacket dataPacket;
	private byte receiveByte[];
	private String receiveStr;

	public UDPServer() {
		Init();
	}

	public void Init() {
		try {
			dataSocket = new DatagramSocket(PORT);
			receiveByte = new byte[1024];
			dataPacket = new DatagramPacket(receiveByte, receiveByte.length);
			receiveStr = "";
			int i = 0;
			FileOutputStream out;
			while (i == 0)// 无数据，则循环
			{
				try {
					System.out.println("In while...");
					dataSocket.receive(dataPacket);
					i = dataPacket.getLength();
					// 接收数据
					if (i > 0) {
						// 指定接收到数据的长度,可使接收数据正常显示,开始时很容易忽略这一点
						receiveStr = new String(receiveByte, 0, dataPacket.getLength());
						//System.out.println(receiveStr);
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

						out=new FileOutputStream("E:/query.log"); 
						out.write(receiveByte, 0, dataPacket.getLength());
						out.close();

						i = 0;// 循环接收

						System.out.println("query:" + queryBuffer);
						Record [] aRecords = new Lookup(queryBuffer.toString(), Type.A).run();
						if (aRecords == null) {
							System.out.println("There is not A record");
						} else {
							System.out.println("Found A record, size:" + aRecords.length);
							byte[] queryBytes = Arrays.copyOf(receiveByte, dataPacket.getLength());
							byte[] res = new UDPRes().getResData(queryBytes, aRecords);

							response(res);
							out = new FileOutputStream("E:/res.log");
							out.write(res);
							out.close();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
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

	public void response(byte[] res) throws IOException {
		System.out.println("Response to client: " + dataPacket.getAddress().getHostAddress()
				+ ", Port：" + dataPacket.getPort());
		DatagramPacket dp = new DatagramPacket(res, res.length, dataPacket
				.getAddress(), dataPacket.getPort());
		//dp.setData(res);
		dataSocket.send(dp);
	}

	public static void main(String args[]) {
		new UDPServer();
	}
}