package com.dev.gr.strategie.rest.service.job;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AddToDisplay implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		((List<String>) context.getMergedJobDataMap().getWrappedMap().get("filenames")).forEach(System.out::println);
		/*System.out.println("Added to display ! : " + context.getMergedJobDataMap().getString("filename"));
		try {
			FileUtils.copyFileToDirectory(Utils.testDataPath().resolve("testFile.txt").toFile(), Utils.dataPath().resolveSibling("Videos").toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
}