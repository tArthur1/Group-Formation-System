package data_access;

import Entities.Project;

import java.util.ArrayList;
import java.util.List;

public class InMemoryProjectDAO implements ProjectDAO {
    private List<Project> projects = new ArrayList<>();

    @Override
    public List<Project> MatchedProjects(String keywords) {

        return List.of();
    }

    // Methods to add, update, delete projects
    public void addProject(Project project) {
        projects.add(project);
    }
}