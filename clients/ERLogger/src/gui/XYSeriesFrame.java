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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Created by max on 2014-12-06.
 */
public class XYSeriesFrame extends JFrame implements DataListener, WindowListener {

    private static final int PREF_X = 800;
    private static final int PREF_Y = 600;
    private static final double DOMAIN_AXIS_SIZE = 100.0;
    private ERSensorConfig sensorConfig;

    public XYSeriesFrame(ERSensorConfig sensorConfig) {
        this(sensorConfig, PREF_X, PREF_Y);
    }

    private XYSeriesFrame(ERSensorConfig sensorConfig, int width, int height, int x, int y) {
        this(sensorConfig, width, height);
        setLocation(x, y);
    }

    protected XYSeriesFrame(ERSensorConfig sensorConfig, int width, int height) {
        super(sensorConfig.sensor + " Plot");
        this.sensorConfig = sensorConfig;
        final JFreeChart chart = ChartFactory.createXYLineChart(
                getTitle(),
                "Time",
                "Value",
                Data.getInstance().getXySeriesCollection(sensorConfig),
                PlotOrientation.VERTICAL,
                true, true, false);

        final XYPlot plot = chart.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(DOMAIN_AXIS_SIZE);

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(width, height));
        // TODO: Put chartPanel in scrollPane if we want scrollz
        //ScrollPane scrollPane = new ScrollPane();
        //scrollPane.add(chartPanel);
        //scrollPane.setPreferredSize(new Dimension(PREF_X, PREF_Y));

        setContentPane(chartPanel);
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
        this.pack();

        Data.getInstance().addListener(this);
        this.addWindowListener(this);
    }

    @Override
    public void dataUpdated(Data data) {
    }

    @Override
    public void dataReset(Data data) {
        // Respawn
        new XYSeriesFrame(sensorConfig, getWidth(), getHeight(), getX(), getY());
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void windowOpened(WindowEvent windowEvent) {

    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {
        Data.getInstance().removeListener(this);
    }

    @Override
    public void windowClosed(WindowEvent windowEvent) {

    }

    @Override
    public void windowIconified(WindowEvent windowEvent) {

    }

    @Override
    public void windowDeiconified(WindowEvent windowEvent) {

    }

    @Override
    public void windowActivated(WindowEvent windowEvent) {

    }

    @Override
    public void windowDeactivated(WindowEvent windowEvent) {

    }
}
