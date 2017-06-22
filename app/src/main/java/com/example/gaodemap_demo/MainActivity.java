package com.example.gaodemap_demo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RouteSearch.OnRouteSearchListener, GeocodeSearch.OnGeocodeSearchListener, AMap.InfoWindowAdapter {
    private MapView mapView;
    private EditText start, end;
    private AMap mmap;
    private RouteSearch routeSearch;
    private GeocodeSearch geocodeSearch;
    private String mStart;
    private String mEnd;
    private Marker geomarker;
    private Marker regeomarker;
    private Location location;
    private LatLonPoint latLonPoint = new LatLonPoint(39.90865, 116.39751);
    private String addressName;
    private boolean value = true;
    private List list;
    private View view;
    private ListView lv;
    private AlertDialog dialog;
    private Marker currentMarker;
    private LatLng mylatlng;
    private LatLng latlog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        mapView = (MapView) findViewById(R.id.MaMap);
        start = (EditText) findViewById(R.id.start);
        end = (EditText) findViewById(R.id.end);
        mapView.onCreate(savedInstanceState);

        if (mmap == null) {
            mmap = mapView.getMap();
        }
        mmap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (currentMarker != null && currentMarker.isInfoWindowShown()) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    currentMarker.hideInfoWindow();
                }
            }
        });

        view = View.inflate(this, R.layout.navlist, null);
        lv = (ListView) view.findViewById(R.id.lv);

        /*
        UiSettings 操控高德地图UI(view)显示
         */
        UiSettings uiSettins = mmap.getUiSettings();
        uiSettins.setZoomControlsEnabled(false);//隐藏缩放图标
        uiSettins.setLogoBottomMargin(-200);//隐藏logo(高德地图)


        mmap.setInfoWindowAdapter(this);

        mmap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                /*
                点击marker覆盖物跳转到本机带有的 地图软件
                */
//                if (checkApkExist(MainActivity.this,"com.autonavi.minimap")){
//                    Intent intent = null;
//                    try {
//
//                        intent = Intent.getIntent("androidamap://viewMap?sourceApplication=GaodeMap_Demo&poiname=天安门&lat=40.3083&lon=116.446813&dev=0");
//                        startActivity(intent);
//                    } catch (URISyntaxException e) {
//                        e.printStackTrace();
//                    }
//
//                }else {
//                    Toast.makeText(MainActivity.this,"未检测到高德地图",Toast.LENGTH_SHORT);
//                }
                currentMarker = marker;
                return false;
            }
        });
        mmap.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                initLv(value);
                dialog.show();
            }
        });

//        geocodeSearch = new GeocodeSearch(this);
//        geocodeSearch.setOnGeocodeSearchListener(this);
//
//        if (null!=location){
//            getAddress(new LatLonPoint(location.getLatitude(),location.getLongitude()));
//        }
    }

    /*
    判断本机是否安有本应用
     */
    public boolean checkApkExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.night:
                mmap.setMapType(AMap.MAP_TYPE_NIGHT);
                break;
            case R.id.in_door:
                mmap.showBuildings(true);
                mmap.showIndoorMap(true);
                break;
            case R.id.nav:
                mmap.setMapType(AMap.MAP_TYPE_NAVI);
                break;
            case R.id.day:
                mmap.setMapType(AMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.location:
                MyLocationStyle myLocationStyle;
                myLocationStyle = new MyLocationStyle();
                myLocationStyle.interval(2000);
                myLocationStyle.showMyLocation(true);
                mmap.setMyLocationStyle(myLocationStyle);
                mmap.getUiSettings().setMyLocationButtonEnabled(true);
                mmap.setMyLocationEnabled(true);
                mmap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location location) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        mylatlng = new LatLng(latitude, longitude);
                    }
                });
                break;

        }
        return true;
    }

    public void click(View view) {
        mStart = start.getText().toString().trim();
        mEnd = end.getText().toString().trim();

        getLatlon(mEnd);


    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int rCode) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }


    /*
    地理编码
    1、逆地理编码回调
    2、地理编码返回的结果
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {

    }

    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        LatLonPoint latLonPoint = result.getGeocodeAddressList().get(0).getLatLonPoint();
        Message msg = handler.obtainMessage();
        msg.what = 0;
        msg.obj = latLonPoint;
        handler.sendMessage(msg);


    }

    /*
    响应地理编码  查询经纬度
     */
    public void getLatlon(String name) {
        //构造 GeocodeSearch 对象，并设置监听。
        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);
        //通过GeocodeQuery设置查询参数,调用getFromLocationNameAsyn(GeocodeQuery geocodeQuery) 方法发起请求。
        //address表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode都ok
        GeocodeQuery query = new GeocodeQuery(name, "010");
        geocodeSearch.getFromLocationNameAsyn(query);
    }

    /*
    响应逆地理编码 查询地址
     */
    public void getAddress(LatLonPoint latLonPoint) {
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocodeSearch.getFromLocationAsyn(query);
    }

    /*
    点击Marker 弹一个dialog
    初始化dialog
     */
    private void initLv(boolean value) {
        if (!value) {
            return;
        }
        if (value) {
            this.value = false;
        }
        boolean installqq = checkApkExist(this, "com.tencent.map");
        boolean installnav = checkApkExist(this, "com.autonavi.minimap");
        boolean installbaidu = checkApkExist(this, "com.baidu.BaiduMap");

        list = new ArrayList<String>();
        if (installqq) {
            list.add("腾讯地图");
        }
        if (installnav) {
            list.add("高德地图");
        }
        if (installbaidu) {
            list.add("百度地图");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (list.get(position).equals("腾讯地图")) {

                } else if (list.get(position).equals("高德地图")) {
                    if (mylatlng != null && latlog != null) {
                        // 高德地图
                        Intent naviIntent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("androidamap://route?sourceApplication=appName&slat=&slon=&sname=我的位置&dlat=" + latlog.latitude + "&dlon=" + latlog.longitude + "&dname=目的地&dev=0&t=2"));
                        startActivity(naviIntent);
                    }
                } else if (list.get(position).equals("百度地图")) {

                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(lv);
        dialog = builder.create();

    }

    @Override
    public View getInfoWindow(Marker marker) {

        return View.inflate(this, R.layout.info_window, null);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                LatLonPoint latlonPoint = (LatLonPoint) msg.obj;

                double longitude = latlonPoint.getLongitude();//经度
                double latitude = latlonPoint.getLatitude();//纬度


                MarkerOptions markerOptions = new MarkerOptions();
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
                latlog = new LatLng(latitude, longitude, true);
                markerOptions.icon(bitmapDescriptor)
                        .alpha(0.7f)
                        .position(latlog)
                        .visible(true)
                        .title("red star")
                        .snippet("Marker测试中");
                mmap.addMarker(markerOptions);
                Toast.makeText(MainActivity.this, latitude + "---" + longitude, Toast.LENGTH_SHORT).show();
            }
        }
    };
}
