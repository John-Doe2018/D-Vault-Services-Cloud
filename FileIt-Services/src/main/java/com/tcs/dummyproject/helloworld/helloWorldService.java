package com.tcs.dummyproject.helloworld;

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
	@GET
	@Path("sayHello")
	public String getConfigurationParameterValue() {
		return "Hello World";
	}

	@GET
	@Path("getMasterJson")
	@Produces("application/json")
	public String getMasterJson() throws FileNotFoundException, IOException, ParseException {
		String filePath = FileInfoPropertyReader.getInstance().getString("masterjson.file.path");
		JSONParser parser = new JSONParser();
		FileReader oFileReader = new FileReader(filePath);
		Object object = parser.parse(oFileReader);
		oFileReader.close();
		JSONObject jsonObject = (JSONObject) object;
		return jsonObject.toJSONString();
	}

	public String readJson(String filePath) {
		/*
		 * byte[] jsonData = Files.;
		 * 
		 * //create ObjectMapper instance ObjectMapper objectMapper = new
		 * ObjectMapper();
		 * 
		 * //convert json string to object BinderList emp =
		 * objectMapper.readValue(jsonData, BinderList.class);
		 * 
		 * System.out.println("Employee Object\n"+emp);
		 * 
		 * //convert Object to json string BinderList emp1 = createEmployee();
		 * //configure Object mapper for pretty print
		 * objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		 * 
		 * //writing to console, can write to any output stream such as file
		 * StringWriter stringEmp = new StringWriter();
		 * objectMapper.writeValue(stringEmp, emp1);
		 * System.out.println("Employee JSON is\n"+stringEmp); }
		 */
		return filePath;

	}

}
