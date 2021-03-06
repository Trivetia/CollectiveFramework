package com.austinv11.collectiveframework.utils.math.geometry;

import com.austinv11.collectiveframework.utils.math.ThreeDimensionalVector;
import com.austinv11.collectiveframework.utils.math.TwoDimensionalVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Base unit for shapes
 */
public class Line {
	
	private ThreeDimensionalVector start, end;
	
	/**
	 * Two dimensional constructor for a line
	 * @param startCoord Start coord of the line
	 * @param endCoord End coord of the line
	 */
	public Line(TwoDimensionalVector startCoord, TwoDimensionalVector endCoord) {
		this(startCoord.to3D(), endCoord.to3D());
	}
	
	/**
	 * Three dimensional constructor for a line
	 * @param startCoord Start coord of the line
	 * @param endCoord End coord of the line
	 */
	public Line(ThreeDimensionalVector startCoord, ThreeDimensionalVector endCoord) {
		start = startCoord;
		end = endCoord;
	}
	
	/**
	 * Gets the starting x coordinate of the line
	 * @return The x coordinate
	 */
	public double getStartX() {
		return start.getX();
	}
	
	/**
	 * Gets the starting y coordinate of the line
	 * @return The y coordinate
	 */
	public double getStartY() {
		return start.getY();
	}
	
	/**
	 * Gets the starting z coordinate of the line
	 * @return The z coordinate
	 */
	public double getStartZ() {
		return start.getZ();
	}
	
	/**
	 * Gets the ending x coordinate of the line
	 * @return The x coordinate
	 */
	public double getEndX() {
		return end.getX();
	}
	
	/**
	 * Gets the ending y coordinate of the line
	 * @return The y coordinate
	 */
	public double getEndY() {
		return end.getY();
	}
	
	/**
	 * Gets the ending z coordinate of the line
	 * @return The z coordinate
	 */
	public double getEndZ() {
		return end.getZ();
	}
	
	/**
	 * Gets the 2D starting point for the line
	 * @return The point
	 */
	public TwoDimensionalVector get2DStart() {
		return start.to2D();
	}
	
	/**
	 * Gets the 2D ending point for the line
	 * @return The point
	 */
	public TwoDimensionalVector get2DEnd() {
		return end.to2D();
	}
	
	/**
	 * Gets the 3D starting point for the line
	 * @return The point
	 */
	public ThreeDimensionalVector get3DStart() {
		return start;
	}
	
	/**
	 * Gets the 3D ending point for the line
	 * @return The point
	 */
	public ThreeDimensionalVector get3DEnd() {
		return end;
	}
	
	/**
	 * Gets the 2D slope for the line
	 * @return The slope or NaN if it is undefined
	 */
	public double get2DSlope() {
		if (end.getX() - start.getX() == 0)
			return Double.NaN;
		return (end.getY() - start.getY())/(end.getX() - start.getX());
	}
	
	private TwoDimensionalVector find2DPoint(double x, double slope) {
		if (Double.isNaN(slope)) {
			return new TwoDimensionalVector(x, start.getY() > end.getY() ? end.getY() : start.getY());
		}
		double yInt = start.getY() - (start.getX() * slope);
		return new TwoDimensionalVector(x, (x * slope) + yInt);
	}
	
	/**
	 * Plugs in the given x coordinate to the line to calculate a 2D point from its equation
	 * @param x The x coordinate
	 * @return The calculated coordinate. NOTE: y will be the minimum y-value of the line if the slope of the line is undefined
	 */
	public TwoDimensionalVector find2DPoint(double x) {
		return find2DPoint(x, get2DSlope());
	}
	
	/**
	 * Checks if the point is valid on a 2D plane
	 * @param coord The coord to check
	 * @return If it's on the line
	 */
	public boolean isPointValid(TwoDimensionalVector coord) {
		if (Double.isNaN(get2DSlope()))
			return ((coord.getY() <= start.getY() && coord.getY() >= end.getY()) || (coord.getY() >= start.getY() && coord.getY() <= end.getY()));
		return ((coord.getX() <= start.getX() && coord.getX() >= end.getX()) || (coord.getX() >= start.getX() && coord.getX() <= end.getX())) && //If point is within bounds of the line
				(find2DPoint(coord.getX()).getY() == coord.getY()); //If the point follows the rules of the line
	}
	
	/**
	 * Returns all points with whole number x values on the line
	 * @return The points
	 */
	public List<TwoDimensionalVector> getAll2DPoints() {
		List<TwoDimensionalVector> vectors = new ArrayList<TwoDimensionalVector>();
		TwoDimensionalVector start = getSorted2DCoords()[0];
		TwoDimensionalVector end = getSorted2DCoords()[1];
		int x = start.getRoundedX();
		if (isPointValid(new TwoDimensionalVector(start.getRoundedX(), start.getRoundedY())))
			vectors.add(new TwoDimensionalVector(start.getRoundedX(), start.getRoundedY()));
		if (Double.isNaN(get2DSlope())) {
			if (isPointValid(new TwoDimensionalVector(x, start.getY()))) {
				int startY = isPointValid(new TwoDimensionalVector(x, start.getRoundedY())) ? start.getRoundedY() : (int)Math.ceil(start.getY());
				for (int y = startY; y <= end.getRoundedY(); y++) {
					if (isPointValid(new TwoDimensionalVector(x, y)))
						vectors.add(new TwoDimensionalVector(x, y));
				}
			}
		} else {
			while (!(x > end.getRoundedX())) {
				x++;
				vectors.add(find2DPoint(x));
			}
		}
		return vectors;
	}
	
	private TwoDimensionalVector[] getSorted2DCoords() {
		TwoDimensionalVector[] array = new TwoDimensionalVector[2];
		if (start.getX() < end.getX()) {
			array[0] = start.to2D();
			array[1] = end.to2D();
		} else {
			array[0] = end.to2D();
			array[1] = start.to2D();
		}
		return array;
	}
	
	private double get2DSlopeForZ() {
		if (end.getX() - start.getX() == 0)
			return Double.NaN;
		return (end.getZ() - start.getZ())/(end.getX() - start.getX());
	}
	
	/**
	 * Plugs in the given x coordinate to the line to calculate a 3D point from its equation
	 * @param x The x coordinate
	 * @return The calculated coordinate
	 */
	public ThreeDimensionalVector find3DPoint(double x) { //Basically does two 2D calculations for y, where the first calculation is actually y and the second z
		double y = find2DPoint(x, get2DSlope()).getY();
		double z = find2DPoint(x, get2DSlopeForZ()).getY();
		return new ThreeDimensionalVector(x, y, z);
	}
	
	/**
	 * Checks if the point is valid on a 3D plane
	 * @param coord The coord to check
	 * @return If it's on the line
	 */
	public boolean isPointValid(ThreeDimensionalVector coord) {
		return ((coord.getX() <= start.getX() && coord.getX() >= end.getX()) || (coord.getX() >= start.getX() && coord.getX() <= end.getX())) && //If point is within bounds of the line
				(find3DPoint(coord.getX()).getY() == coord.getY() && find3DPoint(coord.getX()).getZ() == coord.getZ()); //If the point follows the rules of the line
	}
	
	/**
	 * Returns all points with whole number x values on the line
	 * @return The points
	 */
	public List<ThreeDimensionalVector> getAll3DPoints() {
		List<ThreeDimensionalVector> vectors = new ArrayList<ThreeDimensionalVector>();
		ThreeDimensionalVector start = getSorted3DCoords()[0];
		ThreeDimensionalVector end = getSorted3DCoords()[1];
		int x = start.getRoundedX();
		if (isPointValid(new ThreeDimensionalVector(start.getRoundedX(), start.getRoundedY(), start.getRoundedZ())))
			vectors.add(new ThreeDimensionalVector(start.getRoundedX(), start.getRoundedY(), start.getRoundedZ()));
		if (Double.isNaN(get2DSlope()) || Double.isNaN(get2DSlopeForZ())) {
			if (isPointValid(new ThreeDimensionalVector(x, start.getY(), start.getZ()))) {
				int startY = isPointValid(new ThreeDimensionalVector(x, start.getRoundedY(), start.getZ())) ? start.getRoundedY() : (int)Math.ceil(start.getY());
				for (int y = startY; y <= end.getRoundedY(); y++) {
					int startZ = isPointValid(new ThreeDimensionalVector(x, y, start.getRoundedZ())) ? start.getRoundedZ() : (int)Math.ceil(start.getZ());
					for (int z = startZ; z <= end.getRoundedZ(); z++) {
						if (isPointValid(new ThreeDimensionalVector(x, y, z)))
							vectors.add(new ThreeDimensionalVector(x, y, z));
					}
				}
			}
		} else {
			while (!(x > end.getRoundedX())) {
				x++;
				vectors.add(find3DPoint(x));
			}
		}
		return vectors;
	}
	
	private ThreeDimensionalVector[] getSorted3DCoords() {
		ThreeDimensionalVector[] array = new ThreeDimensionalVector[2];
		if (start.getX() < end.getX()) {
			array[0] = start;
			array[1] = end;
		} else {
			array[0] = end;
			array[1] = start;
		}
		return array;
	}
	
	/**
	 * Gets the length of the line
	 * @return The length
	 */
	public double getLength() {
		return start.distanceTo(end);
	}
	
	/**
	 * Creates a new line as a copy of this line but with the set start coord
	 * @param coord The new start coord
	 * @return The new, modified line
	 */
	public Line setStart(TwoDimensionalVector coord) {
		return setStart(coord.to3D());
	}
	
	/**
	 * Creates a new line as a copy of this line but with the set start coord
	 * @param coord The new start coord
	 * @return The new, modified line
	 */
	public Line setStart(ThreeDimensionalVector coord) {
		return new Line(coord, end);
	}
	
	/**
	 * Creates a new line as a copy of this line but with the set end coord
	 * @param coord The new end coord
	 * @return The new, modified line
	 */
	public Line setEnd(TwoDimensionalVector coord) {
		return setEnd(coord.to3D());
	}
	
	/**
	 * Creates a new line as a copy of this line but with the set end coord
	 * @param coord The new end coord
	 * @return The new, modified line
	 */
	public Line setEnd(ThreeDimensionalVector coord) {
		return new Line(coord, end);
	}
	
	/**
	 * Rotates the line around a point
	 * @param point The point to rotate around
	 * @param angle The angle to rotate by
	 * @return The new line
	 */
	public Line rotate2D(TwoDimensionalVector point, double angle) {
		TwoDimensionalVector newStart = start.to2D().rotate(point, angle);
		TwoDimensionalVector newEnd = end.to2D().rotate(point, angle);
		return new Line(newStart, newEnd);
	}
	
	/**
	 * Rotates the line about the origin
	 * @param angle The angle to rotate by
	 * @return The new line
	 */
	public Line rotate2D(double angle) {
		return rotate2D(new TwoDimensionalVector(0,0), angle);
	}
	
	/**
	 * Rotates the line across the x-axis around a point
	 * @param point The point to rotate around
	 * @param angle The angle to rotate by
	 * @return The new line
	 */
	public Line rotate3DX(ThreeDimensionalVector point, double angle) {
		ThreeDimensionalVector newStart = start.rotateX(point, angle);
		ThreeDimensionalVector newEnd = end.rotateX(point, angle);
		return new Line(newStart, newEnd);
	}
	
	/**
	 * Rotates the line across the x-axis about the origin
	 * @param angle The angle to rotate by
	 * @return The new line
	 */
	public Line rotate3DX(double angle) {
		return rotate3DX(new ThreeDimensionalVector(0, 0, 0), angle);
	}
	
	/**
	 * Rotates the line across the y-axis around a point
	 * @param point The point to rotate around
	 * @param angle The angle to rotate by
	 * @return The new line
	 */
	public Line rotate3DY(ThreeDimensionalVector point, double angle) {
		ThreeDimensionalVector newStart = start.rotateY(point, angle);
		ThreeDimensionalVector newEnd = end.rotateY(point, angle);
		return new Line(newStart, newEnd);
	}
	
	/**
	 * Rotates the line across the y-axis about the origin
	 * @param angle The angle to rotate by
	 * @return The new line
	 */
	public Line rotate3DY(double angle) {
		return rotate3DY(new ThreeDimensionalVector(0, 0, 0), angle);
	}
	
	/**
	 * Rotates the line across the z-axis around a point
	 * @param point The point to rotate around
	 * @param angle The angle to rotate by
	 * @return The new line
	 */
	public Line rotate3DZ(ThreeDimensionalVector point, double angle) {
		ThreeDimensionalVector newStart = start.rotateZ(point, angle);
		ThreeDimensionalVector newEnd = end.rotateZ(point, angle);
		return new Line(newStart, newEnd);
	}
	
	/**
	 * Rotates the line across the z-axis about the origin
	 * @param angle The angle to rotate by
	 * @return The new line
	 */
	public Line rotate3DZ(double angle) {
		return rotate3DZ(new ThreeDimensionalVector(0, 0, 0), angle);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Line)
			return ((Line) other).start.equals(start) && ((Line) other).end.equals(end);
		return false;
	}
	
	@Override
	public String toString() {
		return "Line(X1:"+getStartX()+" Y1:"+getStartY()+" Z1:"+getStartZ()+"X2:"+getEndX()+" Y2:"+getEndY()+" Z2:"+getEndZ()+")";
	}
}
