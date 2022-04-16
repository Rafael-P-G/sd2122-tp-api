package tp1.server.discovery;

import tp1.server.RESTDirServer;
import tp1.server.RESTUsersServer;

import java.io.IOException;
import java.net.*;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * <p>A class to perform service discovery, based on periodic service contact endpoint 
 * announcements over multicast communication.</p>
 * 
 * <p>Servers announce their *name* and contact *uri* at regular intervals. The server actively
 * collects received announcements.</p>
 * 
 * <p>Service announcements have the following format:</p>
 * 
 * <p>&lt;service-name-string&gt;&lt;delimiter-char&gt;&lt;service-uri-string&gt;</p>
 */
public class Discovery {
	private static Logger Log = Logger.getLogger(Discovery.class.getName());

	static {
		// addresses some multicast issues on some TCP/IP stacks
		System.setProperty("java.net.preferIPv4Stack", "true");
		// summarizes the logging format
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}
	
	
	// The pre-aggreed multicast endpoint assigned to perform discovery. 
	static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("227.227.227.227", 2277);
	static final int DISCOVERY_PERIOD = 1000;
	static final int DISCOVERY_TIMEOUT = 5000;

	// Used separate the two fields that make up a service announcement.
	private static final String DELIMITER = "\t";

	private InetSocketAddress addr;
	private String serviceName;
	private String serviceURI;

	private Map<String, Map<String, LocalTime>> receivedAnnouncements;

	/**
	 * @param  serviceName the name of the service to announce
	 * @param  serviceURI an uri string - representing the contact endpoint of the service being announced
	 */
	public Discovery( InetSocketAddress addr, String serviceName, String serviceURI) {
		this.addr = addr;
		this.serviceName = serviceName;
		this.serviceURI  = serviceURI;

		receivedAnnouncements = new HashMap<String, Map<String, LocalTime>>();
	}
	
	/**
	 * Continuously announces a service given its name and uri
	 * 
	 * @param serviceName the composite service name: <domain:service>
	 * @param serviceURI - the uri of the service
	 */
	public void announce(String serviceName, String serviceURI) {
		Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s\n", DISCOVERY_ADDR, serviceName, serviceURI));

		var pktBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();

		DatagramPacket pkt = new DatagramPacket(pktBytes, pktBytes.length, DISCOVERY_ADDR);
		// start thread to send periodic announcements
		new Thread(() -> {
			try (var ds = new DatagramSocket()) {
				for (;;) {
					try {
						Thread.sleep(DISCOVERY_PERIOD);
						ds.send(pkt);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * Listens for the given composite service name, blocks until a minimum number of replies is collected.
	 */
	
	public void listener() {
		Log.info(String.format("Starting discovery on multicast group: %s, port: %d\n", DISCOVERY_ADDR.getAddress(), DISCOVERY_ADDR.getPort()));

		final int MAX_DATAGRAM_SIZE = 65536;
		var pkt = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);

		new Thread(() -> {
			System.out.println(serviceName);
		    try (var ms = new MulticastSocket(DISCOVERY_ADDR.getPort())) {
			    joinGroupInAllInterfaces(ms);
				for(;;) {
					try {
						pkt.setLength(MAX_DATAGRAM_SIZE);
						ms.receive(pkt);
					
						var msg = new String(pkt.getData(), 0, pkt.getLength());
						//System.out.printf( "FROM %s (%s) : %s\n", pkt.getAddress().getCanonicalHostName(),
								//pkt.getAddress().getHostAddress(), msg);
						var tokens = msg.split(DELIMITER);
						if (tokens.length == 2) { //0 is service name  and 1 is service uri
							//TODO: to complete by recording the received information from the other node.
							Map<String, LocalTime> serviceInfo = receivedAnnouncements.get(tokens[0]);
							if(serviceInfo == null){
								serviceInfo = new HashMap<>();
							}
							serviceInfo.put(tokens[1], LocalTime.now());
							receivedAnnouncements.put(tokens[0], serviceInfo);

						}
					} catch (IOException e) {
						e.printStackTrace();
						try {
							Thread.sleep(DISCOVERY_PERIOD);
						} catch (InterruptedException e1) {
						// do nothing
						}
						Log.finest("Still listening...");
					}
				}
  		    } catch (IOException e) {
			    e.printStackTrace();
		    }
		}).start();
	}

	/**
	 * Returns the known servers for a service.
	 * 
	 * @param  serviceName the name of the service being discovered
	 * @return an array of URI with the service instances discovered. 
	 * 
	 */
	public Map<String, LocalTime> knownUrisOf(String serviceName) {
		//System.out.println(receivedAnnouncements);
		Map<String, LocalTime> announcements = receivedAnnouncements.get(serviceName);
		return announcements;
	}
	public String getOptimalURI(String serviceName){

		Map<String, LocalTime> urisFiles = knownUrisOf(serviceName);

		var bestURIWrapper = new Object(){String bestURI; int bestTime = -1;};
		final int currentTime = LocalTime.now().toSecondOfDay();

		urisFiles.forEach((k, v) ->{
			if(bestURIWrapper.bestURI == null){
				bestURIWrapper.bestURI = k;
			}
			else {
				if(bestURIWrapper.bestTime == -1){
					bestURIWrapper.bestTime = v.toSecondOfDay();
				}
				else {
					int timeDif = currentTime - v.toSecondOfDay();
					if(timeDif < bestURIWrapper.bestTime){
						bestURIWrapper.bestTime = v.toSecondOfDay();
						bestURIWrapper.bestURI = k;
					}
				}
			}
		});

		return bestURIWrapper.bestURI;
	}
	
	private void joinGroupInAllInterfaces(MulticastSocket ms) throws SocketException {
		Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
		while (ifs.hasMoreElements()) {
			NetworkInterface xface = ifs.nextElement();
			try {
				ms.joinGroup(DISCOVERY_ADDR, xface);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	}	

	/**
	 * Starts sending service announcements at regular intervals... 
	 */
	public void start() {
		announce(serviceName, serviceURI);
		listener();
	}

	// Main just for testing purposes
	public static void main( String[] args) throws Exception {
		Discovery discovery = new Discovery( DISCOVERY_ADDR, "test", "http://" + InetAddress.getLocalHost().getHostAddress());
		discovery.start();
	}
}
