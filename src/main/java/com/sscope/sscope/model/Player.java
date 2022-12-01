package com.sscope.sscope.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Player {
    @Id
    //@UniqueConstraint
    private String id;
    private String name;
    private int level;

    public Player() {
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

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
