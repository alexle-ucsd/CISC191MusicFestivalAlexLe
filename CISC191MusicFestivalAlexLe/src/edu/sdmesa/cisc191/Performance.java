package edu.sdmesa.cisc191;

/**
 * Represents a DJ performance at the music festival.
 * Stores the artist, stage, and performance date/time.
 *
 * Author: Alex Le
 * CISC 191 - Week 2 Project
 */
public class Performance {

    private int performanceId;
    private String dateTime;
    private Artist artist;
    private Stage stage;

    public Performance(int performanceId, String dateTime,
                       Artist artist, Stage stage) {
        this.performanceId = performanceId;
        this.dateTime = dateTime;
        this.artist = artist;
        this.stage = stage;
    }

    public int getPerformanceId() {
        return performanceId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public Artist getArtist() {
        return artist;
    }

    public Stage getStage() {
        return stage;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void displayPerformance() {
        System.out.println("Performance ID: " + performanceId);
        System.out.println("Artist: " + artist.getName());
        System.out.println("Stage: " + stage.getStageName());
        System.out.println("Date and Time: " + dateTime);
    }
}
