import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.nio.file.*;
import java.nio.charset.*;

public class mySystem {

	private final int MAX_DEPARTURES = 100000;

	private Server server;
	private ArrayList<Event> eventList;
	private ArrayList<Double> IATimes;
	private ArrayList<String> occupancy;
	private LinkedHashMap<Event, Event> packets;
	private LinkedList<Event> queue;
	private double clock;
	private double totalServiceTime;
	private int qSize;
	private int departure;
	private int maxCustomers;
	private int totalPacketsServiced;

	public mySystem() {
		this.server = new Server();
		this.eventList = new ArrayList<Event>();
		this.IATimes = new ArrayList<Double>();
		this.occupancy = new ArrayList<String>();
		this.packets = new LinkedHashMap<Event, Event>();
		this.queue = new LinkedList<Event>();
		this.clock = 0;
		this.qSize = 0;
		this.departure = 0;
		this.maxCustomers = 0;
		this.totalPacketsServiced = 0;
		this.totalServiceTime = 0;
	}

	public void printSystemInfo(int printTime) {
		System.out.println("Event list at time " + printTime);

		for (Event event : eventList) {
			event.print();
		}

		System.out.println("Server busy? " + server.isBusy());

		Entry<Event, Event> tailEntry = null;

		for (Entry<Event, Event> packet : packets.entrySet()) tailEntry = packet;

		double lastDeparture = tailEntry.getValue().getTime();
		double averageSericeTime = totalServiceTime / packets.size();
		double utilization = (packets.size() / lastDeparture) * averageSericeTime;

		System.out.println("Server utilization " + utilization);
		System.out.println();
	}

	public void run() {
		initialize();

		Event arrivalEvt = null;
		Event departureEvt = null;
		boolean have500Stats = false;
		boolean have5000Stats = false;
		boolean have10000Stats = false;

		while (!eventList.isEmpty()) {
			Event e = eventList.get(0);

			clock = e.getTime();

			if (clock > 500 && !have500Stats) {
				have500Stats = true;
				printSystemInfo(500);
			} else if (clock > 5000 && !have5000Stats) {
				have5000Stats = true;
				printSystemInfo(5000);
			} else if (clock > 10000 && !have10000Stats) {
				have10000Stats = true;
				printSystemInfo(10000);
			}

			if (e.getType().equals("arrival")) {
				qSize++;

				arrivalEvt = e;

				if (!server.isBusy()) {
					server.setBusy(true);

					Event ev = new Event("departure", server.getServiceTime() + clock);
					departureEvt = ev;

					packets.put(arrivalEvt, departureEvt);

					int pos = -1;

					for (Event event : eventList) {
						if (event.getTime() < ev.getTime()) {
							pos = eventList.indexOf(event);
						}
					}

					eventList.add(pos + 1, ev);
				} else {
					queue.add(arrivalEvt);
				}

				Event ev = new Event("arrival", getIATime() + clock);

				if (ev.getTime() - clock > 0) {
					int pos = -1;

					for (Event event : eventList) {
						if (event.getTime() < ev.getTime()) {
							pos = eventList.indexOf(event);
						}
					}

					eventList.add(pos + 1, ev);
				}
			}
			else if (e.getType().equals("departure")) {
				qSize--;
				departure++;

				occupancy.add(Integer.toString(qSize));

				if (qSize > maxCustomers) {
					maxCustomers = qSize;
				}

				if (departure == MAX_DEPARTURES) {
					stop();
					return;
				}

				server.setBusy(false);

				if (qSize > 0) {
					double serviceTime = server.getServiceTime();

					totalServiceTime += serviceTime;

					Event ev = new Event("departure", serviceTime + clock);

					arrivalEvt = queue.pop();
					departureEvt = ev;

					packets.put(arrivalEvt, departureEvt);

					server.setBusy(true);
					int pos = -1;

					for (Event event: eventList) {
						if (event.getTime() < ev.getTime()) {
							pos = eventList.indexOf(event);
						}
					}

					eventList.add(pos + 1, ev);
				}
			}

			eventList.remove(0);
		}
	}

	public void stop() {
		int numPacketsAboveTimeLimit = 0;

		for (Map.Entry<Event, Event> entry : packets.entrySet()) {
			if ((entry.getValue().getTime() - entry.getKey().getTime()) > 0.3) {
				numPacketsAboveTimeLimit++;
			}
		}

		System.out.println(numPacketsAboveTimeLimit + " / " + packets.size() + " (" + ((double) numPacketsAboveTimeLimit / packets.size()) + ")");

		Path file = Paths.get("output.txt");
		try {
			Files.write(file, occupancy, Charset.forName("UTF-8"));
		} catch (Exception e) {}
		System.out.println(maxCustomers);
		System.exit(0);
	}

	public void initialize() {
		importTimes();

		Event e = new Event("arrival", 0);
		IATimes.remove(0);
		eventList.add(e);
	}

	public double getIATime() {
		if (IATimes.isEmpty()) {
			return -1;
		}

		return IATimes.remove(0);
	}

	public void importTimes(){

		Scanner scaS;
		Scanner scaIA ;
		String path = "src/files/";
		String service = "serviceTimes-100K.txt";
		String interArrivals = "interArrivalTimes-100K.txt";

		FileReader frS=null;
		FileReader frIA=null;

		try {
			frS = new FileReader(path + service);
			frIA = new FileReader(path + interArrivals);
		} catch(FileNotFoundException e){
			System.out.println("error opening file" + e);
		}
		
		scaS = new Scanner(frS);
		scaIA = new Scanner(frIA);
		ArrayList<Double> serviceTimes = new ArrayList<Double>();

		for (int i = 0; i < MAX_DEPARTURES; i++){
			serviceTimes.add(scaS.nextDouble());
			IATimes.add(scaIA.nextDouble());
		}

		server.setServiceTimes(serviceTimes);

		scaS.close();
		scaIA.close();
	}

	public static void main(String args[]) {
		mySystem m = new mySystem();
		m.run();
	}
}