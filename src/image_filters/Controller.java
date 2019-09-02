package image_filters;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;

public class Controller {
    @FXML private ToggleButton invert_button;
    @FXML private Button cc_button;
    @FXML private Button gamma_button;

    @FXML private Button histogram_button;
    @FXML private Button reset;

    @FXML private Button contrast_button;
    @FXML private TextField gamma_value;
    @FXML private ImageView image_view;

    @FXML private LineChart<Number, Number> red_histogram_chart;
    @FXML private LineChart<Number, Number> green_histogram_chart;
    @FXML private LineChart<Number, Number> blue_histogram_chart;
    @FXML private LineChart<Number, Number> brightness_histogram_chart;

    private final static String fileName = "berries.jpg";

    private Image orgImage;

    private double[][] histogram;
    private double[] mapping;

    @FXML
    void initialize() {
        try {
            setup();
        } catch (Exception e) {
            System.out.println("FILE NOT FOUND!\n" + e);
            System.exit(0);
        }

        //Add all the event handlers (this is a minimal GUI - you may try to do better)
        invert_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //At this point, "image" will be the original image
                //imageView is the graphical representation of an image
                //imageView.getImage() is the currently displayed image

                //Let's invert the currently displayed image by calling the invert function later in the code
                Image inverted_image=ImageInverter(image_view.getImage());
                //Update the GUI so the new image is displayed
                calHistogram(inverted_image);
                image_view.setImage(inverted_image);
            }
        });

        gamma_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try
                {
                    Double val = Double.parseDouble(gamma_value.getText());
                    Image gamma_image = ImageGammaCorrection(image_view.getImage(), val);
                    calHistogram(gamma_image);
                    image_view.setImage(gamma_image);
                }
                catch (NullPointerException | NumberFormatException ex)
                {
                    System.out.println("Not a valid double!");
                }
            }
        });

        contrast_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
//                System.out.println("Contrast Stretching");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("chart.fxml"));
                    Parent root = loader.load();

                    Stage stage = new Stage();

                    stage.setTitle("My New Stage Title");
                    stage.setResizable(false);
                    stage.setScene(new Scene(root, 450, 450));

                    stage.setResizable(false);

                    stage.initModality(Modality.APPLICATION_MODAL);

                    stage.setOnCloseRequest(e -> {
                        ChartController chartCont = loader.getController();

                        Double lowX, lowY, upperX, upperY;

                        lowX = chartCont.getLowerX().doubleValue();
                        lowY = chartCont.getLowerY().doubleValue();

                        upperX = chartCont.getUpperX().doubleValue();
                        upperY = chartCont.getUpperY().doubleValue();

                        Image new_image = contrastStreching(image_view.getImage(), lowX, lowY, upperX, upperY);
                        calHistogram(new_image);
                        image_view.setImage(new_image);
                    });

                    stage.showAndWait();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        reset.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                calHistogram(orgImage);
                image_view.setImage(orgImage);
            }
        });

        histogram_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
//                System.out.println("Histogram");
                Image newImage = histogramEqu(image_view.getImage());
                calHistogram(newImage);
                image_view.setImage(newImage);
            }
        });

        cc_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
//                System.out.println("Cross Correlation");
                Image newImage = filter(image_view.getImage());
                calHistogram(newImage);
                image_view.setImage(newImage);
            }
        });
    }

    private Image filter(Image image) {
        int width = (int)image.getWidth();
        int height = (int)image.getHeight();

        int filterSize = 5;
        int offset = 2;

        int[][] filter = {
                { -4,  -1,  0, -1, -4 },
                { -1,  2,  3, 2, -1 },
                { 0,  3,  4, 3, 0 },
                { -1,  2,  3, 2, -1 },
                { -4,  -1,  0, -1, -4 },
        };

//        int[][] filter = {
//                { 1,  4,  7, 10, 7, 4, 1 },
//                { 4,  12,  26, 33, 26, 12, 4 },
//                { 7,  26,  55, 71, 55, 26, 7},
//                { 10,  33,  71, 91, 71, 33, 10},
//                { 7,  26,  55, 71, 55, 26, 7},
//                { 4,  12,  26, 33, 26, 12, 4 },
//                { 1,  4,  7, 10, 7, 4, 1 },
//        };

//        int[][] filter = {
//                { -1,  0, 1},
//                { -2,  0, 2},
//                { -1,  0, 1},
//        };

//        int[][] filter = {
//                { -1,  -1, -1},
//                { -1,  8, -1},
//                { -1,  -1, -1},
//        };

//        int[][] filter = {
//                { 1,  1, 1, 1, 1},
//                { 1,  1, 1, 1, 1},
//                { 1,  1, 1, 1, 1},
//                 { 1,  1, 1, 1, 1},
//                { 1,  1, 1, 1, 1},
//        };

        WritableImage writable_image = new WritableImage(width, height);
        PixelWriter image_writer = writable_image.getPixelWriter();
        PixelReader image_reader=image.getPixelReader();


        int[][][] kernelPixels = new int[filterSize][filterSize][3];

        int[][][] sumColor = new int[width][height][3];

        int max[] = new int[3];
        int min[] = new int[3];

        boolean initial[] = new boolean[3];

        initial[0] = true;
        initial[1] = true;
        initial[2] = true;

        for(int x = offset; x < width - offset; x++) {
            for(int y = offset; y < height - offset; y++) {

                // get array of colors under current operation
                for (int i = 0; i < filterSize; i++) {
                    for (int j = 0; j < filterSize; j++) {
                        Color colorRGB = image_reader.getColor(x-offset+i, y-offset+j);
                        kernelPixels[i][j][0] = (int)(colorRGB.getRed() * 255);
                        kernelPixels[i][j][1] = (int)(colorRGB.getGreen() * 255);
                        kernelPixels[i][j][2] = (int)(colorRGB.getBlue() * 255);
                    }
                }

                // apply filter
                for(int k = 0; k < 3; k++) {
                    int sum = 0;

                    for (int i = 0; i < filterSize; i++) {
                        for (int j = 0; j < filterSize; j++) {
                            sum += kernelPixels[i][j][k] * filter[i][j];
                        }
                    }

                    if (initial[k]) {
                        min[k] = sum;
                        max[k] = sum;
                        initial[k] = false;
                    } else {
                        if(max[k] < sum) {
                            max[k] = sum;
                        }

                        if(min[k] > sum) {
                            min[k] = sum;
                        }
                    }

                    sumColor[x][y][k] = sum;
                }
            }
        }

        double newRed = 0;
        double newGreen = 0;
        double newBlue = 0;

        for(int x = offset; x < width - offset; x++) {
            for(int y = offset; y < height - offset; y++) {
                newRed = (((sumColor[x][y][0]) - min[0])* 255.0) / (max[0]-min[0]);
                newGreen = (((sumColor[x][y][1]) - min[1])* 255.0) / (max[1]-min[1]);
                newBlue = (((sumColor[x][y][2]) - min[2])* 255.0) / (max[2]-min[2]);

                image_writer.setColor(x, y, Color.color(newRed / 255, newGreen / 255, newBlue / 255));
            }
        }
        return writable_image;
    }

    private void calHistogram(Image image) {
        int width = (int)image.getWidth();
        int height = (int)image.getHeight();

        int data_size = (width * height);

        int levels = 256;

        int red, green, blue, brightness;

        histogram = new double[levels][4]; // Histogram
        mapping = new double[data_size];

        PixelReader image_reader=image.getPixelReader();

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                //For each pixel, get the colour
                Color color = image_reader.getColor(x , y);

                // --------------- RED----------------------
                red = (int)((color.getRed()) * 255);

                // Creating the red histogram
                histogram[red][0]++;

                // --------------- Green -------------------
                green = (int)(color.getGreen()* 255);

                // Creating the green histogram
                histogram[green][1]++;

                // --------------- Blue --------------------
                blue = (int)(color.getBlue() * 255);

                // Creating the blue histogram
                histogram[blue][2]++;

                // --------------- Brightness --------------------
                brightness = (int)(color.getBrightness()* 255);

                // Creating the red histogram
                histogram[brightness][3]++;
            }
        }

        double cumulative_hist = 0;

        // Creating the cumulative distribution function and creating the mapping.
        for(int i = 0; i < levels; i++) {
            cumulative_hist += histogram[i][3];
            mapping[i] = (255.0* cumulative_hist) / data_size;
        }

        XYChart.Series<Number, Number> series1 = new XYChart.Series();
        for(int i = 0; i < levels; i++) {
            series1.getData().add(new XYChart.Data(i, histogram[i][0]));
        }

        XYChart.Series series2 = new XYChart.Series();
        for(int i = 0; i < levels; i++) {
            series2.getData().add(new XYChart.Data(i, histogram[i][1]));
        }

        XYChart.Series series3 = new XYChart.Series();
        for(int i = 0; i < levels; i++) {
            series3.getData().add(new XYChart.Data(i, histogram[i][2]));
        }

        XYChart.Series series4 = new XYChart.Series();
        for(int i = 0; i < levels; i++) {
            series4.getData().add(new XYChart.Data(i, histogram[i][3]));
        }

        red_histogram_chart.setAnimated(false);
        green_histogram_chart.setAnimated(false);
        blue_histogram_chart.setAnimated(false);
        brightness_histogram_chart.setAnimated(false);

        red_histogram_chart.getData().clear();
        green_histogram_chart.getData().clear();
        blue_histogram_chart.getData().clear();
        brightness_histogram_chart.getData().clear();

        red_histogram_chart.getData().add(series1);
        green_histogram_chart.getData().add(series2);
        blue_histogram_chart.getData().add(series3);
        brightness_histogram_chart.getData().add(series4);

    }

    private Image histogramEqu(Image image) {
        int index;

        double brightness;

        //Find the width and height of the image to be process
        int width = (int)image.getWidth();
        int height = (int)image.getHeight();

        WritableImage writable_image = new WritableImage(width, height);
        PixelWriter image_writer = writable_image.getPixelWriter();
        PixelReader image_reader=image.getPixelReader();

        //Iterate over all pixels
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                //For each pixel, get the colour
                Color color = image_reader.getColor(x , y);

                index = (int)((color.getBrightness() * 255));
                brightness = ((mapping[index]) / 255);

                color = Color.hsb(color.getHue(), color.getSaturation(), brightness);

                //Apply the new colour
                image_writer.setColor(x, y, color);
            }
        }
        return writable_image;
    }

    private void setup() throws IOException {
        //Read the image
        Image image = new Image(new FileInputStream(fileName));
        orgImage = image;
        calHistogram(image);

        //Create the graphical view of the image
        image_view.setImage(image);
    }

    //Example function of invert
    public Image ImageInverter(Image image) {
        //Find the width and height of the image to be process
        int width = (int)image.getWidth();
        int height = (int)image.getHeight();
        //Create a new image of that width and height
        WritableImage inverted_image = new WritableImage(width, height);
        //Get an interface to write to that image memory
        PixelWriter inverted_image_writer = inverted_image.getPixelWriter();
        //Get an interface to read from the original image passed as the parameter to the function
        PixelReader image_reader=image.getPixelReader();

        //Iterate over all pixels
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                //For each pixel, get the colour
                Color color = image_reader.getColor(x, y);
                //Do something (in this case invert) - the getColor function returns colours as 0..1 doubles (we could multiply by 255 if we want 0-255 colours)
                color=Color.color(1.0-color.getRed(), 1.0-color.getGreen(), 1.0-color.getBlue());
                //Note: for gamma correction you may not need the divide by 255 since getColor already returns 0-1, nor may you need multiply by 255 since the Color.color function consumes 0-1 doubles.

                //Apply the new colour
                inverted_image_writer.setColor(x, y, color);
            }
        }
        return inverted_image;
    }

    public Image contrastStreching(Image image, Double lowX, Double lowY, Double upperX, Double upperY) {
        double red, green, blue;

        //Find the width and height of the image to be process
        int width = (int)image.getWidth();
        int height = (int)image.getHeight();

        //Create a new image of that width and height
        WritableImage writable_image = new WritableImage(width, height);

        //Get an interface to write to that image memory
        PixelWriter image_writer = writable_image.getPixelWriter();

        //Get an interface to read from the original image passed as the parameter to the function
        PixelReader image_reader=image.getPixelReader();

        //Iterate over all pixels
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                //For each pixel, get the colour
                Color color = image_reader.getColor(x , y);

                red = color.getRed();
                green = color.getGreen();
                blue = color.getBlue();

                red =  calContrastStreching(red, lowX, lowY, upperX, upperY);
                green =  calContrastStreching(green, lowX, lowY, upperX, upperY);
                blue =  calContrastStreching(blue, lowX, lowY, upperX, upperY);

                color = Color.color(red, green, blue);

                //Apply the new colour
                image_writer.setColor(x, y, color);
            }
        }
        return writable_image;
    }

    private Double calContrastStreching(Double val, Double lowX, Double lowY, Double upperX, Double upperY) {
        Double retVal;

        val = val * 255;

        if (val < lowY) {
            retVal = (lowX / lowY) * val;
        } else if(lowY <= val && val <= upperY) {
            retVal = ((upperX - lowX) / (upperY - lowY)) * (val - lowY) + lowX;
        } else {
            retVal = ((255 - upperX) / (255 - upperY)) * (val - upperY) + upperX;
        }
        return retVal / 255;
    }

    public Image ImageGammaCorrection(Image image, double gamma) {
        double red, green, blue;
        double gamma_value = 1.0 / gamma;

        //Find the width and height of the image to be process
        int width = (int)image.getWidth();
        int height = (int)image.getHeight();

        //Create a new image of that width and height
        WritableImage gamma_cor_image = new WritableImage(width, height);

        //Get an interface to write to that image memory
        PixelWriter gamma_cor_image_writer = gamma_cor_image.getPixelWriter();

        //Get an interface to read from the original image passed as the parameter to the function
        PixelReader image_reader=image.getPixelReader();

        //Iterate over all pixels
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                //For each pixel, get the colour
                Color color = image_reader.getColor(x , y );

                red = color.getRed();
                green = color.getGreen();
                blue = color.getBlue();

                red = (Math.pow(red, gamma_value));
                green = (Math.pow(green, gamma_value));
                blue = (Math.pow(blue, gamma_value));

                color = Color.color(red, green, blue);

                //Apply the new colour
                gamma_cor_image_writer.setColor(x, y, color);
            }
        }
        return gamma_cor_image;
    }

}
