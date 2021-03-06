package org.cen.ui.gameboard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumSet;
import java.util.Set;

/**
 * Game board painter. This object draws the game board at the specified
 * dimensions into a given graphics object.
 * 
 * @author Emmanuel ZURMELY
 */
public class GameBoardPainter {

	private static final double ZOOM_FACTOR = 0.2;

	private final Set<GameBoardFlags> drawFlags;

	private IGameBoardService gameBoard;

	// private final BasicStroke[] pathStrokes = { new BasicStroke(3), new
	// BasicStroke(5), new BasicStroke(7) };

	// private List<Location> trajectory;

	/**
	 * The drawing scale.
	 */
	protected double scale;

	/**
	 * The list of shapes to draw.
	 */
	// protected List<ShapeData> shapes = new ArrayList<ShapeData>();

	protected Dimension size;
	private double timestamp = 0.0d;

	private AffineTransform transform;

	private AffineTransform transformInv;

	/**
	 * Constructor.
	 * 
	 * @param servicesProvider
	 *            the services provider
	 */
	public GameBoardPainter() {
		super();
		scale = 1;
		drawFlags = EnumSet.of(GameBoardFlags.OBJECTS, GameBoardFlags.TRAJECTORY, GameBoardFlags.OPPONENT);
	}

	// /**
	// * Adds a shape to draw with the given attributes.
	// *
	// * @param shape
	// * the shape to draw
	// * @param stroke
	// * the stroke to use to draw the shape
	// * @param paint
	// * the paint to use to draw the shape
	// */
	// public void addShape(Shape shape, Stroke stroke, Paint paint) {
	// shapes.add(new ShapeData(shape, stroke, paint));
	// }

	// /**
	// * Clears the list of the shapes to draw.
	// */
	// public void clearShapes() {
	// shapes.clear();
	// }

	public void adjustPosition(int dx, int dy) {
		// convert pixels to real coordinates
		double tx = dx / transform.getScaleX();
		// screen coordinates are indirect, y must be inverted
		double ty = -dy / transform.getScaleX();
		// adjust transform and update inverse
		transform.translate(tx, ty);
		updateInverseTransform();
	}

	public void adjustZoom(double increments, Point position) {
		// zoom factor from increments
		double factor = 1 + ZOOM_FACTOR * Math.abs(increments);
		if (increments > 0) {
			factor = 1 / factor;
		}

		// center zoom on given position
		Point2D c = getRealCoordinates(position);
		double x = c.getX();
		double y = c.getY();
		transform.translate(x, y);
		transform.scale(factor, factor);
		transform.translate(-x, -y);

		// create the inverse transform
		updateInverseTransform();
	}

	private void drawCircle(Graphics2D g2d, int x, int y, int radius) {
		g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
	}

	/**
	 * Returns the draw flags.
	 * 
	 * @return the draw flags
	 */
	public Set<GameBoardFlags> getDrawFlags() {
		return drawFlags;
	}

	public IGameBoardService getGameBoard() {
		return gameBoard;
	}

	/**
	 * Transforms coordinates from scaled coordinates to real coordinates.
	 * 
	 * @param screenCoordinates
	 *            the scaled coordinates in pixel
	 * @return the real coordinates
	 */
	public Point2D getRealCoordinates(Point screenCoordinates) {
		Point2D realCoordinates = new Point2D.Double();
		transformInv.transform(screenCoordinates, realCoordinates);
		return realCoordinates;
	}

	public Dimension getSize() {
		return size;
	}

	public double getTimestamp() {
		return timestamp;
	}

	/**
	 * Renders the game board into the specified graphic object.
	 * 
	 * @param g
	 *            the target graphic object
	 */
	public void paint(Graphics g) {
		// IGameBoardService gameBoard =
		// servicesProvider.getService(IGameBoardService.class);
		// ITrajectoryService trajectoryService =
		// servicesProvider.getService(ITrajectoryService.class);
		// INavigationMap navigationMap = trajectoryService.getNavigationMap();
		// scale transform used for rendering obstacles
		if (transform == null) {
			System.err.println("setSize has not been called");
			return;
		}

		AffineTransform obstacles = AffineTransform.getScaleInstance(1.5, 1.5);
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform oldTransform = g2d.getTransform();
		AffineTransform t = new AffineTransform(oldTransform);
		t.concatenate(transform);
		Paint paint = g2d.getPaint();
		Stroke stroke = g2d.getStroke();
		try {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			for (GameBoardFlags stage : drawFlags) {
				if (stage.equals(GameBoardFlags.OBSTACLES)) {
					g.setColor(new Color(1f, 0f, 0f, .25f));
				}
				for (IGameBoardElement e : gameBoard.getElements()) {
					g2d.setTransform(t);
					Point2D p = e.getPosition();
					double x = p.getX();
					double y = p.getY();
					g2d.translate(x, y);
					double theta = e.getOrientation();
					g2d.rotate(theta);
					switch (stage) {
					case LABELS:
						paintLabels(e, g2d, oldTransform);
						break;
					case OBJECTS:
						paintElement(e, g2d, oldTransform);
						g2d.setPaint(paint);
						g2d.setStroke(stroke);
						break;
					case OBSTACLES:
						Shape s = e.getBounds();
						if (s != null) {
							Area a = new Area(s);
							a.transform(obstacles);
							g2d.fill(a);
						}
						break;
					}
				}
			}
			// g2d.setTransform(transform);
			// if (drawFlags.contains(GameBoardFlags.PATHS)) {
			// paintPaths(g2d, navigationMap, t);
			// }
			// if (drawFlags.contains(GameBoardFlags.TRAJECTORY) && trajectory
			// != null) {
			// TrajectoryPainter trajectoryPainter = new
			// TrajectoryPainter(trajectory);
			// trajectoryPainter.paint(g2d);
			// }

			// paintShapes(g2d);
			// paintRobot(g2d);
			// if (drawFlags.contains(GameBoardFlags.OPPONENT)) {
			// OpponentRobotPainter opponentRobotPainter = new
			// OpponentRobotPainter(servicesProvider);
			// opponentRobotPainter.paint(g2d);
			// }
		} finally {
			g2d.setTransform(oldTransform);
		}
	}

	private void paintElement(IGameBoardElement e, Graphics2D g2d, AffineTransform t) {
		if (e instanceof IGameBoardTimedElement) {
			((IGameBoardTimedElement) e).paint(g2d, timestamp);
		} else {
			e.paint(g2d);
		}
		g2d.setTransform(t);
		Point2D point = e.getPosition();
		Point2D p = transform.transform(point, null);
		g2d.translate(p.getX(), p.getY());
		if (e instanceof IGameBoardTimedElement) {
			((IGameBoardTimedElement) e).paintUnscaled(g2d, timestamp);
		} else {
			e.paintUnscaled(g2d);
		}
	}

	// /**
	// * Renders the shapes into the specified graphic object.
	// *
	// * @param g
	// * the target graphic object
	// */
	// public void paintShapes(Graphics2D g) {
	// for (ShapeData d : shapes) {
	// Stroke stroke = d.getStroke();
	// if (stroke != null) {
	// g.setStroke(stroke);
	// }
	// Paint paint = d.getPaint();
	// if (paint != null) {
	// g.setPaint(paint);
	// }
	// g.draw(d.getShape());
	// }
	// }

	// /**
	// * Draws the navigation points and their labels into the specified graphic
	// * object.
	// *
	// * @param g
	// * the target graphic object
	// */
	// private void paintNavigationPoint(AffineTransform transform, Graphics2D
	// g) {
	// // Draws the point
	// ITrajectoryService trajectoryService =
	// servicesProvider.getService(ITrajectoryService.class);
	// INavigationMap navigationMap = trajectoryService.getNavigationMap();
	// Collection<Location> locations = navigationMap.getLocations();
	// for (Location location : locations) {
	// g.setColor(Color.DARK_GRAY);
	// drawCircle(g, location.getX(), location.getY(), 10);
	// }
	//
	// if (getDrawFlags().contains(GameBoardFlags.LABELS)) {
	// // Draws the name of the point
	// AffineTransform t = g.getTransform();
	// g.setTransform(transform);
	// Point2D point = new Point2D.Float();
	// for (Location location : locations) {
	// Font font = g.getFont().deriveFont(10f);
	// g.setFont(font);
	// Point2D lpoint = location.getPosition();
	// point = t.transform(lpoint, point);
	// // Affichage du nom
	// String name = location.getName();
	// g.drawString(name, (int) point.getX(), (int) point.getY());
	// // Affichage des coordonnées
	// name = "(" + (int) lpoint.getX() + ", " + (int) lpoint.getY() + ")";
	// // g.drawString(name, (int) point.getX(), (int) point.getY() +
	// // 10);
	// }
	// g.setTransform(t);
	// }
	// }

	// private void paintPaths(Graphics2D g2d, INavigationMap navigationMap,
	// AffineTransform t) {
	// g2d.setColor(Color.BLACK);
	// Collection<PathVector> paths = navigationMap.getPathVectors();
	// // Show the Grid of Paths
	// for (PathVector path : paths) {
	// Location start = path.getStart();
	// Location end = path.getEnd();
	// int w = Math.max(Math.min(path.getWeight() / 10, 250), 0);
	// Color color = new Color(w, 0, w);
	// g2d.setColor(color);
	// g2d.setStroke(pathStrokes[w / 85]);
	// g2d.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
	// if (drawFlags.contains(GameBoardFlags.PATHS_WEIGHTS)) {
	// g2d.setTransform(t);
	// Font font = g2d.getFont().deriveFont(10f);
	// g2d.setFont(font);
	// Point p = new Point((start.getX() + end.getX()) / 2, (start.getY() +
	// end.getY()) / 2);
	// transform.transform(p, p);
	// String s = Integer.toString(start.getDistance(end) + path.getWeight());
	// g2d.drawString(s, p.x, p.y);
	// g2d.setTransform(transform);
	// }
	// }
	// // Paint the navigation points and their name
	// paintNavigationPoint(t, g2d);
	// }

	// /**
	// * Renders the robot into the specified graphic object.
	// *
	// * @param g
	// * the target graphic object
	// */
	// private void paintRobot(Graphics2D g2d) {
	// RobotPainter robotPainter = new RobotPainter(servicesProvider);
	// robotPainter.paint(g2d);
	// }

	private void paintLabels(IGameBoardElement e, Graphics2D g, AffineTransform t) {
		g.setTransform(t);
		Point2D point = e.getPosition();
		Point2D p = transform.transform(point, null);

		String name = e.getName();

		Font font = g.getFont().deriveFont(10f);
		g.setFont(font);

		FontRenderContext context = g.getFontRenderContext();
		Rectangle2D rectangle = font.getStringBounds(name, context);
		g.translate(p.getX() - rectangle.getCenterX(), p.getY() - rectangle.getCenterY());
		g.drawString(name, 0, 0);
	}

	public void setGameBoard(IGameBoardService gameBoard) {
		this.gameBoard = gameBoard;
	}

	/**
	 * Sets the size of the rendering area in pixels.
	 * 
	 * @param size
	 *            the size of the rendering area in pixels
	 */
	public void setSize(Dimension size) {
		this.size = size;
		// IGameBoardService gameBoard =
		// servicesProvider.getService(IGameBoardService.class);
		Rectangle2D bounds = gameBoard.getVisibleBounds();
		double sx = size.getWidth() / bounds.getWidth();
		double sy = size.getHeight() / bounds.getHeight();
		scale = Math.min(sx, sy);
		transform = new AffineTransform();
		transform.setToIdentity();
		// (0, 0) is at bottom right
		// positive x goes right
		// positive y goes up
		transform.scale(scale, -scale);
		// set the origin of the game area (0, 0)
		transform.translate(-bounds.getX(), -bounds.getY());
		// make all the board visible
		// transform.translate(-bounds.getWidth(), -bounds.getHeight());
		transform.translate(0, -bounds.getHeight());
		// create the inverse transform for transforming screen coordinates
		// into real coordinates
		updateInverseTransform();
	}

	public void setTimestamp(double timestamp) {
		this.timestamp = timestamp;
	}

	private void updateInverseTransform() {
		try {
			transformInv = transform.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
	}

	// /**
	// * Sets the list of trajectories to render.
	// *
	// * @param trajectory
	// * the list of trajectories to render
	// */
	// public void setTrajectory(List<Location> trajectory) {
	// this.trajectory = trajectory;
	// }
}
