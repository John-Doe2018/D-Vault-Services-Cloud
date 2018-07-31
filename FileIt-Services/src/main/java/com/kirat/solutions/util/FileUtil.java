package com.kirat.solutions.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.kirat.solutions.Constants.BinderConstants;

public class FileUtil {

	public static String createDynamicFilePath(String name) {
		String extension = BinderConstants.EXTENSION;
		String filePath = FileInfoPropertyReader.getInstance().getString("xml.file.path");
		extension = name.concat(extension);
		filePath = filePath + extension;
		return filePath;
	}

	// Path manipulation
	public static String correctFilePath(String filePath) {
		String modifiedfilePath = java.util.regex.Pattern.compile("\\\\").matcher(filePath).replaceAll("\\\\\\\\");
		return modifiedfilePath;
	}

	public static void checkTestJson() throws Exception {
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		try {
			oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"), "test.JSON");
		} catch (FileItException e) {
			JSONArray jsonArray = new JSONArray();
			JSONObject parentObj = new JSONObject();
			parentObj.put("BookList", jsonArray);
			InputStream is = new ByteArrayInputStream(parentObj.toJSONString().getBytes());
			oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"), "test.JSON",
					is, "application/json");
		}

	}

	public static boolean checkBookClassificationJson() throws Exception {
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		try {
			oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"ClassificationMap.JSON");
			return true;
		} catch (Exception e) {
			JSONArray jsonArray = new JSONArray();
			JSONObject parentObj = new JSONObject();
			parentObj.put("BlankArray", jsonArray);
			InputStream is = new ByteArrayInputStream(parentObj.toJSONString().getBytes());
			oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"ClassificationMap.JSON", is, "application/json");
			return false;
		}

	}

	public static JSONArray checkBookList() throws FileItException {
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		InputStream oInputStream;
		JSONArray jsonArray = new JSONArray();
		try {
			oInputStream = oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"BookList.JSON");
			JSONObject array = null;
			JSONParser parser = new JSONParser();
			array = (JSONObject) parser.parse(new InputStreamReader(oInputStream));
			jsonArray = (JSONArray) array.get("Books");
		} catch (Exception e) {
			JSONObject parentObj = new JSONObject();
			parentObj.put("Books", jsonArray);
			InputStream is = new ByteArrayInputStream(parentObj.toJSONString().getBytes());
			oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"BookList.JSON", is, "application/json");
		}
		return jsonArray;

	}
}