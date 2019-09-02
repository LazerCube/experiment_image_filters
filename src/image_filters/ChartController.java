package image_filters;

/**
 * @Author Elliot Lunness
 */

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.layout.BorderPane;

public class ChartController {
    @FXML private BorderPane border_pane;

    @FXML private LineChart<Number, Number> contrast_strech_chart;

    private static XYChart.Series<Number, Number> series;


    @FXML
    void initialize() {
        contrast_strech_chart.setAnimated(false);

        series = new XYChart.Series();

        series.getData().add(new XYChart.Data(0, 0));
        series.getData().add(new XYChart.Data(75, 75));
        series.getData().add(new XYChart.Data(175, 175));
        series.getData().add(new XYChart.Data(255, 255));

        contrast_strech_chart.getData().addAll(series);

        for (Data<Number, Number> data : series.getData()) {
            Node node = data.getNode();
            node.setCursor(Cursor.HAND);
            node.setOnMouseDragged(e -> {
                Point2D pointInScene = new Point2D(e.getSceneX(), e.getSceneY());
                double xAxisLoc = contrast_strech_chart.getXAxis().sceneToLocal(pointInScene).getX();
                double yAxisLoc = contrast_strech_chart.getYAxis().sceneToLocal(pointInScene).getY();
                Number x = contrast_strech_chart.getXAxis().getValueForDisplay(xAxisLoc);
                Number y = contrast_strech_chart.getYAxis().getValueForDisplay(yAxisLoc);
                data.setXValue(x);
                data.setYValue(y);
            });
        }

    }

    public Number getLowerX() {
        return series.getData().get(1).getXValue();
    }

    public Number getLowerY() {
        return series.getData().get(1).getYValue();
    }

    public Number getUpperX() {
        return series.getData().get(2).getXValue();
    }

    public Number getUpperY() {
        return series.getData().get(2).getYValue();
    }

}


