package com.iogroup.chms.app;

/**
 * Created by AV on 30/04/2015.
 */
public class PatientListItem {
    String id;
    String name;
    String ward;
    String room;
    String bed;

    public PatientListItem(){}
    public PatientListItem(String id ,String name ,String ward ,String room ,String bed){
        this.id = id;
        this.name = name;
        this.ward = ward;
        this.room = room;
        this.bed = bed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getBed() {
        return bed;
    }

    public String getRoom() {
        return room;
    }

    public String getWard() {
        return ward;
    }

    public void setBed(String bed) {
        this.bed = bed;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }
}
