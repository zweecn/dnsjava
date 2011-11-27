package com.dns;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Record;

public class UDPRes {
	List<String> iplList;
	Record[] records;
	byte[] queryBytes;
	Map<InetAddress, InetAddress> ipmap;
	String[] cname;
	
	public UDPRes(byte[] queryBytes, Record[] records, Map<InetAddress, InetAddress> ipmap) {
		this.queryBytes = queryBytes;
		this.records = records;
		this.ipmap = ipmap;
		this.cname = new String[0];
	}
	
	public UDPRes(byte[] queryBytes, Record[] records, String[] cname, Map<InetAddress, InetAddress> ipmap) {
		this.queryBytes = queryBytes;
		this.records = records;
		this.ipmap = ipmap;
		this.cname = cname;
	}
	
	public byte[] getResData() {
		System.out.print("Producing the DNS packet...\t");
		long start = System.currentTimeMillis();
		//readBlockIp();
		if (queryBytes == null || records == null) {
			return null;
		}
		List<Byte> resBytes = new ArrayList<Byte>(0);
		// 将请求数据封入首部
		for (byte b : queryBytes) {
			resBytes.add(b);
			
		}
		// 封入恢复标志，第3字节首位置1
		resBytes.set(2, (byte)(((byte)resBytes.get(2)) | ((byte)0x80)));
		// 封入回答个数
		byte[] byteTemp = intToByteArray(records.length + cname.length);
		resBytes.set(6, byteTemp[2]);
		resBytes.set(7, byteTemp[3]);
		
		byte offset = 0;
		int c = 0;
		for (int j = 0; j < records.length; j++) {
			// 2字节域名指针
			if (c == 0) {
				resBytes.add((byte)0xc0); 
				resBytes.add((byte)0x0c);
				offset = (byte)0x0c;
			} else {
				resBytes.add((byte)0xc0);
				//resBytes.add((byte)(offset + 16 + records[0].getName().length()));
				resBytes.add((byte)(offset + 15 + cname[c-1].length()));
				//offset = (byte)(offset + 16 + cname[c-1].length());
			}
			// 2字节规范名称 （类型 Type）
			byteTemp = intToByteArray(records[j].getType());
			resBytes.add(byteTemp[2]);
			resBytes.add(byteTemp[3]);
			int cnamePos = resBytes.size()-1;
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
			
			
			if (c < cname.length) {
				resBytes.set(cnamePos, (byte)0x05);
				// 2字节数据长度 
				byteTemp = shortToByteArray((short)cname[c].length());
				//byteTemp = shortToByteArray(records[j].getName().length());
				resBytes.add(byteTemp[0]);
				resBytes.add(byteTemp[1]);
				// 域名
				String[] domains = (cname[c].split("\\."));				
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
				j--;
				c++;
			} else {
				// 2字节数据长度 
				resBytes.add((byte)0x00);
				resBytes.add((byte)0x04);
				//IP
				InetAddress addr = ((ARecord)records[j]).getAddress();
				//iplList.add(addr.getHostName());
				if (ipmap.get(addr) != null) {
					byteTemp = ipmap.get(addr).getAddress();
				} else {
					byteTemp = addr.getAddress();
				}
				for (byte b : byteTemp) {
					resBytes.add(b);
					//System.out.println(b + " ");
				}
			}
			
		} //end for
		
		byte[] res = new byte[resBytes.size()];
		int i = 0;
		for (byte b : resBytes) {
			res[i++] = b;
		}
		
		long pause = System.currentTimeMillis();
		System.out.println("Produce end. cost " + (pause - start) + " ms");
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
	
//	private void readBlockIp() {
//		addrMap = new HashMap<InetAddress, InetAddress>();
//		try {
//			FileReader fileReader = new FileReader(ipFileName);
//			BufferedReader bufferedReader = new BufferedReader(fileReader);
//			String line = null;
//			while ((line = bufferedReader.readLine()) != null) {
//				
//				if (line.isEmpty() || line.trim().charAt(0)=='#') {
//					continue;
//				}
//				String[] ips = line.trim().split(" ");
//				if (ips == null || ips.length < 1) {
//					continue;
//				} else if (ips.length >= 2) {
//					addrMap.put(InetAddress.getByName(ips[0]), InetAddress.getByName(ips[1]));
//				} else if (ips.length == 1) {
//					addrMap.put(InetAddress.getByName(ips[0]), InetAddress.getByName("127.0.0.1"));
//				}
//			}
//			blockIpSet = addrMap.keySet();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	public List<String> getIPList() {
		iplList = new ArrayList<String>();
		for (Record record : records) {
			InetAddress addr = ((ARecord)record).getAddress();
			if (ipmap.get(addr) != null) {
				iplList.add(ipmap.get(addr).getHostName());
			} else {
				iplList.add(addr.getHostName());
			}
		}
		return iplList;
	}
}
