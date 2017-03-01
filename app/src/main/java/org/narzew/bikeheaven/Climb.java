package org.narzew.bikeheaven;

public class Climb {
	
	int id;
	String name;
	String slope;
	String description;
	String author;
	double points;
	double start_x;
	double start_y;
	double end_x;
	double end_y;
	
	Climb(int id, String name, String slope,  String description, String author, double points, double start_x, double start_y,
	double end_x, double end_y){
		this.id = id;
		this.name = name;
		this.slope = slope;
		this.description = description;
		this.author = author;
		this.points = points;
		this.start_x = start_x;
		this.start_y = start_y;
		this.end_x = end_x;
		this.end_y = end_y;
	}
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getSlope(){
		return slope;
	}
	
	public String getDescription(){
		return description;
	}
	
	public String getAuthor(){
		return author;
	}
	
	public double getPoints(){
		return points;
	}
	
	public double getStartX(){
		return start_x;
	}
	
	public double getStartY(){
		return start_y;
	}
	
	public double getEndX(){
		return end_x;
	}
	
	public double getEndY(){
		return end_y;
	}
	
	public String get_category(){
		if(points>=700){
			return "HC";
		} else if(points>=400){
			return "1+";
		} else if(points>=260){
			return "1";
		} else if(points>=160){
			return "2";
		} else if(points>=100){
			return "3";
		} else if(points>=60){
			return "4";
		} else if(points>=30){
			return "5";
		} else if(points>=15){
			return "6";
		} else {
			return "7";
		}
	}
	
}
