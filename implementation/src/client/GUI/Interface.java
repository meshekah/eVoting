package client.GUI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.*;
import java.awt.event.*;

import client.Client;
import client.GUI.Interface;
import crypto.RSAWrapper;
import java.awt.Dimension;

/**
 * The GUI layer for the client
 */
public abstract class Interface extends JFrame
{

    /**
     * The user's name
     */
    protected JLabel nameLabel;
    
    /**
     * The user's SSN
     */
    protected JLabel ssnLabel;
    
    /**
     * A status label
     */
    protected JLabel statusLabel;
    
    /**
     * The grid to layout components
     */
    protected GridBagLayout grid;
    
    /**
     * Signals registration
     */
    protected JButton registerButton;
    
    /**
     * Choose the candidate
     */
    protected JComboBox idChooser;
    
    /** 
     * Accepts the user's SSN
     */
    protected JTextField ssnField;
    
    /**
     * Accepts the user's name
     */
    protected JTextField nameField;

    /**
     * Signal voting
     */
    protected JButton voteButton;
    
    /**
     * Signal reveal vote
     */
    protected JButton revealButton;
    
    /**
     * Signal changing a vote
     */
    protected JButton changeButton;
    
    /**
     * Signal protest
     */
    protected JButton protestButton;
    
    /**
     * Register a new client
     * @param name The client's name
     * @param SSN The client's SSN
     */
    public abstract void registerClient(String name, int SSN);
    
    /**
     * Submit a vote
     * @param vote The vote to submit
     */
    public abstract void submitVote(String vote);
    
    /**
     * Reveal the vote by sending the decryption parameters
     */
    public abstract void revealVote();
    
    /**
     * Protest that you voted for the given vote
     * @param vote The vote
     */
    public abstract void protestVote(String vote);
    
    /**
     * Change a vote
     * @param vote The new vote
     */
    public abstract void changeVote(String vote);
    
    /**
     * Given a String representing a SSN, returns whether the SSN is valid
     * or not
     * @param value The SSN string to parse
     * @return Whether the SSN is valid or not
     */
    public boolean isValidSSN(String value)
    {
        /* Only accept 123456789 */
        if (value.length() != 9)
        {
            return false;
        }
        try
        {
            Integer ssn = Integer.parseInt(value);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    /**
     * Start the GUI interface for the client
     */
    public void startGUI()
    {
        grid = new GridBagLayout();
        JPanel registerPanel = new JPanel(grid);

        GridBagConstraints c = new GridBagConstraints();

        nameLabel = new JLabel("User Name:");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        registerPanel.add(nameLabel, c);

        nameField = new JTextField(15);
        nameField.setPreferredSize(new Dimension(175,20));
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        registerPanel.add(nameField, c);

        ssnLabel = new JLabel("User SSN:");
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        registerPanel.add(ssnLabel, c);

        ssnField = new JTextField(9);
        ssnField.setSize(new Dimension(175,20));
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        registerPanel.add(ssnField, c);

        registerButton = new JButton();
        registerButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {

                if(nameField.getText().equals(""))
                {
                    statusLabel.setText("Please enter your name");
                    return;
                }

                if (isValidSSN(ssnField.getText()) == false)
                {
                    statusLabel.setText("Please enter a valid SSN. No dashes please.");
                    return;
                }

                statusLabel.setText("Registering... This may take a moment");
                registerClient(nameField.getText(),Integer.parseInt(ssnField.getText()));
//                System.out.println("DONE!");
                statusLabel.setText("");
            }
        });
        registerButton.setSize(20, 20);
        registerButton.setText("Register");
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        registerPanel.add(registerButton, c);

        statusLabel = new JLabel("");
        statusLabel.setPreferredSize(new Dimension(175,20));
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        registerPanel.add(statusLabel, c);

        /* Kill the application when the GUI closes */
        addWindowListener(new WindowAdapter()
        {

            public void windowClosing(WindowEvent e)
            {
                dispose();
                System.exit(0); //calling the method is a must
            }
        });

        registerPanel.setSize(registerPanel.getPreferredSize());
        registerPanel.setVisible(true);
        registerPanel.validate();
        
        JPanel votePanel = new JPanel(grid);
        
        voteButton = new JButton("Submit Vote");
        voteButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae)
            {
                /* Submit the vote */
                submitVote(idChooser.getSelectedItem().toString());
                
                /* Display a change vote button, not submit now */
//                flipButtons();
                        
            }
        });
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
	votePanel.add(voteButton,c);
        
        revealButton = new JButton("Reveal Vote");
        revealButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                revealVote();
            }
        });
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
	votePanel.add(revealButton,c);
        
        protestButton = new JButton("Protest Count");
        protestButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                protestVote(idChooser.getSelectedItem().toString());
            }
        });
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
	votePanel.add(protestButton,c);
        
        changeButton = new JButton("Change Vote");
        changeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                changeVote(idChooser.getSelectedItem().toString());
            }
        });
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
//        changeButton.setVisible(false); // not shown by default
	votePanel.add(changeButton,c);
        
        
        idChooser = new JComboBox();
        idChooser.addItem("John Smith");
        idChooser.addItem("Gordon Freeman");
        idChooser.addItem("Jackie Burkhart");
        idChooser.addItem("Michael Bluth");
        idChooser.addItem("Angela Martin");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
	votePanel.add(idChooser,c);
        votePanel.setSize(votePanel.getPreferredSize());
        votePanel.validate();
        
        
        JPanel diagnosticPanel = new JPanel();
        
        JTabbedPane pane = new JTabbedPane();
        pane.addTab("Register", registerPanel);
        pane.addTab("Vote", votePanel);
//        pane.addTab("Diagnostics", diagnosticPanel);
        pane.validate();
        pane.setVisible(true);
        add(pane);
        
        this.setLayout(grid);
        
        this.setSize(new Dimension(330, 180));
        setVisible(true);
    }
}
