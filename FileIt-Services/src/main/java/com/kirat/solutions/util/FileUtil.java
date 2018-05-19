package com.kirat.solutions.util;

import com.kirat.solutions.Constants.BinderConstants;

public class FileUtil {

	public static String createDynamicFilePath(String name) {
		String extension = BinderConstants.EXTENSION;
		String filePath = FileInfoPropertyReader.getInstance().getString("xml.file.path");
		extension = name.concat(extension);
		filePath = filePath + extension;
		return filePath;
	}
}
