/*
 * Copyright (c) 2010-2016 Tata Consultancy Services Limited. All Rights Reserved.
 * Unauthorised access, copying and replication are prohibited. 
 *
 
package com.tcs.dummyproject.startup;

import org.springframework.context.ApplicationContext;

public final class OmnistoreApplicationContext {

	private static OmnistoreApplicationContext INSTANCE;
	*//**
	 * appContext
	 *//*
	private ApplicationContext appContext;

	private OmnistoreApplicationContext() {

	}

	public void initializeContext(String contextFiles) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				contextFiles);
		appContext = context;
	}

	public void setAppContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}

	*//**
	 * <p>
	 * Get single instance of ItemServiceRegistry.
	 * <p>
	 * 
	 * @return
	 *//*
	public static synchronized OmnistoreApplicationContext getInstance() {
		if (INSTANCE == null) {
			INSTANCE=new OmnistoreApplicationContext();
		}
		return INSTANCE;
	}

	public ApplicationContext getAppContext() {
		return appContext;
	}

}
*/