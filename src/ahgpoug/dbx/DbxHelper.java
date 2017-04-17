package ahgpoug.dbx;

import ahgpoug.objects.Task;
import ahgpoug.util.Globals;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import javafx.collections.ObservableList;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class DbxHelper {
    private static DbxRequestConfig config = new DbxRequestConfig("dropbox/desktopClient1");

    public static class PDF{
        public static ObservableList<Task> checkAllPDFs(ObservableList<Task> list){
            ArrayList<String> PDFs = getAllPDFs();

            if (PDFs == null)
                return null;

            for (Task task : list){
                if (PDFs.contains(task.getId().getValue()))
                    task.setHasPDF(true);
                else
                    task.setHasPDF(false);
            }
            return list;
        }

        private static ArrayList<String> getAllPDFs(){
            DbxClientV2 client = new DbxClientV2(config, Globals.dbxToken);
            ArrayList<String> PDFs = new ArrayList<>();

            try {
                ListFolderResult result = client.files().listFolder("/Задания/");
                while (true) {
                    if (!result.getHasMore()) {
                        break;
                    }

                    result = client.files().listFolderContinue(result.getCursor());
                }

                for (Metadata metadata : result.getEntries()) {
                    PDFs.add(metadata.getName().replace(".pdf", ""));
                }
            } catch (Exception e){
                System.out.println("Invalid token");
                PDFs = null;
            }

            return PDFs;
        }
    }

    public static class Folders{
        public static void createFolders(String groupName, String taskName){
            DbxClientV2 client = new DbxClientV2(config, Globals.dbxToken);

            try {
                String path = String.format("/%s/%s", groupName, taskName);
                client.files().createFolder(path);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        public static void removeFolder(Task task){
            DbxClientV2 client = new DbxClientV2(config, Globals.dbxToken);

            try {
                List<Metadata> result = client.files().listFolder(String.format("/%s/", task.getGroupName().getValue())).getEntries();
                for (Metadata entry : result) {
                    if (entry.getName().equals(task.getTaskName().getValue())) {
                        client.files().delete(entry.getPathDisplay());
                        break;
                    }
                }

                result = client.files().listFolder(String.format("/%s/", task.getGroupName().getValue())).getEntries();
                if (result.size() == 0)
                    client.files().delete(String.format("/%s", task.getGroupName().getValue()));
            } catch (Exception e) {
                System.out.println("Folder doesn't exist");
            }
        }

        public static void showFolder(Task task){
            DbxClientV2 client = new DbxClientV2(config, Globals.dbxToken);
            String url = "https://www.dropbox.com/404";

            try {
                String path = String.format("/%s/%s", task.getGroupName().getValue(), task.getTaskName().getValue());

                List<SharedLinkMetadata> links = client.sharing().listSharedLinksBuilder().withPath(path).start().getLinks();

                if (links.size() == 0)
                    url = client.sharing().createSharedLinkWithSettings(path).getUrl();
                else
                    url = links.get(0).getUrl();
            } catch (Exception e){
                e.printStackTrace();
            }

            openWebpage(URI.create(url));
        }
    }

    public static class Files{
        public static void uploadTaskPDF(File file, Task task){
            DbxClientV2 client = new DbxClientV2(config, Globals.dbxToken);

            try (InputStream in = new FileInputStream(file)) {
                String path = String.format("/Задания/%s.pdf", task.getId().getValue());
                removeTaskPDF(task);
                client.files().uploadBuilder(path).uploadAndFinish(in);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        public static void removeTaskPDF(Task task){
            DbxClientV2 client = new DbxClientV2(config, Globals.dbxToken);

            try {
                List<Metadata> result = client.files().listFolder("/Задания/").getEntries();
                for (Metadata entry : result) {
                    if (entry.getName().replace(".pdf", "").equals(task.getId().getValue())) {
                        client.files().delete(entry.getPathDisplay());
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void showTaskPDF(Task task){
            DbxClientV2 client = new DbxClientV2(config, Globals.dbxToken);
            String url = "https://www.dropbox.com/404";

            try {
                String path = String.format("/Задания/%s.pdf", task.getId().getValue());

                List<SharedLinkMetadata> links = client.sharing().listSharedLinksBuilder().withPath(path).withDirectOnly(true).start().getLinks();

                if (links.size() == 0)
                    url = client.sharing().createSharedLinkWithSettings(path).getUrl();
                else
                    url = links.get(0).getUrl();
            } catch (Exception e){
                e.printStackTrace();
            }

            openWebpage(URI.create(url));
        }

        public static void uploadDb(){
            DbxClientV2 client = new DbxClientV2(config, Globals.dbxToken);
            File file = new File("sqlite.db");

            try (InputStream in = new FileInputStream(file)) {
                removeDb();
                client.files().uploadBuilder("/sqlite.db").uploadAndFinish(in);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        public static void downloadDb(){
            DbxClientV2 client = new DbxClientV2(config, Globals.dbxToken);

            File file = new File("sqlite.db");

            try {
                OutputStream out = new FileOutputStream(file);
                client.files().download("/sqlite.db").download(out);
            } catch (Exception e) {
                System.out.println("Invalid token");
            }
        }

        private static void removeDb(){
            DbxClientV2 client = new DbxClientV2(config, Globals.dbxToken);

            try {
                List<Metadata> result = client.files().listFolder("").getEntries();
                for (Metadata entry : result) {
                    if (entry.getName().equals("sqlite.db")){
                        client.files().delete(entry.getPathDisplay());
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class Token{
        public static boolean checkToken(String token){
            boolean result = true;
            try {
                DbxClientV2 client = new DbxClientV2(config, token);
                client.files().listFolder("").getEntries();
            } catch (Exception e){
                System.out.println("Invalid token");
                result = false;
            }
            return result;
        }
    }

    private static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
