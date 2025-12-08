package codelikethewind;

public class CallRecord {
    public double lat;
    public double lng;
    public int signal_strength;
    public boolean is_dropped;
    public String timestamp;
    public String cell_id; // ✅ add this

    public CallRecord(double lat, double lng, int signal_strength, boolean is_dropped, String timestamp, String cell_id) {
        this.lat = lat;
        this.lng = lng;
        this.signal_strength = signal_strength;
        this.is_dropped = is_dropped;
        this.timestamp = timestamp;
        this.cell_id = cell_id;
    }
}

