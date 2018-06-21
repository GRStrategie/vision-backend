package com.dev.gr.strategie.rest.service.job;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dev.gr.strategie.rest.service.utils.Utils;

public class RemoveFromDisplay implements Job {

	private static final Logger log = LoggerFactory.getLogger(RemoveFromDisplay.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		List<String> filenames = ((List<String>) context.getMergedJobDataMap().getWrappedMap().get("filenames"));
		filenames.forEach(f -> {
			try {
				Files.delete(Utils.dataPath().resolveSibling("Videos").resolve(f));
				log.info("Deleted " + f + " from " + Utils.dataPath().resolveSibling("Videos"));
			} catch (IOException e) {
				log.error("Exception raised :", e);
			}
		});
	}
}