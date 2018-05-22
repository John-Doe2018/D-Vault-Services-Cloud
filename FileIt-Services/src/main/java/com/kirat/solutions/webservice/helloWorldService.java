package com.kirat.solutions.webservice;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.kirat.solutions.util.FileInfoPropertyReader;

public class helloWorldService {

	@SuppressWarnings("unchecked")
	@GET
	@Path("getMasterJson")
	@Produces("application/json")
	public String getMasterJson() throws FileNotFoundException, IOException, ParseException {
		try {
			String filePath = FileInfoPropertyReader.getInstance().getString("masterjson.file.path");
			JSONParser parser = new JSONParser();
			FileReader oFileReader = new FileReader(filePath);
			Object object = parser.parse(oFileReader);
			oFileReader.close();
			JSONObject jsonObject = (JSONObject) object;
			return jsonObject.toJSONString();
		} catch (Exception e) {
			JSONObject oJsonObject = new JSONObject();
			oJsonObject.put("Error", "No Book Present");
			return oJsonObject.toJSONString();
		}

	}

}
