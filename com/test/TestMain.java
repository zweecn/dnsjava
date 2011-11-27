package com.test;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.udp.UDPServer;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String args[]) {
		new UDPServer();
		
//		try {
//			//Record[] records = new Lookup("180.227.217.219.in-addr.arpa", Type.PTR).run();
//			Record[] records = new Lookup("22.255.152.204.in-addr.arpa", Type.PTR).run();
//			System.out.println(records);
//			for (Record record : records) {
//				PTRRecord ptrecord = (PTRRecord) record;
//				System.out.println(ptrecord.getTarget());
//			}
//			
//		} catch (TextParseException e) {
//			e.printStackTrace();
//		}
	}
}
