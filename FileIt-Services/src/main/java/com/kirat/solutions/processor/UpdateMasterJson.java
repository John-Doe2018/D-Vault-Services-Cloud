package com.kirat.solutions.processor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.kirat.solutions.Constants.ErrorCodeConstants;
import com.kirat.solutions.domain.BinderList;
import com.kirat.solutions.util.CloudPropertiesReader;
import com.kirat.solutions.util.CloudStorageConfig;
import com.kirat.solutions.util.ErrorMessageReader;
import com.kirat.solutions.util.FileItException;
import com.kirat.solutions.util.FileUtil;
import com.kirat.solutions.util.ReadJsonUtil;

public class UpdateMasterJson {
	@SuppressWarnings("unchecked")
	public String prepareMasterJson(BinderList bookObject) throws Exception {
		JSONParser parser = new JSONParser();
		JSONObject obj = new JSONObject();
		JSONObject superObj = new JSONObject();
		JSONObject parentObj = new JSONObject();

		boolean isSameName = false;

		// getting the master Json File path
		// String filePath =
		// FileInfoPropertyReader.getInstance().getString("masterjson.file.path");
		// Check any book with same name already present or not
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		InputStream oInputStream = oCloudStorageConfig
				.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"), "test.JSON");
		String xmlFilePath = FileUtil.createDynamicFilePath(bookObject.getName());
		JSONObject array = (JSONObject) parser.parse(new InputStreamReader(oInputStream));
		JSONArray jsonArray = (JSONArray) array.get("BookList");
		if (oInputStream != null) {
			isSameName = ReadJsonUtil.CheckBinderWithSameName(jsonArray, bookObject.getName());
			if (isSameName) {
				throw new FileItException(ErrorCodeConstants.ERR_CODE_0002,
						ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0002));
			} else {
				try {
					// Add the new object to existing
					obj.put("Name", bookObject.getName());
					obj.put("Classification", bookObject.getClassification());
					obj.put("Path", xmlFilePath);
					superObj.put(bookObject.getName(), obj);
					jsonArray.add(superObj);
					parentObj.put("BookList", jsonArray);
					InputStream is = new ByteArrayInputStream(parentObj.toJSONString().getBytes());
					oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
							"test.JSON", is, "application/json");
					is.close();
				} catch (IOException e) {
					throw new FileItException(e.getMessage());
				} catch (ParseException e) {
					throw new FileItException(e.getMessage());
				}
			}
		} else if (!isSameName) {
			obj.put("Name", bookObject.getName());
			obj.put("Classification", bookObject.getClassification());
			obj.put("Path", xmlFilePath);
			superObj.put(bookObject.getName(), obj);
			JSONArray bookList = new JSONArray();
			bookList.add(superObj);
			parentObj.put("BookList", bookList);
			try {
				InputStream is = new ByteArrayInputStream(parentObj.toJSONString().getBytes());
				oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
						"test.JSON", is, "application/json");
				is.close();
			} catch (IOException e) {
				throw new FileItException(e.getMessage());
			}
		} else {
			throw new FileItException(ErrorCodeConstants.ERR_CODE_0002,
					ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0002));
		}
		return bookObject.getName();
	}
}