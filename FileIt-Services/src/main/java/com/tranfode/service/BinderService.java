package com.tranfode.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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

import org.apache.commons.io.FilenameUtils;
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

import com.tranfode.Constants.BinderConstants;
import com.tranfode.domain.AddClassificationRequest;
import com.tranfode.domain.AddClassificationResponse;
import com.tranfode.domain.AddFileRequest;
import com.tranfode.domain.BinderList;
import com.tranfode.domain.BookMarkDetails;
import com.tranfode.domain.BookMarkRequest;
import com.tranfode.domain.BookMarkResponse;
import com.tranfode.domain.CreateBinderRequest;
import com.tranfode.domain.CreateBinderResponse;
import com.tranfode.domain.DeleteBookRequest;
import com.tranfode.domain.DeleteFileRequest;
import com.tranfode.domain.DownloadFileRequest;
import com.tranfode.domain.FileItContext;
import com.tranfode.domain.GetImageRequest;
import com.tranfode.domain.SearchBookRequest;
import com.tranfode.domain.SearchBookResponse;
import com.tranfode.processor.AddClassificationProcessor;
import com.tranfode.processor.AddFileProcessor;
import com.tranfode.processor.BookTreeProcessor;
import com.tranfode.processor.ContentProcessor;
import com.tranfode.processor.DeleteBookProcessor;
import com.tranfode.processor.LookupBookProcessor;
import com.tranfode.processor.TransformationProcessor;
import com.tranfode.processor.UpdateMasterJson;
import com.tranfode.util.BookMarkUtil;
import com.tranfode.util.CloudPropertiesReader;
import com.tranfode.util.CloudStorageConfig;
import com.tranfode.util.FileItException;
import com.tranfode.util.FileUtil;

public class BinderService {

	@SuppressWarnings({ "unchecked", "static-access" })
	@POST
	@Path("create")
	public CreateBinderResponse createBinder(CreateBinderRequest createBinderRequest) throws FileItException {
		CreateBinderResponse createBinderResponse = new CreateBinderResponse();
		JSONArray bookArray = new JSONArray();
		try {
			FileUtil.checkTestJson();
			bookArray = FileUtil.checkBookList();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String htmlContent = createBinderRequest.getHtmlContent();
		TransformationProcessor transformationProcessor = new TransformationProcessor();
		BinderList listOfBinderObj = transformationProcessor.createBinderList(htmlContent);
		transformationProcessor.processHtmlToBinderXml(listOfBinderObj);
		UpdateMasterJson updateMasterJson = new UpdateMasterJson();
		updateMasterJson.prepareMasterJson(listOfBinderObj);
		createBinderResponse.setSuccessMsg("Binder Successfully Created.");
		FileItContext forBookClassifcation = new FileItContext();
		bookArray.add(listOfBinderObj.getName());
		forBookClassifcation.add(BinderConstants.CLASSIFIED_BOOK_LIST, bookArray);
		JSONObject parentObj = new JSONObject();
		parentObj.put("Books", bookArray);
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		InputStream is = new ByteArrayInputStream(parentObj.toJSONString().getBytes());
		oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"), "BookList.JSON",
				is, "application/json");
		return createBinderResponse;
	}

	@POST
	@Path("getImage")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<String> getFile(GetImageRequest oGetImageRequest) throws Exception {
		InputStream fis;
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		String wordToSearchFor1 = oGetImageRequest.getBookName() + '/' + "Contents/";
		List<String> oImages = new ArrayList<>();
		int pagecounter = 0;
		List<String> oList = oCloudStorageConfig
				.listBucket(CloudPropertiesReader.getInstance().getString("bucket.name"));
		for (String word1 : oList) {
			if (word1.contains(wordToSearchFor1)) {
				String extension = FilenameUtils.getExtension(word1);
				String fileName = FilenameUtils.getName(word1);
				fis = oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"), word1);
				ContentProcessor oContentProcessor = new ContentProcessor();
				pagecounter = oContentProcessor.processContentImage(oGetImageRequest.getBookName(), fis,
						oGetImageRequest.getBookName() + "/Images/", extension, fileName, pagecounter);
			}
		}
		String wordToSearchFor = oGetImageRequest.getBookName() + "/Images/";
		List<Integer> oList2 = new ArrayList<>();
		for (String word : oList) {
			if (word.contains(wordToSearchFor)) {
				oList2.add(Integer.valueOf(word.substring(word.indexOf("Images/") + 7, word.indexOf(".jpeg"))));
			}

		}
		Collections.sort(oList2);
		for (int j = 0; j < oList2.size(); j++) {
			oImages.add(
					oCloudStorageConfig.getSignedString(CloudPropertiesReader.getInstance().getString("bucket.name"),
							wordToSearchFor + oList2.get(j).toString() + ".jpeg"));
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
		String url = null;
		if (null != oDownloadFileRequest.getFileName()) {
			if (oDownloadFileRequest.getFileName().size() > 1) {
				File oFile = new File(this.getClass().getClassLoader().getResource("/").getPath()
						+ oDownloadFileRequest.getBookName() + ".zip");
				oContentProcessor.getMultipleFileDownload(oDownloadFileRequest.getBookName(),
						oDownloadFileRequest.getFileName(), oFile);
				InputStream fis = new FileInputStream(oFile);
				oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
						"/" + oDownloadFileRequest.getBookName() + "/" + oDownloadFileRequest.getBookName() + ".zip",
						fis, "application/zip");
				url = oCloudStorageConfig.getSignedString(CloudPropertiesReader.getInstance().getString("bucket.name"),
						"/" + oDownloadFileRequest.getBookName() + "/" + oDownloadFileRequest.getBookName() + ".zip");
			} else {
				url = oCloudStorageConfig.getSignedString(CloudPropertiesReader.getInstance().getString("bucket.name"),
						oDownloadFileRequest.getBookName() + "/Contents/" + oDownloadFileRequest.getFileName().get(0));
			}
		} else {
			File oFile = new File(this.getClass().getClassLoader().getResource("/").getPath()
					+ oDownloadFileRequest.getBookName() + ".zip");
			oContentProcessor.getZipFile(oDownloadFileRequest.getBookName(), oFile);
			InputStream fis = new FileInputStream(oFile);
			oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"/" + oDownloadFileRequest.getBookName() + "/" + oDownloadFileRequest.getBookName() + ".zip", fis,
					"application/zip");
			url = oCloudStorageConfig.getSignedString(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"/" + oDownloadFileRequest.getBookName() + "/" + oDownloadFileRequest.getBookName() + ".zip");
		}
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
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			oJsonObject = contentProcessor.processContent(bookName, bais, path, type, fileName);
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
		String classificationName = deleteBookRequest.getClassificationName();
		DeleteBookProcessor deleteBookProcessor = new DeleteBookProcessor();
		JSONObject succssMsg = deleteBookProcessor.deleteBookProcessor(bookName, classificationName);
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
		JSONObject array = null;
		JSONArray jsonArray = null;
		if (FileItContext.get(BinderConstants.CLASSIFIED_BOOK_LIST) != null) {
			jsonArray = (JSONArray) FileItContext.get(BinderConstants.CLASSIFIED_BOOK_LIST);
		} else {
			CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
			InputStream oInputStream = oCloudStorageConfig
					.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"), "BookList.JSON");
			JSONParser parser = new JSONParser();
			array = (JSONObject) parser.parse(new InputStreamReader(oInputStream));
			jsonArray = (JSONArray) array.get("Books");
			FileItContext forBookClassifcation = new FileItContext();
			forBookClassifcation.add(BinderConstants.CLASSIFIED_BOOK_LIST, jsonArray);
		}

		return jsonArray;

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

	@POST
	@Path("classifiedData")
	public JSONObject getBookClassification() throws Exception {
		return (JSONObject) FileItContext.get(BinderConstants.CLASSIFIED_BOOK_NAMES);

	}

	@POST
	@Path("addClassification")
	public AddClassificationResponse addBookClassification(AddClassificationRequest addClassificationRequest)
			throws FileItException {
		AddClassificationResponse addClassificationResponse = new AddClassificationResponse();
		String className = addClassificationRequest.getClassificationName();
		try {
			addClassificationResponse = AddClassificationProcessor.getInstance().addClassification(className,
					addClassificationResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return addClassificationResponse;
	}

	@GET
	@Path("getClassification")
	public List<String> getClassifications() throws FileItException {
		List<String> getClassifications = new ArrayList<String>();
		try {
			getClassifications = AddClassificationProcessor.getInstance().getClassifications();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return getClassifications;
	}

	@POST
	@Path("tagBook")
	public BookMarkResponse bookMark(BookMarkRequest bookMarkRequest) throws FileItException {
		BookMarkResponse bookMarkResponse = new BookMarkResponse();
		String loggedInUser = bookMarkRequest.getUserName();
		String requestedBookName = bookMarkRequest.getBookName();
		String classificationName = bookMarkRequest.getClassificationName();
		BookMarkDetails bookMarkdetails = new BookMarkDetails();
		bookMarkdetails = BookMarkUtil.getInstance().saveUserBookMarkDetails(loggedInUser, requestedBookName,
				classificationName);
		bookMarkResponse.setBookmarkDetails(bookMarkdetails);
		return bookMarkResponse;

	}

	@POST
	@Path("getBookMarks")
	public BookMarkResponse getBookMarks(BookMarkRequest bookMarkRequest) throws FileItException {
		BookMarkResponse bookMarkResponse = new BookMarkResponse();
		String loggedInUser = bookMarkRequest.getUserName();

		List<BookMarkDetails> bookMarkdetails = new ArrayList<BookMarkDetails>();
		bookMarkdetails = BookMarkUtil.getInstance().getBookMarks(loggedInUser);
		bookMarkResponse.setBookmarkDetailsList(bookMarkdetails);
		return bookMarkResponse;

	}
}
