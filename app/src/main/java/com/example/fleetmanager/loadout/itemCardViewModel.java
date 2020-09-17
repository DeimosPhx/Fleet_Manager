package com.example.fleetmanager.loadout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class itemCardViewModel extends ViewModel {


    private MutableLiveData<String> mText;

    public itemCardViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }
}
