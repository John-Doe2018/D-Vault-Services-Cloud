package com.kirat.solutions.processor;

import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.kirat.solutions.util.FileInfoPropertyReader;

public class LookupBookProcessor {

	public static JSONObject lookupBookbyName(String bookName) {
		String filePath = FileInfoPropertyReader.getInstance().getString("masterjson.file.path");
		JSONParser parser = new JSONParser();
		JSONObject book = null;
		boolean bookNameFound = false;
		try {
			FileReader oFileReader = new FileReader(filePath);
			JSONObject array = (JSONObject) parser.parse(oFileReader);
			oFileReader.close();
			JSONArray jsonArray = (JSONArray) array.get("BookList");
			for (Object obj : jsonArray) {
				book = (JSONObject) obj;
				if (book.containsKey(bookName)) {
					bookNameFound = true;
					break;
				}

			}
		} catch (Exception e) {
			// System.out.println(e.getMessage());
		}

		return book;

	}

}
