package com.redbear.chat;

import java.util.Date;

/**
 * Created by Neil on 05/03/2015.
 */
public class BLESensorValues {

    private int _id;
    private String _devicename;
    private String _sensorcode;
    private Date _timestamp;
    static int index=0;
    public BLESensorValues() {

    }
    @Override
    public String toString(){
        return "ID:"+this._id+" Devicename:"+this._devicename+" Sensorcode:"+this._sensorcode+" Timestamp:" + this._timestamp;
    }


    public BLESensorValues(int id, String devicename,String sensorcode) {
        this._id = id;
        this._devicename = devicename;
        this._sensorcode=sensorcode;
    }

    public BLESensorValues(String devicename,String sensorcode) {
        this._devicename = devicename;
        this._sensorcode=sensorcode;
        this.set_id(index++);
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_devicename() {
        return _devicename;
    }

    public void set_devicename(String _devicename) {
        this._devicename = _devicename;
    }

    public String get_sensorcode() {
        return _sensorcode;
    }

    public void set_sensorcode(String _sensorcode) {
        this._sensorcode = _sensorcode;
    }

    public Date get_timestamp() {
        return _timestamp;
    }

    public void set_timestamp(Date _timestamp) {
        this._timestamp = _timestamp;
    }
}