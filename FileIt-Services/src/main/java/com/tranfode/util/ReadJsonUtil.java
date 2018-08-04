package com.tranfode.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ReadJsonUtil {

	public static boolean CheckBinderWithSameName(JSONArray oJsonArray, String bookName) {
		boolean isSameBookName = false;
		try {

			for (Object obj : oJsonArray) {
				JSONObject book = (JSONObject) obj;
				if (book.containsKey(bookName)) {
					isSameBookName = true;
				}
			}
		} catch (Exception e) {
			// System.out.println(e.getMessage());
		}
		return isSameBookName;

	}
}
