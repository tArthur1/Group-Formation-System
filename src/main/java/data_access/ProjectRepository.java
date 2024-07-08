package data_access;

import Entities.Project;

import java.sql.*;
import java.util.HashSet;

public class ProjectRepository extends SQLDatabaseManager implements IProjectRepository {

    /**
     * Constructs a ProjectRepository object.
     *
     * @param databaseName The name of the database to manage. Note that this must include a '.db' file extension.
     */
    public ProjectRepository(String databaseName) {
        super(databaseName);
    }

    /**
     * Initializes the database with the required tables if they do not already exist.
     */
    @Override
    public void initialize() {
        String projectSql = "CREATE TABLE IF NOT EXISTS Projects (Id INTEGER PRIMARY KEY AUTOINCREMENT, Title TEXT NOT NULL, Budget DOUBLE, Description TEXT NOT NULL)";
        String projectTagsSql = "CREATE TABLE IF NOT EXISTS ProjectTags (ProjectId INTEGER NOT NULL, Tag TEXT NOT NULL, PRIMARY KEY(ProjectId, Tag), FOREIGN KEY(ProjectId) REFERENCES Projects(Id))";
        Connection connection = super.getConnection();

        try {
            connection.setAutoCommit(false);

            try (Statement transaction = connection.createStatement()) {
                transaction.executeUpdate(projectSql);
                transaction.executeUpdate(projectTagsSql);
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch(SQLException rollbackException) {
                System.err.println(rollbackException.getMessage());
            }
            System.err.println(e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch(SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Adds a set of tags to the database. More specifically, the tags are added to the
     * ProjectTags table.
     *
     * @param projectId the id of the project to add the tags to.
     * @param tags a set of tags associated with the project.
     */
    @Override
    public void addTags(int projectId, HashSet<String> tags) {
        String sql = "INSERT INTO ProjectTags (ProjectId, Tag) VALUES (?, ?)";

        executeTagUpdates(projectId, tags, sql);
    }

    /**
     * Removes a set of tags to the database. More specifically, the tags are removed from the
     * ProjectTags table.
     *
     * @param projectId the id of the project to add the tags to.
     * @param tags a set of tags associated with the project.
     */
    @Override
    public void removeTags(int projectId, HashSet<String> tags) {
        String sql = "DELETE FROM ProjectTags WHERE ProjectId = ? AND Tag = ?";

        executeTagUpdates(projectId, tags, sql);
    }

    /**
     * Executes a batch of SQL statements for tag updates.
     *
     * @param projectId the id of the project.
     * @param tags a set of tags associated with the project.
     * @param sql the SQL statement to execute in batch.
     */
    private void executeTagUpdates(int projectId, HashSet<String> tags, String sql) {
        Connection connection = super.getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (String tag : tags) {
                preparedStatement.setInt(1, projectId);
                preparedStatement.setString(2, tag);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch(SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Creates a project in the database. Note that all validation should be done
     * prior to this method in accordance to clean architecture. Particularly, validation
     * should be done in the service class / (data access object ?).
     *
     * @param title the title of the project.
     * @param budget the budget of the project.
     * @param description the description of the project.
     * @param tags a set of tags associated with the project.
     * @return a Project object corresponding to the created project. Otherwise, null.
     */
    @Override
    public Project createProject(String title, double budget, String description, HashSet<String> tags) {
        String projectSql = "INSERT INTO Projects (Title, Budget, Description) VALUES (?, ?, ?)";

        Connection connection = super.getConnection();

        try {
            connection.setAutoCommit(false); // begin transaction

            try (PreparedStatement projectStatement = connection.prepareStatement(projectSql, Statement.RETURN_GENERATED_KEYS)) {
                projectStatement.setString(1, title);
                projectStatement.setDouble(2, budget);
                projectStatement.setString(3, description);

                int affectedRows = projectStatement.executeUpdate();

                if (affectedRows > 0) { // project was added => key generated
                    try (ResultSet keys = projectStatement.getGeneratedKeys()) {
                        if (keys.next()) {
                            int projectId = keys.getInt(1);

                            this.addTags(projectId, tags); // insert tags into ProjectTags table

                            connection.commit(); // end transaction
                            return new Project(projectId, title, budget, description, tags);
                        }
                    }
                }
            }

        } catch(SQLException e) {
            try {
                connection.rollback();
            } catch(SQLException rollbackException) {
                System.err.println(rollbackException.getMessage());
            }
            System.err.println(e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch(SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return null;
    }

    /**
     * Deletes the project associated with the given project ID from the database.
     *
     * @param projectId The ID of the project to delete.
     */
    @Override
    public void deleteProject(int projectId) {
        String deleteProjectSql = "DELETE FROM Projects WHERE Id = ?";
        String deleteProjectTagSql  = "DELETE FROM ProjectTags WHERE ProjectId = ?";

        Connection connection = super.getConnection();

        try {
            connection.setAutoCommit(false); // begin transaction

            try (PreparedStatement deleteProjectStatement = connection.prepareStatement(deleteProjectSql);
                 PreparedStatement deleteProjectTagsStatement = connection.prepareStatement(deleteProjectTagSql)) {
                deleteProjectStatement.setInt(1, projectId);
                deleteProjectStatement.executeUpdate();

                deleteProjectTagsStatement.setInt(1, projectId);
                deleteProjectTagsStatement.executeUpdate();

                connection.commit(); // end transaction
            }
        } catch(SQLException e) {
            try {
                connection.rollback();
            } catch(SQLException rollbackException) {
                System.err.println(rollbackException.getMessage());
            }
            System.err.println(e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Retrieves a project from the database by its ID.
     *
     * @param projectId The ID of the project to retrieve.
     * @return A Project object representing the retrieved project, or null if the project is not found.
     */
    @Override
    public Project getProjectById(int projectId) {
        String sql = "SELECT Title, Budget, Description FROM Projects WHERE Id = ?";

        try (Connection connection = super.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, projectId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("Title");
                    double budget = rs.getDouble("Budget");
                    String description = rs.getString("Description");
                    return new Project(projectId, title, budget, description, new HashSet<>());
                }
            }
        } catch(SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a set of projects that match the given keyword in their title, description, or tags.
     *
     * @param keyword The keyword to search for in the project title, description, and tags.
     * @return A HashSet of Project objects that match the search criteria.
     */
    @Override
    public HashSet<Project> getProjectsByKeyword(String keyword) {
        HashSet<Project> projects = new HashSet<>();

        String sql = "SELECT DISTINCT Projects.Id, Projects.Title, Projects.Budget, Projects.Description FROM Projects " +
                "LEFT JOIN ProjectTags ON Projects.Id = ProjectTags.ProjectId " +
                "WHERE Projects.Title LIKE ? OR Projects.Description LIKE ? OR ProjectTags.Tag LIKE ?;";
        Connection connection = super.getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            String queryKeyword = "%" + keyword + "%";
            preparedStatement.setString(1, queryKeyword);
            preparedStatement.setString(2, queryKeyword);
            preparedStatement.setString(3, queryKeyword);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    int projectId = rs.getInt("Id");
                    String title = rs.getString("Title");
                    double budget = rs.getDouble("Budget");
                    String description = rs.getString("Description");
                    HashSet<String> tags = this.getTagsForProject(projectId);

                    projects.add(new Project(projectId, title, budget, description, tags));
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return projects;
    }

    /**
     * Retrieves a set of tags associated with a given project ID.
     *
     * @param projectId The ID of the project to retrieve tags for.
     * @return A HashSet of tags associated with the specified project.
     */
    private HashSet<String> getTagsForProject(int projectId) {
        String sql = "SELECT Tag FROM ProjectTags WHERE ProjectId = ?";
        HashSet<String> tags = new HashSet<>();

        Connection connection = super.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, projectId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    tags.add(rs.getString("Tag"));
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return tags;
    }
}