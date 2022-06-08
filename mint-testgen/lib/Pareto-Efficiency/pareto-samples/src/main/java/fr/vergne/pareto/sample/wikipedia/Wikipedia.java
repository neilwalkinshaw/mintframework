package fr.vergne.pareto.sample.wikipedia;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import fr.vergne.pareto.ParetoComparator;
import fr.vergne.pareto.ParetoHelper;
import fr.vergne.pareto.sample.common.Canvas;

/**
 * This sample take the example of the Pareto frontier described on <a
 * href="http://en.wikipedia.org/wiki/Pareto_efficiency#Pareto_frontier"
 * >Wikipedia</a>. The SVG is parsed in order to get the points.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
@SuppressWarnings("serial")
public class Wikipedia extends JFrame {

	public static void main(String[] args) throws URISyntaxException {
		new Wikipedia().setVisible(true);
	}

	public Wikipedia() throws URISyntaxException {
		ParetoComparator<Point> comparator = new ParetoComparator<Point>();
		comparator.add(new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				return Integer.valueOf(o1.x).compareTo(Integer.valueOf(o2.x));
			}
		});
		comparator.add(new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				/*
				 * the comparison is reversed because the graphical Y is in the
				 * opposite sense of the mathematical Y.
				 */
				return -Integer.valueOf(o1.y).compareTo(Integer.valueOf(o2.y));
			}
		});

		File file = new File(ClassLoader.getSystemClassLoader()
				.getResource("Front_pareto.svg").toURI());
		Collection<Point> points = SVGHelper.getPointsFrom(file);

		Canvas canvas = new Canvas();
		canvas.setPoints(points);
		canvas.setFrontier(ParetoHelper.<Point> getMinimalFrontierOf(points,
				comparator));
		canvas.setPreferredSize(new Dimension(500, 500));
		setLayout(new GridLayout(1, 1));
		getContentPane().add(canvas);

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("Wikipedia Sample");
		pack();
	}
}
