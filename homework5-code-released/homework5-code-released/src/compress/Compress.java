package compress;

import java.util.Iterator;

import dsl.*;
import ecg.Data;

public class Compress {

	public static final int BLOCK_SIZE = 10;

	public static Query<Integer,Integer> delta() {
		// TODO
        Query<Integer, Integer> pr1 = Q.map(x -> {
            System.out.print(x + " d-> ");
            return x;
        });
        Query<Integer, Integer> pr2 = Q.map(x -> {
            System.out.println(x);
            return x;
        });
        Query<Integer, Integer> q = Q.pipeline(Q.emit(1, 0), Q.sWindow2((a, b) -> b - a));
        return Q.pipeline(pr1, Q.pipeline(q, pr2));
		// return Q.pipeline(Q.emit(1, 0), Q.sWindow2((a, b) -> b - a));
	}

	public static Query<Integer,Integer> deltaInv() {
		// TODO
        // Query<Integer, Integer> pr1 = Q.map(x -> {
        //     System.out.print(x + " d=> ");
        //     return x;
        // });
        // Query<Integer, Integer> pr2 = Q.map(x -> {
        //     System.out.println(x);
        //     return x;
        // });
        // Query<Integer, Integer> q = Q.scan(0, (a, b) -> a + b);
        // return Q.pipeline(pr1, Q.pipeline(q, pr2));
		return Q.scan(0, (a, b) -> a + b);
	}

	public static Query<Integer,Integer> zigzag() {
		// TODO
        Query<Integer, Integer> pr1 = Q.map(x -> {
            System.out.print(x + " -> ");
            return x;
        });
        Query<Integer, Integer> pr2 = Q.map(x -> {
            System.out.println(x);
            return x;
        });
        Query<Integer, Integer> q = Q.map((n) -> (n << 1) ^ (n >> (31)));
		return Q.pipeline(pr1, Q.pipeline(q, pr2));
        // return Q.map((n) -> (n << 1) ^ (n >> (31)));
	}

	public static Query<Integer,Integer> zigzagInv() {
		// TODO
        Query<Integer, Integer> pr1 = Q.map(x -> {
            System.out.print(x + " => ");
            return x;
        });
        Query<Integer, Integer> pr2 = Q.map(x -> {
            System.out.println(x);
            return x;
        });
        Query<Integer, Integer> q = Q.map((n) -> (n >> 1) ^ -(n & 1));
        return Q.pipeline(pr1, Q.pipeline(q, pr2));
        // return Q.map((n) -> (n >> 1) ^ -(n & 1));
	}

	public static Query<Integer,Integer> pack() {
		// TODO
        // Query<Integer, Integer> builder = Q.sWindow3((a, b, c) -> (a << 2 * BLOCK_SIZE) | (b << BLOCK_SIZE) | c);
        // Query<Integer, Integer> count = Q.scan(0, (a, b) -> a + 1);
        // Query<Integer, Integer[]> compress = Q.parallel(builder, count, (a, b) -> new Integer[]{a, b});
        // Query<Integer, Integer[]> printer = Q.pipeline(compress, Q.map(x -> {
        //     System.out.println("pack " + x[0] + " " + x[1]);
        //     return x;
        // }));
        // Query<Integer, Integer[]> filter = Q.pipeline(printer, Q.filter(x -> x[1] % 3 == 0));
        // Query<Integer, Integer> reduce = Q.pipeline(filter, Q.map(x -> x[0]));
        // Query<Integer, Integer> print = Q.pipeline(reduce, Q.map(x -> {
        //     System.out.println("x: " + x);
        //     return x;
        // }));
		// return print;
        return Q.map(x -> x);

	}

	public static Query<Integer,Integer> unpack() {
		// TODO
        // Query<Integer, Integer[]> expand = Q.map(x -> new Integer[]{x >> (2 * BLOCK_SIZE), (x >> BLOCK_SIZE) & ((1 << BLOCK_SIZE) - 1), x & ((1 << BLOCK_SIZE) - 1), 0});
        // Query<Integer, Integer> count = Q.fold(0, (a, b) -> a + 1);
        // Query<Integer, Integer> doubleCount = Q.pipeline(count, Q.dup());
        // Query<Integer, Integer[]> dup = Q.pipeline(expand, Q.pipeline(Q.dup(), Q.dup()));
        // Query<Integer, Integer[]> merge = Q.parallel(dup, doubleCount, (a, b) -> new Integer[]{a[0], a[1], a[2], b});
        // Query<Integer, Integer[]> filter = Q.pipeline(merge, Q.filter(x -> !(x[3] % 4 == 0)));
        // Query<Integer, Integer> reduce = Q.pipeline(filter, Q.map(x -> {
        //     if (x[3] % 4 == 1) {
        //         return x[0];
        //     } else if (x[3] % 4 == 2) {
        //         return x[1];
        //     } else {
        //         return x[2];
        //     }
        // }));

		// return reduce;
        return Q.map(x -> x);
	}

	public static Query<Integer,Integer> compress() {
		// TODO
		return Q.pipeline(Q.map(x -> {
            System.out.println("compress " + x);
            return x;
        }) , Q.pipeline(delta(), Q.pipeline(zigzag(), Q.pipeline(pack(), Q.map(x -> {
            System.out.println("compressed " + x);
            return x;
        })))));
	}

	public static Query<Integer,Integer> decompress() {
		// TODO
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
