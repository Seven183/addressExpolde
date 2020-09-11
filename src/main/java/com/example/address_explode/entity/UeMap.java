package com.example.address_explode.entity;

import java.util.HashMap;
import java.util.Map;

public class UeMap {

    public String province;
    public String city;
    public String distinct;
    public String latitude;
    public String longitude;
    public String address;

    public UeMap(String province, String city, String distinct, String latitude, String longitude ,String address) {
        this.province = province;
        this.city = city;
        this.distinct = distinct;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }
    public UeMap(String province, String city, String distinct) {
        this.province = province;
        this.city = city;
        this.distinct = distinct;
    }

    public UeMap() {
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistinct() {
        return distinct;
    }

    public void setDistinct(String distinct) {
        this.distinct = distinct;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "UeMap{" +
                "province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", distinct='" + distinct + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
