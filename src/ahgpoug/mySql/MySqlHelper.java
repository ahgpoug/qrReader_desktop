package ahgpoug.mySql;

import ahgpoug.objects.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

import static ahgpoug.dbx.DbxHelper.checkAllPDFs;

public class MySqlHelper {
    public static ObservableList<Task> getAllTasks()
    {
        try {
            ObservableList<Task> tasks = FXCollections.observableArrayList();
            String link = "http://www.ahgpoug.xyz/qrreader/getAllTasks.php";

            HttpClient client = HttpClientBuilder.create().build();;
            HttpGet request = new HttpGet();
            request.setURI(new URI(link));
            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            int lineNumber = 1;

            Task task = new Task();

            while ((line = in.readLine()) != null) {
                switch (lineNumber) {
                    case 1:
                        task.setId(line);
                        lineNumber++;
                        break;
                    case 2:
                        task.setTaskName(line);
                        lineNumber++;
                        break;
                    case 3:
                        task.setGroupName(line);
                        lineNumber++;
                        break;
                    case 4:
                        task.setExpDate(line);
                        tasks.add(task);
                        task = new Task();
                        lineNumber = 1;
                        break;
                }
            }


            return checkAllPDFs(tasks);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void addNewTask(String taskName, String groupName, String expDate)
    {
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

            String link = String.format("http://www.ahgpoug.xyz/qrreader/addTask.php?id=%s&taskName=%s&groupName=%s&expDate=%s", newId, taskName.replace(" ", "%20"), groupName.replace(" ", "%20"), expDate.replace(" ", "%20"));

            HttpClient client = HttpClientBuilder.create().build();

            HttpGet request = new HttpGet();
            request.setURI(new URI(link));
            client.execute(request);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void removeTask(Task task)
    {
        try {
            String link = String.format("http://www.ahgpoug.xyz/qrreader/removeTaskById.php?id=%s", task.getId().getValue());

            HttpClient client = HttpClientBuilder.create().build();;
            HttpGet request = new HttpGet();
            request.setURI(new URI(link));
            client.execute(request);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void editExistingTask(Task task, String taskName, String groupName, String expDate)
    {
        try {
            String link = String.format("http://www.ahgpoug.xyz/qrreader/editTaskById.php?id=%s&taskName=%s&groupName=%s&expDate=%s", task.getId().getValue(), taskName.replace(" ", "%20"), groupName.replace(" ", "%20"), expDate.replace(" ", "%20"));

            HttpClient client = HttpClientBuilder.create().build();

            HttpGet request = new HttpGet();
            request.setURI(new URI(link));
            client.execute(request);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static ArrayList<String> getAllIds()
    {
        try {
            ArrayList<String> list = new ArrayList<>();

            String link = "http://www.ahgpoug.xyz/qrreader/getAllIds.php";

            HttpClient client = HttpClientBuilder.create().build();

            HttpGet request = new HttpGet();
            request.setURI(new URI(link));
            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = in.readLine()) != null) {
                list.add(line);
            }
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
}
