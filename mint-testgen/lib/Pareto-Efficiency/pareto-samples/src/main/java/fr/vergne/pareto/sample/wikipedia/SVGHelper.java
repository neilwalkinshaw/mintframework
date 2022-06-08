package fr.vergne.pareto.sample.wikipedia;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

public class SVGHelper {

	public static Collection<Point> getPointsFrom(File file) {
		Collection<Point> points = new HashSet<Point>();
		FileReader fr;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		BufferedReader reader = new BufferedReader(fr);
		String buffer;
		try {
			while ((buffer = reader.readLine()) != null) {
				buffer = buffer.trim().replaceAll("\"", "'");
				String regex = "^<rect width='([^']+)' height='([^']+)' x='([^']+)' y='([^']+)' [^>]+/>$";
				if (buffer.matches(regex)) {
					buffer = buffer.replaceAll(regex, "$1;$2;$3;$4");
					String[] split = buffer.split(";");
					double width = Double.parseDouble(split[0]);
					double height = Double.parseDouble(split[1]);
					if (width == height) {
						Double x = Double.parseDouble(split[2]);
						Double y = Double.parseDouble(split[3]);
						points.add(new Point(x.intValue(), y.intValue()));
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try {
			reader.close();
			fr.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return points;
	}
}
