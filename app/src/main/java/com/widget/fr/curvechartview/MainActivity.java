package com.widget.fr.curvechartview;

import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    CurveChartView chartView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chartView = (CurveChartView) findViewById(R.id.chart_view);

        chartView.setData(new PointF[]{
                new PointF(0,23),
            new PointF(5,53),
            new PointF(10,33),
            new PointF(15,63),
            new PointF(20,43),
            new PointF(25,83),
            new PointF(30,13)
        });
    }
}
