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

public class mySystem2 {

	private final int MAX_DEPARTURES = 100000;

	private Server server1, server2;
	private ArrayList<Event> eventList;
	private ArrayList<Double> IATimes;
	private ArrayList<String> occupancy;
	private LinkedHashMap<Event, Event> packets, packets2;
	private LinkedList<Event> queue, queue2;
	private double clock;
	private double totalServiceTime, totalServiceTime2;
	private double lastDepartureServerOne, lastDepartureServerTwo;
	private int qSize, qSize2;
	private int departure;
	private int maxCustomers;
	private int totalPacketsServicedOne, totalPacketsServicedTwo;

	public mySystem2() {
		this.server1 = new Server();
		this.server2 = new Server();
		this.eventList = new ArrayList<Event>();
		this.IATimes = new ArrayList<Double>();
		this.occupancy = new ArrayList<String>();
		this.packets = new LinkedHashMap<Event, Event>();
		this.packets2 = new LinkedHashMap<Event, Event>();
		this.queue = new LinkedList<Event>();
		this.queue2 = new LinkedList<Event>();
		this.clock = 0;
		this.qSize = 0;
		this.qSize2 = 0;
		this.departure = 0;
		this.maxCustomers = 0;
		this.totalPacketsServicedOne = 0;
		this.totalPacketsServicedTwo = 0;
		this.totalServiceTime = 0;
		this.totalServiceTime2 = 0;
		this.lastDepartureServerOne = 0;
		this.lastDepartureServerTwo = 0;
	}

	public void printSystemInfo(int printTime) {
		System.out.println("Event list at time " + printTime);

		for (Event event : eventList) {
			event.print();
		}

		System.out.println("server1 busy? " + server1.isBusy());
		System.out.println("server2 busy? " + server2.isBusy());

		double averageSericeTimeOne = totalServiceTime / totalPacketsServicedOne;
		double averageSericeTimeTwo = totalServiceTime2 / totalPacketsServicedTwo;

		double utilizationOne = (totalPacketsServicedOne / lastDepartureServerOne) * averageSericeTimeOne;
		double utilizationTwo = (totalPacketsServicedTwo / lastDepartureServerTwo) * averageSericeTimeTwo;

		System.out.println("server1 utilization " + utilizationOne);
		System.out.println("server2 utilization " + utilizationTwo);
		System.out.println();
	}

	public void run() {
		initialize();

		Event arrivalEvt = null;
		Event arrival2Evt = null;
		Event departureEvt = null;
		Event departure2Evt = null;
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

			if (e.getType().equals("arrivalOne")) {
				qSize++;

				arrivalEvt = e;

				if (!server1.isBusy()) {
					server1.setBusy(true);

					Event ev = new Event("departureOne", server1.getServiceTime() + clock);
					departureEvt = ev;

					packets.put(arrivalEvt, departureEvt);
					totalPacketsServicedOne++;

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

				Event ev = new Event("arrivalOne", getIATime() + clock);

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
			else if (e.getType().equals("departureOne")) {
				lastDepartureServerOne = e.getTime();
				qSize--;
				server1.setBusy(false);

				qSize2++;
				arrival2Evt = e;

				if (!server2.isBusy()) {
					server2.setBusy(true);

					Event ev = new Event("departureTwo", server2.getServiceTime() + clock);
					departure2Evt = ev;

					packets2.put(arrival2Evt, departure2Evt);
					totalPacketsServicedTwo++;

					int pos = -1;

					for (Event event : eventList) {
						if (event.getTime() < ev.getTime()) {
							pos = eventList.indexOf(event);
						}
					}

					eventList.add(pos + 1, ev);
				} else {
					queue2.add(arrival2Evt);
				}

				if (qSize > 0) {
					double serviceTime = server1.getServiceTime();

					totalServiceTime += serviceTime;

					Event ev = new Event("departureOne", serviceTime + clock);

					arrivalEvt = queue.pop();
					departureEvt = ev;

					packets.put(arrivalEvt, departureEvt);
					totalPacketsServicedOne++;

					server1.setBusy(true);
					int pos = -1;

					for (Event event: eventList) {
						if (event.getTime() < ev.getTime()) {
							pos = eventList.indexOf(event);
						}
					}

					eventList.add(pos + 1, ev);
				}
			}
			else if (e.getType().equals("departureTwo")) {
				lastDepartureServerTwo = e.getTime();
				qSize2--;
				departure++;

				occupancy.add(Integer.toString(qSize + qSize2));

				if (qSize + qSize2 > maxCustomers) {
					maxCustomers = qSize + qSize2;
				}

				if (departure == MAX_DEPARTURES) {
					stop();
					return;
				}

				server2.setBusy(false);

				if (qSize2 > 0) {
					double serviceTime = server2.getServiceTime();

					totalServiceTime2 += serviceTime;

					Event ev = new Event("departureTwo", serviceTime + clock);

					arrival2Evt = queue2.pop();
					departure2Evt = ev;

					packets2.put(arrival2Evt, departure2Evt);
					totalPacketsServicedTwo++;

					server2.setBusy(true);
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
			Event departure2Evt = packets2.get(entry.getValue());
			if ((departure2Evt.getTime() - entry.getKey().getTime()) > 0.2) {
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

		Event e = new Event("arrivalOne", 0);
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
		Scanner scaS2;
		Scanner scaIA ;
		String path = "src/files/";
		String service = "serviceTimes-100K.txt";
		String service2 = "serviceTimes2-100K.txt";
		String interArrivals = "interArrivalTimes-100K.txt";

		FileReader frS=null;
		FileReader frS2=null;
		FileReader frIA=null;

		try {
			frS = new FileReader(path + service);
			frS2 = new FileReader(path + service2);
			frIA = new FileReader(path + interArrivals);
		} catch(FileNotFoundException e){
			System.out.println("error opening file" + e);
			System.exit(0);
		}

		scaS = new Scanner(frS);
		scaS2 = new Scanner(frS2);
		scaIA = new Scanner(frIA);
		ArrayList<Double> serviceTimes = new ArrayList<Double>();
		ArrayList<Double> serviceTimes2 = new ArrayList<Double>();

		for (int i = 0; i < MAX_DEPARTURES; i++){
			serviceTimes.add(scaS.nextDouble());
			serviceTimes2.add(scaS2.nextDouble());
			IATimes.add(scaIA.nextDouble());
		}

		server1.setServiceTimes(serviceTimes);
		server2.setServiceTimes(serviceTimes2);

		scaS.close();
		scaS2.close();
		scaIA.close();
	}

	public static void main(String args[]) {
		mySystem2 m = new mySystem2();
		m.run();
	}
}