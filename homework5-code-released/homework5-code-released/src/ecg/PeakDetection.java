package ecg;

import dsl.S;
import dsl.Q;
import dsl.Query;

import utils.Or;

public class PeakDetection {

	// The curve length transformation:
	//
	// adjust: x[n] = raw[n] - 1024
	// smooth: y[n] = (x[n-2] + x[n-1] + x[n] + x[n+1] + x[n+2]) / 5
	// deriv: d[n] = (y[n+1] - y[n-1]) / 2
	// length: l[n] = t(d[n-w]) + ... + t(d[n+w]), where
	//         w = 20 (samples) and t(d) = sqrt(1.0 + d * d)

	public static Query<Integer,Double> qLength() {
		// adjust >> smooth >> deriv >> length
        Query<Integer,Integer> x = Q.map(i -> i - 1024);
        Query<Integer, Integer> y1 = Q.sWindowInv(5, 0, Integer::sum, (a, b) -> a - b);
        Query<Integer, Double> y2 = Q.map(i -> i / 5.0);

        Query<Double, Double> d = Q.sWindow3((a, b, c) -> (a - c) / 2.0);

        Query<Double, Double> t= Q.map(i -> Math.sqrt(1.0 + i * i));
        Query<Double, Double> l = Q.sWindowInv(41, 0.0, Double::sum, (Double a, Double b) -> a - b);

        Query<Integer, Integer> p1 = Q.pipeline(x, y1);
        Query<Integer, Double> p2 = Q.pipeline(p1, y2);
        Query<Integer, Double> p3 = Q.pipeline(p2, d);
        Query<Integer, Double> p4 = Q.pipeline(p3, t);
        
        return Q.pipeline(p4, l);
	}

	// In order to detect peaks we need both the raw (or adjusted)
	// signal and the signal given by the curve length transformation.
	// Use the datatype VTL and implement the class Detect.

	public static Query<Integer,Long> qPeaks() {
		// convert to VTL
        Query<Or<Integer,VTL>,Integer> l1 =
        Q.pipeline(Q.filter(Or::isLeft), Q.map(Or::getLeft));

        Query<Or<Integer,VTL>,Long> r1 =
        Q.pipeline(Q.filter(Or::isRight), Q.map(o -> o.getRight().ts));

        Query<Or<Integer,VTL>,Long> r2 = Q.pipeline(r1, Q.emit(1, 39L));

        Query<Or<Integer,VTL>,Integer> l2 =
        Q.pipeline(Q.filter(Or::isLeft), Q.map(Or::getLeft));


        Query<Or<Integer,VTL>, Integer> raw = Q.pipeline(l1, Q.ignore(40));
        Query<Or<Integer,VTL>, Double> l = Q.pipeline(l2, qLength());

        Query<Or<Integer, VTL>, VTL> vtl = Q.parallel(raw, l, (r, len) -> new VTL(r, 0, len));

        Query<Integer, VTL> timestamp = Q.loop(Q.parallel(r2, vtl, (right, v) -> new VTL(v.v, right + 1, v.l)));

        // Query<Integer, VTL> print = Q.pipeline(timestamp, Q.map(v -> {
        // 	System.out.println("VTL Data: " + v.v + " " + v.l + " " + v.ts);
        // 	return v;
        // }));

        Query<Integer, Long> peaks = Q.pipeline(timestamp, new Detect());

		return peaks;
	}

	public static void main(String[] args) {
		System.out.println("****************************************");
		System.out.println("***** Algorithm for Peak Detection *****");
		System.out.println("****************************************");
		System.out.println();

		Q.execute(Data.ecgStream("100.csv"), qPeaks(), S.printer());
	}

}
