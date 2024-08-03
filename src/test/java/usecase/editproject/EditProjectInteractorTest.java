package usecase.editproject;

import api.embeddingapi.EmbeddingAPIInterface;
import api.embeddingapi.OpenAPIDataEmbed;
import dataaccess.IProjectRepository;
import dataaccess.local.LocalProjectRepository;
import entities.Project;
import entities.ProjectInterface;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import usecase.searchforproject.SearchProjectOutputBoundary;
import usecase.searchforproject.SearchProjectsInteractor;
import usecase.searchforproject.SearchProjectsPresenter;
import viewmodel.SearchPanelViewModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the EditProjectInteractor class.
 */
public class EditProjectInteractorTest {

    private final static String SAVE_LOCATION = "local_data/test/edit_projects_interactor/";
    private final static EditProjectOutputBoundary editPresenter = new EditProjectOutputBoundary() {
        @Override
        public void prepareSuccessView(EditProjectOutputData outputData) {
            assertNotNull(outputData);
            System.out.println("Success: " + outputData.getTitle());
        }

        @Override
        public void prepareFailView(String error) {
            fail("Edit project failed: " + error);
        }
    };
    private final static SearchPanelViewModel searchPanelViewModel = new SearchPanelViewModel();
    private final static SearchProjectOutputBoundary searchPresenter = new SearchProjectsPresenter(searchPanelViewModel);
    private final static IProjectRepository projectDAO = new LocalProjectRepository(SAVE_LOCATION);
    private final static EmbeddingAPIInterface apiInterface = new OpenAPIDataEmbed();
    private final static EditProjectInteractor editProjectInteractor = new EditProjectInteractor(projectDAO, editPresenter, apiInterface);
    private final static SearchProjectsInteractor searchProjectInteractor = new SearchProjectsInteractor(searchPresenter, projectDAO);

    private final static String[][] dummyProjects = new String[][]{
            {"1", "Java Project", "1000.0", "A project about Java development, focusing on building robust applications.", "Java;Programming"},
            {"2", "Python Automation", "1500.5", "A project centered around automating tasks with Python scripts.", "Python;Automation"},
            {"3", "Web Development", "2000.0", "This project involves creating responsive websites using HTML, CSS, and JavaScript.", "Web Design;JavaScript"},
            {"4", "Database Management", "1200.0", "A project to enhance database performance and security.", "SQL;Database;Security"},
            {"5", "Machine Learning", "2500.0", "Exploring machine learning algorithms to predict data trends.", "Machine Learning;Data Science"}
    };

    /**
     * Sets up the test environment before all tests.
     */
    @BeforeAll
    public static void setUp() {
        addDummyProjects();
    }

    /**
     * Adds dummy projects to the repository for testing.
     */
    private static void addDummyProjects() {
        for (String[] project : dummyProjects) {
            float[] embedding = apiInterface.getEmbedData(project[3]);
            int editorId = 0; // MAKE THIS THE EDITOR ID
            projectDAO.createProject(project[1],
                    Double.parseDouble(project[2]),
                    project[3],
                    new HashSet<>(Arrays.asList(project[4].split(";"))),
                    embedding, editorId);
        }
    }

    /**
     * Tests editing and searching for projects.
     */
    @Test
    public void testEditAndSearchProjects() {
        // Edit the project with ID 1
        int projectId = 1;
        String newTitle = "Updated Java Project";
        double newBudget = 1100.0;
        String newDescription = "An updated project about Java development, focusing on new features.";
        HashSet<String> newTags = new HashSet<>(Arrays.asList("Java", "Programming", "Updated"));

        int editorId = 0; // MAKE THIS THE PROJECT OWNER ID
        EditProjectInputData inputData = new EditProjectInputData(projectId, newTitle, newBudget, newDescription, newTags, editorId);
        editProjectInteractor.editProject(inputData);

        Project editedProject = projectDAO.getProjectById(projectId);
        assertNotNull(editedProject);
        assertEquals(newTitle, editedProject.getProjectTitle());
        assertEquals(newBudget, editedProject.getProjectBudget());
        assertEquals(newDescription, editedProject.getProjectDescription());
        assertEquals(newTags, editedProject.getProjectTags());

        // Search for projects and verify the modified project is correctly listed
        searchProjectInteractor.searchProjects("development projects");
        ArrayList<ProjectInterface> projectsRanking = searchPanelViewModel.getProject();

        assertEquals(5, projectsRanking.size());
        assertTrue(projectsRanking.stream().anyMatch(project -> "Updated Java Project".equals(project.getProjectTitle())));

        for (ProjectInterface project : projectsRanking) {
            assertNotNull(project);
            System.out.println(project.getProjectTitle());
        }
    }
}
