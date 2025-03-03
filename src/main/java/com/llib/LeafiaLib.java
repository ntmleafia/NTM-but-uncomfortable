package com.llib;

public class LeafiaLib {
	/**
	 * <s>Replaces all occurrences of <tt>a</tt> and <tt>b</tt> within <tt>s</tt> with the other.</s>
	 * <p>Too painful to make.
	 * @param s The string to replace texts within
	 * @param a Pattern A
	 * @param b Pattern B
	 * @return Resulting string
	 */
	public static String stringSwap(String s,String a,String b) {
		/*
		StringBuilder builder = new StringBuilder(s.length());
		StringBuilder patternA = new StringBuilder();
		StringBuilder patternB = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (a.charAt(patternA.length()) == c) {
				patternA.append(c);
				if (patternA.toString().equals(a)) {
					builder.append(patternA);
					patternA = new StringBuilder();
				}
			} else if (b.charAt(patternB.length()) == c) {
				patternB.append(c);
				if (patternB.toString().equals(b)) {
					builder.append(patternB);
					patternB = new StringBuilder();
				}
			} else {

			}
			if (patternA.length() >= a.length()) {
				builder.append(patternA);
				patternA = new StringBuilder();
			} else if (patternB.length() >= b.length()) {
				builder.append(patternB);
				patternB = new StringBuilder();
			}
		}
		return builder.toString();*/
		throw new UnsupportedOperationException("Too painful to make :/");
	}
	/**
	 * Replaces all occurrences of <tt>a</tt> and <tt>b</tt> within <tt>s</tt> with the other.
	 * @param s The string to replace texts within
	 * @param a Char A
	 * @param b Char B
	 * @return Resulting string
	 */
	public static String stringSwap(String s,char a,char b) {
		StringBuilder builder = new StringBuilder(s.length());
		for (char c : s.toCharArray()) {
			if (c == a)
				builder.append(b);
			else if (c == b)
				builder.append(a);
			else
				builder.append(c);
		}
		return builder.toString();
	}
	public static class NumScale {
		static public final int KILO = 1_000;
		static public final int MEGA = 1_000_000;
		static public final int GIGA = 1_000_000_000;
		static public final long TERRA = 1_000_000_000_000L;
		static public final long PETA = 1_000_000_000_000_000L;
		static public final long EXA = 1_000_000_000_000_000_000L;
		//,ZETTA,YOTTA,RONNA,QUETTA;
	}
}
