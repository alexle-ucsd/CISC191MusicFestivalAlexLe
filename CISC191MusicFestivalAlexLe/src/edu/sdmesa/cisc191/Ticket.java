package edu.sdmesa.cisc191;

/**
 * Represents a festival ticket.
 * Author: Alex Le
 * CISC 191 - Week 2 Project
 */
public class Ticket {

    private int ticketId;
    private String ticketType;
    private double price;

    public Ticket(int ticketId, String ticketType,
                  double price) {
        this.ticketId = ticketId;
        this.ticketType = ticketType;
        this.price = price;
    }

    public int getTicketId() {
        return ticketId;
    }

    public String getTicketType() {
        return ticketType;
    }

    public double getPrice() {
        return price;
    }

    public void displayTicket() {
        System.out.println("Ticket ID: " + ticketId);
        System.out.println("Type: " + ticketType);
        System.out.println("Price: $" + price);
    }
}
