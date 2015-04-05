package gui;

import common.Data;
import common.DataListener;
import common.ERSensorConfig;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import java.awt.*;

/**
 * Created by max on 2014-12-06.
 */
public class XYSeriesFrame extends JFrame implements DataListener{

    private static final int PREF_X = 800;
    private static final int PREF_Y = 600;
    private static final double DOMAIN_AXIS_SIZE = 100.0;

    public XYSeriesFrame(ERSensorConfig sensorConfig) {
        super(sensorConfig.sensor + " Plot");
        final JFreeChart chart = ChartFactory.createXYLineChart(
                getTitle(),
                "Time",
                "Value",
                Data.getInstance().getXySeriesCollection(sensorConfig),
                PlotOrientation.VERTICAL,
                true, true, false);
        init(chart);
    }

    private void init(JFreeChart chart) {
        final XYPlot plot = chart.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(DOMAIN_AXIS_SIZE);

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(PREF_X, PREF_Y));
        // TODO: Put chartPanel in scrollPane if we want scrollz
        //ScrollPane scrollPane = new ScrollPane();
        //scrollPane.add(chartPanel);
        //scrollPane.setPreferredSize(new Dimension(PREF_X, PREF_Y));

        setContentPane(chartPanel);
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
        this.pack();

        Data.getInstance().addListener(this);
    }
    @Override
    public void dataUpdated(Data data) {
    }
}
