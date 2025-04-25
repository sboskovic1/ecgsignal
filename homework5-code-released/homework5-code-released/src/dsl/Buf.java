package dsl;

// Buflicate each input item.

public class Buf<A> implements Query<A,A> {

    private final int size;

	public Buf(int n) {
        this.size = n;
		// nothing to do
	}

	@Override
	public void start(Sink<A> sink) {
		// nothing to do
	}

	@Override
	public void next(A item, Sink<A> sink) {
        for (int i = 0; i < size; i++) {
            sink.next(item);
        }
	}

	@Override
	public void end(Sink<A> sink) {
		sink.end();
	}
	
}
