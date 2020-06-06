package com.dpridoy.topnews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.dpridoy.topnews.adapter.Adapter;
import com.dpridoy.topnews.api.Api;
import com.dpridoy.topnews.api.ApiCLient;
import com.dpridoy.topnews.models.Article;
import com.dpridoy.topnews.models.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private static String ApiKey="put api_key from newsapi.org";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles=new ArrayList<>();
    private Adapter adapter;
    private String TAG=MainActivity.class.getSimpleName();
    private TextView topHeadLine;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout errorLayout;
    private ImageView errorImage;
    private TextView errorTitle, errorMessage;
    private Button btnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        swipeRefreshLayout=findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        topHeadLine=findViewById(R.id.topHeadLine);
        recyclerView=findViewById(R.id.recyclerView);
        layoutManager=new LinearLayoutManager(CategoryActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        //loadData("");


        onLoadingSwipeRefresh();

        errorLayout=findViewById(R.id.errorLayout);
        errorImage=findViewById(R.id.errorImage);
        errorTitle=findViewById(R.id.errorTitle);
        errorMessage=findViewById(R.id.errorMessage);
        btnRetry=findViewById(R.id.btnRetry);
    }

    public void loadData(){
        errorLayout.setVisibility(View.GONE);
        topHeadLine.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(true);
        Api api= ApiCLient.getApiClient().create(Api.class);

        Intent intent =getIntent();
        String categoryName = intent.getStringExtra("CATEGORY_NAME");
        String country=Utils.getCountry();
        String language=Utils.getLanguage();
        Call<News> call;
        topHeadLine.setText(categoryName);
        call=api.getNewsCategoryWise(country,categoryName.toLowerCase(),ApiKey);
        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticles()!=null){
                    if (!articles.isEmpty()){
                        articles.clear();
                    }
                    articles=response.body().getArticles();
                    Log.e("Size",String.valueOf(articles.size()));
                    adapter= new Adapter(articles,CategoryActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListener();

                    topHeadLine.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);

                }else {
                    topHeadLine.setVisibility(View.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false);

                    String errorCode;
                    switch (response.code()){
                        case 404:
                            errorCode="404 not found";
                            break;
                        case 500:
                            errorCode="500 Server broken";
                            break;
                        default:
                            errorCode="Unknown error!!";
                            break;
                    }
                    showErrorMessage(
                            R.drawable.no_result,
                            "No Result",
                            "Please Try Again!\n"+errorCode);
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                topHeadLine.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);
                showErrorMessage(
                        R.drawable.no_result,
                        "Opps..",
                        "Network failure, Please Try Again!\n"+t.toString());
            }
        });
    }

    private void initListener(){
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ImageView imageView=view.findViewById(R.id.img);
                Intent intent=new Intent(CategoryActivity.this,NewsDetailActivity.class);
                Article article=articles.get(position);
                intent.putExtra("url",article.getUrl());
                intent.putExtra("title",article.getTitle());
                intent.putExtra("img",article.getUrlToImage());
                intent.putExtra("date",article.getPublishedAt());
                intent.putExtra("source",article.getSource().getName());
                intent.putExtra("author",article.getAuthor());

                Pair<View,String> pair=Pair.create((View)imageView, ViewCompat.getTransitionName(imageView));
                ActivityOptionsCompat optionsCompat=ActivityOptionsCompat.makeSceneTransitionAnimation(
                        CategoryActivity.this,
                        pair
                );
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){
                    startActivity(intent,optionsCompat.toBundle());
                }else {
                    startActivity(intent);
                }

            }
        });
    }


    @Override
    public void onRefresh() {
        loadData();
    }

    private void onLoadingSwipeRefresh(){
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        loadData();
                    }
                }
        );
    }

    private void showErrorMessage(int imageView, String title, String message){
        if (errorLayout.getVisibility()==View.GONE){
            errorLayout.setVisibility(View.VISIBLE);
        }
        errorImage.setImageResource(imageView);
        errorTitle.setText(title);
        errorMessage.setText(message);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoadingSwipeRefresh();
            }
        });
    }
}
