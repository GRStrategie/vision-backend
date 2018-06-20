package com.dev.gr.strategie.rest.service.data;

import java.util.List;
import java.util.Optional;

public class Playlist {
	
	private String name;
	private List<String> filenameList;
	private String cronAdd;
	private Optional<String> cronRem;
	
	public Playlist(String name, List<String> filenameList, String cronAdd, String cronRem) {
		this.name = name;
		this.filenameList = filenameList;
		this.cronAdd = cronAdd;
		this.cronRem = Optional.ofNullable(cronRem);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getFilenameList() {
		return filenameList;
	}

	public void setFilenameList(List<String> filenameList) {
		this.filenameList = filenameList;
	}

	public String getCronAdd() {
		return cronAdd;
	}

	public void setCronAdd(String cronAdd) {
		this.cronAdd = cronAdd;
	}

	public Optional<String> getCronRem() {
		return cronRem;
	}

	public void setCronRem(String cronRem) {
		this.cronRem = Optional.ofNullable(cronRem);
	}
	
	
}
