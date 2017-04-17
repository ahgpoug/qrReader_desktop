package ahgpoug.sqlite;

import ahgpoug.dbx.DbxHelper;
import ahgpoug.objects.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.sqlite.SQLiteException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

public class SqliteHelper {
    public static ObservableList<Task> readAllTasks(){
        ObservableList<Task> tasks = FXCollections.observableArrayList();

        try {
            Statement statement = getConnection();
            ResultSet rs = statement.executeQuery("SELECT * FROM assoc");
            while(rs.next())
            {
                Task task = new Task();
                task.setId(rs.getString("id"));
                task.setTaskName(rs.getString("taskName"));
                task.setGroupName(rs.getString("groupName"));
                task.setExpDate(rs.getString("expDate"));
                tasks.add(task);
            }
        } catch (SQLiteException e) {
            System.out.println("Creating new database...");
            createNewDb();
        } catch (Exception e){
            e.printStackTrace();
        }

        return DbxHelper.PDF.checkAllPDFs(tasks);
    }

    public static void addNewTask(String taskName, String groupName, String expDate) {
        try {
            ArrayList<String> ids = getAllIds();
            String newId = "";

            if (ids != null) {
                do {
                    newId = generateNewId();
                } while ((newId.equals("")) && (ids.contains(newId)));
            } else {
                newId = generateNewId();
            }

            Statement statement = getConnection();
            String query = String.format("INSERT INTO assoc VALUES ('%s', '%s', '%s', '%s')", newId, taskName, groupName, expDate);
            statement.executeUpdate(query);
            DbxHelper.Files.uploadDb();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void removeTask(Task task) {
        try {
            Statement statement = getConnection();
            String query = String.format("DELETE FROM assoc WHERE id='%s'", task.getId().getValue());
            statement.executeUpdate(query);
            DbxHelper.Files.uploadDb();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void editExistingTask(Task task, String taskName, String groupName, String expDate) {
        try {
            Statement statement = getConnection();
            String query = String.format("UPDATE assoc SET taskName = '%s', groupName = '%s', expDate = '%s' WHERE id = '%s'",
                    taskName, groupName, expDate, task.getId().getValue());
            statement.executeUpdate(query);
            DbxHelper.Files.uploadDb();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static ArrayList<String> getAllIds() {
        try {
            ArrayList<String> list = new ArrayList<>();

            Statement statement = getConnection();
            ResultSet rs = statement.executeQuery("SELECT * FROM assoc");
            while(rs.next())
                list.add(rs.getString("id"));
            return list;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static String generateNewId()
    {
        return UUID.randomUUID().toString();
    }

    private static void createNewDb(){
        try {
            Statement statement = getConnection();
            statement.executeUpdate("CREATE TABLE assoc (id TEXT NOT NULL PRIMARY KEY, " +
                    "taskName TEXT NOT NULL, " +
                    "groupName TEXT NOT NULL, " +
                    "expDate TEXT NOT NULL)");

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static Statement getConnection() throws Exception{
        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection("jdbc:sqlite:sqlite.db");
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);
        return statement;
    }
}
