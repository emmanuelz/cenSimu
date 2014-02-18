package org.cen.ui.gameboard.elements.trajectory;

import java.awt.geom.Point2D;

public class KeyFrame implements Comparable<KeyFrame> {
	private TrajectoryMovement movement;
	private double movementSpeed;
	private double orientation;
	private Point2D position;
	private double rotationSpeed;

	private double timestamp;

	public KeyFrame(TrajectoryMovement movement, double movementSpeed, double orientation, double rotationSpeed, Point2D position, double timestamp) {
		super();
		this.movement = movement;
		this.movementSpeed = movementSpeed;
		this.orientation = orientation;
		this.rotationSpeed = rotationSpeed;
		this.position = position;
		this.timestamp = timestamp;
	}

	@Override
	public int compareTo(KeyFrame k) {
		return Double.compare(timestamp, k.timestamp);
	}

	public TrajectoryMovement getMovement() {
		return movement;
	}

	public double getMovementSpeed() {
		return movementSpeed;
	}

	public double getOrientation() {
		return orientation;
	}

	public Point2D getPosition() {
		return position;
	}

	public double getRotationSpeed() {
		return rotationSpeed;
	}

	public double getTimestamp() {
		return timestamp;
	}
}
