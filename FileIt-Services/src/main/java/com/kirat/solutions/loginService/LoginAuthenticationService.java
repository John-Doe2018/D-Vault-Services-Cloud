package com.kirat.solutions.loginService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.kirat.solutions.auth.FileItAuthentication;
import com.kirat.solutions.domain.LoginRequest;
import com.kirat.solutions.domain.LoginResponse;
import com.kirat.solutions.util.FileItException;

public class LoginAuthenticationService {


	@POST
	@Path("login")
	@Produces("application/json")
	public LoginResponse authenticate(LoginRequest loginRequest) throws FileItException {
		LoginResponse loginResponse = new LoginResponse();
		String userName = loginRequest.getUserName();
		String password = loginRequest.getPassword();
		FileItAuthentication.checkCredentials(userName, password);
		loginResponse.setSuccessMsg("Login Successful");
		return loginResponse;
	}


}
