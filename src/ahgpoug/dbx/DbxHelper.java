package ahgpoug.dbx;

import ahgpoug.objects.Task;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import javafx.collections.ObservableList;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class DbxHelper {
    private static String ACCESS_TOKEN = "Gtb6zMf7DEIAAAAAAAABGVMypi2U4fkUrY9AZlwbatBtfxILfxX-IXMNaoSQmGkP";

    public static class PDF{
        public static ObservableList<Task> checkAllPDFs(ObservableList<Task> list){
            ArrayList<String> PDFs = getAllPDFs();
            for (Task task : list){
                if (PDFs.contains(task.getId().getValue()))
                    task.setHasPDF(true);
                else
                    task.setHasPDF(false);
            }
            return list;
        }

        private static ArrayList<String> getAllPDFs(){
            DbxRequestConfig config = new DbxRequestConfig("dropbox/desktopClient1");
            DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
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
                e.printStackTrace();
            }

            return PDFs;
        }
    }

    public static class Folders{
        public static void createFolders(String groupName, String taskName){
            DbxRequestConfig config = new DbxRequestConfig("dropbox/desktopClient1");
            DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

            try {
                String path = String.format("/%s/%s", groupName, taskName);
                client.files().createFolder(path);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        public static void removeFolder(Task task){
            DbxRequestConfig config = new DbxRequestConfig("dropbox/desktopClient1");
            DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

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
                e.printStackTrace();
            }
        }

        public static void showFolder(Task task){
            DbxRequestConfig config = new DbxRequestConfig("dropbox/desktopClient1");
            DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
            String url = "http://ahgpoug.xyz/error";

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
        public static void uploadFile(File file, Task task){
            DbxRequestConfig config = new DbxRequestConfig("dropbox/desktopClient1");
            DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

            try (InputStream in = new FileInputStream(file)) {
                String path = String.format("/Задания/%s.pdf", task.getId().getValue());
                removeFile(task);
                client.files().uploadBuilder(path).uploadAndFinish(in);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        public static void removeFile(Task task){
            DbxRequestConfig config = new DbxRequestConfig("dropbox/desktopClient1");
            DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

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

        public static void showFile(Task task){
            DbxRequestConfig config = new DbxRequestConfig("dropbox/desktopClient1");
            DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
            String url = "http://ahgpoug.xyz/error";

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
