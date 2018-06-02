package com.kirat.solutions.auth;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.kirat.solutions.Constants.ErrorCodeConstants;
import com.kirat.solutions.domain.User;
import com.kirat.solutions.domain.Users;
import com.kirat.solutions.util.ErrorMessageReader;
import com.kirat.solutions.util.FileInfoPropertyReader;
import com.kirat.solutions.util.FileItException;

public class GetOrValidateUser {

	public static String validateUser(String userName) throws FileItException{
		boolean isUser = false;
		String passWord = null;
		String xmlPath =FileInfoPropertyReader.getInstance().getString("doc.user.profile.path");
		Unmarshaller un;
		Users userList;
		try {
			JAXBContext context = JAXBContext.newInstance(Users.class);
			un = context.createUnmarshaller();
			userList = (Users) un.unmarshal(new File(xmlPath));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			throw new FileItException(e.getMessage());
		}
		for(User user: userList.getUsers()) {
			if(userName.equals(user.getUserName())) {
				passWord = user.getPassword();
				isUser = true;
			}
		}
		if(!isUser) {
			throw new FileItException(ErrorCodeConstants.ERR_CODE_0004, ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0004));
		}
		return passWord;
	}

}
