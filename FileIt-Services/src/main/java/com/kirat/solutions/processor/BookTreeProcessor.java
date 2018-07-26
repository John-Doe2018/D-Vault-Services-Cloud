package com.kirat.solutions.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.kirat.solutions.util.CloudPropertiesReader;
import com.kirat.solutions.util.CloudStorageConfig;
import com.kirat.solutions.util.FileItException;

public class BookTreeProcessor {

	public JSONObject processBookXmltoDoc(String bookName) throws Exception {

		String line = "", str = "";
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		JSONObject json;
		try {
			documentBuilder = documentFactory.newDocumentBuilder();
			CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
			String requiredXmlPath = "files/" + bookName + ".xml";
			JSONParser parser = new JSONParser();
			BufferedReader br = null;
			InputStream xmlInputStream = oCloudStorageConfig
					.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"), requiredXmlPath);
			br = new BufferedReader(new InputStreamReader(xmlInputStream));

			while ((line = br.readLine()) != null) {
				str += line;
			}
			br.close();
			org.json.JSONObject jsondata = XML.toJSONObject(str);

			json = (JSONObject) parser.parse(jsondata.toString());
		} catch (ParserConfigurationException | ParseException | IOException e) {
			// TODO Auto-generated catch block
			throw new FileItException(e.getMessage());
		}
		return json;
	}

}