package com.bypriyan.togocart.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bypriyan.togocart.R;
import com.bypriyan.togocart.activity.MainActivity;
import com.bypriyan.togocart.activity.SearchByTypeActivity;
import com.bypriyan.togocart.activity.SearchProductActivity;
import com.bypriyan.togocart.filter.FilterProducts;
import com.bypriyan.togocart.models.ModelProducts;
import com.bypriyan.togocart.ui.home.HomeFragment;
import com.bypriyan.togocart.utilities.Constant;
import com.bypriyan.togocart.utilities.preferenceManager;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductsGrid extends RecyclerView.Adapter<AdapterProductsGrid.HolderProducts> implements Filterable {

    public Context context;
    public ArrayList<ModelProducts> productsArrayList;
    public ArrayList<ModelProducts> productsList;
    private FilterProducts filter;
    private preferenceManager preferenceManager;
    private String activity;

    public AdapterProductsGrid(Context context, ArrayList<ModelProducts> productsArrayList, String activity) {
        this.context = context;
        this.productsArrayList = productsArrayList;
        this.productsList = productsArrayList;
        this.preferenceManager = new preferenceManager(context);
        this.activity = activity;
    }

    @NonNull
    @NotNull
    @Override
    public HolderProducts onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_products, parent, false);
        return new HolderProducts(view);
    }


    @Override
    public void onBindViewHolder(@NonNull @NotNull AdapterProductsGrid.HolderProducts holder, int position) {
        ModelProducts modelProducts = productsArrayList.get(position);

        String productId = modelProducts.getProductId();
        String productImg = modelProducts.getProductImg();
        String discountPercentage= modelProducts.getProductDiscount();
        String productName = modelProducts.getProductName();
        String productMRP= modelProducts.getProductMRP();
        String productSellingPrise = modelProducts.getProductSellingPrise();
        String quentity = modelProducts.getProductQuantity();
        String measuringUnit = modelProducts.getUnitOfMeasurement();

        String productType = modelProducts.getProductType();
        String productCategory = modelProducts.getProductCategory();
        String productBrand = modelProducts.getProductBrand();
        String productDescription = modelProducts.getProductDescription();
        String inStock = modelProducts.getInStock();

        holder.discountPercent.setText(discountPercentage+"% OFF");
        holder.productName.setText(productName);
        holder.mrpPrise.setText("₹"+productMRP);
        holder.sellingPrise.setText("₹"+productSellingPrise);
        holder.mrpPrise.setPaintFlags(holder.mrpPrise.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.quentity.setText(quentity+" "+measuringUnit);

        if(productSellingPrise.equals(productMRP)){
            holder.mrpPrise.setText("");
            holder.discountPercent.setVisibility(View.GONE);
        }else{
            holder.discountPercent.setVisibility(View.VISIBLE);
        }

        if(inStock.equals("true")){
            holder.addBtn.setVisibility(View.VISIBLE);
            holder.productImg.setAlpha((float)1);
            holder.outOfStock.setVisibility(View.GONE);
        }else{
            holder.addBtn.setVisibility(View.GONE);
            holder.productImg.setAlpha((float)0.5);
            holder.outOfStock.setVisibility(View.VISIBLE);
        }

        try {
            Glide.with(context).load(productImg)
                    .centerInside().placeholder(R.drawable.togo_notification).into(holder.productImg);
        }catch (Exception e){
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inStock.equals("true")){
                    shwoBottomSheet(productId, productImg, discountPercentage, productName, productMRP, productSellingPrise,
                            quentity, measuringUnit, productType, productCategory, productBrand, productDescription, inStock, modelProducts);
                }else{
                    Toast.makeText(context, productName+" is out of stock", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddToCartDialog(modelProducts);
            }
        });

    }

    private double cost=0, finalcost=0;
    private int quantity=0;
    private void showAddToCartDialog(ModelProducts modelProducts) {
        String productId = modelProducts.getProductId();
        String productImg = modelProducts.getProductImg();
        String discountPercentage= modelProducts.getProductDiscount();
        String productName = modelProducts.getProductName();
        String productMRP= modelProducts.getProductMRP();
        String productSellingPrise = modelProducts.getProductSellingPrise();
        String quentity = modelProducts.getProductQuantity();
        String measuringUnit = modelProducts.getUnitOfMeasurement();
        String productType = modelProducts.getProductType();
        String productCategory = modelProducts.getProductCategory();
        String productBrand = modelProducts.getProductBrand();
        String productDescription = modelProducts.getProductDescription();
        String inStock = modelProducts.getInStock();

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_product, null);

        ImageView productImgIv = view.findViewById(R.id.productImg);
        ImageView imageMinus = view.findViewById(R.id.imageMinus);
        ImageView imageAddOne = view.findViewById(R.id.imageAddOne);
        MaterialButton addToCartBtn = view.findViewById(R.id.addToCartBtn);

        TextView discountPercentTv = view.findViewById(R.id.discountPercent);
        TextView productNameTv = view.findViewById(R.id.productName);
        TextView quentityTv = view.findViewById(R.id.quentity);
        TextView mrpPriseTv = view.findViewById(R.id.mrpPrise);
        TextView sellingPriseTv = view.findViewById(R.id.sellingPrise);
        TextView finalPrise = view.findViewById(R.id.finalPrise);
        TextView itemCount = view.findViewById(R.id.itemCount);

        cost = Double.parseDouble(productSellingPrise);
        finalcost = Double.parseDouble(productSellingPrise);

        quantity=1;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        try {
            Glide.with(context).load(productImg).centerInside().placeholder(R.drawable.togo_cart_icon).into(productImgIv);
        }catch (Exception e){ }

        productNameTv.setText(productName);
        quentityTv.setText(quentity+measuringUnit);
        mrpPriseTv.setText("₹"+productMRP);
        mrpPriseTv.setPaintFlags(mrpPriseTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        sellingPriseTv.setText("₹"+productSellingPrise);
        discountPercentTv.setText(discountPercentage+"% OFF");
        finalPrise.setText("₹"+productSellingPrise);

        if(productSellingPrise.equals(productMRP)){
            mrpPriseTv.setText("");
            mrpPriseTv.setVisibility(View.GONE);
            discountPercentTv.setVisibility(View.GONE);
        }

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        imageAddOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalcost = finalcost+cost;
                quantity++;

                finalPrise.setText("₹"+finalcost);
                itemCount.setText(""+quantity);

            }
        });

        imageMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(itemCount.getText().toString()) ;
                if(count<=1){
                    dialog.dismiss();
                }else{
                    finalcost = finalcost-cost;
                    quantity--;
                    finalPrise.setText("₹"+finalcost);
                    itemCount.setText(""+quantity);
                }

            }
        });

        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pName = productNameTv.getText().toString().trim();
                String totalPrise = finalPrise.getText().toString().trim().replaceAll("₹","");
                String sellingPriseEach = productSellingPrise;
                String totalProductQuentity = itemCount.getText().toString().trim();
                String productMrpPrise = productMRP;
                String productImage = productImg;
                String productQuentityWithMeasuring = quentity+measuringUnit;

                addToCart(productId, pName, totalPrise, sellingPriseEach, totalProductQuentity, productImage, productQuentityWithMeasuring);
                dialog.dismiss();
            }
        });


    }

    private int itemId= 1;
    private void addToCart(String productId, String pName, String totalPrise, String sellingPriseEach, String totalProductQuentity, String productImage, String productQuentityWithMeasuring) {
        String savedItemId = preferenceManager.getString(Constant.KEY_ITEM_ID);

        if (savedItemId == null || savedItemId.isEmpty()) {
            itemId = 1; // first item
        } else {
            itemId = Integer.parseInt(savedItemId);
        }

        itemId++;

        EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
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

        Boolean b = easyDB.addData("ITEM_ID", itemId)
                .addData("ITEM_PID", productId)
                .addData("ITEM_NAME", pName)
                .addData("ITEM_PRISE_EACH", sellingPriseEach)
                .addData("ITEM_PRISE", totalPrise)
                .addData("ITEM_QUENTITY", totalProductQuentity)
                .addData("ITEM_P_IMG", productImage)
                .addData("ITEM_P_QUENTITY", productQuentityWithMeasuring)
                .doneDataAdding();

        Toast.makeText(context, ""+pName+" "+"Added to cart", Toast.LENGTH_SHORT).show();
        itemId++;
        preferenceManager.putString(Constant.KEY_ITEM_ID,""+itemId);


        if(activity.equals("SearchProductActivity")){
            ((SearchProductActivity)context).allTotalPrise = 0.0;
            ((SearchProductActivity)context).showData();
        }else if(activity.equals("SearchByTypeActivity")){
            ((SearchByTypeActivity)context).allTotalPrise = 0.0;
            ((SearchByTypeActivity)context).showData();
        }

    }

    private void shwoBottomSheet(String productId, String productImg, String discountPercentage, String productName, String productMRP, String productSellingPrise,
                                 String quentity, String measuringUnit, String productType, String productCategory, String productBrand, String productDescription, String inStock, ModelProducts modelProducts) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_product_details);

        ImageView productImgIv = dialog.findViewById(R.id.productImg);
        ImageView backBtnIv = dialog.findViewById(R.id.backBtn);

        TextView productNameTv = dialog.findViewById(R.id.productName);
        TextView productQuentityTv  = dialog.findViewById(R.id.productQuentity);
        TextView addBtn  = dialog.findViewById(R.id.addBtn);
        TextView productSellingPriseTv  = dialog.findViewById(R.id.productSellingPrise);
        TextView productMRPTv  = dialog.findViewById(R.id.productMRP);
        TextView discountPercentTv  = dialog.findViewById(R.id.discountPercent);

        TextView productDescriptionTv  = dialog.findViewById(R.id.productDescription);
        TextView productQuentityUnitTv  = dialog.findViewById(R.id.productQuentityUnit);
        TextView productCategoryTv  = dialog.findViewById(R.id.productCategory);

        backBtnIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        try {
            Glide.with(context).load(productImg)
                    .centerInside().placeholder(R.drawable.togo_cart_icon).into(productImgIv);
        }catch (Exception e){
        }

        productNameTv.setText(productName);
        productQuentityTv.setText(quentity+measuringUnit);
        productMRPTv.setText("₹"+productMRP);
        productMRPTv.setPaintFlags(productMRPTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        productSellingPriseTv.setText("₹"+productSellingPrise);
        discountPercentTv.setText(discountPercentage+"% OFF");
        if(productSellingPrise.equals(productMRP)){
            productMRPTv.setText("");
            discountPercentTv.setVisibility(View.GONE);
        }
        productDescriptionTv.setText(productDescription);
        productQuentityUnitTv.setText(quentity+measuringUnit);
        productCategoryTv.setText(productCategory);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddToCartDialog(modelProducts);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    @Override
    public int getItemCount() {
        return productsArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterProducts(this, productsList);
        }
        return filter;
    }

    public class HolderProducts extends RecyclerView.ViewHolder {

        TextView discountPercent, productName, sellingPrise, mrpPrise, quentity, addBtn, outOfStock;
        ImageView productImg;

        public HolderProducts(@NonNull @NotNull View itemView) {
            super(itemView);

            productImg = itemView.findViewById(R.id.productImg);
            discountPercent = itemView.findViewById(R.id.discountPercent);
            productName = itemView.findViewById(R.id.productName);
            sellingPrise = itemView.findViewById(R.id.sellingPrise);
            mrpPrise = itemView.findViewById(R.id.mrpPrise);
            quentity = itemView.findViewById(R.id.quentity);
            addBtn = itemView.findViewById(R.id.addBtn);
            outOfStock = itemView.findViewById(R.id.outOfStock);
        }
    }

}
