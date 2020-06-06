package com.dpridoy.topnews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
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
import com.dpridoy.topnews.adapter.ViewPagerAdapter;
import com.dpridoy.topnews.api.Api;
import com.dpridoy.topnews.api.ApiCLient;
import com.dpridoy.topnews.models.Article;
import com.dpridoy.topnews.models.CategoryModel;
import com.dpridoy.topnews.models.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

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

    ViewPager viewPager;
    ViewPagerAdapter pagerAdapter;
    List<CategoryModel> models;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager=findViewById(R.id.viewPager);
        setViewPagerHeader();

        swipeRefreshLayout=findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        topHeadLine=findViewById(R.id.topHeadLine);
        recyclerView=findViewById(R.id.recyclerView);
        layoutManager=new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        //loadData("");
        onLoadingSwipeRefresh("");

        errorLayout=findViewById(R.id.errorLayout);
        errorImage=findViewById(R.id.errorImage);
        errorTitle=findViewById(R.id.errorTitle);
        errorMessage=findViewById(R.id.errorMessage);
        btnRetry=findViewById(R.id.btnRetry);
    }

    private void setViewPagerHeader() {

        models=new ArrayList<>();
        models=Utils.getCategory();

        pagerAdapter=new ViewPagerAdapter(models,MainActivity.this);
        viewPager=findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPadding(0,0,0,0);
        pagerAdapter.notifyDataSetChanged();

        pagerAdapter.setOnItemClickListener(new ViewPagerAdapter.ClickListener() {
            @Override
            public void onClick(View v, int position) {
                TextView categoryName=v.findViewById(R.id.category);
                Intent intent=new Intent(MainActivity.this,CategoryActivity.class);
                intent.putExtra("CATEGORY_NAME",categoryName.getText().toString());
                startActivity(intent);
            }
        });

    }

    public void loadData(final String keyword){
        errorLayout.setVisibility(View.GONE);
        topHeadLine.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(true);
        Api api= ApiCLient.getApiClient().create(Api.class);

        String country=Utils.getCountry();
        String language=Utils.getLanguage();
        Call<News> call;

        if (keyword.length()>0){
            call=api.getNewsSearch(keyword,language,"publishedAt",ApiKey);
        }else {
            call=api.getNews(country,ApiKey);
        }

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticles()!=null){
                    if (!articles.isEmpty()){
                        articles.clear();
                    }
                    articles=response.body().getArticles();
                    Log.e("Size",String.valueOf(articles.size()));
                    adapter= new Adapter(articles,MainActivity.this);
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
                Intent intent=new Intent(MainActivity.this,NewsDetailActivity.class);
                Article article=articles.get(position);
                intent.putExtra("url",article.getUrl());
                intent.putExtra("title",article.getTitle());
                intent.putExtra("img",article.getUrlToImage());
                intent.putExtra("date",article.getPublishedAt());
                intent.putExtra("source",article.getSource().getName());
                intent.putExtra("author",article.getAuthor());

                Pair<View,String> pair=Pair.create((View)imageView, ViewCompat.getTransitionName(imageView));
                ActivityOptionsCompat optionsCompat=ActivityOptionsCompat.makeSceneTransitionAnimation(
                        MainActivity.this,
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        SearchManager searchManager=(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView=(SearchView)menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem=menu.findItem(R.id.action_search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search latest News..");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length()>2){
                    //loadData(query);
                    onLoadingSwipeRefresh(query);
                }else {
                    Toast.makeText(MainActivity.this,"Type more than two letters",Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //loadData(newText);
                return false;
            }
        });
        searchMenuItem.getIcon().setVisible(false,false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRefresh() {
        loadData("");
    }

    private void onLoadingSwipeRefresh(final String keyword){
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        loadData(keyword);
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
                onLoadingSwipeRefresh("");
            }
        });
    }
}
