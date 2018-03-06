import java.util.ArrayList;

public class Server {
	private boolean busy;
	private ArrayList<Double> serviceTimes;
	
	public Server() {
		this.busy = false;
		serviceTimes = new ArrayList<Double>();
	}
	
	public boolean isBusy() {
		return busy;
	}
	
	public void setBusy(boolean busy) {
		this.busy = busy;
	}

	public void setServiceTimes(ArrayList<Double> stimes){
		serviceTimes = stimes;
	}

	public double getServiceTime(){
		return serviceTimes.remove(0);
	}

	public int remainingTimes() {
		return serviceTimes.size();
	}
}