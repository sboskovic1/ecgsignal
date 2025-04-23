package ecg;

import dsl.Query;
import dsl.SLastCount;
import dsl.Sink;

import dsl.S;

// The detection algorithm (decision rule) that we described in class
// (or your own slight variant of it).
//
// (1) Determine the threshold using the class TrainModel.
//
// (2) When l[n] exceeds the threhold, search for peak (max x[n] or raw[n])
//     in the next 40 samples.
//
// (3) No peak should be detected for 72 samples after the last peak.
//
// OUTPUT: The timestamp of each peak.

public class Detect implements Query<VTL,Long> {

	// Choose this to be two times the average length
	// over the entire signal.

	private static double THRESHOLD = 2 * 118.24411725491647; // TODO

	public long peakTime;
    public int peakSize;
    public long searchStart;
    public boolean searching;
    public long nextStart;

    private static final int SEARCH_WINDOW = 40;
    private static final int COOLDOWN = 72;

	public Detect() {
		this.peakTime = 0;
        this.peakSize = 0;
        this.searchStart = -1;
        this.searching = false;
        this.nextStart = 0;
	}

	@Override
	public void start(Sink<Long> sink) {
		// Do nothing
	}

	@Override
	public void next(VTL item, Sink<Long> sink) {
		if (!searching && item.l >= THRESHOLD && item.ts > searchStart) {
            searching = true;
            peakTime = item.ts;
            searchStart = item.ts;
            peakSize = item.v;
        } else if (searching) {
            if (item.v > peakSize) {
                peakTime = item.ts;
                peakSize = item.v;
            }
            if (item.ts - searchStart >= SEARCH_WINDOW) {
                searching = false;
                sink.next(peakTime);
                searchStart = item.ts + COOLDOWN;
            }
        }
	}

	@Override
	public void end(Sink<Long> sink) {
		sink.end();
	}
	
}
