package com.niketgoel.niketexpensemanager.cameraservice;

/**
 * This class represents the condition that we cannot open the camera hardware
 * successfully. For example, another process is using the camera.
 */
public class CameraHardwareException extends Exception {

	private static final long serialVersionUID = 8230180283056198888L;

	public CameraHardwareException(Throwable t) {
        super(t);
    }
}
