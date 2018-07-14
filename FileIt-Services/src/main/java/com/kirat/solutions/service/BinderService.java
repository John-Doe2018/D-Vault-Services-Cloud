package com.kirat.solutions.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kirat.solutions.domain.AddFileRequest;
import com.kirat.solutions.domain.BinderList;
import com.kirat.solutions.domain.BookRequests;
import com.kirat.solutions.domain.CreateBinderRequest;
import com.kirat.solutions.domain.CreateBinderResponse;
import com.kirat.solutions.domain.DeleteBookRequest;
import com.kirat.solutions.domain.DeleteFileRequest;
import com.kirat.solutions.domain.DownloadFileRequest;
import com.kirat.solutions.domain.GetImageRequest;
import com.kirat.solutions.domain.SearchBookRequest;
import com.kirat.solutions.domain.SearchBookResponse;
import com.kirat.solutions.processor.AddFileProcessor;
import com.kirat.solutions.processor.BookTreeProcessor;
import com.kirat.solutions.processor.ContentProcessor;
import com.kirat.solutions.processor.DeleteBookProcessor;
import com.kirat.solutions.processor.LookupBookProcessor;
import com.kirat.solutions.processor.TransformationProcessor;
import com.kirat.solutions.processor.UpdateMasterJson;
import com.kirat.solutions.util.CloudPropertiesReader;
import com.kirat.solutions.util.CloudStorageConfig;
import com.kirat.solutions.util.FileItException;
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
	@Path("getImage")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<String> getFile(GetImageRequest oGetImageRequest) throws Exception {

		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		List<String> oImages = new ArrayList<>();
		List<String> oList = oCloudStorageConfig
				.listBucket(CloudPropertiesReader.getInstance().getString("bucket.name"));
		String wordToSearchFor = oGetImageRequest.getBookName() + '/' + "Images";
		for (String word : oList) {
			if (word.contains(wordToSearchFor)) {
				oImages.add(oCloudStorageConfig
						.getSignedString(CloudPropertiesReader.getInstance().getString("bucket.name"), word));
			}

		}
		return oImages;
	}

	@POST
	@Path("download")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONObject downloadFile(DownloadFileRequest oDownloadFileRequest) throws Exception {
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		ContentProcessor oContentProcessor = new ContentProcessor();
		File oFile = new File(this.getClass().getClassLoader().getResource("/").getPath() + "Files.zip");
		oContentProcessor.getZipFile(oDownloadFileRequest.getBookName(), oFile);
		InputStream fis = new FileInputStream(oFile);
		oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
				"/" + oDownloadFileRequest.getBookName() + "/Files.zip", fis, "application/zip");
		String url = oCloudStorageConfig.getSignedString(CloudPropertiesReader.getInstance().getString("bucket.name"),
				"/" + oDownloadFileRequest.getBookName() + "/Files.zip");
		JSONObject object = new JSONObject();
		object.put("URL", url);
		return object;
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("imageConvert")
	public Response submit(MultipartBody multipart) throws Exception {
		JSONObject oJsonObject;
		try {
			Attachment file = multipart.getAttachment("file");
			Attachment file1 = multipart.getAttachment("bookName");
			Attachment file2 = multipart.getAttachment("path");
			Attachment file3 = multipart.getAttachment("type");
			Attachment file4 = multipart.getAttachment("filename");
			String bookName = file1.getObject(String.class);
			String path = file2.getObject(String.class);
			String type = file3.getObject(String.class);
			String fileName = file4.getObject(String.class);
			InputStream fileStream = file.getObject(InputStream.class);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			org.apache.commons.io.IOUtils.copy(fileStream, baos);
			byte[] bytes = baos.toByteArray();
			ContentProcessor contentProcessor = ContentProcessor.getInstance();
			// Read it from ByteArrayOutput Stream
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			contentProcessor.processContent(bookName, bais, path, type, fileName);
			ByteArrayInputStream bais1 = new ByteArrayInputStream(bytes);
			oJsonObject = contentProcessor.processContentImage(bookName, bais1, path, type, fileName);
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

	@POST
	@Path("advancedSearch")
	public JSONArray advancedSearch() throws Exception {
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		InputStream oInputStream = oCloudStorageConfig
				.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"), "test.JSON");
		JSONParser parser = new JSONParser();
		JSONObject array = null;
		array = (JSONObject) parser.parse(new InputStreamReader(oInputStream));
		JSONArray jsonArray = (JSONArray) array.get("BookList");
		JSONArray oArray = new JSONArray();
		for (Object obj : jsonArray) {
			JSONObject book = (JSONObject) obj;
			Set<String> keys = book.keySet();
			for (String s : keys) {
				oArray.add(s);
			}
		}
		return oArray;

	}

	@POST
	@Path("addFile")
	public JSONObject addFiles(AddFileRequest oAddFileRequest) throws Exception {
		AddFileProcessor oAddFileProcessor = new AddFileProcessor();
		oAddFileProcessor.updateXML(oAddFileRequest.getBookName(), oAddFileRequest.getoBookRequests());
		JSONObject oJsonObject = new JSONObject();
		oJsonObject.put("Success", "File Added Successfully");
		return oJsonObject;
	}

	@POST
	@Path("deleteFile")
	public JSONObject deleteFile(DeleteFileRequest oDeleteFileRequest) throws FileItException {
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		InputStream oInputStream;
		InputStream oInputStream1;
		JSONObject oJsonObject = new JSONObject();
		Element topicElement = null;
		try {
			oInputStream = oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"files/" + oDeleteFileRequest.getFileName().replaceFirst("[.][^.]+$", "") + ".JSON");
			JSONParser parser = new JSONParser();
			JSONArray array = (JSONArray) parser.parse(new InputStreamReader(oInputStream));
			oInputStream.close();
			for (int i = 0; i < array.size(); i++) {
				oCloudStorageConfig.deleteFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
						array.get(i).toString());
			}
			oCloudStorageConfig.deleteFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"files/" + oDeleteFileRequest.getFileName().replaceFirst("[.][^.]+$", "") + ".JSON");
			oInputStream1 = oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"files/" + oDeleteFileRequest.getBookName() + ".xml");
			DocumentBuilderFactory docbf = DocumentBuilderFactory.newInstance();
			docbf.setNamespaceAware(true);
			DocumentBuilder docbuilder = docbf.newDocumentBuilder();
			Document document = docbuilder.parse(oInputStream1);
			NodeList fileList = document.getElementsByTagName("topic");
			for (int i = 0; i < fileList.getLength(); i++) {
				Node element = fileList.item(i);
				if (element.getNodeType() == Node.ELEMENT_NODE) {
					topicElement = (Element) element;
					if (oDeleteFileRequest.getFileName().equals(topicElement.getAttribute("name"))) {
						element.getParentNode().removeChild(topicElement);
						break;
					}
				}
			}
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Result res = new StreamResult(baos);
			transformer.transform(domSource, res);
			InputStream isFromFirstData = new ByteArrayInputStream(baos.toByteArray());
			oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"files/" + oDeleteFileRequest.getBookName() + ".xml", isFromFirstData, "application/xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		oJsonObject.put("Success", "Deleted Successfully");
		return oJsonObject;
	}
}
