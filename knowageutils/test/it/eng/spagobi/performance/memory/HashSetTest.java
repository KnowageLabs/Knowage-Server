package it.eng.spagobi.performance.memory;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.hash.TLongHashSet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import objectexplorer.MemoryMeasurer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HashSetTest {

	public Set<Long> setA;
	public Set<Long> setB;
	public TLongHashSet tSetA;
	public TLongHashSet tSetB;
	public int NUMBER_OF_VALUES_SET_A = 500000;
	public int NUMBER_OF_VALUES_SET_B = 500000;
	public int MAX_VALUE = 1000000;

	@Before
	public void setUp() throws Exception {
		setA = new HashSet<>();
		setB = new HashSet<>();
		tSetA = new TLongHashSet();
		tSetB = new TLongHashSet();

		for (int i = 0; i < NUMBER_OF_VALUES_SET_A; i++) {
			long e = ThreadLocalRandom.current().nextLong(MAX_VALUE);
			setA.add(e);
			tSetA.add(e);
		}

		for (int i = 0; i < NUMBER_OF_VALUES_SET_B; i++) {
			long e = ThreadLocalRandom.current().nextLong(MAX_VALUE);
			setB.add(e);
			tSetB.add(e);
		}
	}

	@After
	public void tearDown() throws Exception {
		setA.clear();
		setB.clear();
		tSetA.clear();
		tSetB.clear();
	}

	@Test
	public void test() throws IOException, ClassNotFoundException, DataFormatException {
		System.out.println("---------------------------------------------------------------------");

		float memoryA = MemoryMeasurer.measureBytes(setA) / 1048576;
		float memoryB = MemoryMeasurer.measureBytes(setB) / 1048576;
		System.out.println("Memory used for HashSet<Long> with " + setA.size() + " elements is: " + memoryA + " Mbytes");
		System.out.println("Memory used for HashSet<Long> with " + setB.size() + " elements is: " + memoryB + " Mbytes");
		long startTime = System.currentTimeMillis();
		float intersect = intersectCount(setA, setB);
		System.out.println("Intesect with own method computed in about: " + (System.currentTimeMillis() - startTime) + "ms");
		float notIntersect = notIntersectCount(setA, setB);
		System.out.println("Intersect cardinality: " + intersect);
		System.out.println("Own similary coefficient: " + (intersect / notIntersect));
		System.out.println("Jaccard coefficient: " + (intersect / (setA.size() + setB.size() - intersect)));
		System.out.println("Sorensen coefficient: " + (2 * intersect / (setA.size() + setB.size())));

		System.out.println("---------------------------------------------------------------------");

		memoryA = MemoryMeasurer.measureBytes(tSetA) / 1048576;
		memoryB = MemoryMeasurer.measureBytes(tSetB) / 1048576;
		System.out.println("Memory used for TLongHashSet with " + tSetA.size() + " elements is: " + memoryA + " Mbytes");
		System.out.println("Memory used for TLongHashSet with " + tSetB.size() + " elements is: " + memoryB + " Mbytes");
		startTime = System.currentTimeMillis();
		intersect = tIntersectCount(tSetA, tSetB);
		System.out.println("Intesect with own method computed in about: " + (System.currentTimeMillis() - startTime) + "ms");
		notIntersect = tNotIntersectCount(tSetA, tSetB);
		System.out.println("Intersect cardinality: " + intersect);
		System.out.println("Own similary coefficient: " + (intersect / notIntersect));
		System.out.println("Jaccard coefficient: " + (intersect / (tSetA.size() + tSetB.size() - intersect)));
		System.out.println("Sorensen coefficient: " + (2 * intersect / (tSetA.size() + tSetB.size())));
		byte[] compressed = compress(toByteArray(tSetA.toArray()));
		tSetA.clear();
		System.out.println("Memory used for TLongHashSet with " + tSetA.size() + " elements is: " + memoryA + " Mbytes");
		long[] data = (long[]) toObject(decompress(compressed));
		tSetA.addAll(data);
		System.out.println("Memory used for TLongHashSet with " + tSetA.size() + " elements is: " + memoryA + " Mbytes");

		System.out.println("---------------------------------------------------------------------");
	}

	public int notIntersectCount(Set<Long> setA, Set<Long> setB) {
		int count = 0;
		for (Long e : setA) {
			if (!setB.contains(e)) {
				count++;
			}
		}
		for (Long e : setB) {
			if (!setA.contains(e)) {
				count++;
			}
		}
		return count;
	}

	public int intersectCount(Set<Long> setA, Set<Long> setB) {
		Set<Long> a;
		Set<Long> b;
		if (setA.size() <= setB.size()) {
			a = setA;
			b = setB;
		} else {
			a = setB;
			b = setA;
		}
		int count = 0;
		for (Long e : a) {
			if (b.contains(e)) {
				count++;
			}
		}
		return count;
	}

	public int tNotIntersectCount(TLongHashSet setA, TLongHashSet setB) {
		int count = 0;
		TLongIterator it = setA.iterator();
		while (it.hasNext()) {
			if (!setB.contains(it.next())) {
				count++;
			}
		}
		it = setB.iterator();
		while (it.hasNext()) {
			if (!setA.contains(it.next())) {
				count++;
			}
		}
		return count;
	}

	public int tIntersectCount(TLongHashSet setA, TLongHashSet setB) {
		TLongHashSet a;
		TLongHashSet b;
		if (setA.size() <= setB.size()) {
			a = setA;
			b = setB;
		} else {
			a = setB;
			b = setA;
		}
		int count = 0;
		TLongIterator it = a.iterator();
		while (it.hasNext()) {
			if (b.contains(it.next())) {
				count++;
			}
		}
		return count;
	}

	public static byte[] compress(byte[] data) throws IOException {
		Deflater deflater = new Deflater();
		deflater.setInput(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		deflater.finish();
		byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer); // returns the generated code... index
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		byte[] output = outputStream.toByteArray();
		System.out.println("Original: " + data.length / 1048576 + " Mb");
		System.out.println("Compressed: " + output.length / 1048576 + " Mb");
		return output;
	}

	public byte[] decompress(byte[] data) throws IOException, DataFormatException {
		Inflater inflater = new Inflater();
		inflater.setInput(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		byte[] buffer = new byte[1024];
		while (!inflater.finished()) {
			int count = inflater.inflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		byte[] output = outputStream.toByteArray();
		System.out.println("Original: " + data.length / 1048576 + " Mb");
		System.out.println("Decompressed: " + output.length / 1048576 + " Mb");
		return output;
	}

	public byte[] toByteArray(Object obj) throws IOException {
		byte[] bytes = null;
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			bytes = bos.toByteArray();
		} finally {
			if (oos != null) {
				oos.close();
			}
			if (bos != null) {
				bos.close();
			}
		}
		return bytes;
	}

	public Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
		Object obj = null;
		ByteArrayInputStream bis = null;
		ObjectInputStream ois = null;
		try {
			bis = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(bis);
			obj = ois.readObject();
		} finally {
			if (bis != null) {
				bis.close();
			}
			if (ois != null) {
				ois.close();
			}
		}
		return obj;
	}
}
