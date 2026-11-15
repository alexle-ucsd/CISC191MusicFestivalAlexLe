package edu.sdmesa.cisc191;

import java.util.ArrayList;

/**
 * Manages the electronic music festival.
 * Stores attendees, artists, stages, and performances.
 *
 * Author: Alex Le
 * CISC 191 - Week 2 Project
 */
public class Festival {

    private String festivalName;
    private ArrayList<Attendee> attendees;
    private ArrayList<Artist> artists;
    private ArrayList<Stage> stages;
    private ArrayList<Performance> performances;

    public Festival(String festivalName) {
        this.festivalName = festivalName;
        attendees = new ArrayList<>();
        artists = new ArrayList<>();
        stages = new ArrayList<>();
        performances = new ArrayList<>();
    }

    public String getFestivalName() {
        return festivalName;
    }

    public void addAttendee(Attendee attendee) {
        attendees.add(attendee);
    }

    public void addArtist(Artist artist) {
        artists.add(artist);
    }

    public void addStage(Stage stage) {
        stages.add(stage);
    }

    public void addPerformance(Performance performance) {
        performances.add(performance);
        performance.getStage().addPerformance(performance);
    }

    public Artist searchArtist(String name) {
        for (Artist artist : artists) {
            if (artist.getName().equalsIgnoreCase(name)) {
                return artist;
            }
        }
        return null;
    }

    public void displaySchedule() {
        System.out.println("Festival: " + festivalName);

        for (Stage stage : stages) {
            stage.displaySchedule();
        }
    }
}
