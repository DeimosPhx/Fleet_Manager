package com.example.fleetmanager.ui.home;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.fleetmanager.MainActivity;
import com.example.fleetmanager.R;
import com.example.fleetmanager.loadout.LoadoutManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.InputStream;

public class HomeFragment extends Fragment implements View.OnClickListener{

    private HomeViewModel homeViewModel;
    private SQLiteDatabase fleetDB;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        fleetDB = ((MainActivity)getActivity()).getFleetDB();
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getView().findViewById(R.id.close_manager).setOnClickListener(this);
        try {
            addShipCardsFromDB();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void addShipCardsFromDB() throws IOException {
        Cursor c = fleetDB.rawQuery("SELECT id,Name,Make,ImgURL FROM ships ", null);
        if (c.moveToFirst()){
            do {
                // Passing values
                // Do something Here with values
                LayoutInflater inflater = LayoutInflater.from(getContext());
                ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.ship_fleet_card , null ,false);
                LinearLayout card_container = (LinearLayout) getView().findViewById(R.id.card_container);

                //SETUP CARD
                ((TextView)layout.findViewById(R.id.shipId)).setText(c.getString(0));
                ((TextView)layout.findViewById(R.id.ShipName)).setText(c.getString(1));
                ((TextView)layout.findViewById(R.id.ShipMake)).setText(c.getString(2));
                InputStream is = getActivity().getAssets().open(c.getString(3));
                Bitmap b = BitmapFactory.decodeStream(is);
                Bitmap resized = Bitmap.createScaledBitmap(b,3840,2160,false);
                ((ImageView)layout.findViewById(R.id.thumb)).setImageDrawable(new BitmapDrawable(getContext().getResources(),resized));
                ((FloatingActionButton)layout.findViewById(R.id.removeBtn)).setOnClickListener(this);

                //setup loadout manager
                new LoadoutManager(c.getString(0),layout,getActivity(),getContext());
                //ADD CARD TO CONTAINER
                card_container.addView(layout);
            } while(c.moveToNext());
        }
        c.close();
    }

    private void removeAllCard(){
        LinearLayout card_container = (LinearLayout) getView().findViewById(R.id.card_container);
        card_container.removeAllViewsInLayout();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.close_manager){
            getView().findViewById(R.id.include).setVisibility(View.INVISIBLE);
        }else{
            View parent = (View)v.getParent();
            fleetDB.execSQL("DELETE FROM ships WHERE id='"+((TextView)parent.findViewById(R.id.shipId)).getText()+"'");
            try {
                removeAllCard();
                addShipCardsFromDB();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}