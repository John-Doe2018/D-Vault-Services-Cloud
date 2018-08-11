package com.tranfode.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.tranfode.domain.BookClassMap;
import com.tranfode.domain.BookMarkDetails;

public class BookMarkUtil {

	public static BookMarkUtil bookMark;

	public static BookMarkUtil getInstance() {
		if (bookMark == null) {
			bookMark = new BookMarkUtil();
		}
		return bookMark;
	}

	public BookMarkDetails saveUserBookMarkDetails(String loggedInUser, String bookName, String classificationName)
			throws FileItException {
		BookMarkDetails bookMarkDetails = new BookMarkDetails();
		if (loggedInUser != null && bookName != null && classificationName != null) {
			bookMarkDetails.setUserName(loggedInUser);
			bookMarkDetails.setBookName(bookName);
			bookMarkDetails.setClassification(classificationName);
		} else if (loggedInUser == null || bookName == null || classificationName == null) {
			throw new FileItException("Something went wrong.BookMark can not be added");
		}
		WriteBookMarkDetails(bookMarkDetails);
		return bookMarkDetails;
	}

	public void WriteBookMarkDetails(BookMarkDetails bookMarkDetails) throws FileItException {
		Map<String, List<BookClassMap>> bookMarkDetailMap = new HashMap<String, List<BookClassMap>>();
		// add classification wise books to the map
		List<BookClassMap> bookClassList = new ArrayList<BookClassMap>();
		BookClassMap bookClassMap = new BookClassMap();
		bookClassMap.setBookName(bookMarkDetails.getBookName());
		bookClassMap.setClassification(bookMarkDetails.getClassification());
		bookClassList.add(bookClassMap);
		bookMarkDetailMap.put(bookMarkDetails.getUserName(), bookClassList);
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		JSONParser parser = new JSONParser();
		InputStream inputStream;
		boolean isBookmark = true;
		boolean newClassBookMark = false;
		JSONArray bookMarkClass = new JSONArray();
		boolean newUser = false;
		try {
			inputStream = oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"BookMarkDetails.JSON");
			if (inputStream != null) {
				Gson gson = new Gson();
				String json = gson.toJson(bookMarkDetailMap);
				JSONObject bookArray = (JSONObject) parser.parse(new InputStreamReader(inputStream));
				JSONArray newBookClassListObj = (JSONArray) bookArray.get(bookMarkDetails.getUserName());
				// Check for already added bookName in the same classification
				if (newBookClassListObj != null) {
					for (int i = 0; i < newBookClassListObj.size(); i++) {
						JSONObject json_data = (JSONObject) newBookClassListObj.get(i);
						String bookname = (String) json_data.get("bookName");
						String classificationName = (String) json_data.get("classification");
						if (classificationName.equalsIgnoreCase(bookMarkDetails.getClassification())) {
							if (bookname.equalsIgnoreCase(bookMarkDetails.getBookName())) {
								throw new FileItException("Bookmark already exists.");
							} else {
								isBookmark = false;
							}
						} else {
							newClassBookMark = true;
						}
					}
				} else {
					newClassBookMark = true;
					newUser = true;
				}
				if (newClassBookMark || !isBookmark) {
					if (!newUser) {
						newBookClassListObj.add(bookClassMap);
						bookArray.put(bookMarkDetails.getUserName(), newBookClassListObj);
					} else {
						bookMarkClass.add(bookClassMap);
						bookArray.put(bookMarkDetails.getUserName(), bookMarkClass);
					}
					Gson gsonmap = new Gson();
					String jsonmap = gsonmap.toJson(bookArray);
					InputStream is = new ByteArrayInputStream(jsonmap.getBytes());
					oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
							"BookMarkDetails.JSON", is, "application/json");
				}
			}
		} catch (Exception ex) {
			JSONArray jsonArray1 = new JSONArray();
			JSONObject parentObj = new JSONObject();
			parentObj.put("BookMarkList", jsonArray1);
			Gson gson = new Gson();
			String json = gson.toJson(bookMarkDetailMap);
			InputStream is = new ByteArrayInputStream(json.getBytes());
			oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"BookMarkDetails.JSON", is, "application/json");
		}

	}

}
