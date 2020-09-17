package com.example.fleetmanager.loadout;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.fleetmanager.MainActivity;
import com.example.fleetmanager.R;
import com.example.fleetmanager.ui.home.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;
import java.util.ArrayList;

public class LoadoutManager implements View.OnClickListener{
    private String shipId;
    private ConstraintLayout layout;
    private Activity activity;
    private Context context;

    public LoadoutManager(String id, ConstraintLayout shipCard ,Activity activity, Context context){
        this.shipId = id;
        this.layout = shipCard;
        this.layout.findViewById(R.id.modLoadout).setOnClickListener(this);
        this.activity = activity;
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        View hangar = (View)v.getParent().getParent().getParent().getParent().getParent();
        View container = hangar.findViewById(R.id.item_cards_container);
        //View container = ((View)v.getParent().getParent().getParent()).findViewById(R.id.item_cards_container);
        //Setup all fields
        Cursor c = ((MainActivity) activity).getFleetDB().rawQuery("SELECT DISTINCT weapons,turrets,missiles,shields,quantumDrive,powerPlant,coolers,thrusters,emps,utilities  FROM loadout WHERE id='"+this.shipId+"';", null);
        if (c.moveToFirst()){
            //TODO optimize ?
            String[] weapons = c.getString(0).split(";");
            for(String weapon : weapons)
                createLoadoutItem(LoadoutComponents.weapons.toString(),weapon,container);

            String[] turrets = c.getString(1).split(";");
            for(String turret : turrets)
                createLoadoutItem(LoadoutComponents.turrets.toString(),turret,container);

            String[] missiles = c.getString(2).split(";");
            for(String missile : missiles)
                createLoadoutItem(LoadoutComponents.missiles.toString(),missile,container);

            String[] shields = c.getString(3).split(";");
            for(String shield : shields)
                createLoadoutItem(LoadoutComponents.shield_generators.toString(),shield,container);

            String[] Qts = c.getString(4).split(";");
            for(String Qt : Qts)
                createLoadoutItem(LoadoutComponents.quantum_drives.toString(),Qt,container);

            String[] powerPlants = c.getString(5).split(";");
            for(String powerPlant : powerPlants)
                createLoadoutItem(LoadoutComponents.power_plants.toString(),powerPlant,container);

            String[] coolers = c.getString(6).split(";");
            for(String cooler : coolers)
                createLoadoutItem(LoadoutComponents.coolers.toString(),cooler,container);

            //TODO miss thrusters / emps / utilities (which are not implemented or rare)
        }
        c.close();

        //Finally render loadout manager
        hangar.findViewById(R.id.include).setVisibility(View.VISIBLE);
    }

    private void createLoadoutItem(String type, String name,View container){
        //TODO not working ?
        Log.d("Creating card", ""+type+" | "+name);
        LayoutInflater inflater = LayoutInflater.from(this.context);
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.loadout_item_card , null ,false);
        ((TextView)layout.findViewById(R.id.item_name)).setText(name);
        ((TextView)layout.findViewById(R.id.item_label)).setText(type);
        ((LinearLayout) container).addView(layout);

    }
}
