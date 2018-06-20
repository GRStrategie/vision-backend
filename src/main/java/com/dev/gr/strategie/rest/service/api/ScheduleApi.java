package com.dev.gr.strategie.rest.service.api;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

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
		
		JobDetail job = newJob(AddToDisplay.class).
				withIdentity("addToDisplay:" + playlist.getName()).
				usingJobData(new JobDataMap(jobData)).
				build();
		
		Trigger trigger = newTrigger().
				withIdentity("addToDisplayTrigger:" + playlist.getName()).
				withSchedule(cronSchedule(playlist.getCronAdd())).
				build();
	
		scheduler.scheduleJob(job, trigger);
		res.status(200);
		return new StandardResponse(StatusResponse.SUCCESS);
	};
}
