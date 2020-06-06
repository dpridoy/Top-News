package com.dpridoy.topnews.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.dpridoy.topnews.R;
import com.dpridoy.topnews.models.CategoryModel;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {

    private List<CategoryModel> models;
    private LayoutInflater layoutInflater;
    private Context context;
    private static ClickListener clickListener;

    public ViewPagerAdapter(List<CategoryModel> models, Context context) {
        this.models = models;
        this.context = context;
    }

    @Override
    public int getCount() {
        return models.size();
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        ViewPagerAdapter.clickListener = clickListener;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        layoutInflater=LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_card,container,false);
        ImageView imageView;
        TextView name;
        imageView=view.findViewById(R.id.img);
        name=view.findViewById(R.id.category);

        Glide.with(context).load(models.get(position).getImageUrl()).into(imageView);
//        Picasso.get().load(models.get(position).getImageUrl()).into(imageView);
        name.setText(models.get(position).getCategoryName());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(v,position);
            }
        });

        container.addView(view,0);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public float getPageWidth(int position) {
        return 0.33f;
    }

    public interface ClickListener {
        void onClick(View v, int position);
    }
}
