package com.dreytech.clientdreymart;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.dreytech.clientdreymart.Adapter.CategoryAdapter;
import com.dreytech.clientdreymart.Database.DataSource.CartRepository;
import com.dreytech.clientdreymart.Database.DataSource.FavoriteRepository;
import com.dreytech.clientdreymart.Database.Local.CartDataSource;
import com.dreytech.clientdreymart.Database.Local.DREYRoomDatabase;
import com.dreytech.clientdreymart.Database.Local.FavoriteDataSource;
import com.dreytech.clientdreymart.Model.Banner;
import com.dreytech.clientdreymart.Model.Barang;
import com.dreytech.clientdreymart.Model.Category;
import com.dreytech.clientdreymart.Model.CheckUserResponse;
import com.dreytech.clientdreymart.Model.User;
import com.dreytech.clientdreymart.Retrofit.IDreyMarketAPI;
import com.dreytech.clientdreymart.Utils.Common;
import com.dreytech.clientdreymart.Utils.ProgressRequestBody;
import com.dreytech.clientdreymart.Utils.UploadCallback;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.nex3z.notificationbadge.NotificationBadge;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, UploadCallback {

    private static final int PICK_FILE_REQUEST = 1222;
    TextView txt_name, txt_phone;
    ImageView cart_icon;

    SliderLayout sliderLayout;

    IDreyMarketAPI mService;
    RecyclerView lst_menu;

    NotificationBadge badge;

    CircleImageView img_avatar;

    Uri selectedFileUri;

    SwipeRefreshLayout swipeRefreshLayout;

    //rx java
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mService = Common.getAPI();

        lst_menu = (RecyclerView)findViewById(R.id.lst_menu);
        lst_menu.setLayoutManager(new GridLayoutManager(this,2));
        lst_menu.setHasFixedSize(true);

        sliderLayout = (SliderLayout)findViewById(R.id.slider);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_to_refresh);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);

        txt_name = (TextView)headerView.findViewById(R.id.txt_name);
        txt_phone = (TextView)headerView.findViewById(R.id.txt_phone);
        img_avatar = (CircleImageView)headerView.findViewById(R.id.img_avatar);



        //event
        img_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.currentUser != null)
                chooseImage();
            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                //getBanner
                getBannerImage();

                //getMenu
                getMenu();

                //Topping List
                getToppingList();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);

                //getBanner
                getBannerImage();

                //getMenu
                getMenu();

                //Topping List
                getToppingList();
            }
        });


        //InitDB
        initDB();

        //CheckSessionLogin
        checkSessionLogin();

    }

    private void checkSessionLogin() {
        if (AccountKit.getCurrentAccessToken() !=null)
        {
            swipeRefreshLayout.setRefreshing(true);

            //Cek user
            AccountKit.getCurrentAccount(new AccountKitCallback<com.facebook.accountkit.Account>() {
                @Override
                public void onSuccess(final com.facebook.accountkit.Account account) {
                    mService.checkUserExists(account.getPhoneNumber().toString())
                            .enqueue(new Callback<CheckUserResponse>() {
                                @Override
                                public void onResponse(Call<CheckUserResponse> call, Response<CheckUserResponse> response) {
                                    CheckUserResponse userResponse = response.body();

                                    if (userResponse.isExists())
                                    {
                                        //Meminta informasi
                                        mService.getUserInformation(account.getPhoneNumber().toString())
                                                .enqueue(new Callback<User>() {
                                                    @Override
                                                    public void onResponse(Call<User> call, Response<User> response) {
                                                        Common.currentUser = response.body();
                                                        if (Common.currentUser != null) {
                                                            swipeRefreshLayout.setRefreshing(false);


                                                                //set info
                                                                txt_name.setText(Common.currentUser.getName());
                                                                txt_phone.setText(Common.currentUser.getPhone());

                                                                //Set Foto profil
                                                                if (!TextUtils.isEmpty(Common.currentUser.getAvatarUrl())) {
                                                                    Picasso.with(getBaseContext())
                                                                            .load(new StringBuilder(Common.BASE_URL)
                                                                                    .append("user_avatar/")
                                                                                    .append(Common.currentUser.getAvatarUrl()).toString())
                                                                            .into(img_avatar);
                                                                }

                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<User> call, Throwable t) {
                                                        swipeRefreshLayout.setRefreshing(false);
                                                        Log.d("ERROR", t.getMessage());
                                                    }
                                                });
                                    }
                                    else
                                    {
                                        //Jika user tidak ada, muncul login
                                        startActivity(new Intent(HomeActivity.this,MainActivity.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<CheckUserResponse> call, Throwable t) {
                                    Log.d("ERROR", t.getMessage());
                                }
                            });
                }

                @Override
                public void onError(AccountKitError accountKitError) {
                    Log.d("ERROR", accountKitError.getErrorType().getMessage());
                }
            });
        }
        else
        {
            AccountKit.logOut();

            //clear activity
            Intent intent = new Intent(HomeActivity.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

    }

    private void chooseImage() {
        startActivityForResult(Intent.createChooser(FileUtils.createGetContentIntent(),"Pilih file"),
                PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == PICK_FILE_REQUEST)
            {
                if (data != null)
                {
                    selectedFileUri = data.getData();
                    if (selectedFileUri != null && !selectedFileUri.getPath().isEmpty())
                    {
                        img_avatar.setImageURI(selectedFileUri);
                        uploadFile();
                    }
                }
            }
        }
    }

    private void uploadFile() {
        if (selectedFileUri != null)
        {
            File file = FileUtils.getFile(this,selectedFileUri);

            String fileName = new StringBuilder(Common.currentUser.getPhone())
                    .append(FileUtils.getExtension(file.toString()))
                    .toString();

            ProgressRequestBody requestFile = new ProgressRequestBody(file,this);

            final MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file",fileName, requestFile);

            final MultipartBody.Part userPhone = MultipartBody.Part.createFormData("phone",Common.currentUser.getPhone());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mService.uploadFile(userPhone,body)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    Toast.makeText(HomeActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(HomeActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).start();
        }
    }

    private void initDB() {
        Common.dreyRoomDatabase = DREYRoomDatabase.getInstance(this);
        Common.cartRepository = CartRepository.getInstance(CartDataSource.getInstance(Common.dreyRoomDatabase.cartDAO()));
        Common.favoriteRepository = FavoriteRepository.getInstance(FavoriteDataSource.getInstance(Common.dreyRoomDatabase.favoriteDAO()));
    }

    private void getToppingList() {
        compositeDisposable.add(mService.getBarang(Common.TOPPING_MENU_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Barang>>() {
                    @Override
                    public void accept(List<Barang> barangs) throws Exception {
                        Common.toppingList = barangs;
                    }
                }));
    }

    private void getMenu() {
        compositeDisposable.add(mService.getBanners()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Banner>>() {
                    @Override
                    public void accept(List<Banner> banners) throws Exception {
                        displayImage(banners);
                    }
                }));
    }

    private void getBannerImage() {
        compositeDisposable.add(mService.getMenu()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<List<Category>>() {
            @Override
            public void accept(List<Category> categories) throws Exception {
                displayMenu(categories);
            }
        }));
    }

    private void displayMenu(List<Category> categories) {
        CategoryAdapter adapter = new CategoryAdapter(this,categories);
        lst_menu.setAdapter(adapter);

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void displayImage(List<Banner> banners) {
        HashMap<String, String> bannerMap = new HashMap<>();
        for (Banner item:banners)
            bannerMap.put(item.getName(),item.getLink());

        for (String name:bannerMap.keySet())
        {
            TextSliderView textSliderView = new TextSliderView(this);
            textSliderView.description(name)
                    .image(bannerMap.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit);
            sliderLayout.addSlider(textSliderView);
        }


    }

    //Keluar app
    boolean isBackButtonClicked = false;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (isBackButtonClicked)
            {
                super.onBackPressed();
                return;
            }
            this.isBackButtonClicked = true;
            Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        View view = menu.findItem(R.id.cart_menu).getActionView();
        badge = (NotificationBadge)view.findViewById(R.id.badge);
        cart_icon = (ImageView)view.findViewById(R.id.cart_icon);
        cart_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,CartActivity.class));
            }
        });

        updateCartCount();
        return true;
    }

    private void updateCartCount() {
        if (badge == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Common.cartRepository.countCartItems() == 0)
                    badge.setVisibility(View.VISIBLE);
                else
                {
                    badge.setVisibility(View.VISIBLE);
                    badge.setText(String.valueOf(Common.cartRepository.countCartItems()));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.cart_menu) {
            return true;
        }
        else if (id == R.id.search_menu) {
            startActivity(new Intent(HomeActivity.this, SearchActivity.class));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sign_out) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Keluar Aplikasi");
            builder.setMessage("Apakah kamu ingin keluar dari aplikasi?");

            builder.setNegativeButton("Ya", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AccountKit.logOut();

                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });

            builder.setPositiveButton("Batal", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }
        else if (id == R.id.nav_favorite)
        {
            if (Common.currentUser != null)
            {
                startActivity(new Intent(HomeActivity.this,FavoriteListActivity.class));
            }
            else
            {
                Toast.makeText(this, "Ups! Login untuk menggunakan fitur ini", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.nav_show_orders)
        {
            if (Common.currentUser != null)
            {

                startActivity(new Intent(HomeActivity.this,ShowOrderActivity.class));
            }
            else 
            {
                Toast.makeText(this, "Ups! Login untuk menggunakan fitur ini", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.nav_nearby_store)
        {
            startActivity(new Intent(HomeActivity.this,NearbyStore.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartCount();
        isBackButtonClicked = false;
    }

    @Override
    public void onProgressUpdate(int pertantage) {

    }


}
