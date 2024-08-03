package view;

import usecase.acceptapplication.AcceptApplicationController;
import usecase.deleteproject.DeleteProjectController;
import usecase.editproject.EditProjectController;
import usecase.editproject.EditProjectInputData;
import usecase.getapplications.GetApplicationsController;
import usecase.rejectapplication.RejectApplicationController;
import view.services.hovervoice.HoverVoiceServiceConfig;
import view.services.hovervoice.IHoverVoiceService;
import view.services.playvoice.IPlayVoiceService;
import view.services.playvoice.PlayVoiceServiceConfig;
import viewmodel.DisplayProjectApplicationViewModel;
import viewmodel.EditProjectPanelViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;

/**
 * A panel for editing project details.
 */
public class EditProjectPanel extends JPanel implements PropertyChangeListener {

    private final EditProjectPanelViewModel editProjectViewModel;
    private final EditProjectController editProjectController;
    private final GetApplicationsController getApplicationsController;
    private final DeleteProjectController deleteProjectController;
    private final DisplayProjectApplicationViewModel displayProjectApplicationViewModel;
    private final AcceptApplicationController acceptApplicationController;
    private final RejectApplicationController rejectApplicationController;
    private JTextField titleField;
    private JTextField budgetField;
    private JTextArea descriptionField;
    private JTextField tagsField;
    private JButton saveButton;
    private JButton refreshButton;
    private JButton viewApplicationButton;
    private JButton deleteButton;
    private int projectId;
    private int editorId;

    private final IHoverVoiceService hoverVoiceService;
    private final IPlayVoiceService playVoiceService;

    /**
     * Constructs an EditProjectPanel.
     *
     * @param editProjectViewModel the view model for editing the project
     * @param editProjectController the controller for editing the project
     * @param getApplicationsController the controller for getting applications
     * @param deleteProjectController the controller for deleting the project
     * @param displayProjectApplicationViewModel the view model for displaying project applications
     * @param acceptApplicationController the controller for accepting applications
     * @param rejectApplicationController the controller for rejecting applications
     */
    public EditProjectPanel(
            EditProjectPanelViewModel editProjectViewModel,
            EditProjectController editProjectController,
            GetApplicationsController getApplicationsController,
            DeleteProjectController deleteProjectController,
            DisplayProjectApplicationViewModel displayProjectApplicationViewModel,
            AcceptApplicationController acceptApplicationController,
            RejectApplicationController rejectApplicationController) {
        this.editProjectViewModel = editProjectViewModel;
        this.editProjectViewModel.addPropertyChangeListener(this);
        this.editProjectController = editProjectController;
        this.getApplicationsController = getApplicationsController;
        this.deleteProjectController = deleteProjectController;
        this.displayProjectApplicationViewModel = displayProjectApplicationViewModel;
        this.acceptApplicationController = acceptApplicationController;
        this.rejectApplicationController = rejectApplicationController;

        this.hoverVoiceService = HoverVoiceServiceConfig.getHoverVoiceService();
        this.playVoiceService = PlayVoiceServiceConfig.getPlayVoiceService();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        titleField = new JTextField();
        budgetField = new JTextField();
        descriptionField = new JTextArea();
        tagsField = new JTextField();
        saveButton = new JButton("Save");
        viewApplicationButton = new JButton("View Applications");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("Refresh");

        hoverVoiceService.addHoverVoice(titleField, "Enter new project title here");
        hoverVoiceService.addHoverVoice(budgetField, "Enter new project budget here");
        hoverVoiceService.addHoverVoice(descriptionField, "Enter new project description here");
        hoverVoiceService.addHoverVoice(tagsField, "Enter new project tags here");
        hoverVoiceService.addHoverVoice(saveButton, "Press to save project");
        hoverVoiceService.addHoverVoice(viewApplicationButton, "Press to view applications");
        hoverVoiceService.addHoverVoice(deleteButton, "Press to delete project");
        hoverVoiceService.addHoverVoice(refreshButton, "Press to refresh project");

        add(new JLabel("Project Title:"));
        add(titleField);
        add(new JLabel("Budget:"));
        add(budgetField);
        add(new JLabel("Description:"));
        add(new JScrollPane(descriptionField));
        add(new JLabel("Tags (comma separated):"));
        add(tagsField);
        add(saveButton);
        add(viewApplicationButton);
        add(deleteButton);
        add(refreshButton);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProject();
            }
        });

        viewApplicationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                new DisplayProjectApplicationView(projectId,
                        displayProjectApplicationViewModel,
                        getApplicationsController,
                        acceptApplicationController,
                        rejectApplicationController);

            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int dialogResult = JOptionPane.showConfirmDialog (null,
                        "Are you sure you would like to delete " + projectId + "?",
                        "Warning",
                        JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION){
                    deleteProjectController.deleteProject(projectId);
                }

            }
        });

    }

    /**
     * Sets the project details in the panel.
     *
     * @param projectId the ID of the project
     * @param editorId the ID of the editor
     * @param title the title of the project
     * @param budget the budget of the project
     * @param description the description of the project
     * @param tags the tags associated with the project
     */
    public void setProjectDetails(int projectId, int editorId, String title, double budget, String description, HashSet<String> tags) {
        this.projectId = projectId;
        this.editorId = editorId;
        titleField.setText(title);
        budgetField.setText(String.valueOf(budget));
        descriptionField.setText(description);
        tagsField.setText(String.join(", ", tags));
    }

    /**
     * Saves the project details by calling the edit project controller.
     */
    private void saveProject() {
        String newTitle = titleField.getText();
        double newBudget = Double.parseDouble(budgetField.getText());
        String newDescription = descriptionField.getText();
        HashSet<String> newTags = new HashSet<>();
        for (String tag : tagsField.getText().split(",")) {
            newTags.add(tag.trim());
        }
        editProjectController.editProject(projectId, newTitle, newBudget, newDescription, newTags, editorId);
    }

    /**
     * Refreshes the project details from the view model.
     */
    private void refreshProject() {
        // Assume the view model already has the current project details
        titleField.setText(editProjectViewModel.getTitle());
        budgetField.setText(String.valueOf(editProjectViewModel.getBudget()));
        descriptionField.setText(editProjectViewModel.getDescription());
        tagsField.setText(String.join(", ", editProjectViewModel.getTags()));
        editorId = editProjectViewModel.getEditorId();
        projectId =  editProjectViewModel.getProjectId();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("detailInit")) {
            refreshProject();
        }
        if (evt.getPropertyName().equals("editSuccess")) {
            refreshProject();
            Boolean success = (Boolean) evt.getNewValue();
            if (success) {
                playVoiceService.playVoice("Project updated successfully!");
                JOptionPane.showMessageDialog(null, "Project updated successfully!");
            } else {
                playVoiceService.playVoice("Failed to update project: " + editProjectViewModel.getErrorMessage());
                JOptionPane.showMessageDialog(null, "Failed to update project: " + editProjectViewModel.getErrorMessage());
            }
        }
    }
}
