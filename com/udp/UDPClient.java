package com.udp;

import java.io.*;
import java.net.*;

public class UDPClient {
    private static final int PORT = 5000;
    private DatagramSocket dataSocket;
    private DatagramPacket dataPacket;
    private byte sendDataByte[];
    private String sendStr;

    public UDPClient() {
        Init();
    }

    public void Init() {
        try {
            // 指定端口号，避免与其他应用程序发生冲突
            dataSocket //= new DatagramSocket(PORT+1);
            	= new DatagramSocket();
            sendStr = "UDP方式发送数据";
            sendDataByte = sendStr.getBytes();
            dataPacket = new DatagramPacket(sendDataByte, sendDataByte.length,
                    InetAddress.getByName("localhost"), PORT);
            dataSocket.send(dataPacket);
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public static void main(String args[]) {
        new UDPClient();
    }
}