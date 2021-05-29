package ru.kpfu.itis.datamining.daniayr.idrisov.models;

public class Doubleton {

    private Integer one;

    private Integer two;

    private int count = 0;

    public Doubleton(Integer one, Integer two) {
        this.one = one;
        this.two = two;
    }

    public Integer getOne() {
        return one;
    }

    public Integer getTwo() {
        return two;
    }

    public void setOne(Integer one) {
        this.one = one;
    }

    public void setTwo(Integer two) {
        this.two = two;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}