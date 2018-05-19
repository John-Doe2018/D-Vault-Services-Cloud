package com.kirat.solutions.processor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.kirat.solutions.domain.BinderList;
import com.kirat.solutions.util.FileInfoPropertyReader;
import com.kirat.solutions.util.FileUtil;
import com.kirat.solutions.util.ReadJsonUtil;

public class UpdateMasterJson {

	@SuppressWarnings("unchecked")
	public void prepareMasterJson(BinderList bookObject) throws Exception {
		JSONParser parser = new JSONParser();
		JSONObject obj = new JSONObject();
		JSONObject superObj = new JSONObject();
		JSONObject parentObj = new JSONObject();

		boolean isSameName = false;

		//getting the master Json File path
		String filePath = FileInfoPropertyReader.getInstance().getString("masterjson.file.path");
		//Check any book with same name already present or not
		File tmpDir = new File(filePath);
		boolean isFile = tmpDir.exists();
		if(isFile) {
			isSameName = ReadJsonUtil.CheckBinderWithSameName(filePath,bookObject.getName());
			if(isSameName) {
				throw new Exception() ;
			}else {
				JSONObject array = (JSONObject) parser.parse(new FileReader(filePath));
				JSONArray jsonArray = (JSONArray) array.get("BookList");
				//Add the new object to existing
				obj.put("Name", bookObject.getName());
				obj.put("Classification",bookObject.getClassification());
				String xmlFilePath = FileUtil.createDynamicFilePath(bookObject.getName());
				obj.put("Path", xmlFilePath);
				superObj.put(bookObject.getName(), obj);
				jsonArray.add(superObj);
				parentObj.put("BookList", jsonArray);
				/*array.put(jsonArray);*/
				FileWriter jsonFile = new FileWriter(filePath);
				jsonFile.write(parentObj.toJSONString());
				jsonFile.flush();
				jsonFile.close();
			}
		}
		else if(!isSameName) {
				obj.put("Name", bookObject.getName());
				obj.put("Classification",bookObject.getClassification());
				obj.put("Path", "D:\\files\\xmlFile.xml");
				superObj.put(bookObject.getName(), obj);
				JSONArray bookList = new JSONArray();
				bookList.add(superObj);
				parentObj.put("BookList", bookList);
				try {
					FileWriter jsonFile = new FileWriter(filePath);
					jsonFile.write(parentObj.toJSONString());
					jsonFile.flush();
					jsonFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				//throw Exception e
			}
		}

	}
