package fr.vergne.pareto;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

import org.junit.Test;

public class ParetoHelperTest {

	@Test
	public void testMaximalFrontier() {
		ParetoComparator<Point> comparator = new ParetoComparator<Point>();
		comparator.add(new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				return Integer.valueOf(o1.x).compareTo(Integer.valueOf(o2.x));
			}
		});
		comparator.add(new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				return Integer.valueOf(o1.y).compareTo(Integer.valueOf(o2.y));
			}
		});

		Point p00 = new Point(0, 0);
		Point p01 = new Point(0, 1);
		Point p02 = new Point(0, 2);
		Point p10 = new Point(1, 0);
		Point p11 = new Point(1, 1);
		Point p12 = new Point(1, 2);
		Point p20 = new Point(2, 0);
		Point p21 = new Point(2, 1);
		Point p22 = new Point(2, 2);
		Collection<Point> points = new HashSet<Point>(Arrays.asList(p00, p01,
				p02, p10, p11, p12, p20, p21, p22));

		// XXO
		// XXX
		// XXX
		{
			Collection<Point> frontier = ParetoHelper
					.<Point> getMaximalFrontierOf(points, comparator);
			assertEquals(1, frontier.size());
			assertTrue(frontier.contains(p22));
		}

		// XO
		// XXO
		// XXX
		{
			points.remove(p22);
			Collection<Point> frontier = ParetoHelper
					.<Point> getMaximalFrontierOf(points, comparator);
			assertEquals(2, frontier.size());
			assertTrue(frontier.contains(p12));
			assertTrue(frontier.contains(p21));
		}

		// O
		// XO
		// XXO
		{
			points.remove(p12);
			points.remove(p21);
			Collection<Point> frontier = ParetoHelper
					.<Point> getMaximalFrontierOf(points, comparator);
			assertEquals(3, frontier.size());
			assertTrue(frontier.contains(p02));
			assertTrue(frontier.contains(p11));
			assertTrue(frontier.contains(p20));
		}
	}

	@Test
	public void testMinimalFrontier() {
		ParetoComparator<Point> comparator = new ParetoComparator<Point>();
		comparator.add(new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				return Integer.valueOf(o1.x).compareTo(Integer.valueOf(o2.x));
			}
		});
		comparator.add(new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				return Integer.valueOf(o1.y).compareTo(Integer.valueOf(o2.y));
			}
		});

		Point p00 = new Point(0, 0);
		Point p01 = new Point(0, 1);
		Point p02 = new Point(0, 2);
		Point p10 = new Point(1, 0);
		Point p11 = new Point(1, 1);
		Point p12 = new Point(1, 2);
		Point p20 = new Point(2, 0);
		Point p21 = new Point(2, 1);
		Point p22 = new Point(2, 2);
		Collection<Point> points = new HashSet<Point>(Arrays.asList(p00, p01,
				p02, p10, p11, p12, p20, p21, p22));

		// XXX
		// XXX
		// OXX
		{
			Collection<Point> frontier = ParetoHelper
					.<Point> getMinimalFrontierOf(points, comparator);
			assertEquals(1, frontier.size());
			assertTrue(frontier.contains(p00));
		}

		// XXX
		// OXX
		// _OX
		{
			points.remove(p00);
			Collection<Point> frontier = ParetoHelper
					.<Point> getMinimalFrontierOf(points, comparator);
			assertEquals(2, frontier.size());
			assertTrue(frontier.contains(p01));
			assertTrue(frontier.contains(p10));
		}

		// OXX
		// _OX
		// __O
		{
			points.remove(p01);
			points.remove(p10);
			Collection<Point> frontier = ParetoHelper
					.<Point> getMinimalFrontierOf(points, comparator);
			assertEquals(3, frontier.size());
			assertTrue(frontier.contains(p02));
			assertTrue(frontier.contains(p11));
			assertTrue(frontier.contains(p20));
		}
	}

}
