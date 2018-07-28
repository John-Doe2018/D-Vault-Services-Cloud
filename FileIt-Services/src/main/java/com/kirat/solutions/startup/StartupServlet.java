package com.kirat.solutions.startup;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServlet;

import com.kirat.solutions.processor.PrepareClassificationMap;
import com.kirat.solutions.util.FileInfoPropertyReader;
import com.kirat.solutions.util.FileUtil;

public class StartupServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public StartupServlet() {
		try {
			FileUtil.checkBookClassificationJson();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Timer timer = new Timer();
		TimerTask myTask = new TimerTask() {
			@Override
			public void run() {
				// whatever you need to do every 2 seconds
				try {
					PrepareClassificationMap.createClassifiedMap(FileInfoPropertyReader.getInstance().getString("masterjson.file.path"));
				} catch (Exception e) {
					e.printStackTrace();
				}}
		};

		timer.schedule(myTask, 20000, 20000);
	}
	
}
