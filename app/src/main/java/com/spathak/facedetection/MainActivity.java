package com.spathak.facedetection;

import android.graphics.RectF;
import android.media.Image;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import com.otaliastudios.cameraview.size.Size;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //Initialize empty vars here
    private AppCompatButton selectImage = null;
    private CameraView camera = null;
    private FaceBoundOverlay faceBoundary = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //checkStoragePermission();

        faceBoundary = findViewById(R.id.faceBoundary);

        camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);

        //Limit framesize for performance
        //camera.setFrameProcessingMaxWidth(512);
        //camera.setFrameProcessingMaxHeight(512);

        //Start Frame processor
        camera.addFrameProcessor(new FrameProcessor() {
            @Override
            @WorkerThread
            public void process(@NonNull Frame frame) {

                processFrame(frame);
            }
        });

    }

    //Process the incoming frame from the camera
    private void processFrame(Frame frame) {
        long time = frame.getTime();
        Size size = frame.getSize();
        int format = frame.getFormat();
        int userRotation = frame.getRotationToUser();
        int viewRotation = frame.getRotationToView();

        InputImage image = null;

        if (frame.getDataClass() == byte[].class) {
            byte[] byteBuffer = frame.getData();

            image = InputImage.fromByteArray(byteBuffer,
                    size.getWidth(),
                    size.getHeight(),
                    viewRotation,
                    InputImage.IMAGE_FORMAT_NV21 // or IMAGE_FORMAT_YV12
            );

            // No need to use this as we are using camera 2 engine and camera 2 engine provides Image not byte array
        } else if (frame.getDataClass() == Image.class) {
            Image data = frame.getData();
            image = InputImage.fromMediaImage(data, viewRotation);
        }
        startFaceDetectionLogic(image, frame.getRotationToUser(), size.getWidth(), size.getHeight());
    }

    private void startFaceDetectionLogic(InputImage image, int rotationToUser, int width, int height) {
        try {
            //Create high accuracy face detector options
            FaceDetectorOptions highAccuracyOpts =
                    new FaceDetectorOptions.Builder()
                            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                            .build();

            FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);

            Task<List<Face>> result =
                    detector.process(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<Face>>() {
                                        @Override
                                        public void onSuccess(List<Face> faces) {
                                            presentFaceDataOnUserUI(faces, rotationToUser, width, height, true);
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            Toast.makeText(MainActivity.this, "Exception in fetching faces: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void presentFaceDataOnUserUI(List<Face> faces, int rotationToUser, int tempWidth, int tempHeight, boolean isFrontLens) {
        boolean rotated = rotationToUser == 90 || rotationToUser == 270;
        ArrayList<RectF> tempFaces = new ArrayList<>();
        for (int i = 0; i < faces.size(); i++) {

            boolean reverseDimens = rotationToUser == 90 || rotationToUser == 270;

            float width = 0;
            float height = 0;
            if (reverseDimens) width = tempHeight;
            else width = tempWidth;

            if (reverseDimens) height = tempWidth;
            else height = tempHeight;

            float scaleX = faceBoundary.getWidth() / width;
            float scaleY = faceBoundary.getHeight() / height;

            //get the face instance
            Face face = faces.get(i);

            float flippedLeft = 0;
            if (isFrontLens) flippedLeft = width - face.getBoundingBox().right;
            else flippedLeft = face.getBoundingBox().left;

            float flippedRight = 0;
            if (isFrontLens) flippedRight = width - face.getBoundingBox().left;
            else flippedRight = face.getBoundingBox().right;

            float scaledLeft = scaleX * flippedLeft;
            float scaledTop = scaleY * face.getBoundingBox().top;
            float scaledRight = scaleX * flippedRight;
            float scaledBottom = scaleY * face.getBoundingBox().bottom;


            RectF boundingbox = new RectF(scaledLeft, scaledTop, scaledRight, scaledBottom);

            // Return the scaled bounding box and a tracking id of the detected face. The tracking id
            // remains the same as long as the same face continues to be detected.
            tempFaces.add(boundingbox);
        }
        faceBoundary.updateFaces(tempFaces);
    }

}