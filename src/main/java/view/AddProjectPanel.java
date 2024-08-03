package view;

import entities.User;
import usecase.createproject.CreateProjectController;
import usecase.getloggedinuser.GetLoggedInUserController;
import usecase.manageprojects.ManageProjectsController;
import view.components.NumericTextField;
import viewmodel.AddProjectPanelViewModel;
import viewmodel.ViewManagerModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;

/**
 * Panel for adding a new project.
 */
public class AddProjectPanel extends JPanel implements ActionListener, PropertyChangeListener {

    private final AddProjectPanelViewModel addProjectPanelViewModel;
    private final ManageProjectsController createProjectController;
    private final GetLoggedInUserController getLoggedInUserController;
    private final ViewManagerModel viewManagerModel;

    private final JPanel projectInfoPanel = new JPanel();
    private final JPanel projectDataPanel = new JPanel();
    private final GridLayout projectDataGridLayout = new GridLayout(0, 2);
    private final JLabel projectNameLabel = new JLabel("Project Name");
    private final JLabel projectBudgetLabel = new JLabel("Project Budget");
    private final JLabel projectDescriptionLabel = new JLabel("Project Description");
    private final JLabel projectTagsLabel = new JLabel("Project Tags To Add");
    private final JTextField projectNameField = new JTextField();
    private final NumericTextField projectBudgetField = new NumericTextField();
    private final JTextField projectDescriptionField = new JTextField();

    private final JTextField projectTagsField = new JTextField();
    private final JButton addTagButton = new JButton("Add Tag");
    private final GridLayout tagPanelLayout = new GridLayout(0, 1);
    private final JPanel tagPanel = new JPanel();
    private final JLabel tagPanelLabel = new JLabel("Project tags (Press add tag to add): ");

    private final JButton addProjectButton = new JButton("Create project");

    private final HashSet<String> tags = new HashSet<>();

    /**
     * Constructs an AddProjectPanel.
     *
     * @param viewManagerModel the view manager model
     * @param addProjectPanelViewModel the view model for the add project panel
     * @param createProjectController the controller for creating projects
     * @param getLoggedInUserController the controller for getting the logged-in user
     */
    public AddProjectPanel(ViewManagerModel viewManagerModel,
                           AddProjectPanelViewModel addProjectPanelViewModel,
                           ManageProjectsController createProjectController,
                           GetLoggedInUserController getLoggedInUserController) {
        this.viewManagerModel = viewManagerModel;
        viewManagerModel.addPropertyChangeListener(this);

        this.getLoggedInUserController = getLoggedInUserController;
        this.addProjectPanelViewModel = addProjectPanelViewModel;
        addProjectPanelViewModel.addPropertyChangeListener(this);
        this.createProjectController = createProjectController;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        projectInfoPanel.setLayout(new BoxLayout(projectInfoPanel, BoxLayout.Y_AXIS));

        addTagButton.addActionListener(e -> {
            String tagText = projectTagsField.getText();
            if (!tagText.isEmpty()) {
                addTagToPanel(tagText);
                tags.add(tagText);
                projectTagsField.setText("");
            }
        });

        projectDataPanel.setLayout(projectDataGridLayout);

        projectDataPanel.add(projectNameLabel);
        projectDataPanel.add(projectNameField);
        projectDataPanel.add(projectBudgetLabel);
        projectDataPanel.add(projectBudgetField);
        projectDataPanel.add(projectDescriptionLabel);
        projectDataPanel.add(projectDescriptionField);
        projectDataPanel.add(projectTagsLabel);
        projectDataPanel.add(projectTagsField);
        projectDataPanel.add(addTagButton);

        projectInfoPanel.add(projectDataPanel);

        tagPanel.add(tagPanelLabel);
        tagPanel.setLayout(tagPanelLayout);
        tagPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        projectInfoPanel.add(tagPanel);

        this.add(projectInfoPanel);

        addProjectButton.addActionListener(e -> {
            String title = projectNameField.getText();
            double budget = Double.parseDouble(projectBudgetField.getText());
            String description = projectDescriptionField.getText();
            User loggedInUser = addProjectPanelViewModel.getLoggedInUser();
            if (loggedInUser == null) {
                JOptionPane.showMessageDialog(null, "You must be logged in to create a project.");
                return;
            }
            createProjectController.createProject(title, budget, description, tags, loggedInUser.getUserId());
        });

        this.add(addProjectButton);

    }

    /**
     * Adds a tag to the panel.
     *
     * @param text the tag text
     */
    private void addTagToPanel(String text) {

        if (text.isEmpty()) {
            return;
        }

        if (tags.contains(text)) {
            return;
        }

        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(Color.LIGHT_GRAY);
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JButton removeButton = new JButton("x");

        JPanel tag = new JPanel();
        tag.setBorder(new EmptyBorder(0, 10, 0, 10));
        tag.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 0;

        constraints.gridx = 0;
        constraints.weightx = 0.8;
        tag.add(label, constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.2;
        tag.add(removeButton, constraints);

        removeButton.addActionListener(e -> {
            tags.remove(text);
            tagPanel.remove(tag);
            tagPanel.revalidate();
            tagPanel.repaint();
        });

        tagPanel.add(tag);
        tagPanel.revalidate();
        tagPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // No implementation needed
    }

    /**
     * Clears the panel fields and tags.
     */
    private void clearPanel(){
        projectNameField.setText("");
        projectBudgetField.clear();
        projectDescriptionField.setText("");
        projectTagsField.setText("");
        tags.clear();
        tagPanel.removeAll();
        tagPanel.revalidate();
        tagPanel.repaint();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("success")) {
            boolean success = (boolean) evt.getNewValue();
            String projectName = addProjectPanelViewModel.getProjectName();
            if (success){
                JOptionPane.showMessageDialog(null, "Project " + projectName + " created successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearPanel();
                viewManagerModel.addProjectEvent();
            }
            else {
                JOptionPane.showMessageDialog(null, addProjectPanelViewModel.getErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        if (evt.getPropertyName().equals("login")) {
            getLoggedInUserController.getLoggedInUser();
        }
    }

}
