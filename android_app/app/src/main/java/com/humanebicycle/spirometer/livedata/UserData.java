package com.humanebicycle.spirometer.livedata;

import androidx.lifecycle.MutableLiveData;

import com.humanebicycle.spirometer.model.User;

public class UserData {
    private static MutableLiveData<User> userLiveData;

    public static MutableLiveData<User> getUserLiveData(){
        if (userLiveData == null){
            userLiveData = new MutableLiveData<User>();
            User.refresh();
        }
        return userLiveData;
    }

    public static void refreshUser(){
        User.refresh();
    }

    public static void setUser(User user){
        getUserLiveData().setValue(user);
    }

    public static User getUser(){
        return getUserLiveData().getValue();
    }

}
