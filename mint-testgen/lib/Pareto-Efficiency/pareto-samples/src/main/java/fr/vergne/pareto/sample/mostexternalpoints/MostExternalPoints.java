package fr.vergne.pareto.sample.mostexternalpoints;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import fr.vergne.pareto.ParetoComparator;
import fr.vergne.pareto.ParetoHelper;
import fr.vergne.pareto.sample.common.Canvas;

/**
 * <p>
 * This sample is an example showing a set of 2D points where all the <i>most
 * external</i> points are considered as the frontier to display. In fact, the
 * most external points in the meaning
 * "maximize X or Y in a positive or negative way" or
 * "the one which maximize and the ones which minimize". This way, some cases
 * can show a strange frontier, like a point between two frontier points which
 * can be considered as "as external than the others" but is not in the
 * frontier.
 * </p>
 * <p>
 * This can be observed in particular when one of the coordinates is close to
 * the center. A subjective explanation can be that we try to maximize/minimize
 * X (resp. Y), so a solution having its X (resp. Y) coordinate far to these
 * extremities (close to the center) cannot be selected as a frontier element.
 * </p>
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
/*
 * TODO Make a sample like this one, but completed with rotated axes :
 * 
 * we consider X and Y, but also this axes with a rotation of PI/4. The idea is
 * that we have quite bad results on the angle 0 modulo PI/2, but good ones on
 * PI/4 modulo PI/2. So we try to have a complementary calculation with rotated
 * axes, which are good on the angle 0 modulo PI/2 and bad on PI/4 modulo PI/2.
 */
@SuppressWarnings("serial")
public class MostExternalPoints extends JFrame {

	public static void main(String[] args) {
		new MostExternalPoints().setVisible(true);
	}

	Point center = new Point();

	public MostExternalPoints() {
		ParetoComparator<Point> comparator = new ParetoComparator<Point>();
		comparator.add(new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				/*
				 * The bounding to positive values (regarding the center) is
				 * important, as without it all the points are selected for the
				 * frontier. But I cannot explain it further as I do not get why
				 * for the moment.
				 * 
				 * TODO explain
				 */
				boolean getX = true;
				Integer d1 = Math.max(0, getCoord(o1, getX));
				Integer d2 = Math.max(0, getCoord(o2, getX));
				return d1.compareTo(d2);
			}
		});
		comparator.add(new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				boolean getX = false;
				Integer d1 = Math.max(0, getCoord(o1, getX));
				Integer d2 = Math.max(0, getCoord(o2, getX));
				return d1.compareTo(d2);
			}
		});
		comparator.add(new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				boolean getX = true;
				Integer d1 = Math.max(0, -getCoord(o1, getX));
				Integer d2 = Math.max(0, -getCoord(o2, getX));
				return d1.compareTo(d2);
			}
		});
		comparator.add(new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				boolean getX = false;
				Integer d1 = Math.max(0, -getCoord(o1, getX));
				Integer d2 = Math.max(0, -getCoord(o2, getX));
				return d1.compareTo(d2);
			}
		});

		Collection<Point> points = new HashSet<Point>();
		Random rand = new Random();
		int centerX = 0;
		int centerY = 0;
		for (int i = 0; i < 50; i++) {
			int r = rand.nextInt(50);
			double a = rand.nextFloat() * 2 * Math.PI;
			int x = 50 + (int) (r * Math.cos(a));
			int y = 50 + (int) (r * Math.sin(a));
			points.add(new Point(x, y));
			centerX += x;
			centerY += y;
		}
		centerX /= points.size();
		centerY /= points.size();
		center = new Point(centerX, centerY);

		Canvas canvas = new Canvas();
		canvas.setPoints(points);
		canvas.setFrontier(ParetoHelper.<Point> getMaximalFrontierOf(points,
				comparator));
		canvas.setPreferredSize(new Dimension(500, 500));
		setLayout(new GridLayout(1, 1));
		getContentPane().add(canvas);

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("Circle Sample");
		pack();
	}

	private int getCoord(Point point, boolean getX) {
		return getX ? point.x - center.x : point.y - center.y;
	}

}
