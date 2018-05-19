package com.kirat.solutions.processor;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.kirat.solutions.util.FileInfoPropertyReader;

public class DeleteBookProcessor {

	@SuppressWarnings("unchecked")
	public String deleteBookProcessor(String deleteBookRequest) throws IOException, ParseException {
		JSONObject parentObj = new JSONObject();
		String deleteMsg = null;
		String filePath = FileInfoPropertyReader.getInstance().getString("masterjson.file.path");
		JSONParser parser = new JSONParser();
		FileReader oFileReader = new FileReader(filePath);
		JSONObject array = (JSONObject) parser.parse(oFileReader);
		oFileReader.close();
		JSONArray jsonArray = (JSONArray) array.get("BookList");
		for (Iterator<Object> iterator = jsonArray.iterator(); iterator.hasNext();) {
			JSONObject book = (JSONObject) iterator.next();
			if (book.containsKey(deleteBookRequest)) {
				iterator.remove();
				deleteMsg = "Deleted Successfully";
			}
		}
		parentObj.put("BookList", jsonArray);
		FileWriter jsonFile = new FileWriter(filePath);
		jsonFile.write(parentObj.toJSONString());
		jsonFile.flush();
		jsonFile.close();
		return deleteMsg;
	}

}
