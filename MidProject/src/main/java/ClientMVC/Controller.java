package ClientMVC;


import ClientMVC.View.*;
import Utilities.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class Controller extends Component {
    private static Controller instance;
    private Model model;
    private SignInView signInView;
    private SignUpView signUpView;
    private WelcomeFrame welcomeFrame;
    private HomepageView homepageView;
    private SearchVehicleView searchVehicleView;
    private ManageProfileView manageProfile;
    private AddVehicleView addVehicleView;
    private AdminHomeView adminHomeView;
    private HistoryView historyView;
    private BookingView booking;
    private BookingStatus bookingStatus;
    private PendingRequestView pendingRequestView;
    private VehicleStatus vehicleStatus;


    public Controller() {
        this.model = new Model();
        this.signInView = new SignInView();
        this.signUpView = new SignUpView(null);
        this.welcomeFrame = new WelcomeFrame();
        this.homepageView = new HomepageView();
        this.searchVehicleView = new SearchVehicleView();
        this.addVehicleView = new AddVehicleView();
        this.manageProfile = new ManageProfileView();
        this.adminHomeView = new AdminHomeView();
        this.historyView = new HistoryView();
        this.booking = new BookingView();
        this.bookingStatus = new BookingStatus();
        this.pendingRequestView = new PendingRequestView();
        this.vehicleStatus = new VehicleStatus();
    }


    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }
    /**
     * Initiates the login process by sending the provided username and password to the model for authentication.
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     */
    public void logIn(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please Enter All Fields", "Try Again", JOptionPane.ERROR_MESSAGE);
        }
        try{
            String logInSuccessful = model.logInToServer(username, password);
            if (logInSuccessful.equals("Login successful!")) {
                JOptionPane.showMessageDialog(null, "Successful Login.");
                signInView.dispose();
                showHome();
            } else if (logInSuccessful.equals("Admin login successful!")) {
                JOptionPane.showMessageDialog(null, "Admin login successful!");
                signInView.dispose();
                showAdminHome();
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Error connecting to the server.");
        } catch (InvalidInputCredentialsExceptions e) {
            JOptionPane.showMessageDialog(null, "Invalid username or password.");
        } catch (AlreadyLoggedInException e) {
            JOptionPane.showMessageDialog(null, "Error: User is already logged in.");
        } catch (BannedAccountException e) {
            JOptionPane.showMessageDialog(null, "Your Account is currently Banned");
        }
    }

    /**
     * Handles the user registration process by sending user details to the model for server communication.
     *
     * @param firstName       User's first name.
     * @param lastName        User's last name.
     * @param userName        Desired username.
     * @param email           User's email address.
     * @param password        Desired password.
     * @param retypePassword  Password confirmation.
     * @param phoneNumber     User's phone number.
     */
    public void registerUser(String firstName, String lastName, String userName, String email,
                             String password, String retypePassword, String phoneNumber) {
        firstName = firstName.trim();
        lastName = lastName.trim();

        if (firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty() || userName.isEmpty() || email.isEmpty() || password.isEmpty() || retypePassword.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please Enter All Fields", "Try Again", JOptionPane.ERROR_MESSAGE);
            return ;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(null, "The password should be at least 6 characters long", "Try Again", JOptionPane.ERROR_MESSAGE);
            return ;
        }
        if (!email.matches(".+@.+\\..+")) {
            JOptionPane.showMessageDialog(null, "Invalid email format. Please enter a valid email address.", "Try Again", JOptionPane.ERROR_MESSAGE);
            return ;
        }

        if (phoneNumber.length() != 11) {
            JOptionPane.showMessageDialog(null, "Invalid PhoneNumber format! It should been a 11 number phoneNumber format", "Try Again", JOptionPane.ERROR_MESSAGE);
            return ;
        }
        // Check if passwords match
        if (!password.equals(retypePassword)) {
            JOptionPane.showMessageDialog(null, "Passwords do not match. Please retype the password correctly.", "Try Again", JOptionPane.ERROR_MESSAGE);
            return ;
        }
        try{
            model.registerUser(firstName, lastName, userName, email, password, phoneNumber);
            JOptionPane.showMessageDialog(null, "Registration Successful!");
            signUpView.dispose();
            showSignInView();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error communicating with the server.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
        } catch (UsernameExistsException e) {
            JOptionPane.showMessageDialog(null, "Username already exists! Please try another username", "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Initiates a search based on user input and updates the view with search results.
     */
    public void handleSearchButton() {
        String searchAttribute = searchVehicleView.getSearch().getText();

        // Check if the search attribute is not empty before initiating the search
        if (!searchAttribute.isEmpty()) {
            SwingWorker<java.util.List<String>, Void> worker = new SwingWorker<>() {
                @Override
                protected java.util.List<String> doInBackground() {
                    java.util.List<String> results = new ArrayList<>();
                    if (searchAttribute.isEmpty()) {  // Check if the search value is not empty
                        JOptionPane.showMessageDialog(null, "Search value is empty.", "Search Failed", JOptionPane.INFORMATION_MESSAGE);
                    }
                    String response = null;
                    try {
                        response = String.valueOf(model.searchVehicle(searchAttribute, searchAttribute));
                    } catch (IOException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error communicating with the server.", "Search Failed", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Unexpected error during the search.", "Search Failed", JOptionPane.ERROR_MESSAGE);
                    }
                    if (!response.isEmpty()) {
                        // Split the response by newline character to handle multiple results
                        String[] resultArray = response.substring(1, response.length() - 1).split("\\], \\[");
                        // Loop through each result and add it to the results list
                        for (String result : resultArray) {
                            results.add(result);
                            results.add("\n");
                        }
                    }
                    if (response.contains("No matching vehicles found.")) {
                        JOptionPane.showMessageDialog(null, "No Matching Vehicles found", "Search Failed", JOptionPane.INFORMATION_MESSAGE);
                    }
                    return results;
                }

                @Override
                protected void done() {
                    try {
                        List<String> results = get();
                        SwingUtilities.invokeLater(() -> {
                            if (results.contains("No matching vehicles found.")) {
                                DefaultTableModel tableModel = (DefaultTableModel) searchVehicleView.getSearchOutput().getModel();
                                tableModel.setRowCount(0);
                            } else {
                                updateSearchResults(results);
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            };
            worker.execute();
        } else {
            JOptionPane.showMessageDialog(null, "Search attribute is empty.", "Search Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    /**
     * Updates the search results view with the provided list of vehicle information.
     * @param results List of strings representing vehicle information to display.
     */
    private void updateSearchResults(List<String> results) {
        DefaultTableModel tableModel = (DefaultTableModel) searchVehicleView.getSearchOutput().getModel();
        tableModel.setRowCount(0); // Clear existing rows

        if (results != null && !results.isEmpty()) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<SearchResult>>(){}.getType();
            List<SearchResult> searchResults = gson.fromJson(results.toString(), listType);

            for (SearchResult result : searchResults) {
                if (result != null) { // Add null check here
                    tableModel.addRow(new Object[]{
                            result.getID(),
                            result.getModel(),
                            result.getStatus(),
                            result.getPlateNumber(),
                            result.getPrice(),
                            result.getUsername()
                    });
                }
            }
        } else {
            // If no matching entries found, display a message in the table
            tableModel.setColumnIdentifiers(new Object[]{"No matching entries found."});
        }
    }


    /**
     * Initiates the booking process for a specified vehicle and user.
     *
     * @param Model   The model of the vehicle to be booked.
     * @param plateNo The plate number of the vehicle to be booked.
     * @param from
     * @param to
     * @return True if the booking was successful, false otherwise.
     */
    public Boolean bookForPendingVehicle(String Model, String plateNo, Date from, Date to) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formatter = dateFormat.format(new Date());
        Date currentDate = new Date();
        try {
            currentDate = dateFormat.parse(formatter);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

            if (from == null || to == null) {
                JOptionPane.showMessageDialog(null, "Please enter valid values! all fields shouldn't be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (from.after(to)) {
                JOptionPane.showMessageDialog(null, "The start date shouldn't be greater than the end date ", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (from.before(currentDate)) {
                JOptionPane.showMessageDialog(null, "You can't book in past dates! ", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }

        try {
            List<String[]> rentedDates = model.getDates(plateNo);
            // Check for dates  with existing rentals
            for (String[] dates : rentedDates) {
                Date rentedStartDate = dateFormat.parse(dates[0]);
                Date rentedEndDate = dateFormat.parse(dates[1]);
                if (!(to.before(rentedStartDate) || from.after(rentedEndDate))) {
                    JOptionPane.showMessageDialog(null, "Selected date is unavailable!", "Booking Failed", JOptionPane.ERROR_MESSAGE);
                    StringBuilder message = new StringBuilder("Unavailable dates:\n");
                    for (String[] date : rentedDates) {
                        message.append(date[0]).append(" to ").append(date[1]).append("\n");
                    }
                    JOptionPane.showMessageDialog(null, message.toString(), "Unavailable Dates", JOptionPane.WARNING_MESSAGE);
                    return null;
                }
            }

            try {
                boolean bookingResult = model.bookForPendingVehicle(Model, plateNo, from, to);

                if (bookingResult) {
                    JOptionPane.showMessageDialog(null, "Vehicle booked successfully.\n Please wait for the approval of the renter");
                    getVehicles();
                    return true;
                }

            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error communicating with the server.", "Booking Failed", JOptionPane.ERROR_MESSAGE);
            } catch (CannotBookOwnCarException e) {
                JOptionPane.showMessageDialog(null, "You cannot book your own car.", "Booking Failed", JOptionPane.ERROR_MESSAGE);
            } catch (CarNotAvailableException e) {
                JOptionPane.showMessageDialog(null, "Vehicle not available for booking.", "Booking Failed", JOptionPane.ERROR_MESSAGE);
            } catch (forceLogOutException e) {
                JOptionPane.showMessageDialog(null, "Can't do any action because you have been forced log out by the server", "Forced Log Out", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Adds a new vehicle to the system with the provided details.
     *
     * @param Model       The model of the vehicle.
     * @param PlateNumber The plate number of the vehicle.
     * @param Price       The price of the vehicle.
     */
    public void addVehicle(String Model, String PlateNumber, double Price) {
        if (Model.isEmpty() || PlateNumber.isEmpty()  || Price < 0) {
            JOptionPane.showMessageDialog(null, "Please enter valid values! all fields shouldn't be empty and price can't be lower than 0", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String response = null;
        try {
            response = model.addVehicle(Model, PlateNumber, Price);
            if (response.equals("Vehicle added successfully.")) {
                JOptionPane.showMessageDialog(null, "Vehicle added successfully.\nPlease wait for approval by the administrator.", "Update", JOptionPane.INFORMATION_MESSAGE);
            } else if (response.equals("PlateNumber exists")) {
                JOptionPane.showMessageDialog(null, "Plate number already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }  else {
                JOptionPane.showMessageDialog(null, response, "Error", JOptionPane.ERROR_MESSAGE);
            }
            getAddedVehicles();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (forceLogOutException e) {
            JOptionPane.showMessageDialog(null, "Can't do any action because you have been forced log out by the server", "Forced Log Out", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Cancels the booking for a specified vehicle.
     *
     * @param vehicleId   The ID of the vehicle to cancel.
     * @param vehicleModel The model of the vehicle to cancel.
     * @return True if the cancellation was successful, false otherwise.
     */
    public boolean cancelVehicle(String vehicleId, String vehicleModel) {
        try {
            boolean response = model.cancelVehicle(vehicleId, vehicleModel);
            // Display the cancellation result using JOptionPane
            if (response) {
                JOptionPane.showMessageDialog(null, "Vehicle cancelled successfully!", "Cancellation Result", JOptionPane.INFORMATION_MESSAGE);
                getAddedVehicles();
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Error: Unable to cancel vehicle booking", "Cancellation Result", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (forceLogOutException e) {
            JOptionPane.showMessageDialog(null, "Can't do any action because you have been forced log out by the server", "Forced Log Out", JOptionPane.ERROR_MESSAGE);
return false;
        } catch (Exception e) {
            System.err.println("Error during cancellation for Vehicle ID: " + vehicleId);
            e.printStackTrace(); // Log the exception for debugging
            return false;
        }
    }
    /**
     * Retrieves the list of added vehicles from the server and updates the view.
     */
    public void getAddedVehicles() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<String> addedVehicle = model.getAddedVehiclesFromServer();
                for (int i = 0; i < addedVehicle.size(); i++) {
                    String line = addedVehicle.get(i).replaceAll(",null,", ",waiting,");
                    line = line.replaceAll(",null$", ",waiting");
                    addedVehicle.set(i, line);
                }
                for (String a : addedVehicle) {
                    publish(a);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                DefaultTableModel tableModel = (DefaultTableModel) addVehicleView.getVehiclesResult().getModel();
                tableModel.setRowCount(0); // Clear the table
                for (String added : chunks) {
                    tableModel.addRow(added.split(","));
                }
            }
        };
        worker.execute();
    }
    /**
     * Retrieves the list of vehicles from the server and updates the view.
     */
    public void getVehicles() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<String> getVehicle = model.getAllVehicles();
                for (String a : getVehicle) {
                    publish(a);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                DefaultTableModel tableModel = (DefaultTableModel) BookingView.getBookingResult().getModel();
                DefaultTableModel tableModel2 = (DefaultTableModel) adminHomeView.getBookingResult().getModel();
                tableModel.setRowCount(0);
                tableModel2.setRowCount(0);
                for (String added : chunks) {
                    tableModel.addRow(added.split(","));
                    tableModel2.addRow(added.split(","));
                }
            }

        };
        worker.execute();
    }
    public void getVehiclesStatus() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<String> getVehicle = model.getAllCarsStatus();
                for (String a : getVehicle) {
                    publish(a);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                DefaultTableModel tableModel = (DefaultTableModel) VehicleStatus.getBookingResult2().getModel();
                tableModel.setRowCount(0);
                for (String added : chunks) {
                    tableModel.addRow(added.split(","));
                }
            }

        };
        worker.execute();
    }

    public void getVehiclesForPendingClients(){
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<String> getVehicle = model.getVehiclesForPendingClient();
                for (String a : getVehicle) {
                    publish(a);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                DefaultTableModel tableModel = (DefaultTableModel) PendingRequestView.getPendingRequestOutput().getModel();
                tableModel.setRowCount(0); // Clear the table
                for (String added : chunks) {
                    tableModel.addRow(added.split(","));
                }
            }
        };
        worker.execute();
    }
    /**
     * Displays the user profile information in the manage profile view.
     *
     * @param view The manage profile view.
     */
    public void displayUserProfile(ManageProfileView view) {
        try {
            User user = model.getUserCredentials(); // Fetch the user credentials
            SwingUtilities.invokeLater(() -> {
                view.getFirstNameTextField().setText(user.firstName);
                view.getLastNameTextField().setText(user.lastName);
                view.getEmailTextField().setText(user.email);
                view.getPhoneNumberTextField().setText(user.phoneNumber);
                view.getUsernameTextField().setText(user.UserName);
            });
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "Failed to fetch user profile.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Updates the user credentials with the provided information.
     *
     * @param view The manage profile view containing the updated information.
     */
    public void updateUserCredentials(ManageProfileView view) {
        String newPassword = view.getChangePasswordTextField().getText();
        String confirmPassword = view.getConfirmPasswordTextField().getText();
        String newPhoneNumber = view.getPhoneNumberTextField().getText();
        // Validate the new password and confirm password match
        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(null, "The new password should be at least 6 characters long", "Try Again", JOptionPane.ERROR_MESSAGE);
            return ;
        }
        if (newPhoneNumber.length() != 11) {
            JOptionPane.showMessageDialog(null, "Invalid new PhoneNumber format! It should be an 11-digit phoneNumber format", "Try Again", JOptionPane.ERROR_MESSAGE);
            return ;
        }
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(manageProfile, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(newPassword.isEmpty() || confirmPassword.isEmpty()){
            JOptionPane.showMessageDialog(manageProfile, "Please enter all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if(newPhoneNumber.isEmpty()){
            JOptionPane.showMessageDialog(manageProfile,"Phone number cannot be empty! please insert a phone number");
        }
        try {
            model.updateUserCredentials(view.getUsernameTextField().getText(), newPassword, newPhoneNumber);
            JOptionPane.showMessageDialog(view, "Your profile has been updated successfully.", "Profile Updated", JOptionPane.INFORMATION_MESSAGE);
        }catch (forceLogOutException e){
            JOptionPane.showMessageDialog(null, "Can't do any action because you have been forced log out by the server", "Forced Log Out", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Failed to connect to the server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Logs out the current user from the system.
     *
     * @throws IOException If there is an error communicating with the server.
     */
    public void logout() throws IOException {
        try{
            boolean response = model.logout();
            if (response) {
                JOptionPane.showMessageDialog(null, "Successfully logged out!");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error communicating with the server.", "Log out Failed", JOptionPane.ERROR_MESSAGE);
        } catch (FailedLogOutException e) {
            JOptionPane.showMessageDialog(null, "Unsuccessful log out!");
        }
    }
    /**
     * Retrieves the booking history from the server and updates the history view.
     *
     * @return A list of booking history entries.
     */
    public List<BookingHistoryEntry> showBookingHistory() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<String> History = new ArrayList<>();
                try {
                    // Retrieve booking history from the model
                    List<String> bookingHistory = model.showHistory();
                    History.addAll(bookingHistory);
                    for (String entry : History) {
                        publish(entry);
                    }
                }catch(RemoteException e){
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error communicating with the server.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                // Update the booking history view with the retrieved data
                DefaultTableModel tableModel = (DefaultTableModel) bookingStatus.getBookingResult().getModel();
                tableModel.setRowCount(0); // Clear existing rows
                for (String entry : chunks) {
                    // Parse the entry and add it to the table
                    String[] data = entry.split(",");
                    tableModel.addRow(data);
                }
            }
        };
        worker.execute();
        return null;
    }

    public List<BookingHistoryEntry> showOwmBookingHistory() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<String> History = new ArrayList<>();
                try {
                    // Retrieve booking history from the model
                    List<String> bookingHistory = model.showHistoryOwnBookin();
                    History.addAll(bookingHistory);
                    for (String entry : History) {
                        publish(entry);
                    }
                }catch(RemoteException e){
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error communicating with the server.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                // Update the booking history view with the retrieved data
                DefaultTableModel tableModel = (DefaultTableModel) historyView.getHistoryOutput().getModel();
                tableModel.setRowCount(0); // Clear existing rows
                for (String entry : chunks) {
                    // Parse the entry and add it to the table
                    String[] data = entry.split(",");
                    tableModel.addRow(data);
                }
            }
        };
        worker.execute();
        return null;
    }
    /**
     * Retrieves the booking status history from the server and updates the booking status view.
     *
     * @return A list of booking history entries.
     */
    public List<BookingHistoryEntry> showBookingStatusHistory() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<String> History = new ArrayList<>();
                try {
                    // Retrieve booking history from the model
                    List<String> bookingHistory = model.showHistory();
                    History.addAll(bookingHistory);
                    for (String entry : History) {
                        publish(entry);
                    }
                }catch(RemoteException e){
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error communicating with the server.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                // Update the booking history view with the retrieved data
                DefaultTableModel tableModel = (DefaultTableModel) bookingStatus.getBookingResult().getModel();
                tableModel.setRowCount(0); // Clear existing rows
                for (String entry : chunks) {
                    // Parse the entry and add it to the table
                    String[] data = entry.split(",");
                    tableModel.addRow(data);
                }
            }
        };
        worker.execute();
        return null;
    }
    /**
     * Updates the booking status for a specified booking ID.
     *
     * @param ID     The ID of the booking to update.
     * @param status
     * @param price
     * @return True if the booking status was successfully updated, false otherwise.
     */
    public Boolean updateBookingStatus(int ID, String status, Double price){
        if (status.equalsIgnoreCase("Done")){
            JOptionPane.showMessageDialog(null,"The vehicle you selected is already done!","Error",JOptionPane.WARNING_MESSAGE);
            return false;
        }
       if (!Objects. equals(status, "Rented")){
           JOptionPane.showMessageDialog(null,"The selected vehicle cannot be done!","Error",JOptionPane.WARNING_MESSAGE);
            return false;
       }
        try {
            Boolean response = model.bookingStatusUpdate(ID);
            if(response){
                double adminFee = price * 0.05;
                JOptionPane.showMessageDialog(null,"Successfully set the rent session done! ");
                JOptionPane.showMessageDialog(null,"Admin fee is automatically cut to your account with a total of "
                + adminFee);
                showBookingStatusHistory();
                return true;
            }else {
                System.out.println("Error connecting to the server!");
                return false;
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (forceLogOutException e) {
            JOptionPane.showMessageDialog(null, "Can't do any action because you have been forced log out by the server", "Forced Log Out", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    public Boolean updateBookingCancelled(int ID, String status){
        if (status.equalsIgnoreCase("Cancelled")){
            JOptionPane.showMessageDialog(null,"The vehicle you selected is already cancelled!","Error",JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!Objects. equals(status, "Rented")){
            JOptionPane.showMessageDialog(null,"The selected vehicle cannot be cancelled!","Error",JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (status.equalsIgnoreCase("Done")){
            JOptionPane.showMessageDialog(null,"You cannot cancel a done vehicle!","Error",JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            Boolean response = model.bookingStatusSetCancelled(ID);
            if(response){
                JOptionPane.showMessageDialog(null,"Successfully cancelled the vehicle!");
                showBookingStatusHistory();
                return true;
            }else {
                System.out.println("Error connecting to the server!");
                return false;
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (forceLogOutException e) {
            JOptionPane.showMessageDialog(null, "Can't do any action because you have been forced log out by the server", "Forced Log Out", JOptionPane.ERROR_MESSAGE);
            return false;
        }

    }
    public void carStatusAvailable(int ID, String status){
        if (!Objects. equals(status, "Unavailable")){
            JOptionPane.showMessageDialog(null,"The vehicle you selected is already available!","Error",JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Boolean response = model.carStatusAvailable(ID);
            if(response){
                JOptionPane.showMessageDialog(null,"Successfully set the car available!");
                showBookingStatusHistory();
            }else {
                System.out.println("Error connecting to the server!");
            }getVehiclesStatus();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (forceLogOutException e) {
            JOptionPane.showMessageDialog(null, "Can't do any action because you have been forced log out by the server", "Forced Log Out", JOptionPane.ERROR_MESSAGE);
        }

    }
    public void carStatusUnavailable(int ID, String status){
        if (!Objects. equals(status, "Available")){
            JOptionPane.showMessageDialog(null,"The vehicle you selected is already Unavailable!","Error",JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Boolean response = model.carStatusUnavailable(ID);
            if(response){
                JOptionPane.showMessageDialog(null,"Successfully set the car unavailable!");
                showBookingStatusHistory();
            }else {
                System.out.println("Error connecting to the server!");
            }getVehiclesStatus();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (forceLogOutException e) {
            JOptionPane.showMessageDialog(null, "Can't do any action because you have been forced log out by the server", "Forced Log Out", JOptionPane.ERROR_MESSAGE);
        }

    }
    public void approveClientVehicle(String bookID, String vehicleId, String decision) {
        try {
            Boolean response = model.approveClientVehicle(bookID,vehicleId, decision);
            if (response) {
                JOptionPane.showMessageDialog(pendingRequestView, "Vehicle with ID " + vehicleId + " approved successfully.");
            }else {
                JOptionPane.showMessageDialog(pendingRequestView, "Vehicle declined because the other vehicle is already taken the date!","Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(pendingRequestView, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        getVehiclesForPendingClients();
    }
    public void denyClientVehicle(String bookID, String vehicleId, String decision) {
        try {
            Boolean response = model.declineClientVehicle(bookID,vehicleId,decision);
            if (response) {
                JOptionPane.showMessageDialog(null, "Vehicle with ID " + vehicleId + " has been declined successfully.");
            }else{
                JOptionPane.showMessageDialog(null, "Vehicle automatically declined because the other vehicle is already taken the date!","Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(adminHomeView, "Failed to decline vehicle: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        getVehiclesForPendingClients();
    }
    /**
     * Cancels the registration process and returns the user to the welcome frame.
     */
    public void cancelRegistration() {
        signUpView.dispose();
        welcomeFrame.setVisible(true);
        welcomeFrame.setResizable(false);
        welcomeFrame.setLocationRelativeTo(null);
    }
    /**
     * Displays the welcome frame.
     */
    public void ShowWelcomeFrame(){
        welcomeFrame.setLocationRelativeTo(null);
        welcomeFrame.setResizable(false);
        welcomeFrame.setVisible(true);
    }
    /**
     * Displays the sign-in view.
     */
    public void showSignInView() {
        signInView.setLocationRelativeTo(null);
        signInView.setResizable(false);
        signInView.setVisible(true);
    }
    /**
     * Displays the sign-up view.
     */
    public void showSignUpView() {
        signUpView.setLocationRelativeTo(null);
        signUpView.setResizable(false);
        signUpView.setVisible(true);
    }
    /**
     * Displays the homepage view.
     */
    public void showHome() {
        signInView.dispose();
        homepageView.setLocationRelativeTo(null);
        homepageView.setResizable(false);
        homepageView.setVisible(true);
    }
    /**
     * Displays the admin home view.
     */
    public void showAdminHome() {
        ViewBookingData();
        adminHomeView.setResizable(false);
        adminHomeView.setLocationRelativeTo(null);
        adminHomeView.setVisible(true);
    }
    /**
     * Retrieves the announcement text from the server.
     *
     * @return The announcement text.
     */
    public String getAnnouncement() {
        String text = null;
        try {
            text = model.getAnnouncement();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        return text;
    }

    /**
     * Displays the search vehicle view.
     */
    public void showSearch() {
        searchVehicleView.setLocationRelativeTo(null);
        searchVehicleView.setResizable(false);
        searchVehicleView.setVisible(true);
    }
    /**
     * Displays the add and delete vehicle view.
     */
    public void showAddAndDelete() {
        getAddedVehicles();
        addVehicleView.setLocationRelativeTo(null);
        addVehicleView.setResizable(false);
        addVehicleView.setVisible(true);
    }
    /**
     * Displays the manage profile view.
     */
    public void showManageProfile() {
        manageProfile.setLocationRelativeTo(null);
        manageProfile.setResizable(false);
        manageProfile.setVisible(true);
        displayUserProfile(manageProfile);
    }
    /**
     * Displays the booking history view.
     */
    public void showHistory() {
        showOwmBookingHistory();
        historyView.setLocationRelativeTo(null);
        historyView.setResizable(false);
        historyView.setVisible(true);
    }
    /**
     * Displays the bookings view.
     */
    public void showBookings(){
        getVehicles();
        booking.setLocationRelativeTo(null);
        booking.setResizable(false);
        booking.setVisible(true);
    }
    /**
     * Displays the booking status view.
     */
    public void showBookingStatus(){
        showBookingStatusHistory();
        bookingStatus.setLocationRelativeTo(null);
        bookingStatus.setResizable(false);
        bookingStatus.setVisible(true);
    }
    public void showPendingClient(){
        getVehiclesForPendingClients();
        pendingRequestView.setLocationRelativeTo(null);
        pendingRequestView.setResizable(false);
        pendingRequestView.setVisible(true);
    }

    public void showVehicleStatus(){
        getVehiclesStatus();
        vehicleStatus.setLocationRelativeTo(null);
        vehicleStatus.setResizable(false);
        vehicleStatus.setVisible(true);
    }


    //========================Admin Side==============================================//
    /**
     * Refreshes the pending requests and updates the admin home view.
     */
    public void refreshPendingRequests() {
        List<Vehicle> vehicles = new ArrayList<>();
        try {
            List<String> response = model.processPendingRequests();
            for (String line : response) {
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                String model = parts[1];
                String status = parts[2];
                String plateNumber = parts[3];
                double price = Double.parseDouble(parts[4]);
                String username = parts[5];
                Vehicle vehicle = new Vehicle(id, model, status, plateNumber, price, username);
                vehicles.add(vehicle);
            }
            if (response != null) {
                adminHomeView.updatePendingRequestsTable(vehicles);
            } else {
                System.out.println("No pending requests to display.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to refresh pending requests: " + e.getMessage());
        }
    }
    /**
     * Sends an announcement to all clients.
     *
     * @param text The text of the announcement.
     */
    public void sendAnnouncement(String text) {
        model.sendAnnouncementToServer(text);
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "Announcement Broadcast Successfully!","Successful Announcement",JOptionPane.INFORMATION_MESSAGE);
        });
    }
    /**
     * Approves a vehicle with the given ID.
     *
     * @param vehicleId The ID of the vehicle to approve.
     * @param decision  The decision to approve or deny.
     */
    public void approveVehicle(String vehicleId,String decision) {
        try {
            String response = model.approveVehicle(vehicleId, decision);
            if (response.equalsIgnoreCase("true")) {
                JOptionPane.showMessageDialog(adminHomeView, "Vehicle with ID " + vehicleId + " approved successfully.");
            }else {
                JOptionPane.showMessageDialog(null, "Vehicle doesn't exist","Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(adminHomeView, "Failed to approve vehicle: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        refreshPendingRequests();
    }
    /**
     * Denies a vehicle with the given ID.
     *
     * @param vehicleId The ID of the vehicle to deny.
     * @param decision  The decision to approve or deny.
     */
    public void denyVehicle(String vehicleId, String decision) {
        try {
            String response = model.denyVehicle(vehicleId,decision);
            if (response.equalsIgnoreCase("true")) {
                JOptionPane.showMessageDialog(null, "Vehicle with ID " + vehicleId + " has been declined successfully.");
            }else{
                JOptionPane.showMessageDialog(null, "Vehicle doesn't exist","Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(adminHomeView, "Failed to decline vehicle: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        refreshPendingRequests();
    }
    /**
     * Retrieves the client data and updates the manage user table in the admin home view.
     */
    public void getClients() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<String> getUser = model.getAllClient();
                for (String a : getUser) {
                    publish(a);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                DefaultTableModel tableModel = (DefaultTableModel) adminHomeView.getManageUserTable().getModel();
                tableModel.setRowCount(0); // Clear the table
                for (String added : chunks) {
                    tableModel.addRow(added.split(","));
                }
            }
        };
        worker.execute();
    }
    /**
     * Handles the client search button action and updates the manage user table accordingly.
     */
    public void handleClientSearchButton() {
        String searchAttribute = adminHomeView.getTxtFieldSearchManageUser().getText();

        // Check if the search attribute is not empty before initiating the search
        if (!searchAttribute.isEmpty()) {
            SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<String> doInBackground() {
                    List<String> results = new ArrayList<>();
                    if (searchAttribute.isEmpty()) {  // Check if the search value is not empty
                        JOptionPane.showMessageDialog(null, "Search value is empty.", "Search Failed", JOptionPane.INFORMATION_MESSAGE);
                    }
                    try{
                        List<String> response = model.searchUser(searchAttribute); // calling searchUser method directly
                        if (!response.isEmpty()) {
                            for (String user : response) {
                                // Process each user's information
                                StringBuilder userInfoBuilder = new StringBuilder();
                                String[] userInfo = user.split(",");
                                for (int i = 0; i < userInfo.length; i++) {
                                    userInfoBuilder.append(userInfo[i]);
                                    if (i < userInfo.length - 1) {
                                        userInfoBuilder.append(", "); // Add comma and space if not last column
                                    }
                                }
                                results.add(userInfoBuilder.toString()); // Add concatenated user info to the results list
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "No matching users found", "Search Failed", JOptionPane.INFORMATION_MESSAGE);
                        } } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Unexpected error during the search.", "Search Failed", JOptionPane.ERROR_MESSAGE);
                    }
                    return results;
                }

                @Override
                protected void done() {
                    try {
                        List<String> results = get();
                        SwingUtilities.invokeLater(() -> {
                            if (results.contains("No matching users found.")) {
                                // Set the table to null or perform other actions as needed
                                updateClientSearchResults(null);
                            } else {
                                updateClientSearchResults(results);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            worker.execute();
        } else {
            JOptionPane.showMessageDialog(null, "Search attribute is empty.", "Search Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Your existing updateClientSearchResults method
    public void updateClientSearchResults(List<String> results) {
        DefaultTableModel tableModel = (DefaultTableModel) adminHomeView.getManageUserTable().getModel();
        tableModel.setRowCount(0); // Clear existing rows
        try {
            for (String result : results) {
                String[] rowData = result.split(", ");
                tableModel.addRow(rowData);
            }
        } catch (NullPointerException ignore) {
            // Handle NullPointerException if needed
        }
    }
    /**
     * Refreshes the client data.
     */
    public void refreshClientData() {
        getClients();
    }
    /**
     * Deletes a user with the given username.
     *
     * @param username The username of the user to delete.
     * @return True if the user deletion was successful, false otherwise.
     */
    public boolean deleteUser(String username) {
        try {
            boolean cancellationResult = model.deleteUser(username);
            if (cancellationResult) {
                JOptionPane.showMessageDialog(null, "User Deletion successful!", "Deletion Result", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Error: Unable to get server response", "Deletion Result", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }catch (AlreadyLoggedInException e){
            JOptionPane.showMessageDialog(null, "Error: Unable to delete account! user: " + username +" is online", "Deletion Result", JOptionPane.ERROR_MESSAGE);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (matchAccountException e) {
            JOptionPane.showMessageDialog(null, "You cannot delete yourself", "Deletion Result", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    /**
     * Retrieves the vehicle data and updates the manage car table in the admin home view.
     */
    public void getCars() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<String> getCars = model.getAllCars();
                for (String a : getCars) {
                    publish(a);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                DefaultTableModel tableModel = (DefaultTableModel) adminHomeView.getManageCarTable().getModel();
                tableModel.setRowCount(0); // Clear the table
                for (String added : chunks) {
                    tableModel.addRow(added.split(","));
                }
            }
        };
        worker.execute();
    }
    /**
     * Refreshes the vehicle data.
     */
    public void refreshCarsData() {
        getCars();
    }
    /**
     * Deletes a vehicle with the given ID and model.
     *
     * @param vehicleId    The ID of the vehicle to delete.
     * @param vehicleModel The model of the vehicle to delete.
     * @return True if the vehicle deletion was successful, false otherwise.
     */
    public boolean deleteVehicleAdmin(String vehicleId, String vehicleModel) {
        try {
            boolean cancellationResult = model.cancelVehicle(vehicleId, vehicleModel);
            getVehicles();
            if (cancellationResult) {
                JOptionPane.showMessageDialog(this, "Vehicle cancellation successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Vehicle not found!", "Failed", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * Handles the vehicle search button action and updates the manage car table accordingly.
     */
    public void handleVehicleSearchButtonAdmin() {
        String searchAttribute = adminHomeView.getTxtFieldSearchManageCar().getText();
        // Check if the search attribute is not empty before initiating the search
        if (!searchAttribute.isEmpty()) {
            SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<String> doInBackground() {
                    List<String> results = new ArrayList<>();
                    if (searchAttribute.isEmpty()) {  // Check if the search value is not empty
                        JOptionPane.showMessageDialog(null, "Search value is empty.", "Search Failed", JOptionPane.INFORMATION_MESSAGE);
                    }
                    try {
                        List<String> response = model.searchVehicle(searchAttribute);
                        if (!response.isEmpty()) {
                            for (String vehicle : response) {
                                StringBuilder vehicleInfoBuilder = new StringBuilder();
                                String[] vehicleInfo = vehicle.split(",");
                                for (int i = 0; i < vehicleInfo.length; i++) {
                                    vehicleInfoBuilder.append(vehicleInfo[i]);
                                    if (i < vehicleInfo.length - 1) {
                                        vehicleInfoBuilder.append(", ");
                                    }
                                }
                                results.add(vehicleInfoBuilder.toString());
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "No matching vehicle found", "Search Failed", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }catch (IOException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error communicating with the server.", "Search Failed", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Unexpected error during the search.", "Search Failed", JOptionPane.ERROR_MESSAGE);
                    }
                    return results;
                }

                @Override
                protected void done() {
                    try {
                        List<String> results = get();
                        SwingUtilities.invokeLater(() -> {
                            if (results.contains("No matching vehicle found.")) {
                                updateVehicleSearchResults(null);
                            } else {
                                updateVehicleSearchResults(results);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            worker.execute();
        } else {
            JOptionPane.showMessageDialog(null, "Search attribute is empty.", "Search Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    /**
     * Updates the manage car table with the search results.
     *
     * @param results The search results to display in the manage car table.
     */
    public void updateVehicleSearchResults(List<String> results) {
        DefaultTableModel tableModel = (DefaultTableModel) adminHomeView.getManageCarTable().getModel();
        tableModel.setRowCount(0); // Clear existing rows
        try {
            for (String result : results) {
                String[] rowData = result.split(", ");
                tableModel.addRow(rowData);
            }
        } catch (NullPointerException ignore) {
        }
    }

    public void getPurchaseHistory() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<String> getPurchaseHistory = model.getAllPurchaseHistory();
                for (String p : getPurchaseHistory) {
                    publish(p);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                DefaultTableModel tableModel = (DefaultTableModel) adminHomeView.getPurchaseHistory().getModel();
                tableModel.setRowCount(0); // Clear the table
                for (String added : chunks) {
                    tableModel.addRow(added.split(","));
                }
            }
        };
        worker.execute();
    }
    //if viewBooking is clicked
    public void ViewBookingData() {
        refreshPendingRequests();
    }

    //if purchaseHistory table is clicked
    public void PurchaseHistoryData() {
        getPurchaseHistory();
    }

    public void addVehicleAdmin(String user,String Model, String PlateNumber, double Price) {
        if (Model.isEmpty() || PlateNumber.isEmpty()  || Price < 0) {
            JOptionPane.showMessageDialog(null, "Please enter valid values! all fields shouldn't be empty and price can't be lower than 0", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String response = null;
        try {
            response = model.addVehicleAdmin(Model, PlateNumber, Price,user);
            if (response.equals("Vehicle added successfully.")) {
                JOptionPane.showMessageDialog(null, "Vehicle added successfully.\nPlease approve the vehicle if you want to add it in the carlist.", "Update", JOptionPane.INFORMATION_MESSAGE);
            } else if (response.equals("PlateNumber exists")) {
                JOptionPane.showMessageDialog(null, "Plate number already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }  else {
                JOptionPane.showMessageDialog(null, response, "Error", JOptionPane.ERROR_MESSAGE);
            }
            getAddedVehicles();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (forceLogOutException e) {
            JOptionPane.showMessageDialog(null, "Can't do any action because you have been forced log out by the server", "Forced Log Out", JOptionPane.ERROR_MESSAGE);
        } catch (UsernameExistsException e) {
            JOptionPane.showMessageDialog(null, "User doesn't exist!", "User not found", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void banUser(String user, String decision, String status) {
       if (status.equalsIgnoreCase("banned")){
           JOptionPane.showMessageDialog(null, "The user is already banned!","Error",JOptionPane.ERROR_MESSAGE);
           return;
       }
        try {
            Boolean response = model.banUser(user,decision);
            if (response) {
                JOptionPane.showMessageDialog(null, "Username: " + user + " successfully banned!");
            }else{
                JOptionPane.showMessageDialog(null, "user doesn't exist","Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(adminHomeView, "Failed to decline vehicle: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        refreshClientData();
    }

    public void unbanUser(String user, String decision, String status) {
        if (status.equalsIgnoreCase("not banned")){
            JOptionPane.showMessageDialog(null, "The user is already not banned!","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Boolean response = model.unbanUser(user,decision);
            if (response) {
                JOptionPane.showMessageDialog(null, "Username: " + user + " Successfully unbanned!");
            }else{
                JOptionPane.showMessageDialog(null, " user doesn't exist","Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(adminHomeView, "Failed to decline vehicle: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        refreshClientData();
    }
    public Boolean bookForPendingVehicleAdmin(String Model, String plateNo, Date from, Date to) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formatter = dateFormat.format(new Date());
        Date currentDate = new Date();
        String user = JOptionPane.showInputDialog(null,"Please enter the username of the booker","Username",JOptionPane.INFORMATION_MESSAGE);
        try {
            currentDate = dateFormat.parse(formatter);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        if (from == null || to == null) {
            JOptionPane.showMessageDialog(null, "Please enter valid values! all fields shouldn't be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (from.after(to)) {
            JOptionPane.showMessageDialog(null, "The start date shouldn't be greater than the end date ", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (from.before(currentDate)) {
            JOptionPane.showMessageDialog(null, "You can't book in past dates! ", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            List<String[]> rentedDates = model.getDates(plateNo);
            // Check for dates  with existing rentals
            for (String[] dates : rentedDates) {
                Date rentedStartDate = dateFormat.parse(dates[0]);
                Date rentedEndDate = dateFormat.parse(dates[1]);
                if (!(to.before(rentedStartDate) || from.after(rentedEndDate))) {
                    JOptionPane.showMessageDialog(null, "Selected date is unavailable!", "Booking Failed", JOptionPane.ERROR_MESSAGE);
                    StringBuilder message = new StringBuilder("Unavailable dates:\n");
                    for (String[] date : rentedDates) {
                        message.append(date[0]).append(" to ").append(date[1]).append("\n");
                    }
                    JOptionPane.showMessageDialog(null, message.toString(), "Unavailable Dates", JOptionPane.WARNING_MESSAGE);
                    return null;
                }
            }

            try {
                boolean bookingResult = model.bookForPendingVehicleAdmin(Model, plateNo, from, to,user);

                if (bookingResult) {
                    JOptionPane.showMessageDialog(null, "Vehicle booked successfully.\n Please wait for the approval of the renter");
                    getVehicles();
                    return true;
                }

            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error communicating with the server.", "Booking Failed", JOptionPane.ERROR_MESSAGE);
            } catch (CannotBookOwnCarException e) {
                JOptionPane.showMessageDialog(null, "You cannot book the vehicle of its own.", "Booking Failed", JOptionPane.ERROR_MESSAGE);
            } catch (CarNotAvailableException e) {
                JOptionPane.showMessageDialog(null, "Vehicle not available for booking.", "Booking Failed", JOptionPane.ERROR_MESSAGE);
            } catch (UsernameExistsException e) {
                JOptionPane.showMessageDialog(null, "Username not found!", "Booking Failed", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void getActivityLog(JTextArea jTextArea) {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String activityLog = model.activityLog();
                // Split the activity log by newline and publish each line
                String[] logs = activityLog.split("\\r?\\n");
                for (String log : logs) {
                    publish(log);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String added : chunks) {
                    jTextArea.append(added + "\n");
                }
            }
        };
        worker.execute();
    }
}