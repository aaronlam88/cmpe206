package test;

import java.util.Arrays;

public class SplitString {

	public static void main(String[] args) {
		String str = "[1-10]";
		str = str.substring(1, str.length() - 1);
		String[] tokens = str.split("-");

		System.out.println(Arrays.toString(tokens));
	}

}
