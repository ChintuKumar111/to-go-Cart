package com.bypriyan.togocart.activity;

import static com.google.android.gms.auth.GoogleAuthUtil.getToken;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.bypriyan.togocart.R;
import com.bypriyan.togocart.adapter.AdapterCartItem;
import com.bypriyan.togocart.adapter.AdapterProductsGrid;
import com.bypriyan.togocart.databinding.ActivityMainBinding;
import com.bypriyan.togocart.models.ModelCartitem;
import com.bypriyan.togocart.models.ModelProducts;
import com.bypriyan.togocart.utilities.Constant;
import com.bypriyan.togocart.utilities.Token;
import com.bypriyan.togocart.utilities.preferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private long backPressed;
    private Toast backToast;

    private BottomNavigationView bottomNavigationView;
    private NavController navController;
    private preferenceManager preferenceManager;
    private ArrayList<ModelCartitem> cartitemList;
    private ArrayList<ModelProducts> productsArrayList;
    private EasyDB easyDB;
    public double allTotalPrise = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        }
        getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this,R.color.white));// set status background white
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        navController = Navigation.findNavController(this, R.id.frameLayout);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        preferenceManager = new preferenceManager(MainActivity.this);

        easyDB = EasyDB.init(MainActivity.this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("ITEM_ID", new String[]{"text", "unique"}))
                .addColumn(new Column("ITEM_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("ITEM_NAME", new String[]{"text", "not null"}))
                .addColumn(new Column("ITEM_PRISE_EACH", new String[]{"text", "not null"}))
                .addColumn(new Column("ITEM_PRISE", new String[]{"text", "not null"}))
                .addColumn(new Column("ITEM_QUENTITY", new String[]{"text", "not null"}))
                .addColumn(new Column("ITEM_P_IMG", new String[]{"text", "not null"}))
                .addColumn(new Column("ITEM_P_QUENTITY", new String[]{"text", "not null"}))
                .doneTableColumn();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        config.setConfigSettingsAsync(configSettings);

        config.fetchAndActivate().addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                try{
                    String pinCodes = config.getString("pinCodes");
                    String deliveryTime = config.getString("DeliveryTime");
                    preferenceManager.putString(Constant.KEY_PIN_CODES,pinCodes);
                    preferenceManager.putString(Constant.KEY_EXPECTED_DELIVERYTIME, deliveryTime);
                }catch (NullPointerException e){

                }

            }
        });

        showData();
        binding.goToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CartActivity.class));
            }
        });

       // getToken();
        loadMessage();

    }

    private void loadMessage() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                try {
                    String fcmKey = ""+snapshot.child("key").getValue().toString();
                    preferenceManager.putString(Constant.KEY_FCM_SERVER_KEY, fcmKey);
                }catch (NullPointerException e){}

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void UpdateToken(String token) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.e("UpdateToken", "Firebase user is null. Token not updated.");
            return;
        }

        String uid = user.getUid();

        DatabaseReference reference =
                FirebaseDatabase.getInstance().getReference(Constant.KEY_TOKENSUSER);

        Token token1 = new Token(token);
        reference.child(uid).setValue(token1);
    }

    public void showData() {

        cartitemList = new ArrayList<>();
        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("ITEM_ID", new String[]{"text", "unique"}))
                .addColumn(new Column("ITEM_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("ITEM_NAME", new String[]{"text", "not null"}))
                .addColumn(new Column("ITEM_PRISE_EACH", new String[]{"text", "not null"}))
                .addColumn(new Column("ITEM_PRISE", new String[]{"text", "not null"}))
                .addColumn(new Column("ITEM_QUENTITY", new String[]{"text", "not null"}))
                .addColumn(new Column("ITEM_P_IMG", new String[]{"text", "not null"}))
                .addColumn(new Column("ITEM_P_QUENTITY", new String[]{"text", "not null"}))
                .doneTableColumn();

        Cursor res= easyDB.getAllData();
        while (res.moveToNext()){
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String prise = res.getString(4);
            String cost = res.getString(5);
            String quentity = res.getString(6);
            String pImg = res.getString(7);
            String pQuentity = res.getString(8);

            allTotalPrise = allTotalPrise+Double.parseDouble(cost);

            ModelCartitem modelCartitem = new ModelCartitem(""+id,
                    ""+pId,
                    ""+name,
                    ""+prise,
                    ""+cost,
                    ""+quentity,
                    ""+pImg,
                    ""+pQuentity);

            cartitemList.add(modelCartitem);
        }

        if(cartitemList.isEmpty()){
            binding.goToCart.setVisibility(View.GONE);
        }else{
            binding.goToCart.setVisibility(View.VISIBLE);
            binding.totalAmount.setText("₹"+allTotalPrise);
            binding.totalItems.setText(cartitemList.size()+" items");
        }
    }

    @Override
    public void onBackPressed() {
        if(backPressed+2500 > System.currentTimeMillis()){
            super.onBackPressed();
            backToast.cancel();
            return;
        }else{
            backToast =  Toast.makeText(this, "press again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }

        backPressed = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        allTotalPrise=0.0;
        showData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        allTotalPrise=0.0;
        showData();
    }

}