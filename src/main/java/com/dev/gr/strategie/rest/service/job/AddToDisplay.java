package com.dev.gr.strategie.rest.service.job;

import static com.dev.gr.strategie.rest.service.utils.Utils.*;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddToDisplay implements Job {
	
	private static final Logger log = LoggerFactory.getLogger(AddToDisplay.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		List<String> filenames = ((List<String>) context.getMergedJobDataMap().getWrappedMap().get("filenames"));
		filenames.forEach(f -> {
			try {
				FileUtils.copyFileToDirectory(dataPath().resolve(f).toFile(), dataPath().resolveSibling("Videos").toFile());
				log.info("Copied " + f + " to " + data().getAbsolutePath());
			} catch (IOException e) {
				log.error("Exception raised :", e);
			}
		});
	}
}