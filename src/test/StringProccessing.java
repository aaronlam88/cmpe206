package test;

import java.util.Arrays;

public class StringProccessing {

	public static void main(String[] args) {
		String s = "this is a       test 	test1";
		String tokens[] = s.split("[ \t]+");

		System.out.println("'" + Arrays.toString(tokens) + "'");

		String numbs = "[0-10]";
		String[] numb = numbs.split("[\\[\\]-]");

		for (int i = 0; i < numb.length; ++i) {
			System.out.println(numb[i]);
		}
	}

}
