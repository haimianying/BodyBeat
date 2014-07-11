package com.example.bluetoothmessenger;

public class SensorData {
	
	//private variables
    int _id;
    String _Date_Time;
    String _Latitude;
    String _Longitude;
    String _Altitude;
    String _Provider;
    String _Speed;
    String _Bearing;
    
    String _BodySoundClass;
    
    String _Activity;
    
 
    // Empty constructor
    public SensorData(){
    	this._Date_Time="";
    	this._Latitude="";
    	this._Longitude="";
    	this._Altitude="";
    	this._Provider="";
    	this._Speed="";
    	this._Bearing="";
    	this._BodySoundClass="";
    	this._Activity="";
    	
    }
    
    public SensorData(int id, String dt, String lat, String lon, String alt, String pro, String spe, String bea,
    		String bsc, String act){
    	this._id = id;
    	this._Date_Time=dt;
    	this._Latitude=lat;
    	this._Longitude=lon;
    	this._Altitude=alt;
    	this._Provider=pro;
    	this._Speed=spe;
    	this._Bearing=bea;
    	this._BodySoundClass=bsc;
    	this._Activity=act;
    }
    
    // getting ID
    public int getID(){
        return this._id;
    }
 
    // setting id
    public void setID(int id){
        this._id = id;
    }
    
    // setting location
    public void setDateAndTime(String dt){
    	this._Date_Time=dt;
    }
    
    //setting Body Sound Class
    public void setBodySoundClass(String bsc){
    	this._BodySoundClass=bsc;
    }
    
    //setting Activity
    public void setActivity(String act){
    	this._Activity=act;
    }
    
    // setting location
    public void setGPSData(double lat, double lon, double alt, String pro, float spe, float bea){
    	this._Latitude=Double.toString(lat);
    	this._Longitude=Double.toString(lon);
    	this._Altitude=Double.toString(alt);
    	this._Provider=pro;
    	this._Speed=Float.toString(spe);
    	this._Bearing=Float.toString(bea);
    }
    
    public void setGPSData(String lat, String lon, String alt, String pro, String spe, String bea){
    	this._Latitude=lat;
    	this._Longitude=lon;
    	this._Altitude=alt;
    	this._Provider=pro;
    	this._Speed=spe;
    	this._Bearing=bea;
    }
    
    // getting user Date
    public String getDateAndTime(){
        return this._Date_Time;
    }
    
    // getting user Lat
    public String getLatitude(){
        return this._Latitude;
    }
    
    // getting long
    public String getLongitude(){
        return this._Longitude;
    }
    
    // getting alt
    public String getAltitude(){
        return this._Altitude;
    }
    
    // getting prov
    public String getProvider(){
        return this._Provider;
    }
    
    // getting speed
    public String getSpeed(){
        return this._Speed;
    }
    
    // getting bearing
    public String getBearing(){
        return this._Bearing;
    }
    
    // getting Body Sound Class
    public String getBodySoundClass(){
        return this._BodySoundClass;
    }
    
    // getting Activity
    public String getActivity(){
    	return this._Activity;
    }
}
