import dataaccess.DataAccessConfig;
import usecase.acceptapplication.AcceptApplicationController;
import usecase.acceptapplication.AcceptApplicationUseCaseFactory;
import usecase.createapplication.CreateApplicationController;
import usecase.createapplication.CreateApplicationUseCaseFactory;
import usecase.createproject.CreateProjectController;
import usecase.createproject.CreateProjectUseCaseFactory;
import usecase.createuser.CreateUserController;
import usecase.createuser.CreateUserUseCaseFactory;
import usecase.deleteproject.DeleteProjectController;
import usecase.deleteproject.DeleteProjectUseCaseFactory;
import usecase.edituser.EditUserController;
import usecase.edituser.EditUserUseCaseFactory;
import usecase.getapplications.GetApplicationsController;
import usecase.getapplications.GetApplicationsUseCaseFactory;
import usecase.getloggedinuser.GetLoggedInUserController;
import usecase.getloggedinuser.GetLoggedInUserUseCaseFactory;
import usecase.getprojects.GetProjectsController;
import usecase.getprojects.GetProjectsUseCaseFactory;
import usecase.loginuser.LoginUserController;
import usecase.loginuser.LoginUserUseCaseFactory;
import usecase.logout.LogoutController;
import usecase.logout.LogoutUseCaseFactory;
import usecase.rejectapplication.RejectApplicationController;
import usecase.rejectapplication.RejectApplicationUseCaseFactory;
import usecase.searchforproject.SearchProjectController;
import usecase.searchforproject.SearchProjectUseCaseFactory;
import view.*;
import viewmodel.*;

import javax.swing.*;
import java.awt.*;

class Main {
    public static void main(String[] args) {

        JFrame application = new JFrame("A Screen");
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        application.setSize(1200, 1200);

        CardLayout cardLayout = new CardLayout();
        JPanel views = new JPanel(cardLayout);
        application.add(views);

        DataAccessConfig.initializeDatabase();

        ViewManagerModel viewManagerModel = new ViewManagerModel();
        ViewManager viewManager = new ViewManager(views, cardLayout, viewManagerModel);

        // Create User Panel
        CreateUserPanelViewModel createUserPanelViewModel = new CreateUserPanelViewModel();
        CreateUserController createUserController = CreateUserUseCaseFactory.create(createUserPanelViewModel);
        CreateUserPanel createUserPanel = new CreateUserPanel(createUserPanelViewModel, createUserController);

        // Login Panel
        LoginPanelViewModel loginPanelViewModel = new LoginPanelViewModel();
        LoginUserController loginUserController = LoginUserUseCaseFactory.create(loginPanelViewModel);
        LoginPanel loginPanel = new LoginPanel(viewManagerModel, loginPanelViewModel, loginUserController);

        // Search Project Panel
        SearchPanelViewModel searchPanelViewModel = new SearchPanelViewModel();
        SearchProjectController searchProjectController = SearchProjectUseCaseFactory.createSearchProjectController(searchPanelViewModel);
        GetLoggedInUserController searchPanelGetLoggedInUserController = GetLoggedInUserUseCaseFactory.create(searchPanelViewModel);
        CreateApplicationController createApplicationController = CreateApplicationUseCaseFactory.createController(searchPanelViewModel);
        SearchPanel searchPanel = new SearchPanel(viewManagerModel,searchPanelViewModel, searchProjectController, searchPanelGetLoggedInUserController, createApplicationController);

        // Add Project Panel
        AddProjectPanelViewModel addProjectPanelModel = new AddProjectPanelViewModel();
        CreateProjectController createProjectController = CreateProjectUseCaseFactory.createController(addProjectPanelModel);
        GetLoggedInUserController addProjectGetLoggedInUserController = GetLoggedInUserUseCaseFactory.create(addProjectPanelModel);
        AddProjectPanel addProjectPanel = new AddProjectPanel(viewManagerModel, addProjectPanelModel, createProjectController, addProjectGetLoggedInUserController);

        // My Projects Panel
        MyProjectsPanelViewModel myProjectsViewModel = new MyProjectsPanelViewModel();
        GetProjectsController getProjectsController = GetProjectsUseCaseFactory.createGetProjectsController(myProjectsViewModel);
        DeleteProjectController deleteProjectController = DeleteProjectUseCaseFactory.createDeleteProjectController(myProjectsViewModel);

        // Display Project Application View
        DisplayProjectApplicationViewModel displayProjectApplicationViewModel = new DisplayProjectApplicationViewModel();
        GetApplicationsController getApplicationsController = GetApplicationsUseCaseFactory.createController(displayProjectApplicationViewModel);
        AcceptApplicationController acceptApplicationController = AcceptApplicationUseCaseFactory.createController(displayProjectApplicationViewModel);
        RejectApplicationController rejectApplicationController = RejectApplicationUseCaseFactory.createController(displayProjectApplicationViewModel);

        MyProjectsPanel myProjectsPanel = new MyProjectsPanel(myProjectsViewModel, viewManagerModel, getProjectsController, deleteProjectController, displayProjectApplicationViewModel, getApplicationsController, acceptApplicationController, rejectApplicationController);

        // Edit Profile Panel
        EditProfileViewModel editProfileViewModel = new EditProfileViewModel();
        EditUserController editUserController = EditUserUseCaseFactory.create(editProfileViewModel);
        GetLoggedInUserController myProfileGetLoggedInUserController = GetLoggedInUserUseCaseFactory.create(editProfileViewModel);
        EditProfilePanel editProfilePanel = new EditProfilePanel(viewManagerModel, editUserController, myProfileGetLoggedInUserController);

        // add views to card layout
        views.add(createUserPanel, createUserPanelViewModel.getViewName());
        views.add(loginPanel, loginPanelViewModel.getViewName());
        views.add(searchPanel, searchPanelViewModel.getViewName());
        views.add(addProjectPanel, addProjectPanelModel.getViewName());
        views.add(myProjectsPanel, myProjectsViewModel.getViewName());
        views.add(editProfilePanel, editProfileViewModel.getViewName());

        SwitchViewButtonPanelViewModel switchViewButtonPanelViewModel = new SwitchViewButtonPanelViewModel();
        LogoutController logoutController = LogoutUseCaseFactory.create(switchViewButtonPanelViewModel);
        JPanel switchButtons = new SwitchViewButtonPanel(viewManagerModel, switchViewButtonPanelViewModel, logoutController);

        viewManagerModel.setActiveView(createUserPanelViewModel.getViewName());
        viewManagerModel.firePropertyChanged();
        viewManagerModel.logout();

        application.getContentPane().add(views, BorderLayout.CENTER);
        application.getContentPane().add(switchButtons, BorderLayout.SOUTH);

        application.pack();
        application.setVisible(true);

    }
}