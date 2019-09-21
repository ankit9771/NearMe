package com.example.nearme;


import com.google.android.gms.maps.model.LatLng;

public class IndividualLocation {

    private String CompanyId = " ";
    private String CompanyName_En = " ";
    public String Phone=" ";
    private String Region = " ";
    private String Province_En =" ";
    public String lat = " ";
    public String lon = " ";

    public IndividualLocation(String CompanyId, String CompanyName_En, String Phone, String Region, String Province_En,String lat,String lon) {
        this.CompanyId = CompanyId;
        this.CompanyName_En = CompanyName_En;
        this.Phone = Phone;
        this.Region = Region;
        this.Province_En = Province_En;
        this.lat = lat;
        this.lon = lon;

    }

    public String getCompanyId() {
        return CompanyId;
    }

    public String getCompanyName_En() {
        return CompanyName_En;
    }

    public String getPhone() { return Phone;}

    public String getRegion() {
        return Region;
    }

    public String getProvince_En() {
        return Province_En;
    }

    public String getlat() {
        return lat;
    }
    public String getlon() {
        return lon;
    }


}