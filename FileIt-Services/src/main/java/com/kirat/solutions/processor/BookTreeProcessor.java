package com.kirat.solutions.processor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.XML;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.kirat.solutions.util.FileInfoPropertyReader;

public class BookTreeProcessor {

	public JSONObject processBookXmltoDoc(String bookName) throws FileNotFoundException, IOException, ParseException {
		String line = "", str = "";
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String filePath = FileInfoPropertyReader.getInstance().getString("masterjson.file.path");
		String requiredXmlPath = "";
		JSONParser parser = new JSONParser();
		FileReader oFileReader = new FileReader(filePath);
		JSONObject array = (JSONObject) parser.parse(oFileReader);
		oFileReader.close();
		JSONArray jsonArray = (JSONArray) array.get("BookList");
		for (Object obj : jsonArray) {
			JSONObject book = (JSONObject) obj;
			if (book.containsKey(bookName)) {
				JSONObject jsonObject = (JSONObject) book.get(bookName);
				requiredXmlPath = (String) jsonObject.get("Path");
			}
		}
		BufferedReader br = new BufferedReader(new FileReader(requiredXmlPath));
		while ((line = br.readLine()) != null) {
			str += line;
		}
		br.close();
		org.json.JSONObject jsondata = XML.toJSONObject(str);
		JSONObject json = (JSONObject) parser.parse(jsondata.toString());
		return json;
	}

}
