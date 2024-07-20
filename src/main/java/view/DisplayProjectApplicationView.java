package view;

import entities.ProjectInterface;
import usecase.acceptapplication.AcceptApplicationController;
import usecase.acceptapplication.AcceptApplicationInputData;
import usecase.acceptapplication.AcceptApplicationInteractor;
import usecase.acceptapplication.AcceptApplicationPresenter;
import usecase.getapplications.GetApplicationsController;
import usecase.getapplications.GetApplicationsInputData;
import usecase.getapplications.GetApplicationsInteractor;
import usecase.getapplications.GetApplicationsPresenter;
import usecase.rejectapplication.RejectApplicationController;
import usecase.rejectapplication.RejectApplicationInteractor;
import usecase.rejectapplication.RejectApplicationPresenter;
import view.components.ButtonAction;
import view.components.ButtonColumn;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DisplayProjectApplicationView extends JFrame implements ActionListener, PropertyChangeListener {

    private JTextField projectTitleField;
    private JTextArea projectDescriptionArea;
    private final int projectId;

    private final int[] columnWidths = {400, 200, 200, 200};
    private final String[] columnNames = {"Applicant", "View", "Accept", "Decline"};
    private final JTable infoTable = new JTable();
    private final JScrollPane infoPanel = new JScrollPane(infoTable);

    private final GetApplicationsPresenter getApplicationsPresenter;
    private final GetApplicationsController getApplicationsController;

    private final AcceptApplicationPresenter acceptApplicationPresenter;
    private final AcceptApplicationController acceptApplicationController;

    private final RejectApplicationPresenter rejectApplicationPresenter;
    private final RejectApplicationController rejectApplicationController;

    public DisplayProjectApplicationView(int projectId) {
        this.projectId = projectId;
        this.getApplicationsPresenter = new GetApplicationsPresenter(this);
        this.getApplicationsController = new GetApplicationsController(new GetApplicationsInteractor(this.getApplicationsPresenter));

        this.acceptApplicationPresenter = new AcceptApplicationPresenter(this);
        this.acceptApplicationController = new AcceptApplicationController(new AcceptApplicationInteractor(this.acceptApplicationPresenter));

        this.rejectApplicationPresenter = new RejectApplicationPresenter(this);
        this.rejectApplicationController = new RejectApplicationController(new RejectApplicationInteractor(this.rejectApplicationPresenter));


        setTitle("Project Applications");
        setSize(400, 600);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getApplicationsController.getApplicationsForProject(new GetApplicationsInputData(projectId));
    }

    public void displayApplicants(Object[][] applicationsData){
        ArrayList<ButtonAction> viewButtonActions = new ArrayList<>();
        ArrayList<ButtonAction> acceptButtonActions = new ArrayList<>();
        ArrayList<ButtonAction> declineButtonActions = new ArrayList<>();

        Object[][] info = new Object[applicationsData.length][4];

        DisplayProjectApplicationView temp = this;
        for (int i = 0; i < applicationsData.length; i++) {
            System.out.println(applicationsData[i][0]);
//            applicationsData[count][0] = user.getFirstName() + " " + user.getLastName();
//            applicationsData[count][1] = application.getSenderUserId();
//            applicationsData[count][2] = application.getText();
//            applicationsData[count][3] = application.getPdfBytes();
            info[i][0] = applicationsData[i][0];
            info[i][1] = "View";
            info[i][2] = "Accept";
            info[i][3] = "Decline";
            int finalI = i;
            viewButtonActions.add(new ButtonAction() {
                @Override
                public void onClick() {
                    System.out.println("clicked on edit for " );
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                    Path downloadsPath = Paths.get(System.getProperty("user.home"), "Downloads");
                    File downloadsDirectory = downloadsPath.toFile();
                    if (downloadsDirectory.exists() && downloadsDirectory.isDirectory()) {
                        fileChooser.setCurrentDirectory(downloadsDirectory);
                    }

                    int result = fileChooser.showOpenDialog(temp);
                    if (result == JFileChooser.APPROVE_OPTION) {

                        File selectedDirectory = fileChooser.getSelectedFile();

                        File outputFile = new File(selectedDirectory, applicationsData[finalI][0]+" application.pdf");

                        try(FileOutputStream fos = new FileOutputStream(outputFile)){
                            fos.write((byte[])applicationsData[finalI][3]);
                        } catch (IOException ex){
                            JOptionPane.showMessageDialog(temp, "Error saving file: " + ex.getMessage());
                        }
                    }
                }
            });
            acceptButtonActions.add(new ButtonAction() {
                @Override
                public void onClick() {
                    JOptionPane.showMessageDialog(null, "Accepted application for: " + applicationsData[finalI][0]);
                    acceptApplicationController.acceptApplicant(projectId, (Integer) applicationsData[finalI][1]);
                    getApplicationsController.getApplicationsForProject(new GetApplicationsInputData(projectId));
                }
            });
            declineButtonActions.add(new ButtonAction() {
                @Override
                public void onClick() {
                    JOptionPane.showMessageDialog(null, "Rejected application for: " + applicationsData[finalI][0]);
                    rejectApplicationController.rejectApplicant(projectId, (Integer) applicationsData[finalI][1]);
                    getApplicationsController.getApplicationsForProject(new GetApplicationsInputData(projectId));
                }
            });
        }

        DefaultTableModel infoTableModel = new DefaultTableModel(info, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make only the button column editable
                return column == 1 || column == 2 || column == 3;
            }
        };
        infoTable.setModel(infoTableModel);

        ButtonColumn viewColumn = new ButtonColumn(infoTable, 1);
        viewColumn.setActions(viewButtonActions);

        ButtonColumn acceptColumn = new ButtonColumn(infoTable, 2);
        acceptColumn.setActions(acceptButtonActions);

        ButtonColumn declineColumn = new ButtonColumn(infoTable, 3);
        declineColumn.setActions(declineButtonActions);

        TableColumnModel columnModel = infoTable.getColumnModel();
        for (int i = 0; i < columnWidths.length; i++) {
            columnModel.getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

        projectTitleField = new JTextField("test");
        projectTitleField.setEditable(false);
        projectDescriptionArea = new JTextArea("desc");
        projectDescriptionArea.setEditable(false);
        // Project Title
        detailsPanel.add(new JLabel("Project Title:"  ));

        // Project ID
        detailsPanel.add(new JLabel("Project ID:"));

        // Project Tags
        detailsPanel.add(new JLabel("Tags:"  ));

        add(infoTable);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals("displayDetailProject")){
            ProjectInterface project = (ProjectInterface) evt.getNewValue();
            projectTitleField.setText(project.getProjectTitle());
        }
    }
}
