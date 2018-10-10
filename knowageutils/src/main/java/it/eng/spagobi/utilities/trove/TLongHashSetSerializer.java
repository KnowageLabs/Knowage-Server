package it.eng.spagobi.utilities.trove;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.hash.TLongHashSet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class TLongHashSetSerializer extends Serializer<TLongHashSet> {

	private static final boolean DOES_NOT_ACCEPT_NULL = true;
	private static final boolean IMMUTABLE = false;

	public TLongHashSetSerializer() {
		super(DOES_NOT_ACCEPT_NULL, IMMUTABLE);
	}

	@Override
	public TLongHashSet read(Kryo kryo, Input input, Class<TLongHashSet> type) {
		final int size = input.readInt(true);
		TLongHashSet object = new TLongHashSet();
		for (int i = 0; i < size; ++i) {
			object.add(kryo.readObject(input, long.class));
		}
		return object;
	}

	@Override
	public void write(Kryo kryo, Output output, TLongHashSet object) {
		output.writeInt(object.size(), true);
		TLongIterator it = object.iterator();
		while (it.hasNext()) {
			kryo.writeObject(output, it.next());
		}
	}
}
