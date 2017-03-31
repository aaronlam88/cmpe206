package test;

public class StringCompare {

	public static void main(String[] args) {
		String number = "1234";
		int numb = 1234;
		if (number.equals(Integer.toString(numb))) {
			System.out.println("TRUE");
		} else {
			System.out.println("FALSE");
		}
	}

}
