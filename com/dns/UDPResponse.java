package com.dns;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Record;

public class UDPResponse {
	byte[] queryData;

	short resCount;
	byte[] resData;

	short[] pDomain;
	short[] type;
	short[] dclass;
	long[] ttl;
	short[] dataLength;
	String[] name;
	byte[][] ip;

	public UDPResponse() {

	}

	public UDPResponse(Record[] records) {
		resCount = (short) (records.length);
		pDomain = new short[resCount];
		type = new short[resCount];
		dclass = new short[resCount];
		ttl = new long[resCount];
		dataLength = new short[resCount];
		ip = new byte[resCount][4];
		name = new String[resCount];
		
		for (int i = 0; i < records.length; i++) {
			//System.out.println(records[i].getName().toString());
			name[i] = ((ARecord)records[i]).getName().toString();
			if (i==0){
				pDomain[i] = (short) 0xc00c;
			} else if (name[i].equals(name[0])) {
				pDomain[i] = (short) (pDomain[0] + (short) 17 + (short) name[0].length());
			}
			type[i] = (short) records[i].getType();
			dclass[i] = (short) records[i].getDClass();
			ttl[i] = (short) records[i].getTTL();
			ip[i] = ((ARecord) records[i]).getAddress().getAddress().clone();
		}
	}

	public void setName(String[] name) {
		this.name = name.clone();
	}

	public void setQueryData(byte[] query) {
		this.queryData = query.clone();
	}

	public void setPdomain(int i, short pDomain) {
		this.pDomain[i] = pDomain;
	}

	public void setType(int i, short type) {
		this.type[i] = type;
	}

	public void setDclass(int i, short dclass) {
		this.dclass[i] = dclass;
	}

	public void setTTL(int i, long ttl) {
		this.ttl[i] = ttl;
	}

	public void setDataLength(int i, short dataLength) {
		this.dataLength[i] = dataLength;
	}

//	public void setIP(int i, int ip) {
//		this.ip[i] = ip;
//	}

	public byte[] getResData() {
		int len = queryData.length;
		len += 12 * resCount;
		for (int l : dataLength) {
			len += l;
		}
		resData = new byte[len];
		System.out.println("RES LENGTH:" + len);
		int i = 0;
		// 将请求数据封入首部
		for (; i < queryData.length; i++) {
			resData[i] = queryData[i];
		}
		// 封入回答个数
		byte[] resCnt = shortToByteArray(resCount);
		resData[6] = resCnt[0];
		resData[7] = resCnt[1];
		
		for (int j = 0; j < resCount; j++) {
			byte[] byteArray = shortToByteArray(pDomain[j]);
			resData[i++] = byteArray[0];
			resData[i++] = byteArray[1];
			
			byteArray = shortToByteArray(type[j]);
			resData[i++] = byteArray[0];
			resData[i++] = byteArray[1];
			
			byteArray = shortToByteArray(dclass[j]);
			resData[i++] = byteArray[0];
			resData[i++] = byteArray[1];
			
			byteArray = longToByteArray(ttl[j]);
			resData[i++] = byteArray[0];
			resData[i++] = byteArray[1];
			resData[i++] = byteArray[2];
			resData[i++] = byteArray[3];
			
			byteArray = shortToByteArray(dataLength[j]);
			resData[i++] = byteArray[0];
			resData[i++] = byteArray[1];
			
			if ((j==0 || !name[j].equals(name[0]))) {
				byte[] domainBytes = name[j].getBytes();
				for (int k = 0; k < domainBytes.length; k++) {
					resData[i++] = domainBytes[k];
				}
			} else {
				resData[i++] = ip[j][0];
				resData[i++] = ip[j][1];
				resData[i++] = ip[j][2];
				resData[i++] = ip[j][3];
			}
		}

		return resData;
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
	
	private  byte[] intToByteArray(long s) {
		byte[] shortBuf = new byte[4];
		for(int i=0; i<4; i++) {
			int offset = (shortBuf.length - 1 -i)*8;
			shortBuf[i] = (byte)((s >>> offset)&0xff);
		}
		return shortBuf;
	}
}
