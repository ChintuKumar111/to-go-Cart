package com.bypriyan.togocart.ui.home;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.bypriyan.togocart.activity.CartActivity;
import com.bypriyan.togocart.activity.NotificationsActivity;
import com.bypriyan.togocart.adapter.AdapterCategory;
import com.bypriyan.togocart.adapter.SliderAdapterViewPager;
import com.bypriyan.togocart.databinding.FragmentHomeBinding;
import com.bypriyan.togocart.models.ModelCategories;
import com.bypriyan.togocart.showCart.ShowCategories;
import com.bypriyan.togocart.showCart.SliderItems;
import com.bypriyan.togocart.utilities.Constant;
import com.bypriyan.togocart.activity.SearchByTypeActivity;
import com.bypriyan.togocart.activity.SearchProductActivity;
import com.bypriyan.togocart.utilities.FcmNotificationsSender;
import com.bypriyan.togocart.utilities.preferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private int totalCounts, i;
    private EasyDB easyDB;
    private preferenceManager preferenceManager;
    private ArrayList<SliderItems> sliderImagesList;
    private Handler sliderHandler = new Handler();
    private ArrayList<ModelCategories> categoriesArrayList;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());

        preferenceManager = new preferenceManager(getActivity());
        categoriesArrayList = new ArrayList<>();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        firebaseAuth = FirebaseAuth.getInstance();

        loadTime();
        loadMessage();
        loadBannerinViewPager();

        easyDB = EasyDB.init(getActivity(), "ITEMS_DB")
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

        cartCount();

        binding.vegetableIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starActiv("Vegetables");
            }
        });

        binding.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SearchProductActivity.class));
            }
        });

        binding.fruitsIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starActiv("Fruits");
            }
        });

        //

        binding.search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SearchProductActivity.class));
            }
        });

        binding.cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CartActivity.class));
            }
        });

        binding.notificationFrame.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), NotificationsActivity.class));
        });

        binding.sendSuggestionBtn.setOnClickListener(v -> {
            if (!binding.couopnCodeEd.getText().toString().isEmpty()) {
                sendSuggestionMessage(binding.couopnCodeEd.getText().toString());
            }

        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadNotificationCount();
            }
        },1500);

        executorService.execute(new Runnable() {
            public void run() {
                ShowCategories showCategories = new ShowCategories(categoriesArrayList);
                int i=showCategories.getCategoriesArraylist().size();
                //
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i > 0) {
                            binding.recyclearCategories.setAdapter(new AdapterCategory(getActivity(), categoriesArrayList));
                        }
                    }
                });
                //
            }
        });

        executorService.shutdown();

        return binding.getRoot();
    }

    private void loadNotificationCount(){
        i=0;
        String uid = firebaseAuth.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.KEY_MESSAGE_REPLY);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot  ds: snapshot.getChildren()){
                    String senderId = ""+ds.child(Constant.KEY_MESSAGE_SENDER_ID).getValue().toString();
                    String isSeen = ""+ds.child(Constant.KEY_IS_SEEN).getValue().toString();
                    if(senderId.equals(uid) && isSeen.equals("false")){
                        i++;
                        break;
                    }
                }
                if(i==0){
                    binding.notificationCount.setVisibility(View.GONE);
                }else{
                    binding.notificationCount.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void loadBannerinViewPager() {
        sliderImagesList = new ArrayList<>();
        sliderImagesList.clear();
        FirebaseDatabase.getInstance().getReference("Banner")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        try {
                            sliderImagesList.add(new SliderItems("" + snapshot.child("1").getValue().toString()));
                            sliderImagesList.add(new SliderItems("" + snapshot.child("2").getValue().toString()));
                            sliderImagesList.add(new SliderItems("" + snapshot.child("3").getValue().toString()));
                            sliderImagesList.add(new SliderItems("" + snapshot.child("4").getValue().toString()));
                            sliderImagesList.add(new SliderItems("" + snapshot.child("5").getValue().toString()));
                        } catch (NullPointerException e) {
                        }

                        binding.viewPagerImageSlider.setAdapter(new SliderAdapterViewPager(sliderImagesList, binding.viewPagerImageSlider, getContext()));
                        binding.viewPagerImageSlider.setClipToPadding(false);
                        binding.viewPagerImageSlider.setClipChildren(false);
                        binding.viewPagerImageSlider.setOffscreenPageLimit(3);
                        binding.viewPagerImageSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

                        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
                        compositePageTransformer.addTransformer(new MarginPageTransformer(50));
                        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
                            @Override
                            public void transformPage(@NonNull @NotNull View page, float position) {
                                float r = 1 - Math.abs(position);
                                page.setScaleY(0.85f + r * 0.15f);

                            }
                        });

                        binding.viewPagerImageSlider.setPageTransformer(compositePageTransformer);
                        binding.viewPagerImageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                            @Override
                            public void onPageSelected(int position) {
                                super.onPageSelected(position);
                                sliderHandler.removeCallbacks(sliderRunnable);
                                sliderHandler.postDelayed(sliderRunnable, 6000);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void sendNotificationToAll(String title, String description, String orderId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.KEY_TOKENS);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String token = "" + ds.child("token").getValue().toString();
                    sendNotification(token, title, description, orderId);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getActivity(), "" + error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void sendNotification(String token, String title, String description, String orderId) {
        String fcmKey = preferenceManager.getString(Constant.KEY_FCM_SERVER_KEY);
        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(token, title, description, getActivity(), fcmKey);
        notificationsSender.SendNotifications();
    }

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            binding.viewPagerImageSlider.setCurrentItem(binding.viewPagerImageSlider.getCurrentItem() + 1);
        }
    };

    private void sendSuggestionMessage(String message) {
        String uid = ""+firebaseAuth.getUid();
        String timeStamp = "" + System.currentTimeMillis();
        String phoneNo = preferenceManager.getString(Constant.KEY_PHONE_NUMBER);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constant.KEY_SUGGESTION_MESSAGE, message);
        hashMap.put(Constant.KEY_SUGGESTION_PH_NO, phoneNo);
        hashMap.put(Constant.KEY_SUGGESTION_MESSAGE_ID, timeStamp);
        hashMap.put(Constant.KEY_UID, uid);
        hashMap.put(Constant.KEY_IS_SUGGESTION_REAS,"false");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.KEY_MESSAGES_SUGGESTIONS);
        reference.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getActivity(), "Request noted successfully.", Toast.LENGTH_SHORT).show();
                sendNotificationToAll("New Message", message, "123456");
                binding.couopnCodeEd.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(getActivity(), "" + e, Toast.LENGTH_SHORT).show();
                binding.couopnCodeEd.setText("");
            }
        });

    }

    public void cartCount() {
        int count = easyDB.getAllData().getCount();
        if (count <= 0) {
            binding.cartItemCount.setVisibility(View.GONE);
            binding.above.setVisibility(View.GONE);
        } else {
            binding.cartItemCount.setVisibility(View.VISIBLE);
            binding.cartItemCount.setText("" + count);
            binding.above.setVisibility(View.VISIBLE);
        }
    }

    public void starActiv(String searchType) {
        Intent intent = new Intent(getContext(), SearchByTypeActivity.class);
        intent.putExtra("searchType", searchType);
        startActivity(intent);
    }

    private void loadMessage() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String message = "" + snapshot.child("message").getValue().toString();
                String fcmKey = "" + snapshot.child("key").getValue().toString();

                preferenceManager.putString(Constant.KEY_FCM_SERVER_KEY, fcmKey);
                binding.messageTv.setText(message);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        cartCount();
    }

    @Override
    public void onResume() {
        super.onResume();
        cartCount();
        sliderHandler.postDelayed(sliderRunnable, 6000);
    }

    @Override
    public void onPause() {
        super.onPause();
        cartCount();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    private void loadTime() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 0 && timeOfDay < 12) {
            binding.time.setText("Hey! Good Morning" + " \uD83D\uDE0A");
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            binding.time.setText("Hey! Good Afternoon" + " \uD83C\uDF1E");
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            binding.time.setText("Hey! Good Evening" + " \uD83C\uDF1B");
        } else if (timeOfDay >= 21 && timeOfDay < 24) {
            binding.time.setText("Hey! Good Night" + " \uD83C\uDF1B");
        }

    }

}