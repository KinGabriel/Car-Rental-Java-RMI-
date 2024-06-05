package Server;

import Utilities.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The ServerController class implements the server-side logic for the car rental system.
 * It provides methods for user authentication, registration, vehicle management, and booking cancellation.
 */

public class ServerController extends UnicastRemoteObject implements Remote, servant {
    /**
     * Constructor for ServerController class.
     * @throws RemoteException If there is an error in the remote operation.
     */
    public ServerController() throws RemoteException {
    }
    // Map to store active user sessions
    private static final Map<String, Boolean> activeSessions = new HashMap<>();
    // Object for synchronization
    private static final Object lock = new Object();
    private static Random random = new Random();
    // File paths for JSON data
    private static final String JSON_FILE_PATH_CREDENTIALS = "MidProject/src/main/java/File/credentials.json";
    private static final String JSON_FILE_PATH_CAR_INFO = "MidProject/src/main/java/File/Car_Informations.json";
    private static final String JSON_FILE_PATH_BOOKING_INFO = "MidProject/src/main/java/File/booking.json";
    private static final String JSON_FILE_PATH_PENDING_VEHICLES = "MidProject/src/main/java/File/pendingCars.json";

    // List of JSON file paths
    List<String> jsonFilePaths = Arrays.asList("MidProject/src/main/java/File/Car_Informations.json", "MidProject/src/main/java/File/pendingCars.json");


    /**
     * Logs out the user from the system.
     * @param username The username of the user to log out.
     * @return True if logout is successful, otherwise False.
     * @throws RemoteException If there is an error in the remote operation.
     * @throws FailedLogOutException If logout fails.
     */
    @Override
    public Boolean logout(String username) throws RemoteException, FailedLogOutException {
        boolean sessionClosed = closeSession(username);
        if (sessionClosed) {
            return true;
        }else
            throw new FailedLogOutException("Unsuccessful log out");
    }
    /**
     * Authenticates the user by verifying the provided credentials.
     * @param credentials A string containing username and password separated by comma.
     * @return A message indicating the result of the login attempt.
     * @throws RemoteException If there is an error in the remote operation.
     * @throws AlreadyLoggedInException If the user is already logged in.
     * @throws InvalidInputCredentialsExceptions If the provided credentials are invalid.
     */
    @Override
    public String login(String credentials) throws RemoteException, AlreadyLoggedInException, InvalidInputCredentialsExceptions, BannedAccountException {
        String[] loginInfo = credentials.split(",");
        String username = loginInfo[0];
        String password = loginInfo[1];

        synchronized (lock) {
            // Validate login
            LoginResult loginResult = logInvalidation(username, password);
            switch (loginResult) {
                case clientLogIn:
                    // Check if the user is already logged in
                    if (!validateSession(username)) {
                        // Create a new session for the user
                        if (createSession(username)) {
                            return "Login successful!";
                        }
                    } else {
                        throw new AlreadyLoggedInException("Error: User is already logged in.");
                    }
                    break;
                case AdminLogIn:
                    if (!validateSession(username)) {
                        // Create a new session for the user
                        if (createSession(username)) {
                            return "Admin login successful!";
                        }
                    } else {
                        throw new AlreadyLoggedInException("Error: User is already logged in.");
                    }
                    break;
                case InvalidLogIn:
                    throw new InvalidInputCredentialsExceptions("Invalid username or password!");
                case BannedAccount:
                    throw new BannedAccountException("Your Account is currently Banned");
            }
        }
        return null;
    }
    /**
     * Registers a new user in the system.
     * @param credentials A string containing user registration information.
     * @return A message indicating the result of the registration attempt.
     * @throws RemoteException If there is an error in the remote operation.
     * @throws UsernameExistsException If the provided username already exists.
     */
    @Override
    public String register(String credentials) throws RemoteException, UsernameExistsException {
        synchronized (lock) {
            String[] registrationInfo = credentials.split(",");
            String firstName = registrationInfo[0];
            String lastName = registrationInfo[1];
            String username = registrationInfo[2];
            String password = registrationInfo[3];
            String email = registrationInfo[4];
            String phoneNumber = registrationInfo[5];

            // Validate registration
            if (checkRegistration(username)) {
                // Registration is valid
                addUserToJson(firstName, lastName, username, password, email, phoneNumber);
                try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt",true))) {
                    printWriter.println("Username: " + username + "successfully created his account!");
                    return "Registration successful!";
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new UsernameExistsException("Username already exists");
            }
        }
    }
    /**
     * Searches for vehicles based on the specified attribute and value.
     * @param attribute The attribute to search for (e.g., model).
     * @param value The value to search for.
     * @return A list of JSONObjects representing matching vehicles.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public List<JSONObject> searchVehicle(String attribute, String value) throws RemoteException {
        List<JSONObject> matchingVehicles = new ArrayList<>();

        try {
            JSONParser parser = new JSONParser();
            JSONArray vehicles = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CAR_INFO));
            System.out.println(value);
            for (Object obj : vehicles) {
                JSONObject vehicle = (JSONObject) obj;
                String model = (String) vehicle.get("Model");
                if (model != null && model.toLowerCase().contains(value.toLowerCase())) {
                    matchingVehicles.add(vehicle);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception as needed
        }

        return matchingVehicles;
    }
    /**
     * Adds a new vehicle to the system.
     * @param name The name of the vehicle.
     * @param plateNumber The plate number of the vehicle.
     * @param schedule The schedule for the vehicle.
     * @param price The price of the vehicle.
     * @param username The username of the user adding the vehicle.
     * @return A message indicating the result of the operation.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public String addVehicle(String name, String plateNumber, Date schedule, Double price, String username) throws RemoteException, forceLogOutException {
        String status = "Pending";
        if (!validateSession(username)) {
            throw new forceLogOutException("");
        }
        try {
            // Parse the JSON file
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(JSON_FILE_PATH_PENDING_VEHICLES));
            Object obj1 = parser.parse(new FileReader(JSON_FILE_PATH_CAR_INFO));
            JSONArray jsonArray = (JSONArray) obj;
            JSONArray jsonArray1 = (JSONArray) obj1;

            // Check if plate number already exists with Pending status
            if (isPlateNumberExists(jsonArray, plateNumber) || isPlateNumberExists(jsonArray1, plateNumber)) {
                jsonArray1.clear();
                throw new ExistingVehicleException("PlateNumber exists");
            }

            int uniqueID = generateUniqueID(jsonArray);

            // Create a new JSON object for the new vehicle
            JSONObject vehicleObject = new JSONObject();
            vehicleObject.put("ID", uniqueID);
            vehicleObject.put("Model", name);
            vehicleObject.put("PlateNumber", plateNumber);
            vehicleObject.put("Status", status);
            vehicleObject.put("Price", price);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            vehicleObject.put("Schedule", sdf.format(schedule));
            vehicleObject.put("Username", username);

            // Add the new vehicle object to the JSON array
            jsonArray.add(vehicleObject);

            // Write the updated JSON array back to the file
            FileWriter fileWriter = new FileWriter(JSON_FILE_PATH_PENDING_VEHICLES);
            fileWriter.write(jsonArray.toJSONString());
            fileWriter.flush();
            fileWriter.close();
            try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt",true))) {
                printWriter.println(username + " Added a vehicle " +name+ " with a platenumber " + plateNumber);
            }
            {
                return "Vehicle added successfully.";
            }
        } catch (ExistingVehicleException e) {
            return "PlateNumber exists";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while adding the vehicle.";
        }
    }

    /**
     * Cancels a vehicle booking.
     * @param model The model of the vehicle.
     * @param plateNo The plate number of the vehicle.
     * @return True if the booking cancellation is successful, otherwise False.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public Boolean cancelVehicle(String model, String plateNo,String currentUser) throws RemoteException, forceLogOutException {
        if (!validateSession(currentUser)) {
            throw new forceLogOutException("");
        }
        try {
            String id = findPendingVehicleID(model, plateNo);

            if (id != null) {
                deletePendingVehicle(id);
            } else {
                System.out.println("Error: Vehicle with model " + model + " and plate number " + plateNo + " not found.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: Failed to cancel booking for vehicle with model " + model + " and plate number " + plateNo);
        }
        return true;
    }
    /**
     * Retrieves user credentials based on the provided username.
     * @param userName The username for which credentials are to be retrieved.
     * @return A list of User objects containing the matching user credentials.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public List<User> getCredentials(String userName) throws RemoteException {
        List<User> matchingUsers = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser();
            JSONArray usersArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CREDENTIALS));

            for (Object obj : usersArray) {
                JSONObject userObject = (JSONObject) obj;
                String storedUsername = (String) userObject.get("Username");

                if (storedUsername.equals(userName)) {
                    String firstName = (String) userObject.get("FirstName");
                    String lastName = (String) userObject.get("LastName");
                    String email = (String) userObject.get("Email");
                    String phoneNumber = (String) userObject.get("PhoneNumber");

                    User user = new User(firstName, lastName, email, storedUsername, phoneNumber);
                    matchingUsers.add(user);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return matchingUsers;
    }
    /**
     * Updates user credentials with new password and phone number.
     * @param username The username of the user whose credentials are to be updated.
     * @param newPassword The new password for the user.
     * @param newPhoneNumber The new phone number for the user.
     * @return True if the update is successful, otherwise False.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public Boolean updateCredentials(String username, String newPassword, String newPhoneNumber) throws RemoteException, forceLogOutException {
        if (!validateSession(username)) {
            throw new forceLogOutException("");
        }
        synchronized (lock) {
            try {
                // Read the JSON file containing user credentials
                JSONParser parser = new JSONParser();
                JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CREDENTIALS));
                for (Object obj : jsonArray) {
                    JSONObject userObject = (JSONObject) obj;
                    String storedUsername = (String) userObject.get("Username");

                    if (storedUsername.equals(username)) {
                        // Update password and phone number
                        userObject.put("Password", newPassword);
                        userObject.put("PhoneNumber", newPhoneNumber);

                        // Write the updated JSON array to the file
                        try (FileWriter fileWriter = new FileWriter(JSON_FILE_PATH_CREDENTIALS)) {
                            fileWriter.write(jsonArray.toJSONString());
                            return true;
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }
    /**
     * Retrieves booking history for the specified user.
     * @param currentUser The username of the user whose booking history is to be retrieved.
     * @return A list of strings containing booking details.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public List<String> showHistory(String currentUser) throws RemoteException {

        List<String> userBookingHistory = new ArrayList<>();
        synchronized (lock) {
            try {
                // Parse the JSON file
                JSONParser parser = new JSONParser();
                JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_BOOKING_INFO));

                // Iterate over the JSON array
                for (Object obj : jsonArray) {
                    JSONObject jsonObject = (JSONObject) obj;

                    // Extract the username from the JSON object
                    String username = (String) jsonObject.get("Username");

                    // Check if the username matches the current user
                    if (username.equals(currentUser)) {
                        // Extract other required fields
                        Long bookID = (Long) jsonObject.get("BookID");
                        Long vehicleID = (Long) jsonObject.get("ID");
                        String model = (String) jsonObject.get("Model");
                        String status = (String) jsonObject.get("Status");
                        String plateNumber = (String) jsonObject.get("PlateNumber");
                        Double price = (Double) jsonObject.get("Price");
                        String startDate = (String) jsonObject.get("StartDate");
                        String endDate = (String) jsonObject.get("EndDate");
                        Long days = (Long) jsonObject.get("TotalDays");
                        String rentBy = (String) jsonObject.get("rentBy");
                        String PhoneNumber = (String) jsonObject.get("PhoneNumber");;
                        // Construct the booking details string
                        String bookingDetails = String.format("%d,%d,%s,%s,%s,%.2f,%s,%s,%d,%s,%s",
                               bookID,vehicleID, model, plateNumber, status, price, startDate,endDate, days, rentBy,PhoneNumber);
                        userBookingHistory.add(bookingDetails);
                    }
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                // Handle file IO exception or JSON parsing exception
            }
        }
        return userBookingHistory;
    }
    /**
     * Saves an announcement to a JSON file.
     * @param announcement The announcement to be saved.
     * @return The saved announcement.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public String saveAnnouncement(String announcement,String Username) throws RemoteException {
        synchronized (lock) {
            try (FileWriter fileWriter = new FileWriter("MidProject/src/main/java/File/Announcement.json",false)) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("announcement", announcement);

                fileWriter.write(jsonObject.toJSONString());
                System.out.println("Announcement saved to file.");
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error saving announcement to file", e);
            }
        }
        return announcement;
    }
    /**
     * Reads the announcement from a JSON file.
     * @return The announcement read from the file.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public String readAnnouncement() throws RemoteException {
        StringBuilder content = new StringBuilder();
        try (FileReader fileReader = new FileReader("MidProject/src/main/java/File/Announcement.json")) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(fileReader);
            String announcement = (String) jsonObject.get("announcement");
            content.append(announcement);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return "Error reading the announcement file.";
        }
        return content.toString();
    }
    /**
     * Books a vehicle for the specified user.
     * @param model The model of the vehicle to be booked.
     * @param plateNo The plate number of the vehicle to be booked.
     * @param currentUser The username of the user booking the vehicle.
     * @return True if the booking is successful, otherwise False.
     * @throws RemoteException If there is an error in the remote operation.
     * @throws CannotBookOwnCarException If the user tries to book their own car.
     * @throws CarNotAvailableException If the requested vehicle is not available for booking.
     */
    @Override
    public Boolean bookVehicle(String model, String plateNo, String currentUser,Date from, Date to) throws RemoteException, CannotBookOwnCarException, CarNotAvailableException, forceLogOutException {
        synchronized (lock) {
            if (!validateSession(currentUser)) {
                throw new forceLogOutException("");
            }
            try {
                // Read car information from JSON file
                List<Cars> carsList = showCarListing(JSON_FILE_PATH_CAR_INFO);

                Cars bookedCar = null;

                // Find the car to be booked
                for (Cars car : carsList) {
                    if (car.getModel().equalsIgnoreCase(model) && car.getPlateNumber().equalsIgnoreCase(plateNo) && car.getStatus().equalsIgnoreCase("Available")) {
                        bookedCar = car;
                        break;
                    }
                }

                if (bookedCar != null) {
                    // Check if the vehicle is not owned by the current user
                    if (!bookedCar.getUserName().equalsIgnoreCase(currentUser)) {
                        //bookedCar.setStatus("Unavailable");
                        //bookedCar.setRentBy(currentUser);
                       // bookedCar.setDays(days);
                     //   writeCarInfo(carsList, JSON_FILE_PATH_CAR_INFO);
                        writeBookInfoInPending(bookedCar, currentUser,from,to,JSON_FILE_PATH_BOOKING_INFO);
                        try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt",true))) {
                            printWriter.println(model + " " + plateNo + " is pending for rent by user " + currentUser);
                        }
                        return true;
                    } else {
                        throw new CannotBookOwnCarException("Cannot book you own car");
                    }
                } else {
                    throw new CarNotAvailableException("Vehicle not available for booking.");
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Retrieves details of vehicles added by a specific user from JSON files.
     * @param targetUsername The username of the user whose vehicles are to be retrieved.
     * @return A list of strings containing details of vehicles added by the specified user.
     */
    @Override
    public List<String> getUserAddedVehicleFromJson(String targetUsername) {
        List<String> vehicleDetailsList = new ArrayList<>();

        try {
            for (String jsonFilePath : jsonFilePaths) {
                JSONArray jsonArray = (JSONArray) new JSONParser().parse(new FileReader(jsonFilePath));

                for (Object obj : jsonArray) {
                    JSONObject jsonObject = (JSONObject) obj;
                    String username = (String) jsonObject.get("Username");

                    if (targetUsername.equals(username)) {
                        int id = ((Long) jsonObject.get("ID")).intValue();
                        String model = (String) jsonObject.get("Model");
                        String status = (String) jsonObject.get("Status");
                        String plateNumber = (String) jsonObject.get("PlateNumber");
                        double price = (Double) jsonObject.get("Price");
                        String schedule = (String) jsonObject.get("Schedule");
                        String rentBy = (String) jsonObject.get("rentBy");
                        Object daysObj = jsonObject.get("TotalDays");
                        int days = daysObj != null ? ((Long) daysObj).intValue() : 0;


                        String vehicleDetails = id + "," + model + "," + status + "," + plateNumber + "," +
                                price + "," + schedule + "," + rentBy + "," + days;

                        vehicleDetailsList.add(vehicleDetails);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vehicleDetailsList;
    }
    /**
     * Retrieves details of available vehicles from JSON files.
     * @return A list of strings containing details of available vehicles.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public List<String> getVehicleDetails() throws RemoteException {
        List<String> vehicleDataList = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CAR_INFO));

            for (Object obj : jsonArray) {
                JSONObject vehicle = (JSONObject) obj;
                String status = (String) vehicle.get("Status");
                if (status.equalsIgnoreCase("Available")) {
                    int id = ((Long) vehicle.get("ID")).intValue();
                    String modelCopy = (String) vehicle.get("Model");
                    String plateNo = (String) vehicle.get("PlateNumber");
                    double price = (double) vehicle.get("Price");
                    String username = (String) vehicle.get("Username");
                    String vehicleDetails = id + "," + modelCopy + "," + status + "," + plateNo + "," +
                            price + "," + username;
                    System.out.println(vehicleDetails);
                    vehicleDataList.add(vehicleDetails);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace(); // Log exception
        }
        return vehicleDataList;
    }
    /**
     * Reads pending vehicle requests from a JSON file.
     * @return A list of strings containing details of pending vehicle requests.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public List<String> readPendingRequests() throws RemoteException {
        List<String> vehicleDataList = new ArrayList<>();
        synchronized (lock) {
            try {
                JSONParser parser = new JSONParser();
                JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_PENDING_VEHICLES));

                for (Object obj : jsonArray) {
                    JSONObject vehicle = (JSONObject) obj;
                    String status = (String) vehicle.get("Status");
                    if (status.equalsIgnoreCase("Pending")) {
                        int id = ((Long) vehicle.get("ID")).intValue();
                        String modelCopy = (String) vehicle.get("Model");
                        String plateNo = (String) vehicle.get("PlateNumber");
                        double price = (double) vehicle.get("Price");
                        String username = (String) vehicle.get("Username");
                        String schedule = (String) vehicle.get("Schedule");

                        // Format the vehicle details as a comma-separated string
                        String vehicleDetails = id + "," + modelCopy + "," + status + "," + plateNo + "," +
                                price + "," + username + "," + schedule;
                        vehicleDataList.add(vehicleDetails);
                    }
                }

            } catch (IOException | ParseException e) {
                e.printStackTrace(); // Log exception
            }
            return vehicleDataList;
        }
    }
    /**
     * Processes pending vehicle requests based on the provided decision.
     * @param vehicleID The ID of the vehicle request to be processed.
     * @param decision The decision to be made (accept or decline).
     * @return The decision made (accept or decline).
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public String processPendingRequests(String vehicleID, String decision) throws RemoteException {
       String result = "";
        synchronized (lock) {
            try {
                JSONParser parser = new JSONParser();
                JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_PENDING_VEHICLES));

                for (Object obj : jsonArray) {
                    JSONObject vehicle = (JSONObject) obj;
                    String id = String.valueOf(vehicle.get("ID"));
                    if(id != vehicleID){
                         result = "false";
                    }
                    if (id.equals(vehicleID)) {
                        // Retrieve the decision from somewhere
                        if (decision.equals("accept")) {
                            moveVehicleToCarInformation(vehicleID);
                            vehicle.put("Status", "Accepted");
                             result = "true";
                            try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt",true))) {
                                printWriter.println("Vehicle ID:" + vehicleID + " has been accepted in the car list!");
                            }
                        } else if (decision.equals("decline")) {
                            vehicle.put("Status", "Declined");
                             result = "true";
                            try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt",true))) {
                                printWriter.println("Vehicle ID:" + vehicleID + " has been declined in the car list!");
                            }
                        }
                        break;
                    }
                }
                writeJsonFile(jsonArray, JSON_FILE_PATH_PENDING_VEHICLES);
            } catch (Exception e) {
                e.printStackTrace();
                // Handle exception
            }
            return result;
        }
    }
    /**
     * Retrieves client information from a JSON file.
     * @return A list of strings containing client information.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public  List<String> getClientInfo() {
        List<String> clientDataList = new ArrayList<>();
        try {
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray) jsonParser.parse(new FileReader(JSON_FILE_PATH_CREDENTIALS));

            for (Object obj : jsonArray) {
                JSONObject client = (JSONObject) obj;
                String firstName = (String) client.get("FirstName");
                String lastName = (String) client.get("LastName");
                String username = (String) client.get("Username");
                String email = (String) client.get("Email");
                String pNumber = (String) client.get("PhoneNumber");
                String status = (String) client.get("Type");
                Boolean isBanned = (Boolean) client.get("isBanned");
                String store = "";
                if (isBanned){
                    store = "Banned";
                } else if (!isBanned) {
                    store = "not banned";
                }

                // Create a string representation of the user data
                String userDetails = String.format("%s,%s,%s,%s,%s,%s,%s", username, firstName, lastName, email, pNumber, status,store);
                clientDataList.add(userDetails);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return clientDataList;
    }
    /**
     * Searches for users based on the provided value.
     * @param value The value to search for in user data (username, first name, last name, or email).
     * @return A list of strings containing user information matching the search value.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public List<String> searchUser(String value) throws RemoteException {
        List<String> matchingUser = new ArrayList<>();

        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CREDENTIALS));

            for (Object obj : jsonArray) {
                JSONObject userObj = (JSONObject) obj;
                String username = (String) userObj.get("Username");
                String fName = (String) userObj.get("FirstName");
                String lName = (String) userObj.get("LastName");
                String email = (String) userObj.get("Email");
                String phoneNum = (String) userObj.get("PhoneNumber");
                String status = (String) userObj.get("Type");


                boolean userNameMatch = username != null && username.toLowerCase().contains(value.toLowerCase());
                boolean firstNameMatch = fName != null && fName.toLowerCase().contains(value.toLowerCase());
                boolean lastNameMatch = lName != null && lName.toLowerCase().contains(value.toLowerCase());
                boolean emailMatch = email != null && email.toLowerCase().contains(value.toLowerCase());
                boolean phoneNumMatch = phoneNum != null && phoneNum.contains(value);
                boolean statusMatch = status != null && status.toLowerCase().contains(value.toLowerCase());

                if (userNameMatch || firstNameMatch || lastNameMatch || emailMatch || phoneNumMatch || statusMatch) {
                    matchingUser.add(getUserContent(userObj));
                }
                System.out.println(phoneNumMatch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return matchingUser;
    }
    /**
     * Deletes a user from the system along with associated data.
     *
     * @param username    The username of the user to be deleted.
     * @param currentUser
     * @return True if the user is successfully deleted, otherwise false.
     * @throws RemoteException          If there is an error in the remote operation.
     * @throws AlreadyLoggedInException If the user is currently logged in and cannot be deleted.
     */
    @Override
    public boolean deleteUser(String username, String currentUser) throws RemoteException, matchAccountException {
        if (username.equals(currentUser)){
            throw new matchAccountException("");
        }
        try {
            deleteFromJson(JSON_FILE_PATH_CREDENTIALS, username);
            deleteFromJson(JSON_FILE_PATH_CAR_INFO, username);
            deleteFromJson(JSON_FILE_PATH_PENDING_VEHICLES, username);
            deleteFromJson(JSON_FILE_PATH_BOOKING_INFO, username);
            forceLogout(username);
            try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt",true))) {
                printWriter.println("Username: "+ username + " has been deleted!");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Retrieves information about cars from a JSON file.
     * @return A list of strings containing car information.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public List<String> getCarInfo() throws RemoteException {
        List<String> carDataList = new ArrayList<>();
        try {
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray) jsonParser.parse(new FileReader(JSON_FILE_PATH_CAR_INFO));

            for (Object obj : jsonArray) {
                JSONObject car = (JSONObject) obj;
                String id = String.valueOf(car.get("ID"));
                String model = (String) car.get("Model");
                String status = (String) car.get("Status");
                String plateNumber = (String) car.get("PlateNumber");
                double price = (double) car.get("Price"); // Parse as double

                String carDetails = String.format("%s,%s,%s,%s,%.2f", id, model, status, plateNumber, price);
                carDataList.add(carDetails);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return carDataList;
    }
    /**
     * Searches for vehicles based on the provided value.
     * @param value The value to search for in vehicle data (e.g., model).
     * @return A list of strings containing vehicle information matching the search value.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public List<String> searchVehicle(String value) throws RemoteException {
        List<String> matchingModels = new ArrayList<>();

        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CAR_INFO));

            for (Object obj : jsonArray) {
                JSONObject userObject = (JSONObject) obj;
                String model = (String) userObject.get("Model");

                if (model != null && model.toLowerCase().contains(value.toLowerCase())) {
                    matchingModels.add(getVehicleContent(userObject));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return matchingModels;
    }
    /**
     * Marks a booking as done and updates the status of the associated car.
     * @param ID The ID of the booking to be marked as done.
     * @return True if the booking is successfully marked as done, otherwise false.
     * @throws RemoteException If there is an error in the remote operation.
     */
    @Override
    public boolean bookingDone(int ID,String currentUser) throws RemoteException, forceLogOutException {
        if (!validateSession(currentUser)) {
            throw new forceLogOutException("");
        }
        synchronized (lock) {
            try {
                JSONParser parser = new JSONParser();
                JSONArray bookingArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_BOOKING_INFO));
              //  JSONArray carArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CAR_INFO));
                for (Object obj : bookingArray) {
                    JSONObject booking = (JSONObject) obj;
                    int bookingID = ((Long) booking.get("BookID")).intValue();
                    int vehicleId = ((Long) booking.get("ID")).intValue();
                    double price = (double) booking.get("Price");
                    double adminFee = price * 0.05;
                    if (bookingID == ID) {
                        booking.put("Status", "Done");
                        try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt",true))) {
                            printWriter.println("Vehicle ID: "+ vehicleId + " rent is done and the cut for the admin fee is automatically " +
                                    "received with total of " + adminFee + " from renter " + currentUser);
                        }
                    }
                }
                writeJsonFile(bookingArray, JSON_FILE_PATH_BOOKING_INFO);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public Boolean processBanAndUnban(String username, String decision) throws RemoteException {
        Boolean result = false;
        synchronized (lock) {
            try {
                JSONParser parser = new JSONParser();
                JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CREDENTIALS));
                for (Object obj : jsonArray) {
                    JSONObject accounts = (JSONObject) obj;
                    String user = (String) accounts.get("Username");
                    if (user.equalsIgnoreCase(username)) {
                        // Retrieve the decision from somewhere
                        if (decision.equals("Ban")) {
                            accounts.put("isBanned", true);
                            forceLogout(username);
                            result = true;
                            try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt", true))) {
                                printWriter.println("Username: " + username + " has been banned!");
                            }
                            result = true;
                        } else if (decision.equals("Unban")) {
                            accounts.put("isBanned", false);
                            result = true;
                            try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt", true))) {
                                printWriter.println("Username: " + username + " has been unbanned!");
                            }
                        }
                        break;
                    }
                }
                writeJsonFile(jsonArray, JSON_FILE_PATH_CREDENTIALS);
            } catch (Exception e) {
                e.printStackTrace();
                // Handle exception
            }
            return result;
        }
    }

    @Override
    public Boolean processPendingClient(String bookID,String vehicleID, String decision, String currentUser) throws IOException, forceLogOutException {
        Boolean result = false;
        if (!validateSession(currentUser)) {
            throw new forceLogOutException("");
        }
        synchronized (lock) {
            try {
                JSONParser parser = new JSONParser();
                JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_BOOKING_INFO));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                for (Object obj : jsonArray) {
                    JSONObject vehicle = (JSONObject) obj;
                    String id = String.valueOf(vehicle.get("BookID"));
                    String carID = String.valueOf(vehicle.get("ID"));
                    String startDate = (String) vehicle.get("StartDate");
                    String endDate = (String) vehicle.get("EndDate");
                    String rentBy = (String) vehicle.get("rentBy");
                    String status = (String) vehicle.get("Status");
                    Date rentedStartDate = dateFormat.parse(startDate);
                    Date rentedEndDate = dateFormat.parse(endDate);
                    System.out.println(carID + vehicleID);
                   if (carID.equals(vehicleID) && status.equals("Rented")){
                        if (!(rentedEndDate.before(rentedStartDate) || rentedStartDate.after(rentedEndDate))) {
                            System.out.println("Hello");
                            vehicle.put("Status", "Declined");
                            try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt", true))) {
                                printWriter.println("Vehicle ID: " + vehicleID + " is declined for rent due to overlapping dates!");
                            }
                            writeJsonFile(jsonArray, JSON_FILE_PATH_BOOKING_INFO);
                          return false;
                        }
                    }
                    if (id.equals(bookID) && "Pending".equals(vehicle.get("Status"))) {
                        if (decision.equals("accept")) {
                                vehicle.put("Status", "Rented");
                                result = true;
                                try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt", true))) {
                                    printWriter.println("Vehicle ID: " + vehicleID + " is now accepted for rent starting " + startDate + " to " + endDate + " by " +
                                            rentBy + " from renter " + currentUser);
                                }
                            } else if (decision.equals("decline")) {
                                vehicle.put("Status", "Declined");
                                result = true;
                                try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt", true))) {
                                    printWriter.println("Vehicle ID: " + vehicleID + " is declined for rent!");
                                }
                            }
                        }
                    }

                writeJsonFile(jsonArray, JSON_FILE_PATH_BOOKING_INFO);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle exception
            } catch (CarNotAvailableException e) {
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            } catch (java.text.ParseException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
            }
            return result;
        }
    }

    @Override
    public List<String> getVehicleForPendingClient(String currentUser) throws RemoteException {
            List<String> vehicleDataList = new ArrayList<>();
            try {
                JSONParser parser = new JSONParser();
                JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_BOOKING_INFO));

                for (Object obj : jsonArray) {
                    JSONObject vehicle = (JSONObject) obj;
                    String username = (String) vehicle.get("Username");
                    String status = (String) vehicle.get("Status");
                    if (status.equalsIgnoreCase("Pending")){
                        if (username.equalsIgnoreCase(currentUser)) {
                            int id = ((Long) vehicle.get("BookID")).intValue();
                            int vehicleID = ((Long) vehicle.get("ID")).intValue();
                            String modelCopy = (String) vehicle.get("Model");
                            String plateNo = (String) vehicle.get("PlateNumber");
                            double price = (double) vehicle.get("Price");
                            String startDate = (String) vehicle.get("StartDate");
                            String endDate = (String) vehicle.get("EndDate");
                            String rentBy = (String) vehicle.get("rentBy");
                            long days = (long) vehicle.get("TotalDays");
                            String vehicleDetails = id + "," + vehicleID + "," + modelCopy+","+ plateNo + "," + price + "," +
                                    startDate + "," + endDate + "," + days + "," + rentBy;
                            System.out.println(vehicleDetails);
                            vehicleDataList.add(vehicleDetails);
                        }
                    }
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace(); // Log exception
            }
            return vehicleDataList;
    }

    @Override
    public List<String> getCarInfoSpecificUser(String currentUser) throws RemoteException {
        List<String> carDataList = new ArrayList<>();
        try {
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray) jsonParser.parse(new FileReader(JSON_FILE_PATH_CAR_INFO));
            for (Object obj : jsonArray) {
                JSONObject car = (JSONObject) obj;
                String username = (String) car.get("Username");
                if (username.equalsIgnoreCase(currentUser)) {
                String id = String.valueOf(car.get("ID"));
                String model = (String) car.get("Model");
                String status = (String) car.get("Status");
                String plateNumber = (String) car.get("PlateNumber");
                double price = (double) car.get("Price"); // Parse as double
                String user = (String) car.get("Username");

                String carDetails = String.format("%s,%s,%s,%s,%.2f,%s", id, model, plateNumber, status, price, user);
                carDataList.add(carDetails);
            }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return carDataList;
    }

    @Override
    public boolean statusAvailable(int ID, String currentUser) throws RemoteException, forceLogOutException {
        if (!validateSession(currentUser)) {
            throw new forceLogOutException("");
        }
        synchronized (lock) {
            try {
                JSONParser parser = new JSONParser();
                JSONArray carArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CAR_INFO));
                for (Object obj : carArray) {
                    JSONObject car = (JSONObject) obj;
                    int bookingID = ((Long) car.get("ID")).intValue();
                    String status = (String) car.get("Status");
                    if (bookingID == ID) {
                        if (status.equalsIgnoreCase("Unavailable")) {
                            car.put("Status", "Available");
                            try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt",true))) {
                                printWriter.println("Vehicle ID: "+ ID + " is now available for rent");
                            }
                        }
                    }
                }
                try {
                    writeJsonFile(carArray, JSON_FILE_PATH_CAR_INFO);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            return true;
        }
    }
    @Override
    public boolean statusUnavailable(int ID, String currentUser) throws RemoteException, forceLogOutException {
        if (!validateSession(currentUser)) {
            throw new forceLogOutException("");
        }
        synchronized (lock) {
            try {
                JSONParser parser = new JSONParser();
                JSONArray carArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CAR_INFO));
                for (Object obj : carArray) {
                    JSONObject car = (JSONObject) obj;
                    int bookingID = ((Long) car.get("ID")).intValue();
                    String status = (String) car.get("Status");
                    if (bookingID == ID) {
                        if (status.equalsIgnoreCase("Available")) {
                            car.put("Status", "Unavailable");
                            try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt",true))) {
                                printWriter.println("Vehicle ID: "+ ID + " is set unavailable for rent");
                            }
                        }
                    }
                }
                try {
                    writeJsonFile(carArray, JSON_FILE_PATH_CAR_INFO);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    @Override
    public boolean bookingCancelled(int ID, String currentUser) throws RemoteException, forceLogOutException {
        if (!validateSession(currentUser)) {
            throw new forceLogOutException("");
        }
        synchronized (lock) {
            try {
                JSONParser parser = new JSONParser();
                JSONArray bookingArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_BOOKING_INFO));
                for (Object obj : bookingArray) {
                    JSONObject booking = (JSONObject) obj;
                    int bookingID = ((Long) booking.get("BookID")).intValue();
                    String rentBy = (String) booking.get("rentBy");
                    int vehicleId = ((Long) booking.get("ID")).intValue();
                    if (bookingID == ID) {
                        booking.put("Status", "Cancelled");
                        try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt",true))) {
                            printWriter.println("Vehicle ID: "+ vehicleId + " rent has been cancelled for user " + rentBy);
                        }
                    }
                }
                writeJsonFile(bookingArray, JSON_FILE_PATH_BOOKING_INFO);
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<String> showHistoryForOwnBooking(String currentUser) throws RemoteException {
            List<String> userBookingHistory = new ArrayList<>();
            synchronized (lock) {
                try {
                    // Parse the JSON file
                    JSONParser parser = new JSONParser();
                    JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_BOOKING_INFO));
                    // Iterate over the JSON array
                    for (Object obj : jsonArray) {
                        JSONObject jsonObject = (JSONObject) obj;
                        // Extract the username from the JSON object
                        String username = (String) jsonObject.get("rentBy");
                        // Check if the username matches the current user
                        if (username.equals(currentUser)) {
                            // Extract other required fields
                            Long vehicleID = (Long) jsonObject.get("ID");
                            String model = (String) jsonObject.get("Model");
                            String status = (String) jsonObject.get("Status");
                            String plateNumber = (String) jsonObject.get("PlateNumber");
                            Double price = (Double) jsonObject.get("Price");
                            String startDate = (String) jsonObject.get("StartDate");
                            String endDate = (String) jsonObject.get("EndDate");
                            Long days = (Long) jsonObject.get("TotalDays");
                            String rentTo = (String) jsonObject.get("Username");
                            JSONArray users = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CREDENTIALS));
                            String PhoneNumber = null;
                            for (Object objs : users) {
                                JSONObject userObject = (JSONObject) objs;
                                String storedUsername = (String) userObject.get("Username");
                                if (storedUsername.equals(rentTo)) {
                                    PhoneNumber = (String) userObject.get("PhoneNumber");
                                }
                            }
                            // Construct the booking details string
                            String bookingDetails = String.format("%d,%s,%s,%s,%.2f,%s,%s,%s,%d,%s",
                                    vehicleID, model, status, plateNumber, price, rentTo,startDate,endDate, days,PhoneNumber);
                            userBookingHistory.add(bookingDetails);
                        }
                    }
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                    // Handle file IO exception or JSON parsing exception
                }
            }
            return userBookingHistory;

    }

    @Override
    public List<String[]> getRentedDates(String plateNumber) throws RemoteException {
            List<String[]> rentedDates = new ArrayList<>();
            try {
                JSONParser parser = new JSONParser();
                JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_BOOKING_INFO));
                for (Object obj : jsonArray) {
                    JSONObject jsonObject = (JSONObject) obj;
                    String status = (String) jsonObject.get("Status");
                    String id = (String) jsonObject.get("PlateNumber");
                    if ("Rented".equals(status) && id.equals(plateNumber)) {
                        String startDate = (String) jsonObject.get("StartDate");
                        String endDate = (String) jsonObject.get("EndDate");
                        rentedDates.add(new String[]{startDate, endDate});
                    }
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            return rentedDates;

    }



    public void forceLogout(String username) throws RemoteException, forceLogOutException {
        if (activeSessions.containsKey(username)) {
            activeSessions.remove(username);
        }
    }


    /**
     * Creates a new session for the specified username.
     * @param username The username for which to create the session.
     * @return True if the session was successfully created, otherwise false.
     */
    public static synchronized boolean createSession(String username) {
        if (!activeSessions.containsKey(username)) {
            activeSessions.put(username, true);
            return true;
        }
        return false; // Session already exists for the user
    }
    /**
     * Checks if a session exists for the given username.
     * @param username The username to check for an active session.
     * @return True if a session exists for the given username, otherwise false.
     */
    public static synchronized boolean validateSession(String username) {
        return activeSessions.containsKey(username) && activeSessions.get(username);
    }
    /**
     * Closes the session for the specified username.
     * @param username The username for which to close the session.
     * @return True if the session was successfully closed, otherwise false.
     */
    public synchronized boolean closeSession(String username) {
        if (activeSessions.containsKey(username)) {
            activeSessions.remove(username);
            return true; // Session was found and closed
        }
        return false; // No session found for the user
    }
    /**
     * Enum representing the result of a login attempt.
     */
    enum LoginResult {
        clientLogIn,
        AdminLogIn,
        InvalidLogIn,
        BannedAccount
    }
    /**
     * Logs in a user and determines the type of login (client, admin, or invalid).
     * @param username The username of the user attempting to log in.
     * @param password The password provided by the user attempting to log in.
     * @return The type of login result (client, admin, or invalid).
     */
    public static LoginResult logInvalidation(String username, String password) {
        try {
            JSONParser parser = new JSONParser();
            JSONArray users = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CREDENTIALS));
            for (Object obj : users) {
                JSONObject user = (JSONObject) obj;
                String userType = (String) user.get("Type");
                String storedUsername = (String) user.get("Username");
                String storedPassword = (String) user.get("Password");
                Boolean isBanned = (Boolean) user.get("isBanned");
                if (storedUsername.equals(username) && storedPassword.equals(password)) {
                    if (isBanned != null && isBanned) {
                        return LoginResult.BannedAccount;
                    }
                    if (userType.equals("Client")) {
                        return LoginResult.clientLogIn;
                    } else if (userType.equals("Admin")) {
                        return LoginResult.AdminLogIn;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return LoginResult.InvalidLogIn;
    }
    /**
     * Checks if a username is available for registration.
     * @param username The username to check for availability.
     * @return True if the username is available, otherwise false.
     */
    public static boolean checkRegistration(String username) {
        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CREDENTIALS));

            for (Object obj : jsonArray) {
                JSONObject user = (JSONObject) obj;
                String storedUsername = (String) user.get("Username");
                if (storedUsername.equals(username)) {
                    return false; // Username already exists
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return true; // Username does not exist
    }
    /**
     * Adds a new user to the JSON file containing user credentials.
     *
     * @param firstName   The first name of the user.
     * @param lastName    The last name of the user.
     * @param username    The username of the user.
     * @param password    The password of the user.
     * @param email       The email address of the user.
     * @param phoneNumber The phone number of the user.
     */
    public static void addUserToJson(String firstName, String lastName, String username, String password, String email, String phoneNumber) {
        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonArray;
            JSONObject userObject = new JSONObject();
            userObject.put("Type", "Client");
            userObject.put("FirstName", firstName);
            userObject.put("LastName", lastName);
            userObject.put("Username", username);
            userObject.put("Password", password);
            userObject.put("Email", email);
            userObject.put("PhoneNumber", phoneNumber);
            userObject.put("isBanned", false);

            // Read existing JSON array or create a new one
            try (FileReader reader = new FileReader(JSON_FILE_PATH_CREDENTIALS)) {
                jsonArray = (JSONArray) parser.parse(reader);
            } catch (IOException | ParseException e) {
                jsonArray = new JSONArray();
            }

            // Add new user object to the array
            jsonArray.add(userObject);

            // Write updated JSON array back to the file
            try (FileWriter writer = new FileWriter(JSON_FILE_PATH_CREDENTIALS)) {
                writer.write(jsonArray.toJSONString());
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Retrieves a list of cars from the JSON file specified by the filePath parameter.
     *
     * @param filePath The path to the JSON file containing car information.
     * @return A list of Cars objects representing car details.
     */
    public static List<Cars> showCarListing(String filePath) {
        try {
            // Read the JSON file as a string
            JSONParser parser = new JSONParser();

            // Parse the JSON string
            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(filePath));

            List<Cars> carsList = new ArrayList<>();

            // Iterate through the JSON array
            for (Object obj : jsonArray) {
                JSONObject carObject = (JSONObject) obj;

                // Extract car details from JSON object
                int ID = ((Long) carObject.get("ID")).intValue();
                String model = (String) carObject.get("Model");
                String status = (String) carObject.get("Status");
                String plateNumber = (String) carObject.get("PlateNumber");
                double price = (double) carObject.get("Price");

                String username = (String) carObject.get("Username");

                Cars car = new Cars(ID, model, status, plateNumber, price, username);
                carsList.add(car);
            }

            return carsList;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Writes booking information to the booking JSON file.
     *
     * @param bookedCar   The car that has been booked.
     * @param currentUser The username of the user who made the booking.
     * @param from
     * @param to
     * @param filePath    The path to the booking JSON file.
     * @throws IOException If an I/O error occurs while writing the booking information.
     */
    public static void writeBookInfoInPending(Cars bookedCar, String currentUser, Date from, Date to, String filePath) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject bookingObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());
        String fromBook = dateFormat.format(new Date(String.valueOf(from)));
        String ToBook = dateFormat.format(new Date(String.valueOf(to)));
        long days = ((to.getTime() - from.getTime()) / (1000 * 60 * 60 * 24));
        if (fromBook.equals(ToBook)) {
            days = 1;
        }
        int bookID = generateUniqueID(jsonArray);
        bookingObject.put("BookID",bookID);
        bookingObject.put("ID", bookedCar.getID());
        bookingObject.put("Model", bookedCar.getModel());
        bookingObject.put("Status", "Pending");
        bookingObject.put("PlateNumber", bookedCar.getPlateNumber());
        bookingObject.put("Price", bookedCar.getPrice() * days);
        bookingObject.put("Username", bookedCar.getUserName());
        bookingObject.put("rentBy", currentUser);
        bookingObject.put("Schedule", currentDate);
        bookingObject.put("TotalDays", days);
        bookingObject.put("StartDate", fromBook);
        bookingObject.put("EndDate", ToBook);
        JSONArray users = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CREDENTIALS));
        String phoneNumber = null;
        for (Object obj : users) {
            JSONObject userObject = (JSONObject) obj;
            String storedUsername = (String) userObject.get("Username");

            if (storedUsername.equals(currentUser)) {
                phoneNumber = (String) userObject.get("PhoneNumber");
            }
        }
        bookingObject.put("PhoneNumber", phoneNumber);
        // Write booking information to booking JSON file
        try (FileReader reader = new FileReader(filePath)) {
            jsonArray = (JSONArray) parser.parse(reader);
        } catch (IOException | ParseException e) {
            jsonArray = new JSONArray();
        }
        jsonArray.add(bookingObject);


        try (FileWriter writer = new FileWriter(JSON_FILE_PATH_BOOKING_INFO)) {
            writer.write(jsonArray.toJSONString());
            writer.flush();
        }
    }
    /**
     * Writes car information to the JSON file specified by the filePath parameter.
     *
     * @param carsList A list of Cars objects containing car information.

     * @param filePath The path to the JSON file where car information will be written.
     * @throws IOException If an I/O error occurs while writing the car information.
     */
    public static void writeCarInfo(List<Cars> carsList, String rentBy, int days, int vehicleID, String filePath) throws IOException {
        // Read existing car information from JSON file
        JSONArray jsonArray = new JSONArray();
        JSONParser parser = new JSONParser();
        try (FileReader fileReader = new FileReader(filePath)) {
            jsonArray = (JSONArray) parser.parse(fileReader);
            for (Object obj : jsonArray) {
                JSONObject carObject = (JSONObject) obj;
                int carID = Integer.parseInt(String.valueOf(carObject.get("ID")));
                if (carID == vehicleID) {
                    // Modify car information
                    carObject.put("Status", "Unavailable");
                    carObject.put("rentBy", rentBy);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String currentDate = dateFormat.format(new Date());
                    carObject.put("Schedule", currentDate);
                    carObject.put("TotalDays", days);
                    break;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonArray.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Checks if a given plate number exists in the JSON array.
     *
     * @param jsonArray   The JSON array to search for plate numbers.
     * @param plateNumber The plate number to check for existence.
     * @return True if the plate number exists in the JSON array, false otherwise.
     */
    private static boolean isPlateNumberExists(JSONArray jsonArray, String plateNumber) {
        for (Object obj : jsonArray) {
            JSONObject vehicleObject = (JSONObject) obj;
            String currentPlateNumber = (String) vehicleObject.get("PlateNumber");
            if (currentPlateNumber.equalsIgnoreCase(plateNumber)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Checks if a given plate number is pending in the JSON array.
     *
     * @param jsonArray   The JSON array to search for pending plate numbers.
     * @param plateNumber The plate number to check for pending status.
     * @return True if the plate number is pending in the JSON array, false otherwise.
     */
    private static boolean isPlateNumberPending(JSONArray jsonArray, String plateNumber) {
        for (Object obj : jsonArray) {
            JSONObject vehicleObject = (JSONObject) obj;
            String currentPlateNumber = (String) vehicleObject.get("PlateNumber");
            String currentStatus = (String) vehicleObject.get("Status");
            if (currentPlateNumber.equals(plateNumber) && currentStatus.equals("Pending")) {
                return true;
            }
        }
        return false;
    }
    /**
     * Generates a unique ID that does not already exist in the provided JSON array.
     *
     * @param jsonArray The JSON array to check for existing IDs.
     * @return A unique ID that is not present in the JSON array.
     */
    private static int generateUniqueID(JSONArray jsonArray) {
        int uniqueID;
        do {
            uniqueID = random.nextInt(10000) + 1;
        } while (isIDExistsInArray(jsonArray, uniqueID));

        return uniqueID;
    }
    /**
     * Checks if a given ID exists in the JSON array.
     *
     * @param jsonArray The JSON array to search for IDs.
     * @param id        The ID to check for existence.
     * @return True if the ID exists in the JSON array, false otherwise.
     */
    private static boolean isIDExistsInArray(JSONArray jsonArray, int id) {
        for (Object obj : jsonArray) {
            JSONObject vehicleObject = (JSONObject) obj;
            long currentID = (long) vehicleObject.get("ID");
            if (currentID == id) {
                return true;
            }
        }
        return false;
    }
    /**
     * Finds the ID of a pending vehicle with the specified model and plate number.
     *
     * @param model   The model of the vehicle to search for.
     * @param plateNo The plate number of the vehicle to search for.
     * @return The ID of the pending vehicle, or null if not found.
     */
    private static String findPendingVehicleID(String model, String plateNo) {
        try {
            List<String> jsonFilePaths = Arrays.asList(JSON_FILE_PATH_CAR_INFO, JSON_FILE_PATH_PENDING_VEHICLES);
            for (String jsonFilePath : jsonFilePaths) {
                JSONArray jsonArray = readJsonFile(jsonFilePath);

                for (Object obj : jsonArray) {
                    JSONObject jsonObject = (JSONObject) obj;
                    String vehicleModel = (String) jsonObject.get("Model");
                    String vehiclePlateNo = (String) jsonObject.get("PlateNumber");
                    if (vehicleModel.equals(model) && vehiclePlateNo.equals(plateNo)) {
                        return String.valueOf(jsonObject.get("ID"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Deletes a pending vehicle with the specified ID.
     *
     * @param id The ID of the pending vehicle to delete.
     */
    private static void deletePendingVehicle(String id) {
        try {
            List<String> jsonFilePaths = Arrays.asList(JSON_FILE_PATH_CAR_INFO, JSON_FILE_PATH_PENDING_VEHICLES);
            for (String jsonFilePath : jsonFilePaths) {
                JSONArray jsonArray = readJsonFile(jsonFilePath);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    if (id.equals(jsonObject.get("ID").toString())) {
                        jsonArray.remove(i);
                        writeJsonFile(jsonArray, jsonFilePath);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: Failed to delete vehicle.");
        }
    }
    /**
     * Reads a JSON file and returns its contents as a JSON array.
     *
     * @param filePath The path to the JSON file to read.
     * @return The contents of the JSON file as a JSON array.
     * @throws Exception If an error occurs while reading the JSON file.
     */
    private static JSONArray readJsonFile(String filePath) throws Exception {
        JSONParser parser = new JSONParser();
        FileReader reader = new FileReader(filePath);
        return (JSONArray) parser.parse(reader);
    }
    /**
     * Writes a JSON array to a file specified by the filePath parameter.
     *
     * @param jsonArray The JSON array to write to the file.
     * @param filePath  The path to the file where the JSON array will be written.
     * @throws Exception If an error occurs while writing the JSON file.
     */
    private static void writeJsonFile(JSONArray jsonArray, String filePath) throws Exception {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonArray.toJSONString());
        }
    }
    /**
     * Moves a pending vehicle with the specified ID to the car information JSON file.
     *
     * @param vehicleID The ID of the pending vehicle to move.
     */
    private static void moveVehicleToCarInformation(String vehicleID) {
        try {
            JSONParser parser = new JSONParser();
            JSONArray pendingRequestsArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_PENDING_VEHICLES));
            JSONArray carInfoArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CAR_INFO));

            JSONObject vehicleToRemove = null;
            for (Object obj : pendingRequestsArray) {
                JSONObject vehicle = (JSONObject) obj;
                String id = String.valueOf(vehicle.get("ID"));
                if (id.equals(vehicleID)) {
                    vehicleToRemove = vehicle;
                    break;
                }
            }
            if (vehicleToRemove != null) {
                vehicleToRemove.put("Status", "Available");
                carInfoArray.add(vehicleToRemove);
               //pendingRequestsArray.remove(vehicleToRemove);
            } else {
                throw new Exception("Vehicle with ID " + vehicleID + " not found in pending requests.");
            }

          writeJsonFile(pendingRequestsArray,JSON_FILE_PATH_PENDING_VEHICLES);
            writeJsonFile(carInfoArray,JSON_FILE_PATH_CAR_INFO);
        } catch ( Exception e) {
            e.printStackTrace();
            // Handle exception
        }
    }

    /**
     * Gets a formatted string representation of vehicle details from a JSON object.
     *
     * @param car The JSON object containing vehicle details.
     * @return A formatted string containing vehicle details.
     */
    private static String getVehicleContent(JSONObject car) {
        String id = String.valueOf(car.get("ID"));
        String model = (String) car.get("Model");
        String status = (String) car.get("Status");
        String plateNumber = (String) car.get("PlateNumber");
        double price = (double) car.get("Price"); // Parse as double
        String user = (String) car.get("Username");
        return String.format("%s,%s,%s,%s,%.2f,%s", id, model, status, plateNumber, price, user);
    }
    /**
     * Gets a formatted string representation of user details from a JSON object.
     *
     * @param userObject The JSON object containing user details.
     * @return A formatted string containing user details.
     */
    private static String getUserContent(JSONObject userObject) {
        String firstName = (String) userObject.get("FirstName");
        String lastName = (String) userObject.get("LastName");
        String email = (String) userObject.get("Email");
        String username = (String) userObject.get("Username");
        String phoneNum = (String) userObject.get("PhoneNumber");
        String status = (String) userObject.get("Type");

        return String.format( username +"," + firstName +"," + lastName +"," + email + "," + phoneNum +"," + status);
    }
    /**
     * Deletes a user from a JSON file based on the provided username.
     *
     * @param filePath The path to the JSON file containing user information.
     * @param username The username of the user to delete.
     * @throws IOException            If an I/O error occurs while deleting the user.
     * @throws ParseException         If an error occurs while parsing the JSON file.
     */
    private static void deleteFromJson(String filePath, String username) throws IOException, ParseException {
        File file = new File(filePath);
        JSONParser parser = new JSONParser();

        try (FileReader fileReader = new FileReader(file)) {
            JSONArray jsonArray = (JSONArray) parser.parse(fileReader);

            // Create a new JSON array to store non-matching entries
            JSONArray updatedArray = new JSONArray();

            // Iterate over the original JSON array
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                String storedUsername = (String) jsonObject.get("Username");

                // Check if the username matches the one to be deleted
                if (!storedUsername.equalsIgnoreCase(username)) {
                    // If the username doesn't match, add the entry to the updated array
                    updatedArray.add(jsonObject);
                }
            }

            // Write the updated JSON array back to the file
            saveJsonArray(updatedArray, filePath);
        }
    }
    /**
     * Writes a JSON array to a file specified by the filePath parameter.
     *
     * @param jsonArray The JSON array to write to the file.
     * @param filePath  The path to the file where the JSON array will be written.
     * @throws IOException If an I/O error occurs while writing the JSON file.
     */
    private static void saveJsonArray(JSONArray jsonArray, String filePath) throws IOException {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonArray.toJSONString());
        }
    }

    @Override
    public List<String> getPurchaseHistoryInfo() throws RemoteException {
        List<String> purchaseHistoryDataList = new ArrayList<>();
        try {
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray) jsonParser.parse(new FileReader(JSON_FILE_PATH_BOOKING_INFO));

            for (Object obj : jsonArray) {
                JSONObject car = (JSONObject) obj;
                String status = (String) car.get("Status");
                if(status.equalsIgnoreCase("Done")) {
                    String username = (String) car.get("Username");
                    String id = String.valueOf(car.get("ID"));
                    String model = (String) car.get("Model");
                    String plateNumber = (String) car.get("PlateNumber");
                    double price = (double) car.get("Price");
                    String dateBooked = (String) car.get("StartDate");
                    int days = (int) (long) car.get("TotalDays");
                    double adminFee = price * 0.05;
                    String purchaseHistoryList = String.format("%s,%s,%s,%s,%s,%.2f,%s,%d,%.2f", username, id, model, status, plateNumber, price, dateBooked, days, adminFee);
                    purchaseHistoryDataList.add(purchaseHistoryList);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return purchaseHistoryDataList;
    }

    @Override
    public String addVehicleAdmin(String name, String plateNumber, Date schedule, Double price, String username) throws RemoteException, UsernameExistsException {
        String status = "Pending";
        try {
            // Parse the JSON file
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(JSON_FILE_PATH_PENDING_VEHICLES));
            Object obj1 = parser.parse(new FileReader(JSON_FILE_PATH_CAR_INFO));
            JSONArray jsonArray = (JSONArray) obj;
            JSONArray jsonArray1 = (JSONArray) obj1;
            JSONArray jsonArray2 = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CREDENTIALS));
            int verify = 0;
            for (Object obj2 : jsonArray2) {
                JSONObject user = (JSONObject) obj2;
                String targetUser = (String) user.get("Username");
                if (targetUser.equals(username)) {
                    verify = 1;
                    break;
                }
            }
            if (verify == 0){
                jsonArray2.clear();
                throw new UsernameExistsException("");
            }
                // Check if plate number already exists with Pending status
                if (isPlateNumberExists(jsonArray, plateNumber) || isPlateNumberExists(jsonArray1, plateNumber)) {
                    jsonArray2.clear();
                    throw new ExistingVehicleException("PlateNumber exists");
                }

            int uniqueID = generateUniqueID(jsonArray);

            // Create a new JSON object for the new vehicle
            JSONObject vehicleObject = new JSONObject();
            vehicleObject.put("ID", uniqueID);
            vehicleObject.put("Model", name);
            vehicleObject.put("PlateNumber", plateNumber);
            vehicleObject.put("Status", status);
            vehicleObject.put("Price", price);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            vehicleObject.put("Schedule", sdf.format(schedule));
            vehicleObject.put("Username", username);

            // Add the new vehicle object to the JSON array
            jsonArray.add(vehicleObject);

            // Write the updated JSON array back to the file
            FileWriter fileWriter = new FileWriter(JSON_FILE_PATH_PENDING_VEHICLES);
            fileWriter.write(jsonArray.toJSONString());
            fileWriter.flush();
            fileWriter.close();
            try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt", true))) {
                printWriter.println(username + " Added a vehicle " + name + " with a platenumber " + plateNumber);
            }
            {
                return "Vehicle added successfully.";
            }
        } catch (ExistingVehicleException e) {
            return "PlateNumber exists";
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return "Error occurred while adding the vehicle.";
        }
    }

    @Override
    public Boolean bookVehicleAdmin(String model, String plateNo, String username,Date from, Date to) throws RemoteException, CannotBookOwnCarException, CarNotAvailableException, UsernameExistsException {
        synchronized (lock) {
            JSONArray jsonArray = null;
            try {
                JSONParser parser = new JSONParser();
                jsonArray = (JSONArray) parser.parse(new FileReader(JSON_FILE_PATH_CREDENTIALS));
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
            int verify = 0;
            for (Object obj2 : jsonArray) {
                JSONObject user = (JSONObject) obj2;
                String targetUser = (String) user.get("Username");
                if (targetUser.equals(username)) {
                    verify = 1;
                    break;
                }
            }
            if (verify == 0){
                jsonArray.clear();
                throw new UsernameExistsException("");
            }
            try {
                // Read car information from JSON file
                List<Cars> carsList = showCarListing(JSON_FILE_PATH_CAR_INFO);

                Cars bookedCar = null;

                // Find the car to be booked
                for (Cars car : carsList) {
                    if (car.getModel().equalsIgnoreCase(model) && car.getPlateNumber().equalsIgnoreCase(plateNo) && car.getStatus().equalsIgnoreCase("Available")) {
                        bookedCar = car;
                        break;
                    }
                }

                if (bookedCar != null) {
                    // Check if the vehicle is not owned by the current user
                    if (!bookedCar.getUserName().equalsIgnoreCase(username)) {
                        writeBookInfoInPending(bookedCar, username,from,to,JSON_FILE_PATH_BOOKING_INFO);
                        try (PrintWriter printWriter = new PrintWriter(new FileWriter("MidProject/src/main/java/File/activities.txt",true))) {
                            printWriter.println(model + " " + plateNo + " is pending for rent by user " + username);
                        }
                        return true;
                    } else {
                        throw new CannotBookOwnCarException("Cannot book you own car");
                    }
                } else {
                    throw new CarNotAvailableException("Vehicle not available for booking.");
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public String readActivitiesFromFile() throws RemoteException {
        StringBuilder activityLog = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("MidProject/src/main/java/File/activities.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                activityLog.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return activityLog.toString();
    }
}