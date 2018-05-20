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
import com.kirat.solutions.util.FileItException;

public class DeleteBookProcessor {

	@SuppressWarnings("unchecked")
	public JSONObject deleteBookProcessor(String deleteBookRequest) throws FileItException {
		JSONObject parentObj = new JSONObject();
		JSONObject deleteMsg = new JSONObject();
		String filePath = FileInfoPropertyReader.getInstance().getString("masterjson.file.path");
		JSONParser parser = new JSONParser();
		JSONObject array;
		try {
			FileReader oFileReader = new FileReader(filePath);
			array = (JSONObject) parser.parse(oFileReader);
			oFileReader.close();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			throw new FileItException(e.getMessage());
		}
		JSONArray jsonArray = (JSONArray) array.get("BookList");
		for (Iterator<Object> iterator = jsonArray.iterator(); iterator.hasNext();) {
			JSONObject book = (JSONObject) iterator.next();
			if (book.containsKey(deleteBookRequest)) {
				iterator.remove();
				deleteMsg.put("Success", "Deleted Successfully");
				break;
			}
		}
		parentObj.put("BookList", jsonArray);
		FileWriter jsonFile = null;
		try {
			jsonFile = new FileWriter(filePath);
			jsonFile.write(parentObj.toJSONString());
			jsonFile.flush();
			jsonFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			throw new FileItException(e.getMessage());
		}
		return deleteMsg;
	}

}