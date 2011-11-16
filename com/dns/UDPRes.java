package com.dns;

import java.util.ArrayList;
import java.util.List;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Record;

public class UDPRes {
	public byte[] getResData(byte[] queryBytes, Record[] records) {
		if (queryBytes == null) {
			return null;
		}
		List<Byte> resBytes = new ArrayList<Byte>(0);
		//System.out.println("Original, the size is: " + resBytes.size());
		// 将请求数据封入首部
		//System.out.println("The query size is:" + queryBytes.length);
		for (byte b : queryBytes) {
			//System.out.println("In add query, the size is:" + resBytes.size());
			resBytes.add(b);
			
		}
		// 封入恢复标志，第3字节首位置1
		resBytes.set(2, (byte)(((byte)resBytes.get(2)) | ((byte)0x80)));
		// 封入回答个数
		byte[] byteTemp = intToByteArray(records.length);
		resBytes.set(6, byteTemp[2]);
		resBytes.set(7, byteTemp[3]);
		
		//System.out.println("Out of for, the size is: " + resBytes.size());
		for (int j = 0; j < records.length; j++) {
			//System.out.println("\nAdd record: " + j + " the size is:" + resBytes.size());
			// 2字节域名指针
			if (j == 0) {
				resBytes.add((byte)0xc0); 
				resBytes.add((byte)0x0c);
			} else {
				resBytes.add((byte)0xc0);
				resBytes.add((byte)(0x0c + 16 + records[0].getName().length()));
			}
			//System.out.println("After add, the size is:" + resBytes.size());
			// 2字节规范名称 （类型 Type）
			byteTemp = intToByteArray(records[j].getType());
			resBytes.add(byteTemp[2]);
			resBytes.add(byteTemp[3]);
			// 2字节类 （类 Dclass）
			byteTemp = intToByteArray(records[j].getDClass());
			resBytes.add(byteTemp[2]);
			resBytes.add(byteTemp[3]);
			// 4字节TTL
			byteTemp = longToByteArray(records[j].getTTL());
			resBytes.add(byteTemp[0]);
			resBytes.add(byteTemp[1]);
			resBytes.add(byteTemp[2]);
			resBytes.add(byteTemp[3]);
			
			
			if (j == 0) {
				// 2字节数据长度 
				byteTemp = shortToByteArray(records[j].getName().length());
				resBytes.add(byteTemp[0]);
				resBytes.add(byteTemp[1]);
				// 域名
				String[] domains = records[j].getName().toString().split("\\.");
				for (String s : domains) {
					if (s != null && !s.isEmpty() && s!="") {
						byteTemp = s.getBytes();
						resBytes.add((byte)byteTemp.length);
						for (byte b : byteTemp) {
							resBytes.add(b);
						}
					}
				}
				resBytes.add((byte)0);
			} else { 
				// 2字节数据长度 
				resBytes.add((byte)0x00);
				resBytes.add((byte)0x04);
				//IP
				byteTemp = ((ARecord)records[j]).getAddress().getAddress();
				for (byte b : byteTemp) {
					resBytes.add(b);
				}
			}
		}
		
		byte[] res = new byte[resBytes.size()];
		int i = 0;
		for (byte b : resBytes) {
			res[i++] = b;
		}
		return res;
	}

	private  byte[] shortToByteArray(short s) {
		byte[] shortBuf = new byte[2];
		for(int i=0; i<2; i++) {
			int offset = (shortBuf.length - 1 -i)*8;
			shortBuf[i] = (byte)((s >>> offset)&0xff);
		}
		return shortBuf;
	}
	
	private  byte[] longToByteArray(long s) {
		byte[] shortBuf = new byte[4];
		for(int i=0; i<4; i++) {
			int offset = (shortBuf.length - 1 -i)*8;
			shortBuf[i] = (byte)((s >>> offset)&0xff);
		}
		return shortBuf;
	}
	
	private  byte[] intToByteArray(int s) {
		byte[] shortBuf = new byte[4];
		for(int i=0; i<4; i++) {
			int offset = (shortBuf.length - 1 -i)*8;
			shortBuf[i] = (byte)((s >>> offset)&0xff);
		}
		return shortBuf;
	}
}
