package com.llib.group;
import com.llib.LLibRsc.*;
import com.llib.exceptions.LeafiaDevFlaw;

import java.util.*;

/**
 * A precise Set that also allows concurrent modifications during iteration.
 * <p>Biggest difference is the <tt>contains</tt> method, while HashSet and HashMap compares
 * only the hash of instances, this one uses <tt>.equals()</tt> for more proper detection, albeit slower.
 * <p>Much recommended to use this one than HashSet unless we're dealing with billions of elements.
 * @param <E> the type of elements maintained by this set
 */
public class LeafiaSet<E> extends AbstractSet<E> implements Set<E>, Cloneable {
	static final LeafiaVoid VOID = new LeafiaVoid();
	transient int quickAccessIndex = 0;
	transient int[] quickAccess;
	transient int[] quickAccessSorted;
	public transient final int quickAccessSize;
	transient int skipStart;

	static Object[] emptyArray = new Object[]{};
	transient Object[] contents;

	public LeafiaSet(int shortcutBits) {
		this.quickAccessSize = 1 << shortcutBits;
		this.init();
	}
	public LeafiaSet() {
		this.quickAccessSize = 8;
		this.init();
	}
	private void init() {
		contents = emptyArray;
		quickAccess = new int[quickAccessSize];
		quickAccessSorted = new int[quickAccessSize];
		Arrays.fill(quickAccess,-1);
		this.skipStart = quickAccessSize;
	}
	public void reflect(LeafiaSet<E> set) {
		if (this.quickAccessSize != set.quickAccessSize) throw new LeafiaDevFlaw("Cannot reflect LeafiaSet with different quickAccessSize");
		quickAccess = new int[set.quickAccess.length];
		quickAccessSorted = new int[set.quickAccessSorted.length];
		skipStart = set.skipStart;
		quickAccessIndex = set.quickAccessIndex;
		contents = new Object[set.contents.length];
		System.arraycopy(set.quickAccess,0,quickAccess,0,set.quickAccess.length);
		System.arraycopy(set.quickAccessSorted,0,quickAccessSorted,0,set.quickAccessSorted.length);
		System.arraycopy(set.contents,0,contents,0,set.contents.length);
	}
	boolean isEquals(Object a,Object b) {
		if (a instanceof LeafiaVoid) return false;
		if (b instanceof LeafiaVoid) return false;
		if (a == null | b == null) {
			return (a == null) && (b == null);
		} else
			return a.equals(b);
	}
	boolean isEqualsInd(int a,int b) {
		if (Math.min(a,b) < 0) return false;
		return isEquals(contents[a],contents[b]);
	}
	void sortQuickAccess() {
		System.arraycopy(quickAccess,0,quickAccessSorted,0,quickAccessSize);
		Arrays.sort(quickAccessSorted);
		for (int i = 0; i < quickAccessSize; i++) {
			skipStart = i;
			if (quickAccessSorted[i] >= 0) break;
		}
	}
	void moveQuickAccess(int from,int to) {
		int amt = to-from;
		for (int i = 0; i < quickAccessSize; i++) {
			if (quickAccess[i] >= from)
				quickAccess[i] += amt;
		}
	}
	void addQuickAccess(int element) {
		int sizeSub = quickAccessSize-1;
		int nextIndex = (quickAccessIndex+1)&sizeSub;
		for (int i = quickAccessIndex; i > quickAccessIndex-quickAccessSize; i--) {
			// if there was duplicate on the quick access
			if (isEqualsInd(quickAccess[i&sizeSub],element)) {
				if ((i&sizeSub) == quickAccessIndex) return; // if it was already the first entry, do nothing
				if ((i&sizeSub) == nextIndex) break; // if it was the next to be replaced, ignore as it will be replaced by the same value anyway
				quickAccess[i&sizeSub] = quickAccess[nextIndex]; // swap out the duplicate with entry that would be overwritten next otherwise
				break;
			}
		}
		quickAccess[nextIndex] = element; // write quick access
		quickAccessIndex = nextIndex;
		sortQuickAccess();
	}
	void removeQuickAccess(int element) {
		int sizeSub = quickAccessSize-1;
		int nextIndex = (quickAccessIndex+1)&sizeSub;
		for (int i = quickAccessIndex; i > quickAccessIndex-quickAccessSize; i--) {
			if (isEqualsInd(quickAccess[i&sizeSub],element)) {
				quickAccess[i&sizeSub] = quickAccess[nextIndex];
				quickAccess[nextIndex] = -1;
				sortQuickAccess();
				return;
			}
		}
	}
	int quickSearch(Object element) {
		int sizeSub = quickAccessSize-1;
		int nextIndex = (quickAccessIndex+1)&sizeSub;
		for (int i = 0; i < quickAccessSize; i++) {
			int item = quickAccess[(quickAccessIndex-i)&sizeSub];
			if (item >= 0) {
				if (isEquals(contents[item],element)) {
					// swap out the position of this entry with the latest one
					// do not call sortQuickAccess as this does not change values, just the order
					quickAccess[(quickAccessIndex-i)&sizeSub] = quickAccess[nextIndex];
					quickAccess[nextIndex] = item;
					quickAccessIndex = nextIndex;
					return item;
				}
			}
		}
		return -1;
	}
	@Override
	public Iterator<E> iterator() {
		Object[] copy = new Object[LeafiaSet.this.contents.length];
		System.arraycopy(contents,0,copy,0,contents.length);
		return new Iterator<E>() {
			int needle = 0;
			@Override
			public boolean hasNext() {
				return copy.length > needle;
			}
			@Override
			public E next() {
				return (E)(copy[needle++]);
			}
		};
	}

	////////////////////////////////////////////////////////////

	@Override
	public int size() {
		return this.contents.length;
	}
	@Override
	public boolean isEmpty() {
		return super.isEmpty();
	}
	@Override
	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}
	public int indexOf(Object element) {
		int quick = quickSearch(element);
		if (quick >= 0) return quick;
		int skip = skipStart;
		for (int i = 0; i < contents.length; i++) {
			if (skip < quickAccessSize) {
				if (quickAccessSorted[skip] == i) {
					skip++;
					continue;
				}
			}
			if (isEquals(element,contents[i])) {
				addQuickAccess(i);
				return i;
			}
		}
		return -1;
	}
	@Override
	public Object[] toArray() {
		Object[] readOnlyArray = new Object[contents.length];
		System.arraycopy(contents,0,readOnlyArray,0,contents.length);
		return readOnlyArray;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return super.toArray(a);
	}

	@Override
	public boolean add(E e) {
		if (contains(e)) return false;
		contents = Arrays.copyOf(contents,contents.length+1);
		contents[contents.length-1] = e;
		return true;
	}
	@Override
	public boolean remove(Object o) {
		int index = indexOf(o);
		if (index < 0) return false;
		removeIndex(index);
		return true;
	}
	public void removeIndex(int index) {
		Object[] transform = new Object[contents.length-1];
		if (index > 0)
			System.arraycopy(contents,0,transform,0,index);
		if (index < contents.length-1)
			System.arraycopy(contents,index+1,transform,index,contents.length-index-1);
		removeQuickAccess(index);
		contents = transform;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return super.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return super.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return super.retainAll(c);
	}

	@Override
	public void clear() {
		contents = emptyArray;
		Arrays.fill(quickAccess,-1);
		skipStart = quickAccessSize;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public LeafiaSet<E> clone() {
		LeafiaSet<E> copy = new LeafiaSet<>(Integer.numberOfTrailingZeros(quickAccessSize));
		copy.reflect(this);
		return copy;
	}
}
