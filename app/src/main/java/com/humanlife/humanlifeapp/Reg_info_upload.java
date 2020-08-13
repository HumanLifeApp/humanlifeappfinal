package com.humanlife.humanlifeapp;

public class Reg_info_upload {
    public String photoURL,name,email,mobile,dob,city,status;

    //Empty constructor
    public Reg_info_upload(){

    }

    public Reg_info_upload(String city, String dob, String email, String mobile, String name, String photoURL) {
        this.photoURL = photoURL;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.dob = dob;
        this.city = city;

    }

    public String getPhotoURL() {
        return photoURL;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getDob() {
        return dob;
    }

    public String getCity() {
        return city;
    }



    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

