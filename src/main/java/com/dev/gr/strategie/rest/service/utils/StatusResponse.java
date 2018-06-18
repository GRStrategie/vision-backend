package com.dev.gr.strategie.rest.service.utils;

public enum StatusResponse {
    SUCCESS ("Success"),
    ERROR ("Error");
  
    private String status;  
    
    StatusResponse(String status) {
    	this.setStatus(status);
    }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}   
}