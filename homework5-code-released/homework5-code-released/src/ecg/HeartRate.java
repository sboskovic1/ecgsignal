package ecg;

import dsl.S;
import dsl.Q;
import dsl.Query;

// This file is devoted to the analysis of the heart rate of the patient.
// It is assumed that PeakDetection.qPeaks() has already been implemented.

public class HeartRate {

	// RR interval length (in milliseconds)
	public static Query<Integer,Double> qIntervals() {
        Query<Integer,Long> peaks = PeakDetection.qPeaks();
        Query<Long, Double> diff = Q.sWindow2((a, b) -> (double)(b - a));
		return Q.pipeline(peaks, diff);
	}

	// Average heart rate (over entire signal) in bpm.
	public static Query<Integer,Double> qHeartRateAvg() {
		return Q.pipeline(Q.map((i) -> (double)i), Q.foldAvg());
	}

	// Standard deviation of NN interval length (over the entire signal)
	// in milliseconds.
	public static Query<Integer,Double> qSDNN() {
		return Q.pipeline(PeakDetection.qPeaks(), Q.pipeline(Q.map(i -> (double)i), Q.foldStdev()));
	}

	// RMSSD measure (over the entire signal) in milliseconds.
	public static Query<Integer,Double> qRMSSD() {
		Query<Integer,Double> peaks = qIntervals();
        Query<Integer, Double> sq = Q.pipeline(peaks, Q.map(i -> i * i));
        Query<Integer, Double> avg = Q.pipeline(sq, Q.foldAvg());
        Query<Integer, Double> sqrt = Q.pipeline(avg, Q.map(i -> Math.sqrt(i)));

		return sqrt;
	}

	// Proportion (in %) derived by dividing NN50 by the total number
	// of NN intervals (calculated over the entire signal).
	public static Query<Integer,Double> qPNN50() {
		Query<Integer,Double> peaks = qIntervals();
        Query<Integer, Double> map50 = Q.pipeline(peaks, Q.map(i -> i > 50 ? 1.0 : 0.0));
        Query<Integer, Double> avg = Q.pipeline(map50, Q.foldAvg());
        Query<Integer, Double> pnn50 = Q.pipeline(avg, Q.map(i -> i * 100));
		return pnn50;
	}

	public static void main(String[] args) {
		System.out.println("****************************************");
		System.out.println("***** Algorithm for the Heart Rate *****");
		System.out.println("****************************************");
		System.out.println();

		System.out.println("***** Intervals *****");
		Q.execute(Data.ecgStream("100.csv"), qIntervals(), S.printer());
		System.out.println();

		System.out.println("***** Average heart rate *****");
		Q.execute(Data.ecgStream("100-all.csv"), qHeartRateAvg(), S.printer());
		System.out.println();

		System.out.println("***** HRV Measure: SDNN *****");
		Q.execute(Data.ecgStream("100-all.csv"), qSDNN(), S.printer());
		System.out.println();

		System.out.println("***** HRV Measure: RMSSD *****");
		Q.execute(Data.ecgStream("100-all.csv"), qRMSSD(), S.printer());
		System.out.println();

		System.out.println("***** HRV Measure: pNN50 *****");
		Q.execute(Data.ecgStream("100-all.csv"), qPNN50(), S.printer());
		System.out.println();
	}

}
