package com.example.remindit2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }

        final TextView location_textview = (TextView)findViewById(R.id.location_textview);

        gpsTracker = new GpsTracker(MainActivity.this);

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        String address = getCurrentAddress(latitude, longitude);
        location_textview.setText(address);

        StrictMode.enableDefaults();

        getWeatherInfomation(latitude, longitude);
        getDustInformation(address);
    }

    private void getDustInformation(String location) {
        String serviceKey = "LCJMIigZhQPogkJ%2BbB4Gbn15OVfx4AfaW1HIukODkYVdK7uwSjYWl8AmBEM8pne2Mpl9d8QbsEV1SJHXLjsQVA%3D%3D";
        String[] array = location.split(" ");
        String stationName = array[2];
        System.out.println(stationName);

        boolean inPm10Grade = false, inPm25Grade = false;

        String pm10Grade = null, pm25Grade = null;

        try {
            URL url = new URL("http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?stationName="
                    +stationName + "&dataTerm=daily&pageNo=1&numOfRows=10&ServiceKey=" + serviceKey + "&ver=1.3");

            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();

            parser.setInput(url.openStream(), null);

            int parserEvent = parser.getEventType();
            System.out.println("start parsing!");

            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    //parser가 시작 태그를 만나면 실행
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("pm10Grade")) {
                            inPm10Grade = true;
                        }
                        else if (parser.getName().equals("pm25Grade")) {
                            inPm25Grade = true;
                        }
                        break;
                    //parser가 내용에 접근했을 때
                    case XmlPullParser.TEXT:
                        if (inPm10Grade) {
                            pm10Grade = parser.getText();
                            inPm10Grade = false;
                        } else if (inPm25Grade) {
                            pm25Grade = parser.getText();
                            inPm25Grade = false;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("item")) {
                            int intPm10Grade = Integer.parseInt(pm10Grade);
                            int intPm25Grade = Integer.parseInt(pm25Grade);
                            if(intPm10Grade == 3 || intPm25Grade == 3){
                                ImageView mask_imageview = (ImageView)findViewById(R.id.mask_imageview);
                                mask_imageview.setImageResource(R.drawable.mask2);
                            }
                            else if(intPm10Grade == 4 || intPm25Grade == 4){
                                ImageView mask_imageview = (ImageView)findViewById(R.id.mask_imageview);
                                mask_imageview.setImageResource(R.drawable.mask3);
                            }
                        }
                        break;
                }
                parserEvent = parser.next();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void getWeatherInfomation(double getLatitude, double getLongitude){
        //TextView status1 = (TextView)findViewById(R.id.weather_textview); //파싱된 결과확인!

        // 요청 메시지 명세
        String serviceKey = "PcQ2tkbOZwhAnzNm06m6ZJCBpso%2Br%2Bq0CabMYV4mSK%2F33AgT3pleaaS6O5SOOAn0kabjkBqEL6COCNMBc16zTg%3D%3D";
        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMdd");
        String baseDate = simpleDate.format(mDate);
        System.out.println("xxxxxxxxx basedate : "+baseDate);
        simpleDate = new SimpleDateFormat("HHmm");
        String baseTime = simpleDate.format(mDate);
        int intBaseTime = Integer.parseInt(baseTime);
        System.out.println("xxxxxxxxxxxxx intSimpleDate : " + intBaseTime);
        if(intBaseTime > 0210 && intBaseTime <= 0510 )
            baseTime = "0200";
        else if(intBaseTime > 0510 && intBaseTime <= 1110 )
            baseTime = "0500";
        else if(intBaseTime > 1110 && intBaseTime <= 1410 )
            baseTime = "1100";
        else if(intBaseTime > 1410 && intBaseTime <= 1710 )
            baseTime = "1400";
        else if(intBaseTime > 1710 && intBaseTime <= 2010 )
            baseTime = "1700";
        else if(intBaseTime > 2010 && intBaseTime <= 2310 )
            baseTime = "2000";
        else if((intBaseTime > 2310 && intBaseTime <= 2400)
                || (intBaseTime > 0000 && intBaseTime <= 0210 ))
            baseTime = "2300";

        System.out.println("xxxxxxxxxxxxx baseTime : " + baseTime);
        String nx = String.valueOf(Integer.parseInt(String.valueOf(Math.round(getLatitude))));
        String ny = String.valueOf(Integer.parseInt(String.valueOf(Math.round(getLongitude))));
        System.out.println("xxxxxxxxx nx : "+nx);
        System.out.println("xxxxxxxxx ny : "+ny);

        boolean initem = false, inFcstValue = false, inCategory = false;

        String fcstValue = null, category = null;
        String POP = null;

        try{
            URL url = new URL("http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst?"
                    +"serviceKey=" + serviceKey + "&numOfRows=10&pageNo=1"
                    + "&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + nx + "&ny=" + ny
            ); //검색 URL부분

            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();

            parser.setInput(url.openStream(), null);

            int parserEvent = parser.getEventType();
            System.out.println("파싱시작합니다.");

            while (parserEvent != XmlPullParser.END_DOCUMENT){
                switch(parserEvent){
                    case XmlPullParser.START_TAG://parser가 시작 태그를 만나면 실행
                        if(parser.getName().equals("category")){ //title 만나면 내용을 받을수 있게 하자
                            inCategory = true;
                        }
                        if(parser.getName().equals("fcstValue")){ //title 만나면 내용을 받을수 있게 하자
                            inFcstValue = true;
                        }
                        break;

                    case XmlPullParser.TEXT://parser가 내용에 접근했을때
                        if(inCategory){
                            category = parser.getText();
                            inCategory = false;
                        }

                        if(inFcstValue){ //isTitle이 true일 때 태그의 내용을 저장.
                            fcstValue = parser.getText();
                            if(category.equals("POP")){
                                POP = fcstValue;
                            }
                            inFcstValue = false;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if(parser.getName().equals("item")){

                            double dPOP = Double.parseDouble(POP);
                            if (dPOP >=0) {
                                //status1.setText("dPOP" + POP);
                                System.out.println("xxxxxxxxx dPOP : " + dPOP);
                                ImageView umbrella_imageview = (ImageView)findViewById(R.id.umbrella_imageview);
                                umbrella_imageview.setImageResource(R.drawable.umbrella2);
                                initem = false;
                            }
                        }
                        break;
                }
                parserEvent = parser.next();
            }
        } catch(Exception e){
            //status1.setText("에러  " + e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                //위치 값을 가져올 수 있음
                ;
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}