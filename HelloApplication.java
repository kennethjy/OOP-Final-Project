package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static java.lang.Math.*;

public class HelloApplication extends Application {
    private Map<String, Input> keyInputs;
    private final List<String> keydownKeys = new ArrayList<>();
    private final List<String> keyDownThisFrame = new ArrayList<>();
    private final List<longNote> longs = new ArrayList<>();
    protected double offset;
    protected double bpm;
    MediaPlayer player;
    ImageView iView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("idk");

        Pane container = new Pane();

        List<Note> Notes = readTapNotesFromFile(container);

        initializeKeyInputs();

        Rectangle line = new Rectangle(0, 950, 1000, 10);
        line.setFill(Color.BLACK);
        container.getChildren().add(line);

        iView = new ImageView(new Image("C:\\Users\\user\\IdeaProjects\\demo\\E.png"));
        iView.setFitHeight(1000);

        Scene scene = new Scene(container, 1000, 1000);
        scene.setOnKeyPressed(event -> handleKeyPress(event.getCode().toString()));
        scene.setOnKeyReleased(event -> handleKeyRelease(event.getCode().toString()));

        primaryStage.setScene(scene);
        primaryStage.show();

        Media song = new Media(new File("RushE.mp3").toURI().toString());
        player = new MediaPlayer(song);
        AnimationTimer animationTimer = new AnimationTimer() {
            long prevTime = System.currentTimeMillis();

            @Override
            public void handle(long currentTime) {
                // play song
                player.setAutoPlay(true);
                // getting time between this frame and previous frame
                long deltaTime = System.currentTimeMillis() - prevTime;
                prevTime = System.currentTimeMillis();

                // checking keys tapped on this frame
                List<Input> list = new ArrayList<>();
                for (String key : keyDownThisFrame) {
                    if (keyInputs.containsKey(key)) {
                        list.add(keyInputs.get(key));
                    }
                }

                // checking keys held down
                List<Input> kDownList = new ArrayList<>();
                for (String key : keydownKeys) {
                    if (keyInputs.containsKey(key)) {
                        kDownList.add(keyInputs.get(key));
                    }
                }

                // reduce time for each note in the list
                for (int i = 0; i < Notes.toArray().length; i++) {
                    Note note = Notes.get(i);
                    note.reduceTime(deltaTime);

                    // watch until the end of the demo for E
                    if (note.getTimeStart() <= -50 && note.getClass().getName().equals("com.example.demo.E") && !container.getChildren().contains(iView)){
                        container.getChildren().add(iView);
                    }

                    // check if notes are hit
                    if (note.getTimeStart() < 80){
                        if(note.judge(list, container) > 0){
                            list.remove(note.getPointHit());
                            // E
                            if (note.getClass().getName().equals("com.example.demo.E") && !container.getChildren().contains(iView)){
                                container.getChildren().add(iView);
                            }
                        }
                    }

                    // auto remove notes after it reaches 0
                    if (note.getTimeEnd() < -100){
                        Notes.remove(note);
                        longs.remove(note);
                        note.remove();
                        i++;
                        container.getChildren().remove(note);
                    }
                }

                // check note held down each turn
                for (int i = 0; i < longs.toArray().length; i++){
                    longNote note = longs.get(i);
                    note.checkHeld(kDownList, container);
                }
                keyDownThisFrame.clear();
            }
        };
        animationTimer.start();

    }

    private List<Note> readTapNotesFromFile(Pane pane) throws IOException {
        // parsing each line into a note object and adding it into a list
        List<Note> tapNotes = new ArrayList<>();
        File file = new File("data.txt");
        Scanner chart = new Scanner(file);
        String line;
        line = chart.nextLine();
        offset = Double.parseDouble(line);
        line = chart.nextLine();
        bpm = Double.parseDouble(line);
        chart.nextLine();
        while (chart.hasNextLine()) {
            line = chart.nextLine();
            String[] parts = line.split(" ");
            double time = Double.parseDouble(parts[0]);
            double lane = Double.parseDouble(parts[2]);
            switch (parts[1]) {
                case "bpm" -> { // lane here is actually bpm
                    this.offset += 60000 * time / this.bpm;
                    this.bpm = lane;
                }
                case "tap" -> {
                    time = (int) round((60000 * time / this.bpm) + this.offset);
                    if (parts.length == 4){
                        E note = new E((int) time, lane, 0.9, 900, 900);
                        tapNotes.add(note);
                        pane.getChildren().add(note.getBlock());
                    } else {
                        TapNote note = new TapNote((int) time, lane, 0.9, 900, 900);
                        tapNotes.add(note);
                        pane.getChildren().add(note.getBlock());
                    }

                }
                case "flick" -> {
                    time = (int) round((60000 * time / bpm) + offset);
                    FlickNote note = new FlickNote((int) time, lane, 0.9, 900, 900);
                    tapNotes.add(note);
                    pane.getChildren().add(note.getBlock());
                }
                case "hold" -> {
                    time = (int) round((60000 * time / bpm) + offset);
                    double length = Double.parseDouble(parts[3]);
                    length = (60000 * length / bpm);
                    HoldNote note = new HoldNote((int) time, lane, 0.9, length, 900, 900);
                    tapNotes.add(note);
                    longs.add(note);
                    pane.getChildren().add(note.getBlock());
                }
                case "slide" -> {
                    time = (int) round((60000 * time / bpm) + offset);
                    double t2 = Double.parseDouble(parts[3]);
                    t2 = (int) round((60000 * t2 / bpm) + offset);
                    double m2 = Double.parseDouble(parts[4]);
                    double width = 0.9;
                    if (parts.length >= 7) {
                        if ( parts.length == 8 ) {
                            width = Double.parseDouble(parts[7]);
                        }
                        double t3 = Double.parseDouble(parts[5]);
                        t3 = (int) round((60000 * t3 / bpm) + offset);
                        double m3 = Double.parseDouble(parts[6]);
                        SlideNote note = new SlideNote((int) time, (int) t2, (int) t3, lane, m2, m3, width, 900, 900, pane);
                        tapNotes.add(note);
                        longs.add(note);
                    } else {
                        if ( parts.length == 6 ) {
                            width = Double.parseDouble(parts[5]);
                        }
                        SlideNote note = new SlideNote((int) time, (int) t2, lane, m2, width, 900, 900, pane);
                        tapNotes.add(note);
                        longs.add(note);
                    }
                }
            }
        }
        return tapNotes;
    }
    private void initializeKeyInputs() {
        keyInputs = new HashMap<>();
        keyInputs.put("Q", new Input(0.125, 2));
        keyInputs.put("W", new Input(0.625, 2));
        keyInputs.put("E", new Input(1.125, 2));
        keyInputs.put("R", new Input(1.625, 2));
        keyInputs.put("T", new Input(2.125, 2));
        keyInputs.put("Y", new Input(2.625, 2));
        keyInputs.put("U", new Input(3.125, 2));
        keyInputs.put("I", new Input(3.625, 2));
        keyInputs.put("O", new Input(4.125, 2));
        keyInputs.put("P", new Input(4.625, 2));
        keyInputs.put("A", new Input(0.25, 1));
        keyInputs.put("S", new Input(0.75, 1));
        keyInputs.put("D", new Input(1.25, 1));
        keyInputs.put("F", new Input(1.75, 1));
        keyInputs.put("G", new Input(2.25, 1));
        keyInputs.put("H", new Input(2.75, 1));
        keyInputs.put("J", new Input(3.25, 1));
        keyInputs.put("K", new Input(3.75, 1));
        keyInputs.put("L", new Input(4.25, 1));
        keyInputs.put("SEMICOLON", new Input(4.75, 1));
        keyInputs.put("Z", new Input(0.5, 0));
        keyInputs.put("X", new Input(1, 0));
        keyInputs.put("C", new Input(1.5, 0));
        keyInputs.put("V", new Input(2, 0));
        keyInputs.put("B", new Input(2.5, 0));
        keyInputs.put("N", new Input(3, 0));
        keyInputs.put("M", new Input(3.5, 0));
        keyInputs.put("COMMA", new Input(4, 0));
        keyInputs.put("PERIOD", new Input(4.5, 0));

        // Table to check which key corresponds to what input object
    }

    private void handleKeyPress(String key) {
        if (!keydownKeys.contains(key)) {
            keyDownThisFrame.add(key);
            keydownKeys.add(key);
        }
        // Check keydown events
    }

    private void handleKeyRelease(String key) {
        keydownKeys.remove(key);
        // remove key from list storing held down keys
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
    private double pointHit;

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

    public double getTimeStart() {
        return time;
    }
    public double getTimeEnd() {
        return time;
    }
    public int judge(List<Input> points, Pane pane){

        if (isHit){
            return -2;
        }
        for (Input input : points) {
            double point = input.point;
            if (point <= midpoint + width / 2 + 0.6 && point >= midpoint - width / 2 - 0.6) {
                if (abs(time) <= 120) {
                    isHit = true;
                    pointHit = point;
                    if (abs(time) < 60) {
                        super.setColor(Color.rgb(255, 255, 0, 0.3));
                        time = 20;
                        return 0;
                    }
                    super.setColor(Color.rgb(135, 206, 250, 0.3));
                    time = 20;
                    return 1;
                }
            }
        }
        return -1;
    }
    public double getPointHit(){
        return pointHit;
    }
    public void remove() {
        reduceTime(10000);
    }

}

class E extends TapNote{
    public E(int time, double midpoint, double width, int sWidth, int sHeight) {
        super(time, midpoint, width, sWidth, sHeight);
    }
}
class FlickNote extends Pentagon implements Note{
    protected int time;
    private final int sWidth;
    private final int sHeight;
    protected double width;
    protected final double midpoint;
    protected boolean isHit = false;
    protected boolean hit = false;
    private int length = 60;
    private double pointHit;
    private double height;

    public FlickNote(int time, double midpoint, double width, int sWidth, int sHeight) {
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

    public double getTimeStart() {
        return time;
    }
    public double getTimeEnd() {
        return time;
    }

    public int judge(List<Input> points, Pane pane){
        for(Input input : points) {
            double point = input.point;
            double height = input.height;
            if (hit) {
                if (point <= midpoint + width / 2 + 0.9 && point >= midpoint - width / 2 - 0.9 && height > this.height) {
                    if (abs(time) <= 200) {
                        isHit = true;
                        super.toQuad();
                        if (abs(time) < 100) {
                            super.setColor(Color.rgb(255, 255, 0, 0.3));
                            time = 20;
                            return 0;
                        }
                        super.setColor(Color.rgb(135, 206, 250, 0.3));
                        time = 20;
                        return 1;
                    }
                }
            } else if (point <= midpoint + width / 2 + 0.6 && point >= midpoint - width / 2 - 0.6) {
                if (abs(time) <= 120) {
                    hit = true;
                    this.height = height;
                    pointHit = point;
                    return -1;
                }
            }
        }
        return -1;
    }

    @Override
    public double getPointHit() {
        return pointHit;
    }
    public void remove() {
        reduceTime(10000);
    }

}

class HoldNote extends Quadrilateral implements Note, longNote {
    private int time;
    private final int sWidth;
    private final int sHeight;
    private final double width;
    private final double midpoint;
    private double length;
    private final double oriLength;
    boolean isHit = false;
    boolean isHeld = false;
    public double pointHit;

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
            if(this.time > 0 && this.time <= 80){
                length += time - 20;
            }
            this.time = 20;
        }
        updateBlock();
    }

    public double getTimeStart() {
        return time;
    }
    public double getTimeEnd() {
        return (int) (time + length);
    }
    public int judge(List<Input> points, Pane pane){
        for (Input input : points){
            double point = input.point;
            if (!isHit) {
                if (point <= midpoint + width / 2 + 0.6 && point >= midpoint - width / 2 - 0.6) {
                    if (abs(time) <= 120) {
                        super.setColor(Color.BLACK);
                        isHit = true;
                        pointHit = point;
                        if (abs(time) < 60) {
                            return 0;
                        }
                        return 1;
                    }
                }
            } else {
                return checkHeld(points, pane);
            }
        }
        return -1;
    }
    public double getPointHit(){
        return pointHit;
    }

    public int checkHeld(List<Input> points, Pane pane){
        for (Input input : points){
            double point = input.point;
            if(point <= midpoint + width / 2 + 0.5 && point >= midpoint - width / 2 - 0.5 && isHit){
                setColor(Color.BLACK);
                isHeld = true;
                if(oriLength % 100 == 0) {
                    return 2;
                }
                return 0;
            }
        }
        isHeld = false;
        setColor(Color.GRAY);
        if(oriLength % 100 == 0){
            return -1;
        }
        return 0;
    }
    public void remove() {
        reduceTime(10000);
    }
}

class SlideNote implements Note, longNote{
    private final int time;
    private final int endTime;
    boolean held;
    Pane ptr;
    ArrayList<SlideSegment> blocks = new ArrayList<>();
    ArrayList<SlideSegment> toUpdate = new ArrayList<>();

    SlideNote(int time1, int time2, double midpoint1, double midpoint2, double width, int sWidth, int sHeight, Pane pane){
        for (double i = 0; i < 1.0; i += 0.05){
            double t1 = getValue(i, time1, time2);
            double t2 = getValue(i + 0.05, time1, time2);
            double mid1 = getValue(i, midpoint1, midpoint2);
            double mid2 = getValue(i + 0.05, midpoint1, midpoint2);
            SlideSegment block = new SlideSegment(t1, mid2, mid1, t2 - t1, width, sWidth, sHeight);
            blocks.add(block);
            toUpdate.add(block);
            pane.getChildren().add(block.getBlock());
        }
        time = time1;
        endTime = time2;
        ptr = pane;
    }
    SlideNote(int time1, int time2, int time3, double midpoint1, double midpoint2, double midpoint3, double width, int sWidth, int sHeight, Pane pane){
        for (double i = 0; i < 1.0; i += 0.05){
            double t1 = getValue(i, getValue(i, time1, time2), getValue(i, time2, time3));
            double t2 = getValue(i + 0.05, getValue(i + 0.05, time1, time2), getValue(i + 0.05, time2, time3));
            double mid1 = getValue(i, getValue(i, midpoint1, midpoint2), getValue(i, midpoint2, midpoint3));
            double mid2 = getValue(i + 0.05, getValue(i + 0.05, midpoint1, midpoint2), getValue(i + 0.05, midpoint2, midpoint3));
            SlideSegment block = new SlideSegment(t1, mid2, mid1, t2 - t1, width, sWidth, sHeight);
            blocks.add(block);
            toUpdate.add(block);
            pane.getChildren().add(block.getBlock());
        }
        time = time1;
        endTime = time3;
        ptr = pane;
    }

    private double getValue(double p, double point1, double point2){
        return point1 + p * (point2 - point1);
    }
    public void reduceTime(long time){
        for (SlideSegment block : toUpdate){
            block.reduceTime(time);
            block.updateBlock();
        }
    }
    public double getTimeStart(){
        return time;
    }
    public double getTimeEnd(){
        return endTime;
    }
    public int judge(List<Input> points, Pane pane){
        return -1;
    }

    public int checkHeld(List<Input> points, Pane pane){
        if (toUpdate.size() == 0){
            return -1;
        }
        if (toUpdate.get(0).time == 0 || toUpdate.get(0).length == 0){
            SlideSegment b =  toUpdate.remove(0);
            b.reduceTime(10000);
            return 1;
        }
        boolean tempHeld = toUpdate.get(0).checkHeld(points) >= 0;
        if (tempHeld != held) {
            held = tempHeld;
            if (held) {
                for (SlideSegment segment : toUpdate) {
                    segment.setColor(Color.BLACK);
                }
            } else {
                for (SlideSegment segment : toUpdate) {
                    segment.setColor(Color.GRAY);
                }
            }
        }
        return -1;
    }
    public double getPointHit(){
        return -1;
    }

    @Override
    public void remove() {
        for (SlideSegment block: blocks){
            block.setColor(Color.TRANSPARENT);
        }
    }
}

class SlideSegment extends Quadrilateral{
    protected double time;
    private final int sWidth;
    private final int sHeight;
    protected double width;
    protected double length;
    protected double prevLength;
    protected double midpointTop;
    protected double midpointBottom;
    private boolean isHeld;
    private boolean counted = false;

    SlideSegment(double time, double midpointTop, double midpointBottom, double length, double width, int sWidth, int sHeight) {
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
        if (this.time <= 20 && isHeld){
            length += time - 20;
            this.time = 20;
            length -= time;
            midpointBottom = midpointBottom + length / prevLength * (midpointTop - midpointBottom);
            prevLength = length;
        } else {
            this.time -= time;
        }
    }
    public int checkHeld(List<Input> points){
        if (this.time > 25){
            return -1;
        }
        double midpoint = midpointBottom + length / prevLength * (midpointTop - midpointBottom);
        for (Input input : points){
            double point = input.point;
            if(point <= midpoint + width / 2 + 0.3 && point >= midpoint - width / 2 - 0.3){
                isHeld = true;
                if(length < 10 && !counted) {
                    counted = true;
                    return 1;
                }
                return 0;
            }
        }
        isHeld = false;
        return -1;
    }

    public void updateBlock(){
        super.updateBlock(time, midpointTop, midpointBottom, width, length, sWidth, sHeight);
    }
}

class Input{
    public double point;
    public double height;
    Input(double point, double height){
        this.point = point;
        this.height = height;
    }
}

interface longNote{
    int checkHeld(List<Input> point, Pane pane);
}
interface Note {
    void reduceTime(long time);
    double getTimeStart();
    double getTimeEnd();
    int judge(List<Input> point, Pane pane);
    double getPointHit();
    void remove();
}

class Quadrilateral {
    double[] xPoints = {0, 0, 0, 0};
    double[] yPoints = {0, 0, 0, 0};
    final double hFactor = 1.1;
    final int bottom = 80;
    Polygon block = new Polygon();
    Color color = Color.BLACK;

    /* Basically, this code is to draw a given rectangle using several parameters with a 3d perspective.
    The code simulates 1-point perspective
    the basic idea is the formula gets the percentage value of the point from the top to the bottom.
     */

    public void initBlock(double time, double midpointTop, double midpointBottom, double width, double height, int sWidth, int sHeight) {
        height = min(height, 800 - time);
        int vanishX = (int) round(sWidth * 0.5);
        /* the formula of a point (x,y) on a plane with dimensions 4,1000 is
        ((1000 - y) / 1000)^2 * (x * screen width/4 - x vanishing point) + x vanishing point,
        bottom line + y vanishing point + ((1000 - y) / 1000)^2 * (screen height - y vanishing point)))
        the ^2 is there so the animation looks smoother. */
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

        // setting the points into the polygon
        block.getPoints().addAll(
                xPoints[0], yPoints[0],
                xPoints[1], yPoints[1],
                xPoints[2], yPoints[2],
                xPoints[3], yPoints[3]
        );

    }

    public void updateBlock(double time, double midpointTop, double midpointBottom, double width, double height, int sWidth, int sHeight) {
        // only display when time to the line is 580 ms instead of 1000 ms to simulate horizon line
        if (time > 580 || time + height < 0) {
            block.setFill(Color.TRANSPARENT);
            return;
        }
        block.setFill(color);
        double tempHeight = min(min(height, 580 - time), 580);
        midpointTop = tempHeight / height * (midpointTop - midpointBottom) + midpointBottom;
        height = tempHeight;

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

class Pentagon extends Quadrilateral{
    double[] xPoints = {0, 0, 0, 0, 0};
    double[] yPoints = {0, 0, 0, 0, 0};
    final double hFactor = 1.1;
    final int bottom = 100;
    Polygon block = new Polygon();
    Color color = Color.BLACK;
    private boolean isQuad = false;

    public void initBlock(double time, double midpointTop, double midpointBottom, double width, double height, int sWidth, int sHeight) {
        int vanishX = (int) round(sWidth * 0.5);
        double percentTop = (1000 - time - height * (1 + (1 - time / 800.0) * (hFactor - 1))) / 1000.0;
        xPoints[0] = pow(percentTop, 2) * ((midpointTop - 0.5 + (width / 2.0)) * sWidth / 4 - vanishX) + vanishX;
        xPoints[1] = pow((1000 - time) / 1000.0, 2) * ((midpointBottom - 0.5 + (width / 2.0)) * sWidth / 4 - vanishX) + vanishX;
        xPoints[2] = pow((1000 - time) / 1000.0, 2) * ((midpointBottom - 0.5 - (width / 2.0)) * sWidth / 4 - vanishX) + vanishX;
        xPoints[3] = pow(percentTop, 2) * ((midpointTop - 0.5 - (width / 2.0)) * sWidth / 4 - vanishX) + vanishX;
        xPoints[4] = pow(percentTop, 2) * ((midpointTop - 0.5) * sWidth / 4 - vanishX) + vanishX;

        int vanishY = (int) round(sHeight * -0.1);
        yPoints[0] = bottom + vanishY + pow(percentTop, 2) * (sHeight - vanishY);
        yPoints[1] = bottom + vanishY + pow((1000 - time) / 1000.0, 2) * (sHeight - vanishY);
        yPoints[2] = yPoints[1];
        yPoints[3] = yPoints[0];
        yPoints[4] = bottom + vanishY + pow((1000 - time - height * (1.8 + (1 - time / 800.0) * (hFactor - 1))) / 1000.0, 2) * (sHeight - vanishY);

        block.getPoints().addAll(
                xPoints[0], yPoints[0],
                xPoints[1], yPoints[1],
                xPoints[2], yPoints[2],
                xPoints[3], yPoints[3],
                xPoints[4], yPoints[4]
        );
        if (time < 800) {
            block.setFill(color);
        }

    }

    public void updateBlock(double time, double midpointTop, double midpointBottom, double width, double height, int sWidth, int sHeight) {
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
        xPoints[4] = pow(percentTop, 2) * ((midpointTop - 0.5) * sWidth / 4 - vanishX) + vanishX;

        int vanishY = (int) round(sHeight * -0.1);
        yPoints[0] = bottom + vanishY + pow(percentTop, 2) * (sHeight - vanishY);
        yPoints[1] = bottom + vanishY + pow((1000 - time) / 1000.0, 2) * (sHeight - vanishY);
        yPoints[2] = yPoints[1];
        yPoints[3] = yPoints[0];
        yPoints[4] = bottom + vanishY + pow((1000 - time - height * (1.8 + (1 - time / 800.0) * (hFactor - 1))) / 1000.0, 2) * (sHeight - vanishY);
        yPoints[2] = yPoints[1];

        if (!isQuad){
            block.getPoints().setAll(
                    xPoints[0], yPoints[0],
                    xPoints[1], yPoints[1],
                    xPoints[2], yPoints[2],
                    xPoints[3], yPoints[3],
                    xPoints[4], yPoints[4]
            );
        } else {
            block.getPoints().setAll(
                    xPoints[0], yPoints[0],
                    xPoints[1], yPoints[1],
                    xPoints[2], yPoints[2],
                    xPoints[3], yPoints[3]
            );
        }
    }

    public void toQuad(){
        isQuad = true;
        block.getPoints().removeAll();
        block.getPoints().addAll(
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
