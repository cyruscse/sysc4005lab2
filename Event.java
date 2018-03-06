public class Event {
	private String type;
	private double time;

	public Event(String type, double time) {
		this.type = type;
		this.time = time;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public double getTime() {
		return time;
	}
	
	public void setTime(double time) {
		this.time = time;
	}

	public void print(){
		System.out.println(type + " @ " + time);
	}
}