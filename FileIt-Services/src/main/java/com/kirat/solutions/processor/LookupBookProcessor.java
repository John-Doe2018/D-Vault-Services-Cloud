package com.kirat.solutions.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.kirat.solutions.Constants.ErrorCodeConstants;
import com.kirat.solutions.util.CloudStorageConfig;
import com.kirat.solutions.util.ErrorMessageReader;
import com.kirat.solutions.util.FileItException;

public class LookupBookProcessor {

	public static JSONObject lookupBookbyName(String bookName) throws Exception {
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		InputStream oInputStream = oCloudStorageConfig.getFile("1dvaultdata", "test.JSON");
		JSONParser parser = new JSONParser();
		JSONObject book = null;
		boolean bookNameFound = false;
		JSONObject array = null;
		try {
			array = (JSONObject) parser.parse(new InputStreamReader(oInputStream));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new FileItException(e.getMessage());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new FileItException(e.getMessage());
		}
		JSONArray jsonArray = (JSONArray) array.get("BookList");
		for (Object obj : jsonArray) {
			book = (JSONObject) obj;
			if (book.containsKey(bookName)) {
				bookNameFound = true;
				break;
			}

		}
		if (!bookNameFound) {
			throw new FileItException(ErrorCodeConstants.ERR_CODE_0003,
					ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0003));
		}
		return book;
	}

}