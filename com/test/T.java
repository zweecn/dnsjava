package com.test;

public class T {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int a = 0x1234;
		System.out.printf("%x ", (byte)0xc0);
		byte[] array = intToByteArray(a);
		for (byte b : array) {
			System.out.printf("%x ", b);
		}
		
		
	}
	
	public  static byte[] intToByteArray(int s) {
		byte[] shortBuf = new byte[4];
		for(int i=0; i<4; i++) {
			int offset = (shortBuf.length - 1 -i)*8;
			System.out.println(offset);
			shortBuf[i] = (byte)((s >>> offset)&0xff);
		}
		return shortBuf;
	}
}
