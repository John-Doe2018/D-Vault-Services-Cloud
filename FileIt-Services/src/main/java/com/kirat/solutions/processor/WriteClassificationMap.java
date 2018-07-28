package com.kirat.solutions.processor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.kirat.solutions.Constants.BinderConstants;
import com.kirat.solutions.domain.BookClassification;
import com.kirat.solutions.domain.FileItContext;
import com.kirat.solutions.util.CloudPropertiesReader;
import com.kirat.solutions.util.CloudStorageConfig;

public class WriteClassificationMap {

	public static void writeClassificationMap(Map<String, List<String>> classifiedBook) throws Exception {
		String classification = null;
		List<String> bookList = new ArrayList<String>();
		BookClassification bookClassification = new BookClassification();
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		for (Map.Entry<String, List<String>> entry : classifiedBook.entrySet()) {
			classification = entry.getKey();
			bookList = classifiedBook.get(classification);
			bookClassification = checkifClassPresent(classification, bookList);
			if (bookClassification.isClassification()) {
				InputStream is = new ByteArrayInputStream(bookClassification.getJsonArray().toJSONString().getBytes());
				oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
						"ClassificationMap.JSON", is, "application/json");
				FileItContext forBookClassifcation = new FileItContext();
				forBookClassifcation.remove(BinderConstants.CLASSIFIED_BOOK_NAMES);
				forBookClassifcation.add(BinderConstants.CLASSIFIED_BOOK_NAMES, bookClassification.getJsonArray());
				is.close();
			} else {
			}
		}
	}

	public static BookClassification checkifClassPresent(String classificationName, List<String> bookList)
			throws Exception {
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		InputStream oInputStream;
		BookClassification bookClassification = new BookClassification();
		try {
			JSONParser parser = new JSONParser();
			oInputStream = oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"ClassificationMap.JSON");
			if (null != oInputStream) {
				bookClassification.setClassification(true);
				JSONObject array = (JSONObject) parser.parse(new InputStreamReader(oInputStream));
				JSONArray jsonArray = (JSONArray) array.get(classificationName);
				// jsonArray.writeJSONString(out);
				// System.out.println(jsonArray);
				if (null != jsonArray) {
					for (String bookName : bookList) {
						// Check for same book name
						/*
						 * if(jsonArray.contains(bookName)) { }
						 */
						jsonArray.add(bookName);
					}
					array.put(classificationName, jsonArray);
				} else {
					array.put(classificationName, bookList);
				}
				bookClassification.setJsonArray(array);
			} else {
				bookClassification.setClassification(false);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return bookClassification;
	}
}
