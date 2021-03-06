package org.secfirst.umbrella.models;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;

@DatabaseTable(tableName = "forms")
public class Form {
    public static final String FIELD_ID = "_id";
    public static final String FIELD_IDSTRING = "id";
    public static final String FIELD_TITLE = "title";
    @DatabaseField(columnName = FIELD_ID, generatedId = true, allowGeneratedIdInsert = true)
    private int _id;
    @DatabaseField(columnName = FIELD_TITLE)
    @SerializedName("name")
    String title;
    @DatabaseField(persisted = false)
    ArrayList<FormScreen> screenArrayList;
    @ForeignCollectionField(eager = true)
    ForeignCollection<FormScreen> screens;
    @DatabaseField(columnName = FIELD_IDSTRING)
    @SerializedName("id")
    private String id;

    public Form() {}

    public Form(String title) {
        this.title = title;
    }

    public Form(String title, ForeignCollection<FormScreen> screens) {
        this.title = title;
        setScreens(screens);
    }

    public void setScreenArrayList(ArrayList<FormScreen> screenArrayList) {
        this.screenArrayList = screenArrayList;
    }

    public ArrayList<FormScreen> getScreenArrayList() {
        if (screenArrayList==null) screenArrayList = new ArrayList<FormScreen>();
        return screenArrayList;
    }

    public int get_id() {
        return _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ForeignCollection<FormScreen> getScreens() {
        return screens;
    }

    public void setScreens(ForeignCollection<FormScreen> screens) {
        this.screens = screens;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
