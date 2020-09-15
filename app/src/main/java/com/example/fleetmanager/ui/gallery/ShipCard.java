package com.example.fleetmanager.ui.gallery;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.fleetmanager.R;
import com.example.fleetmanager.ui.home.HomeViewModel;

public class ShipCard extends Fragment {
    private ShipCardViewModel shipCardViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        shipCardViewModel =
                ViewModelProviders.of(this).get(ShipCardViewModel.class);
        View root = inflater.inflate(R.layout.ship_catalog_card, container, false);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

    }


}
