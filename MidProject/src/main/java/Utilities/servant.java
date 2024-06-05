package Utilities;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

public interface servant extends Remote {
    public Boolean logout (String username) throws RemoteException, FailedLogOutException;
    public  String login(String credentials) throws RemoteException, AlreadyLoggedInException, InvalidInputCredentialsExceptions, BannedAccountException;
    public String register(String credentials) throws RemoteException, UsernameExistsException;
    public List<JSONObject> searchVehicle(String attribute, String value) throws RemoteException;
    public String addVehicle(String name, String plateNumber, Date schedule, Double price, String username) throws RemoteException, forceLogOutException;
    public Boolean  cancelVehicle(String model, String plateNo,String currentUser) throws RemoteException, forceLogOutException;
    public List<User> getCredentials(String userName) throws RemoteException;
    public Boolean updateCredentials(String username, String newPassword, String newPhoneNumber) throws RemoteException, forceLogOutException;
    public List<String> showHistory(String currentUser) throws RemoteException;
    public String saveAnnouncement(String announcement,String Username) throws RemoteException;
    public String readAnnouncement() throws RemoteException;
    public Boolean bookVehicle(String model, String plateNo, String currentUser,Date from, Date to) throws RemoteException, CannotBookOwnCarException, CarNotAvailableException, forceLogOutException;
    public List<String> getUserAddedVehicleFromJson(String targetUsername) throws RemoteException;
    public  List<String> getVehicleDetails() throws RemoteException;
    public List<String> readPendingRequests() throws RemoteException;
    public String processPendingRequests(String vehicleID, String decision) throws RemoteException;
    public  List<String>getClientInfo() throws RemoteException;
    public  List<String> searchUser(String value) throws RemoteException;
    public  boolean deleteUser(String username, String currentUser) throws RemoteException, matchAccountException;
    public  List<String> getCarInfo() throws  RemoteException;
    public List<String> searchVehicle(String value) throws RemoteException;
    public boolean bookingDone(int ID,String currentUser) throws RemoteException, forceLogOutException;
    public Boolean processBanAndUnban(String username, String decision) throws RemoteException;
    public Boolean processPendingClient(String bookID,String vehicleID, String decision, String currentUser) throws IOException, forceLogOutException;
    public List<String> getVehicleForPendingClient(String username) throws RemoteException;
    public List<String> getCarInfoSpecificUser(String currentUser) throws RemoteException;
    public boolean statusAvailable(int ID,String currentUser) throws RemoteException, forceLogOutException;
    public boolean statusUnavailable(int ID,String currentUser) throws RemoteException, forceLogOutException;
    public boolean bookingCancelled(int ID,String currentUser) throws RemoteException, forceLogOutException;
    public List<String> showHistoryForOwnBooking(String currentUser) throws RemoteException;
    public List<String[]> getRentedDates(String plateNumber) throws RemoteException;
    public List<String> getPurchaseHistoryInfo() throws RemoteException;
    public String addVehicleAdmin(String name, String plateNumber, Date schedule, Double price, String username) throws RemoteException, forceLogOutException, UsernameExistsException;
    public Boolean bookVehicleAdmin(String model, String plateNo, String currentUser,Date from, Date to) throws RemoteException, CannotBookOwnCarException, CarNotAvailableException, UsernameExistsException;
    public String readActivitiesFromFile() throws RemoteException;
}