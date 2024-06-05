package Utilities;

public class BookingHistoryEntry {
    private String vehicleID;
    private String model;
    private String status;
    private String plateNumber;
    private double price;
    private String username;
    private String date;
    private int days;

    // Constructor
    public BookingHistoryEntry(String vehicleID, String model, String status, String plateNumber, double price, String username, String date, int days) {
        this.vehicleID = vehicleID;
        this.model = model;
        this.status = status;
        this.plateNumber = plateNumber;
        this.price = price;
        this.username = username;
        this.date = date;
        this.days = days;
    }

    // Getters and setters
    public String getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}