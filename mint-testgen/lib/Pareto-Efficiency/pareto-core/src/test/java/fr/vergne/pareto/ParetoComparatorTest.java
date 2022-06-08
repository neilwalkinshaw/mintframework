package fr.vergne.pareto;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.Comparator;

import org.junit.Test;

public class ParetoComparatorTest {

	@Test
	public void testComparator2d() {
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

		{
			Point ref = p00;
			assertTrue(comparator.compare(ref, p00) == 0);
			assertTrue(comparator.compare(ref, p01) < 0);
			assertTrue(comparator.compare(ref, p02) < 0);
			assertTrue(comparator.compare(ref, p10) < 0);
			assertTrue(comparator.compare(ref, p11) < 0);
			assertTrue(comparator.compare(ref, p12) < 0);
			assertTrue(comparator.compare(ref, p20) < 0);
			assertTrue(comparator.compare(ref, p21) < 0);
			assertTrue(comparator.compare(ref, p22) < 0);
		}

		{
			Point ref = p01;
			assertTrue(comparator.compare(ref, p00) > 0);
			assertTrue(comparator.compare(ref, p01) == 0);
			assertTrue(comparator.compare(ref, p02) < 0);
			assertTrue(comparator.compare(ref, p10) == 0);
			assertTrue(comparator.compare(ref, p11) < 0);
			assertTrue(comparator.compare(ref, p12) < 0);
			assertTrue(comparator.compare(ref, p20) == 0);
			assertTrue(comparator.compare(ref, p21) < 0);
			assertTrue(comparator.compare(ref, p22) < 0);
		}

		{
			Point ref = p02;
			assertTrue(comparator.compare(ref, p00) > 0);
			assertTrue(comparator.compare(ref, p01) > 0);
			assertTrue(comparator.compare(ref, p02) == 0);
			assertTrue(comparator.compare(ref, p10) == 0);
			assertTrue(comparator.compare(ref, p11) == 0);
			assertTrue(comparator.compare(ref, p12) < 0);
			assertTrue(comparator.compare(ref, p20) == 0);
			assertTrue(comparator.compare(ref, p21) == 0);
			assertTrue(comparator.compare(ref, p22) < 0);
		}

		{
			Point ref = p10;
			assertTrue(comparator.compare(ref, p00) > 0);
			assertTrue(comparator.compare(ref, p01) == 0);
			assertTrue(comparator.compare(ref, p02) == 0);
			assertTrue(comparator.compare(ref, p10) == 0);
			assertTrue(comparator.compare(ref, p11) < 0);
			assertTrue(comparator.compare(ref, p12) < 0);
			assertTrue(comparator.compare(ref, p20) < 0);
			assertTrue(comparator.compare(ref, p21) < 0);
			assertTrue(comparator.compare(ref, p22) < 0);
		}

		{
			Point ref = p11;
			assertTrue(comparator.compare(ref, p00) > 0);
			assertTrue(comparator.compare(ref, p01) > 0);
			assertTrue(comparator.compare(ref, p02) == 0);
			assertTrue(comparator.compare(ref, p10) > 0);
			assertTrue(comparator.compare(ref, p11) == 0);
			assertTrue(comparator.compare(ref, p12) < 0);
			assertTrue(comparator.compare(ref, p20) == 0);
			assertTrue(comparator.compare(ref, p21) < 0);
			assertTrue(comparator.compare(ref, p22) < 0);
		}

		{
			Point ref = p12;
			assertTrue(comparator.compare(ref, p00) > 0);
			assertTrue(comparator.compare(ref, p01) > 0);
			assertTrue(comparator.compare(ref, p02) > 0);
			assertTrue(comparator.compare(ref, p10) > 0);
			assertTrue(comparator.compare(ref, p11) > 0);
			assertTrue(comparator.compare(ref, p12) == 0);
			assertTrue(comparator.compare(ref, p20) == 0);
			assertTrue(comparator.compare(ref, p21) == 0);
			assertTrue(comparator.compare(ref, p22) < 0);
		}

		{
			Point ref = p20;
			assertTrue(comparator.compare(ref, p00) > 0);
			assertTrue(comparator.compare(ref, p01) == 0);
			assertTrue(comparator.compare(ref, p02) == 0);
			assertTrue(comparator.compare(ref, p10) > 0);
			assertTrue(comparator.compare(ref, p11) == 0);
			assertTrue(comparator.compare(ref, p12) == 0);
			assertTrue(comparator.compare(ref, p20) == 0);
			assertTrue(comparator.compare(ref, p21) < 0);
			assertTrue(comparator.compare(ref, p22) < 0);
		}

		{
			Point ref = p21;
			assertTrue(comparator.compare(ref, p00) > 0);
			assertTrue(comparator.compare(ref, p01) > 0);
			assertTrue(comparator.compare(ref, p02) == 0);
			assertTrue(comparator.compare(ref, p10) > 0);
			assertTrue(comparator.compare(ref, p11) > 0);
			assertTrue(comparator.compare(ref, p12) == 0);
			assertTrue(comparator.compare(ref, p20) > 0);
			assertTrue(comparator.compare(ref, p21) == 0);
			assertTrue(comparator.compare(ref, p22) < 0);
		}

		{
			Point ref = p22;
			assertTrue(comparator.compare(ref, p00) > 0);
			assertTrue(comparator.compare(ref, p01) > 0);
			assertTrue(comparator.compare(ref, p02) > 0);
			assertTrue(comparator.compare(ref, p10) > 0);
			assertTrue(comparator.compare(ref, p11) > 0);
			assertTrue(comparator.compare(ref, p12) > 0);
			assertTrue(comparator.compare(ref, p20) > 0);
			assertTrue(comparator.compare(ref, p21) > 0);
			assertTrue(comparator.compare(ref, p22) == 0);
		}

	}

}
