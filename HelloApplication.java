package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Math.*;

public class HelloApplication extends Application {
    private static final int NOTE_DURATION = 100; // Duration of each note in milliseconds

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("idk");

        Pane container = new Pane();

        List<Note> Notes = readTapNotesFromFile(container);

        Rectangle line = new Rectangle(0, 950, 1000, 10);
        line.setFill(Color.BLACK);
        container.getChildren().add(line);

        Scene scene = new Scene(container, 1000, 1000);
        primaryStage.setScene(scene);
        primaryStage.show();

        AnimationTimer animationTimer = new AnimationTimer() {
            long prevTime = System.currentTimeMillis();

            @Override
            public void handle(long currentTime) {
                long deltaTime = System.currentTimeMillis() - prevTime;
                prevTime = System.currentTimeMillis();

                // Call reduceTime for each object in the list
                int removed = 0;
                for (int i = 0; i < Notes.toArray().length; i++) {
                    Note note = Notes.get(i - removed);
                    note.reduceTime(deltaTime);
                    if (note.getTimeStart() < 0){
                        note.judge(3);
                    }
                    if (note.getTimeEnd() < -100){
                        Notes.remove(note);
                        removed++;
                    }
                }
            }
        };
        animationTimer.start();
    }

    private List<Note> readTapNotesFromFile(Pane pane) throws IOException {
        List<Note> tapNotes = new ArrayList<>();
        File file = new File("data.txt");
        Scanner chart = new Scanner(file);
        String line;
        while (chart.hasNextLine()) {
            line = chart.nextLine();
            String[] parts = line.split(" ");
            int time = Integer.parseInt(parts[0]);
            int lane = Integer.parseInt(parts[2]);
            if (parts[1].equals("tap")) {
                TapNote note = new TapNote(time, lane, 0.9, 900, 900);
                tapNotes.add(note);
                pane.getChildren().add(note.getBlock());
            } else if (parts[1].equals("flick")) {
                FlickNote note = new FlickNote(time, lane, 0.9, 900, 900);
                tapNotes.add(note);
                pane.getChildren().add(note.getBlock());
            } else {
                int length = Integer.parseInt(parts[3]);
                HoldNote note = new HoldNote(time, lane, 0.9, length, 900, 900);
                tapNotes.add(note);
                pane.getChildren().add(note.getBlock());
            }
        }
        return tapNotes;
    }
}


class TapNote extends Quadrilateral implements Note{
    protected int time;
    private final int sWidth;
    private final int sHeight;
    protected double width;
    protected final double midpoint;
    protected boolean isHit = false;
    private int length = 60;

    public TapNote(int time, double midpoint, double width, int sWidth, int sHeight) {
        this.time = time;
        this.midpoint = midpoint;
        this.width = width;
        this.sWidth = sWidth;
        this.sHeight = sHeight;
        initBlock();
    }

    private void initBlock() {
        super.initBlock(time, midpoint, midpoint, width, 20, sWidth, sHeight);
    }

    private void updateBlock() {
        if(!isHit){
            super.updateBlock(time, midpoint, midpoint, width, 20, sWidth, sHeight);
        } else {
            super.updateBlock(time, midpoint, midpoint, width, length, sWidth, sHeight);
        }
    }

    public void reduceTime(long time) {
        if (!isHit) {
            this.time -= (int) time;
        } else {
            this.length += 5;
            this.width -= 0.05;
            if (this.width <= 0){
                this.time = -200;
                block.setFill(Color.TRANSPARENT);
            }
        }
        updateBlock();
    }

    public int getTimeStart() {
        return time;
    }
    public int getTimeEnd() {
        return time;
    }
    public int judge(double point){
        if (isHit){
            return -2;
        }
        if (!(point <= midpoint + width / 2 && point >= midpoint - width / 2)){
            return -1;
        }
        if (abs(time) > 80){
            return -1;
        }
        isHit = true;
        if (abs(time) < 40){
            super.setColor(Color.rgb(255, 255, 0, 0.3));
            time = 20;
            return 0;
        }
        super.setColor(Color.rgb(135, 206, 250, 0.3));
        time = 20;
        return 1;


    }
}

class FlickNote extends TapNote{
    private boolean hit = false;
    private int height;

    public FlickNote(int time, double midpoint, double width, int sWidth, int sHeight) {
        super(time, midpoint, width, sWidth, sHeight);
    }

    public int judge(double point, int height){
        if (hit){
            if (!(point <= midpoint + width / 2 && point >= midpoint - width / 2 && height > this.height)){
                return -1;
            }
            if (abs(time) > 80){
                return -1;
            }
            isHit = true;
            if (abs(time) < 40){
                super.setColor(Color.rgb(255, 255, 0, 0.3));
                time = 20;
                return 0;
            }
            super.setColor(Color.rgb(135, 206, 250, 0.3));
            time = 20;
            return 1;
        }
        if (!(point <= midpoint + width / 2 && point >= midpoint - width / 2)){
            return -1;
        }
        if (abs(time) > 80){
            return -1;
        }
        hit = true;
        this.height = height;
        return -1;
    }

}

class HoldNote extends Quadrilateral implements Note {
    private int time;
    private final int sWidth;
    private final int sHeight;
    private final double width;
    private final double midpoint;
    private double length;
    private final double oriLength;
    boolean isHit = false;

    public HoldNote(int time, double midpoint, double width, double length, int sWidth, int sHeight) {
        this.time = time;
        this.midpoint = midpoint;
        this.width = width;
        this.sWidth = sWidth;
        this.sHeight = sHeight;
        this.length = length;
        this.oriLength = length;
        initBlock();
    }

    private void initBlock() {
        super.initBlock(time, midpoint, midpoint, width, length, sWidth, sHeight);
        setColor(Color.GRAY);
    }

    private void updateBlock() {
        super.updateBlock(time, midpoint, midpoint, width, length, sWidth, sHeight);
    }

    public void reduceTime(long time) {
        if(!isHit) {
            this.time -= (int) time;
        } else {
            length -= time;
        }
        updateBlock();
    }

    public int getTimeStart() {
        return time;
    }
    public int getTimeEnd() {
        return (int) (time + length);
    }
    public int judge(double point){
        if (!(point <= midpoint + width / 2 && point >= midpoint - width / 2)){
            return -1;
        }
        if (abs(time) > 80){
            return -1;
        }
        super.setColor(Color.BLACK);
        isHit = true;
        if (abs(time) < 40){
            return 0;
        }
        return 1;
    }

    public int checkHit(List<Double> points){
        for (double point : points){
            if(point <= midpoint + width / 2 && point >= midpoint - width / 2){
                isHit = true;
                if(oriLength % 100 == 0) {
                    return 1;
                }
                return 0;
            }
        }
        isHit = false;
        return 0;
    }
}

class SlideNote implements Note{
    private int time;
    private int sWidth;
    private int sHeight;
    private double width;
    boolean isHit = false;
    ArrayList<SlideSegment> blocks = new ArrayList<>();
    private double midpoint;

    SlideNote(){

    }
    public void reduceTime(long time){
        for (SlideSegment block : blocks){
            block.reduceTime(time);
        }
    }
    public int getTimeStart(){
        return time;
    }
    public int getTimeEnd(){
        return blocks.get(blocks.toArray().length - 1).time + (int) blocks.get(blocks.toArray().length - 1).length;
    }
    public int judge(List<Double> points){
        if (blocks.get(0).time == 0 && blocks.get(0).length == 0){
            blocks.remove(0);
        }
        return blocks.get(0).checkHeld(points);
    }
}

class SlideSegment extends Quadrilateral{
    protected int time;
    private final int sWidth;
    private final int sHeight;
    protected double width;
    protected double length;
    protected double prevLength;
    protected double midpointTop;
    protected double midpointBottom;
    private boolean isHeld;
    private boolean counted = false;

    SlideSegment(int time, double midpointTop, double midpointBottom, double length, double width, int sWidth, int sHeight) {
        this.time = time;
        this.midpointTop = midpointTop;
        this.midpointBottom = midpointBottom;
        this.length = length;
        this.prevLength = length;
        this.width = width;
        this.sWidth = sWidth;
        this.sHeight = sHeight;
        initBlock();
    }
    private void initBlock() {
        super.initBlock(time, midpointTop, midpointBottom, width, length, sWidth, sHeight);
        setColor(Color.GRAY);
    }
    public void reduceTime(long time){
        this.time -= time;
        if (this.time < 0 && isHeld){
            length -= time;
            midpointBottom = midpointBottom + length / prevLength * (midpointTop - midpointBottom);
            prevLength = length;
        }
    }
    public int checkHeld(List<Double> points){
        double midpoint = midpointBottom + length / prevLength * (midpointTop - midpointBottom);

        for (double point : points){
            if(point <= midpoint + width / 2 && point >= midpoint - width / 2){
                isHeld = true;
                if(length < 10 && !counted) {
                    counted = true;
                    return 1;
                }
                return 0;
            }
        }
        isHeld = false;
        return 0;
    }
}

interface Note {
    void reduceTime(long time);
    int getTimeStart();
    int getTimeEnd();
    int judge(double point);
}

class Quadrilateral {
    double[] xPoints = {0, 0, 0, 0};
    double[] yPoints = {0, 0, 0, 0};
    final double hFactor = 1.1;
    final int bottom = 100;
    Polygon block = new Polygon();
    Color color = Color.BLACK;

    public void initBlock(int time, double midpointTop, double midpointBottom, double width, double height, int sWidth, int sHeight) {
        int vanishX = (int) round(sWidth * 0.5);
        double percentTop = (1000 - time - height * (1 + ((1 - time / 800.0) * (hFactor - 1)))) / 1000.0;
        xPoints[0] = percentTop * ((midpointTop - 0.5 + (width / 2.0)) * sWidth / 4 - vanishX) + vanishX;
        xPoints[1] = (1000 - time) / 1000.0 * ((midpointBottom - 0.5 + (width / 2.0)) * sWidth / 4 - vanishX) + vanishX;
        xPoints[2] = (1000 - time) / 1000.0 * ((midpointBottom - 0.5 - (width / 2.0)) * sWidth / 4 - vanishX) + vanishX;
        xPoints[3] = percentTop * ((midpointTop - 0.5 - (width / 2.0)) * sWidth / 4 - vanishX) + vanishX;

        int vanishY = (int) round(sHeight * -0.1);
        yPoints[0] = bottom + vanishY + percentTop * (sHeight - vanishY);
        yPoints[1] = bottom + vanishY + (1000 - time) / 1000.0 * (sHeight - vanishY);
        yPoints[2] = yPoints[1];
        yPoints[3] = yPoints[0];

        block.getPoints().addAll(
                xPoints[0], yPoints[0],
                xPoints[1], yPoints[1],
                xPoints[2], yPoints[2],
                xPoints[3], yPoints[3]
        );
        if (time < 800) {
            block.setFill(color);
        }

    }

    public void updateBlock(int time, double midpointTop, double midpointBottom, double width, double height, int sWidth, int sHeight) {
        if (time > 800 || time + height < 0) {
            block.setFill(Color.WHITE);
            return;
        }
        block.setFill(color);

        int vanishX = (int) round(sWidth * 0.5);
        double percentTop = (1000 - time - height * (1 + (1 - time / 800.0) * (hFactor - 1))) / 1000.0;
        xPoints[0] = pow(percentTop, 2) * ((midpointTop - 0.5 + (width / 2.0)) * sWidth / 4 - vanishX) + vanishX;
        xPoints[1] = pow((1000 - time) / 1000.0, 2) * ((midpointBottom - 0.5 + (width / 2.0)) * sWidth / 4 - vanishX) + vanishX;
        xPoints[2] = pow((1000 - time) / 1000.0, 2) * ((midpointBottom - 0.5 - (width / 2.0)) * sWidth / 4 - vanishX) + vanishX;
        xPoints[3] = pow(percentTop, 2) * ((midpointTop - 0.5 - (width / 2.0)) * sWidth / 4 - vanishX) + vanishX;

        int vanishY = (int) round(sHeight * -0.1);
        yPoints[0] = bottom + vanishY + pow(percentTop, 2) * (sHeight - vanishY);
        yPoints[1] = bottom + vanishY + pow((1000 - time) / 1000.0, 2) * (sHeight - vanishY);
        yPoints[2] = yPoints[1];
        yPoints[3] = yPoints[0];

        block.getPoints().setAll(
                xPoints[0], yPoints[0],
                xPoints[1], yPoints[1],
                xPoints[2], yPoints[2],
                xPoints[3], yPoints[3]
        );
    }

    public void setColor(Color color){
        this.color = color;
    }
    public Polygon getBlock(){
        return block;
    }
}
