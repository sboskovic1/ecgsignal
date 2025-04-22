package ecg;

import dsl.S;
import dsl.Q;
import dsl.Query;

public class TrainModel {

	// The average value of the signal l[n] over the entire input.
	public static Query<Integer,Double> qLengthAvg() {
        Query<Integer, Double> l = PeakDetection.qLength();

        Query<Integer, Integer> count = Q.fold(0, (a, b) -> b + 1);
        Query<Double, Double> sum = Q.fold(0.0, (a, b) -> a + b);
        Query<Integer, Double> pl = Q.pipeline(l, sum);
        return Q.parallel(pl, count, (a, b) -> a / b);
	}

	public static void main(String[] args) {
		System.out.println("***********************************************");
		System.out.println("***** Algorithm for finding the threshold *****");
		System.out.println("***********************************************");
		System.out.println();

		Q.execute(Data.ecgStream("100-all.csv"), qLengthAvg(), S.printer());
	}

}
