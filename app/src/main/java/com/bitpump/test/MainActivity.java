package com.bitpump.test;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.highsoft.highcharts.common.HIColor;
import com.highsoft.highcharts.common.hichartsclasses.HICSSObject;
import com.highsoft.highcharts.common.hichartsclasses.HIChart;
import com.highsoft.highcharts.common.hichartsclasses.HICondition;
import com.highsoft.highcharts.common.hichartsclasses.HIData;
import com.highsoft.highcharts.common.hichartsclasses.HIDataLabels;
import com.highsoft.highcharts.common.hichartsclasses.HIEvents;
import com.highsoft.highcharts.common.hichartsclasses.HIFilter;
import com.highsoft.highcharts.common.hichartsclasses.HILayoutAlgorithm;
import com.highsoft.highcharts.common.hichartsclasses.HIOptions;
import com.highsoft.highcharts.common.hichartsclasses.HIPackedbubble;
import com.highsoft.highcharts.common.hichartsclasses.HIPlotOptions;
import com.highsoft.highcharts.common.hichartsclasses.HIPoint;
import com.highsoft.highcharts.common.hichartsclasses.HIResponsive;
import com.highsoft.highcharts.common.hichartsclasses.HIRules;
import com.highsoft.highcharts.common.hichartsclasses.HIStyle;
import com.highsoft.highcharts.common.hichartsclasses.HISubtitle;
import com.highsoft.highcharts.common.hichartsclasses.HITitle;
import com.highsoft.highcharts.common.hichartsclasses.HITooltip;
import com.highsoft.highcharts.core.HIChartView;
import com.highsoft.highcharts.core.HIFunction;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    HIChartView chartView;
    HashMap<String, ThemeInfo> themeMap;
    public static final String[] colors = new String[]{
            "e51c23",
            "e91e63",
            "9c27b0",
            "673ab7",
            "3f51b5",
            "5677fc",
            "03a9f4",
            "00bcd4",
            "009688",
            "259b24",
            "8bc34a",
            "afb42b",
            "ff9800",
            "ff5722",
            "795548",
            "d64840",
            "d68140",
            "d6c540",
            "9dd640",
            "48d640",
            "40d675",
            "40d6a6",
            "40c9d6",
            "40a9d6",
            "407ad6",
            "404dd6",
            "5940d6",
            "7440d6",
            "8e40d6",
            "b340d6",
            "cf40d6",
            "d640b6"
    };


    public class LimitedQueue<E> extends LinkedList<E> {

        private int limit;

        public LimitedQueue(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean add(E o) {
            boolean added = super.add(o);
            while (added && size() > limit) {
                super.remove();
            }
            return added;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chartView = findViewById(R.id.hc);
        chartView.theme = "dark-unica"; //dark-unica,brand-dark sand-signika.scss
        themeMap = makeColorSymbolMap();

        HIOptions options = new HIOptions();

        HIChart chart = new HIChart();
        chart.setType("packedbubble");

        chart.setHeight("100%");
        options.setChart(chart);

        HITitle title = new HITitle();
        title.setText("업비트 1분 거래량 버블차트");
        options.setTitle(title);

        HISubtitle subtitle = new HISubtitle();
        subtitle.setText("3초간격으로 1분 거래대금을 누적 합니다");
        options.setSubtitle(subtitle);


        HITooltip tooltip = new HITooltip();
        tooltip.setEnabled(true);
        tooltip.setUseHTML(true);
        //tooltip.setPointFormat("<b>{point.name}:</b> {point.y}m CO<sub>2</sub>");
        //tooltip.setPointFormat("<b>{point.name}</b></br>거래대금 : {point.y:,.0f} 만원"); //<sub>만원</sub>
        tooltip.setFormatter(new HIFunction("function(){ return " +
                "'<img src=\"https://static.upbit.com/logos/'+this.point.name+'.png\" style=\"height:10px;width:10px;margin-right:2px\"><b>'+this.point.name+'</b>" +
                "</br>현재가 : <b style=\"font-size:x-small\" >'+this.point.options.custom.c+'</b>" +
                "</br>전일대비 : <b style=\"color:'+(this.point.options.custom.p>0?'#ff504d':'#b2deec')+';font-size:x-small\">'+(this.point.options.custom.p).toFixed(2)+'%</b>" + // 네온 블루, 레드
                "</br>변동률(1m) : <b style=\"color:'+(this.point.options.custom.c_p==0.0?'#FFFFFF':'#FF8C00')+';font-size:x-small\">±'+(this.point.options.custom.c_p.toFixed(2))+'%</b>" +
                "</br>거래대금(1m) : <b style=\"color:#ff504d;font-size:x-small\">'+(this.y/100000000).toFixed(1)+'억</b>" +
                "</br>거래대금(24h) : <b style=\"font-size:x-small\">'+(this.point.options.custom.q/100000000).toFixed(0)+'억</b>'" +
                ";}"));

//        font-size: xx-small;
//        font-size: x-small;
//        font-size: small;
//        font-size: medium;
//        font-size: large;
//        font-size: x-large;
//        font-size: xx-large;
        options.setTooltip(tooltip);

        // point.percentage = 1029.9
//        {point.percentage:,.0f} // returns: `1,030`
//        {point.percentage:,.1f} // returns: `1,029.9`

        HIPlotOptions plotoptions = new HIPlotOptions();
        plotoptions.setPackedbubble(new HIPackedbubble());

        /** 레이아웃 알고리즘 설정 **/
        HILayoutAlgorithm layoutAlgorithm = new HILayoutAlgorithm();
        layoutAlgorithm.setBubblePadding(5);
        //layoutAlgorithm.setEnableSimulation(false); // 애니메이션 효과 끄기
        layoutAlgorithm.setInitialPositions("random"); //"circle", "random"
        layoutAlgorithm.setInitialPositionRadius(10); //Defaults to 20.
        layoutAlgorithm.setFriction(-0.981); //Defaults to -0.981.
        layoutAlgorithm.setGravitationalConstant(0.01); //Defaults to 0.0625.
        //layoutAlgorithm.setMaxSpeed(30); //Defaults to 10.
        plotoptions.getPackedbubble().setLayoutAlgorithm(layoutAlgorithm);

        plotoptions.getPackedbubble().setPoint(new HIPoint());
        plotoptions.getPackedbubble().getPoint().setEvents(new HIEvents());
        plotoptions.getPackedbubble().getPoint().getEvents().setClick(new HIFunction(
                f -> {
                    String symbol = (String) f.getProperty("name");
                    Toast.makeText(MainActivity.this, symbol + " Click!!", Toast.LENGTH_SHORT).show();

//                    tooltip.setEnabled(!tooltip.getEnabled());
//                    chartView.setOptions(options);
                },
                new String[]{"name"}
        ));


        HIDataLabels dataLabels = new HIDataLabels();

        /** 라벨 활성화 **/
        dataLabels.setEnabled(true);
        //dataLabels.setFormatter(new HIFunction("return this.y + '°C';"));
///<img src="https://static.upbit.com/logos/BTC.png" style="height:10px;width:10px;margin:0px;background:red">
        /** 라벨명 포멧 설정 **/
        //dataLabels.setFormat("{point.name}"); // point.name , point.y /// point.options.custom.value
        dataLabels.setFormatter(new HIFunction(
                "function() { if (this.y >= 50000000) { " +
                        "return '" +
                        "<p style=\"font-size:x-small;font-weight:600\">'+this.point.name+'</p><br/>" +
                        "<p style=\"color:#ff6461;'+(this.y <100000000?'font-size:xx-small;':'font-size:x-small')+'\">'+(this.y/100000000).toFixed(1)+'억</p>'" +
                        "+(this.point.options.custom.c_p>0.8?" +
                        "'<br/><p style=\"font-size:x-small;text-decoration:underline;color:'+(this.point.options.custom.p>0?'#ff504d':'#b2deec')+'\">'+(Math.abs(this.point.options.custom.p).toFixed(2))+'%</p>'" +
                        ":" +
                        "'<br/><p style=\"font-size:xx-small;color:'+(this.point.options.custom.p>0?'#ff504d':'#b2deec')+'\">'+(Math.abs(this.point.options.custom.p).toFixed(2))+'%</p>'); " +
                        "}else{ " +
                        "return " +
                        "'<p style=\"font-size:x-small\">'+this.point.name+'</p>' } }"
        ));
        //dataLabels.setFormat("{point.name}<br><p style=\"color:red\">{point.options.custom.value}</p>"); // point.name , point.y /// point.options.custom.value

        /** 라벨명을 표시할 조건 설정**/
        dataLabels.setFilter(new HIFilter());
        dataLabels.getFilter().setProperty("y"); //y value, percentage and others listed under
        dataLabels.getFilter().setOperator(">"); //>, <, >=, <=, ==, and ===.
        dataLabels.getFilter().setValue(20000000);

        /** 라벨명을 스타일 설정**/
        dataLabels.setStyle(new HIStyle());
        //dataLabels.getStyle().setFontWeight("bold");
        dataLabels.getStyle().setFontSize("12");

        /** 라벨 가운데 정렬 **/
        dataLabels.setAlign("center"); //left, center or right.
        dataLabels.setVerticalAlign("middle"); // top, middle or bottom
        //dataLabels.setY(-10);
        //dataLabels.setX(-20);

        ArrayList<HIDataLabels> dataLabelsList = new ArrayList<>();
        dataLabelsList.add(dataLabels);

        plotoptions.getPackedbubble().setDataLabels(dataLabelsList);
        plotoptions.getPackedbubble().setMinSize(3.0f);
        plotoptions.getPackedbubble().setMaxSize(90.0f);

        options.setPlotOptions(plotoptions);

        ArrayList series = new ArrayList<>();

        String[] markets = new String[]{"KRW", "BTC"};
        for (String market : markets) {
            HIPackedbubble krw_bubble = new HIPackedbubble();
            krw_bubble.setName(market);
            krw_bubble.setData(new ArrayList());
            series.add(krw_bubble);

            bubbleMap.put(market, krw_bubble);
        }

        options.setSeries(series);

        HIResponsive responsive = new HIResponsive();
        HIRules rule = new HIRules();
        rule.setCondition(new HICondition());
        rule.getCondition().setMaxHeight(500);
        HashMap<String, HashMap<String, String>> ruleOptions = new HashMap<>();
        HashMap<String, String> legendRules = new HashMap<>();
        legendRules.put("align", "bottom");
        legendRules.put("verticalAlign", "middle");
        legendRules.put("layout", "vertical");
        //ruleOptions.put("legend", legendRules);
        rule.setChartOptions(ruleOptions);
        responsive.setRules(new ArrayList<>(Collections.singletonList(rule)));
        options.setResponsive(responsive);


        chartView.setOptions(options);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //((HIData)packedbubble2.getData().get(0)).setValue(new Random().nextInt(10000));
                //packedbubble2.update(packedbubble2,true);
                request();
            }

        }, 0, 3000);


    }

    public void update() {
    }

    public class InfoValue {
        Object value;
        long timestamp;

        public InfoValue(Object value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }

    public class Info {
        LimitedQueue<InfoValue> queue_q = new LimitedQueue<>(20); // 1분간 거래량을 계산하기 위한 queue
        LimitedQueue<InfoValue> queue_p = new LimitedQueue<>(20); // 1분간 변동률 계산하기 위한 queue
        double c; // 종가
        double p; // 변동률
        double q; // 누적 거래량 UTC 0시 기준
        String t; // 누적 거래량 UTC 0시 기준


    }

    Handler handler = new Handler();

    int UPBIT_POS = 0;
    Map<String, Info> map = new HashMap<>();
    Map<String, HIPackedbubble> bubbleMap = new HashMap<>();

    public void request() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://bitpump.herokuapp.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService service = retrofit.create(RetrofitService.class);

        service.getPosts().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {

                    if (response != null && response.isSuccessful()) {

                        String result = response.body().string();
                        JSONObject data = new JSONObject(result);
                        data.remove("usd2krw");

                        Iterator<String> iterator = data.keys();

                        while (iterator.hasNext()) {
                            String symbol = iterator.next();

                            JSONObject o = data.getJSONObject(symbol);
                            if (!o.getJSONArray("q").isNull(UPBIT_POS)) {
                                double q = o.getJSONArray("q").getDouble(UPBIT_POS); // 누적 거래량
                                double c = o.getJSONArray("c").getDouble(UPBIT_POS); // 종가
                                double p = o.getJSONArray("p").getDouble(UPBIT_POS); // 전일대비 변동률
                                String t = o.getJSONArray("t").getString(UPBIT_POS); // 타겟마켓

                                if (map.get(symbol) == null) {
                                    Info info = new Info();
                                    info.queue_q.add(new InfoValue(q, System.currentTimeMillis()));
                                    info.queue_p.add(new InfoValue(p, System.currentTimeMillis()));
                                    info.q = q;
                                    info.c = c;
                                    info.p = p;
                                    info.t = t;

                                    map.put(symbol, info);
                                } else {


                                    // 24h 거래량이 리셋되었을 경우 Queue를 초기화한다.
                                    if ((double) map.get(symbol).queue_q.getLast().value > q + 1000000000) { // BTC마켓의 경우 비트코인 가격에 따라 오차가 있으므로 보정값으로 10억을 주었다.
                                        map.get(symbol).queue_q.clear();
                                        map.get(symbol).queue_p.clear();
                                    }

                                    map.get(symbol).queue_q.add(new InfoValue(q, System.currentTimeMillis()));
                                    map.get(symbol).queue_p.add(new InfoValue(p, System.currentTimeMillis()));
                                    map.get(symbol).q = q;
                                    map.get(symbol).c = c;
                                    map.get(symbol).p = p;
                                    map.get(symbol).t = t;
                                }
                            }
                        }

                        Map<String, ArrayList<HIData>> new_data = new HashMap<>();
                        Iterator<String> keys = map.keySet().iterator();
                        while (keys.hasNext()) {
                            String symbol = keys.next();
                            Info info = map.get(symbol);

                            if (new_data.get(info.t) == null) {
                                new_data.put(info.t, new ArrayList<>());
                            }

                            if (info.queue_q.size() > 1) { //if(volume1min >0){
                                double volume1min = gapValueDouble(info.queue_q);
                                HIData hiData = new HIData();
                                hiData.setName(symbol);
                                hiData.setValue(volume1min);
                                hiData.setId(symbol);
                                hiData.setCustom(makeCustom(info));
                                hiData.setColor(HIColor.initWithHexValue(colorFromText(BUBBLE_COLOR_DEFAULT, symbol)));
                                new_data.get(info.t).add(hiData);
                            }
                        }

                        keys = new_data.keySet().iterator();
                        while (keys.hasNext()) { //KRW , BTC
                            String t = keys.next();
                            bubbleMap.get(t).setData(new_data.get(t));
                            bubbleMap.get(t).update(bubbleMap.get(t));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    //last_update_time = System.currentTimeMillis();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public static final int BUBBLE_COLOR_DEFAULT = 0;
    public static final int BUBBLE_COLOR_THEME = 1;


    public class ThemeInfo {
        String tag;
        String hexColor;

        public ThemeInfo(String tag) {
            this.tag = tag;
            this.hexColor = getColorHexFromString(tag);

            Log.e(TAG, "ThemeInfo: " + hexColor);
        }

        public String getColorHexFromString(String symbol) {

            int hash = 0;
            for (int i = 0; i < symbol.length(); i++) {
                hash = symbol.charAt(i) + ((hash << 5) - hash);
            }
            String colour = "";
            for (int i = 0; i < 3; i++) {
                int value = (hash >> (i * 8)) & 0xFF;
                colour += String.format("%02X", value);
            }
            return colour;
        }
    }

    public HashMap<String, ThemeInfo> makeColorSymbolMap() {
        HashMap<String, ThemeInfo> result = new HashMap<>();

        ThemeInfo t1 = new ThemeInfo("비트코인");
        result.put("BTC", t1);
        result.put("BCH", t1);
        result.put("BTG", t1);
        result.put("BSV", t1);


        ThemeInfo t2 = new ThemeInfo("플랫폼");
        result.put("XRP", t2);
        result.put("ETH", t2);
        result.put("BTG", t2);
        result.put("BSV", t2);

        ThemeInfo t3 = new ThemeInfo("NFT");
        result.put("MANA", t3);
        result.put("SAND", t3);
        result.put("BORA", t3);
        result.put("AXS", t3);
        result.put("FLOW", t3);
        result.put("CHZ", t3);
        result.put("THETA", t3);
        result.put("ENJ", t3);
        result.put("WAXP", t3);
        result.put("AQT", t3);
        result.put("KLAY", t3);

        ThemeInfo themeDefault = new ThemeInfo("");
        result.put("DEFAULT", themeDefault);

        return result;
    }


    public String colorFromText(int opt, String symbol) {

        if (opt == BUBBLE_COLOR_THEME) {
            if (themeMap.get(symbol) != null) {
                return themeMap.get(symbol).hexColor;
            }
        }

        int hash = 0;
        for (int i = 0; i < symbol.length(); i++) {
            hash = symbol.charAt(i) + ((hash << 5) - hash);
            hash = hash & hash;
        }
        hash = ((hash % colors.length) + colors.length) % colors.length;
        return colors[hash];//Color.parseColor(colors[hash]);
    }

    public String getColorHexFromString(String symbol) {

        int hash = 0;
        for (int i = 0; i < symbol.length(); i++) {
            hash = symbol.charAt(i) + ((hash << 5) - hash);
        }
        String colour = "";
        for (int i = 0; i < 3; i++) {
            int value = (hash >> (i * 8)) & 0xFF;
            colour += String.format("%02X", value);
        }
        return colour;
    }

    public double gapValueDouble(LimitedQueue<InfoValue> queue) {
        double min = (double) queue.getLast().value;
        double max = (double) queue.getLast().value;
        for (InfoValue value : queue) {
            if (System.currentTimeMillis() - value.timestamp < 60000) {
                min = Math.min(min, (double) value.value);
                max = Math.max(max, (double) value.value);
            }
        }
        return max - min;
    }

//    public double gapValueLong(LimitedQueue<InfoValue> queue) {
//        long min = (long) queue.get(0).value;
//        long max = (long) queue.get(0).value;
//        for (InfoValue value : queue) {
//            min = Math.min(min, (long) value.value);
//            max = Math.max(max, (long) value.value);
//        }
//        return max - min;
//    }

    public HashMap<String, Object> makeCustom(Info info) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("c", info.c);
        result.put("p", info.p);
        result.put("q", info.q);
        result.put("c_p", Math.abs(gapValueDouble(info.queue_p)));
        return result;
    }


    public interface RetrofitService {

        // @GET( EndPoint-자원위치(URI) )
        @GET("api/exchangeQuote2")
        Call<ResponseBody> getPosts();

    }
}