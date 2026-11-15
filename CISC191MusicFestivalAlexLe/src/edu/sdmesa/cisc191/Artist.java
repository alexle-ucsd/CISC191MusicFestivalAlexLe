package edu.sdmesa.cisc191;

/**
 * Represents a DJ or artist at the music festival.
 * Author: Alex Le
 * CISC 191 - Week 2 Project
 */
public class Artist {

    private int artistId;
    private String name;
    private String genre;

    public Artist(int artistId, String name, String genre) {
        this.artistId = artistId;
        this.name = name;
        this.genre = genre;
    }

    public int getArtistId() {
        return artistId;
    }

    public String getName() {
        return name;
    }

    public String getGenre() {
        return genre;
    }

    public void displayArtist() {
        System.out.println("Artist ID: " + artistId);
        System.out.println("Name: " + name);
        System.out.println("Genre: " + genre);
    }
}
