package com.example.fleetmanager.loadout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.fleetmanager.R;
import com.example.fleetmanager.ui.gallery.ShipCardViewModel;

public class itemCard extends Fragment {
    private itemCardViewModel shipCardViewModel;
    private String name;
    private String type;

    public itemCard(String type,String name){
        this.name = name;
        this.type = type;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        shipCardViewModel =
                ViewModelProviders.of(this).get(itemCardViewModel.class);
        View root = inflater.inflate(R.layout.loadout_item_card, container, false);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((TextView)getView().findViewById(R.id.item_label)).setText(this.type);
        ((TextView)getView().findViewById(R.id.item_name)).setText(this.name);
        //TODO load and assign icon from assets that need to be imported OR ask the image as parameter
    }
}
