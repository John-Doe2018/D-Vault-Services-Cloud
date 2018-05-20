package com.kirat.solutions.processor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import com.kirat.solutions.Constants.BinderConstants;
import com.kirat.solutions.domain.FileItContext;
import com.kirat.solutions.util.FileInfoPropertyReader;
import com.kirat.solutions.util.FileItException;

public class ContentProcessor {
	FileItContext fileItContext;
	List<String> paths = new ArrayList<String>();
	private static ContentProcessor INSTANCE;
	static int pagecounter = 0;

	public static synchronized ContentProcessor getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new ContentProcessor();
		}
		return INSTANCE;
	}

	@SuppressWarnings({ "static-access", "unchecked" })
	public void processContentImage(String bookName) throws FileItException {
		PDDocument document = null;
		BufferedImage bufferedImage = null;
		String extension = BinderConstants.IMG_EXTENSION;
		paths = (List<String>) fileItContext.get(BinderConstants.CONTXT_PATH_NAMES);

		try {
			for (String path : paths) {
				// i = counter;
				document = PDDocument.load(path);
				List<PDPage> pages = document.getDocumentCatalog().getAllPages();
				// int count = document.getDocumentCatalog().getAllPages().size();
				for (PDPage page : pages) {
					pagecounter++;
					String imagePath = createDyanmicImagePath(pagecounter, bookName, extension);
					bufferedImage = page.convertToImage();
					File outputFile = new File(imagePath);
					ImageIO.write(bufferedImage, "jpg", outputFile);
				}
			}
		} catch (IOException e) {
			throw new FileItException(e.getMessage());
		}

	}

	public static String createDyanmicImagePath(int i, String bookName, String extension) {
		boolean isDirectory = false;
		String fullContentDirectory = null;
		String absoluteImgPath = null;

		String counter = String.valueOf(i);
		String staticPath = FileInfoPropertyReader.getInstance().getString("doc.static.path");
		fullContentDirectory = staticPath.concat("\\" + bookName + "\\Images");
		java.io.File file = new File(fullContentDirectory);
		isDirectory = file.isDirectory();
		if (!isDirectory) {
			file.mkdirs();
		}
		absoluteImgPath = fullContentDirectory.concat("\\" + counter.concat(extension));
		return absoluteImgPath;
	}

}