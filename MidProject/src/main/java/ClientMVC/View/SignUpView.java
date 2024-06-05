package ClientMVC.View;


import ClientMVC.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignUpView extends JDialog {
    private JTextField tffirstName;
    private JTextField tflastName;
    private JTextField tfuserName;
    private JTextField tfemail;
    private JPasswordField pfPassword;
    private JPasswordField pfRetypePassword;
    private JTextField tfphoneNumber;
    private JButton registerButton;
    private JButton cancelButton;
    private JPanel registerPanel;
    private JLabel logo;

;

    private void createUIComponents() {
        // Initialize your components here
        tffirstName = new JTextField();
        tflastName = new JTextField();
        tfuserName = new JTextField();
        tfemail = new JTextField();
        pfPassword = new JPasswordField();
        pfRetypePassword = new JPasswordField();
        tfphoneNumber = new JTextField();
        registerButton = new JButton("Register");
        cancelButton = new JButton("Cancel");
        registerPanel = new JPanel(new GridLayout(8, 2));

        registerPanel.add(new JLabel("First Name:"));
        registerPanel.add(tffirstName);

        registerPanel.add(registerButton);
        registerPanel.add(cancelButton);
    }

    public SignUpView(JFrame parent) {
        super(parent);
        setTitle("Registration Form");
        setContentPane(registerPanel);
        setMinimumSize(new Dimension(500, 500));
        setResizable(false);
        setLocationRelativeTo(parent);

        registerButton.addActionListener(new ActionListener() {
           @Override
            public void actionPerformed(ActionEvent e) {
                Controller.getInstance().registerUser(
                       tffirstName.getText(), tflastName.getText(), tfuserName.getText(),
                        tfemail.getText(), new String(pfPassword.getPassword()),
                      new String(pfRetypePassword.getPassword()), tfphoneNumber.getText()
                );
          }
        });
        cancelButton.addActionListener(new ActionListener() {
           @Override
            public void actionPerformed(ActionEvent e) {
              dispose();
               Controller.getInstance().cancelRegistration();
           }
        });

    }
}
