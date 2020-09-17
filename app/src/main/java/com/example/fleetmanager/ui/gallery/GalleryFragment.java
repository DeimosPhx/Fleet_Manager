package com.example.fleetmanager.ui.gallery;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.fleetmanager.MainActivity;
import com.example.fleetmanager.R;
import com.example.fleetmanager.loadout.LoadoutComponents;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class GalleryFragment extends Fragment implements View.OnClickListener{

    private GalleryViewModel galleryViewModel;
    private ConstraintLayout acutalConstructionLayout;
    private SQLiteDatabase fleetDB;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        //fleetDB = SQLiteDatabase.openOrCreateDatabase("Fleet", null);
        LoadShipData();
    }

    public void LoadShipData(){
        //Load & Parse JSON file
        String JSONDataRaw = loadJSONFromAsset();
        if(JSONDataRaw != null){
            //On parsing split on ship
            if(!parseJSONDataAndCreateCards(JSONDataRaw)){
                Log.d("ERROR -->","Error while parsing and/or creating a ship card");
            }

    //EXAMPLE of text alteration
        /*
            View ship1 = getView().findViewById(R.id.ship1);
            TextView ship1Name = (TextView) ship1.findViewById(R.id.ShipName);
            ship1Name.setText("ISSOU LE DEBILE");
         */
        }

    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("JSON/ships.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private boolean parseJSONDataAndCreateCards(String JSONData){
        try {
            JSONObject obj = new JSONObject(JSONData);
            JSONArray m_jArry = obj.getJSONArray("data");
            ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> m_li;

            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject shipJSON = m_jArry.getJSONObject(i);
                setupShipCard(shipJSON);
            }

            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setupShipCard(JSONObject shipJSON){
        try {
            //Create ship card layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.ship_catalog_card , null ,false);
            acutalConstructionLayout = layout;
            LinearLayout card_container = (LinearLayout) getView().findViewById(R.id.card_container);
            
            //Set card fields
            ((TextView) layout.findViewById(R.id.ShipName)).setText(shipJSON.getString("name"));
            ((TextView) layout.findViewById(R.id.ShipMake)).setText(shipJSON.getJSONObject("manufacturer").getString("name"));

            //Bind action add to DB on click btn_addToFleet
            /*
                    Log.d("Clicked","Clicked");
                    Log.d("Clicked","name & make "+((TextView)v.findViewById(R.id.ShipName)).getText()+" & "+((TextView)v.findViewById(R.id.ShipMake)).getText());
             */
            FloatingActionButton addToFleetBTN = layout.findViewById(R.id.btn_addToFleet);
            addToFleetBTN.setOnClickListener(this);

            //get image from url
            String url = shipJSON.getJSONArray("media").getJSONObject(0).getString("source_url");
            String[] urlSplit = url.split("/");
            String imgResName = urlSplit[urlSplit.length - 2]+".jpg";
            ((TextView) layout.findViewById(R.id.ThumbnailURL)).setText("ships_img/"+imgResName);

            InputStream is = getActivity().getAssets().open("ships_img/"+imgResName);
            Bitmap b = BitmapFactory.decodeStream(is);
            Bitmap resized = Bitmap.createScaledBitmap(b,3840,2160,false);
            ((ImageView) layout.findViewById(R.id.thumb)).setImageDrawable(new BitmapDrawable(getContext().getResources(),resized));

            card_container.addView(layout);

        }catch (JSONException | IOException e){
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View view){
        View parent = (View) view.getParent();
        //add ship to DB for hangar cards
       ((MainActivity)getActivity()).getFleetDB().execSQL("INSERT INTO ships (Name,Make,ImgURL) VALUES" +
               " ('"+((TextView)parent.findViewById(R.id.ShipName)).getText()+"','"+((TextView)parent.findViewById(R.id.ShipMake)).getText()+"','"+((TextView) parent.findViewById(R.id.ThumbnailURL)).getText()+"');");

       //add default loadout for added ship
        //start by getting last entry on ship db to get the ship's ID
        Cursor c = ((MainActivity)getActivity()).getFleetDB().rawQuery("SELECT MAX(id) FROM ships ", null);
        c.moveToFirst();
        String id = c.getString(0);
        //parse JSON
        HashMap<String,String> defaultLoadout = getDefaultLoadoutFromJSON(((TextView)parent.findViewById(R.id.ShipName)).getText().toString());
        if(defaultLoadout.get("ERROR") == null) {
            //insert loadout to DB linked by ship's ID
            String[] comps = Arrays.toString(LoadoutComponents.values()).replaceAll("^.|.$", "").split(", ");
            HashMap<String,String> compiled = new HashMap<>();
            //handle multiple componenet for class [EXAMPLE : having 2 weapons]
            for(String comp : comps){
                String complingComp = "";
                int cpt = 0;
                while(defaultLoadout.containsKey(comp+"_"+cpt)){
                    if(cpt!=0)
                        complingComp += ";";
                    complingComp += defaultLoadout.get(comp+"_"+cpt);
                    cpt++;
                }
                compiled.put(comp,complingComp);
            }
            ((MainActivity) getActivity()).getFleetDB().execSQL("INSERT INTO loadout (id,weapons,turrets,missiles,shields,quantumDrive,powerPlant,coolers,thrusters,emps,utilities)" +
                    "VALUES ('" + id + "','" + compiled.get(LoadoutComponents.weapons.toString()) + "','" + compiled.get(LoadoutComponents.turrets.toString()) + "','" + compiled.get(LoadoutComponents.missiles.toString()) + "','" + compiled.get(LoadoutComponents.shield_generators.toString()) +
                    "','" + compiled.get(LoadoutComponents.quantum_drives.toString()) + "','" + compiled.get(LoadoutComponents.power_plants.toString()) + "','" + compiled.get(LoadoutComponents.coolers.toString()) + "','','" +
                    compiled.get(LoadoutComponents.emps.toString())+ "','" + compiled.get(LoadoutComponents.utility_items.toString()) + "');");
        }
    }


    private HashMap<String,String> getDefaultLoadoutFromJSON(String shipName){
        try {
            HashMap<String,String> ReturnMap = new HashMap<>();
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            String[] comps = Arrays.toString(LoadoutComponents.values()).replaceAll("^.|.$", "").split(", ");
            JSONArray m_jArry = obj.getJSONArray("data");
            ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> m_li;
            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject shipJSON = m_jArry.getJSONObject(i);
                if(shipJSON.getString("name").equals(shipName)){
                    //found ship get default loadout
                    JSONObject loadout = shipJSON.getJSONObject("compiled");
                    JSONObject RSIAvionic = loadout.getJSONObject("RSIAvionic");
                    JSONObject RSIModular = loadout.getJSONObject("RSIModular");
                    JSONObject RSIPropulsion = loadout.getJSONObject("RSIPropulsion");
                    JSONObject RSIThrusters = loadout.getJSONObject("RSIThruster");
                    JSONObject RSIWeapon = loadout.getJSONObject("RSIWeapon");
                    for(String comp : comps){
                        if(RSIAvionic.has(comp)){
                            JSONArray array = RSIAvionic.getJSONArray(comp);
                            for(int j=0;j<array.length();j++)
                                ReturnMap.put(comp+"_"+j,array.getJSONObject(j).getString("name"));
                        }
                        else if(RSIModular.has(comp)){
                            JSONArray array = RSIModular.getJSONArray(comp);
                            for(int j=0;j<array.length();j++)
                                ReturnMap.put(comp+"_"+j,array.getJSONObject(j).getString("name"));
                        }
                        else if(RSIPropulsion.has(comp)){
                            JSONArray array = RSIPropulsion.getJSONArray(comp);
                            for(int j=0;j<array.length();j++)
                                ReturnMap.put(comp+"_"+j,array.getJSONObject(j).getString("name"));
                        }
                        else if(RSIThrusters.has(comp)){
                            JSONArray array = RSIThrusters.getJSONArray(comp);
                            for(int j=0;j<array.length();j++)
                                ReturnMap.put(comp+"_"+j,array.getJSONObject(j).getString("name"));
                        }
                        else if(RSIWeapon.has(comp)){
                            JSONArray array = RSIWeapon.getJSONArray(comp);
                            for(int j=0;j<array.length();j++)
                                ReturnMap.put(comp+"_"+j,array.getJSONObject(j).getString("name"));
                        }
                        else
                            Log.d("COMPONENENT ERROR -->","COULD'NT FIND "+comp);
                    }
                }
            }
            return ReturnMap;
        }catch (JSONException ex){
            HashMap<String,String> errorMap = new HashMap<>();
            errorMap.put("ERROR",ex.getMessage());
            return errorMap;
        }
    }
}