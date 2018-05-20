package com.kirat.solutions.domain;

public class SearchBookRequest extends Response {

	String bookName;

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

}