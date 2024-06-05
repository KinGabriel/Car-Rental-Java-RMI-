/**
 * Names:
 * Caparas, Joaquin Gabriel
 * Carino,Mark Lorenz
 * DeMesa, Rovic Louie
 * San Miguel, Chloe Lee
 * Sin, Lawrence Edward
 * Vergara, Carlos Miguel
 * Project: IntegLee Car Rental - RMI implementation
 */
package Run;

import ClientMVC.View.IPFrameView;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        // Set Nimbus Look and Feel for a consistent appearance across platforms
        setNimbusLookAndFeel();

        // Initialize and display the main GUI window on the Event Dispatch Thread to ensure thread safety.
        SwingUtilities.invokeLater(() -> {
            IPFrameView ipFrameView = new IPFrameView(); // Create the main GUI frame
            configureFrame(ipFrameView); // Configure frame properties
            ipFrameView.setVisible(true); // Make the frame visible to the user
        });
    }

    /**
     * Sets the Nimbus Look and Feel for the application.
     * Nimbus provides a modern and aesthetic look for Java Swing applications.
     */
    private static void setNimbusLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace(); // Log any errors encountered while setting the Look and Feel
        }
    }

    /**
     * Configures basic properties of the application's main frame.
     * @param frame The main application frame to be configured.
     */
    private static void configureFrame(JFrame frame) {
        frame.setLocationRelativeTo(null); // Center the frame on the screen for better visibility
        frame.setResizable(false); // Disable resizing to maintain the designed layout
    }
}
