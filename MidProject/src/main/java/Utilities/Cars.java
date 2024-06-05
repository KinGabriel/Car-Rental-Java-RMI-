package Utilities;

import java.io.Serializable;

/**
 * The Cars class represents a car with various attributes and is used within
 * a server context to manage car-related data.
 */
public class Cars implements Serializable {
    // Serialization ID for ensuring class consistency during the serialization and deserialization process.
    private static final long serialVersionUID = 1L;

    // Private fields representing the car's properties.
    private int ID;
    private String model;
    private String status;
    private String plateNumber;
    private Double price;
    private String firstName;
    private String lastName;
    private String userName;
    private String rentBy;
    private int days;

    /**
     * Constructor for creating a Cars object with all necessary attributes.
     *
     * @param ID          the unique identifier of the car.
     * @param model       the model of the car.
     * @param status      the current status of the car (e.g., available, rented).
     * @param plateNumber the license plate number of the car.
     * @param price       the price of the car.
     * @param username
     */
    public Cars(int ID, String model, String status, String plateNumber, Double price, String username) {
        this.ID = ID;
        this.model = model;
        this.status = status;
        this.plateNumber = plateNumber;
        this.price = price;
        this.userName = username;
        this.days = days;
        this.rentBy = rentBy;
    }

    /**
     * Default constructor for Cars object.
     */
    public Cars() {
        // This constructor is empty and can be used to create an instance without setting properties initially.
    }

    // Setters (also known as mutators) for the Cars class fields.

    public void setDays(int days) {
        this.days = days;
    }

    public void setRentBy(String rentBy) {
        this.rentBy = rentBy;
    }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setModel(String model) { this.model = model; }
    public void setStatus(String status) { this.status = status; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public void setID(int ID) { this.ID = ID; }
    public void setPrice(Double price) { this.price = price; }

    // Getters (also known as accessors) for the Cars class fields.
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUserName() { return userName; }
    public String getModel() { return model; }
    public String getStatus() { return status; }
    public String getPlateNumber() { return plateNumber; }
    public int getID() { return ID; }
    public Double getPrice() { return price; }

    public int getDays() {
        return days;
    }

    public String getRentBy() {
        return rentBy;
    }
}
