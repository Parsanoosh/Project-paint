package org.example;

public enum CameraType {
    FAR(1), NORMAL(1.5), CLOSE(2);


    final double zoom;

    CameraType(double zoom) {
        this.zoom = zoom;
    }

    public double getZoom() {
        return zoom;
    }
}
