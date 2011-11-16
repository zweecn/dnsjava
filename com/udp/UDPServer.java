package com.udp;

import java.net.*;
import java.io.*;

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
            	System.out.println("In while...");
                dataSocket.receive(dataPacket);
                i = dataPacket.getLength();
                // 接收数据
                if (i > 0) {
                    // 指定接收到数据的长度,可使接收数据正常显示,开始时很容易忽略这一点
                    receiveStr = new String(receiveByte, 0, dataPacket.getLength());
                    System.out.println(receiveStr);
                    out=new FileOutputStream("E:/query.log"); 
                    out.write(receiveByte, 0, dataPacket.getLength());
                    out.close();
                    //System.out.println(dataPacket.getLength());
                    for (int j=0; j<dataPacket.getLength(); j++) {
                    	System.out.printf("%02x", receiveByte[j]);
                    	System.out.print(" ");
                    	if (j==15) {
                    		System.out.println();
                    	}
                    }
                    System.out.println();
                    //i = 0;// 循环接收
                    
                    
                    Record [] records = new Lookup("www.google.cn", Type.A).run();
                    UDPRes udpResponse = new UDPRes();
                    byte[] queryBytes = new byte[dataPacket.getLength()];
                    for (int j = 0; j < queryBytes.length; j++) {
						queryBytes[j] = receiveByte[j];
					}
                    byte[] res = udpResponse.getResData(queryBytes, records);
                    response(res);
                    
                    out = new FileOutputStream("E:/res.log");
                    out.write(res);
                    out.close();
                    
                    System.out.println("\nThe response is:");
                    for (int k=0; k<res.length; k++) {
                    	if (k%16 == 0) {
							System.out.println();
						}
                    	System.out.printf("%02x ", res[k]);

					}
                    
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void response(byte[] res) throws IOException {
        System.out.println("客户端地址 : " + dataPacket.getAddress().getHostAddress()
                + ",端口：" + dataPacket.getPort());
        DatagramPacket dp = new DatagramPacket(res, res.length, dataPacket
                .getAddress(), dataPacket.getPort());
        //dp.setData(res);
        dataSocket.send(dp);
    }

    public static void main(String args[]) {
        new UDPServer();
    }
}