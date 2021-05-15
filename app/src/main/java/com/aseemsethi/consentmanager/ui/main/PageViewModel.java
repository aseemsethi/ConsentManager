package com.aseemsethi.consentmanager.ui.main;

import android.util.Log;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class PageViewModel extends ViewModel {
    final String TAG = "CM View:";
    final String Agree = "We consent to allowing Aravind Eye Care and its subsidaries and partners " +
    "to access and store our PII data comprising of Name, Phone number, Address, Health Records and " +
    "any other documents pertaining to our medical evaluation. \n\n\n" +
    "We also udnerstand that, we can have all our data deleted along with this consent on a " +
    "written request via email to xyz@aravindeyecare.com, post which all our PII and Health Record " +
    "data will be deleted permanently in 2 weeks time";

    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private LiveData<String> mText = Transformations.map(mIndex, new Function<Integer, String>() {
        @Override
        public String apply(Integer input) {
            if (input == 1) {
                return Agree;
            } else {
                return "Please provide your consent via Aadhar Authentication";
            }
        }
    });

    public void setIndex(int index) {
        mIndex.setValue(index);
        Log.i(TAG, String.valueOf(index));
    }

    public LiveData<String> getText() {
        return mText;
    }
}