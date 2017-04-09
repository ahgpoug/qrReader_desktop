package ahgpoug.objects;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Task {
    private String id;
    private String taskName;
    private String groupName;
    private String expDate;
    private Boolean hasPDF;

    public Task(String id, String taskName, String groupName, String expDate){
        this.id = id;
        this.taskName = taskName;
        this.groupName = groupName;
        this.expDate = expDate;
    }

    public Task() {
        this.hasPDF = false;
    }

    public StringProperty getId() {
        return new SimpleStringProperty(id);
    }

    public StringProperty getTaskName() {
        return new SimpleStringProperty(taskName);
    }

    public StringProperty getGroupName() {
        return new SimpleStringProperty(groupName);
    }

    public StringProperty getExpDate() {
        return new SimpleStringProperty(expDate);
    }

    public Boolean isHasPDF()
    {
        return hasPDF;
    }

    public StringProperty getPDFstate(){
        if (hasPDF)
            return new SimpleStringProperty("✔");
        else
            return new SimpleStringProperty("✘");

    }

    public void setId(String id)
    {
        this.id  = id;
    }

    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public void setExpDate(String expDate)
    {
        this.expDate = expDate;
    }

    public void setHasPDF(Boolean hasPDF)
    {
        this.hasPDF = hasPDF;
    }
}
