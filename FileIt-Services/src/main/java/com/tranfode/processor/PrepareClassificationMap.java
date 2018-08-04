package com.tranfode.processor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.tranfode.Constants.BinderConstants;
import com.tranfode.domain.FileItContext;
import com.tranfode.util.CloudPropertiesReader;
import com.tranfode.util.CloudStorageConfig;

public class PrepareClassificationMap {

	public static void createClassifiedMap(String masterBook) throws Exception {
		FileItContext fileItContext = new FileItContext();
		String classification = null;
		String bookNameKey = null;
		Map<String, List<String>> bookWithClassification = new HashMap<String, List<String>>();
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		InputStream fileInputStream = oCloudStorageConfig
				.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"), masterBook);
		// InputStream fileInputStream = new FileInputStream(jsonFile);
		JSONParser parser = new JSONParser();
		JSONObject array = null;
		array = (JSONObject) parser.parse(new InputStreamReader(fileInputStream));
		fileInputStream.close();
		JSONArray jsonArray = (JSONArray) array.get("BookList");
		if (jsonArray.size() > 0) {
			for (Object obj : jsonArray) {
				JSONObject book = (JSONObject) obj;
				Set<String> keys = book.keySet();
				Object[] keyString = keys.toArray();
				for (Object objKey : keyString) {
					bookNameKey = (String) objKey;
				}
				JSONObject jsonObject = (JSONObject) book.get(bookNameKey);
				classification = (String) jsonObject.get("Classification");
				if (bookWithClassification.keySet().contains(classification)) {
					bookWithClassification.get(classification).add(bookNameKey);
				} else {
					List<String> bookValueList = new ArrayList<String>();
					bookValueList.add(bookNameKey);
					bookWithClassification.put(classification, bookValueList);

				}
			}

			// Write this in ClassificationMap.json file
			WriteClassificationMap.writeClassificationMap(bookWithClassification);
			JSONArray jsonArray1 = new JSONArray();
			JSONObject parentObj = new JSONObject();
			parentObj.put("BookList", jsonArray1);
			InputStream is = new ByteArrayInputStream(parentObj.toJSONString().getBytes());
			oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"), "test.JSON",
					is, "application/json");
		} else if (jsonArray.size() == 0 && FileItContext.get(BinderConstants.CLASSIFIED_BOOK_NAMES) == null) {
			InputStream oInputStream = oCloudStorageConfig
					.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"), "ClassificationMap.JSON");
			JSONObject bookArray = (JSONObject) parser.parse(new InputStreamReader(oInputStream));
			fileItContext.add(BinderConstants.CLASSIFIED_BOOK_NAMES, bookArray);
		}

	}
}