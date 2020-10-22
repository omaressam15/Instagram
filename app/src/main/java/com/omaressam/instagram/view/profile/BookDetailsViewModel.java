package com.omaressam.instagram.view.profile;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.omaressam.instagram.models.User;

   public class BookDetailsViewModel extends ViewModel {

   public MutableLiveData <User>  user = new MutableLiveData<>() ;

}
