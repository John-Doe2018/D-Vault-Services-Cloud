package com.tranfode.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.tranfode.Constants.ErrorCodeConstants;
import com.tranfode.util.CloudPropertiesReader;
import com.tranfode.util.CloudStorageConfig;
import com.tranfode.util.ErrorMessageReader;
import com.tranfode.util.FileItException;

public class LookupBookProcessor {

	public static JSONObject lookupBookbyName(String bookName) throws Exception {
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		InputStream oInputStream = oCloudStorageConfig
				.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"), "BookList.JSON");
		JSONParser parser = new JSONParser();
		String book = null;
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
		JSONArray jsonArray = (JSONArray) array.get("Books");
		for (Object obj : jsonArray) {
			book = String.valueOf(obj);
			if (jsonArray.contains(obj)) {
				bookNameFound = true;
				break;
			}
		}
		if (!bookNameFound) {
			throw new FileItException(ErrorCodeConstants.ERR_CODE_0003,
					ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0003));
		}
		JSONObject object = new JSONObject();
		object.put("BookName", book);
		return object;
	}

}