package Utilities;

import java.io.Serializable;

public class User implements Serializable {
    /**
     * Represents a user in the system with basic user information.
     * This class is used to create a user object containing their first name, last name, email, username, and phone number.
     */
        // User's first name
        public String firstName;
        // User's last name
        public String lastName;
        // User's username
        public String UserName;
        // User's email address
        public String email;
        // User's phone number
        public String phoneNumber;

        /**
         * Constructs a new User object with specified first name, last name, email, username, and phone number.
         *
         * @param firstName The user's first name.
         * @param lastName The user's last name.
         * @param email The user's email address.
         * @param UserName The user's username.
         * @param phoneNumber The user's phone number.
         */
        public User(String firstName, String lastName, String email, String UserName, String phoneNumber) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.UserName = UserName;
            this.phoneNumber = phoneNumber;
        }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return UserName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
         * Returns a string representation of the User object.
         * The format is "firstName,lastName,email,UserName,phoneNumber".
         *
         * @return A string representation of the user's information.
         */
        public String toString() {
            return firstName + "," + lastName + "," + email + "," + UserName + "," + phoneNumber;
        }
    }



