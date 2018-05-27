package com.kirat.solutions.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
			oCloudStorageConfig.getFile("1dvaultdata", "test.JSON");
		} catch (Exception e) {
			JSONArray jsonArray = new JSONArray();
			JSONObject parentObj = new JSONObject();
			parentObj.put("BookList", jsonArray);
			InputStream is = new ByteArrayInputStream(parentObj.toJSONString().getBytes());
			oCloudStorageConfig.uploadFile("1dvaultdata", "test.JSON", is, "application/json");
		}
		
	}
}