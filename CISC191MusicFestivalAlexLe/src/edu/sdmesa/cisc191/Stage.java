package edu.sdmesa.cisc191;

import java.util.ArrayList;

/**
 * Represents a stage at the music festival. Stores the stage name and scheduled
 * performances.
 *
 * Author: Alex Le CISC 191 - Week 2 Project
 */
public class Stage {

	private int stageId;
	private String stageName;
	private ArrayList<Performance> performances;

	public Stage(int stageId, String stageName) {
		this.stageId = stageId;
		this.stageName = stageName;
		this.performances = new ArrayList<>();
	}

	public int getStageId() {
		return stageId;
	}

	public String getStageName() {
		return stageName;
	}

	public void addPerformance(Performance performance) {
		performances.add(performance);
	}

	public ArrayList<Performance> getPerformances() {
		return new ArrayList<>(performances);
	}

	public void displaySchedule() {
		System.out.println("Stage: " + stageName);

		for (Performance performance : performances) {
			performance.displayPerformance();
		}
	}
}
