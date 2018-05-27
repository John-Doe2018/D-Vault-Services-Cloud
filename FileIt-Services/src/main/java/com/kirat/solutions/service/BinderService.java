package com.kirat.solutions.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.kirat.solutions.domain.BinderList;
import com.kirat.solutions.domain.CreateBinderRequest;
import com.kirat.solutions.domain.CreateBinderResponse;
import com.kirat.solutions.domain.DeleteBookRequest;
import com.kirat.solutions.domain.SearchBookRequest;
import com.kirat.solutions.domain.SearchBookResponse;
import com.kirat.solutions.processor.BookTreeProcessor;
import com.kirat.solutions.processor.ContentProcessor;
import com.kirat.solutions.processor.DeleteBookProcessor;
import com.kirat.solutions.processor.LookupBookProcessor;
import com.kirat.solutions.processor.TransformationProcessor;
import com.kirat.solutions.processor.UpdateMasterJson;
import com.kirat.solutions.util.FileUtil;

public class BinderService {

	@POST
	@Path("create")
	public CreateBinderResponse createBinder(CreateBinderRequest createBinderRequest) throws Exception {
		CreateBinderResponse createBinderResponse = new CreateBinderResponse();
		FileUtil.checkTestJson();
		String htmlContent = createBinderRequest.getHtmlContent();
		TransformationProcessor transformationProcessor = new TransformationProcessor();
		BinderList listOfBinderObj = transformationProcessor.createBinderList(htmlContent);
		transformationProcessor.processHtmlToBinderXml(listOfBinderObj);
		// append in MasterJson
		UpdateMasterJson updateMasterJson = new UpdateMasterJson();
		String bookName = updateMasterJson.prepareMasterJson(listOfBinderObj);
		// Prepare the Content Structure of the book with image
		createBinderResponse.setSuccessMsg("Binder Successfully Created.");
		return createBinderResponse;
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("imageConvert")
	public Response submit(MultipartBody multipart) {
		JSONObject oJsonObject;
		try {
			Attachment file = multipart.getAttachment("file");
			Attachment file1 = multipart.getAttachment("bookName");
			Attachment file2 = multipart.getAttachment("path");
			Attachment file3 = multipart.getAttachment("type");
			String bookName = file1.getObject(String.class);
			String path = file2.getObject(String.class);
			String type = file3.getObject(String.class);
			InputStream fileStream = file.getObject(InputStream.class);
			ContentProcessor contentProcessor = ContentProcessor.getInstance();
			oJsonObject = contentProcessor.processContentImage(bookName, fileStream, path, type);
		} catch (Exception ex) {
			return Response.status(600).entity(ex.getMessage()).build();
		}
		return Response.status(200).entity(oJsonObject).build();
	}

	@POST
	@Path("delete")
	@Produces("application/json")
	public JSONObject deleteBinder(DeleteBookRequest deleteBookRequest) throws Exception {
		String bookName = deleteBookRequest.getBookName();
		DeleteBookProcessor deleteBookProcessor = new DeleteBookProcessor();
		JSONObject succssMsg = deleteBookProcessor.deleteBookProcessor(bookName);
		return succssMsg;
	}

	@POST
	@Path("getBookTreeDetail")
	@Produces("application/json")
	public JSONObject BookTreeDetail(String bookName) throws Exception {
		BookTreeProcessor bookTreeProcessor = new BookTreeProcessor();
		JSONObject document = bookTreeProcessor.processBookXmltoDoc(bookName);
		return document;
	}

	@POST
	@Path("getPDF")
	@Produces("application/pdf")
	public Response getPDF(String pathName) throws FileNotFoundException, IOException, ParseException {
		pathName = FileUtil.correctFilePath(pathName);
		File file = new File(pathName.substring(1, pathName.length() - 1));
		ResponseBuilder response = Response.ok((Object) file);
		response.header("Content-Disposition", "attachment; filename=PrivacyByDesignVer1.0.pdf");
		return response.build();
	}

	@POST
	@Path("searchBook")
	public SearchBookResponse searchBook(SearchBookRequest searchBookRequest) throws Exception {
		SearchBookResponse bookResponse = new SearchBookResponse();
		String bookName = searchBookRequest.getBookName();
		JSONObject jsonObject = null;
		LookupBookProcessor lookupBookProcessor = new LookupBookProcessor();
		jsonObject = lookupBookProcessor.lookupBookbyName(bookName);
		bookResponse.setJsonObject(jsonObject);
		return bookResponse;
	}
}
