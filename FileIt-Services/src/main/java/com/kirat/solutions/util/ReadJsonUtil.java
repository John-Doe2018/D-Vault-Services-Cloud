package com.kirat.solutions.util;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ReadJsonUtil {

	public static boolean CheckBinderWithSameName(String url,String bookName) {
		JSONParser parser = new JSONParser();
		boolean isSameBookName = false;
		try {
			
			JSONObject array = (JSONObject) parser.parse(new FileReader(url));
			JSONArray jsonArray = (JSONArray) array.get("BookList");
			List<String> bookNameList =  new ArrayList<String>();
			for (Object obj : jsonArray)
			{
				JSONObject book = (JSONObject) obj;
				if(book.containsKey(bookName)) {
					isSameBookName = true;
				}
				/*if(book.keySet().contains("Name")) {
						bookNameList.add((String) book.get("Name"));
					}
				}
	              
				if(bookNameList.contains(binderName)) {
					isSameBookName = true;
				}*/
			}	
		}catch(Exception e) {
			//System.out.println(e.getMessage());
		}
		return isSameBookName;

	}
}
