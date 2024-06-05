package ClientMVC;

import Utilities.*;

import javax.swing.*;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Model {
    private servant servant;
    private String currentUser;


    public Model() {
        String serverIp = IP.getServerIp();
        try {
            Registry registry = LocateRegistry.getRegistry(serverIp, 2000);
            servant = (servant) registry.lookup("IntegLee");

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public String logInToServer(String username, String password) throws InvalidInputCredentialsExceptions, AlreadyLoggedInException, RemoteException, BannedAccountException {
        String serverResponse = servant.login(username.trim() + "," + password.trim());
        this.currentUser = username;
        return serverResponse;
    }
    public void registerUser(String firstName, String lastName, String userName, String email, String password, String phoneNumber) throws UsernameExistsException, RemoteException {
        servant.register(firstName + "," + lastName + "," + userName + "," + password + "," + email + "," + phoneNumber);
    }
    public String getAnnouncement() throws RemoteException {
        String response = servant.readAnnouncement();
        return response;
    }

    public String searchVehicle(String attribute, String value) throws RemoteException {
        String response = String.valueOf(servant.searchVehicle(attribute, value));
        return response;
    }

    public boolean bookForPendingVehicle(String model,String plateNo,Date from,Date To) throws CarNotAvailableException, CannotBookOwnCarException, RemoteException, forceLogOutException {
        Boolean serverResponse = servant.bookVehicle(model, plateNo, currentUser,from,To);
        return serverResponse;
    }

    public List<String> getAllVehicles() throws IOException {
        List<String> addedVehicles = new ArrayList<>();
        List<String> response = servant.getVehicleDetails();
        addedVehicles.addAll(response);
        return addedVehicles;
    }

    public List<String> getVehiclesForPendingClient() throws IOException{
        List<String> clientVehicles = new ArrayList<>();
        List<String> response = servant.getVehicleForPendingClient(currentUser);
        clientVehicles.addAll(response);
        return clientVehicles;
    }

    public String addVehicle(String model, String plateNumber, double price) throws RemoteException, forceLogOutException {
        String response = servant.addVehicle(model, plateNumber, new Date(), price, currentUser);
        return response;
    }

    public boolean cancelVehicle(String vehicleId, String plateNo) throws forceLogOutException {
        try {
            Boolean response = servant.cancelVehicle(vehicleId, plateNo,currentUser);
            return response;
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error:", "Cancellation Result", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    public List<String[]> getDates(String plateNumber) throws RemoteException{
        List<String[]> response = servant.getRentedDates(plateNumber);
            return response;
    }


    public List<String> getAddedVehiclesFromServer() throws RemoteException {
        try {
            List<String> response = servant.getUserAddedVehicleFromJson(currentUser);
            return response;
        } catch (RemoteException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public Boolean logout() throws IOException, FailedLogOutException {
            Boolean response = servant.logout(currentUser);
            this.currentUser = null; // Reset current user
            return response;

    }
    public User getUserCredentials() throws IOException {
        List<User> response = servant.getCredentials(currentUser);
            User user = response.get(0);
            return user;
    }

    public boolean updateUserCredentials(String username, String newPassword, String newPhoneNumber) throws IOException, forceLogOutException {
        Boolean response = servant.updateCredentials(username,newPassword,newPhoneNumber);
        return "Credentials updated successfully.".equals(response);
    }
    public List<String> showHistory() throws RemoteException {
        List<String> history = servant.showHistory(currentUser);
        return history;
    }
    public List<String> showHistoryOwnBookin() throws RemoteException {
        List<String> history = servant.showHistoryForOwnBooking(currentUser);
        return history;
    }
    public boolean bookingStatusUpdate(int ID) throws RemoteException, forceLogOutException {
        Boolean serverResponse = servant.bookingDone(ID,currentUser);
        return serverResponse;
    }
    public Boolean approveClientVehicle(String bookID, String vehicleId, String decision) throws IOException, forceLogOutException {
        Boolean response = servant.processPendingClient(bookID,vehicleId,decision,currentUser);
        return response;
    }

    public Boolean declineClientVehicle(String bookID, String vehicleId, String decision) throws IOException, forceLogOutException {
        Boolean response = servant.processPendingClient(bookID,vehicleId,decision,currentUser);
        return response;
    }
    public List<String> getAllCarsStatus() throws IOException {
        List<String> cars = new ArrayList<>();
        List<String>carlist = servant.getCarInfoSpecificUser(currentUser);
        cars.addAll(carlist);
        return cars;
    }
    public boolean carStatusAvailable(int ID) throws RemoteException, forceLogOutException {
        Boolean serverResponse = servant.statusAvailable(ID,currentUser);
        return serverResponse;
    }
    public boolean bookingStatusSetCancelled(int ID) throws RemoteException, forceLogOutException {
        Boolean serverResponse = servant.bookingCancelled(ID,currentUser);
        return serverResponse;
    }
    public boolean carStatusUnavailable(int ID) throws RemoteException, forceLogOutException {
        Boolean serverResponse = servant.statusUnavailable(ID,currentUser);
        return serverResponse;
    }
    //========================Admin Side==============================================//

    public void sendAnnouncementToServer(String text) {
        try {
             servant.saveAnnouncement(text,currentUser);
        } catch (IOException e) {
           e.printStackTrace();
        }
    }
    public List<String> processPendingRequests() throws IOException {
        List<String> response = servant.readPendingRequests();
        return response;
    }
    public String approveVehicle(String vehicleId, String decision) throws RemoteException {
       String response = servant.processPendingRequests(vehicleId,decision);
       return response;
    }
    public String denyVehicle(String vehicleId, String decision) throws RemoteException {
        String response = servant.processPendingRequests(vehicleId,decision);
        return response;
    }

    public List<String> getAllClient() throws IOException {
        List<String> addedClient = new ArrayList<>();
        List<String> client = servant.getClientInfo();
         addedClient.addAll(client);
        return addedClient;
    }
    public List<String> searchUser(String value) throws RemoteException {
        List<String> response = servant.searchUser(value);
        return response;
    }

    public boolean deleteUser(String userName) throws AlreadyLoggedInException, RemoteException, matchAccountException {
            Boolean serverResponse = servant.deleteUser(userName,currentUser);
            // Display the cancellation result using JOptionPane
           return serverResponse;
    }
    public List<String> getAllCars() throws IOException {
        List<String> cars = new ArrayList<>();
        List<String>carlist = servant.getCarInfo();
        cars.addAll(carlist);
        return cars;
    }
    public List<String> searchVehicle(String value) throws RemoteException {
        List<String> response = servant.searchVehicle(value);
        return response;
    }
    public List<String> getAllPurchaseHistory() throws IOException {
        List<String> purchaseHistory = new ArrayList<>();
        List<String> purchaseHistoryList = servant.getPurchaseHistoryInfo();
        purchaseHistory.addAll(purchaseHistoryList);
        return purchaseHistory;
    }

    public String addVehicleAdmin(String model, String plateNumber, double price,String user) throws RemoteException, forceLogOutException, UsernameExistsException {
        String response = servant.addVehicleAdmin(model, plateNumber, new Date(), price, user);
        return response;
    }

    public Boolean banUser(String user, String decision) throws RemoteException {
        Boolean response = servant.processBanAndUnban(user,decision);
        return response;
    }
    
    public Boolean unbanUser(String user, String decision) throws RemoteException {
        Boolean response = servant.processBanAndUnban(user,decision);
        return response;
    }
    
    public boolean bookForPendingVehicleAdmin(String model,String plateNo,Date from,Date To,String user) throws CarNotAvailableException, CannotBookOwnCarException, RemoteException, UsernameExistsException {
        Boolean serverResponse = servant.bookVehicleAdmin(model, plateNo, user,from,To);
        return serverResponse;
    }

    public String activityLog() throws IOException {
        String addedActivity = servant.readActivitiesFromFile();
        return addedActivity;
    }
}