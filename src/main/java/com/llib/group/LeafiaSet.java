package com.llib.group;
import com.llib.LLibRsc.*;

import java.util.*;

public class LeafiaSet<E> extends AbstractSet<E> implements Set<E> {
	static final LeafiaVoid VOID = new LeafiaVoid();
	transient int quickAccessIndex = 0;
	transient int[] quickAccess;
	transient int[] quickAccessSorted;
	private transient final int quickAccessSize;
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
			if (isEqualsInd(quickAccess[i&sizeSub],element)) {
				if ((i&sizeSub) == quickAccessIndex) return;
				if ((i&sizeSub) == nextIndex) break;
				quickAccess[i&sizeSub] = quickAccess[nextIndex];
				break;
			}
		}
		quickAccess[nextIndex] = element;
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
		for (int i = 0; i < quickAccessSize; i++) {
			int item = quickAccess[(quickAccessIndex-i)&sizeSub];
			if (item >= 0) {
				if (isEquals(contents[item],element))
					return item;
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
		Object[] transform = new Object[contents.length-1];
		if (index > 0)
			System.arraycopy(contents,0,transform,0,index);
		if (index < contents.length-1)
			System.arraycopy(contents,index+1,transform,index,contents.length-index-1);
		removeQuickAccess(index);
		contents = transform;
		return true;
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
		contents = new Object[]{};
		Arrays.fill(quickAccess,-1);
		skipStart = quickAccessSize;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
