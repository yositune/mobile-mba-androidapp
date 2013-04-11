/*
2013 Measuring Broadband America Program
Mobile Measurement Android Application
Copyright (C) 2012  SamKnows Ltd.

The FCC Measuring Broadband America (MBA) Program's Mobile Measurement Effort developed in cooperation with SamKnows Ltd. and diverse stakeholders employs an client-server based anonymized data collection approach to gather broadband performance data in an open and transparent manner with the highest commitment to protecting participants privacy.  All data collected is thoroughly analyzed and processed prior to public release to ensure that subscribers’ privacy interests are protected.

Data related to the radio characteristics of the handset, information about the handset type and operating system (OS) version, the GPS coordinates available from the handset at the time each test is run, the date and time of the observation, and the results of active test results are recorded on the handset in JSON(JavaScript Object Notation) nested data elements within flat files.  These JSON files are then transmitted to storage servers at periodic intervals after the completion of active test measurements.

This Android application source code is made available under the GNU GPL2 for testing purposes only and intended for participants in the SamKnows/FCC Measuring Broadband American program.  It is not intended for general release and this repository may be disabled at any time.


This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


package com.samknows.measurement.activity.components;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ResizeAnimation extends Animation {

    int originalHeight;
    int targetHeight;
    int offsetHeight;
    int adjacentHeightIncrement;
    View view, adjacentView;
    boolean down;

    //This constructor makes the animation start from height 0px
    public ResizeAnimation(View view, int offsetHeight, boolean down) {
        this.view           = view;
        this.originalHeight = 0;
        this.targetHeight   = 0;
        this.offsetHeight   = offsetHeight;
        this.down           = down;
    }
    
    //This constructor allow us to set a starting height
    public ResizeAnimation(View view, int originalHeight, int targetHeight, boolean down) {
        this.view           = view;
        this.originalHeight = originalHeight;
        this.targetHeight   = targetHeight;
        this.offsetHeight   = targetHeight - originalHeight;
        this.down           = down;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newHeight;
        if (down) 
            newHeight = (int) (offsetHeight * interpolatedTime);
        
        else 
            newHeight = (int) (offsetHeight * (1 - interpolatedTime));
        
        //The new view height is based on start height plus the height increment
        view.getLayoutParams().height = newHeight + originalHeight;
        view.requestLayout();
        
        if (adjacentView != null) {
                        //This line is only triggered to animate and adjacent view 
            adjacentView.getLayoutParams().height = view.getLayoutParams().height + adjacentHeightIncrement;
            adjacentView.requestLayout();
        }
    }

    @Override
    public void initialize(int width, int height, int parentWidth,
            int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
    
    public void setAdjacentView(View adjacentView) {
        this.adjacentView = adjacentView;
    }
    
    public void setAdjacentHeightIncrement(int adjacentHeightIncrement) {
        this.adjacentHeightIncrement = adjacentHeightIncrement;
    }
}