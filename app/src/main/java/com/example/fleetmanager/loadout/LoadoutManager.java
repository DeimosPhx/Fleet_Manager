package com.example.fleetmanager.loadout;

import android.util.Log;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.fleetmanager.R;

public class LoadoutManager implements View.OnClickListener{
    private String shipId;
    private ConstraintLayout layout;
    public LoadoutManager(String id,ConstraintLayout shipCard){
        this.shipId = id;
        this.layout = shipCard;
        this.layout.findViewById(R.id.modLoadout).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }
}
