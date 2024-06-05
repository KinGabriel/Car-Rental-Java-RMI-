package Utilities;
/**
 * The Vehicle class represents a vehicle with various details and user information.
 */
public class Vehicle {
    // Private fields for vehicle properties
    private int id;
    private String model;
    private String status;
    private String plateNumber;
    private double price;
    private String username;
    private String firstName;
    private String lastName;
    private String schedule;
    /**
     * Constructor for Vehicle without an ID.
     * @param model       The model of the vehicle.
     * @param status      The current status of the vehicle (e.g., available, rented).
     * @param plateNumber The plate number of the vehicle.
     * @param price       The price of the vehicle.
     * @param username    The username of the user associated with the vehicle.
     * @param firstname   The first name of the user associated with the vehicle.
     * @param lastname    The last name of the user associated with the vehicle.
     */
    public Vehicle (String model, String status, String plateNumber, double price, String username, String firstname, String lastname){
        this.model = model;
        this.status = status;
        this.plateNumber = plateNumber;
        this.price = price;
        this.username = username;
        this.firstName = firstname;
        this.lastName = lastname;
    }
    /**
     * Constructor for Vehicle with an ID.
     *
     * @param id          The unique identifier of the vehicle.
     * @param model       The model of the vehicle.
     * @param status      The current status of the vehicle.
     * @param plateNumber The plate number of the vehicle.
     * @param price       The price of the vehicle.
     * @param username    The username of the user associated with the vehicle.
     */
    public Vehicle(int id, String model, String status, String plateNumber, double price, String username) {
        this.id = id;
        this.model = model;
        this.status = status;
        this.plateNumber = plateNumber;
        this.price = price;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    public Vehicle(String model, String plateNumber, double price) {
        this.model = model;
        this.plateNumber = plateNumber;
        this.price = price;
    }

    public Vehicle(String id, String model, String status, String plateNumber, double price, String username, String firstName, String lastName) {
        this.id = Integer.parseInt(id);
        this.model = model;
        this.status = status;
        this.plateNumber = plateNumber;
        this.price = price;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }



    public Vehicle(int id, String model, String status, String plateNumber, double price, String username, int days) {
        this.id = Integer.parseInt(String.valueOf(id));
        this.model = model;
        this.status = status;
        this.plateNumber = plateNumber;
        this.price = price;
        this.username = username;
        this.schedule = schedule;
    }

    /**
     * Sets the unique identifier for the vehicle.
     * @param id the ID to set
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * Sets the model of the vehicle.
     * @param model the model to set
     */
    public void setModel(String model) {
        this.model = model;
    }
    /**
     * Sets the current status of the vehicle.
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
    /**
     * Sets the plate number of the vehicle.
     * @param plateNumber the plate number to set
     */
    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }
    /**
     * Sets the price of the vehicle.
     * @param price the price to set
     */
    public void setPrice(double price) {
        this.price = price;
    }
    /**
     * Sets the username of the user associated with the vehicle.
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
    /**
     * Sets the first name of the user associated with the vehicle.
     * @param firstName the first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    /**
     * Sets the last name of the user associated with the vehicle.
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    /**
     * Gets the unique identifier of the vehicle.
     * @return the vehicle ID
     */
    public int getId() {
        return id;
    }
    /**
     * Gets the model of the vehicle.
     * @return the model of the vehicle
     */
    public String getModel() {
        return model;
    }
    /**
     * Gets the current status of the vehicle.
     * @return the status of the vehicle
     */
    public String getStatus() {
        return status;
    }
    /**
     * Gets the plate number of the vehicle.
     * @return the plate number of the vehicle
     */
    public String getPlateNumber() {
        return plateNumber;
    }

    /**
     * Gets the price of the vehicle.
     * @return the price of the vehicle
     */
    public double getPrice() {
        return price;
    }

    /**
     * Gets the username of the user associated with the vehicle.
     * @return the username of the associated user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the first name of the user associated with the vehicle.
     * @return the first name of the associated user
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Gets the last name of the user associated with the vehicle.
     * @return the last name of the associated user
     */
    public String getLastName() {
        return lastName;
    }
}