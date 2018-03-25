package com.ajdev.dualdonutchart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button btnRefresh;
    private DualDonutChart dualDonutChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dualDonutChart = (DualDonutChart) findViewById(R.id.dualDonutChart);
        btnRefresh = (Button) findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Random r = new Random();
                int low = 100, high = 500;
                int positive = r.nextInt(high-low) + low;
                int negative = r.nextInt(high-low) + low;
                /**
                 * setValues() method of the DualDonutChart helps to dynamically
                 * change the chart values. The last boolean flag enables the animation.
                 */
                dualDonutChart.setValues(positive,negative,true);
            }
        });

        dualDonutChart.setAnimationListener(new DualDonutChart.AnimationListener() {
            @Override
            public void onAnimationStart() {
                btnRefresh.setEnabled(false);
            }

            @Override
            public void onAnimationEnd() {
                btnRefresh.setEnabled(true);
            }
        });
    }
}
