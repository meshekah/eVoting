/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ctf.GUI;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
 *
 * @author stkerr & Mohammed
 */
public abstract class Interface extends JFrame
{

    /**
     * The label used to mark the current period
     */
    protected JLabel timeLabel;
    
    /**
     * The grid used to layout components
     */
    protected GridBagLayout grid;
    
    /**
     * The results panel
     */
    protected JPanel resultsPanel;
    
    /**
     * Allows changing the different voting periods
     */
    protected JPanel timePanel;
    
    /**
     * Displays the results of the election
     */
    protected JTextArea resultsField;
    
    /**
     * Start the registration period
     */
    public abstract void startRegistrationPeriod();
    
    /**
     * Start the voting period
     */
    public abstract void startVotingPeriod();
    
    /**
     * Start the vote revealing period
     */
    public abstract void startRevealPeriod();
    
    /**
     * Calculate the results of the election
     * @return A string ready to be displayed, containing the election results
     */
    public abstract String calculateResults();
    
    /**
     * Add a new potential candidate
     * @param candidate The potential candidate's name
     */
    public abstract void addCandidate(String candidate);
    
    /**
     * Configure the GUI for the CTF
     */
    public void configGUI()
    {
        grid = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        /*
        JPanel adminPanel = new JPanel(grid);
        candidateField = new JTextField(15);
        c.gridx = 0;
        c.gridy = 0;
        candidateField.setSize(new Dimension(175,20));
        candidateField.setMinimumSize(new Dimension(175,20));
        adminPanel.add(candidateField,c);
        JButton addCandButton = new JButton("Add Candidate");
        c.gridx = 0;
        c.gridy = 1;
        addCandButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                addCandidate(candidateField.getText());
            }
        });
        adminPanel.add(addCandButton,c);
        */
        
        timePanel = new JPanel(grid);

        JButton startReg = new JButton("Start Registration");
        c.gridx = 0;
        c.gridy = 1;
        startReg.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                startRegistrationPeriod();
            }
        });
        timePanel.add(startReg,c);
        
        JButton startVote = new JButton("Start Vote");
        c.gridx = 1;
        c.gridy = 1;
        startVote.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                startVotingPeriod();
            }
        });
        timePanel.add(startVote,c);
        
        JButton startReveal = new JButton("Start Reveal");
        startReveal.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                startRevealPeriod();
            }
        });
        c.gridx = 2;
        c.gridy = 1;
        timePanel.add(startReveal,c);
        
        timeLabel = new JLabel("Current Period: None");
        timeLabel.setMinimumSize(new Dimension(100,20));
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        timePanel.add(timeLabel,c);
        
        resultsPanel = new JPanel();
        
        JButton calcResults = new JButton("Calculate Results");
        calcResults.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                resultsField.setText(calculateResults());
            }
        });
        c.gridx = 0;
        c.gridy = 0;
        resultsPanel.add(calcResults, c);
        
        resultsField = new JTextArea();
        resultsField.setPreferredSize(new Dimension(180,80));
        resultsField.setSize(new Dimension(180,80));
        resultsField.setMinimumSize(new Dimension(180,80));
        c.gridx = 1;
        c.gridy = 1;
        resultsPanel.add(resultsField,c);
        resultsPanel.validate();
        
        
        JTabbedPane pane = new JTabbedPane();
//        pane.addTab("Administration", adminPanel);
        pane.addTab("Time Periods", timePanel);
        pane.addTab("Results", resultsPanel);
        pane.validate();
        pane.setVisible(true);
        add(pane);
        
        addWindowListener(new WindowListener() {

            public void windowOpened(WindowEvent e)
            {
                return;
            }

            public void windowClosing(WindowEvent e)
            {
                dispose();
                System.exit(0);
            }

            public void windowClosed(WindowEvent e)
            {
                return;
            }

            public void windowIconified(WindowEvent e)
            {
                return;
            }

            public void windowDeiconified(WindowEvent e)
            {
                return;
            }

            public void windowActivated(WindowEvent e)
            {
                return;
            }

            public void windowDeactivated(WindowEvent e)
            {
                return;
            }
        });
        setLayout(grid);
        
        this.setSize(new Dimension(400, 150));
    }
}
