package com.kirat.solutions.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.transform.TransformerException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.kirat.solutions.domain.BinderList;
import com.kirat.solutions.domain.CreateBinderRequest;
import com.kirat.solutions.domain.CreateBinderResponse;
import com.kirat.solutions.domain.SearchBookResponse;
import com.kirat.solutions.domain.UpdateBookRequest;
import com.kirat.solutions.processor.BookTreeProcessor;
import com.kirat.solutions.processor.DeleteBookProcessor;
import com.kirat.solutions.processor.LookupBookProcessor;
import com.kirat.solutions.processor.TransformationProcessor;
import com.kirat.solutions.processor.UpdateMasterJson;

public class BinderService {

	@POST
	@Path("create")
	public CreateBinderResponse createBinder(CreateBinderRequest createBinderRequest) throws Exception {
		CreateBinderResponse createBinderResponse = new CreateBinderResponse();
		String htmlContent = createBinderRequest.getHtmlContent();
		BinderList listOfBinderObj;
		TransformationProcessor transformationProcessor = new TransformationProcessor();
		listOfBinderObj = transformationProcessor.processHtmlToBinderXml(htmlContent);

		// append in MasterJson
		UpdateMasterJson updateMasterJson = new UpdateMasterJson();
		updateMasterJson.prepareMasterJson(listOfBinderObj);
		createBinderResponse.setSuccessMsg("Binder Successfully Created.");
		return createBinderResponse;
	}

	@POST
	@Path("update")
	public String updateBinder(UpdateBookRequest updateBookRequest) throws TransformerException {
		// append in MasterJson
		String s = "Serice was run Successfully";
		return s;
	}

	@POST
	@Path("delete")
	public String deleteBinder(String bookName) throws TransformerException, IOException, ParseException {
		String succssMsg;
		DeleteBookProcessor deleteBookProcessor = new DeleteBookProcessor();
		succssMsg = deleteBookProcessor.deleteBookProcessor(bookName);
		// append in MasterJson
		return succssMsg;
	}

	@POST
	@Path("getBookTreeDetail")
	@Produces("application/json")
	public JSONObject BookTreeDetail(String bookName) throws FileNotFoundException, IOException, ParseException {
		BookTreeProcessor bookTreeProcessor = new BookTreeProcessor();
		JSONObject document = bookTreeProcessor.processBookXmltoDoc(bookName);
		return document;
	}

	@POST
	@Path("getPDF")
	@Produces("application/pdf")
	public Response getPDF(String pathName) throws FileNotFoundException, IOException, ParseException {
		File file = new File(pathName);
		ResponseBuilder response = Response.ok((Object) file);
		response.header("Content-Disposition", "attachment; filename=test.pdf");
		return response.build();
	}

	@POST
	@Path("searchBook")
	public SearchBookResponse searchBook(String bookname) {
		SearchBookResponse bookResponse = new SearchBookResponse();
		JSONObject jsonObject = null;
		LookupBookProcessor lookupBookProcessor = new LookupBookProcessor();
		jsonObject = lookupBookProcessor.lookupBookbyName(bookname);
		bookResponse.setJsonObject(jsonObject);
		return bookResponse;
	}
}
