package com.dev.gr.strategie.rest.service.job;

import org.apache.commons.io.FileUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.dev.gr.strategie.rest.service.utils.Utils;

public class RemoveFromDisplay implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("Removed from display ! : " + context.getMergedJobDataMap().getString("filename"));
		FileUtils.deleteQuietly(Utils.dataPath().resolveSibling("Videos").resolve("testFile.txt").toFile());
	}
}