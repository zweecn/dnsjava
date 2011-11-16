package com.tcp;

import java.io.*;
import java.net.*;

public class TCPEchoServer {  
    private static final int BUFSIZE=32;  
      
    public static void main(String [] args) throws IOException, InterruptedException{  
        if(args.length!=1){  
            throw new IllegalArgumentException("Parameter(s):<Port>");  
        }  
          
        int servPort=Integer.parseInt(args[0]);  
          
        //1.创建一个ServerSocket实例并制定本地端口。此套接字的功能是侦听该制定端口收到的连接。  
        //ServerSocket servSock=new ServerSocket(servPort);  
        System.out.println("Listening prot: 53");
        ServerSocket servSock=new ServerSocket(53);  
        
        int recvMsgSize;  
          
        byte [] receiveBuf=new byte[BUFSIZE];  
          
        //2.重复执行  
        while(true){  
            //a.调用ServerSocket的accept()方法以获取下一个客户端连接。  
            //基于新建立的客户端连接，创建一个Socket实例，并由accept()方法返回  
            System.out.println("In while..");
        	Socket clntSock=servSock.accept();
            SocketAddress clientAddress=clntSock.getRemoteSocketAddress();  
            System.out.println("Handling client at " + clientAddress);  
              
            //b,使用所返回的Socket实例的InputStream和OutputStream与客户端进行通信  
            InputStream in=clntSock.getInputStream();  
            OutputStream out=clntSock.getOutputStream();  
  
            while((recvMsgSize=in.read(receiveBuf))!=-1){
            	for (byte b : receiveBuf) {
            		System.out.print(b);
            	}
            	System.out.println();
                out.write(receiveBuf, 0, recvMsgSize);  
            }
          
            //c，通信完成后，使用Socket的close()方法关闭该客户端套接字链接  
            clntSock.close();  
        }  
    }  
}  