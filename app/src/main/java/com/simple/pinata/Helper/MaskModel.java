package com.simple.pinata.Helper;

public class MaskModel {

    private String Name;
    private int Id;
    private int Type;

    public MaskModel(String name, int id, int type) {
        Name = name;
        Id = id;
        Type = type;
    }

    public String getName() {
        return Name;
    }

    public int getId() {
        return Id;
    }

    public int getType() {
        return Type;
    }
}
