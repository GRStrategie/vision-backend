package com.dev.gr.strategie.rest.service.api;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dev.gr.strategie.rest.service.data.Playlist;
import com.dev.gr.strategie.rest.service.job.AddToDisplay;
import com.dev.gr.strategie.rest.service.job.RemoveFromDisplay;
import com.dev.gr.strategie.rest.service.utils.StandardResponse;
import com.dev.gr.strategie.rest.service.utils.StatusResponse;
import com.google.gson.Gson;

import spark.Route;

public class ScheduleApi {
	/* 
	 * Will be used to control the file copy / deletion to / from the data folder
	 * When a period is submitted :
	 *  - create a job copying the file on the daily start period
	 *  - create a job deleting the file on the daily end period
	 *  - once the after period is done, delete both jobs from the scheduler
	 */

	private static final Logger log = LoggerFactory.getLogger(ScheduleApi.class);
	private static final Gson gson = new Gson();
	private static Scheduler scheduler;
	static {
				try {
					scheduler = new StdSchedulerFactory().getScheduler();
					scheduler.start();
				} catch (SchedulerException e) {
					e.printStackTrace();
				}		
	}
	
	public static Route schedulePlaylist = (req, res) -> {	
		Playlist playlist = gson.fromJson(req.body(), Playlist.class);
		
		Map jobData = new HashMap<String, List<String>>();
		jobData.put("filenames", playlist.getFilenameList());
		
		JobDetail addJob = newJob(AddToDisplay.class).
				withIdentity("addToDisplay:" + playlist.getName()).
				usingJobData(new JobDataMap(jobData)).
				build();
		
		Trigger addTrigger = newTrigger().
				withIdentity("addToDisplayTrigger:" + playlist.getName()).
				withSchedule(cronSchedule(playlist.getCronAdd())).
				build();
	
		scheduler.scheduleJob(addJob, addTrigger);
		log.info("Successfully scheduled job addToDisplay:" + playlist.getName());
		
		if(playlist.getCronRem().isPresent()) {
			JobDetail remJob = newJob(RemoveFromDisplay.class).
					withIdentity("removeFromDisplay:" + playlist.getName()).
					usingJobData(new JobDataMap(jobData)).
					build();
			
			Trigger remTrigger = newTrigger().
					withIdentity("removeFromDisplayTrigger:" + playlist.getName()).
					withSchedule(cronSchedule(playlist.getCronRem().get())).
					build();
		
			scheduler.scheduleJob(remJob, remTrigger);
			log.info("Successfully scheduled job removeFromDisplay:" + playlist.getName());
		}
		res.status(200);
		res.type("application/json");
		return new StandardResponse(StatusResponse.SUCCESS);
	};
	
	public static Route deletePlaylist = (req, res) -> {
		String playlistName = req.params(":playlistName");
		res.type("application/json");
		try {
			scheduler.deleteJob(new JobKey("addToDisplay:" + playlistName));
			log.info("Successfully deleted job addToDisplay:" + playlistName);
			scheduler.deleteJob(new JobKey("removeFromDisplay:" + playlistName));
			log.info("Successfully deleted job removeFromDisplay:" + playlistName);	
			res.status(200);
			return new StandardResponse(StatusResponse.SUCCESS);
		} catch(SchedulerException e) {
			res.status(500);
			log.error("Exception raised : ", e);
			return new StandardResponse(StatusResponse.ERROR, gson.toJsonTree(e.toString()));
		}	
	};
}