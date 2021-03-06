package org.cen.ui.gameboard.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import org.cen.ui.gameboard.AbstractGameBoardElement;

public class Border extends AbstractGameBoardElement {
	public static String BORDER_WIDTH = "borderWidth";

	private final Color color;

	private final double length;

	private final double width;

	public Border(String name, double length, double width, Color color, Point2D position, double orientation, int order) {
		super(name, position, orientation, order);
		this.length = length;
		this.color = color;
		this.width = width;
	}

	@Override
	public void paint(Graphics2D g) {
		g.setColor(color);
		g.fillRect(0, 0, (int) length, (int) width);
	}
}
