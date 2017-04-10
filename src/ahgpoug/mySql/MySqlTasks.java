package ahgpoug.mySql;

import ahgpoug.dbx.DbxHelper;
import ahgpoug.objects.Task;
import javafx.collections.ObservableList;

public final class MySqlTasks {
    public class GetAllTasks extends javafx.concurrent.Task<ObservableList<Task>>
    {
        @Override
        protected ObservableList<Task> call() throws Exception
        {
            return MySqlHelper.getAllTasks();
        }
    }

    public class AddNewTask extends javafx.concurrent.Task<Boolean>
    {
        private String taskName;
        private String groupName;
        private String expDate;

        public AddNewTask(String taskName, String groupName, String expDate){
            this.taskName = taskName;
            this.groupName = groupName;
            this.expDate = expDate;
        }

        @Override
        protected Boolean call() throws Exception
        {
            MySqlHelper.addNewTask(taskName, groupName, expDate);
            DbxHelper.Folders.createFolders(groupName, taskName);
            return true;
        }
    }
}
