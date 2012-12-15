package cz.muni.fi.smartlib.utils;

import java.util.List;

import android.widget.RatingBar;

public class Validator {
	public static boolean isNullorEmpty(String s) {
		if (s == null || s.trim().equals("")) {
			return true;
		}
		return false;
	}
	
	public static boolean isNullorEmpty(List l) {
		if (l == null || l.isEmpty()) {
			return true;
		}
		return false;
	}
	
	public static boolean isRatingOK(RatingBar rating) {
		return (rating.getRating() > 0 && rating.getRating() <= 5)? true :false;
	}
	
	private static final int ONE = 1;
	private static final int THREE = 3;
	
	public static boolean isISBN13 (String barcode) {
		if (barcode.length() != 13) {
			return false;
		}
		
		int sum = 0;
		
		//make int array from barcode
		int[] barcodeArray = new int[13];
		for (int i = 0; i < 13; i++) {
			barcodeArray[i] = Integer.valueOf(Character.toString(barcode.charAt(i)));
		}
		
		
		int multipler = ONE; 
		for (int i = 0; i < 13; i++) {
			multipler = (i % 2 == 0)? ONE : THREE;
			sum += multipler * barcodeArray[i];
		}
		
		return (sum % 10 == 0);
	}
	
	public static boolean isISBN10 (String barcode) {
		if (barcode.length() != 10) {
			return false;
		}
		
		int sum = 0;
		
		//make int array from barcode
		int[] barcodeArray = new int[10];
		for (int i = 0; i < 10; i++) {
			barcodeArray[i] = Integer.valueOf(Character.toString(barcode.charAt(i)));
			
		}
		
		 
		for (int i = 0; i < 10; i++) {
			sum += (10 - i) * barcodeArray[i];
		}
		
		return (sum % 11 == 0);
	}
	
	public static String constructISBN10FromEAN13(String isbn13) throws IllegalArgumentException {
		
		if (!isISBN13(isbn13)) {
			throw new IllegalArgumentException("isbn13 is not EAN13!");
		}
		
		String isbn10 = isbn13.substring(3, isbn13.length() - 1);
		System.out.println("odstraneni prefixu a suffixu: " + isbn10);
		int[] isbn10Array = new int[9];
		for (int i = 0; i < 9; i++) {
			isbn10Array[i] = Integer.valueOf(Character.toString(isbn10.charAt(i)));
			System.out.println(isbn10Array[i]);
		}
		
		
		int sum = 0;
		
		for (int i = 0; i < 9; i++) {
			sum += (10 - i) * isbn10Array[i];
		}
		System.out.println("sum 9: " + sum);
		
		isbn10 += String.valueOf(11 - (sum % 11));
		System.out.println("finalni isbn10:" + isbn10);
		return isbn10;
	}
}
