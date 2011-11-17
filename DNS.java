import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import org.xbill.DNS.*;


public class DNS {

	/**
	 * @param args
	 * @throws ZoneTransferException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ZoneTransferException {
		// TODO Auto-generated method stub

		InetAddress addr = Address.getByName("www.dnsjava.org");
		System.out.println(addr.getHostAddress());
		

		System.out.println();
		//Record [] records = new Lookup("google.com", Type.MX).run();
		Record [] records = new Lookup("www.google.cn", Type.A).run();
		for (int i = 0; i < records.length; i++) {
			//MXRecord mx = (MXRecord) records[i];
			ARecord a = (ARecord) records[i];
			//System.out.println("Host " + mx.getTarget() + " has preference " + mx.getPriority());
//			System.out.println("Host: " + a.getName() + " TTL:" + a.getTTL() 
//					+ " Type:" + a.getType() + " Class:" + a.getDClass() + " Name:" + a.getName());
			System.out.println("Type:" + a.getType() + " Class:" + a.getDClass() + " TTL:" + a.getTTL()
					+ " Name_Length:" + a.getName().length() 
					+ " Name:" + a.getName() + " IP:" + a.getAddress().getHostAddress());
		}
		
		
//		System.out.println();
//		Lookup l = new Lookup("version.bind.", Type.TXT, DClass.CH);
//		l.setResolver(new SimpleResolver(args[0]));
//		l.run();
//		if (l.getResult() == Lookup.SUCCESSFUL)
//			System.out.println(l.getAnswers()[0].rdataToString());
		
//		ZoneTransferIn xfr = ZoneTransferIn.newAXFR(new Name("."), "75.119.196.166", null);
//		List record = xfr.run();
//		for (Iterator it = record.iterator(); it.hasNext(); )
//			System.out.println(it.next());

	}

}
