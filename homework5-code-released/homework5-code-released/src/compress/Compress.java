package compress;

import java.util.Iterator;

import dsl.*;
import ecg.Data;

public class Compress {

	public static final int BLOCK_SIZE = 10;

	public static Query<Integer,Integer> delta() {
		return Q.pipeline(Q.emit(1, 0), Q.sWindow2((a, b) -> b - a));
	}

	public static Query<Integer,Integer> deltaInv() {

		return Q.scan(0, (a, b) -> a + b);
	}

	public static Query<Integer,Integer> zigzag() {
        return Q.map((n) -> (n << 1) ^ (n >> (31)));
	}

	public static Query<Integer,Integer> zigzagInv() {
        return Q.map((n) -> (n >> 1) ^ -(n & 1));
	}

	public static Query<Integer,Integer> pack() {
        return Q.map(x -> x);

	}

	public static Query<Integer,Integer> unpack() {
        return Q.map(x -> x);
	}

	public static Query<Integer,Integer> compress() {
		return Q.pipeline(delta(), Q.pipeline(zigzag(), pack()));
	}

	public static Query<Integer,Integer> decompress() {
		return Q.pipeline(unpack(), Q.pipeline(zigzagInv(), deltaInv()));
	}


	public static void main(String[] args) {
		System.out.println("**********************************************");
		System.out.println("***** ToyDSL & Compression/Decompression *****");
		System.out.println("**********************************************");
		System.out.println();

		System.out.println("***** Compress *****");
		{
			// from range [0,2048) to [0,256)
			Query<Integer,Integer> q1 = Q.map(x -> x / 8);
			Query<Integer,Integer> q2 = compress();
			Query<Integer,Integer> q = Q.pipeline(q1, q2);
			Iterator<Integer> it = Data.ecgStream("100-all.csv");
			Q.execute(it, q, S.lastCount());
		}
		System.out.println();

		System.out.println("***** Compress & Decompress *****");
		{
			// from range [0,2048) to [0,256)
			Query<Integer,Integer> q1 = Q.map(x -> x / 8);
			Query<Integer,Integer> q2 = compress();
			Query<Integer,Integer> q3 = decompress();
			Query<Integer,Integer> q = Q.pipeline(q1, q2, q3);
			Iterator<Integer> it = Data.ecgStream("100-all.csv");
			Q.execute(it, q, S.lastCount());
		}
		System.out.println();
	}

}
