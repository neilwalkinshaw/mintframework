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
 * This sample is the same than {@link MostExternalPoints}, but we consider
 * radius and angle instead of X and Y.
 * </p>
 * <p>
 * The result is not really good, moreover we only use one comparator, meaning
 * there is no real Pareto frontier, just ordering in several parts of the
 * space. So it is more a bad example than a real Pareto sample {'^_^}.
 * </p>
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
@SuppressWarnings("serial")
public class MostExternalPoints2 extends JFrame {

	public static void main(String[] args) {
		new MostExternalPoints2().setVisible(true);
	}

	Point center = new Point();

	public MostExternalPoints2() {
		ParetoComparator<Point> comparator = new ParetoComparator<Point>();
		comparator.add(new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				Integer r1 = o1.x * o1.x + o1.y * o1.y;
				Double a1 = Math.signum(o1.y) * Math.acos(o1.x / r1);
				Integer r2 = o2.x * o2.x + o2.y * o2.y;
				Double a2 = Math.signum(o2.y) * Math.acos(o2.x / r1);

				/*
				 * we compare radius only in given "parts" of the angles : we
				 * cut the space like a cake and order elements in each part of
				 * the cake only. Elements in different part are considered as
				 * equivalent (we cannot order them).
				 */
				int parts = 10;
				a1 = Math.floor(a1 / Math.PI * parts) * Math.PI / parts;
				a2 = Math.floor(a2 / Math.PI * parts) * Math.PI / parts;

				if (a1.equals(a2)) {
					return r1.compareTo(r2);
				} else {
					return 0;
				}
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
}
