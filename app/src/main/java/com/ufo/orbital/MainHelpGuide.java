package com.ufo.orbital;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.MessageButtonBehaviour;
import agency.tango.materialintroscreen.SlideFragmentBuilder;

public class MainHelpGuide extends MaterialIntroActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableLastSlideAlphaExitTransition(true);

        //welcome
        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.HelpColor)
                .buttonsColor(R.color.colorAccent)
                .image(R.drawable.bigface)
                .title("Welcome")
                .description("Swipe to Continue Tutorial")
                .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, "Skip"));

        //cam or gallery
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.HelpColor)
                        .buttonsColor(R.color.colorAccent)
                        .image(R.drawable.circle_around_camera)
                        .title("Snap from Your Camera")
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, "Skip"));

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.HelpColor)
                        .buttonsColor(R.color.colorAccent)
                        .image(R.drawable.circle_around_gallery)
                        .title("Take from Your Gallery")
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, "Skip"));

        //man mode
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.HelpColor)
                        .buttonsColor(R.color.colorAccent)
                        .image(R.drawable.combined_image)
                        .title("Tap to Change Mode")
                        .description("Manual Cropping or Face Detection")
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, "Skip"));

        //man1
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.HelpColor)
                        .buttonsColor(R.color.colorAccent)
                        .image(R.drawable.activate_manmode)
                        .title("Crop the Photo")
                        .description("According to Your Preferred Dimensions")
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, "Skip"));

        //man2
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.HelpColor)
                        .buttonsColor(R.color.colorAccent)
                        .image(R.drawable.man2)
                        .title("Drag the Box")
                        .description("Press 'CLICK TO START' when Ready")
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, "Skip"));

        //man3
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.HelpColor)
                        .buttonsColor(R.color.colorAccent)
                        .image(R.drawable.man3)
                        .title("Slide the Bar")
                        .description("To View the Before/After Difference")
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, "Skip"));

        //auto1
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.HelpColor)
                        .buttonsColor(R.color.colorAccent)
                        .image(R.drawable.activate_automode)
                        .title("Face Detection")
                        .description("Let the App Crop For You!")
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, "Skip"));

        //auto2
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.HelpColor)
                        .buttonsColor(R.color.colorAccent)
                        .image(R.drawable.auto2)
                        .title("Tap On the Cards")
                        .description("To View the Different Faces")
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, "Skip"));

        //dir1
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.HelpColor)
                        .buttonsColor(R.color.colorAccent)
                        .image(R.drawable.dir1)
                        .title("View Previous Photos")
                        .description("You Can Check Previously Converted Photos!")
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, "Skip"));

        //dir2
        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.HelpColor)
                        .buttonsColor(R.color.colorAccent)
                        .image(R.drawable.dir2)
                        .title("Scroll Through the List")
                        .description("1. Click on a Photo to Enlarge!\n 2. Hold Click to View Full Size")
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, "Skip"));
    }

    @Override
    public void onFinish() {
        super.onFinish();
    }

}
