package com.tranfode.processor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.tranfode.domain.AddClassificationResponse;
import com.tranfode.util.CloudPropertiesReader;
import com.tranfode.util.CloudStorageConfig;
import com.tranfode.util.FileItException;

public class AddClassificationProcessor {

	private static AddClassificationProcessor addClassProcessor;

	/**
	 * Create a static method to get instance.
	 */
	public static AddClassificationProcessor getInstance() {
		if (addClassProcessor == null) {
			addClassProcessor = new AddClassificationProcessor();
		}
		return addClassProcessor;
	}

	@SuppressWarnings("unchecked")
	public AddClassificationResponse addClassification(String classificationName,
			AddClassificationResponse addClassificationResponse) throws FileItException {
		List<String> classifcations = new ArrayList<String>();
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		JSONParser parser = new JSONParser();
		InputStream inputStream;
		try {
			classifcations.add(classificationName);
			inputStream = oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"ClassificationList.JSON");

			JSONObject array = (JSONObject) parser.parse(new InputStreamReader(inputStream));
			JSONArray jsonArray = (JSONArray) array.get("classificationList");
			if (null != jsonArray) {
				jsonArray.add(classificationName);
			} else {
				array.put("classificationList", classifcations);
			}
		} catch (Exception e) {
			e.printStackTrace();

		}

		return null;

	}

}
